#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#include "byteorder.h"

#define  INPUT_VECTOR_SIZE 2
#define OUTPUT_VECTOR_SIZE 4

typedef struct {
	long   t;
	float _1;
	int   _2;
	int   _3;
	int   _4;
	long  _5;
} _s1_input_tuple_t __attribute__((aligned(1)));

typedef union {
	_s1_input_tuple_t tuple;
	uchar16 vectors [INPUT_VECTOR_SIZE];
} _s1_input_t;

typedef struct {
    long   t;
    float _1;
    int   _2;
    int   _3;
    int   _4;
    long  _5;
} _s2_input_tuple_t __attribute__((aligned(1)));

typedef union {
    _s2_input_tuple_t tuple;
    uchar16 vectors [INPUT_VECTOR_SIZE];
} _s2_input_t;

typedef struct {
	long t;
	/* Attributes from first stream */
	float _1_1;
	int   _1_2;
	int   _1_3;
	int   _1_4;
	long  _1_5;
	/* Attributes from second stream */
	float _2_1;
	int   _2_2;
	int   _2_3;
	int   _2_4;
	long  _2_5;
	long   pad; /* 64 bytes in total */
} output_tuple_t  __attribute__((aligned(1)));

typedef union {
	output_tuple_t tuple;
	uchar16 vectors [OUTPUT_VECTOR_SIZE];
} output_t;

inline int selectf(__global _s1_input_t *p1, __global _s2_input_t *p2) {
	int value1  = __bswap32(p1->tuple._2);
	int value2  = __bswap32(p2->tuple._2);
	int result = 0;
	if (value1 == value2) 
		result = 1;
	return result;
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

__kernel void joinKernel (
	const int __s1_tuples,
	const int __s2_tuples,
	__global const uchar *__s1_input,
	__global const uchar *__s2_input,
	__global const int *window_ptrs_,
	__global const int *_window_ptrs,
	__global int *flags,
	__global int *offsets,
	__global int *partitions,
	__global uchar *output,
	__local int *x
) {
	int tid = get_global_id (0);
	if (tid >= __s1_tuples)
		return ;
	const int lidx = tid * sizeof(_s1_input_t);
	__global _s1_input_t *lp = (__global _s1_input_t *) &__s1_input[lidx];
	/* Scan second stream */
	const int offset_ = window_ptrs_[tid];
	const int _offset = _window_ptrs[tid];
	int idx = tid * __s2_tuples;
	int i;
	for (i = 0; i < __s2_tuples; i++) {
		const int ridx = i * sizeof(_s2_input_t);
		if (ridx < offset_ || ridx > _offset)
			return;
		/* Read tuple from second stream */
		__global _s2_input_t *rp = (__global _s2_input_t *) &__s2_input[ridx];
		flags[idx] = selectf (lp, rp);
		idx += 1;
	}
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
	const int __s1_tuples,
	const int __s2_tuples,
	__global const uchar *__s1_input,
	__global const uchar *__s2_input,
	__global const int *window_ptrs_,
	__global const int *_window_ptrs,
	__global int *flags,
	__global int *offsets,
	__global int *partitions,
	__global uchar *output,
	__local int *x
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
	
	int _product = __s1_tuples * __s2_tuples;
	
	/* Copy flag to local memory */
	x[ _left] = (left  < _product) ? flags[ left] : 0;
	x[_right] = (right < _product) ? flags[right] : 0;
	
	upsweep(x, L);
	
	if (lid == (lgs - 1)) {
		partitions[gid] = x[_right];
		x[_right] = 0;
	}
	
	downsweep(x, L);
	
	/* Write results to global memory */
	offsets[ left] = ( left < _product) ? x[ _left] : -1;
	offsets[right] = (right < _product) ? x[_right] : -1;
}

__kernel void compactKernel (
	const int __s1_tuples,
	const int __s2_tuples,
	__global const uchar *__s1_input,
	__global const uchar *__s2_input,
	__global const int *window_ptrs_,
	__global const int *_window_ptrs,
	__global int *flags,
	__global int *offsets,
	__global int *partitions,
	__global uchar *output,
	__local int *x
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
	
	int _product = __s1_tuples * __s2_tuples;
	
	/* Compact left and right */
	if (left < _product && flags[left] == 1) {
		
		const int lq = (offsets[left] + pivot) * sizeof(output_t);
		/* Merge two tuples */
		const int s1idx = left / __s2_tuples;
		const int s2idx = left % __s2_tuples;
		
		const int lp1 = s1idx * sizeof(_s1_input_t);
		const int lp2 = s2idx * sizeof(_s2_input_t);
		
		flags[left] = lq + sizeof(output_t);
		
		__global _s1_input_t *lx1 = (__global _s1_input_t *) &__s1_input[lp1];
		__global _s2_input_t *lx2 = (__global _s2_input_t *) &__s2_input[lp2];
		
		__global output_t *ly = (__global output_t *) & output[lq];
		
		ly->vectors[0] = lx1->vectors[0];
		ly->vectors[1] = lx1->vectors[1];
		
		ly->tuple._2_1 = lx2->tuple._1;
		ly->tuple._2_2 = lx2->tuple._2;
		ly->tuple._2_3 = lx2->tuple._3;
		ly->tuple._2_4 = lx2->tuple._4;
		ly->tuple._2_5 = lx2->tuple._5;
		ly->tuple.pad  = 0;
	}
	
	if (right < _product && flags[right] == 1) {
		
		const int rq = (offsets[right] + pivot) * sizeof(output_t);
		/* Merge two tuples */
		const int s1idx = right / __s2_tuples;
		const int s2idx = right % __s2_tuples;
		
		const int rp1 = s1idx * sizeof(_s1_input_t);
		const int rp2 = s2idx * sizeof(_s2_input_t);
		
		flags[right] = rq + sizeof(output_t);
		
		__global _s1_input_t *rx1 = (__global _s1_input_t *) &__s1_input[rp1];
		__global _s2_input_t *rx2 = (__global _s2_input_t *) &__s2_input[rp2];
		
		__global output_t *ry = (__global output_t *) & output[rq];
		ry->vectors[0] = rx1->vectors[0];
		ry->vectors[1] = rx1->vectors[1];
		
		ry->tuple._2_1 = rx2->tuple._1;
		ry->tuple._2_2 = rx2->tuple._2;
		ry->tuple._2_3 = rx2->tuple._3;
		ry->tuple._2_4 = rx2->tuple._4;
		ry->tuple._2_5 = rx2->tuple._5;
		ry->tuple.pad  = 0;
	}
}
