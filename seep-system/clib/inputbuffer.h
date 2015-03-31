#ifndef __INPUT_BUFFER_H_
#define __INPUT_BUFFER_H_

#include <CL/cl.h>

#include <jni.h>

typedef struct input_buffer *inputBufferP;
typedef struct input_buffer {
	int size;
	cl_mem device_buffer;
	cl_mem pinned_buffer;
	void  *mapped_buffer;
} input_buffer_t;

inputBufferP getInputBuffer (cl_context, cl_command_queue, void *, int);

inputBufferP pinInputBuffer (cl_context, int);

void freeInputBuffer (inputBufferP, cl_command_queue);

int getInputBufferSize (inputBufferP);

#endif /* __INPUT_BUFFER_H_ */
