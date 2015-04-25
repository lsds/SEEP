#include "gpuquery.h"

#include "openclerrorcode.h"
#include "debug.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <CL/cl.h>

#include <unistd.h>
#include <sched.h>

static int gpu_query_exec_1 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* w/o  pipelining */
static int gpu_query_exec_2 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* with pipelining */
static int gpu_query_exec_3 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* copy w/o  worker thread */

#ifdef GPU_IIDMVMT
static int gpu_query_exec_4 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* copy with worker thread */
static int gpu_query_exec_5 (gpuQueryP, size_t *, size_t *, queryOperatorP, JNIEnv *, jobject, int); /* with pipelining and with worker thread */

static volatile unsigned count = 0;

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
		while (count != 1)
			;
		gpu_context_readOutput (__ctx, __op_->readOutput, env, __obj, qid);
		/* Block again */
		count -= 1;
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
	
	if (! env)
		return;
#ifdef GPU_IIDMVMT
	handler_t handler;
	(*env)->GetJavaVM(env, &handler.jvm);
	handler.env = env;
	handler.qid = qid;
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
	
	const char *flags = "-cl-fast-relaxed-math -cl-nv-verbose -Werror"; 
	/* -cl-nv-arch sm_20 */
	
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

int gpu_query_setOutput (gpuQueryP q, int ndx, void *buffer, int size, 
	int writeOnly, int doNotMove, int bearsMark, int readEvent) {
	
	if (! q)
		return -1;
	if (ndx < 0 || ndx > q->contexts[0]->kernelOutput.count)
		return -1;
	int i;
	for (i = 0; i < DEPTH; i++)
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

int gpu_query_exec (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
	queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
	   return gpu_query_exec_1 (q, threads, threadsPerGroup, operator, env, obj, qid);   
	/* return gpu_query_exec_2 (q, threads, threadsPerGroup, operator, env, obj, qid); */
	/* return gpu_query_exec_3 (q, threads, threadsPerGroup, operator, env, obj, qid); */
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
	// gpu_context_waitForWriteEvent (p);
	
	/* Write input */
	// gpu_context_writeInput (p, operator->writeInput, env, obj, qid);

	// gpu_context_moveInputBuffers (p);

	// gpu_context_submitKernel (p, threads, threadsPerGroup);

	// gpu_context_moveOutputBuffers (p);

	// gpu_context_flush (p);
	
	// gpu_context_waitForReadEvent (p);
	
	// gpu_context_readOutput (p, operator->readOutput, env, obj, qid);

#ifdef GPU_PROFILE
	gpu_context_profileQuery (p);
#endif

	return 0;
}

static int gpu_query_exec_2 (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
	queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	
	gpuContextP theOther = (operator->execKernel(p));
	
	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);
	
	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);
	
	if (theOther) {
		
		/* Wait for read event from previous query */
		gpu_context_waitForReadEvent (theOther);
		/* Read output */
		gpu_context_readOutput (theOther, operator->readOutput, env, obj, qid);
#ifdef GPU_PROFILE
		gpu_context_profileQuery (theOther);
#endif
	}
	
	gpu_context_moveInputBuffers (p);
	
	gpu_context_submitKernel (p, threads, threadsPerGroup);
	
	gpu_context_moveOutputBuffers (p);
	
	gpu_context_flush (p);
	
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
		count = 1;
	}
	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);
	
	return 0;
}

static int gpu_query_exec_5 (gpuQueryP q, size_t *threads, size_t *threadsPerGroup,
	queryOperatorP operator, JNIEnv *env, jobject obj, int qid) {
	
	if (! q)
		return -1;
	gpuContextP p = gpu_context_switch (q);
	
	gpuContextP theOther = (operator->execKernel(p));
	
	/* Set static variables */
	__ctx = theOther;
	__op_ = operator;
	__obj = obj;
	if (theOther) {
		/* Wait for read event from previous query */
		gpu_context_waitForReadEvent (theOther);
		/* Notify output handler */
		count = 1;
	}
	
	/* Wait for write event */
	gpu_context_waitForWriteEvent (p);
	
	/* Write input */
	gpu_context_writeInput (p, operator->writeInput, env, obj, qid);
	
	gpu_context_moveInputBuffers (p);
	
	gpu_context_submitKernel (p, threads, threadsPerGroup);
	
	gpu_context_moveOutputBuffers (p);
	
	gpu_context_flush (p);
	
	/* Wait until read output from previous query has finished */
	while (count == 1);
	
#ifdef GPU_PROFILE
	if (theOther)
		gpu_context_profileQuery (p);
#endif
	
	return 0;
}
#endif

