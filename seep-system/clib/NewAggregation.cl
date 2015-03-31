#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable
#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable

#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#define T int
#define identity 0
#define indexof(x) x
#define apply(a,b) (a + b)

#define _MAX_ITERATIONS_  20
#define _HASH_FUNCTIONS_  5

#define _stash_ 100

#define EMPTY_KEY (0)

#define  INPUT_VECTOR_SIZE 2
#define OUTPUT_VECTOR_SIZE 1

typedef struct {
	long t;
	float _1; /* The aggregate */
	int _2;   /* The key */
	int _3;
	int _4;
	int _5;
	int _6;
} input_tuple_t  __attribute__((aligned(1)));

typedef union {
	input_tuple_t tuple;
	uchar16 vectors [INPUT_VECTOR_SIZE];
} input_t;

typedef struct {
	long t;
	int _1;   /* Key   */
	float _2; /* Value */
} output_tuple_t  __attribute__((aligned(1)));

typedef union {
	output_tuple_t tuple;
	uchar16 vectors [OUTPUT_VECTOR_SIZE];
} output_t;

inline int pack_key (__global input_t *p) {
	return p->tuple._2;
}

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
	__global int* part,
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
				atomic_inc(&stashed[0]);
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
	 * - write to contents[pos + table_offset] (values are added or incremented atomically);
	 * - Prefix sum
	 * - [TBD: propagate sum across work groups]
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
		__global output_t *out = (__global output_t *) &contents[output_idx];
		out->tuple.t = p->tuple.t;
		out->tuple._1 = key;
		atomic_inc((__global int *) (&contents[output_idx + 8 + 4]));
		
		idx += group_offset;
	}
}

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
* First phase of a multiblock scan.
*
* Given a global array [data] of length arbitrary length [n].
* We assume that we have k workgroups each of size m/2 workitems.
* Each workgroup handles a subarray of length [m] (where m is a power of two).
* The last subarray will be padded with 0 if necessary (n < k*m).
* We use the primitives above to perform a scan operation within each subarray.
* We store the intermediate reduction of each subarray (following upsweep_pow2) in [part].
* These partial values can themselves be scanned and fed into [scan_inc_subarrays].
*/


__kernel void scanKernel (
	const int tuples,
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
	__global int* indices, /* instead of flags */
	__global int* offsets,
	__global int* part,
	__global uchar* output,
	__local int* buffer
)
{
	int wx = get_local_size (0);
	// global identifiers and indexes
	int gid = get_global_id (0);
	int lane0 = (2*gid) ;
	int lane1 = (2*gid)+1;
	// local identifiers and indexes
	int lid = get_local_id(0);
	int local_lane0 = (2*lid) ;
	int local_lane1 = (2*lid)+1;
	int grpid = get_group_id(0);
	// list lengths
	int m = wx * 2;
	int k = get_num_groups(0);

//	/* Fetch tuple and apply selection filter */
//	const int idx1 = lane0 * sizeof(input_t);
//	const int idx2 = lane1 * sizeof(input_t);
//	__global input_t *p1 = (__global input_t *) &input[idx1];
//	__global input_t *p2 = (__global input_t *) &input[idx2];
//	flags[lane0] = selectf (p1);
//	flags[lane1] = selectf (p2);

	indices[lane0] = (indices[lane0] == EMPTY_KEY) ? 0 : 1;
	indices[lane1] = (indices[lane1] == EMPTY_KEY) ? 0 : 1;

//	flags[lane0] = 100;
//	flags[lane1] = 101;

	// copy into local data padding elements >= n with 0

	buffer[local_lane0] = (lane0 < tuples) ? indices[lane0] : 0;
	buffer[local_lane1] = (lane1 < tuples) ? indices[lane1] : 0;

	// buffer[local_lane0] = indices[lane0];
	// buffer[local_lane1] = indices[lane1];

	// ON EACH SUBARRAY
	// a reduce on each subarray
	upsweep(buffer, m);
	// last workitem per workgroup saves last element of each subarray in [part] before zeroing
	if (lid == (wx-1)) {
	part[grpid] = buffer[local_lane1];
	buffer[local_lane1] = 0;
	}
	// a sweepdown on each subarray
	downsweep(buffer, m);

	//  copy back to global data
	if (lane0 < tuples) {
		offsets[lane0] = buffer[local_lane0];
	}
	if (lane1 < tuples) {
		offsets[lane1] = buffer[local_lane1];
	}
}

/*
* Perform the second phase of an inplace exclusive scan on a global array [data] of arbitrary length [n].
*
* We assume that we have k workgroups each of size m/2 workitems.
* Each workgroup handles a subarray of length [m] (where m is a power of two).
* We sum each element by the sum of the preceding subarrays taken from [part].
*/

__kernel void compactKernel (
		const int tuples,
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
		__global int* indices, /* instead of flags */
		__global int* offsets,
		__global int* part,
		__global uchar* output,
		__local int* buffer
)
{

	// global identifiers and indexes
	int gid = get_global_id(0);
	int lane0 = (2*gid) ;
	int lane1 = (2*gid)+1;
	// local identifiers and indexes
	int lid = get_local_id(0);
	int local_lane0 = (2*lid) ;
	int local_lane1 = (2*lid)+1;
	int grpid = get_group_id(0);

	/* Compute pivot value */

	__local int pivot;
	if (lid == 0) {
		pivot = 0;
		if (grpid > 0) {
			for (int i = 1; i <= grpid; i++) {
				// int idx = i * _bundle - 1;
				pivot += (part[i]); //  + 1);
			}
		}
	}
	barrier(CLK_LOCAL_MEM_FENCE);

	if (indices[lane0] == 1) {
		const int q1 = (offsets[lane0] + pivot) * sizeof(output_t);
		const int p1 = lane0 * sizeof(output_t);
		indices[lane0] = offsets[lane0];
		__global output_t *x0 = (__global output_t *) & contents[p1];
		__global output_t *y0 = (__global output_t *) & output[q1];
		y0->vectors[0] = x0->vectors[0];
	}

	if (indices[lane1] == 1) {
		const int q2 = (offsets[lane1] + pivot) * sizeof(output_t);
		const int p2 = lane1 * sizeof(output_t);
		indices[lane1] = offsets[lane1];
		__global output_t *x1 = (__global output_t *) & contents[p2];
		__global output_t *y1 = (__global output_t *) & output[q2];
		y1->vectors[0] = x1->vectors[0];
	}
}
