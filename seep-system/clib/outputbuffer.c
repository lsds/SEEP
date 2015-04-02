#include "outputbuffer.h"

#include "openclerrorcode.h"

#include "debug.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <stdint.h>

static int IS_ZERO_COPY (void *ptr, int len) {
	dbg("p is %lu\n", (uintptr_t) ptr);
	if ((uintptr_t) ptr % 256 != 0) /* 256-byte alignment*/
		return 0;
	if (len % 64 != 0) /* Cache alignment */
		return 0;
	return 1;
}

outputBufferP getOutputBuffer (cl_context context, cl_command_queue queue,
		void *buffer, int size, int writeOnly) {
	
	outputBufferP p = malloc(sizeof(output_buffer_t));
	if (! p) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit (1);
	}
	p->size = size;
	p->writeOnly = (unsigned char) writeOnly;
	int error;
	cl_mem_flags flags;
	if (writeOnly)
		flags = CL_MEM_WRITE_ONLY;
	else
		flags = CL_MEM_READ_WRITE;
	/* Set p->device_buffer */
	p->device_buffer = clCreateBuffer (
		context, 
		flags,
		p->size, 
		NULL, 
		&error);
	if (! p->device_buffer) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	if (buffer == NULL) {
		p->pinned_buffer = clCreateBuffer (
		context, 
		CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR,
		p->size, 
		NULL, 
		&error);
	} else {
		if (! IS_ZERO_COPY (buffer, size)) {
			fprintf(stderr, "warning: buffer is not a zero-copy buffer (%s)\n", __FUNCTION__);
		}
		p->pinned_buffer = clCreateBuffer (
		context,
		CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR,
		p->size,
		buffer,
		&error);
	}
	if (! p->pinned_buffer) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	if (! IS_ZERO_COPY (p->pinned_buffer, size)) {
		fprintf(stderr, "opencl warning: buffer is not a zero-copy buffer (%s)\n", __FUNCTION__);
	}
	p->mapped_buffer = (void *) clEnqueueMapBuffer (
		queue, 
		p->pinned_buffer,
		CL_TRUE, 
		CL_MAP_READ, 
		0, 
		p->size, 
		0, NULL, NULL, 
		&error);
	if (! p->mapped_buffer) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return p;
}

outputBufferP pinOutputBuffer (cl_context context, int size) {

	outputBufferP p = malloc(sizeof(output_buffer_t));
	if (! p) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit (1);
	}
	p->size = size;
	p->writeOnly = 1;
	int error;
	/* Set p->device_buffer */
	p->device_buffer = NULL;
	p->pinned_buffer = clCreateBuffer (
		context,
		CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR,
		p->size,
		NULL,
		&error);
	if (! p->pinned_buffer) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	p->mapped_buffer = NULL;
	return p;
}

int getOutputBufferSize (outputBufferP b) {
	return b->size;
}

void freeOutputBuffer (outputBufferP b, cl_command_queue queue) {
	if (b) {
		if (b->mapped_buffer)
			clEnqueueUnmapMemObject (
				queue, 
				b->pinned_buffer, 
				(void *) b->mapped_buffer,
				0, NULL, NULL); /* Zero dependencies */
		if (b->pinned_buffer)
			clReleaseMemObject(b->pinned_buffer);
		if (b->device_buffer)
			clReleaseMemObject(b->device_buffer);
		free (b);
	}
}
