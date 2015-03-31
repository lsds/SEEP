#include "gpuquery.h"

#include "openclerrorcode.h"
#include "debug.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <CL/cl.h>

#include <unistd.h>
#include <sched.h>

static pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t  cond;
static unsigned count = 0;
/* Current versions */
static gpuContextP __ctx = NULL;
static queryOperatorP __cop = NULL;
static jobject __obj = NULL;

static int started = 0;

static void *output_handler (void *args) {
	handler_t *handler = (handler_t *) args;
	JNIEnv *env = handler->env;
	JavaVM *jvm = handler->jvm;

	int qid = handler->qid;

	(*jvm)->AttachCurrentThread(jvm, (void **) &env, NULL);

	int core = 3;
	cpu_set_t set;
	CPU_ZERO (&set);
	CPU_SET  (core, &set);
	sched_setaffinity (0, sizeof(set), &set);

	fprintf(stderr, "[GPU] output handler attached (%s)\n", __FUNCTION__);
	fflush (stderr);

	started = 1;

	while (started) {
		pthread_mutex_lock (&lock);
		while (count == 0)
			pthread_cond_wait(&cond, &lock);
		// fprintf(stderr, "Pass.\n");
		// fflush (stderr);
		gpu_context_readOutput (__ctx, __cop->readOutput, env, __obj, qid);
		count -= 1;
		pthread_mutex_unlock(&lock);
	}

	(*jvm)->DetachCurrentThread(jvm);
	return (args) ? NULL : args;
}

void gpu_query_init (gpuQueryP q, JNIEnv *env, int qid) {

	fprintf(stderr, "[GPU] %s\n", __FUNCTION__);
	fflush (stderr);

	if (! env)
		return;

//	handler_t handler;
//	(*env)->GetJavaVM(env, &handler.jvm);
//	handler.env = env;
//	handler.qid = qid;
//
//	pthread_cond_init (&cond, NULL);
//	pthread_mutex_init(&lock, NULL);
//
//	/* Initialise thread */
//	if (pthread_create(&q->thr, NULL, output_handler, (void *) &handler)) {
//		fprintf(stderr, "error: failed to create output handler thread\n");
//		exit (1);
//	}
//
//	while (! started)
//		;
	return ;
}

gpuQueryP gpu_query_new (cl_device_id device, cl_context context,
	const char *source, int _kernels, int _inputs, int _outputs) {
	
	int i;
	int error = 0;
	char msg [32768]; /* Compiler message */
	size_t length;
	const char *flags = "-cl-nv-verbose -Werror"; /* "-cl-fast-relaxed-math -cl-nv-verbose -Werror"; */ /* -cl-nv-arch sm_20 */
	gpuQueryP p = (gpuQueryP) malloc (sizeof(gpu_query_t));
	if (! p) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit(1);
	}
	p->device = device;
	p->context = context;
	/* Create program */
	p->program = clCreateProgramWithSource (
		p->context, 
		1, 
		(const char **) &source, 
		NULL, 
		&error);
	if (! p->program) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	/* Build program */
	error = clBuildProgram (
		p->program, 
		1, 
		&device, 
		flags, 
		NULL, 
		NULL);
	/* Get compiler info (or error) */
	clGetProgramBuildInfo (
		p->program, 
		p->device, 
		CL_PROGRAM_BUILD_LOG, 
		sizeof(msg), 
		msg, 
		&length);
	fprintf(stderr, "%s (%zu chars)\n", msg, length);
	fflush (stderr);
	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	p->ndx = -1;
	p->phase = 0;
	for (i = 0; i < DEPTH; i++) {
		p->contexts[i] =
			gpu_context (p->device, p->context, p->program, _kernels, _inputs, _outputs);
	}
	return p;
}

void gpu_query_free (gpuQueryP p) {
	int i;
	if (p) {
		for (i = 0; i < DEPTH; i++)
			gpu_context_free (p->contexts[i]);
		if (p->program)
			clReleaseProgram (p->program);
		free (p);
	}
}

int gpu_query_setInput (gpuQueryP q, int ndx, void *buffer, int size) {
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernelInput.count)
		return -1;
	int i;
	for (i = 0; i < DEPTH; i++)
		gpu_context_setInput (q->contexts[i], ndx, buffer, size);
	return 0;
}

int gpu_query_setOutput (gpuQueryP q, int ndx, void *buffer, int size, int writeOnly) {
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernelOutput.count)
		return -1;
	int i;
	for (i = 0; i < DEPTH; i++)
		gpu_context_setOutput (q->contexts[i], ndx, buffer, size, writeOnly);
	return 0;
}

int gpu_query_setKernel (gpuQueryP q, int ndx, const char * name,
		void (*callback)(cl_kernel, gpuContextP, int *), int *args) {
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernel.count)
		return -1;
	int i;
	for (i = 0; i < DEPTH; i++)
		gpu_context_setKernel (q->contexts[i], ndx, name, callback, args);
	return 0;
}

gpuContextP gpu_context_switch (gpuQueryP p) {
	if (! p) {
		fprintf (stderr, "error: null query\n");
		return NULL;
	}
#ifdef GPU_VERBOSE
	int current = (p->ndx) % DEPTH;
#endif
	int next = (++p->ndx) % DEPTH;
#ifdef GPU_VERBOSE
	if (current >= 0)
		dbg ("[DBG] switch from %d (%lld read(s), %lld write(s)) to context %d\n",
			current, p->contexts[current]->readCount, p->contexts[current]->writeCount, next);
#endif
	return p->contexts[next];
}

//int gpu_query_exec (gpuQueryP q, size_t threads, size_t threadsPerGroup,
//		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
//	if (! q)
//		return -1;
//	gpuContextP p = gpu_context_switch (q);
//
//	// printf("[DBG] gpu_query_exec\n");
//
//
//	gpu_context_waitForReadEvent (p);
//	gpu_context_readOutput (p, operator->readOutput, env, obj, qid);
//
////	pthread_mutex_lock(&lock);
////
////	/* Set static variables */
////
////	__cop = operator;
////	__ctx = p;
////	__obj = obj;
////	if (count == 0)
////		pthread_cond_signal(&cond);
////	count += 1;
////	pthread_mutex_unlock(&lock);
//
//	/* Wait for write event */
//	gpu_context_waitForWriteEvent (p);
//
//	/* Write input */
//	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);
//
//	// gpu_context_flush (p);
//
////	 while (count == 1)
////	 	;
//
//
//	// gpu_context_flush (p);
//
//	/* Wait for read event */
//
//	/* Wait for execute event */
//	/* gpu_context_waitForExecEvent (p); */
//
//
//	// gpu_context_flush (p);
//
//	gpu_context_moveInputBuffers (p);
//
//	gpu_context_submitKernel (p, threads, threadsPerGroup);
//
//	// gpu_context_moveOutputBuffers (p);
//
////	gpu_context_flush (p);
//
//
////	pthread_mutex_lock(&lock);
////	while (count == 1)
////		pthread_cond_wait(&cond, &lock);
////	pthread_mutex_unlock(&lock);
//
//	// pthread_join (q->thr, NULL);
//
//	/* Read output */
//	// gpu_context_readOutput (p, operator->readOutput, env, obj, qid);
//
//	/* Submit task */
//	gpu_context_moveOutputBuffers (p);
//
//	gpu_context_flush (p);
//
//	return 0;
//}

int gpu_query_exec (gpuQueryP q, size_t threads, size_t threadsPerGroup,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	
	gpuContextP theOther = q->contexts[(q->ndx + 1) % DEPTH];

	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);
	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	gpu_context_moveInputBuffers (p);

	gpu_context_submitKernel (p, threads, threadsPerGroup);

	gpu_context_moveOutputBuffers (p);

	gpu_context_flush (p);

	gpu_context_waitForReadEvent (theOther);
	gpu_context_readOutput (theOther, operator->readOutput, env, obj, qid);

	return 0;
}

int gpu_query_custom_exec (gpuQueryP q, size_t threads, size_t threadsPerGroup,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid, size_t _threads, size_t _threadsPerGroup) {
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	gpuContextP theOther = q->contexts[(q->ndx + 1) % DEPTH];

//	printf("[DBG] In gpu_query_custom_exec...\n");

	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);
	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	gpu_context_moveInputBuffers (p);

	gpu_context_custom_submitKernel (p, threads, threadsPerGroup, _threads, _threadsPerGroup);

	gpu_context_moveOutputBuffers (p);

	gpu_context_flush (p);

	gpu_context_waitForReadEvent (theOther);

	gpu_context_readOutput (theOther, operator->readOutput, env, obj, qid);

	return 0;
}

int gpu_query_testJNIDataMovement (gpuQueryP q,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	/* Read output */
	gpu_context_readOutput (p, operator->readOutput, env, obj, qid);

	return 0;
}

int gpu_query_copyInputBuffers (gpuQueryP q,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	return 0;
}

int gpu_query_moveInputBuffers (gpuQueryP q,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {

	(void) operator;
	(void) env;
	(void) obj;
	(void) qid;

	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);

	/* Move input */
	gpu_context_moveInputBuffers (p);
	gpu_context_flush(p);

	return 0;
}

int gpu_query_copyOutputBuffers (gpuQueryP q,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	/* Read output */
	gpu_context_readOutput (p, operator->readOutput, env, obj, qid);

	return 0;
}

int gpu_query_moveOutputBuffers (gpuQueryP q,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {

	(void) operator;
	(void) env;
	(void) obj;
	(void) qid;

	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	/* Wait for read event */
	gpu_context_waitForReadEvent (p);

	/* Move output */
	gpu_context_moveOutputBuffers (p);
	gpu_context_flush(p);

	return 0;
}

int gpu_query_moveInputAndOutputBuffers (gpuQueryP q,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {

	(void) operator;
	(void) env;
	(void) obj;
	(void) qid;

	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);

	/* Wait for read event */
	gpu_context_waitForReadEvent (p);

	/* Move input */
	gpu_context_moveInputBuffers (p);

	/* Move output */
	gpu_context_moveOutputBuffers (p);

	gpu_context_flush(p);

	return 0;
}

int gpu_query_testDataMovement (gpuQueryP q,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {

	(void) operator;
	(void) env;
	(void) obj;
	(void) qid;

	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	
	/* Wait for write event */
	// gpu_context_waitForWriteEvent (p);

	/* Wait for read event */
	// gpu_context_waitForReadEvent (p);

	/* Write input */
	/* gpu_context_writeInput (p, operator->writeInput, env, obj, qid); */
	
	/* Read output */
	/* gpu_context_readOutput (p, operator->readOutput, env, obj, qid); */
	
	/* Move input */
	// gpu_context_moveInputBuffers (p);
	
	/* gpu_context_submitKernel (p, 524288, 256); */
	
	/* Move output */
	// gpu_context_moveOutputBuffers (p);
	
	// gpu_context_flush (p);
	
	/*
	 * Returns results of this task
	 */
	
	gpu_context_waitForWriteEvent (p);
	gpu_context_waitForReadEvent (p);

	gpu_context_writeInput (p, operator->readOutput, env, obj, qid);
	gpu_context_readOutput (p, operator->readOutput, env, obj, qid);

	gpu_context_moveInputBuffers (p);
	// gpu_context_flush (p);
	gpu_context_submitKernel (p, 524288, 256);
	gpu_context_moveOutputBuffers (p);
	gpu_context_flush (p);

	return 0;
}

int gpu_query_testOverlap (gpuQueryP q, size_t threads, size_t threadsPerGroup,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	gpu_context_waitForWriteEvent (p);
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	gpu_context_waitForReadEvent (p);
	gpu_context_readOutput (p, operator->readOutput, env, obj, qid);

	gpu_context_submitOverlappingTask (p, threads, threadsPerGroup);

	return 0;
}

int gpu_query_interTaskOverlap (gpuQueryP q, size_t threads, size_t threadsPerGroup,
		queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {

	(void) operator;
	(void) env;
	(void) obj;
	(void) qid;

	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	/* Get the other context */
	gpuContextP theOther = q->contexts[(q->ndx + 1) % DEPTH];

	if (q->phase == 0) {

		/* Schedule read on the other context */
		gpu_context_moveOutputBuffers(theOther);

		/* Schedule write on this context */
		/* gpu_context_waitForWriteEvent (p); */
		/* gpu_context_writeInput (p, operator->writeInput, env, obj, qid); */

		gpu_context_moveInputBuffers (p);

		/* Flush command queues */
		gpu_context_flush (p);
		gpu_context_flush (theOther);

		/* Read output */
		/* gpu_context_waitForReadEvent (p); */
		/* gpu_context_readOutput (p, operator->readOutput, env, obj, qid); */

		gpu_context_waitForReadEvent (p);
		gpu_context_waitForWriteEvent (p);

		/* Next phase */
		q->phase = 1;

	} else { /* Execute phase 1 and 2 */

		/* Schedule task on the other context */
		gpu_context_submitKernel (theOther, threads, threadsPerGroup);

		/* Schedule write on this context */
		/* gpu_context_waitForWriteEvent (p); */
		/* gpu_context_writeInput (p, operator->writeInput, env, obj, qid); */
		gpu_context_moveInputBuffers (p);

		/* Flush command queues */
		gpu_context_flush (p);
		gpu_context_flush (theOther);

		gpu_context_submitKernel (p, threads, threadsPerGroup);
		gpu_context_moveOutputBuffers (theOther);

		/* Flush command queues */
		gpu_context_flush (p);
		gpu_context_flush (theOther);

		/* Read output */
		/* gpu_context_waitForReadEvent (p); */
		/* gpu_context_readOutput (p, operator->readOutput, env, obj, qid); */

		gpu_context_waitForReadEvent (p);
		gpu_context_waitForWriteEvent (p);

		/* Next phase */
		q->phase = 0;
	}

	return 0;
}
