__kernel void clearKernel (
	const int  tuples,
	const int  bytes,
	const long previousPaneId,
	const long batchOffset,
	__global const uchar *input,
	__global int *window_ptrs_, /* window start pointers */
	__global int *_window_ptrs, /* window end   pointers */
	__global long *offset,      /* Temp. variable holding the window pointer offset */
	__global uchar *output,
	__local float *scratch
) {
	int tid = (int) get_global_id  (0);
	int lid = (int) get_local_id   (0);
	int gid = (int) get_group_id   (0);
	int lgs = (int) get_local_size (0); /* Local group size */

	if (tid == 0) {
		offset[0] = 0;
		offset[1] = 0;
	}
}

__kernel void partialReduceKernel (
	const int  tuples,
	const int  bytes,
	const long previousPaneId,
	const long batchOffset,
	__global const uchar *input,
	__global int *window_ptrs_, /* window start pointers */
	__global int *_window_ptrs, /* window end   pointers */
	__global long *offset,      /* Temp. variable holding the window pointer offset */
	__global uchar *output,
	__local float *scratch
) {
	int tid = (int) get_global_id  (0);
	int lid = (int) get_local_id   (0);
	int gid = (int) get_group_id   (0);
	int lgs = (int) get_local_size (0); /* Local group size */
	
	long wid;
	long paneId, normalisedPaneId;

	long currPaneId;
	long prevPaneId;
	/* Every thread is assigned a tuple */
	__global input_t *curr = (__global input_t *) &input[tid * sizeof(input_t)];
#ifdef RANGE_BASED
	currPaneId = curr->tuple.t / PANE_SIZE;
#else
	currPaneId = ((batchOffset + (tid * sizeof(input_t))) / sizeof(input_t)) / PANE_SIZE;
#endif
	/* It also reads the previous tuple */
	if (tid > 0) {
		__global input_t *prev = (__global input_t *) &input[(tid - 1) * sizeof(input_t)];
#ifdef RANGE_BASED
		prevPaneId = prev->tuple.t / PANE_SIZE;
#else
		prevPaneId = ((batchOffset + ((tid - 1) * sizeof(input_t))) / sizeof(input_t)) / PANE_SIZE;
#endif
	} else {
		prevPaneId = previousPaneId; // currPaneId - 1; // previousPaneId;
	}
//	if (tid == 0) {
//		offset[0] = prevPaneId; // prevPaneId;
//		offset[1] = currPaneId;
//	}

	if (prevPaneId < currPaneId) {
		/* Compute offset based on the first closing window */
		// while (prevPaneId < currPaneId) {
			paneId = prevPaneId + 1;
			normalisedPaneId = paneId - PANES_PER_WINDOW;
			if (normalisedPaneId >= 0 && normalisedPaneId % PANES_PER_SLIDE == 0) {
				wid = normalisedPaneId / PANES_PER_SLIDE;
				if (wid >= 0) {
					// atom_min(&offset[0], wid); // convert_int_sat(wid));
					offset[0] = wid;
					// break;
				}
			}
			// prevPaneId += 1;
		// }
	}

	return ;
}
