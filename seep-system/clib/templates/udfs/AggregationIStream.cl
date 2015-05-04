#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable
#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable

#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#include "byteorder.h"

#define _HASH_FUNCTIONS_  5

#define _stash_ 100

#define EMPTY_KEY (0)

#define  INPUT_VECTOR_SIZE 2
#define OUTPUT_VECTOR_SIZE 2

typedef struct {
	long t;
	int _1; /* The key */
	float _2;
	int _2;
	int _3;
	int _4;
	int _5;
} input_tuple_t  __attribute__((aligned(1)));

typedef union {
	input_tuple_t tuple;
	uchar16 vectors [INPUT_VECTOR_SIZE];
} input_t;

#define output_t input_t
#define intermediate_t input_t

inline int pack_key (__global input_t *p) {
	int key = __bswap32(p->tuple._1);
	return key;
}

inline void storef (__global intermediate_t *q, __global input_t *p, int idx) {
	/*
	 * We want to maintain the most recent tuple for a key.
	 *
	 * There are two ways to do this, based on time stamps
	 * and based on input buffer indices. The larger value
	 * replaces the previous value. We choose the latter.
	 */
	long lidx = convert_long(idx);
	long prev = atom_max((global long *) &(q->tuple.t), lidx);
	if (prev == lidx) {
		/* Store the most recent tuple */
		q->vectors[0] = p->vectors[0];
		q->vectors[1] = p->vectors[1];
	}
	return ;
}

inline void clearf (__global intermediate_t *p) {
    p->vectors[0] = 0;
    p->vectors[1] = 0;
}

inline void copyf (__global intermediate_t *p, __global output_t *q) {
	q->vectors[0] = p->vectors[0];
	q->vectors[1] = p->vectors[1];
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
	const int dummyParam1,
	const int dummyParam2,
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

	/* For debugging purposes only, tuples/group
	 *
	 * int tpg = (_offset - offset_ + 1) / sizeof(input_t);
	 */
	int idx =  lid * sizeof(input_t) + offset_;
	int bundle = 0;
	/* int tuple_id = 0; */

	if (lid == 0) {
		failed [gid] = 0;
		stashed[gid] = 0;
	}
	barrier(CLK_LOCAL_MEM_FENCE);

	while (idx < _offset) {

		/* For debugging purposes only
		 *
		 * tuple_id = lid + bundle * lgs + tpg * gid;
		 */
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
		/* attempts[tuple_id] = (iterations > 0) ? iterations : max_iterations; */

		idx += group_offset;
		bundle += 1;
	}

	barrier (CLK_LOCAL_MEM_FENCE);

	if (failed[gid] > 0)
		return ;

	/* Second phase */

	/*
	 * For each hash table:
	 * - lookup the position of each tuple; and
	 * - write to contents[pos + table_offset]
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
		storef(out, p, idx);
		idx += group_offset;
	}
}

__kernel void intersectKernel (
	const int tuples,
	const int dummyParam1,
	const int dummyParam2,
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

	/*
	 * The first window is used to reconstruct the initial
	 * state of this query based on the last window of the
	 * previously processed batch.
	 *
	 * It does not produce any results.
	 */
	if (gid < 1)
		return;

	int curr_table_offset = gid * (_table_ + _stash_);
	int group_offset = lgs * sizeof(input_t);
	int offset_ =  window_ptrs_ [gid]; /* Window start and end pointers */
	int _offset = _window_ptrs  [gid];

	/* Get the previous window */
	int _gid = gid - 1;
	int prev_table_offset = _gid * (_table_ + _stash_);
	/*
	 * Ideally, if a vehicle `vid` is stored at slot `i` in this table, then
	 * it would be stored at slot `i` in the table of the previous window as
	 * well. However, due to cuckoo hashing this is not true. So, we have to
	 * lookup every vehicle in the previous table as well.
	 *
	 * The possible `locations`, though, should be the same for both tables.
	 */
	int locations[_HASH_FUNCTIONS_];
	idx =  lid * sizeof(input_t) + offset_;
	while (idx < _offset) {
		__global input_t *p = (__global input_t *) &input[idx];
		int q = pack_key (p);
		/* Search current hash table */
		for (int i = 0; i < _HASH_FUNCTIONS_; i++) {
			locations[i] = __s_major_hash(x[i], y[i], q) % _table_;
		}
		/* Check locations */
		int currPos = locations[0];
		int currkey = indices[currPos];
#pragma unroll
		for (int i = 1; i < _HASH_FUNCTIONS_; i++) {
			if (currKey != q && currKey != EMPTY_KEY) {
				/* Retry */
				currPos = locations[i];
				currKey = indices[currPos];
			}
		}
		/* Check stashed keys */
		if (currKey != q) {
			currPos = _table_ + __s_minor_hash(__stash_x, __stash_y, q);
		}
		/* Assume position found; worst case, the key was stashed */
		int curr_output_idx = (curr_table_offset + currPos) * sizeof(output_t);
		__global intermediate_t *currOut = (__global intermediate_t *) &contents[curr_output_idx];
		/*
		 * Now search the hash table of the previous window
		 */
		int prevPos = locations[0];
		int prevkey = indices[prevPos];
#pragma unroll
		for (int i = 1; i < _HASH_FUNCTIONS_; i++) {
			if (prevKey != q && prevKey != EMPTY_KEY) {
				prevPos = locations[i];
				prevKey = indices[prevPos];
			}
		}
		if (prevKey != q) {
			prevPos = _table_ + __s_minor_hash(__stash_x, __stash_y, q);
		}
		int prev_output_idx = (prev_table_offset + prevPos) * sizeof(output_t);
		__global intermediate_t *prevOut = (__global intermediate_t *) &contents[prev_output_idx];
		/*
		 * Compare the two entries, `currOut` and `prevOut`. If they refer to the same tuple,
		 * then do emit the tuple in the current window.
		 */
		indices[currPos] = (prevOut->tuple.t == currOut->tuple.t) ? EMPTY_KEY : indices[currPos];
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
	const int dummyParam1,
	const int dummyParam2,
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

	indices[ left] = (indices[ left] == EMPTY_KEY) ? 0 : 1;
	indices[right] = (indices[right] == EMPTY_KEY) ? 0 : 1;
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
	const int dummyParam1,
	const int dummyParam2,
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

	/*
	 * The first window is used to reconstruct the initial
	 * state of this query based on the last window of the
	 * previously processed batch.
	 *
	 * It does not produce any results.
	 */
	if (gid < 1)
		return;
	/* Compute pivot value */
	__local int pivot;
	if (lid == 0) {
		pivot = 0;
		if (gid > 1) {
			for (int i = 1; i < gid; i++) {
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
		copyf (lx, ly);
	}

	if (indices[right] != EMPTY_KEY) {

		const int rq = (offsets[right] + pivot) * sizeof(output_t);
		const int rp = right * sizeof(output_t);
		indices[right] = rq + sizeof(output_t);
		__global intermediate_t *rx = (__global intermediate_t *) &contents[rp];
		__global output_t *ry = (__global output_t *) &output[rq];
		copyf (rx, ry);
	}
}

__kernel void clearKernel (
	const int tuples,
	const int dummyParam1,
	const int dummyParam2,
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

