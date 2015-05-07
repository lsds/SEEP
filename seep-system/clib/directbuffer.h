#ifndef __DIRECT_BUFFER_H_
#define __DIRECT_BUFFER_H_

#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif

#include <jni.h>

typedef struct direct_buffer *directBufferP;
typedef struct direct_buffer {
	int size;
	cl_mem pinned_buffer;
	void  *mapped_buffer;
} direct_buffer_t;

directBufferP getDirectBuffer (cl_context, cl_command_queue, int, int);

void freeDirectBuffer (directBufferP, cl_command_queue);

int getDirectBufferSize (directBufferP);

#endif /* __DIRECT_BUFFER_H_ */
