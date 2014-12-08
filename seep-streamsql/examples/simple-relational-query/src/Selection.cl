#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics: enable
#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#define T int
#define identity 0
#define indexof(x) x
#define apply(a,b) (a + b)

typedef struct {
	int _1;
    int _2;
    int _3;
    int _4;
    int _5;
    int _6;
    int _7;
    int _8;
} input_tuple_t __attribute__((aligned(1)));

typedef union {
	input_tuple_t tuple;
	float4 values[2];
} input_t;

/* Since this is a selection, the output type is the same as the input */
#define output_t input_t

inline T scan_exclusive
(
	__local T *input,
	size_t idx,
	const uint lane
)
{
	if (lane > 0) input[idx] = apply(input[indexof(idx - 1)], input[indexof(idx)]);
	if (lane > 1) input[idx] = apply(input[indexof(idx - 2)], input[indexof(idx)]);
	if (lane > 3) input[idx] = apply(input[indexof(idx - 4)], input[indexof(idx)]);
	if (lane > 7) input[idx] = apply(input[indexof(idx - 8)], input[indexof(idx)]);
	if (lane >15) input[idx] = apply(input[indexof(idx -16)], input[indexof(idx)]);
	return (lane > 0) ? input[idx - 1] : identity;
}

inline T scan_inclusive
(
	__local T *input,
	size_t idx,
	const uint lane
)
{
	if (lane > 0) input[idx] = apply(input[indexof(idx - 1)], input[indexof(idx)]);
	if (lane > 1) input[idx] = apply(input[indexof(idx - 2)], input[indexof(idx)]);
	if (lane > 3) input[idx] = apply(input[indexof(idx - 4)], input[indexof(idx)]);
	if (lane > 7) input[idx] = apply(input[indexof(idx - 8)], input[indexof(idx)]);
	if (lane >15) input[idx] = apply(input[indexof(idx -16)], input[indexof(idx)]);
	return input[idx];
}

inline T scan
(
	__local T *buffer,
	const uint idx,
	const uint lane,
	const uint wid /* wrap id */
)
{
	T value;

	/* Step 1: perform warp scan */
	value = scan_exclusive (buffer, idx, lane);

	barrier(CLK_LOCAL_MEM_FENCE);

	/* Step 2: collect per-warp partial results */
	if (lane > 30)
		buffer[wid] = buffer[idx];

	barrier(CLK_LOCAL_MEM_FENCE);

	/* Step 3: use 1st warp to scan per-warp results */
	if (wid < 1)
		scan_exclusive(buffer, idx, lane);

	barrier(CLK_LOCAL_MEM_FENCE);

	/* Step 4: accumulate results from step 1 and step 3 */
	if (wid > 0)
		value = apply(buffer[wid - 1], value);

	barrier(CLK_LOCAL_MEM_FENCE);

	/* Step 5: write and return the final result */
	buffer[idx] = value;

	barrier(CLK_LOCAL_MEM_FENCE);

	return value;
}

inline int predicate
(
	__global input_t *p
)
{
	return 1;
}

__kernel void selectKernel
(
	const int size,
	const int tuples,
	const int _bundle, /* bundle size */
	const int bundles, /* # bundles */
	__global const uchar *input,
	__global int *flags,
	__global int *offsets,
	__local int *buffer
)
{
	/* Populate indices */
	const size_t lid = get_local_id(0);
	const size_t gid = get_group_id(0);
	const size_t tpb  = get_local_size(0); /* #threads/block */
	const uint lane = lid & 31;
	const uint wid  = lid >> 5; /* Wrap id, assuming 32 wraps */

	T value = identity;
	for (int i = 0; i < bundles; i++) {

		const int offset = i * tpb + (gid * _bundle);
		const int pos = offset + lid;
		if (pos > tuples - 1)
			return;

		/* Step 0: apply the selection filter */
		const int idx = pos * sizeof(input_t);
		__global input_t *p = (__global input_t *) &input[idx];
		int result = predicate (p);
		flags[0] = result;

		/* Step 1: read `tpb` elements from global to local memory */
		T _input = buffer[lid] = flags[pos];

		barrier(CLK_LOCAL_MEM_FENCE);

		/* Step 2: scan `tpb` elements */
		T v = scan (buffer, lid, lane, wid);

		/* Step 3: propagate result from previous block */
		v = apply (v, value);

		/* Step 4: write result to global memory */
		offsets[pos] = v;

		/* Step 5: choose next reduce value */
		if (lid == (tpb - 1))
			buffer[lid] = apply (_input, v);

		barrier(CLK_LOCAL_MEM_FENCE);

		value = buffer[tpb - 1];

		barrier(CLK_LOCAL_MEM_FENCE);
	}
	return ;
}

__kernel void compactKernel
(
	const int size,
	const int tuples,
	const int _bundle, /* bundle size */
	const int bundles, /* # bundles */
	__global const uchar *input,
	__global const int *flags,
	__global const int *offsets,
	__global const int *pivots,
	__global uchar *output
)
{
	/*
	 * First, for each offset, uniformly add pivot value to offset. Then,
	 * for each tuple, if flag is not zero, then copy it to output at the
	 * specific offset.
	 */

	/* Populate indices */
	const size_t lid = get_local_id(0);
	const size_t gid = get_group_id(0);
	const size_t tpb  = get_local_size(0); /* # threads/block */

	const int pivot = pivots[gid];

	for (int i = 0; i < bundles; i++) {
		const int offset = i * tpb + (gid * _bundle);
		const int pos = offset + lid; /* tuple index */
		if (pos >= tuples)
			return;
		/* Copy tuple `pos` into position `j` if flag is set */
		if (flags[pos] == 1) {
			const int q = (offsets[pos] + pivot) * sizeof(output_t);
			const int p = pos * sizeof(input_t);
			__global  input_t *x = (__global  input_t *) &  input[p];
			__global output_t *y = (__global output_t *) & output[q];
			y->values[0] = x->values[0];
			y->values[1] = x->values[1];
		}
	}
	return ;
}
