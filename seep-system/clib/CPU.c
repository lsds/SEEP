#include "uk_ac_imperial_lsds_seep_multi_TheCPU.h"
#include <jni.h>

#include <unistd.h>
#include <sched.h>

/* Thread affinity library calls */

static cpu_set_t fullSet;

static cpu_set_t *getFullSet (void) {
	static int init = 0;
	if (init == 0) {
		int i;
		int ncores = sysconf(_SC_NPROCESSORS_ONLN);
		CPU_ZERO (&fullSet);
		for (i = 0; i < ncores; i++)
			CPU_SET (i, &fullSet);
		init = 1;
	}
	return &fullSet;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheCPU_getNumCores
(JNIEnv *env, jobject obj) {

	(void) env;
	(void) obj;

	int ncores = 0;
	ncores = sysconf(_SC_NPROCESSORS_ONLN);

	return ncores;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheCPU_bind
(JNIEnv *env, jobject obj, jint core) {

	(void) env;
	(void) obj;

	cpu_set_t set;
	CPU_ZERO (&set);
	CPU_SET  (core, &set);

	return sched_setaffinity (0, sizeof(set), &set);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheCPU_unbind
(JNIEnv *env, jobject obj) {

	(void) env;
	(void) obj;

	return sched_setaffinity (0, sizeof (cpu_set_t), getFullSet());
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheCPU_getCpuId
(JNIEnv *env, jobject obj) {

	(void) env;
	(void) obj;

	int core = -1;
	cpu_set_t set;

	int error = sched_getaffinity (0, sizeof (set), &set);
	if (error < 0)
		return core; /* -1 */
	for (core = 0; core < CPU_SETSIZE; core++) {
		if (CPU_ISSET (core, &set))
			break;
	}
	return core;
}
