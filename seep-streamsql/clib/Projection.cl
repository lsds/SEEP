#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

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
} input_tuple_t  __attribute__((aligned(1)));

typedef union {
	input_tuple_t tuple;
	uchar16 vectors [INPUT_VECTOR_SIZE];
} input_t;

typedef struct {
	long t;
	int _1;
	int _2;
	int _3;
	int _4;
	int _5;
	int _6;
} output_tuple_t  __attribute__((aligned(1)));

typedef union {
	output_tuple_t tuple;
	uchar16 vectors [OUTPUT_VECTOR_SIZE];
} output_t;

__kernel void projectKernel (
	const int tuples,
	const int bytes,
	__global const uchar *input,
	__global uchar *output,
	__local uchar *_input,
	__local uchar *_output
) {
	int lid = (int) get_local_id  (0);
	int gid = (int) get_group_id  (0);
	int lgs = (int) get_local_size(0); /* Local group size */

	int input_idx  = gid * lgs * sizeof( input_t);
	int output_idx = gid * lgs * sizeof(output_t);

	/* Cache data into local memory */
	barrier (CLK_LOCAL_MEM_FENCE);
	event_t e = (event_t) 0;
	e = async_work_group_copy (
		(__local uchar *) &_input[0],
		(const __global uchar *) &input[input_idx],
		INPUT_VECTOR_SIZE * lgs,
		e);
	/* wait_group_events (1, &e); */

	 __local  input_t* p = (__local  input_t*) &_input [lid * sizeof( input_t)];
	 __local output_t* q = (__local output_t*) &_output[lid * sizeof(output_t)];

	// q->tuple.t = 0;

	q->tuple.t  = p->tuple.t;
	q->tuple._1 = p->tuple._1;
	q->tuple._2 = p->tuple._2;
	q->tuple._3 = p->tuple._3;
	q->tuple._4 = p->tuple._4;
	q->tuple._5 = p->tuple._5;
	q->tuple._6 = p->tuple._6;

	/* Write results in main memory */
	barrier (CLK_LOCAL_MEM_FENCE);
	e = async_work_group_copy (
			(__global uchar *) &output[output_idx],
			(__local uchar *) &_output[0],
			OUTPUT_VECTOR_SIZE * lgs, 
			e);
	wait_group_events (1, &e);

	return ;
}
