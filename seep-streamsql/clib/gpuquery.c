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
	char msg [2048]; /* Compiler message */
	size_t length;
	const char *flags = "-cl-fast-relaxed-math -cl-nv-verbose";
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
	fprintf(stderr, "%s\n", msg);
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
	if (! p)
		return NULL;
	int idx = (++p->ndx) % DEPTH;
	dbg ("[DBG] switch to context %d\n", idx);
	return p->contexts[idx];
}

int gpu_query_exec (gpuQueryP q, void *input, void *output, size_t threads, size_t threadsPerGroup) {
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);

	/* Write input */
	gpu_context_writeInput (p, input);

	/* Wait for read event */
	gpu_context_waitForReadEvent (p);

	/* Read output */
	gpu_context_readOutput (p, output);

	/* Submit task */
	gpu_context_submitTask (p, threads, threadsPerGroup);

	return 0;
}

//void gpu_exec () { /* Execute task within current context */
//
//	gpuContextP q = contexts[current_context];
//	gpu_context_switch (q);
//	/*
//	*/
//
//	/* Wait for write event */
//	wait_for_event (q->write_event);
//	/*
//	Make sure the write_event is not queried until
//	the second invocation.
//	*/
//
//	/* Write input */
//	write_input (q->theInput);
//	/*
//	This is a callback to Java. The query operator holds pointers
//	*/
//
//	/* Start task */
//	submit_task ();
//
//	/*
//	Tasks can be submitted before we read because, although
//	it is a requirement to finish the execution of the previous
//	task before the new one begins, the task flow blocks on
//	clWaitForEvent (q->read_event), not clFinish(q->commandQueue).
//	*/
//	/* Wait for read event */
//	wait_for_event (q->read_event);
//
//	/* Read output */
//	read_output (q->theOutput);
//}


