#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable
#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable

#pragma OPENCL EXTENSION cl_khr_byte_addressable_store: enable

#define _MAX_ITERATIONS_  20
#define _HASH_FUNCTIONS_  5

#define _stash_ 100

#define  INPUT_VECTOR_SIZE 2
#define OUTPUT_VECTOR_SIZE 1

typedef struct {
	long t;
	float _1; /* The aggregate */
	int _2; /* The key */
	int _3;
	int _4;
	int _5;
	int _6;
} input_tuple_t  __attribute__((aligned(1)));

typedef union {
	input_tuple_t tuple;
	uchar16 vectors [INPUT_VECTOR_SIZE];
} input_t;

typedef struct {
	long t;
	int _1; /* Key */
	float _2; /* Value */
} output_tuple_t  __attribute__((aligned(1)));

typedef union {
	output_tuple_t tuple;
	uchar16 vectors [OUTPUT_VECTOR_SIZE];
} output_t;

inline int pack_key (__global input_t *p) {
	return p->tuple._2;
}

inline int __s_major_hash (int x, int y, int k) {
    const unsigned int prime = 2147483647U;
    unsigned int xl = convert_uint(x);
    unsigned int yl = convert_uint(y);
    unsigned int kl = convert_uint(k);
    unsigned int result = ((xl ^ kl) + yl) % prime;
    return convert_int(result);
}

inline int __s_minor_hash (int x, int y, int k) {
    const unsigned int stash_size = 101U;
    unsigned int xl = convert_uint(x);
    unsigned int yl = convert_uint(y);
    unsigned int kl = convert_uint(k);
    unsigned int result = ((xl ^ kl) + yl) % stash_size;
    return convert_int(result);
}

inline int getNextLocation(               int  size,
                                          int   key,
                                          int  prev,
                           __global const int*    x,
                           __global const int*    y){

	/* Identify possible locations for key */
	int locations[_HASH_FUNCTIONS_];
	int i;
	#pragma unroll
	for (i = 0; i < _HASH_FUNCTIONS_; i++) {
		locations[i] = __s_major_hash (x[i], y[i], key) % size;
	}

	int next = locations[0];
	#pragma unroll
	for (i = _HASH_FUNCTIONS_ - 2; i >= 0; --i) {
		next = (prev == locations[i] ? locations[i+1] : next);
	}
	return next;
}

inline int V (__global int *s) {
	int v = atom_xchg (s, 1);
	while (v > 0)
		v = atom_xchg (s, 1);
	return v;
}

inline int P (__global int *s) {
	return atom_xchg (s, 0);
}

__kernel void aggregateKernel (
	const int tuples,
	const int _table_,
	const int __stash_x,
	const int __stash_y,
	const int max_iterations,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global const int* x,
	__global const int* y,
	__global uchar* contents,
	__global int* stashed,
	__global int* failed,
	__global int* attempts,
	__global int* locks,
	__global uchar* output
	) {

	int lid = (int) get_local_id   (0);
	int gid = (int) get_group_id   (0);
	int lgs = (int) get_local_size (0); /* Local group size */

	int table_offset = gid * (_table_ + _stash_);
	int group_offset = lgs * sizeof(input_t);
	int offset_ =  window_ptrs_ [gid]; /* Window start and end pointers */
	int _offset = _window_ptrs  [gid];

	if (lid == 0) {
		stashed[gid] = gid;
		 failed[gid] = gid;
	}

	/* For debugging purposes only */ /* Tuples/group */
	int tpg = (_offset - offset_ + 1) / sizeof(input_t);

	int idx =  lid * sizeof(input_t) + offset_;
	int bundle = 0;
	int tuple_id = 0;
	while (idx < _offset) {

		/* For debugging purposes only */ /* Tuple id */
		tuple_id = lid + bundle * lgs + tpg * gid;

		bool success = true;
		int iterations = 0;
		__global input_t *p = (__global input_t *) &input[idx];
		int k_ = pack_key (p);
		int pos = __s_major_hash(x[0], y[0], k_) % _table_;
		for (int i = 1; i < max_iterations; i++) {
			/* Acquire lock `pos` */
			int v = atom_cmpxchg (&locks[table_offset + pos], 0, 1);
			while (v > 0)
				v = atom_cmpxchg (&locks[table_offset + pos], 0, 1);

			// locks[table_offset + pos] = 1;
			int s = atom_xchg (&locks[table_o
			                          ffset + pos], 0);

			/* Release lock `pos`*/
			// P (&locks[table_offset + pos]);
		}
		attempts[tuple_id] = table_offset;
		idx += group_offset;
		bundle += 1;
	}

//		bool success = true;
//		int iterations = 0;
//		/* Get tuple from main memory */
//		__global input_t *p = (__global input_t *) &input[idx];
//		/* Insert into hash table */
//		int k_ = pack_key (p);
//		int pos = __s_major_hash(x[0], y[0], k_) % _table_;
//		for (int i = 1; i < max_iterations; i++) {
//			/* Acquire lock `pos` */
//			V (&locks[pos]);
//			/* Get current entry from hash table */
//			__global output_t *q = (__global output_t *) &contents[f(pos)];
//
//			_k = q->tuple._1;
//			q->tuple._1 = p->tuple._1;
//			q->tuple._2 = (k_ == _k) ? (q->tuple._2 + p->tuple._1) : p->tuple._1;
//			/* Release lock `pos`*/
//			P (&locks[pos]);
//			if (is_empty) {
//				iterations = i;
//				break ;
//			}
//			pos = getNextLocation(_table_, k_, pos, x, y);
//		}
//		if (k_ != EMPTY___KEY) { /* Insert into stash */
//			int slot =
//				__s_minor_hash(__stash_x, __stash_y, k_);
//			long replaced = atom_cmpxchg(&contents[_table_ + slot], EMPTY_ENTRY, e);
//			if (replaced != EMPTY_ENTRY)
//				success = false;
//			else
//				atomic_inc(&stashed[0]);
//		}
//		if (success == false)
//			atomic_inc(&failed[0]);
//		attempts[id] = iterations;
//	}

}
