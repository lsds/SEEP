#include "gpuquery.h"

#include "openclerrorcode.h"
#include "debug.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <CL/cl.h>

gpuQueryP gpu_query_new (cl_device_id device, cl_context context,
	const char *source, int _kernels, int _inputs, int _outputs) {
	
	int i;
	int error = 0;
	char msg [4096]; /* Compiler message */
	size_t length;
	const char *flags = "-cl-fast-relaxed-math -cl-nv-verbose -cl-nv-arch sm_30";
	gpuQueryP p = (gpuQueryP) malloc (sizeof(gpu_query_t));
	if (! p) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit(1);
	}
	p->device = device;
	p->context = context;
	/* Create program */
	p->program = clCreateProgramWithSource (
		p->context, 
		1, 
		(const char **) &source, 
		NULL, 
		&error);
	if (! p->program) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	/* Build program */
	error = clBuildProgram (
		p->program, 
		0, 
		NULL, 
		flags, 
		NULL, 
		NULL);
	/* Get compiler info (or error) */
	clGetProgramBuildInfo (
		p->program, 
		p->device, 
		CL_PROGRAM_BUILD_LOG, 
		sizeof(msg), 
		msg, 
		&length);
	fprintf(stderr, "%s (%zu chars)\n", msg, length);
	fflush (stderr);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	p->ndx = -1;
	for (i = 0; i < DEPTH; i++) {
		p->contexts[i] =
			gpu_context (p->device, p->context, p->program, _kernels, _inputs, _outputs);
	}
	return p;
}

void gpu_query_free (gpuQueryP p) {
	int i;
	if (p) {
		for (i = 0; i < DEPTH; i++)
			gpu_context_free (p->contexts[i]);
		if (p->program)
			clReleaseProgram (p->program);
		free (p);
	}
}

int gpu_query_setInput (gpuQueryP q, int ndx, int size) {
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernelInput.count)
		return -1;
	int i;
	for (i = 0; i < DEPTH; i++)
		gpu_context_setInput (q->contexts[i], ndx, size);
	return 0;
}

int gpu_query_setOutput (gpuQueryP q, int ndx, int size, int writeOnly) {
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernelOutput.count)
		return -1;
	int i;
	for (i = 0; i < DEPTH; i++)
		gpu_context_setOutput (q->contexts[i], ndx, size, writeOnly);
	return 0;
}

int gpu_query_setKernel (gpuQueryP q, int ndx, const char * name,
		void (*callback)(cl_kernel, gpuContextP, int *), int *args) {
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernel.count)
		return -1;
	int i;
	for (i = 0; i < DEPTH; i++)
		gpu_context_setKernel (q->contexts[i], ndx, name, callback, args);
	return 0;
}

gpuContextP gpu_context_switch (gpuQueryP p) {
	if (! p) {
		fprintf (stderr, "error: null query\n");
		return NULL;
	}
#ifdef GPU_VERBOSE
	int current = (p->ndx) % DEPTH;
#endif
	int next = (++p->ndx) % DEPTH;
#ifdef GPU_VERBOSE
	if (current >= 0)
		dbg ("[DBG] switch from %d (%lld read(s), %lld write(s)) to context %d\n",
			current, p->contexts[current]->readCount, p->contexts[current]->writeCount, next);
#endif
	return p->contexts[next];
}

int gpu_query_exec (gpuQueryP q, size_t threads, size_t threadsPerGroup,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	
	/* gpu_context_finish (p); */
	
	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);

	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	/* Wait for execute event */
	/* gpu_context_waitForExecEvent (p); */

	/* Wait for read event */
	gpu_context_waitForReadEvent (p);
	
	/* Read output */
	gpu_context_readOutput (p, operator->readOutput, env, obj, qid);

	/* Submit task */
	gpu_context_submitTask (p, threads, threadsPerGroup);
	
	gpu_context_flush (p);

	return 0;
}
