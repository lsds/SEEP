#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#define  INPUT_VECTOR_SIZE 2
#define OUTPUT_VECTOR_SIZE 1

typedef struct {
	long _t;
	int _1;
	int _2;
	int _3;
	int _4;
	int _5;
	int _6;
} input_tuple_t  __attribute__((aligned(1)));

typedef union {
	input_tuple_t tuple;
	int4 vectors [INPUT_VECTOR_SIZE];
} input_t;

typedef struct {
	long _t;
	int _1;
	int _2;
	// int _3;
	// int _4;
	// int _5;
	// int _6;
} output_tuple_t  __attribute__((aligned(1)));

typedef union {
	output_tuple_t tuple;
	int4 vectors [OUTPUT_VECTOR_SIZE];
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
	// event_t e =
	// 	async_work_group_copy ((__local int4*) &_input[0], (const __global int4*) &input[input_idx], INPUT_VECTOR_SIZE * lgs, (event_t) 0);
	int i = 0;
	// for (i = 0; i < INPUT_VECTOR_SIZE; i++) {
		barrier (CLK_LOCAL_MEM_FENCE);
		int g_idx = i * lgs * sizeof( input_t) + input_idx;
		int l_idx = i * lgs * sizeof( input_t);
		event_t e = async_work_group_copy ((__local int4*) &_input[l_idx], (const __global int4*) &input[g_idx], 2 * lgs, (event_t) 0);
	// }
	// event_t e =
	//		async_work_group_copy ((__local int4*) &_input[0], (const __global int4*) &input[input_idx], lgs, (event_t) 0);
	wait_group_events (1, &e);
	// }



	__local  input_t* p = (__local  input_t*) &_input [lid * sizeof( input_t)];
	__local output_t* q = (__local output_t*) &_output[lid * sizeof(output_t)];
// 	q->tuple._t = 0;
//	q->tuple._1 = 0;
//	q->tuple._2 = 0;

	q->tuple._t = p->tuple._t;
	q->tuple._1 = p->tuple._1;
	q->tuple._2 = p->tuple._2;
	// q->tuple._3 = p->tuple._3;
	// q->tuple._4 = p->tuple._4;
	// q->tuple._5 = p->tuple._5;
	// q->tuple._6 = p->tuple._6;

	/* Write results in main memory */
	barrier (CLK_LOCAL_MEM_FENCE);
	// event_t g =
	// 	async_work_group_copy ((__global int4*) &output[output_idx], (__local int4*) &_output[0], OUTPUT_VECTOR_SIZE * lgs, (event_t) 0);
	event_t g =
		async_work_group_copy ((__global int4*) &output[output_idx], (__local int4*) &_output[0], lgs, (event_t) 0);
	wait_group_events (1, &g);

	return ;
}
