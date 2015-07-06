#ifndef __GPU_QUERY_H_
#define __GPU_QUERY_H_

#include "gpucontext.h"

#include "GPU.h"

#include <jni.h>

#include <pthread.h>

typedef struct {
	JavaVM *jvm;
	JNIEnv *env;
	int qid;
} handler_t;

typedef struct gpu_query *gpuQueryP;
typedef struct gpu_query {
	
	cl_device_id device;
	cl_context  context;
	cl_program  program;
	
	int ndx;
	int phase;
	pthread_t thr;

	gpuContextP contexts [NCONTEXTS];

} gpu_query_t;

/* Constractor */
gpuQueryP gpu_query_new (cl_device_id, cl_context, const char *, int, int, int);

void gpu_query_init (gpuQueryP, JNIEnv *, int);

void gpu_query_free (gpuQueryP);

/* Execute query in another context */
gpuContextP gpu_context_switch (gpuQueryP);

int gpu_query_setInput (gpuQueryP, int, void *, int);

int gpu_query_setOutput (gpuQueryP, int, void *, int, int, int, int, int);

int gpu_query_setKernel (gpuQueryP, int,
		const char *, void (*callback)(cl_kernel, gpuContextP, int *, long *), int *, long *);

int gpu_query_configureKernel (gpuQueryP, int,
		const char *, void (*callback)(cl_kernel, gpuContextP, int *, long *), int *, long *);

/* Execute task */
int gpu_query_exec (gpuQueryP, size_t *, size_t *, queryOperatorP,
		JNIEnv *, jobject, int);

int gpu_query_exec_direct (gpuQueryP, size_t *, size_t *, int *, int *, queryOperatorP,
		JNIEnv *, jobject, int);

#endif /* __GPU_QUERY_H_ */
