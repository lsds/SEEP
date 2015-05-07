#include "GPU.h"

#include "uk_ac_imperial_lsds_seep_multi_TheGPU.h"
#include <jni.h>

#include "utils.h"
#include "debug.h"

#include "gpuquery.h"
#include "openclerrorcode.h"

#include "directbuffer.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif

#include <unistd.h>
#include <sched.h>

/* Lock memory pages */
#include <sys/mman.h>
#include <errno.h>

static cl_platform_id platform = NULL;
static cl_device_id device = NULL;
static cl_context context = NULL;
static cl_command_queue theQueue = NULL;

static int Q; /* Number of queries */
static int freeIndex;
static gpuQueryP queries [MAX_QUERIES];

static int bufferIndex;
static directBufferP buffers [MAX_BUFFERS];

static gpuContextP previousQuery [DEPTH];

void callback_setKernelDummy     (cl_kernel, gpuContextP, int *);
void callback_setKernelProject   (cl_kernel, gpuContextP, int *);
void callback_setKernelReduce    (cl_kernel, gpuContextP, int *);
void callback_setKernelSelect    (cl_kernel, gpuContextP, int *);
void callback_setKernelCompact   (cl_kernel, gpuContextP, int *);
void callback_setKernelAggregate (cl_kernel, gpuContextP, int *);
void callback_setKernelThetaJoin (cl_kernel, gpuContextP, int *);

/* UDFs */
void callback_setKernelAggregateIStream (cl_kernel, gpuContextP, int *);

void callback_writeInput (gpuContextP, JNIEnv *, jobject, int, int, int);
void callback_readOutput (gpuContextP, JNIEnv *, jobject, int, int, int);
/* Get previous execution context and set current one */
gpuContextP callback_execKernel (gpuContextP);

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

static void setQueue () {
	int error;
	theQueue = clCreateCommandQueue (
		context,
		device,
		CL_QUEUE_PROFILING_ENABLE,
		&error);
	if (! theQueue) {
		fprintf(stderr, "opencl error (%d): %s (%s)\n",
			error, getErrorMessage(error), __FUNCTION__);
		exit (1);
	}
}

static void getDeviceInfo () {
	cl_int error = 0;
	cl_uint value = 0;
	error = clGetDeviceInfo (device, CL_DEVICE_MEM_BASE_ADDR_ALIGN, sizeof (cl_uint), &value, NULL);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	fprintf(stderr, "[GPU] alignment is %u bits\n", value);
	return ;
}

void gpu_init (int _queries) { /* Initialise `n` queries */
	int i;

	setPlatform ();
	setDevice ();
	setContext ();
	setQueue ();
	getDeviceInfo ();

	Q = _queries; /* Number of queries */

	freeIndex = 0;
	for (i = 0; i < MAX_QUERIES; i++)
		queries[i] = NULL;

	for (i = 0; i < DEPTH; i++)
		previousQuery[i] = NULL;

	bufferIndex = 0;
	for (i = 0; i < MAX_BUFFERS; i++)
		buffers[i] = NULL;
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

int gpu_getQuery (const char *source, int _kernels, int _inputs, int _outputs, 
JNIEnv *env) {
	
	int ndx = freeIndex++;
	if (ndx < 0 || ndx >= Q)
		return -1;
	queries[ndx] = gpu_query_new (device, context,
			source, _kernels, _inputs, _outputs);

	gpu_query_init (queries[ndx], env, ndx);

	fprintf(stderr, "[GPU] _getQuery returns %d (%d/%d)\n", ndx, freeIndex, Q);
	return ndx;
}

int gpu_getDirectBuffer (int size, int readOnly) {

	int ndx = bufferIndex++;
	if (ndx < 0 || ndx >= MAX_BUFFERS)
		return -1;
	buffers[ndx] = getDirectBuffer (context, theQueue, size, readOnly);
	return ndx;
}

int gpu_setInput  (int qid, int ndx, void *buffer, int size) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_setInput (p, ndx, buffer, size);
}

int gpu_setOutput (int qid, int ndx, void *buffer, int size, 
	int writeOnly, int doNotMove, int bearsMark, int readEvent) {
	
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_setOutput (p, ndx, buffer, size, 
		writeOnly, doNotMove, bearsMark, readEvent);
}

int gpu_setKernel (int qid, int ndx, const char *name,
		void (*callback)(cl_kernel, gpuContextP, int *), int *args) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_setKernel (p, ndx, name, callback, args);
}

int gpu_exec (int qid, size_t *threads, size_t *threadsPerGroup,
		queryOperatorP operator, JNIEnv *env, jobject obj) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_exec (p, threads, threadsPerGroup, operator, env, obj, qid);
}

int gpu_exec_direct (int qid, size_t *threads, size_t *threadsPerGroup,
		int *startPointers, int *endPointers,
		queryOperatorP operator, JNIEnv *env, jobject obj) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_exec_direct (p, threads, threadsPerGroup,
		startPointers, endPointers, operator, env, obj, qid);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_init
(JNIEnv *env, jobject obj, jint N) {

	(void) env;
	(void) obj;

	gpu_init (N);

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_getQuery
(JNIEnv *env, jobject obj, jstring source, jint _kernels, jint _inputs, jint _outputs) {

	(void) obj;

	const char *_source = (*env)->GetStringUTFChars (env, source, NULL);
	int qid = gpu_getQuery (_source, _kernels, _inputs, _outputs, env);
	(*env)->ReleaseStringUTFChars (env, source, _source);

	return qid;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setInput
(JNIEnv *env, jobject obj, jint qid, jint ndx, jint size) {

	(void) env;
	(void) obj;
	
	return gpu_setInput (qid, ndx, NULL, size);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setDirectInput
(JNIEnv *env, jobject obj, jint qid, jint ndx, jint size, jint bid) {

	(void) env;
	(void) obj;

	if (bid < 0 || bid >= MAX_BUFFERS)
		return -1;
	return gpu_setInput (qid, ndx, buffers[bid], size);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setOutput
(JNIEnv *env, jobject obj, jint qid, jint ndx, jint size, 
	jint writeOnly, jint doNotMove, jint bearsMark, jint readEvent) {

	(void) env;
	(void) obj;

	return gpu_setOutput (qid, ndx, NULL, size, 
		writeOnly, doNotMove, bearsMark, readEvent);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setDirectOutput
(JNIEnv *env, jobject obj, jint qid, jint ndx, jint size,
		jint writeOnly, jint doNotMove, jint bearsMark, jint readEvent, jint bid) {

	(void) env;
	(void) obj;

	if (bid < 0 || bid >= MAX_BUFFERS)
		return -1;
	return gpu_setOutput (qid, ndx, buffers[bid], size,
		writeOnly, doNotMove, bearsMark, readEvent);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_free
(JNIEnv *env, jobject obj) {

	(void) env;
	(void) obj;

	gpu_free ();

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_execute
(JNIEnv *env, jobject obj, jint qid, jintArray _threads, jintArray _threadsPerGroup) {

	/* Create operator */
	queryOperatorP operator = (queryOperatorP) malloc (sizeof(query_operator_t));
	if (! operator) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit(1);
	}
	jsize argc = (*env)->GetArrayLength(env, _threads);
	jint *threads = (*env)->GetIntArrayElements(env, _threads, 0);
	jint *threadsPerGroup = (*env)->GetIntArrayElements(env, _threadsPerGroup, 0);
	int i;
	for (i = 0; i < argc; i++) {
		dbg("[DBG] kernel %d: %10d threads %10d threads/group\n", 
			i, threads[i], threadsPerGroup[i]);
	}
	size_t *__threads = (size_t *) malloc (argc * sizeof(size_t));
	size_t *__threadsPerGroup = (size_t *) malloc (argc * sizeof(size_t));
	for (i = 0; i < argc; i++) {
		__threads[i] = (size_t) threads[i];
		__threadsPerGroup[i] = (size_t) threadsPerGroup[i];
	}
	
	/* Currently, we assume the same execution pattern for all queries */
	operator->writeInput = callback_writeInput;
	operator->readOutput = callback_readOutput;
	operator->execKernel = callback_execKernel;
	gpu_exec (qid, __threads, __threadsPerGroup, operator, env, obj);
	/* Free operator */
	if (operator)
		free (operator);
	/* Release Java arrays */
	(*env)->ReleaseIntArrayElements(env, _threads, threads, 0);
	(*env)->ReleaseIntArrayElements(env, _threadsPerGroup, threadsPerGroup, 0);
	/* Free memory */
	if (__threads)
		free (__threads);
	if (__threadsPerGroup)
		free (__threadsPerGroup);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_executeDirect
(JNIEnv *env, jobject obj, jint qid, jintArray _threads, jintArray _threadsPerGroup,
	jintArray _start, jintArray _end) {

	/* Create operator */
	queryOperatorP operator = (queryOperatorP) malloc (sizeof(query_operator_t));
	if (! operator) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit(1);
	}
	int i;
	/* Assert that `targc` = #kernels */
	jsize targc = (*env)->GetArrayLength(env, _threads);
	jint *threads = (*env)->GetIntArrayElements(env, _threads, 0);
	jint *threadsPerGroup = (*env)->GetIntArrayElements(env, _threadsPerGroup, 0);
	/* Debug # threads and threads/group */
	for (i = 0; i < targc; i++) {
		dbg("[DBG] kernel %d: %10d threads %10d threads/group\n",
			i, threads[i], threadsPerGroup[i]);
	}
	size_t *__threads = (size_t *) malloc (targc * sizeof(size_t));
	size_t *__threadsPerGroup = (size_t *) malloc (targc * sizeof(size_t));
	for (i = 0; i < targc; i++) {
		__threads[i] = (size_t) threads[i];
		__threadsPerGroup[i] = (size_t) threadsPerGroup[i];
	}
	/* Assert that `pargc` = #inputs */
	jsize pargc = (*env)->GetArrayLength(env, _start);
	jint *start = (*env)->GetIntArrayElements(env, _start, 0);
	jint *end = (*env)->GetIntArrayElements(env, _end, 0);
	/* Debug start and end pointers for input buffers */
	for (i = 0; i < pargc; i++) {
		dbg("[DBG] input %d: start %13d end %13d\n", i, start[i], end[i]);
	}

	/* Currently, we assume the same execution pattern for all queries */
	operator->writeInput = callback_writeInput;
	operator->readOutput = callback_readOutput;
	operator->execKernel = callback_execKernel;

	gpu_exec_direct (qid, __threads, __threadsPerGroup, start, end, operator, env, obj);
	/* Free operator */
	if (operator)
		free (operator);
	/* Release Java arrays */
	(*env)->ReleaseIntArrayElements(env, _threads, threads, 0);
	(*env)->ReleaseIntArrayElements(env, _threadsPerGroup, threadsPerGroup, 0);

	(*env)->ReleaseIntArrayElements(env, _start, start, 0);
	(*env)->ReleaseIntArrayElements(env, _end, end, 0);

	/* Free allocated memory */
	if (__threads)
		free (__threads);
	if (__threadsPerGroup)
		free (__threadsPerGroup);
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelDummy
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) env;
	(void) obj;

	(void) _args;

	gpu_setKernel (qid, 0, "dummyKernel", &callback_setKernelDummy, NULL);

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelProject
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

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelSelect
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) obj;

	jsize argc = (*env)->GetArrayLength(env, _args);
	if (argc != 3) /* # selection kernel constants */
		return -1;
	jint *args = (*env)->GetIntArrayElements(env, _args, 0);
	/* Object `int []` pinned */
	gpu_setKernel (qid, 0,  "selectKernel",  &callback_setKernelSelect, args);
	gpu_setKernel (qid, 1, "compactKernel", &callback_setKernelCompact, args);
	(*env)->ReleaseIntArrayElements(env, _args, args, 0);
	/* Object `int []` released */
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelAggregate
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) obj;

	jsize argc = (*env)->GetArrayLength(env, _args);
	if (argc != 8) /* # aggregation kernel constants */
		return -1;
	jint *args = (*env)->GetIntArrayElements(env, _args, 0);
	/* Object `int []` pinned */

	gpu_setKernel (qid, 0,  "clearKernel",     &callback_setKernelAggregate, args);
	gpu_setKernel (qid, 1,  "aggregateKernel", &callback_setKernelAggregate, args);
	gpu_setKernel (qid, 2,  "scanKernel",      &callback_setKernelAggregate, args);
	gpu_setKernel (qid, 3,  "compactKernel",   &callback_setKernelAggregate, args);

	(*env)->ReleaseIntArrayElements(env, _args, args, 0);
	/* Object `int []` released */
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelReduce
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

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelThetaJoin
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) obj;
	
	jsize argc = (*env)->GetArrayLength(env, _args);
	if (argc != 4) /* # theta-join kernel constants */
		return -1;
	jint *args = (*env)->GetIntArrayElements(env, _args, 0);
	/* Object `int []` pinned */

	/*
	 * Previous ThetaJoin code
	 *
	gpu_setKernel (qid, 0, "joinKernel",    &callback_setKernelThetaJoin, args);
	gpu_setKernel (qid, 1, "scanKernel",    &callback_setKernelThetaJoin, args);
	gpu_setKernel (qid, 2, "compactKernel", &callback_setKernelThetaJoin, args);
	*/

	gpu_setKernel (qid, 0, "countKernel",   &callback_setKernelThetaJoin, args);
	gpu_setKernel (qid, 1, "scanKernel",    &callback_setKernelThetaJoin, args);
	gpu_setKernel (qid, 2, "compactKernel", &callback_setKernelThetaJoin, args);
	gpu_setKernel (qid, 3, "joinKernel",    &callback_setKernelThetaJoin, args);

	(*env)->ReleaseIntArrayElements(env, _args, args, 0);
	/* Object `int []` released */
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelAggregateIStream
(JNIEnv *env, jobject obj, jint qid, jintArray _args) {

	(void) obj;

	jsize argc = (*env)->GetArrayLength(env, _args);
	if (argc != 8) /* # UDF kernel constants */
		return -1;
	jint *args = (*env)->GetIntArrayElements(env, _args, 0);
	/* Object `int []` pinned */

	gpu_setKernel (qid, 0,  "clearKernel",     &callback_setKernelAggregateIStream, args);
	gpu_setKernel (qid, 1,  "aggregateKernel", &callback_setKernelAggregateIStream, args);
	/* */
	gpu_setKernel (qid, 1,  "intersectKernel", &callback_setKernelAggregateIStream, args);
	/* */
	gpu_setKernel (qid, 2,  "scanKernel",      &callback_setKernelAggregateIStream, args);
	gpu_setKernel (qid, 3,  "compactKernel",   &callback_setKernelAggregateIStream, args);

	(*env)->ReleaseIntArrayElements(env, _args, args, 0);
	/* Object `int []` released */
	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_allocateBuffer
(JNIEnv *env, jobject obj, jint size, jint readOnly) {

	(void) env;
	(void) obj;

	/*
	 * Align memory according to CL_DEVICE_MEM_BASE_ADDR_ALIGN (see getDeviceInfo()).
	 *
	 * void *p = aligned_alloc (512, size);
	 * if (! p) {
	 * 	fprintf(stderr, "fatal error: out of memory\n");
	 * 	exit(1);
	 * }
	 *
	 * Pin memory
	 *
	 * if (mlock(p, size) != 0) {
	 * 	fprintf(stderr, "fatal error: failed to pin allocated buffer\n");
	 *	int e = errno;
	 *	if (e == EAGAIN) { fprintf(stderr, "EAGAIN\n");
	 *	} else
	 *	if (e == ENOMEM) { fprintf(stderr, "ENOMEM\n");
	 *	} else
	 *	if (e == EPERM)  { fprintf(stderr,  "EPERM\n");
	 *	} else
	 *	if (e == EINVAL) { fprintf(stderr, "EINVAL\n");
	 * 	}
	 *	exit (1);
	 * }
	 */

	return gpu_getDirectBuffer (size, readOnly);
}

JNIEXPORT jobject JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_getDirectByteBuffer
(JNIEnv *env, jobject obj, jint idx) {

	(void) obj;

	if (idx < 0 || idx >= MAX_BUFFERS)
		return NULL;
	directBufferP p = buffers[idx];
	jobject buffer = (*env)->NewDirectByteBuffer(env, p->mapped_buffer, getDirectBufferSize(p));
	return buffer;
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

void callback_setKernelThetaJoin (cl_kernel kernel, gpuContextP context, int *constants) {

	/*
	 * Theta Join kernel signature
	 *
	 * const int __s1_tuples,
	 * const int __s2_tuples,
	 * const int output_size,
	 * __global const uchar *__s1_input,
	 * __global const uchar *__s2_input,
	 * __global const int *window_ptrs_,
	 * __global const int *_window_ptrs,
	 * __global int *flags,
	 * __global int *offsets,
	 * __global int *partitions,
	 * __global uchar *output,
	 * __local int *x
	 */

	/* Get all constants */
	int    __s1_tuples = constants[0];
	int    __s2_tuples = constants[1];
	int    output_size = constants[2];
	int  _scratch_size = constants[3]; /* Local buffer memory size */

	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *) &__s1_tuples);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *) &__s2_tuples);
	error |= clSetKernelArg (kernel, 2, sizeof(int), (void *) &output_size);
	/* Set I/O byte buffers */
	error |= clSetKernelArg (
		kernel,
		3,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		4,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		5,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		6,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[3]->device_buffer));
	
	error |= clSetKernelArg (
		kernel,
		7,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		8,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		9,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		10,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[3]->device_buffer));
	/* Set local memory */
	error |= clSetKernelArg (kernel, 11, (size_t)  _scratch_size, (void *) NULL);

	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return;
}

void callback_setKernelSelect (cl_kernel kernel, gpuContextP context, int *constants) {
	
	/* Get all constants */
	int        size = constants[0];
	int      tuples = constants[1];
	int buffer_size = constants[2]; /* Local buffer memory size */
	
	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *)    &size);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *)  &tuples);
	/* Set I/O byte buffers */
	error |= clSetKernelArg (
		kernel,
		2, 
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		3, 
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		4, 
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		5, 
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		6, 
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[3]->device_buffer));
	/* Set local memory */
	error |= clSetKernelArg (kernel, 7, (size_t) buffer_size, (void *) NULL);
	
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return;
}

void callback_setKernelCompact (cl_kernel kernel, gpuContextP context, int *constants) {

	/* The configuration of this kernel is identical to the select kernel. */
	callback_setKernelSelect (kernel, context, constants);
}

void callback_setKernelAggregate (cl_kernel kernel, gpuContextP context, int *constants) {
	
	/* Kernel arguments are:
 	 * 	
	const int tuples,
	const int bundle_,
	const int bundles,
	const int _table_,
	const int __stash_x,
	const int __stash_y,
	const int max_iterations,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global const int* x,
	__global const int* y,
	__global uchar* contents,
	__global int* stashed,
	__global int* failed,
	__global int* attempts,
	__global int* indices,
	__global int* offsets,
	__global int* partitions,
	__global uchar* output,
	__local int* x
	*/
	
	 /* Get all constants */
	int         tuples = constants[0];
	int        bundle_ = constants[1];
	int        bundles = constants[2];
	int        _table_ = constants[3];
	int      __stash_x = constants[4];
	int      __stash_y = constants[5];
	int max_iterations = constants[6]; 
	int   _buffer_size = constants[7]; /* Local memory size */
	
	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *)         &tuples);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *)        &bundle_);
	error |= clSetKernelArg (kernel, 2, sizeof(int), (void *)        &bundles);
	error |= clSetKernelArg (kernel, 3, sizeof(int), (void *)        &_table_);
	error |= clSetKernelArg (kernel, 4, sizeof(int), (void *)      &__stash_x);
	error |= clSetKernelArg (kernel, 5, sizeof(int), (void *)      &__stash_y);
	error |= clSetKernelArg (kernel, 6, sizeof(int), (void *) &max_iterations);
	/* Set input buffers */
	error |= clSetKernelArg (
		kernel,
		7,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		8,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		9,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		10,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[3]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		11,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[4]->device_buffer));
	/* Set output buffers */
	error |= clSetKernelArg (
        kernel,
        12,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[0]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        13,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[1]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        14,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[2]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        15,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[3]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        16,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[4]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        17,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[5]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        18,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[6]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        19,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[7]->device_buffer));
	/* Set local memory */
	error |= clSetKernelArg (kernel, 20, (size_t) _buffer_size, (void *) NULL);
	
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	
	return;
}

void callback_setKernelAggregateIStream (cl_kernel kernel, gpuContextP context, int *constants) {

	/* Kernel arguments are:
 	 *
	const int tuples,
	const int dummyParam1,
	const int dummyParam2,
	const int _table_,
	const int __stash_x,
	const int __stash_y,
	const int max_iterations,
	__global const uchar* input,
	__global const int* window_ptrs_,
	__global const int* _window_ptrs,
	__global const int* x,
	__global const int* y,
	__global uchar* contents,
	__global int* stashed,
	__global int* failed,
	__global int* attempts,
	__global int* indices,
	__global int* offsets,
	__global int* partitions,
	__global uchar* output,
	__local int* x
	*/

	 /* Get all constants */
	int         tuples = constants[0];
	int    dummyParam1 = constants[1];
	int    dummyParam2 = constants[2];
	int        _table_ = constants[3];
	int      __stash_x = constants[4];
	int      __stash_y = constants[5];
	int max_iterations = constants[6];
	int   _buffer_size = constants[7]; /* Local memory size */

	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *)         &tuples);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *)    &dummyParam1);
	error |= clSetKernelArg (kernel, 2, sizeof(int), (void *)    &dummyParam2);
	error |= clSetKernelArg (kernel, 3, sizeof(int), (void *)        &_table_);
	error |= clSetKernelArg (kernel, 4, sizeof(int), (void *)      &__stash_x);
	error |= clSetKernelArg (kernel, 5, sizeof(int), (void *)      &__stash_y);
	error |= clSetKernelArg (kernel, 6, sizeof(int), (void *) &max_iterations);
	/* Set input buffers */
	error |= clSetKernelArg (
		kernel,
		7,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		8,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		9,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		10,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[3]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		11,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[4]->device_buffer));
	/* Set output buffers */
	error |= clSetKernelArg (
        kernel,
        12,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[0]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        13,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[1]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        14,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[2]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        15,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[3]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        16,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[4]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        17,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[5]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        18,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[6]->device_buffer));
	error |= clSetKernelArg (
        kernel,
        19,
        sizeof(cl_mem),
        (void *) &(context->kernelOutput.outputs[7]->device_buffer));
	/* Set local memory */
	error |= clSetKernelArg (kernel, 20, (size_t) _buffer_size, (void *) NULL);

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
		JNIEnv *env, jobject obj, int qid, int ndx, int mark) {
	// fprintf(stderr, "callback_readOutput\n");
	jclass class = (*env)->GetObjectClass (env, obj);
	jmethodID method = (*env)->GetMethodID (env, class,
			"outputDataMovementCallback", "(IIJII)V");
	if (! method) {
		fprintf(stderr, "JNI error: failed to acquire read method pointer\n");
		exit(1);
	}
	
	if (! context->kernelOutput.outputs[ndx]->writeOnly)
	 	return ;
	
	/* Use the mark */
	int theSize;
	if (mark > 0)
	 	theSize = mark;
	else
		theSize = context->kernelOutput.outputs[ndx]->size;
	
	if (theSize > context->kernelOutput.outputs[ndx]->size) {
		fprintf(stderr, "error: output buffer (qid %d ndx %d) overflow (%d bytes > %d bytes)\n", 
			qid, ndx, theSize, context->kernelOutput.outputs[ndx]->size);
		exit(1);
	}
	/* Copy data across the JNI boundary */
	(*env)->CallVoidMethod (
			env, obj, method,
			qid,
			ndx,
			(long) (context->kernelOutput.outputs[ndx]->mapped_buffer),
			theSize,
			0);

	(*env)->DeleteLocalRef(env, class);
	return;
}

gpuContextP callback_execKernel (gpuContextP context) {
	gpuContextP p = previousQuery[0];
#ifdef GPU_VERBOSE
	if (! p)
		dbg("[DBG] (null) callback_execKernel(%p)\n", context);
	else
		dbg("[DBG] %p callback_execKernel(%p)\n", p, context);
#endif
	/* Shift */
	int i;
	for (i = 0; i < DEPTH - 1; i++) {
		previousQuery[i] = previousQuery [i + 1];
	}
	previousQuery[DEPTH - 1] = context;
	return p;
}
