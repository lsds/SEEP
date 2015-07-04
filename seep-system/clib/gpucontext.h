#ifndef __GPU_CONTEXT_H_
#define __GPU_CONTEXT_H_

#include "utils.h"

#include "debug.h"

#include "inputbuffer.h"
#include "outputbuffer.h"

#include <jni.h>

#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif

#include <pthread.h>

typedef struct gpu_kernel_input {
	int count;
	inputBufferP inputs [MAX__INPUTS];
} gpu_kernel_input_t;

typedef struct gpu_kernel_output {
	int count;
	outputBufferP outputs [MAX_OUTPUTS];
} gpu_kernel_output_t;

typedef struct a_kernel *aKernelP;
typedef struct a_kernel {
	cl_kernel kernel [2];
} a_kernel_t;

typedef struct gpu_kernel {
	int count;
	aKernelP kernels [MAX_KERNELS]; /* Every query has one or more kernels */
} gpu_kernel_t;

typedef struct gpu_context *gpuContextP;
typedef struct gpu_context {
	cl_device_id device;
	cl_context context;
	cl_program program;
	gpu_kernel_t kernel;
	gpu_kernel_input_t kernelInput;
	gpu_kernel_output_t kernelOutput;
	cl_command_queue queue [2];
	int scheduled;
	cl_event read_event;
#ifdef GPU_PROFILE
	cl_event exec_event[MAX_KERNELS];
#endif
	cl_event write_event;
	long long readCount;
	long long writeCount;

} gpu_context_t;

gpuContextP gpu_context (cl_device_id, cl_context, cl_program, int, int, int);

void gpu_context_free (gpuContextP);

void gpu_context_setInput (gpuContextP, int, void *, int);

void gpu_context_setOutput (gpuContextP, int, void *, int, int, int, int, int);

void gpu_context_setKernel (gpuContextP, int,
		const char *, void (*callback)(cl_kernel, gpuContextP, int *), int *);

void gpu_context_setKernel_another (gpuContextP, int,
		const char *, void (*callback)(cl_kernel, gpuContextP, int *, long *), int *, long *);

void gpu_context_configureKernel (gpuContextP, int,
		const char *, void (*callback)(cl_kernel, gpuContextP, int *, long *), int *, long *);

void gpu_context_waitForReadEvent (gpuContextP);

void gpu_context_waitForWriteEvent (gpuContextP);

void gpu_context_waitForExecEvent (gpuContextP);

void gpu_context_profileQuery (gpuContextP);

void gpu_context_flush (gpuContextP);

void gpu_context_finish (gpuContextP);

void gpu_context_submitTask (gpuContextP, size_t *, size_t *);

void gpu_context_submitKernel (gpuContextP, size_t *, size_t *);

void gpu_context_writeInput (gpuContextP,
		void (*callback)(gpuContextP, JNIEnv *, jobject, int, int, int),
		JNIEnv *, jobject, int);

void gpu_context_readOutput (gpuContextP,
		void (*callback)(gpuContextP, JNIEnv *, jobject, int, int, int),
		JNIEnv *, jobject, int);

void gpu_context_moveInputBuffers (gpuContextP);

void gpu_context_moveDirectInputBuffers (gpuContextP, int *, int *);

void gpu_context_moveOutputBuffers (gpuContextP);

#endif /* __GPU_CONTEXT_H_ */
