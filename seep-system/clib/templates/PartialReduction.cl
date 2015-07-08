__kernel void clearKernel (
	const int tuples,
	const int bytes,
	const long previousPaneId,
	const long batchOffset,
	__global const uchar *input,
	__global int *window_ptrs_, /* window start pointers */
	__global int *_window_ptrs, /* window end   pointers */
	__global long *offset,      /* Temp. variable holding the window pointer offset */
	__global int *windowCounts,
	__global uchar *output,
	__local float *scratch
) {
	int tid = (int) get_global_id (0);
	if (tid == 0) {
		offset[0] = LONG_MAX;
		offset[1] = 0;
	}
	/* The maximum number of window pointers is as many as the tuples */
	 window_ptrs_[tid] = -1;
	_window_ptrs [tid] = -1;
	
	/* Initialise window counters: closing, pending, complete, opening */
	if (tid < 4)
		windowCounts[tid] = 0;

	return;
}

__kernel void computeOffsetKernel (
	const int tuples,
	const int bytes,
	const long previousPaneId,
	const long batchOffset,
	__global const uchar *input,
	__global int *window_ptrs_, /* window start pointers */
	__global int *_window_ptrs, /* window end   pointers */
	__global long *offset,      /* Temp. variable holding the window pointer offset */
	__global int *windowCounts,
	__global uchar *output,
	__local float *scratch
) {
	int tid = (int) get_global_id  (0);
	
	long wid;
	long paneId, normalisedPaneId;

	long currPaneId;
	long prevPaneId;
	
	/* Every thread is assigned a tuple */
#ifdef RANGE_BASED
	__global input_t *curr = (__global input_t *) &input[tid * sizeof(input_t)];
	currPaneId = curr->tuple.t / PANE_SIZE;
#else
	currPaneId = ((batchOffset + (tid * sizeof(input_t))) / sizeof(input_t)) / PANE_SIZE;
#endif
	if (tid > 0) {
#ifdef RANGE_BASED
		__global input_t *prev = (__global input_t *) &input[(tid - 1) * sizeof(input_t)];
		prevPaneId = prev->tuple.t / PANE_SIZE;
#else
		prevPaneId = ((batchOffset + ((tid - 1) * sizeof(input_t))) / sizeof(input_t)) / PANE_SIZE;
#endif
	} else {
		prevPaneId = previousPaneId;
	}

	if (prevPaneId < currPaneId) {
		/* Compute offset based on the first closing window */
		while (prevPaneId < currPaneId) {
			paneId = prevPaneId + 1;
			normalisedPaneId = paneId - PANES_PER_WINDOW;
			if (normalisedPaneId >= 0 && normalisedPaneId % PANES_PER_SLIDE == 0) {
				wid = normalisedPaneId / PANES_PER_SLIDE;
				if (wid >= 0) {
					atom_min(&offset[0], wid);
					break;
				}
			}
			prevPaneId += 1;
		}
	}

	return ;
}

__kernel void computePointersKernel (
	const int tuples,
	const int bytes,
	const long previousPaneId,
	const long batchOffset,
	__global const uchar *input,
	__global int *window_ptrs_, /* window start pointers */
	__global int *_window_ptrs, /* window end   pointers */
	__global long *offset,      /* Temp. variable holding the window pointer offset */
	__global int *windowCounts,
	__global uchar *output,
	__local float *scratch
) {
	int tid = (int) get_global_id  (0);
	
	long wid;
	long paneId, normalisedPaneId;

	long currPaneId;
	long prevPaneId;
	
	/* Every thread is assigned a tuple */
#ifdef RANGE_BASED
	__global input_t *curr = (__global input_t *) &input[tid * sizeof(input_t)];
	currPaneId = curr->tuple.t / PANE_SIZE;
#else
	currPaneId = ((batchOffset + (tid * sizeof(input_t))) / sizeof(input_t)) / PANE_SIZE;
#endif
	if (tid > 0) {
#ifdef RANGE_BASED
		__global input_t *prev = (__global input_t *) &input[(tid - 1) * sizeof(input_t)];
		prevPaneId = prev->tuple.t / PANE_SIZE;
#else
		prevPaneId = ((batchOffset + ((tid - 1) * sizeof(input_t))) / sizeof(input_t)) / PANE_SIZE;
#endif
	} else {
		prevPaneId = previousPaneId;
	}

	long windowOffset = offset[0];
	int index;

	if (prevPaneId < currPaneId) {
		while (prevPaneId < currPaneId) {
			paneId = prevPaneId + 1;
			normalisedPaneId = paneId - PANES_PER_WINDOW;
			/* Check closing windows */
			if (normalisedPaneId >= 0 && normalisedPaneId % PANES_PER_SLIDE == 0) {
				wid = normalisedPaneId / PANES_PER_SLIDE;
				if (wid >= 0) {
					index = convert_int_sat(wid - windowOffset);
					atom_max(&offset[1], (wid - windowOffset));
					_window_ptrs[index] = tid * sizeof(input_t);
				}
			}
			/* Check opening windows */
			if (paneId % PANES_PER_SLIDE == 0) {
				wid = paneId / PANES_PER_SLIDE;
				index = convert_int_sat(wid - windowOffset);
				atom_max(&offset[1], wid - windowOffset);
				window_ptrs_[index] = tid * sizeof(input_t);
			}
			prevPaneId += 1;
		}
	}

	return ;
}

__kernel void reduceKernel (
	const int tuples,
    const int bytes,
    const long previousPaneId,
    const long batchOffset,
    __global const uchar *input,
    __global int *window_ptrs_, /* window start pointers */
    __global int *_window_ptrs, /* window end   pointers */
    __global long *offset,      /* Temp. variable holding the window pointer offset */
	__global int *windowCounts,
    __global uchar *output,
    __local float *scratch
) {
	int lid = (int) get_local_id   (0);
	int gid = (int) get_group_id   (0);
	int lgs = (int) get_local_size (0); /* Local group size */
	int nlg = (int) get_num_groups (0);
	
	__local int num_windows;
	if (lid == 0)
		convert_int_sat(offset[1]);
	barrier(CLK_LOCAL_MEM_FENCE);
	if (tid == 0)
		windowCounts[4] = num_windows * sizeof(output_t);
	
	int group_offset = lgs * sizeof(input_t);
	
	int wid = gid;
	/* A group may process more than one windows */
	while (wid <= num_windows) {
		int  offset_ =  window_ptrs_ [wid]; /* Window start and end pointers */
		int _offset  = _window_ptrs  [wid];
		/* Check if a window is closing, opening, pending, or complete. */
		if (offset_ < 0 && _offset >= 0) {
			/* A closing window; set start offset */
			offset_ = 0;
			if (lid == 0)
				atomic_inc(&windowCounts[0]);
		} else
		if (offset_ >= 0 && _offset < 0) {
			/* An opening window; set end offset */
			_offset = bytes;
			if (lid == 0)
				atomic_inc(&windowCounts[3]);
		} else
		if (offset_ < 0 && _offset < 0) {
			/* A pending window */
			int old = atomic_cmpxchg(&windowCounts[1], 0, 1);
			if (old > 0) {
				wid += nlg;
				continue;
			}
			_offset  = 0;
			 offset_ = bytes;
		} else {
			/* A complete window */
			if (lid == 0)
				atomic_inc(&windowCounts[2]);
		}
		
		if (offset_ == _offset) {
			wid += nlg;
			continue;
		}

		int idx = lid * sizeof(input_t) + offset_;

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
				(__global output_t *) &output[wid * sizeof(output_t)];

			result->tuple.t = 0L; // p->tuple.t;
			result->tuple._1 = __bswapfp(scratch[lid]);
		}
		
		wid += nlg; /* try next window */
	}
	return ;
}
