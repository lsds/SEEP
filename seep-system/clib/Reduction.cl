#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#include "byteorder.h"

#define  INPUT_VECTOR_SIZE 2
#define OUTPUT_VECTOR_SIZE 1

typedef struct {
	long t;
	int _1;
	int _2;
	float _3;
	int _4;
	long _5;
} input_tuple_t  __attribute__((aligned(1)));

typedef union {
	input_tuple_t tuple;
	uchar16 vectors [INPUT_VECTOR_SIZE];
} input_t;

typedef struct {
	long t;
	float _1;
	int pad;
} output_tuple_t  __attribute__((aligned(1)));

typedef union {
	output_tuple_t tuple;
	uchar16 vectors [OUTPUT_VECTOR_SIZE];
} output_t;

#define INITIAL_VALUE 0 /* FLT_MIN, FLT_MAX */

inline float sum (float p, float q, int n) {
	(void) n;
	return (p + q);
}

inline float cnt (float p, float q, int n) {
	(void) n;
	(void) q;
	return p + 1;
}

inline float _min (float p, float q, int n) {
	(void) n;
	return (p < q) ? p : q;
}

inline float _max (float p, float q, int n) {
	(void) n;
	return (p > q) ? p : q;
}

inline float _avg (float p, float q, int n) {
	return (n * p + q) / (n + 1);
}

inline float reducef (float p, float q, int n) {
	
	return _avg (p, q, n);
}

inline float mergef (float p, float q, int n) {

	return _avg (p, q, n);
}

__kernel void reduceKernel (
	const int tuples,
	const int bytes,
	__global const uchar *input,
	__global const int *window_ptrs_,
	__global const int *_window_ptrs,
	__global uchar *output,
	__local float *scratch
) {
	int lid = (int) get_local_id   (0);
	int gid = (int) get_group_id   (0);
	int lgs = (int) get_local_size (0); /* Local group size */

	int group_offset = lgs * sizeof(input_t);
	int offset_ =  window_ptrs_ [gid]; /* Window start and end pointers */
	int _offset = _window_ptrs  [gid];
	int idx =  lid * sizeof(input_t) + offset_;

	float value = INITIAL_VALUE;
	int count = 0;
	/* The sequential part */
	while (idx < _offset && idx < bytes) {
		/* Get tuple from main memory */
		__global input_t *p = (__global input_t *) &input[idx];
		float attr = __bswapfp(p->tuple._3);
		value = reducef (value, attr, count);
		idx += group_offset;
		count += 1;
	}
	/* Write value to scratch memory */
	scratch[lid] = value;
	barrier(CLK_LOCAL_MEM_FENCE);
	count = 0;
	/* Parallel reduction */
	for (int pos = lgs / 2; pos > 0; pos = pos / 2) {
		if (lid < pos) {
			float mine = scratch[lid];
			float other = scratch[lid + pos];
			scratch[lid] = mergef (mine, other, count);
			count += 1;
		}
		barrier(CLK_LOCAL_MEM_FENCE);
	}
	/* Write result */
	if (lid == 0) {
		__global output_t *result =
			(__global output_t *) &output[gid * sizeof(output_t)];

		result->tuple.t = 0L; // p->tuple.t;
		result->tuple._1 = __bswapfp(scratch[lid]);
		result->tuple.pad = 0;
	}
	return ;
}
