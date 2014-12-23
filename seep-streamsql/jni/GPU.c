/*
 * GPU.c
 */

#include <jni.h>

#include <CL/cl.h>

#include <stdio.h>
#include <string.h>

#include <sys/time.h>

#include "uk_ac_imperial_lsds_streamsql_op_gpu_GPU.h"
#include "timer.h"
#include "log.h"

cl_platform_id platform;
cl_device_id device;
cl_context context;
cl_command_queue commandQueue[2];
cl_program program;
cl_kernel kernel[2];

cl_mem pinnedInputBuffer, pinnedOutputBuffer; /* Pinned memory */
jbyte *mappedInputBuffer,*mappedOutputBuffer; /* Mapped pointer to pinned memory */

cl_mem inputBuffer, outputBuffer;

int _input, _output; /* Input and output size */

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
		fprintf(stderr, "error: cannot get platform\n");
		return -1;
	}
	printf("Obtained 1/%u platforms available\n", count);
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
		fprintf(stderr, "error (%d): cannot get device\n", error);
		return -1;
	}
	printf("Obtained 1/%u devices available\n", count);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createContext
(JNIEnv *env, jobject object) {

	(void) env;
	(void) object; /* Supress warnings */

	int error = 0;
	context = clCreateContext (0, 1, &device, NULL, NULL, &error);
	if (! context) {
		fprintf(stderr, "error (%d): cannot create compute context\n", error);
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
		fprintf(stderr, "error (%d): failed to create command queue\n", error);
		return -1;
	}
	commandQueue[1] = clCreateCommandQueue (context, device, CL_QUEUE_PROFILING_ENABLE, &error);
	if (! commandQueue[1]) {
		fprintf(stderr, "error (%d): failed to create command queue\n", error);
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
		fprintf(stderr, "error (%d): failed to create program\n", error);
		(*env)->ReleaseStringUTFChars(env, source, _source);
		return -1;
	}
	/* Build the program */
	error = clBuildProgram (program, 0, NULL, flags, NULL, NULL);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d): failed to build program\n", error);
	}
	/* Get compiler info (or error) */
	clGetProgramBuildInfo (program, device, CL_PROGRAM_BUILD_LOG, sizeof(blog), blog, &len);
	printf("%s\n", blog);
	(*env)->ReleaseStringUTFChars(env, source, _source);
	return error;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_createKernel
(JNIEnv *env, jobject object, jstring name) {

	(void) object; /* Supress warnings */

	int error = 0;
	const char *_name = (*env)->GetStringUTFChars(env, name, NULL);
	kernel[0] = clCreateKernel(program, _name, &error);
	if (! kernel[0] || error != CL_SUCCESS) {
		fprintf(stderr, "error (%d): failed to create kernel\n", error);
		(*env)->ReleaseStringUTFChars(env, name, _name);
		return -1;
	}
	kernel[1] = clCreateKernel(program, _name, &error);
	if (! kernel[0] || error != CL_SUCCESS) {
		fprintf(stderr, "error (%d): failed to create kernel\n", error);
		(*env)->ReleaseStringUTFChars(env, name, _name);
		return -1;
	}
	(*env)->ReleaseStringUTFChars(env, name, _name);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_setKernelArgs
(JNIEnv *env, jobject object, jint tuples, jint size, jboolean overlap) {

	(void) env;
	(void) object; /* Supress warnings */

	int error = 0;
	size_t _size = (size_t) size; /* Size of local memory */
	if (overlap)
		_size /= 2;
	int i;
	for (i = 0; i < 2; i++) {
		error  = clSetKernelArg (kernel[i], 0, sizeof(int), (void *) &tuples);
		error |= clSetKernelArg (kernel[i], 1, sizeof(int), (void *) &_input);
		error |= clSetKernelArg (kernel[i], 2, sizeof(cl_mem), (void *) &inputBuffer);
		error |= clSetKernelArg (kernel[i], 3, sizeof(cl_mem), (void *) &outputBuffer);
		error |= clSetKernelArg (kernel[i], 4, (size_t) _size, (void *) NULL);
		error |= clSetKernelArg (kernel[i], 5, (size_t) _size, (void *) NULL);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d): failed to set kernel arguments\n", error);
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
		fprintf(stderr, "error (%d): failed to allocate device memory\n", error);
		return -1;
	}
	pinnedInputBuffer = clCreateBuffer (context, CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR, _input, NULL, &error);
	if (! pinnedInputBuffer) {
		fprintf(stderr, "error (%d): failed to allocate device memory\n", error);
		return -1;
	}
	mappedInputBuffer = (jbyte *) clEnqueueMapBuffer(commandQueue[0], pinnedInputBuffer, CL_TRUE, CL_MAP_WRITE, 0, _input, 0, NULL, NULL, &error);
	if (! mappedInputBuffer) {
		fprintf(stderr, "error (%d): failed to map device memory\n", error);
		return -1;
	}
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
		fprintf(stderr, "error (%d): failed to allocate device memory\n", error);
		return -1;
	}
	pinnedOutputBuffer = clCreateBuffer (context, CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR,
		_output, NULL, &error);
	if (! pinnedOutputBuffer) {
		fprintf(stderr, "error (%d): failed to allocate device memory\n", error);
		return -1;
	}
	mappedOutputBuffer = (jbyte *) clEnqueueMapBuffer(commandQueue[0], pinnedOutputBuffer,
		CL_TRUE, CL_MAP_READ, 0, _output, 0, NULL, NULL, &error);
	if (! mappedOutputBuffer) {
		fprintf(stderr, "error (%d): failed to map device memory\n", error);
		return -1;
	}
	return (long) mappedOutputBuffer;
}

float normalise (cl_ulong timestamp) {
	float result = ((float) (timestamp - _timestamp)) / 1000.;
	return result;
}

void clPrintEventProfilingInfo(cl_event event, const char *acronym) {
	cl_ulong q, s, x, f; /* queued, submitted, start, and end timestamps */
	float _q, _s, _x, _f;
	clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_QUEUED, sizeof(cl_ulong), &q, NULL);
	if (_timestamp < 1)
		_timestamp = q;
	clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_SUBMIT, sizeof(cl_ulong), &s, NULL);
	clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_START , sizeof(cl_ulong), &x, NULL);
	clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_END   , sizeof(cl_ulong), &f, NULL);
	_q = normalise(q);
	_s = normalise(s);
	_x = normalise(x);
	_f = normalise(f);
	printf("%s\t q %10.1f\t s %10.1f\t x %10.1f\t f %10.1f\n", acronym, _q, _s, _x, _f);
}

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
		fprintf(stderr, "error (%d): failed to write to device memory\n", error);
		return -1;
	}
	error = clWaitForEvents(1, &w_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d): write event failed (%s)\n", error, getErrorMessage(error));
		return -1;
	}
	/* Enqueue kernel command */
	error = clEnqueueNDRangeKernel (commandQueue[0], kernel[0], 1, NULL, &_threads, &_threadsPerGroup,
		0, NULL, &x_event);
	if (error != CL_SUCCESS) {
	 	fprintf(stderr, "error (%d): failed to execute kernel\n", error);
		return -1;
	}
	error = clWaitForEvents(1, &x_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d): execute event failed (%s)\n", error, getErrorMessage(error));
		return -1;
	}
	/* Read output */
	error = clEnqueueReadBuffer (commandQueue[0], outputBuffer, CL_FALSE, 0, _output,
		(void *) &mappedOutputBuffer[0], 0, NULL, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d): failed to read from device memory\n", error);
		return -1;
	}
	error = clWaitForEvents(1, &r_event);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "error (%d): read event failed (%s)\n", error, getErrorMessage(error));
		return -1;
	}
	if (profile) {
		dt = timer_getElapsedTime(timer);

		clPrintEventProfilingInfo(w_event, "W0");
		clPrintEventProfilingInfo(x_event, "X0");
		clPrintEventProfilingInfo(r_event, "R0");

		/* Get profiling information */
		/*
		cl_ulong start = 0, end = 0;
		float Tw, Tx, Tr;
		clGetEventProfilingInfo(w_event, CL_PROFILING_COMMAND_START, sizeof(cl_ulong), &start, NULL);
		clGetEventProfilingInfo(w_event, CL_PROFILING_COMMAND_END  , sizeof(cl_ulong),   &end, NULL);
		Tw = (float) (end - start) / 1000.;
		clGetEventProfilingInfo(x_event, CL_PROFILING_COMMAND_START, sizeof(cl_ulong), &start, NULL);
		clGetEventProfilingInfo(x_event, CL_PROFILING_COMMAND_END  , sizeof(cl_ulong),   &end, NULL);
		Tx = (float) (end - start) / 1000.;
		clGetEventProfilingInfo(r_event, CL_PROFILING_COMMAND_START, sizeof(cl_ulong), &start, NULL);
		clGetEventProfilingInfo(r_event, CL_PROFILING_COMMAND_END  , sizeof(cl_ulong),   &end, NULL);
		Tr = (float) (end - start) / 1000.;
		printf("T=%llu usec T(W)=%6.1f T(X)=%6.1f T(R)=%6.1f\n", dt, Tw, Tx, Tr);
		*/
		timer_free(timer);
	}

	clReleaseEvent (w_event);
	clReleaseEvent (x_event);
	clReleaseEvent (r_event);

	return 0;
}

jint pipelinedKernelExecution (jint threads, jint threadsPerGroup, jboolean profile) {

	(void) threads;
	(void) threadsPerGroup;

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
				(void *) &mappedInputBuffer[offset], 0, NULL, &w0_event);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d): failed to write to device memory\n", error);
			return -1;
		}

		clFlush(commandQueue[0]);
		/* clFlush(commandQueue[1]); */

		/* Step 2.a) */
		error = clEnqueueNDRangeKernel (commandQueue[0], kernel[0], 1, NULL, &_threads, &_threadsPerGroup,
			0, NULL, &x0_event);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d): failed to execute kernel\n", error);
			return -1;
		}
		/* Step 2.b) */
		error = clEnqueueWriteBuffer (commandQueue[1], inputBuffer, CL_FALSE, offset + step, step,
			(void *) &mappedInputBuffer[offset + step], 0, NULL, &w1_event);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d): failed to write to device memory\n", error);
			return -1;
		}

		clFlush(commandQueue[0]);
		clFlush(commandQueue[1]);

		/* Step 3.a) */
		error = clEnqueueNDRangeKernel (commandQueue[1], kernel[1], 1, NULL, &_threads,
			&_threadsPerGroup, 0, NULL, &x1_event);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d): failed to execute kernel\n", error);
			return -1;
		}
		/* Step 3.b) */
		error = clEnqueueReadBuffer (commandQueue[0], outputBuffer, CL_FALSE, offset, step,
			(void *) &mappedOutputBuffer[offset], 0, NULL, &r0_event);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d): failed to read from device memory\n", error);
			return -1;
		}

		clFlush(commandQueue[0]);
		clFlush(commandQueue[1]);

		/* Step 1.a) */
		error = clEnqueueReadBuffer (commandQueue[1], outputBuffer, CL_FALSE, offset + step, step,
			(void *) &mappedOutputBuffer[offset + step], 0, NULL, &r1_event);
		if (error != CL_SUCCESS) {
			fprintf(stderr, "error (%d): failed to read from device memory\n", error);
			return -1;
		}

	} /* end for loop */

	clFinish(commandQueue[0]);
	clFinish(commandQueue[1]);

	if (profile) {
		dt = timer_getElapsedTime(timer);

		/*
		clPrintEventProfilingInfo(w0_event, "W0");
		clPrintEventProfilingInfo(x0_event, "X0");
		clPrintEventProfilingInfo(w1_event, "W1");
		clPrintEventProfilingInfo(x1_event, "X1");
		clPrintEventProfilingInfo(r0_event, "R0");
		clPrintEventProfilingInfo(r1_event, "R1");
		*/

		printf("T=%llu usec\n", dt);
		timer_free(timer);
	}
	/*
	clReleaseEvent(w0_event);
	clReleaseEvent(x0_event);
	clReleaseEvent(w1_event);
	clReleaseEvent(x1_event);
	clReleaseEvent(r0_event);
	clReleaseEvent(r1_event);
	*/
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_GPU_invokeKernel
(JNIEnv *env, jobject object, jint threads, jint threadsPerGroup, jboolean overlap, jboolean profile) {

	(void) env;
	(void) object; /* Supress warnings */

	if (! overlap)
		return sequentialKernelExecution (threads, threadsPerGroup, profile);
	else
		return pipelinedKernelExecution (threads, threadsPerGroup, profile);
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
