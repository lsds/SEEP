__kernel void selectKernel3 (
	
	const int size,
	const int tuples,
	const int _bundle,
	const int bundles,
	__global const uchar *input,
	__global int *flags,
	__global int *offsets,
	__global int *part,
	__global uchar *output,
	__local  int *x
) 
{
	int gid = get_group_id(0);
	part[gid] = get_num_groups(0);
	return ;
}

__kernel void compactKernel3 (
	
	const int size,
        const int tuples,
        const int _bundle,
        const int bundles, 
        __global const uchar *input,
        __global int *flags,
        __global int *offsets,
        __global int *part,
        __global uchar *output,
        __local  int *x
) 
{
	return ;
}

