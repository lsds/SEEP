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

inline T scan_exclusive
(
	__local T *input,
	size_t idx,
	const uint lane
)
{
	if (lane > 0) input[idx] = apply(input[indexof(idx - 1)], input[indexof(idx)]);
	if (lane > 1) input[idx] = apply(input[indexof(idx - 2)], input[indexof(idx)]);
	if (lane > 3) input[idx] = apply(input[indexof(idx - 4)], input[indexof(idx)]);
	if (lane > 7) input[idx] = apply(input[indexof(idx - 8)], input[indexof(idx)]);
	if (lane >15) input[idx] = apply(input[indexof(idx -16)], input[indexof(idx)]);
	return (lane > 0) ? input[idx - 1] : identity;
}

inline T scan_inclusive
(
	__local T *input,
	size_t idx,
	const uint lane
)
{
	if (lane > 0) input[idx] = apply(input[indexof(idx - 1)], input[indexof(idx)]);
	if (lane > 1) input[idx] = apply(input[indexof(idx - 2)], input[indexof(idx)]);
	if (lane > 3) input[idx] = apply(input[indexof(idx - 4)], input[indexof(idx)]);
	if (lane > 7) input[idx] = apply(input[indexof(idx - 8)], input[indexof(idx)]);
	if (lane >15) input[idx] = apply(input[indexof(idx -16)], input[indexof(idx)]);
	return input[idx];
}

inline T scan
(
	__local T *buffer,
	const uint idx,
	const uint lane,
	const uint wid /* wrap id */
)
{
	T value;

	/* Step 1: perform warp scan */
	value = scan_exclusive (buffer, idx, lane);

	barrier(CLK_LOCAL_MEM_FENCE);

	/* Step 2: collect per-warp partial results */
	if (lane > 30)
		buffer[wid] = buffer[idx];

	barrier(CLK_LOCAL_MEM_FENCE);

	/* Step 3: use 1st warp to scan per-warp results */
	if (wid < 1)
		scan_exclusive(buffer, idx, lane);

	barrier(CLK_LOCAL_MEM_FENCE);

	/* Step 4: accumulate results from step 1 and step 3 */
	if (wid > 0)
		value = apply(buffer[wid - 1], value);

	barrier(CLK_LOCAL_MEM_FENCE);

	/* Step 5: write and return the final result */
	buffer[idx] = value;

	barrier(CLK_LOCAL_MEM_FENCE);

	return value;
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
	
	barrier (CLK_LOCAL_MEM_FENCE);

	/* Next step: prefix sum */

	const uint lane = lid & 31;
	const uint wid  = lid >> 5; /* Wrap id, assuming 32 wraps */
	
	int ngroups = (int) get_num_groups(0);
	
	int _bundle = _table_ + _stash_;
	int bundles = _bundle / lgs;
	int items = _bundle * ngroups;

	T value = identity;
	for (int i = 0; i < bundles; i++) {
		const int offset = i * lgs + (gid * _bundle);
		const int pos = offset + lid;
		if (pos > items - 1)
			return;
		/* Step 0: apply filter */
		indices[pos] = (indices[pos] == EMPTY_KEY) ? 0 : 1;

		/* Step 1: read `tpb` elements from global to local memory */
		T _input = buffer[lid] = indices[pos];

		barrier(CLK_LOCAL_MEM_FENCE);

		/* Step 2: scan `lgs` elements */
		T v = scan (buffer, lid, lane, wid);

		/* Step 3: propagate result from previous block */
		v = apply (v, value);

		/* Step 4: write result to global memory */
		offsets[pos] = v;

		/* Step 5: choose next reduce value */
		if (lid == (lgs - 1))
			buffer[lid] = apply (_input, v);

		barrier(CLK_LOCAL_MEM_FENCE);

		value = buffer[lgs - 1];

		barrier(CLK_LOCAL_MEM_FENCE);
	}
	
	/* Compute pivot value */
	int pivot = 0;
	if (lid == 0 && gid > 0) {
		for (int i = 0; i < gid; i++) {
			idx = i * _bundle - 1;
			pivot += (offsets[idx] + 1);
		}
	}
}

__kernel void compactKernel
(
	const int _table_,
	__global uchar* contents,
	__global int* indices,
	__global int* offsets,
	__global uchar* output
	) {
	/* Populate indices */
	const size_t lid = get_local_id(0);
	const size_t gid = get_group_id(0);
	const size_t lgs = get_local_size(0); /* # threads/block */
	
	int ngroups = (int) get_num_groups(0);
	
	int _bundle = _table_ + _stash_;
	int bundles = _bundle / lgs;
	int items = _bundle * ngroups;

	/* Compute pivot value */
	__local int pivot;
	if (lid == 0) {
		pivot = 0;
		if (gid > 0) {
			for (int i = 1; i <= gid; i++) {
				int idx = i * _bundle - 1;
				pivot += (offsets[idx] + 1);
			}
		}
	}
	barrier(CLK_LOCAL_MEM_FENCE);

	for (int i = 0; i < bundles; i++) {
		const int offset = i * lgs + (gid * _bundle);
		const int pos = offset + lid; /* tuple index */
		if (pos >= items)
			return;
		/* Copy tuple `pos` into position `j` if index is positive */
		if (indices[pos] == 1) {
			const int q = (offsets[pos] + pivot) * sizeof(output_t);
			const int p = pos * sizeof(input_t);
			__global output_t *x = (__global output_t *) &contents[p];
			__global output_t *y = (__global output_t *)   &output[q];
			y->vectors[0] = x->vectors[0];
		}
	}
	return ;
}
