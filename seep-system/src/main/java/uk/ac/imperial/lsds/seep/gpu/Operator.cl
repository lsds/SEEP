__kernel void noop  (          
                              const int        size,
                     __global const float*    input,
                     __global const int*    offsets,
                     __global const int*     counts,
                     __global       float*   output) {
	/* */
	int tid = (int) get_global_id(0);
	if (tid >= size)
		return;
	output[tid] = input[tid];
	return ;
}

