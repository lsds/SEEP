__kernel void aggregateKernel (
	const int tuples,
	const int bytes,
	const int _table_,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global int *failed,
	__global int *attempts,
	__global uchar* contents
) {

	//int lid = (int) get_local_id   (0);
	//int gid = (int) get_group_id   (0);
	//int lgs = (int) get_local_size (0); /* Local group size */

	return;
}

__kernel void clearKernel (
	const int tuples,
	const int bytes,
	const int _table_,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global int *failed,
	__global int *attempts,
	__global uchar* contents
) {

	int tid = get_global_id (0);
	failed[tid] = 0;

	return;
}

