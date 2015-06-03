#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable

#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#include "/Users/akolious/SEEP/seep-system/clib/byteorder.h"
#define  INPUT_VECTOR_SIZE 2
#define OUTPUT_VECTOR_SIZE 2

typedef struct {
	long t;
	int _1;
	int _2;
	int _3;
	int _4;
	int _5;
	int _6;
} input_tuple_t __attribute__((aligned(1)));

typedef union {
	input_tuple_t tuple;
	uchar16 vectors[2];
} input_t;

typedef struct {
	long t;
	int _1;
	int _2;
	int _3;
	int _4;
	int _5;
	int _6;
} output_tuple_t __attribute__((aligned(1)));

typedef union {
	output_tuple_t tuple;
	uchar16 vectors[2];
} output_t;


inline void copyf (__global input_t *p, __global output_t *q) {
	q->vectors[0] = p->vectors[0];
	q->vectors[1] = p->vectors[1];
}


__kernel void dummyKernel (
	__global const uchar *input,
	__global uchar *output
) 
{
	int tid = get_global_id (0);
	__global  input_t *p = (__global  input_t *)  &input[tid * sizeof( input_t)];
	__global output_t *q = (__global output_t *) &output[tid * sizeof(output_t)];
	/* Copy p into q */
	copyf (p, q);
	return;
}
