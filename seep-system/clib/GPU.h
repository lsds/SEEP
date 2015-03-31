#ifndef __GPU_H_
#define __GPU_H_

#include "gpucontext.h"
#include "openclerrorcode.h"

#include "outputbuffer.h"

#include <jni.h>

typedef struct query_operator *queryOperatorP;
typedef struct query_operator {
	/* void (*setKernels) (cl_kernel, gpuContextP, int *); */
	void (*writeInput) (gpuContextP, JNIEnv *, jobject, int, int, int);
	void (*readOutput) (gpuContextP, JNIEnv *, jobject, int, int, int);
	void (*execKernel) ();
} query_operator_t;

void gpu_init ();

void gpu_free ();

/* Set one context per query */
int gpu_getQuery (const char *, int, int, int, JNIEnv *);

int gpu_setInput (int, int, void *, int);

int gpu_setOutput (int, int, void *, int, int);

outputBufferP gpu_getOutput (int, int);

int gpu_setKernel (int, int, const char *, void (*callback) (cl_kernel, gpuContextP, int *), int *);

/* Execute task */
int gpu_exec (int, size_t, size_t, queryOperatorP, JNIEnv *, jobject);

int gpu_custom_exec (int, size_t, size_t, queryOperatorP, JNIEnv *, jobject, size_t, size_t);

/* Copy both input and output buffers */
int gpu_testJNIDataMovement (int, queryOperatorP, JNIEnv *, jobject);

int gpu_copyInputBuffers (int, queryOperatorP, JNIEnv *, jobject);

int gpu_copyOutputBuffers (int, queryOperatorP, JNIEnv *, jobject);

int gpu_moveInputBuffers (int, queryOperatorP, JNIEnv *, jobject);

int gpu_moveOutputBuffers (int, queryOperatorP, JNIEnv *, jobject);

int gpu_moveInputAndOutputBuffers (int, queryOperatorP, JNIEnv *, jobject);

int gpu_testDataMovement (int, queryOperatorP, JNIEnv *, jobject);

int gpu_testOverlap (int, size_t, size_t, queryOperatorP, JNIEnv *, jobject);

#endif /* SEEP_GPU_H_ */
