__kernel void dummyKernel (
	__global const int *input,
	__global int *output
) 
{
	int id = get_global_id (0);
	output[id] = input[id];
	return;
}
