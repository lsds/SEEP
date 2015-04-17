#define _HASH_FUNCTIONS_  5

#define _stash_ 100

#define EMPTY_KEY (0)

inline int __s_major_hash (int x, int y, int k) {
    const unsigned int prime = 2147483647U;
    unsigned int xl = convert_uint(x);
    unsigned int yl = convert_uint(y);
    unsigned int kl = convert_uint(k);
    unsigned int result = ((xl ^ kl) + yl) % prime;
    return convert_int(result);
}

inline int __s_minor_hash (int x, int y, int k) {
    const unsigned int stash_size = 101U;
    unsigned int xl = convert_uint(x);
    unsigned int yl = convert_uint(y);
    unsigned int kl = convert_uint(k);
    unsigned int result = ((xl ^ kl) + yl) % stash_size;
    return convert_int(result);
}

inline int getNextLocation(int size,
                           int  key,
                           int prev,
           __global const int*    x,
           __global const int*    y){

	/* Identify possible locations for key */
	int locations[_HASH_FUNCTIONS_];
	int i;
	#pragma unroll
	for (i = 0; i < _HASH_FUNCTIONS_; i++) {
		locations[i] = __s_major_hash (x[i], y[i], key) % size;
	}

	int next = locations[0];
	#pragma unroll
	for (i = _HASH_FUNCTIONS_ - 2; i >= 0; --i) {
		next = (prev == locations[i] ? locations[i+1] : next);
	}
	return next;
}

__kernel void aggregateKernel (
	const int tuples,
	const int _bundle,
	const int bundles,
	const int _table_,
	const int __stash_x,
	const int __stash_y,
	const int max_iterations,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global const int* x,
	__global const int* y,
	__global uchar* contents,
	__global int* stashed,
	__global int* failed,
	__global int* attempts,
	__global int* indices,
	__global int* offsets,
	__global int* partitions,
	__global uchar* output,
	__local int* buffer
	) {

	int lid = (int) get_local_id   (0);
	int gid = (int) get_group_id   (0);
	int lgs = (int) get_local_size (0); /* Local group size */

	int table_offset = gid * (_table_ + _stash_);
	int group_offset = lgs * sizeof(input_t);
	int offset_ =  window_ptrs_ [gid]; /* Window start and end pointers */
	int _offset = _window_ptrs  [gid];

	/* For debugging purposes only */ /* Tuples/group */
	int tpg = (_offset - offset_ + 1) / sizeof(input_t);

	int idx =  lid * sizeof(input_t) + offset_;
	int bundle = 0;
	int tuple_id = 0;

	if (lid == 0) {
		failed [gid] = 0;
		stashed[gid] = 0;
	}
	barrier(CLK_LOCAL_MEM_FENCE);

	while (idx < _offset) {

		/* For debugging purposes only */ /* Tuple id */
		tuple_id = lid + bundle * lgs + tpg * gid;

		bool success = true;
		bool inserted = false;
		int iterations = 0;
		__global input_t *p = (__global input_t *) &input[idx];
		/* Insert into hash table */
		int _k, k_ = pack_key (p);
		int pos = __s_major_hash(x[0], y[0], k_) % _table_;
		for (int i = 1; i < max_iterations; i++) {
			_k = atomic_xchg (&indices[table_offset + pos], k_);
			if (_k == EMPTY_KEY || _k == k_) {
				iterations = i;
				inserted = true;
				break ;
			}
			k_ = _k;
			pos = getNextLocation(_table_, k_, pos, x, y);
		}
		if (inserted == false) {
			/* Insert into stash */
			int slot = __s_minor_hash (__stash_x, __stash_y, k_);
			_k = atomic_cmpxchg (&indices[table_offset + _table_ + slot],
				EMPTY_KEY, k_);
			if (_k != EMPTY_KEY)
				success = false;
			else
				atomic_inc(&stashed[gid]);
		}
		if (success == false)
			atomic_inc(&failed[gid]);
		attempts[tuple_id] = (iterations > 0) ? iterations : max_iterations;

		idx += group_offset;
		bundle += 1;
	}

	barrier (CLK_LOCAL_MEM_FENCE);

	if (failed[gid] > 0)
		return ;

	/* Second phase */

	/*
	 * For each hash table:
	 * - lookup the position of each tuple;
	 * - write to contents[pos + table_offset]
	 *   (values are added or incremented atomically);
	 *
	 * - Prefix sum
	 * - Propagate sum across work groups
	 * - Compress
	 */

	int locations[_HASH_FUNCTIONS_];

	idx =  lid * sizeof(input_t) + offset_;
	while (idx < _offset) {
		__global input_t *p = (__global input_t *) &input[idx];
		/* Search hash table */
		int q = pack_key (p);
		for (int i = 0; i < _HASH_FUNCTIONS_; i++) {
			locations[i] = __s_major_hash(x[i], y[i], q) % _table_;
		}
		/* Check locations */
		int pos = locations[0];
		int key = indices[pos];
		#pragma unroll
		for (int i = 1; i < _HASH_FUNCTIONS_; i++) {
			if (key != q && key != EMPTY_KEY) {
				/* Retry */
				pos = locations[i];
				key = indices[pos];
			}
		}
		/* Check stashed keys */
		if (key != q) {
			pos = _table_ + __s_minor_hash(__stash_x, __stash_y, q);
		}
		/* Assume position found; worst case, the key was stashed */
		int output_idx = (table_offset + pos) * sizeof(output_t);
		__global intermediate_t *out = (__global intermediate_t *) &contents[output_idx];
		storef(out, p);
		idx += group_offset;
	}
}

/* Scan based on the implementation of [...] */

/*
 * Up-sweep (reduce) on a local array `data` of length `length`.
 * `length` must be a power of two.
 */
inline void upsweep (__local int *data, int length) {

	int lid  = get_local_id (0);
	int b = (lid * 2) + 1;
	int depth = 1 + (int) log2 ((float) length);

	for (int d = 0; d < depth; d++) {

		barrier(CLK_LOCAL_MEM_FENCE);
		int mask = (0x1 << d) - 1;
		if ((lid & mask) == mask) {

			int offset = (0x1 << d);
			int a = b - offset;
			data[b] += data[a];
		}
	}
}

/*
 * Down-sweep on a local array `data` of length `length`.
 * `length` must be a power of two.
 */
inline void downsweep (__local int *data, int length) {

	int lid = get_local_id (0);
	int b = (lid * 2) + 1;
	int depth = (int) log2 ((float) length);
	for (int d = depth; d >= 0; d--) {

		barrier(CLK_LOCAL_MEM_FENCE);
		int mask = (0x1 << d) - 1;
		if ((lid & mask) == mask) {

			int offset = (0x1 << d);
			int a = b - offset;
			int t = data[a];
			data[a] = data[b];
			data[b] += t;
		}
	}
}

/* Scan */
inline void scan (__local int *data, int length) {

	int lid = get_local_id (0);
	int lane = (lid * 2) + 1;

	upsweep (data, length);

	if (lane == (length - 1))
		data[lane] = 0;

	downsweep (data, length);
	return ;
}

/*
 * Assumes:
 *
 * - N tuples
 * - Every thread handles two tuples, so #threads = N / 2
 * - L threads/group
 * - Every thread group handles (2 * L) tuples
 * - Let, W be the number of groups
 * - N = 2L * W => W = N / 2L
 *
 */

__kernel void scanKernel (
	const int tuples,
	const int bundle_,
	const int bundles,
	const int _table_,
	const int __stash_x,
	const int __stash_y,
	const int max_iterations,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global const int* x,
	__global const int* y,
	__global uchar* contents,
	__global int* stashed,
	__global int* failed,
	__global int* attempts,
	__global int* indices, /* Flags */
	__global int* offsets,
	__global int* partitions,
	__global uchar* output,
	__local int* buffer
)
{
	int lgs = get_local_size (0);
	int tid = get_global_id (0);

	int  left = (2 * tid);
	int right = (2 * tid) + 1;

	int lid = get_local_id(0);

	/* Local memory indices */
	int  _left = (2 * lid);
	int _right = (2 * lid) + 1;

	int gid = get_group_id (0);
	/* A thread group processes twice as many tuples */
	int L = 2 * lgs;

#ifdef HAVING_CLAUSE
	const int lp = left * sizeof(output_t);
	__global intermediate_t *lx = (__global intermediate_t *) &contents[lp];

	const int rp = right * sizeof(output_t);
	__global intermediate_t *rx = (__global intermediate_t *) &contents[rp];

	indices[ left] = (indices[ left] == EMPTY_KEY) ? 0 : selectf(lx);
	indices[right] = (indices[right] == EMPTY_KEY) ? 0 : selectf(rx);
#else
	indices[ left] = (indices[ left] == EMPTY_KEY) ? 0 : 1;
	indices[right] = (indices[right] == EMPTY_KEY) ? 0 : 1;
#endif
	/* Copy flag to local memory */

	/* Checking left and right:
	 *
	 * There is no need to check `left` and `right` indices
	 * since the boundaries have been checked by the host.
	 */
	buffer[ _left] = indices[ left];
	buffer[_right] = indices[right];

	upsweep(buffer, L);

	if (lid == (lgs - 1)) {
		partitions[gid] = buffer[_right];
		buffer[_right] = 0;
	}

	downsweep(buffer, L);

	/* Write results to global memory */
	offsets[ left] = buffer[ _left];
	offsets[right] = buffer[_right];
}

__kernel void compactKernel (
	const int tuples,
	const int bundle_,
	const int bundles,
	const int _table_,
	const int __stash_x,
	const int __stash_y,
	const int max_iterations,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global const int* x,
	__global const int* y,
	__global uchar* contents,
	__global int* stashed,
	__global int* failed,
	__global int* attempts,
	__global int* indices, /* Flags */
	__global int* offsets,
	__global int* partitions,
	__global uchar* output,
	__local int* buffer
) {

	int tid = get_global_id (0);

	int  left = (2 * tid);
	int right = (2 * tid) + 1;

	int lid = get_local_id (0);

	int gid = get_group_id (0);

	/* Compute pivot value */
	__local int pivot;
	if (lid == 0) {
		pivot = 0;
		if (gid > 0) {
			for (int i = 0; i < gid; i++) {
				pivot += (partitions[i]);
			}
		}
	}
	barrier(CLK_LOCAL_MEM_FENCE);

	/* Compact left and right */
	if (indices[left] != EMPTY_KEY) {

		const int lq = (offsets[left] + pivot) * sizeof(output_t);
		const int lp = left * sizeof(output_t);
		indices[left] = lq + sizeof(output_t);
		__global intermediate_t *lx = (__global intermediate_t *) &contents[lp];
		__global output_t *ly = (__global output_t *) &output[lq];
		/* ly->vectors[0] = lx->vectors[0]; */
		ly->tuple.t  = 0L;
		ly->tuple._1 = __bswap32(lx->tuple.key);
		ly->tuple._2 = __bswapfp(convert_float(lx->tuple.val));
	}

	if (indices[right] != EMPTY_KEY) {

		const int rq = (offsets[right] + pivot) * sizeof(output_t);
		const int rp = right * sizeof(output_t);
		indices[right] = rq + sizeof(output_t);
		__global intermediate_t *rx = (__global intermediate_t *) &contents[rp];
		__global output_t *ry = (__global output_t *) &output[rq];
		/* ry->vectors[0] = rx->vectors[0]; */
		ry->tuple.t  = 0L;
		ry->tuple._1 = __bswap32(rx->tuple.key);
		ry->tuple._2 = __bswapfp(convert_float(rx->tuple.val));
	}
}

__kernel void clearKernel (
	const int tuples,
	const int bundle_,
	const int bundles,
	const int _table_,
	const int __stash_x,
	const int __stash_y,
	const int max_iterations,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global const int* x,
	__global const int* y,
	__global uchar* contents,
	__global int* stashed,
	__global int* failed,
	__global int* attempts,
	__global int* indices, /* Flags */
	__global int* offsets,
	__global int* partitions,
	__global uchar* output,
	__local int* buffer
) {
	int tid = get_global_id (0);
	indices[tid] = EMPTY_KEY;
	offsets[tid] = -1;
	__global intermediate_t *c = (__global intermediate_t *) &contents[tid * sizeof(intermediate_t)];
	clearf(c);
}

