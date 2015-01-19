#include "gpucontext.h"

#include "openclerrorcode.h"
#include "debug.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <CL/cl.h>

gpuContextP gpu_context (
	cl_device_id device, 
	cl_context  context, 
	cl_program  program,
	int        _kernels,
	int         _inputs,
	int        _outputs) 
{
	gpuContextP q = (gpuContextP) malloc (sizeof(gpu_context_t));
	if (! q) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit(1);	
	}
	/* Initialise OpenCL execution context */
	q->device = device;
	q->context = context;
	q->program = program; 
	q->kernel.count = _kernels;
	q->kernelInput.count = _inputs;
	q->kernelOutput.count = _outputs;
	/* Create command queues */
	int error;
	q->queue[0] = clCreateCommandQueue (
		q->context, 
		q->device, 
		CL_QUEUE_PROFILING_ENABLE, 
		&error);
	if (! q->queue[0]) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	q->queue[1] = clCreateCommandQueue (
		q->context, 
		q->device, 
		CL_QUEUE_PROFILING_ENABLE, 
		&error);
	if (! q->queue[1]) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	q->scheduled = 0; /* No read or write events scheduled */
	q->readCount = 0;
	q->writeCount = 0;
	return q;
}

void gpu_context_free (gpuContextP q) {
	if (q) {
		int i;
		/* Free input(s) */
		for (i = 0; i < q->kernelInput.count; i++)
			freeInputBuffer(q->kernelInput.inputs[i], q->queue[0]);
		/* Free output(s) */
		for (i = 0; i < q->kernelOutput.count; i++)
			freeOutputBuffer(q->kernelOutput.outputs[i], q->queue[0]);
		/* Free kernel(s) */
		for (i = 0; i < q->kernel.count; i++) {
			clReleaseKernel(q->kernel.kernels[i]->kernel[0]);
			clReleaseKernel(q->kernel.kernels[i]->kernel[1]);
			free (q->kernel.kernels[i]);
		}
		if (q->queue[0])
			clReleaseCommandQueue(q->queue[0]);
		if (q->queue[1])
			clReleaseCommandQueue(q->queue[1]);
		free(q);
	}
}

void gpu_context_setKernel (gpuContextP q, int ndx,
		const char *name, void (*callback)(cl_kernel, gpuContextP, int *), int *args) {
	if (ndx < 0 || ndx >= q->kernel.count)
		return;
	int error = 0;
	int i;
	q->kernel.kernels[ndx] = (aKernelP) malloc (sizeof(a_kernel_t));
	for (i = 0; i < 2; i++) {
		q->kernel.kernels[ndx]->kernel[i] = clCreateKernel (q->program, name, &error);
		if (! q->kernel.kernels[ndx]->kernel[i]) {
			fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
			exit (1);
		} else {
			(*callback) (q->kernel.kernels[ndx]->kernel[i], q, args);
		}
	}
	return;
}

void gpu_context_setInput (gpuContextP q, int ndx, int size) {
	if (ndx < 0 || ndx >= q->kernelInput.count)
		return;
	q->kernelInput.inputs[ndx] = getInputBuffer (q->context, q->queue[0], size); 
}

void gpu_context_setOutput (gpuContextP q, int ndx, int size, int writeOnly) {
	if (ndx < 0 || ndx >= q->kernelOutput.count)
		return;
	q->kernelOutput.outputs[ndx] = getOutputBuffer (q->context, q->queue[0],
			size, writeOnly);
}

#ifdef GPU_VERBOSE
static int getEventReferenceCount (cl_event event) {
	int error = 0;
	cl_uint count = 0;
	error = clGetEventInfo (
		event,
		CL_EVENT_REFERENCE_COUNT,
		sizeof(cl_uint),
		(void *) &count,
		NULL);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		return -1;
	} else
		return (int) count;
}

static char *getEventCommandStatus (cl_event event) {
	int error = 0;
	cl_int status;
	error = clGetEventInfo (
		event,
		CL_EVENT_COMMAND_EXECUTION_STATUS,
		sizeof(cl_int),
		(void *) &status,
		NULL);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		return getCommandExecutionStatus(-1);
	} else
		return getCommandExecutionStatus(status);
}

static char *getEventCommandType (cl_event event) {
	int error = 0;
	cl_command_type type;
	error = clGetEventInfo (
		event,
		CL_EVENT_COMMAND_TYPE,
		sizeof(cl_command_type),
		(void *) &type,
		NULL);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		return getCommandType(-1);
	} else
		return getCommandType(type);
}
#endif

void gpu_context_waitForReadEvent (gpuContextP q) {
	if (q->scheduled < 1)
		return;
	dbg("[DBG] read : %2d references, command %30s status %20s \n",
			getEventReferenceCount (q->read_event),
			getEventCommandType    (q->read_event),
			getEventCommandStatus  (q->read_event)
			);
	/* Wait for read event */
	int error = clWaitForEvents(1, &(q->read_event));
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	q->readCount -= 1;
	clReleaseEvent(q->read_event);
	return ;
}

void gpu_context_waitForExecEvent (gpuContextP q) {
	if (q->scheduled < 1)
		return;
	/* Wait for execute event */
	int error = clWaitForEvents(1, &(q->exec_event));
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	clReleaseEvent(q->exec_event);
	return ;
}

void gpu_context_waitForWriteEvent (gpuContextP q) {
	if (q->scheduled < 1)
		return;
	dbg("[DBG] write: %2d references, command %30s status %20s \n",
			getEventReferenceCount (q->write_event),
			getEventCommandType    (q->write_event),
			getEventCommandStatus  (q->write_event)
			);
	/* Wait for write event */
	int error = clWaitForEvents(1, &(q->write_event));
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	q->writeCount -= 1;
	clReleaseEvent(q->write_event);
	return ;
}

void gpu_context_flush (gpuContextP q) {
	int error = 0;
	error |= clFlush (q->queue[0]);
	error |= clFlush (q->queue[1]);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
}

void gpu_context_finish (gpuContextP q) {
	if (q->scheduled < 1)
		return;
	/* There are tasks scheduled */
	int error = 0;
	error |= clFinish (q->queue[0]);
	error |= clFinish (q->queue[1]);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
}

void gpu_context_submitTask (gpuContextP q, size_t threads, size_t threadsPerGroup) {

	int error = 0;
	/* Write */
	error = clEnqueueWriteBuffer (
		q->queue[0],
		q->kernelInput.inputs[0]->device_buffer,
		CL_FALSE,
		0,
		q->kernelInput.inputs[0]->size,
		q->kernelInput.inputs[0]->mapped_buffer,
		0, NULL, &q->write_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	q->writeCount += 1;

	/* Execute */
	error = clEnqueueNDRangeKernel (
		q->queue[0],
		q->kernel.kernels[0]->kernel[0],
		1,
		NULL,
		&threads,
		&threadsPerGroup,
		0, NULL, NULL);

	/* Execute and get event notification */
	/*
	error = clEnqueueNDRangeKernel (
		q->queue[0],
		q->kernel.kernels[0]->kernel[0],
		1,
		NULL,
		&threads,
		&threadsPerGroup,
		0, NULL, &q->exec_event);
	*/
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}

	/* Read */
	error = clEnqueueReadBuffer (
		q->queue[0],
		q->kernelOutput.outputs[0]->device_buffer,
		CL_FALSE,
		0,
		q->kernelOutput.outputs[0]->size,
		q->kernelOutput.outputs[0]->mapped_buffer,
		0, NULL, &q->read_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	q->readCount += 1;

	/* Flush command queues */
	gpu_context_flush (q);
	q->scheduled = 1;

	return;
}

void gpu_context_writeInput (gpuContextP q,
		void (*callback)(gpuContextP, JNIEnv *, jobject, int, int, int),
		JNIEnv *env, jobject obj, int qid) {
	int idx;
	for (idx = 0; idx < q->kernelInput.count; idx++)
		(*callback) (q, env, obj, qid, idx, 0);
	return;
}

void gpu_context_readOutput (gpuContextP q,
		void (*callback)(gpuContextP, JNIEnv *, jobject, int, int, int),
		JNIEnv *env, jobject obj, int qid) {
	int idx;
	for (idx = 0; idx < q->kernelOutput.count; idx++)
		(*callback) (q, env, obj, qid, idx, 0);
	return;
}
