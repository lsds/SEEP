#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#define  INPUT_VECTOR_SIZE 2
#define OUTPUT_VECTOR_SIZE 1

typedef struct {
	long t;
	float _1;
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
	long t;
	float _1;
	int p0;
} output_tuple_t; //  __attribute__((aligned(1)));

typedef union {
	output_tuple_t tuple;
	int4 vectors [OUTPUT_VECTOR_SIZE];
} output_t;

__kernel void reduce (
	const int tuples,
	const int bytes,
	__global const uchar *input,
	__global uchar *output,
	__global const int *window_ptrs_,
	__global const int *_window_ptrs,
	__local float *scratch
) {
	int lid = (int) get_local_id   (0);
	int gid = (int) get_group_id   (0);
	int lgs = (int) get_local_size (0); /* Local group size */

//	int group_offset = lgs * sizeof(input_t);
//	int offset_ =  window_ptrs_ [gid]; /* Window start and end pointers */
//	int _offset = _window_ptrs  [gid];
//	int idx =  lid * sizeof(input_t) + offset_;
//
//	float value = INFINITY;
//	// long tstamp = 0;
//	/* The sequential part */
//	while (idx < _offset) {
//		/* Get tuple from main memory */
//		__global input_t *p = (__global input_t *) &input[idx];
//		// long _tstamp = p->tuple.t;
//		float attr = p->tuple._1;
//		// tstamp = (value < attr) ? tstamp : _tstamp;
//		value  = (value < attr) ? value : attr;
//		idx += group_offset;
//	}
//	/* Write value to scratch memory */
//	/* __local output_t* q =
//			(__local output_t*) &scratch[lid * sizeof(output_t)];
//	q->tuple.t  = tstamp;
//	q->tuple._1 = value; */
//	scratch[lid] = value;
//	barrier(CLK_LOCAL_MEM_FENCE);
//
//	/* Parallel reduction */
//	for (int pos = lgs / 2; pos > 0; pos = pos / 2) {
//		if (lid < pos) {
//			float mine = scratch[lid];
//			float other = scratch[lid + pos];
//			scratch[lid] = (mine < other) ? mine : other;
//			// __local output_t* other =
//			// 	(__local output_t*) &scratch[(lid + pos) * sizeof(output_t)];
//			// q->tuple._1 = (q->tuple._1 < other->tuple._1) ? q->tuple._1 : other->tuple._1;
//			// q->vectors[0] = (q->tuple._1 < other->tuple._1) ? q->vectors[0] : other->vectors[0];
//		}
//		barrier(CLK_LOCAL_MEM_FENCE);
//	}
//	/* Write result */
//	if (lid == 0) {
//		// __global output_t *result =
//		//		(__global output_t *) &output[gid * sizeof(output_t)];
//		/*
//		 * In case output was an integer:
//		 */
//		 __global int *value = (__global int *) &output[gid * sizeof(int)];
//		 *value = scratch[lid];
//		 /**/
//		// result->tuple.t = 0L;
//		// result->tuple._1 = scratch[lid]; // scratch[lid];
//		// result->tuple.p0 = 0;
//	}
	return ;
}
