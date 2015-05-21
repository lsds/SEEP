#include "gpuquery.h"

#include "openclerrorcode.h"
#include "debug.h"

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

static int gpu_query_exec_1 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* w/o  pipelining */
static int gpu_query_exec_2 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* with pipelining */
static int gpu_query_exec_3 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* copy w/o  worker thread */

#ifdef GPU_TMSRMNT
#include "timer.h"
timerP timer;
unsigned long long nmeasurements = 0ULL;
unsigned long long total = 0ULL;
#endif

#ifdef GPU_IIDMVMT
static int gpu_query_exec_4 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* copy with worker thread */
static int gpu_query_exec_5 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* with pipelining and with worker thread */

static volatile unsigned count = 0;

pthread_mutex_t *mutex;
pthread_cond_t *reading, *waiting;

/* Current versions */
static gpuContextP    __ctx = NULL;
static queryOperatorP __op_ = NULL;
static jobject        __obj = NULL;

static volatile int started = 0;

static void *output_handler (void *args) {
	
	handler_t *handler = (handler_t *) args;
	JNIEnv *env = handler->env;
	JavaVM *jvm = handler->jvm;
	int qid = handler->qid;
	
	(*jvm)->AttachCurrentThread(jvm, (void **) &env, NULL);
	/* Pin this thread to a particular core: 0 is the dispatcher, 1 is the GPU. */
	int core = 2;
	cpu_set_t set;
	CPU_ZERO (&set);
	CPU_SET (core, &set);
	sched_setaffinity (0, sizeof(set), &set);
	
	fprintf(stderr, "[GPU] output handler attached (%s, core=%02d)\n", __FUNCTION__, core);
	fflush (stderr);
	
	started = 1;
	
	while (started) {

		pthread_mutex_lock (mutex);
		while (count != 1)
			pthread_cond_wait(waiting, mutex);
		gpu_context_readOutput (__ctx, __op_->readOutput, env, __obj, qid);
		/* Block again */
		count -= 1;
		pthread_mutex_unlock (mutex);
		pthread_cond_signal (reading);
	}
	
	(*jvm)->DetachCurrentThread(jvm);
	return (args) ? NULL : args;
}
#endif

void gpu_query_init (gpuQueryP q, JNIEnv *env, int qid) {

	fprintf(stderr, "[GPU] %s\n", __FUNCTION__);
	fflush (stderr);
	
	(void) q;
	(void) qid;

#ifdef GPU_TMSRMNT
	printf ("Create timer...\n");
	timer = timer_new();
#endif
	
	if (! env)
		return;
#ifdef GPU_IIDMVMT
	handler_t handler;
	(*env)->GetJavaVM(env, &handler.jvm);
	handler.env = env;
	handler.qid = qid;

	/* Initialise mutex and conditions */
	count = 0;
	mutex = (pthread_mutex_t *) malloc (sizeof(pthread_mutex_t));
	pthread_mutex_init (mutex, NULL);
	reading = (pthread_cond_t *) malloc (sizeof(pthread_cond_t));
	pthread_cond_init (reading, NULL);
	waiting = (pthread_cond_t *) malloc (sizeof(pthread_cond_t));
	pthread_cond_init (waiting, NULL);

	/* Initialise thread */
	if (pthread_create(&q->thr, NULL, output_handler, (void *) &handler)) {
		fprintf(stderr, "error: failed to create output handler thread\n");
		exit (1);
	}
	/* Wait until thread attaches itself to the JVM */
	while (! started) ;
#endif
	return ;
}

gpuQueryP gpu_query_new (cl_device_id device, cl_context context,
	const char *source, int _kernels, int _inputs, int _outputs) {
	
	int i;
	int error = 0;
	char msg [32768]; /* Compiler message */
	size_t length;
	/*
	 * TODO
	 *
	 * Remove the following macro and select -cl-nv-verbose
	 * based on the type of GPU device (i.e. NVIDIA or not)
	 */
#ifdef __APPLE__
	const char *flags = "-cl-fast-relaxed-math -Werror";
#else
	const char *flags = "-cl-fast-relaxed-math -Werror -cl-nv-verbose";
#endif
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
	for (i = 0; i < NCONTEXTS; i++) {
		p->contexts[i] =
			gpu_context (p->device, p->context, p->program, _kernels, _inputs, _outputs);
	}
	return p;
}

void gpu_query_free (gpuQueryP p) {
	int i;
	if (p) {
		for (i = 0; i < NCONTEXTS; i++)
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
	for (i = 0; i < NCONTEXTS; i++)
		gpu_context_setInput (q->contexts[i], ndx, buffer, size);
	return 0;
}

int gpu_query_setOutput (gpuQueryP q, int ndx, void *buffer, int size, 
	int writeOnly, int doNotMove, int bearsMark, int readEvent) {
	
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernelOutput.count)
		return -1;
	int i;
	for (i = 0; i < NCONTEXTS; i++)
		gpu_context_setOutput (q->contexts[i], ndx, buffer, size, 
			writeOnly, doNotMove, bearsMark, readEvent);
	return 0;
}

int gpu_query_setKernel (gpuQueryP q, int ndx, const char * name,
		void (*callback)(cl_kernel, gpuContextP, int *), int *args) {
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernel.count)
		return -1;
	int i;
	for (i = 0; i < NCONTEXTS; i++)
		gpu_context_setKernel (q->contexts[i], ndx, name, callback, args);
	return 0;
}

gpuContextP gpu_context_switch (gpuQueryP p) {
	if (! p) {
		fprintf (stderr, "error: null query\n");
		return NULL;
	}
#ifdef GPU_VERBOSE
	int current = (p->ndx) % NCONTEXTS;
#endif
	int next = (++p->ndx) % NCONTEXTS;
#ifdef GPU_VERBOSE
	if (current >= 0)
		dbg ("[DBG] switch from %d (%lld read(s), %lld write(s)) to context %d\n",
			current, p->contexts[current]->readCount, p->contexts[current]->writeCount, next);
#endif
	return p->contexts[next];
}

int gpu_query_exec (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
	queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
#ifndef GPU_IIDMVMT
	if (NCONTEXTS > 1)
		return gpu_query_exec_2 (q, threads, threadsPerGroup, operator, env, obj, qid);
	else
		return gpu_query_exec_1 (q, threads, threadsPerGroup, operator, env, obj, qid);
#else
	return gpu_query_exec_5 (q, threads, threadsPerGroup, operator, env, obj, qid);
#endif
}

/* */
static int gpu_query_exec_1 (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
	queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	
	gpuContextP theOther = (operator->execKernel(p));
	(void) theOther;
	
	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);
	
	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	gpu_context_moveInputBuffers (p);

	gpu_context_submitKernel (p, threads, threadsPerGroup);

	gpu_context_moveOutputBuffers (p);

	gpu_context_flush (p);
	
	gpu_context_waitForReadEvent (p);
	
	
	gpu_context_readOutput (p, operator->readOutput, env, obj, qid);

	return 0;
}

static int gpu_query_exec_2 (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
	queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	
	gpuContextP theOther = (operator->execKernel(p));
	
	if (p == theOther) {
		fprintf(stderr, "error: invalid context\n");
		exit(-1);
	}
	
#ifdef GPU_TMSRMNT
	timer_start(timer);
#endif
	/* Wait for write event */
	// gpu_context_waitForWriteEvent (p);

	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	if (theOther) {

		/* Wait for read event from previous query */
		// gpu_context_waitForReadEvent (theOther);
		// gpu_context_finish(theOther);
		/* Read output */
		// gpu_context_readOutput (theOther, operator->readOutput, env, obj, qid);
	}

#ifdef GPU_TMSRMNT
	tstamp_t dt = timer_getElapsedTime(timer);
	fprintf (stderr, "[PRF] copy input/output %10llu usecs\n", dt);
#endif
	
	gpu_context_moveInputBuffers (p);
	
	gpu_context_submitKernel (p, threads, threadsPerGroup);
	
	gpu_context_moveOutputBuffers (p);
	
	gpu_context_flush (p);
	
	// gpu_context_waitForReadEvent (p);

	return 0;
}

static int gpu_query_exec_3 (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
    queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
	(void) threads;
	(void) threadsPerGroup;
	
	if (! q)
		return -1;
	
	gpuContextP p = gpu_context_switch (q);
	gpuContextP theOther = (operator->execKernel(p));
	
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);
	if (theOther)
		gpu_context_readOutput (theOther, operator->readOutput, env, obj, qid);
	
	return 0;
}

#ifdef GPU_IIDMVMT
static int gpu_query_exec_4 (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
    queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
	(void) threads;
	(void) threadsPerGroup;
	
	if (! q)
		return -1;
	
	gpuContextP p = gpu_context_switch (q);
	gpuContextP theOther = (operator->execKernel(p));
	
	/* Set static variables */
	__ctx = theOther;
	__op_ = operator;
	__obj = obj;
	if (theOther) {
		/* Notify output handler */
		pthread_mutex_lock (mutex);
		count = 1;
		pthread_mutex_unlock (mutex);
		pthread_cond_signal (waiting);
	}
	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);
	
	/* Wait until read output from previous query has finished */
	if (theOther) {
		pthread_mutex_lock (mutex);
		while (count == 1)
			pthread_cond_wait (reading, mutex);
		pthread_mutex_unlock (mutex);
	}

	return 0;
}

static int gpu_query_exec_5 (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
	queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	
	gpuContextP theOther = (operator->execKernel(p));

#ifdef GPU_TMSRMNT
	timer_start(timer);
#endif
	/* Set static variables */
	__ctx = theOther;
	__op_ = operator;
	__obj = obj;
	if (theOther) {
		/* Wait for read event from previous query */
 		// gpu_context_waitForReadEvent (theOther);
		gpu_context_finish(theOther);
		/* Notify output handler */
		pthread_mutex_lock (mutex);
		count = 1;
		pthread_mutex_unlock (mutex);
		pthread_cond_signal (waiting);
	}
	
	/* Wait for write event */
 	// gpu_context_waitForWriteEvent (p);

	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

#ifdef GPU_TMSRMNT
	tstamp_t dt = timer_getElapsedTime(timer);
	total += dt;
	nmeasurements += 1;
	if (nmeasurements == 100) {
		fprintf (stderr, "[PRF] 100 copy input/output took %10llu usecs\n", total);
		total = 0ULL;
		nmeasurements = 0;
	}
#endif

	gpu_context_moveInputBuffers (p);

	gpu_context_submitKernel (p, threads, threadsPerGroup);
	
	gpu_context_moveOutputBuffers (p);
	
	gpu_context_flush (p);
	
	/* Wait until read output from previous query has finished */
 	if (theOther) {
		pthread_mutex_lock (mutex);
		while (count == 1) {
			pthread_cond_wait (reading, mutex);
		}
		pthread_mutex_unlock (mutex);
	}

	return 0;
}
#endif

int gpu_query_exec_direct (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
	int *start, int *end,
	queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {

	(void) env;
	(void) obj;
	(void) qid;

	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);

	gpuContextP theOther = (operator->execKernel(p));
	(void) theOther;

	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);

	gpu_context_moveDirectInputBuffers (p, start, end);

	gpu_context_submitKernel (p, threads, threadsPerGroup);

	gpu_context_moveOutputBuffers (p);

	gpu_context_flush (p);

	gpu_context_waitForReadEvent (p);

	return 0;
}
