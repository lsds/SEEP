#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

typedef struct {
	long t;
	int _1;
	int _2;
	int _3;
	int _4;
	int _5;
	int _6;
} input_t  __attribute__((aligned(1)));

typedef struct {
	long t;
	int _1;
	int _2;
	int _3;
	int _4;
	int _5;
	int _6;
} output_t  __attribute__((aligned(1)));

__kernel void project (
	const int tuples,
	const int bytes,
	__global const uchar* input,
	__global uchar* output
) {
	int tid = (int) get_global_id(0);
	int idx = tid * sizeof(input_t);
	__global  input_t* p = (__global  input_t*) &input [idx];
	__global output_t* q = (__global output_t*) &output[idx];
	
	/* q = p; */
	
	q-> t = p-> t;
	q->_1 = p->_1;
	q->_2 = p->_2;
	q->_3 = p->_3;
	q->_4 = p->_4;
	q->_5 = p->_5;
	q->_6 = p->_6;
	
	return ;
}
