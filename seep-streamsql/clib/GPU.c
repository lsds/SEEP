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

void callback_setKernelIdentity   (cl_kernel, gpuContextP);
void callback_setKernelSelection  (cl_kernel, gpuContextP);
void callback_setKernelProjection (cl_kernel, gpuContextP);
void callback_setKernelReduction  (cl_kernel, gpuContextP);

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

int gpu_setOutput (int qid, int ndx, int size) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_setOutput (p, ndx, size);
}

int gpu_setKernel (int qid, int ndx,
		const char *name, void (*callback)(cl_kernel, gpuContextP)) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_setKernel (p, ndx, name, callback);
}

int gpu_exec (int qid, void *input, void *output, size_t threads, size_t threadsPerGroup) {
	if (qid < 0 || qid >= Q)
		return -1;
	gpuQueryP p = queries[qid];
	return gpu_query_exec (p, input, output, threads, threadsPerGroup);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_init
(JNIEnv *env, jobject obj) {

	(void) env;
	(void) obj;

	gpu_init ();

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
(JNIEnv *env, jobject obj, jint qid, jint ndx, jint size) {

	(void) env;
	(void) obj;

	return gpu_setOutput (qid, ndx, size);
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_free
(JNIEnv *env, jobject obj) {

	(void) env;
	(void) obj;

	gpu_free ();

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setKernelIdentity
(JNIEnv *env, jobject obj, jint qid) {

	(void) env;
	(void) obj;

	gpu_setKernel (qid, 0, "dummy", &callback_setKernelIdentity);

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setKernelProjection
(JNIEnv *env, jobject obj, jint qid) {

	(void) env;
	(void) obj;

	gpu_setKernel (qid, 0, "project", &callback_setKernelProjection);

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setKernelSelection
(JNIEnv *env, jobject obj, jint qid) {

	(void) env;
	(void) obj;

	gpu_setKernel (qid, 0, "selection", &callback_setKernelProjection);

	return 0;
}

JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU_setKernelReduction
(JNIEnv *env, jobject obj, jint qid) {

	(void) env;
	(void) obj;

	gpu_setKernel (qid, 0, "reduce", &callback_setKernelReduction);

	return 0;
}

void callback_setKernelIdentity (cl_kernel kernel, gpuContextP context) {
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

void callback_setKernelProjection (cl_kernel kernel, gpuContextP context) {

	return;
}

void callback_setKernelReduction (cl_kernel kernel, gpuContextP context) {

	return;
}

void callback_setKernelSelection (cl_kernel kernel, gpuContextP context) {

	return;
}
