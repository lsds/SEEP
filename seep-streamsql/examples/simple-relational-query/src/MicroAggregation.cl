#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable
#pragma OPENCL EXTENSION cl_khr_int64_base_atomics : enable

#define _MAX_ITERATIONS_  20
#define _HASH_FUNCTIONS_  5

#define EMPTY___KEY 0xffffffff
#define EMPTY_ENTRY (convert_long(EMPTY___KEY) << 32)

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

inline long pair(int key, int value) {
	long entry = (convert_long(key) << 32) | (value & 0xffffffff);
	return entry;
}

inline int getKey (long entry) {
	return convert_int(entry >> 32);
}

inline int getValue (long entry) {
	return convert_int(entry);
}

__kernel void keyvalues (         const int    size,
                         __global const int*   keys,
                         __global const int* values,
                         __global       int* output) {

	int id = (int) get_global_id(0);
	if (id >= size)
		return;
	int k = keys  [id];
	int v = values[id];
	long entry = pair(k, v);
	int k_ = getKey(entry);
	int v_ = getValue(entry);
	int result = ((k == k_) && (v == v_)) ? 0 : 1;
	output[id] = result;
}

__kernel void empty (         const int       slots,
                     __global const long*  contents,
                     __global       int*    results) {

	int id = (int) get_global_id(0);
	if (id >= slots)
		return ;
	long e = contents[id];
	int k = getKey(e);
	/* results[id] = (e == EMPTY_ENTRY) ? 0 : 1; */
	results[id] = (k == EMPTY___KEY) ? 0 : 1;
	return ;
}

__kernel void testhash (         const int       size,
                        __global const int*      keys,
                                 const int    _table_,
                        __global const int*         x,
                        __global const int*         y,
                        __global       int* results_0,
                        __global       int* results_1,
                        __global       int* results_2,
                        __global       int* results_3,
                        __global       int* results_4) {
	int id = (int) get_global_id(0);
	if (id >= size)
		return ;
	int k = keys[id];
	int pos[_HASH_FUNCTIONS_];
	for (int i = 0; i < _HASH_FUNCTIONS_; i++) {
		pos[i] =
			__s_major_hash(x[i], y[i], k) % _table_;
	}
	results_0[id] = pos[0];
	results_1[id] = pos[1];
	results_2[id] = pos[2];
	results_3[id] = pos[3];
	results_4[id] = pos[4];
}

__kernel void hashstats (         const int     size,
                         __global const int*    keys,
                                  const int  _table_,
                         __global const int*       x,
                         __global const int*       y,
                         __global       int*   count,
                         __global       int*   slots,
                         __global       int*  failed) {

	int id = (int) get_global_id(0);
	if (id >= size)
		return ;
	int k = keys[id];
	int locations[_HASH_FUNCTIONS_];
	for (int i = 0; i < _HASH_FUNCTIONS_; i++) {
		locations[i] =
			__s_major_hash(x[i], y[i], k) % _table_;
		atomic_inc(&count[locations[i]]);
	}
	int available = 1;
	for (int i = 1; i < _HASH_FUNCTIONS_; i++) {
		bool match = false;
		for (int j = 0; j < i; j++) {
			if (locations[i] == locations[j]) {
				match = true;
				break;
			}
		}
		if (! match) { available++; }
	}
	slots[id] = available;
	if (available != _HASH_FUNCTIONS_)
		atomic_inc(&failed[0]);
	return ;
}

__kernel void build (         const int        size,
                     __global const int*       keys,
                     __global const int*     values,
                              const int     _table_,
                     __global const int*          x,
                     __global const int*          y,
                              const int   __stash_x,
                              const int   __stash_y,
                              const int  max_iterations,
                     __global       long*  contents,
                     __global       int*    stashed,
                     __global       int*     failed,
                     __global       int*   attempts) {

	int id = (int) get_global_id(0);
	int f = failed[0];
	if (id >= size || f > 0)
		return ;

	int k = keys[id];
	int v = values[id];
	long entry = pair(k, v);

	bool success = true;
	int iterations = 0;
	/* Insert */
	long e = entry;
	int k_ = getKey(entry);
	int pos =
		__s_major_hash(x[0], y[0], k_) % _table_;
	for (int i = 1; i < max_iterations; i++) {
		e = atomic_xchg(&contents[pos], e);
		k_= getKey(e);
		if (k_ == EMPTY___KEY) {
			iterations = i;
			break ;
		}
		pos = getNextLocation(_table_, k_, pos, x, y);
	}

	if (k_ != EMPTY___KEY) { /* Insert into stash */
		int slot =
			__s_minor_hash(__stash_x, __stash_y, k_);
		long replaced = atom_cmpxchg(&contents[_table_ + slot], EMPTY_ENTRY, e);
		if (replaced != EMPTY_ENTRY)
			success = false;
		else
			atomic_inc(&stashed[0]);
	}

	if (success == false)
		atomic_inc(&failed[0]);
	attempts[id] = iterations;
	return ;
}

__kernel void query (         const int        size,
                     __global const int*    queries,
                              const int     _table_,
                     __global const long*  contents,
                     __global const int*          x,
                     __global const int*          y,
                              const int   __stash_x,
                              const int   __stash_y,
                              const int   __stash__,
                     __global       int*     values,
                     __global       int*     probes) {

	int id = (int) get_global_id(0);
	if (id >= size)
		return ;
	int query = queries[id];
	int locations[_HASH_FUNCTIONS_];
	for (int i = 0; i < _HASH_FUNCTIONS_; i++) {
		locations[i] =
			__s_major_hash(x[i], y[i], query) % _table_;
	}
	/* Check locations */
	int tries = 1;
	long entry = contents[locations[0]];
	int key = getKey(entry);
	#pragma unroll
	for (int i = 1; i < _HASH_FUNCTIONS_; i++) {
		if (key != query && key != EMPTY___KEY) {
			/* Retry */
			tries ++;
			entry = contents[locations[i]];
			key = getKey(entry);
		}
	}

	/* Check stashed keys */
	int stash_idx;
	if (__stash__ > 0 && key != query) {
		tries ++;
		stash_idx = __s_minor_hash(__stash_x, __stash_y, query);
		entry = contents[_table_ + stash_idx];
		key = getKey(entry);
	}

	/* Keep statistics */
	probes[id] = tries;
	/* Return result */
	values[id] = (key == query) ? getValue(entry) : EMPTY___KEY;
	return ;
}
