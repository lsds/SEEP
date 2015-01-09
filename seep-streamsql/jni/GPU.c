/*
 * GPU.c
 */

#include <jni.h>

#include <CL/cl.h>

#include <stdio.h>
#include <string.h>

#include <sys/time.h>

/* Auto-generated JNI header */
#include "uk_ac_imperial_lsds_streamsql_op_gpu_GPU.h"

#include "timer.h"
#include "openclerrorcode.h"
#include "debug.h"

cl_platform_id platform;
cl_device_id device;
cl_context context;
cl_command_queue commandQueue[2];
cl_program program;
cl_kernel kernel[2];

static cl_mem pinnedInputBuffer,  pinnedOutputBuffer; /* Pinned memory */
static jbyte *mappedInputBuffer, *mappedOutputBuffer; /* Mapped pointer to pinned memory */
/* The "mirror" */
static cl_mem _pinnedInputBuffer, _pinnedOutputBuffer;
static jbyte *_mappedInputBuffer,*_mappedOutputBuffer;

static jbyte *mappedInputBufferPtrs  [2]; /* { mappedInputBuffer,  _mappedInputBuffer  } */
static jbyte *mappedOutputBufferPtrs [2]; /* { mappedOutputBuffer, _mappedOutputBuffer } */

static cl_mem pinnedInputBufferPtrs  [2]; /* { pinnedInputBuffer,  _pinnedInputBuffer  } */
static cl_mem pinnedOutputBufferPtrs [2]; /* { pinnedOutputBuffer, _pinnedOutputBuffer } */

/* Window start and end pointer */
static cl_mem pinnedWindowStartPointersBuffer,  pinnedWindowEndPointersBuffer; /* Pinned memory */
static int *mappedWindowStartPointersBuffer, *mappedWindowEndPointersBuffer;   /* Mapped pointer to pinned memory */

static cl_event  readEvent,  writeEvent;
static cl_event _readEvent, _writeEvent;

static cl_event  readEventPtrs [2]; /* { readEvent, _readEvent } */
static cl_event writeEventPtrs [2];

static int ndx = 0; /* Index into *Ptrs[] */

static struct input_buffer_t {
	jbyte *mappedInputBufferPtr;
	cl_mem pinnedInputBufferPtr;
	cl_event eventPtr;
	cl_command_queue queuePtr;
} theInputBuffer;

static struct output_buffer_t {
	jbyte *mappedOutputBufferPtr;
	cl_mem pinnedOutputBufferPtr;
	cl_event eventPtr;
	cl_command_queue queuePtr;
} theOutputBuffer;

cl_mem inputBuffer, outputBuffer;
cl_mem windowStartPointersBuffer, windowEndPointersBuffer;

int _input, _output; /* Input and output size */
int _startPointers, _endPointers;

cl_ulong _timestamp = 0; /* Reference timestamp */

cl_event w0_event, w1_event;
cl_event x0_event, x1_event;
cl_event r0_event, r1_event;

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_getPlatform
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	int error = 0;
	cl_uint count = 0;
	error = clGetPlatformIDs (1, &platform, &count);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): cannot get platform\n", error, getErrorMessage(error));
		return -1;
	}
	dbg("Obtained 1/%u platforms available\n", count);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_getDevice
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	int error = 0;
	cl_uint count = 0;
	error = clGetDeviceIDs (platform, CL_DEVICE_TYPE_GPU, 1, &device, &count);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): cannot get device\n", error, getErrorMessage(error));
		return -1;
	}
	dbg("Obtained 1/%u devices available\n", count);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createContext
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	int error = 0;
	context = clCreateContext (0, 1, &device, NULL, NULL, &error);
	if (! context) {
		fprintf(stderr, "error (%d: %s): cannot create compute context\n", error, getErrorMessage(error));
		return -1;
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createCommandQueue
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	int error = 0;
	commandQueue[0] = clCreateCommandQueue (context, device, CL_QUEUE_PROFILING_ENABLE, &error);
	if (! commandQueue[0]) {
		fprintf(stderr, "error (%d: %s): failed to create command queue\n", error, getErrorMessage(error));
		return -1;
	}
	commandQueue[1] = clCreateCommandQueue (context, device, CL_QUEUE_PROFILING_ENABLE, &error);
	if (! commandQueue[1]) {
		fprintf(stderr, "error (%d: %s): failed to create command queue\n", error, getErrorMessage(error));
		return -1;
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createProgram
(JNIEnv *env, jobject object, jstring source) {

	(void) object; /* Supress warnings */

	char blog [2048];
	size_t len;
	int error = 0;
	const char *flags = "-cl-fast-relaxed-math -cl-nv-verbose";
	const char *_source = (*env)->GetStringUTFChars(env, source, NULL);
	program = clCreateProgramWithSource(context, 1, (const char **) &_source, NULL, &error);
	if (! program) {
		fprintf(stderr, "error (%d: %s): failed to create program\n", error, getErrorMessage(error));
		/* Free source */
		(*env)->ReleaseStringUTFChars(env, source, _source);
		return -1;
	}
	/* Build the program */
	error = clBuildProgram (program, 0, NULL, flags, NULL, NULL);
	if (error != CL_SUCCESS)
		fprintf(stderr, "error (%d: %s): failed to build program\n", error, getErrorMessage(error));
	/* Get compiler info (or error) */
	clGetProgramBuildInfo (program, device, CL_PROGRAM_BUILD_LOG, sizeof(blog), blog, &len);
	printf("%s\n", blog);
	/* Free source */
	(*env)->ReleaseStringUTFChars(env, source, _source);
	return error;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createKernel
(JNIEnv *env, jobject object, jstring name) {

	(void) object; /* Supress warnings */

	int error = 0;
	const char *_name = (*env)->GetStringUTFChars(env, name, NULL);
	kernel[0] = clCreateKernel (program, _name, &error);
	if (! kernel[0] || error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to create kernel\n", error, getErrorMessage(error));
		(*env)->ReleaseStringUTFChars(env, name, _name);
		return -1;
	}
	kernel[1] = clCreateKernel (program, _name, &error);
	if (! kernel[0] || error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to create kernel\n", error, getErrorMessage(error));
		(*env)->ReleaseStringUTFChars(env, name, _name);
		return -1;
	}
	(*env)->ReleaseStringUTFChars(env, name, _name);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_setProjectionKernelArgs
(JNIEnv *env, jobject object, jint tuples, jint size, jboolean overlap) {

	(void) env;
	(void) object; /* Supress warnings */

	int error = 0;

	size_t _size = (size_t) size; /* Size of local memory */
	if (overlap)
		_size /= 2;

	/* Configure the two kernels */
	int i;
	for (i = 0; i < 2; i++) {
		error  = clSetKernelArg (kernel[i], 0, sizeof(int)   , (void *) &tuples);
		error |= clSetKernelArg (kernel[i], 1, sizeof(int)   , (void *) &_input);
		error |= clSetKernelArg (kernel[i], 2, sizeof(cl_mem), (void *) &inputBuffer);
		error |= clSetKernelArg (kernel[i], 3, sizeof(cl_mem), (void *) &outputBuffer);
		/* Local memory */
		error |= clSetKernelArg (kernel[i], 4, (size_t) _size, (void *) NULL);
		error |= clSetKernelArg (kernel[i], 5, (size_t) _size, (void *) NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to set kernel arguments\n", error, getErrorMessage(error));
			return -1;
		}
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_setReductionKernelArgs
(JNIEnv *env, jobject object, jint tuples, jint size, jboolean overlap) {

	(void) env;
	(void) object; /* Supress warnings */

	int error = 0;

	size_t _size = (size_t) size; /* Size of local memory */
	if (overlap)
		_size /= 2;
	printf("local size is %zu\n", _size);

	/* Configure the two kernels */
	int i;
	for (i = 0; i < 2; i++) {
		error  = clSetKernelArg (kernel[i], 0, sizeof(int)   , (void *) &tuples);
		error |= clSetKernelArg (kernel[i], 1, sizeof(int)   , (void *) &_input);
		error |= clSetKernelArg (kernel[i], 2, sizeof(cl_mem), (void *) &inputBuffer);
		error |= clSetKernelArg (kernel[i], 3, sizeof(cl_mem), (void *) &outputBuffer);
		error |= clSetKernelArg (kernel[i], 4, sizeof(cl_mem), (void *) &windowStartPointersBuffer);
		error |= clSetKernelArg (kernel[i], 5, sizeof(cl_mem), (void *) &windowEndPointersBuffer);
		/* Local memory */
		error |= clSetKernelArg (kernel[i], 6, (size_t) _size, (void *) NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to set kernel arguments\n", error, getErrorMessage(error));
			return -1;
		}
	}
	return 0;
}

JNIEXPORT jlong JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createInputBuffer
(JNIEnv *env, jobject object, jint size) {

	(void) env;
	(void) object; /* Supress warnings */

	_input = size;
	int error = 0;
	inputBuffer = clCreateBuffer (context, CL_MEM_READ_ONLY, _input, NULL, &error);
	if (! inputBuffer) {
		fprintf(stderr, "error (%d :%s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	pinnedInputBuffer = clCreateBuffer (context, CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR, _input, NULL, &error);
	if (! pinnedInputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	mappedInputBuffer = (jbyte *) clEnqueueMapBuffer(commandQueue[0], pinnedInputBuffer, CL_TRUE, CL_MAP_WRITE, 0, _input, 0, NULL, NULL, &error);
	if (! mappedInputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to map device memory\n", error, getErrorMessage(error));
		return -1;
	}
	/* Mirror mapped memory */
	_pinnedInputBuffer = clCreateBuffer (context, CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR, _input, NULL, &error);
	if (! _pinnedInputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	_mappedInputBuffer = (jbyte *) clEnqueueMapBuffer(commandQueue[0], _pinnedInputBuffer, CL_TRUE, CL_MAP_WRITE, 0, _input, 0, NULL, NULL, &error);
	if (! _mappedInputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to map device memory\n", error, getErrorMessage(error));
		return -1;
	}
	/*
	 * Initialise `theInputBuffer`
	 */
	mappedInputBufferPtrs[0] = mappedInputBuffer; /* Initialise elements of arrays */
	mappedInputBufferPtrs[1] =_mappedInputBuffer;

	pinnedInputBufferPtrs[0] = pinnedInputBuffer;
	pinnedInputBufferPtrs[1] =_pinnedInputBuffer;

	writeEventPtrs[0] = writeEvent;
	writeEventPtrs[1] =_writeEvent;

	ndx = 0;
	theInputBuffer.mappedInputBufferPtr = mappedInputBufferPtrs[ndx];
	theInputBuffer.pinnedInputBufferPtr = pinnedInputBufferPtrs[ndx];
	theInputBuffer.eventPtr = writeEventPtrs[ndx];
	theInputBuffer.queuePtr = commandQueue[ndx];

	/* Note that this pointer is not used unless the data movement across
	 * the JNI boundary is handled by the Java host thread.
	 * */
	return (long) mappedInputBuffer;
}

JNIEXPORT jlong JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createOutputBuffer
(JNIEnv *env, jobject object, jint size) {

	(void) env;
	(void) object; /* Supress warnings */

	_output = size;
	int error = 0;
	outputBuffer = clCreateBuffer (context, CL_MEM_WRITE_ONLY, _output, NULL, &error);
	if (! outputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	pinnedOutputBuffer = clCreateBuffer (context, CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR,
		_output, NULL, &error);
	if (! pinnedOutputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	mappedOutputBuffer = (jbyte *) clEnqueueMapBuffer(commandQueue[0], pinnedOutputBuffer,
		CL_TRUE, CL_MAP_READ, 0, _output, 0, NULL, NULL, &error);
	if (! mappedOutputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to map device memory\n", error, getErrorMessage(error));
		return -1;
	}
	/* Mirror mapped memory */
	_pinnedOutputBuffer = clCreateBuffer (context, CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR,
		_output, NULL, &error);
	if (! _pinnedOutputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	_mappedOutputBuffer = (jbyte *) clEnqueueMapBuffer(commandQueue[0], _pinnedOutputBuffer,
		CL_TRUE, CL_MAP_READ, 0, _output, 0, NULL, NULL, &error);
	if (! _mappedOutputBuffer) {
		fprintf(stderr, "error (%d: %s): failed to map device memory\n", error, getErrorMessage(error));
		return -1;
	}
	/*
	 * Initialise `theOutputBuffer`
	 */
	mappedOutputBufferPtrs[0] = mappedOutputBuffer; /* Initialise elements of arrays */
	mappedOutputBufferPtrs[1] =_mappedOutputBuffer;

	pinnedOutputBufferPtrs[0] = pinnedOutputBuffer;
	pinnedOutputBufferPtrs[1] =_pinnedOutputBuffer;

	readEventPtrs[0] = readEvent;
	readEventPtrs[1] =_readEvent;

	ndx = 0;
	theOutputBuffer.mappedOutputBufferPtr = mappedOutputBufferPtrs[ndx];
	theOutputBuffer.pinnedOutputBufferPtr = pinnedOutputBufferPtrs[ndx];
	theOutputBuffer.eventPtr = readEventPtrs[ndx];
	theOutputBuffer.queuePtr = commandQueue[ndx];

	/* Note that this pointer is not used unless the data movement across
	 * the JNI boundary is handled by the Java host thread.
	 * */
	return (long) mappedOutputBuffer;
}

JNIEXPORT jlong JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createWindowStartPointersBuffer
(JNIEnv *env, jobject object, jint size) {
	(void) env;
	(void) object; /* Supress warnings */

	_startPointers = size * sizeof(int);
	int error = 0;
	windowStartPointersBuffer = clCreateBuffer (context, CL_MEM_READ_ONLY, _startPointers, NULL, &error);
	if (! windowStartPointersBuffer) {
		fprintf(stderr, "error (%d :%s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	pinnedWindowStartPointersBuffer = clCreateBuffer (context, CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR, _startPointers, NULL, &error);
	if (! pinnedWindowStartPointersBuffer) {
		fprintf(stderr, "error (%d: %s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	mappedWindowStartPointersBuffer = (int *) clEnqueueMapBuffer(commandQueue[0], pinnedWindowStartPointersBuffer,
			CL_TRUE, CL_MAP_WRITE, 0, _startPointers, 0, NULL, NULL, &error);
	if (! mappedWindowStartPointersBuffer) {
		fprintf(stderr, "error (%d: %s): failed to map device memory\n", error, getErrorMessage(error));
		return -1;
	}
	return (long) mappedWindowStartPointersBuffer;
}

JNIEXPORT jlong JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createWindowEndPointersBuffer
(JNIEnv *env, jobject object, jint size) {
	(void) env;
	(void) object; /* Supress warnings */

	_endPointers = size * sizeof(int);
	int error = 0;
	windowEndPointersBuffer = clCreateBuffer (context, CL_MEM_READ_ONLY, _endPointers, NULL, &error);
	if (! windowEndPointersBuffer) {
		fprintf(stderr, "error (%d :%s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	pinnedWindowEndPointersBuffer = clCreateBuffer (context, CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR, _endPointers, NULL, &error);
	if (! pinnedWindowEndPointersBuffer) {
		fprintf(stderr, "error (%d: %s): failed to allocate device memory\n", error, getErrorMessage(error));
		return -1;
	}
	mappedWindowEndPointersBuffer = (int *) clEnqueueMapBuffer(commandQueue[0], pinnedWindowEndPointersBuffer,
			CL_TRUE, CL_MAP_WRITE, 0, _endPointers, 0, NULL, NULL, &error);
	if (! mappedWindowEndPointersBuffer) {
		fprintf(stderr, "error (%d: %s): failed to map device memory\n", error, getErrorMessage(error));
		return -1;
	}
	return (long) mappedWindowEndPointersBuffer;
}

float normalise (cl_ulong timestamp) {
	float result = ((float) (timestamp - _timestamp)) / 1000.;
	return result;
}

void clPrintEventProfilingInfo(cl_event event, const char *acronym) {
	cl_ulong q, s, x, f; /* queued, submitted, start, and end timestamps */
	float   _q,_s,_x,_f;
	int error = 0;
	error |= clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_QUEUED, sizeof(cl_ulong), &q, NULL);
	error |= clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_SUBMIT, sizeof(cl_ulong), &s, NULL);
	error |= clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_START , sizeof(cl_ulong), &x, NULL);
	error |= clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_END   , sizeof(cl_ulong), &f, NULL);
	if (_timestamp < 1)
		_timestamp = q;
	_q = normalise(q);
	_s = normalise(s);
	_x = normalise(x);
	_f = normalise(f);
	printf("%5s\t q %10.1f\t s %10.1f\t x %10.1f\t f %10.1f\n", acronym, _q, _s, _x, _f);
}

/*
 * In this execution mode, data movements across the JNI boundary
 * is handled by the host Java thread.
 *
 * Write, execute, and read GPU commands are executed sequentially,
 * one after the other, without overlap.
 */
jint sequentialKernelExecution (jint threads, jint threadsPerGroup, jboolean profile) {

	int error = 0;
	size_t _threads = threads;
	size_t _threadsPerGroup = threadsPerGroup;
	cl_event w_event;
	cl_event x_event;
	cl_event r_event;
	/* Clear GPU command queue */
	clFinish(commandQueue[0]);
	timerP timer;
	tstamp_t dt;
	if (profile) {
		timer = timer_new();
		timer_start(timer);
	}
	/* Write input */
	error = clEnqueueWriteBuffer (commandQueue[0], inputBuffer, CL_FALSE, 0, _input,
		(void *) &mappedInputBuffer[0], 0, NULL, &w_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &w_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): write event failed\n", error, getErrorMessage(error));
		return -1;
	}
	/* Enqueue kernel command */
	error = clEnqueueNDRangeKernel (commandQueue[0], kernel[0], 1, NULL, &_threads, &_threadsPerGroup,
		0, NULL, &x_event);
	if (error != CL_SUCCESS) {
	 	fprintf(stderr, "error (%d: %s): failed to execute kernel\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &x_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): execute event failed\n", error, getErrorMessage(error));
		return -1;
	}
	/* Read output */
	error = clEnqueueReadBuffer (commandQueue[0], outputBuffer, CL_FALSE, 0, _output,
		(void *) &mappedOutputBuffer[0], 0, NULL, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): read event failed\n", error, getErrorMessage(error));
		return -1;
	}
	if (profile) {
		dt = timer_getElapsedTime(timer);
		clPrintEventProfilingInfo(w_event, "W0");
		clPrintEventProfilingInfo(x_event, "X0");
		clPrintEventProfilingInfo(r_event, "R0");
		printf("T=%llu usec\n", dt);
		timer_free(timer);
	}
	clReleaseEvent (w_event);
	clReleaseEvent (x_event);
	clReleaseEvent (r_event);
	return 0;
}

/*
 * In this execution mode, data movements across the JNI boundary
 * is handled by this code.
 *
 * As above, write, execute, and read GPU commands are executed in
 * order, without overlap.
 */
jint sequentialKernelExecutionWithDataMovement (JNIEnv *env, jobject object,
		jint threads, jint threadsPerGroup, jboolean profile) {

	int error = 0;
	size_t _threads = threads;
	size_t _threadsPerGroup = threadsPerGroup;
	cl_event w_event;
	cl_event x_event;
	cl_event r_event;
	/* Clear GPU command queue */
	clFinish(commandQueue[0]);

	jclass class = (*env)->GetObjectClass (env, object);
	jmethodID writeMethod = (*env)->GetMethodID(env, class,  "inputDataMovementCallback",  "(JI)V");
	if (! writeMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void inputDataMovementCallback (long, int)`\n");
		return -1;
	}
	jmethodID  readMethod = (*env)->GetMethodID(env, class, "outputDataMovementCallback", "(JII)V");
	if (! readMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void outputDataMovementCallback (long, int, int)`\n");
		return -1;
	}

	timerP timer;
	tstamp_t dt;
	if (profile) {
		timer = timer_new();
		timer_start(timer);
	}

	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod(env, object, writeMethod, (long) mappedInputBuffer, _input);

	/* Write input */
	error = clEnqueueWriteBuffer (commandQueue[0], inputBuffer, CL_FALSE, 0, _input,
		(void *) &mappedInputBuffer[0], 0, NULL, &w_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &w_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): write event failed\n", error, getErrorMessage(error));
		return -1;
	}
	/* Enqueue kernel command */
	error = clEnqueueNDRangeKernel (commandQueue[0], kernel[0], 1, NULL, &_threads, &_threadsPerGroup,
		0, NULL, &x_event);
	if (error != CL_SUCCESS) {
	 	fprintf(stderr, "error (%d: %s): failed to execute kernel\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &x_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): execute event failed\n", error, getErrorMessage(error));
		return -1;
	}
	/* Read output */
	error = clEnqueueReadBuffer (commandQueue[0], outputBuffer, CL_FALSE, 0, _output,
		(void *) &mappedOutputBuffer[0], 0, NULL, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): read event failed\n", error, getErrorMessage(error));
		return -1;
	}

	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod(env, object, readMethod, (long) mappedOutputBuffer, _output);

	if (profile) {
		dt = timer_getElapsedTime(timer);
		clPrintEventProfilingInfo(w_event, "W0");
		clPrintEventProfilingInfo(x_event, "X0");
		clPrintEventProfilingInfo(r_event, "R0");
		printf("T=%llu usec\n", dt);
		timer_free(timer);
	}
	clReleaseEvent (w_event);
	clReleaseEvent (x_event);
	clReleaseEvent (r_event);
	return 0;
}

jint pipelinedKernelExecution (jint threads, jint threadsPerGroup, jboolean profile) {

	int error = 0;
	size_t _threads = threads / 2;
	size_t _threadsPerGroup = threadsPerGroup / 2;
	int bundle = 1048576 / 2;
	int loops = _input / bundle;
	int step = bundle / 2;
	/* Clear command queues */
	clFinish(commandQueue[0]);
	clFinish(commandQueue[1]);
	timerP timer;
	tstamp_t dt;
	if (profile) {
		timer = timer_new();
		timer_start(timer);
	}
	int i;
	int offset;
	for (i = 0; i < loops; i++) {
		offset = i * bundle;

		/* Step 1.b) */
		error = clEnqueueWriteBuffer (commandQueue[0], inputBuffer, CL_FALSE, offset, step,
				(void *) &mappedInputBuffer[offset], 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
			return -1;
		}

		clFlush(commandQueue[0]);
		/* clFlush(commandQueue[1]); */

		/* Step 2.a) */
		error = clEnqueueNDRangeKernel (commandQueue[0], kernel[0], 1, NULL, &_threads, &_threadsPerGroup,
			0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to execute kernel\n", error, getErrorMessage(error));
			return -1;
		}
		/* Step 2.b) */
		error = clEnqueueWriteBuffer (commandQueue[1], inputBuffer, CL_FALSE, offset + step, step,
			(void *) &mappedInputBuffer[offset + step], 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
			return -1;
		}

		clFlush(commandQueue[0]);
		clFlush(commandQueue[1]);

		/* Step 3.a) */
		error = clEnqueueNDRangeKernel (commandQueue[1], kernel[1], 1, NULL, &_threads,
			&_threadsPerGroup, 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to execute kernel\n", error, getErrorMessage(error));
			return -1;
		}
		/* Step 3.b) */
		error = clEnqueueReadBuffer (commandQueue[0], outputBuffer, CL_FALSE, offset, step,
			(void *) &mappedOutputBuffer[offset], 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
			return -1;
		}

		clFlush(commandQueue[0]);
		clFlush(commandQueue[1]);

		/* Step 1.a) */
		error = clEnqueueReadBuffer (commandQueue[1], outputBuffer, CL_FALSE, offset + step, step,
			(void *) &mappedOutputBuffer[offset + step], 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
			return -1;
		}

	} /* end for loop */
	clFinish(commandQueue[0]);
	clFinish(commandQueue[1]);
	if (profile) {
		dt = timer_getElapsedTime(timer);
		printf("T=%llu usec\n", dt);
		timer_free(timer);
	}
	return 0;
}

jint pipelinedKernelExecutionWithDataMovement (JNIEnv *env, jobject object,
		jint threads, jint threadsPerGroup, jboolean profile) {

	// TODO: Configure overlap accordingly
	int error = 0;
	size_t _threads = threads / 2;
	size_t _threadsPerGroup = threadsPerGroup / 2;
	int bundle = 1048576 / 2;
	int loops = _input / bundle;
	int step = bundle / 2;

	/* Clear command queues */
	clFinish(commandQueue[0]);
	clFinish(commandQueue[1]);

	jclass class = (*env)->GetObjectClass (env, object);
	jmethodID writeMethod = (*env)->GetMethodID(env, class,  "inputDataMovementCallback",  "(JI)V");
	if (! writeMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void inputDataMovementCallback (long, int)`\n");
		return -1;
	}
	jmethodID  readMethod = (*env)->GetMethodID(env, class, "outputDataMovementCallback", "(JII)V");
	if (! readMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void outputDataMovementCallback (long, int, int)`\n");
		return -1;
	}

	timerP timer;
	tstamp_t dt;
	if (profile) {
		timer = timer_new();
		timer_start(timer);
	}
	int i;
	int offset;
	for (i = 0; i < loops; i++) {
		offset = i * bundle;
		/* Copy data across the JNI boundary */
		(*env)->CallVoidMethod(env, object, writeMethod, (long) (mappedInputBuffer + offset), bundle);

		/* Step 1.b) */
		error = clEnqueueWriteBuffer (commandQueue[0], inputBuffer, CL_FALSE, offset, step,
				(void *) &mappedInputBuffer[offset], 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
			return -1;
		}

		clFlush(commandQueue[0]);
		/* clFlush(commandQueue[1]); */

		/* Step 2.a) */
		error = clEnqueueNDRangeKernel (commandQueue[0], kernel[0], 1, NULL, &_threads, &_threadsPerGroup,
			0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to execute kernel\n", error, getErrorMessage(error));
			return -1;
		}
		/* Step 2.b) */
		error = clEnqueueWriteBuffer (commandQueue[1], inputBuffer, CL_FALSE, offset + step, step,
			(void *) &mappedInputBuffer[offset + step], 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
			return -1;
		}

		clFlush(commandQueue[0]);
		clFlush(commandQueue[1]);

		/* Step 3.a) */
		error = clEnqueueNDRangeKernel (commandQueue[1], kernel[1], 1, NULL, &_threads,
			&_threadsPerGroup, 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to execute kernel\n", error, getErrorMessage(error));
			return -1;
		}
		/* Step 3.b) */
		error = clEnqueueReadBuffer (commandQueue[0], outputBuffer, CL_FALSE, offset, step,
			(void *) &mappedOutputBuffer[offset], 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
			return -1;
		}

		clFlush(commandQueue[0]);
		clFlush(commandQueue[1]);

		/* Step 1.a) */
		error = clEnqueueReadBuffer (commandQueue[1], outputBuffer, CL_FALSE, offset + step, step,
			(void *) &mappedOutputBuffer[offset + step], 0, NULL, NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
			return -1;
		}

	} /* end for loop */

	clFinish(commandQueue[0]);
	clFinish(commandQueue[1]);

	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod(env, object, readMethod, (long) mappedOutputBuffer, _output, 0);

	if (profile) {
		dt = timer_getElapsedTime(timer);
		printf("T=%llu usec\n", dt);
		timer_free(timer);
	}

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeKernel
(JNIEnv *env, jobject object, jint threads, jint threadsPerGroup, jboolean overlap, jboolean profile) {

	int result;
	if (! overlap)
		result = sequentialKernelExecutionWithDataMovement (env, object, threads, threadsPerGroup, profile);
	else
		result = pipelinedKernelExecutionWithDataMovement  (env, object, threads, threadsPerGroup, profile);
	return result;
}

void inputBufferSwap () {
	int i;
	i = ++ndx % 2;
	theInputBuffer.eventPtr = writeEventPtrs[i];
	theInputBuffer.queuePtr = commandQueue[i];
	theInputBuffer.mappedInputBufferPtr = mappedInputBufferPtrs[i];
	theInputBuffer.pinnedInputBufferPtr = pinnedInputBufferPtrs[i];
}

void outputBufferSwap () {
	int i;
	i = ++ndx % 2;
	theOutputBuffer.eventPtr = writeEventPtrs[i];
	theOutputBuffer.queuePtr = commandQueue[i];
	theOutputBuffer.mappedOutputBufferPtr = mappedOutputBufferPtrs[i];
	theOutputBuffer.pinnedOutputBufferPtr = pinnedOutputBufferPtrs[i];
}

void inputAndOutputBufferSwap () {
	int i;
	i = ++ndx % 2;

	theInputBuffer.eventPtr = writeEventPtrs[i];
	theInputBuffer.queuePtr = commandQueue[i];
	theInputBuffer.mappedInputBufferPtr = mappedInputBufferPtrs[i];
	theInputBuffer.pinnedInputBufferPtr = pinnedInputBufferPtrs[i];

	theOutputBuffer.eventPtr = writeEventPtrs[i];
	theOutputBuffer.queuePtr = commandQueue[i];
	theOutputBuffer.mappedOutputBufferPtr = mappedOutputBufferPtrs[i];
	theOutputBuffer.pinnedOutputBufferPtr = pinnedOutputBufferPtrs[i];
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeInputDataMovementCallback
(JNIEnv *env, jobject object) {
	jclass class = (*env)->GetObjectClass (env, object);
	jmethodID writeMethod = (*env)->GetMethodID(env, class,  "inputDataMovementCallback",  "(JI)V");
	if (! writeMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void inputDataMovementCallback (long, int)`\n");
		return ;
	}
	(*env)->CallVoidMethod(env, object, writeMethod, (long) mappedInputBuffer, _input);
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeAlternativeInputDataMovementCallback
(JNIEnv *env, jobject object) {
	jclass class = (*env)->GetObjectClass (env, object);
	jmethodID writeMethod = (*env)->GetMethodID(env, class,  "inputDataMovementCallback",  "(JI)V");
	if (! writeMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void inputDataMovementCallback (long, int)`\n");
		return ;
	}
	inputBufferSwap();
	(*env)->CallVoidMethod(env, object, writeMethod, (long) theInputBuffer.mappedInputBufferPtr, _input);
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeGPUWrite
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	cl_event w_event;
	int error;
	/* Clear GPU command queue */
	clFinish(commandQueue[0]);
	/* Write input */
	error = clEnqueueWriteBuffer (commandQueue[0], inputBuffer, CL_FALSE, 0, _input,
		(void *) &mappedInputBuffer[0], 0, NULL, &w_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
		return ;
	}
	error = clWaitForEvents(1, &w_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): write event failed\n", error, getErrorMessage(error));
		return ;
	}
	return ;
}

int waitForInputBufferEvent () {
	int error;
	error = clWaitForEvents(1, &theInputBuffer.eventPtr);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): write event failed\n", error, getErrorMessage(error));
		return -1;
	} else
		return 0;
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeAlternativeGPUWrite
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	int error;
	jbyte *mappedBuffer = theInputBuffer.mappedInputBufferPtr;
	/* Write input */

	clFinish(theInputBuffer.queuePtr);

	error = clEnqueueWriteBuffer (theInputBuffer.queuePtr, inputBuffer, CL_FALSE, 0, _input,
		(void *) &mappedBuffer[0], 0, NULL, &theInputBuffer.eventPtr);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
		return ;
	}
	/* waitForInputBufferEvent (); */
	return ;
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeGPURead
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	cl_event r_event;
	int error;
	/* Clear GPU command queue */
	clFinish(commandQueue[0]);
	/* Read output */
	error = clEnqueueReadBuffer (commandQueue[0], outputBuffer, CL_FALSE, 0, _output,
		(void *) &mappedOutputBuffer[0], 0, NULL, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
		return ;
	}
	error = clWaitForEvents(1, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): read event failed\n", error, getErrorMessage(error));
		return ;
	}
	return ;
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeAlternativeGPURead
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	int error;
	jbyte *mappedBuffer = theOutputBuffer.mappedOutputBufferPtr;

	/* Read output */
	error = clEnqueueReadBuffer (theOutputBuffer.queuePtr, outputBuffer, CL_FALSE, 0, _output,
		(void *) &mappedBuffer[0], 0, NULL, &theOutputBuffer.eventPtr);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
		return ;
	}

	outputBufferSwap();

	return ;
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeOutputDataMovementCallback
(JNIEnv *env, jobject object) {
	jclass class = (*env)->GetObjectClass (env, object);
	jmethodID  readMethod = (*env)->GetMethodID(env, class, "outputDataMovementCallback", "(JII)V");
	if (! readMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void outputDataMovementCallback (long, int, int)`\n");
		return ;
	}
	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod(env, object, readMethod, (long) mappedOutputBuffer, _output, 0);
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeAlternativeOutputDataMovementCallback
(JNIEnv *env, jobject object) {
	jclass class = (*env)->GetObjectClass (env, object);
	jmethodID  readMethod = (*env)->GetMethodID(env, class, "outputDataMovementCallback", "(JII)V");
	if (! readMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void outputDataMovementCallback (long, int, int)`\n");
		return ;
	}

	clFinish(theOutputBuffer.queuePtr);

	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod(env, object, readMethod, (long) theOutputBuffer.mappedOutputBufferPtr, _output, 0);
}

JNIEXPORT void JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeNullKernel
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	int error;
	jclass class = (*env)->GetObjectClass (env, object);
	jmethodID writeMethod = (*env)->GetMethodID(env, class,  "inputDataMovementCallback",  "(JI)V");
	if (! writeMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void inputDataMovementCallback (long, int)`\n");
		return ;
	}
	jmethodID  readMethod = (*env)->GetMethodID(env, class, "outputDataMovementCallback", "(JII)V");
	if (! readMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void outputDataMovementCallback (long, int, int)`\n");
		return ;
	}
	inputAndOutputBufferSwap();
	clFinish(theInputBuffer.queuePtr); /* Must be the same as the output buffer command queue */
	/* Copy  input */
	(*env)->CallVoidMethod(env, object, writeMethod, (long) theInputBuffer.mappedInputBufferPtr, _input);
	/* Prepare for read and write events */
	jbyte *theMappedInputBuffer  = theInputBuffer.mappedInputBufferPtr;
	jbyte *theMappedOutputBuffer = theOutputBuffer.mappedOutputBufferPtr;
	/* Write input */
	error = clEnqueueWriteBuffer (theInputBuffer.queuePtr, inputBuffer, CL_FALSE, 0, _input,
		(void *) &theMappedInputBuffer[0], 0, NULL, &theInputBuffer.eventPtr);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
		return ;
	}
	/* Read output */
	error = clEnqueueReadBuffer (theOutputBuffer.queuePtr, outputBuffer, CL_FALSE, 0, _output,
		(void *) &theMappedOutputBuffer[0], 1, &theInputBuffer.eventPtr, &theOutputBuffer.eventPtr);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
		return ;
	}
	/* Copy output */
	(*env)->CallVoidMethod(env, object, readMethod, (long) theOutputBuffer.mappedOutputBufferPtr, _output, 0);
	return ;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeReductionOperatorKernel
(JNIEnv *env, jobject object, jint threads, jint threadsPerGroup, jboolean profile) {

	int error = 0;
	size_t _threads = threads;
	size_t _threadsPerGroup = threadsPerGroup;
	cl_event w1_event;
	cl_event w2_event;
	cl_event w3_event;
	cl_event  x_event;
	cl_event  r_event;
	/* Clear GPU command queue */
	clFinish(commandQueue[0]);

	jclass class = (*env)->GetObjectClass (env, object);
	jmethodID writeInputBufferMethod = (*env)->GetMethodID(env, class,  "inputDataMovementCallback",  "(J)V");
	if (! writeInputBufferMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void inputDataMovementCallback (long)`\n");
		return -1;
	}
	jmethodID writeStartPointersMethod = (*env)->GetMethodID(env, class,  "windowStartPointersDataMovementCallback",  "(JI)V");
	if (! writeStartPointersMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void windowStartPointersDataMovementCallback (long, int)`\n");
		return -1;
	}
	jmethodID writeEndPointersMethod = (*env)->GetMethodID(env, class,  "windowEndPointersDataMovementCallback",  "(JI)V");
	if (! writeEndPointersMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void windowEndPointersDataMovementCallback (long, int)`\n");
		return -1;
	}
	jmethodID  readOutputBufferMethod = (*env)->GetMethodID(env, class, "outputDataMovementCallback", "(JII)V");
	if (! readOutputBufferMethod) {
		fprintf(stderr, "error: failed to acquire pointer to function `void outputDataMovementCallback (long, int, int)`\n");
		return -1;
	}

	timerP timer;
	tstamp_t dt;
	if (profile) {
		timer = timer_new();
		timer_start(timer);
	}

	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod(env, object, writeInputBufferMethod, (long) mappedInputBuffer);

	(*env)->CallVoidMethod(env, object, writeStartPointersMethod, (long) mappedWindowStartPointersBuffer, _startPointers);
	(*env)->CallVoidMethod(env, object, writeEndPointersMethod, (long) mappedWindowEndPointersBuffer, _endPointers);

	/* Write input */
	error = clEnqueueWriteBuffer (commandQueue[0], inputBuffer, CL_FALSE, 0, _input,
		(void *) &mappedInputBuffer[0], 0, NULL, &w1_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &w1_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): write event failed\n", error, getErrorMessage(error));
		return -1;
	}
	/* Write start and end pointers */
	error = clEnqueueWriteBuffer (commandQueue[0], windowStartPointersBuffer, CL_FALSE, 0, _startPointers,
		(void *) &mappedWindowStartPointersBuffer[0], 0, NULL, &w2_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &w2_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): write event failed\n", error, getErrorMessage(error));
		return -1;
	}
	error = clEnqueueWriteBuffer (commandQueue[0], windowEndPointersBuffer, CL_FALSE, 0, _endPointers,
		(void *) &mappedWindowEndPointersBuffer[0], 0, NULL, &w3_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to write to device memory\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &w3_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): write event failed\n", error, getErrorMessage(error));
		return -1;
	}
	/* Enqueue kernel command */
	error = clEnqueueNDRangeKernel (commandQueue[0], kernel[0], 1, NULL, &_threads, &_threadsPerGroup,
		0, NULL, &x_event);
	if (error != CL_SUCCESS) {
	 	fprintf(stderr, "error (%d: %s): failed to execute kernel\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &x_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): execute event failed\n", error, getErrorMessage(error));
		return -1;
	}
	/* Read output */
	error = clEnqueueReadBuffer (commandQueue[0], outputBuffer, CL_FALSE, 0, _output,
		(void *) &mappedOutputBuffer[0], 0, NULL, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): failed to read from device memory\n", error, getErrorMessage(error));
		return -1;
	}
	error = clWaitForEvents(1, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d: %s): read event failed\n", error, getErrorMessage(error));
		return -1;
	}

	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod(env, object, readOutputBufferMethod, (long) mappedOutputBuffer, _output);

	if (profile) {
		dt = timer_getElapsedTime(timer);
		clPrintEventProfilingInfo(w1_event, "W0_1");
		clPrintEventProfilingInfo(w2_event, "W0_2");
		clPrintEventProfilingInfo(w3_event, "W0_3");
		clPrintEventProfilingInfo( x_event, "X0"  );
		clPrintEventProfilingInfo( r_event, "R0"  );
		printf("T=%llu usec\n", dt);
		timer_free(timer);
	}
	clReleaseEvent (w1_event);
	clReleaseEvent (w2_event);
	clReleaseEvent (w3_event);
	clReleaseEvent ( x_event);
	clReleaseEvent ( r_event);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_releaseAll
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	if (mappedInputBuffer)
		clEnqueueUnmapMemObject(commandQueue[0], pinnedInputBuffer, (void *) mappedInputBuffer, 0, NULL, NULL);

	if (pinnedInputBuffer)
		clReleaseMemObject(pinnedInputBuffer);

	if (inputBuffer)
		clReleaseMemObject(inputBuffer);

	if (mappedOutputBuffer)
		clEnqueueUnmapMemObject(commandQueue[0], pinnedOutputBuffer, (void *) mappedOutputBuffer, 0, NULL, NULL);

	if (pinnedOutputBuffer)
		clReleaseMemObject(pinnedOutputBuffer);

	if (outputBuffer)
		clReleaseMemObject(outputBuffer);

	if (program)
		clReleaseProgram(program);

	if (kernel[0])
		clReleaseKernel(kernel[0]);

	if (kernel[1])
			clReleaseKernel(kernel[1]);

	if (commandQueue[0])
		clReleaseCommandQueue(commandQueue[0]);

	if (commandQueue[1])
		clReleaseCommandQueue(commandQueue[1]);

	if (context)
		clReleaseContext(context);

	return 0;
}
