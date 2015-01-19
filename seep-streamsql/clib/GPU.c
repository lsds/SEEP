#include "GPU.h"

#include "uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU.h"
#include <jni.h>

#include "utils.h"
#include "debug.h"

#include "gpuquery.h"
#include "openclerrorcode.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <CL/cl.h>

static cl_platform_id platform = NULL;
static cl_device_id device = NULL;
static cl_context context = NULL;

static int Q; /* Number of queries */
static int freeIndex;
static gpuQueryP queries [MAX_QUERIES];

void callback_setKernelDummy   (cl_kernel, gpuContextP, int *);
void callback_setKernelProject (cl_kernel, gpuContextP, int *);
void callback_setKernelReduce  (cl_kernel, gpuContextP, int *);
void callback_setKernelSelect  (cl_kernel, gpuContextP, int *);
void callback_setKernelCompact (cl_kernel, gpuContextP, int *);

void callback_writeInput (gpuContextP, JNIEnv *, jobject, int, int, int);
void callback_readOutput (gpuContextP, JNIEnv *, jobject, int, int, int);

static void setPlatform () {
	int error = 0;
	cl_uint count = 0;
	error = clGetPlatformIDs (1, &platform, &count);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	dbg("Obtained 1/%u platforms available\n", count);
	return;
}

static void setDevice () {
	int error = 0;
	cl_uint count = 0;
	error = clGetDeviceIDs (platform, CL_DEVICE_TYPE_GPU, 1, &device, &count);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	dbg("Obtained 1/%u devices available\n", count);
	return;
}

static void setContext () {
	int error = 0;
	context = clCreateContext (
		0,
		1,
		&device,
		NULL,
		NULL,
		&error);
	if (! context) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return ;
}

void gpu_init (int _queries) { /* Initialise `n` queries */
	setPlatform ();
	setDevice ();
	setContext ();
	Q = _queries; /* Number of queries */
	freeIndex = 0;
	int i;
	for (i = 0; i < MAX_QUERIES; i++)
		queries[i] = NULL;
	return;
}

void gpu_free () {
	int i;
	for (i = 0; i < MAX_QUERIES; i++)
		if (queries[i])
			gpu_query_free (queries[i]);
	if (context)
		clReleaseContext (context);
	return;
}

int gpu_getQuery (const char *source, int _kernels, int _inputs, int _outputs) {
	int ndx = freeIndex++;
	if (ndx < 0 || ndx >= Q)
		return -1;
	queries[ndx] = gpu_query_new (device, context,
			source, _kernels, _inputs, _outputs);
	return ndx;
}

int gpu_setInput  (int qid, int ndx, int size) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_setInput (p, ndx, size);
}

int gpu_setOutput (int qid, int ndx, int size, int writeOnly) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_setOutput (p, ndx, size, writeOnly);
}

int gpu_setKernel (int qid, int ndx, const char *name,
		void (*callback)(cl_kernel, gpuContextP, int *), int *args) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_setKernel (p, ndx, name, callback, args);
}

int gpu_exec (int qid, size_t threads, size_t threadsPerGroup,
		queryOperatorP operator, JNIEnv *env, jobject obj) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_exec (p, threads, threadsPerGroup, operator, env, obj, qid);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_init
(JNIEnv *env, jobject obj, jint N) {

	(void) env;
	(void) obj;

	gpu_init (N);

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_getQuery
(JNIEnv *env, jobject obj, jstring source, jint _kernels, jint _inputs, jint _outputs) {

	(void) obj;

	const char *_source = (*env)->GetStringUTFChars (env, source, NULL);
	int qid = gpu_getQuery (_source, _kernels, _inputs, _outputs);
	(*env)->ReleaseStringUTFChars (env, source, _source);

	return qid;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setInput
(JNIEnv *env, jobject obj, jint qid, jint ndx, jint size) {

	(void) env;
	(void) obj;

	return gpu_setInput (qid, ndx, size);
}
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setOutput
(JNIEnv *env, jobject obj, jint qid, jint ndx, jint size, jint writeOnly) {

	(void) env;
	(void) obj;

	return gpu_setOutput (qid, ndx, size, writeOnly);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_free
(JNIEnv *env, jobject obj) {

	(void) env;
	(void) obj;

	gpu_free ();

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_execute
(JNIEnv *env, jobject obj, jint qid, jint threads, jint threadsPerGroup) {

	/* Create operator */
	queryOperatorP operator = (queryOperatorP) malloc (sizeof(query_operator_t));
	if (! operator) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit(1);
	}
	/* Currently, we assume the same execution pattern for all queries */
	operator->writeInput = callback_writeInput;
	operator->readOutput = callback_readOutput;
	operator->execKernel = NULL;
	gpu_exec (qid, (size_t) threads, (size_t) threadsPerGroup, operator, env, obj);
	/* Free operator */
	if (operator)
		free (operator);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setKernelDummy
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) env;
	(void) obj;

	(void) _args;

	gpu_setKernel (qid, 0, "dummy", &callback_setKernelDummy, NULL);

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setKernelProject
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) obj;

	jsize argc = (*env)->GetArrayLength(env, _args);
	if (argc != 4) /* # projection kernel constants */
		return -1;
	jint *args = (*env)->GetIntArrayElements(env, _args, 0);
	/* Object `int []` pinned */
	gpu_setKernel (qid, 0, "projectKernel", &callback_setKernelProject, args);
	(*env)->ReleaseIntArrayElements(env, _args, args, 0);
	/* Object `int []` released */
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setKernelSelect
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) obj;

	jsize argc = (*env)->GetArrayLength(env, _args);
	if (argc != 5) /* # selection kernel constants */
		return -1;
	jint *args = (*env)->GetIntArrayElements(env, _args, 0);
	/* Object `int []` pinned */
	gpu_setKernel (qid, 0,  "selectKernel",  &callback_setKernelSelect, args);
	gpu_setKernel (qid, 1, "compactKernel", &callback_setKernelCompact, args);
	(*env)->ReleaseIntArrayElements(env, _args, args, 0);
	/* Object `int []` released */
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setKernelReduce
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) obj;

	jsize argc = (*env)->GetArrayLength(env, _args);
	if (argc != 3) /* # reduction kernel constants */
		return -1;
	jint *args = (*env)->GetIntArrayElements(env, _args, 0);
	/* Object `int []` pinned */
	gpu_setKernel (qid, 0, "reduceKernel", &callback_setKernelReduce, args);
	(*env)->ReleaseIntArrayElements(env, _args, args, 0);
	/* Object `int []` released */
	return 0;
}

void callback_setKernelDummy (cl_kernel kernel, gpuContextP context, int *constants) {

	(void) constants;

	int error = 0;
	error |= clSetKernelArg (
		kernel,
		0, /* First argument */
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		1, /* Second argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[0]->device_buffer));
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return;
}

void callback_setKernelProject (cl_kernel kernel, gpuContextP context, int *constants) {
	/*
	 * Projection kernel signature
	 *
	 * __kernel void projectKernel (
	 * const int tuples,
	 * const int bytes,
	 * __global const uchar *input,
	 * __global uchar *output,
	 * __local uchar *_input,
	 * __local uchar *_output
	 * )
	 */

	/* Get all constants */
	int       tuples = constants[0];
	int        bytes = constants[1];
	int  _input_size = constants[2]; /* Local buffer memory sizes */
	int _output_size = constants[3];

	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *) &tuples);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *)  &bytes);
	/* Set I/O byte buffers */
	error |= clSetKernelArg (
		kernel,
		2, /* 3rd argument */
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		3, /* 4th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[0]->device_buffer));
	/* Set local memory */
	error |= clSetKernelArg (kernel, 4, (size_t)  _input_size, (void *) NULL);
	error |= clSetKernelArg (kernel, 5, (size_t) _output_size, (void *) NULL);

	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return;
}

void callback_setKernelReduce (cl_kernel kernel, gpuContextP context, int *constants) {

	/*
	 * Reduction kernel signature
	 *
	 * __kernel void reduce (
	 * const int tuples,
	 * const int bytes,
	 * __global const uchar *input,
	 * __global const int *startPointers,
	 * __global const int *endPointers,
	 * __global uchar *output,
	 * __local float *scratch
	 * )
	 */

	/* Get all constants */
	int         tuples = constants[0];
	int          bytes = constants[1];
	int  _scratch_size = constants[2]; /* Local buffer memory size */

	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *) &tuples);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *)  &bytes);
	/* Set I/O byte buffers */
	error |= clSetKernelArg (
		kernel,
		2, /* 3rd argument */
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		3, /* 4th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		4, /* 5th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		5, /* 6th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[0]->device_buffer));
	/* Set local memory */
	error |= clSetKernelArg (kernel, 6, (size_t)  _scratch_size, (void *) NULL);

	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return;
}

void callback_setKernelSelect (cl_kernel kernel, gpuContextP context, int *constants) {
	/*
	 * Selection kernel signature
	 *
	 * __kernel void selectKernel (
	 * const int size,
	 * const int tuples,
	 * const int _bundle,
	 * const int bundles,
	 * __global const uchar *input,
	 * __global int *flags,
	 * __global int *offsets,
	 * __local int *buffer
	 * )
	 */

	/* Get all constants */
	int        size = constants[0];
	int      tuples = constants[1];
	int     _bundle = constants[2];
	int     bundles = constants[3];
	int buffer_size = constants[4]; /* Local buffer memory size */

	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *)    &size);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *)  &tuples);
	error |= clSetKernelArg (kernel, 2, sizeof(int), (void *) &_bundle);
	error |= clSetKernelArg (kernel, 3, sizeof(int), (void *) &bundles);
	/* Set I/O byte buffers */
	error |= clSetKernelArg (
		kernel,
		4, /* 5th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		5, /* 6th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		6, /* 7th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[1]->device_buffer));
	/* Set local memory */
	error |= clSetKernelArg (kernel, 7, (size_t) buffer_size, (void *) NULL);

	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return;
}

void callback_setKernelCompact (cl_kernel kernel, gpuContextP context, int *constants) {
	/*
	 * Compact kernel signature
	 *
	 * __kernel void compactKernel (
	 * const int size,
	 * const int tuples,
	 * const int _bundle,
	 * const int bundles,
	 * __global const uchar *input,
	 * __global int *flags,
	 * __global int *offsets,
	 * __global const int *pivots,
	 * __global uchar *output
	 * )
	 */

	/* Get all constants */
	int        size = constants[0];
	int      tuples = constants[1];
	int     _bundle = constants[2];
	int     bundles = constants[3];

	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *)    &size);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *)  &tuples);
	error |= clSetKernelArg (kernel, 2, sizeof(int), (void *) &_bundle);
	error |= clSetKernelArg (kernel, 3, sizeof(int), (void *) &bundles);
	/* Set I/O byte buffers */
	error |= clSetKernelArg (
		kernel,
		4, /* 5th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		5, /* 6th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		6, /* 7th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		7, /* 8th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		8, /* 9th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[3]->device_buffer));

	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return;
}

void callback_writeInput (gpuContextP context,
		JNIEnv *env, jobject obj, int qid, int ndx, int offset) {

	jclass class = (*env)->GetObjectClass (env, obj);
	jmethodID method = (*env)->GetMethodID (env, class,
			"inputDataMovementCallback",  "(IIJII)V");
	if (! method) {
		fprintf(stderr, "JNI error: failed to acquire write method pointer\n");
		exit(1);
	}
	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod (
			env, obj, method,
			qid,
			ndx,
			(long) (context->kernelInput.inputs[ndx]->mapped_buffer),
			context->kernelInput.inputs[ndx]->size,
			offset);

	(*env)->DeleteLocalRef(env, class);
	return ;
}

void callback_readOutput (gpuContextP context,
		JNIEnv *env, jobject obj, int qid, int ndx, int offset) {

	jclass class = (*env)->GetObjectClass (env, obj);
	jmethodID method = (*env)->GetMethodID (env, class,
			"outputDataMovementCallback", "(IIJII)V");
	if (! method) {
		fprintf(stderr, "JNI error: failed to acquire read method pointer\n");
		exit(1);
	}
	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod (
			env, obj, method,
			qid,
			ndx,
			(long) (context->kernelOutput.outputs[ndx]->mapped_buffer),
			context->kernelOutput.outputs[ndx]->size,
			offset);

	(*env)->DeleteLocalRef(env, class);
	return;
}
