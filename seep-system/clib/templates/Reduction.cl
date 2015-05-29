__kernel void reduceKernel (
	const int tuples,
	const int bytes,
	const int nwindows,
	const int window_size,
	const int window_slide,
	__global const uchar *input,
	// __global const int *window_ptrs_,
	// __global const int *_window_ptrs,
	__global uchar *output,
	__local uchar *_input,
	__local float *scratch
) {
	int tid = (int) get_global_id  (0);
	int lid = (int) get_local_id   (0);
	int gid = (int) get_group_id   (0);
	int lgs = (int) get_local_size (0); /* Local group size */
	
	int bpw = sizeof(input_t) * window_size;
	int window_offset = sizeof(input_t) * window_slide;
	
	int group_offset = lgs * sizeof(input_t);
	
	/* Fetch pane to local memory */
	barrier (CLK_LOCAL_MEM_FENCE);
	event_t e = (event_t) 0;
	e = async_work_group_copy (
		(__local uchar *) &_input[0],
		(const __global uchar *) &input[gid * group_offset],
		group_offset,
		e);
	
	__local input_t *p = (__local input_t *) &_input[lid * sizeof(input_t)];
	scratch[lid] = __bswapfp(p->tuple._1); 
	barrier (CLK_LOCAL_MEM_FENCE);
	
	int offset_, _offset;
	int thread_offset = tid * sizeof(input_t);
	
	__global output_t *result = NULL;
	for (int w = 0; w < nwindows; w++) {
		/* Compute start and end pointers of a window */
		offset_ = w * window_offset;
		_offset = offset_ + bpw;
		if (thread_offset == offset_) {
			/* This thread's tuple is the first one in window `w` */
			// for (int i = lid + 1; i < lgs; i++) {
			//	scratch[lid] += scratch[i];
			// }
			/* Write result to output buffer */
			result = (__global output_t *) &output[w * sizeof(output_t)];
			result->tuple.t = 0L;
			result->tuple._1 = __bswapfp(scratch[lid]);
			break;
		}
	}
	
	// __local input_t *p = NULL;
	// int pointer;
	// float mine, other;
	// /* For each window... */
	// int accessed = 0;
	//	/* A thread group whose tuples do not belong to window `w` should exit. */
	//	if (_offset <  (gid + 0) * group_offset)
	//		continue;
	//	if (offset_ >= (gid + 1) * group_offset)
	//		break;
	//	accessed += 1;
	//	scratch[lid] = 0; /* Undefined value */
	//	for (int pos = lgs / 2; pos > 0; pos = pos / 2) {
	//		if (lid < pos) {
	//			/* Check `lid` */
	//			pointer = (((gid * lgs) + lid) * sizeof(input_t));
	//			p = (__local input_t *) &_input[lid * sizeof(input_t)];
	//			if (pointer >= offset_ && pointer < _offset)
	//				scratch[lid] = __bswapfp(p->tuple._1);
	//			/* Check `lid + pos` */
	//			pointer = pointer + (pos * sizeof(input_t));
	//			p = (__local input_t *) &_input[(lid + pos) * sizeof(input_t)];
	//			if (pointer >= offset_ && pointer < _offset)
	//				scratch[lid + pos] = __bswapfp(p->tuple._1);
	//			
	//			mine = scratch[lid];
	//			other = scratch[lid + pos];
	//			scratch[lid] = mine + other; 
	//		}
	//		barrier(CLK_LOCAL_MEM_FENCE);
	//	}
	// 	if (lid == 0) {
	//		__global output_t *result = (__global output_t *) &output[w * sizeof(output_t)];
	//		/* Atomically update value */
	//		atomic_add ((global int *) &(result->tuple._1), convert_int_rtp(__bswapfp(scratch[lid])));
	//	}
	// }
	//__global output_t *result = (__global output_t *) &output[gid * sizeof(output_t)];
	//result->tuple.t = 0L;
	//result->tuple._1 = __bswapfp(convert_float(accessed));
	
	return ;
}
