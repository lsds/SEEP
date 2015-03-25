/*
 * Up-sweep (reduce) on a local array `data` of length `length`.
 * `length` must be a power of two.
 */

inline void upsweep (__local int *data, int length) {
	
	int lid  = get_local_id (0);
	int b = (lid * 2) + 1;
	int depth = 1 + (int) log2 ((float) length);
	
	for (int d = 0; d < depth; d++) {
		
		barrier(CLK_LOCAL_MEM_FENCE);
		int mask = (0x1 << d) - 1;
		if ((lid & mask) == mask) {
			
			int offset = (0x1 << d);
			int a = b - offset;
			data[b] += data[a];
		}
	}
}

/*
 * Down-sweep on a local array `data` of length `length`.
 */

inline void downsweep (__local int *data, int length) {

	int lid = get_local_id (0);
	int b = (lid * 2) + 1;
	int depth = (int) log2 ((float) length);
	for (int d = depth; d >= 0; d--) {
		
		barrier(CLK_LOCAL_MEM_FENCE);
		int mask = (0x1 << d) - 1;
		if ((lid & mask) == mask) {
			
			int offset = (0x1 << d);
			int a = b - offset;
			int t = data[a];
			data[a] = data[b];
			data[b] += t;
		}
	}
}

/* Scan */
inline void scan (__local int *data, int length) {
	
	int lid = get_local_id (0);
	int lane = (lid * 2) + 1;
	
	upsweep (data, length);
	
	if (lane == (length - 1))
		data[lane] = 0;
	
	downsweep (data, length);
	return ;
}

/*
* First phase of a multiblock scan.
*
* Given a global array [data] of length arbitrary length [n].
* We assume that we have k workgroups each of size m/2 workitems.
* Each workgroup handles a subarray of length [m] (where m is a power of two).
* The last subarray will be padded with 0 if necessary (n < k*m).
* We use the primitives above to perform a scan operation within each subarray.
* We store the intermediate reduction of each subarray (following upsweep_pow2) in [part].
* These partial values can themselves be scanned and fed into [scan_inc_subarrays].
*/

__kernel void selectKernel2 (
	
	const int size,
	const int tuples,
	const int _bundle, /* To be removed */
	const int bundles, /* To be removed */
	__global const uchar *input,
	__global int *flags,
	__global int *offsets,
	__global int *part,
	__global uchar *output,
	__local  int *x
) 
{
	int wx = get_local_size (0);
	// global identifiers and indexes
	int gid = get_global_id (0);
	int lane0 = (2*gid) ;
	int lane1 = (2*gid)+1;
	// local identifiers and indexes
	int lid = get_local_id(0);
	int local_lane0 = (2*lid) ;
	int local_lane1 = (2*lid)+1;
	int grpid = get_group_id(0);
	// list lengths
	int m = wx * 2;
	int k = get_num_groups(0);
	
	/* Fetch tuple and apply selection filter */
	const int idx1 = lane0 * sizeof(input_t);
	const int idx2 = lane1 * sizeof(input_t);
	__global input_t *p1 = (__global input_t *) &input[idx1];
	__global input_t *p2 = (__global input_t *) &input[idx2];
	flags[lane0] = selectf (p1);
	flags[lane1] = selectf (p2);
	
//	flags[lane0] = 100;
//	flags[lane1] = 101;	
	
	// copy into local data padding elements >= n with 0
	x[local_lane0] = (lane0 < tuples) ? flags[lane0] : 0;
	x[local_lane1] = (lane1 < tuples) ? flags[lane1] : 0;
	// ON EACH SUBARRAY
	// a reduce on each subarray
	upsweep(x, m);
	// last workitem per workgroup saves last element of each subarray in [part] before zeroing
	if (lid == (wx-1)) {
	part[grpid] = x[local_lane1];
	x[local_lane1] = 0;
	}
	// a sweepdown on each subarray
	downsweep(x, m);
	
	//  copy back to global data
	if (lane0 < tuples) {
	offsets[lane0] = x[local_lane0];
	}
	if (lane1 < tuples) {
	offsets[lane1] = x[local_lane1];
	}
}

/*
* Perform the second phase of an inplace exclusive scan on a global array [data] of arbitrary length [n].
*
* We assume that we have k workgroups each of size m/2 workitems.
* Each workgroup handles a subarray of length [m] (where m is a power of two).
* We sum each element by the sum of the preceding subarrays taken from [part].
*/

__kernel void compactKernel2 (
	
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
	
//	// global identifiers and indexes
//	int gid = get_global_id(0);
//	int lane0 = (2*gid) ;
//	int lane1 = (2*gid)+1;
//	// local identifiers and indexes
//	int lid = get_local_id(0);
//	int local_lane0 = (2*lid) ;
//	int local_lane1 = (2*lid)+1;
//	int grpid = get_group_id(0);
//
//	/* Compute pivot value */
//
//      __local int pivot;
//        if (lid == 0) {
//        	pivot = 0;
//                if (grpid > 0) {
//                        for (int i = 1; i <= grpid; i++) {
//                                // int idx = i * _bundle - 1;
//                                pivot += (part[i]); //  + 1);
//                        }
//                }
//        }
//        barrier(CLK_LOCAL_MEM_FENCE);
//
//
//	// copy into local data padding elements >= n with identity
//	//
//	// x[local_lane0] = (lane0 < tuples) ? offsets[lane0] : 0;
//	//x[local_lane1] = (lane1 < tuples) ? offsets[lane1] : 0;
//	//x[local_lane0] += pivot; // part[grpid];
//	//x[local_lane1] += pivot; // part[grpid];
//	// copy back to global data
//	//if (lane0 < tuples) {
//	//offsets[lane0] = x[local_lane0];
//	//}
//	//if (lane1 < tuples) {
//	//offsets[lane1] = x[local_lane1];
//	//}
//
//	//barrier(CLK_LOCAL_MEM_FENCE);
//	//
//
//	if (flags[lane0] == 1) {
//		const int q1 = (offsets[lane0] + pivot) * sizeof(output_t);
//		const int p1 = lane0 * sizeof(input_t);
//		flags[lane0] = offsets[lane0];
//		__global  input_t *x0 = (__global  input_t *) &  input[p1];
//		__global output_t *y0 = (__global output_t *) & output[q1];
//		y0->vectors[0] = x0->vectors[0];
//		y0->vectors[1] = x0->vectors[1];
//	}
//
//	if (flags[lane1] == 1) {
//		const int q2 = (offsets[lane1] + pivot) * sizeof(output_t);
//		const int p2 = lane1 * sizeof(input_t);
//		flags[lane1] = offsets[lane1];
//		__global  input_t *x1 = (__global  input_t *) &  input[p2];
//		__global output_t *y1 = (__global output_t *) & output[q2];
//		y1->vectors[0] = x1->vectors[0];
//		y1->vectors[1] = x1->vectors[1];
//	}
	
}
