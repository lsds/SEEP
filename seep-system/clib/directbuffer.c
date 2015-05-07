#include "directbuffer.h"

#include "openclerrorcode.h"

#include "debug.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

directBufferP getDirectBuffer (cl_context context, cl_command_queue queue, int size, int readOnly) {
	
	directBufferP p = malloc(sizeof(direct_buffer_t));
	if (! p) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit(1);
	}
	p->size = size;
	int error;
	/* Set p->pinned_memory */
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
	/* Set p->mapped_memory */
	p->mapped_buffer = (void *) clEnqueueMapBuffer (
		queue, 
		p->pinned_buffer,
		CL_TRUE, /* Blocking */
		(readOnly == 0) ? CL_MAP_WRITE : CL_MAP_READ,
		0, 
		p->size, 
		0, NULL, NULL, /* Zero dependencies */
		&error);
	if (! p->mapped_buffer) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return p;
}

int getDirectBufferSize (directBufferP b) {
	return b->size;
}

void freeDirectBuffer (directBufferP b, cl_command_queue queue) {
	if (b) {
		if (b->mapped_buffer)
			clEnqueueUnmapMemObject (
				queue, 
				b->pinned_buffer, 
				(void *) b->mapped_buffer,
				0, NULL, NULL); /* Zero dependencies */
		if (b->pinned_buffer)
			clReleaseMemObject(b->pinned_buffer);
		free (b);
	}
}

