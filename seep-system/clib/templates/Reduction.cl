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
		float attr = getAggregateAttribute(p);
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
