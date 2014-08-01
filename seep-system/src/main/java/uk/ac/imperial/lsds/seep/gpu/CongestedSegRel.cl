__kernel void dummyPLQ  (          
                              const int             size,
                     __global const int*           input,
                     __global       int*          output) {
	/* */
	return ;
}

__kernel void dummyWLQ  (          
                              const int             size,
                     __global const int*           input,
                     __global       int*          output) {
	/* */
	return ;
}

__kernel void dummyAGG  (        
                              const int             size,
                     __global const int*           input,
                     __global       int*          output) {
	/* */
	return ;
}

__kernel void plq (  const int     inputs,
                     const int     max_key,
            __global const int*    keys,
            __global const int*    values,
            __global const int*    offsets,
            __global const int*    counts,
            __global       int*    sum,
            __global       int*    N) {
	
	int tid = (int) get_global_id(0);
	int gid = (int) get_group_id(0);
	int group_size = (int) get_local_size(0);
	int lid = (int) get_local_id(0); /* 0, 1, ... `group_size` */
	
	int start = offsets[gid];
	int  pane_size = counts[gid];
	int _pane_size = pane_size + (pane_size - (pane_size % group_size));
	/* Split work between work-group threads */
	int tuples_per_thread = (pane_size < group_size) ? 1 : (_pane_size / group_size);
	int k, v;
	int in, out; /* input and output indices */
	int idx = lid;
	#pragma unroll
	for (int i = 0; i < tuples_per_thread; i++) {
		if (idx >= pane_size)
			return;
		in = start + idx;
		k = keys[in];
		v = values[in];
		out = k + max_key * gid;
		atom_add(&sum[out], v);
		atom_inc(&N[out]);
		idx = idx + group_size;
	}
	return ;
}

__kernel void wlq (
                       const  int       size,
                       const  int     groups,
                       const  int      panes,
              __global const  int*         S,
              __global const  int*         N,
              __global        int*        F) {

	int tid    = (int) get_global_id(0); /* */
	int group  = (int) get_group_id(0);
	int lid    = (int) get_local_id(0);
	
	int4 sum = 0;
	int4 cnt = 0;
	float4 avg = 0;
	int4 result;
	int idx;
	int out = lid + group * 50; /* `50` is the number of threads per group. */
                                /* It should passed as an argument. */
	if (out * 4 >= (size))
		return;
	for (int i = group; i < group + panes; i++) {
		/* `i` points at the right pane.
		 * 
		 * The first element of pane `i` is at position: i * groups.
		 * We want each thread in a group to accumulate a particular
		 * set of key (`lid`).
		 */
		idx = lid + i * 50;
		if (idx * 4 >= (size)) 
			return ;
		sum += vload4(idx, S);
		cnt += vload4(idx, N);
	}
	avg = convert_float4(sum) / convert_float4(cnt);
	result = (avg < 40.);
	vstore4(result, out, F);
}

