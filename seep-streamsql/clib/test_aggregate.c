#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <signal.h>

#include <unistd.h>

#include <fcntl.h>

#include <sys/stat.h>

#include <math.h>
#include <float.h>

#include "GPU.h"

#include "debug.h"

void callback_setKernels (cl_kernel, gpuContextP, int *);
void callback_writeInput (gpuContextP, JNIEnv *, jobject, int, int, int);
void callback_readOutput (gpuContextP, JNIEnv *, jobject, int, int, int);

typedef struct {
	long t;
	float _1; /* The aggregate */
	int _2; /* The key */
	int _3;
	int _4;
	int _5;
	int _6;
} input_tuple_t  __attribute__((aligned(1)));

typedef struct {
	long t;
	int _1; /* Key */
	float _2; /* Value */
} output_tuple_t  __attribute__((aligned(1)));

static int _hash_functions = 5;

static float _scale_factor = 1.25F;

static float _min_space_requirements [] = {
	FLT_MAX,
	FLT_MAX,
	2.01F,
	1.10F,
	1.03F,
	1.02F
};

/* Default stash table size (# tuples) */
static int _stash = 100;

static unsigned char  *input = NULL;
static unsigned char *output = NULL;

static int *startPtrs = NULL;
static int *endPtrs   = NULL;

static unsigned char *contents = NULL;

static int *indices = NULL;

static int *offsets = NULL;

static int  *stashed = NULL;
static int   *failed = NULL;
static int *attempts = NULL;

static int *x = NULL;
static int *y = NULL;

static int must_exit = 0;
static int sig_received;

static int nextInt (int n) {
	if ((n - 1) == RAND_MAX) {
		return rand();
	} else {
		long end = RAND_MAX / n;
		if (end <= 0) {
			fprintf(stderr, "random number generator error:\n");
			exit (1);
		}
		end *= n;
		int r;
		while ((r = rand()) >= end)
			;
		return r % n;
	}
}

static int computeIterations (int n) {
	int result = 7;
	double logn = (double) (log((double) n) / log(2.0f));
	return (int) (result * logn);
}

static void constants (int *x, int *y, int count, int *__stash_x, int *__stash_y) {
	int prime = 2147483647;
	int i;
	int t;
	for (i = 0; i < count; i++) {
		t = nextInt(prime);
		x[i] = (1 > t ? 1 : t);
		y[i] = nextInt(prime) % prime;
	}
	/* Stash hash constants */
	int v = nextInt(prime);
	v = (v > 1) ? v : 1;
	*__stash_x = v % prime;
	*__stash_y = nextInt(prime) % prime;
}

static void signal_handler (int signum) {
	must_exit = 1;
	sig_received = signum;
}

int main (int argc, char* argv[]) {

	char *kernelfile;
	char *kernelcode;
	int fd;
	struct stat fileinfo;
	if (argc < 1 || argv[1] == NULL) {
		fprintf(stderr, "./test_reduce [kernel filename]\n");
		exit (1);
	}
	kernelfile = argv[1];
	if ((fd = open(kernelfile, O_RDONLY)) < 0) {
		fprintf(stderr, "error: cannot open %s\n", kernelfile);
		exit (1);
	}
	if ((fstat(fd, &fileinfo)) < 0) {
		fprintf(stderr, "error: cannot stat %s\n", kernelfile);
		exit (1);
	}
	kernelcode = (char *) malloc(fileinfo.st_size);
	if (! kernelcode) {
		fprintf(stderr, "error: malloc(%d) failed\n", (int) fileinfo.st_size);
		exit (1);
	}
	int bytes = 0;
	while (bytes < (int) fileinfo.st_size)
		bytes += pread(fd, kernelcode + bytes, fileinfo.st_size, bytes);
	if (bytes != (int) fileinfo.st_size) {
		fprintf(stderr, "error: read only %d/%d bytes from %s\n",
			bytes, (int) fileinfo.st_size, kernelfile);
		free (kernelcode);
		exit (1);
	}

	int size = 1048576;
	int  _input_tuple_size =  32;
	int _output_tuple_size =  16;
	size_t threadsPerGroup = 256;
	int windows = 32;

	int _input_size = size;
	int tuples = _input_size / _input_tuple_size;
	/* Configure #threads, #groups and threads group size */
	int groups = windows;
	size_t threads = groups * threadsPerGroup;
	int _output_size = tuples * _output_tuple_size;
	/* Assume tumbling windows */
	int tpg = tuples / groups;

	/* Check for correctness */
	dbg ("[DBG] %zu threads %d groups\n", threads, groups);

	/* Initialise input and output */

	input  = (unsigned char *) malloc ( _input_size);
	output = (unsigned char *) malloc (_output_size);

	int _window_ptrs_size = sizeof (int) * windows;
	startPtrs = (int *) malloc (_window_ptrs_size);
	endPtrs   = (int *) malloc (_window_ptrs_size);

	x = (int *) malloc (sizeof (int) * _hash_functions);
	y = (int *) malloc (sizeof (int) * _hash_functions);
	int __stash_x = 0;
	int __stash_y = 0;
	/* Populate hash constants */
	constants (x, y, _hash_functions, &__stash_x, &__stash_y);

	float alpha = _scale_factor;
	if (alpha < _min_space_requirements[_hash_functions]) {
		fprintf(stderr, "fatal confuiguration error:\n");
		exit (1);
	}
	int _table_size = (int) ceil ((float) tpg * alpha);
	while (((_table_size + _stash) % threadsPerGroup) != 0)
		_table_size += 1;
	int _table_slots = _table_size + _stash;

	int _contents_size = _table_slots * groups * _output_tuple_size;
	contents = (unsigned char *) malloc (_contents_size);

	int _indices_size = _table_slots * groups * sizeof (int);
	indices = (int *) malloc (_indices_size);

	int _offsets_size = _table_slots * groups * sizeof (int);
	offsets = (int *) malloc (_offsets_size);

	stashed  = (int *) malloc (sizeof(int) * groups);
	failed   = (int *) malloc (sizeof(int) * groups);
	attempts = (int *) malloc (sizeof(int) * tuples);

	int iterations = computeIterations (tpg);

	int i;
	int idx;
	for (i = 0; i < tuples; i++) {
		idx = i * sizeof(input_tuple_t);
		input_tuple_t *t = (input_tuple_t *) &input[idx];
		t->_1 = 1;
		t->_2 = nextInt(100);
	}

	memset (output, 0, _output_size);

	memset (contents, 0, _contents_size);
	memset (indices, -1, _indices_size);
	memset (offsets, 0, _offsets_size);

	memset (stashed,  0, sizeof(int) * groups);
	memset (failed,   0, sizeof(int) * groups);
	memset (attempts, 0, sizeof(int) * tuples);

	/* Initialise window pointers
	 *
	 * #tuples/window is `tuples`/`windows`
	 */
	int tpw = tuples / windows;
	for (i = 0; i < windows; i++) {
		startPtrs[i] = i * tpw * _input_tuple_size;
		endPtrs  [i] = startPtrs[i] + tpw * _input_tuple_size - 1;
	}

	gpu_init(1);
	int qid = gpu_getQuery (kernelcode, 1, 5, 7);
	printf("qid %d\n", qid);

	gpu_setInput  (qid, 0, _input_size);
	gpu_setInput  (qid, 1, _window_ptrs_size);
	gpu_setInput  (qid, 2, _window_ptrs_size);
	gpu_setInput  (qid, 3, sizeof (int) * _hash_functions);
	gpu_setInput  (qid, 4, sizeof (int) * _hash_functions);

	gpu_setOutput (qid, 0, _contents_size, 0);
	gpu_setOutput (qid, 1, sizeof(int) * groups, 0);
	gpu_setOutput (qid, 2, sizeof(int) * groups, 0);
	gpu_setOutput (qid, 3, sizeof(int) * tuples, 0);
	gpu_setOutput (qid, 4, _indices_size, 0);
	gpu_setOutput (qid, 5, _offsets_size, 0);
	gpu_setOutput (qid, 6, _output_size, 1);

	int args[5];
	args[0] = tuples;
	args[1] = _table_size;
	args[2] = __stash_x;
	args[3] = __stash_y;
	args[4] = iterations;

	printf("|t| = %d\n", _table_size);
	printf("%d iterations\n", iterations);

	gpu_setKernel (qid, 0, "aggregateKernel", &callback_setKernels, &args[0]);
	queryOperatorP operator = (queryOperatorP) malloc (sizeof(query_operator_t));
	if (! operator) {
		fprintf(stderr, "fatal error: out of memory\n");
		exit (1);
	}
	operator->writeInput = callback_writeInput;
	operator->readOutput = callback_readOutput;
	/* Establish signal handlers */
	if (signal (SIGTERM, signal_handler) == SIG_IGN)
		signal (SIGTERM, SIG_IGN);
	if (signal (SIGINT,  signal_handler) == SIG_IGN)
		signal (SIGINT,  SIG_IGN);
	if (signal (SIGHUP,  signal_handler) == SIG_IGN)
		signal (SIGHUP,  SIG_IGN);
	printf ("Running...\n");
	int runs = 0;
	while (! must_exit && runs++ < 3) {
	 	gpu_exec (qid, threads, threadsPerGroup, operator, NULL, NULL);
	}
	for (i = 0; i < groups; i++) {
		 printf("stashed[%4d] = %3d\t failed[%4d] = %3d\n", i, stashed[i], i, failed[i]);
	}
	for (i = 0; i < _table_slots; i++) {
		printf("offsets[%4d] = %6d\tindices[%4d] = %6d\n", i, offsets[i], i, indices[i]);
	}
	printf ("Bye.\n");

	gpu_free();
	free (input);
	free (output);
	free (startPtrs);
	free (endPtrs);
	free (x);
	free (y);
	free (contents);
	free (indices);
	free (stashed);
	free (failed);
	free (attempts);
	free (kernelcode);
	exit (0);
}

void callback_setKernels (cl_kernel kernel, gpuContextP context, int *constants) {

	/* __kernel void aggregateKernel (
	 * const int tuples,
	 * const int _table_,
	 * const int __stash_x,
	 * const int __stash_y,
	 * const int max_iterations,
	 *
	 * __global const uchar* input,
	 * __global const int* window_ptrs_,
	 * __global const int* _window_ptrs,
	 * __global const int* x,
	 * __global const int* y,
	 *
	 * __global uchar* contents,
	 * __global int* stashed,
	 * __global int* failed,
	 * __global int* attempts,
	 * __global int* indices,
	 * __global int* offsets,
	 * __global uchar* output,
	 * __local int* buffer
	 * );
	 * */

	/* Get all constants */
	int         tuples = constants[0];
	int        _table_ = constants[1];
	int      __stash_x = constants[2];
	int      __stash_y = constants[3];
	int max_iterations = constants[4];
	int   _buffer_size = constants[5];

	int error = 0;
	/* Set constant arguments */
	error |= clSetKernelArg (kernel, 0, sizeof(int), (void *)          &tuples);
	error |= clSetKernelArg (kernel, 1, sizeof(int), (void *)         &_table_);
	error |= clSetKernelArg (kernel, 2, sizeof(int), (void *)       &__stash_x);
	error |= clSetKernelArg (kernel, 3, sizeof(int), (void *)       &__stash_y);
	error |= clSetKernelArg (kernel, 4, sizeof(int), (void *)  &max_iterations);
	/* Set I/O byte buffers */
	error |= clSetKernelArg (
		kernel,
		5,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		6,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		7,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		8,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[3]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		9,
		sizeof(cl_mem),
		(void *) &(context->kernelInput.inputs[4]->device_buffer));

	error |= clSetKernelArg (
		kernel,
		10,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[0]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		11,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[1]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		12,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[2]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		13,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[3]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		14,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[4]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		15,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[5]->device_buffer));
	error |= clSetKernelArg (
		kernel,
		16,
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[6]->device_buffer));

	/* Set local memory */
	error |= clSetKernelArg (kernel, 17, (size_t) _buffer_size, (void *) NULL);

	if (error != CL_SUCCESS) {
		fprintf(stderr, "opencl error (%d): %s\n", error, getErrorMessage(error));
		exit (1);
	}
	return;
}

void callback_writeInput (gpuContextP context,
	JNIEnv *env, jobject obj, int qid, int ndx, int offset) {

	(void) env;
	(void) obj;
	(void) qid;

	if (ndx == 0) {
		memcpy (context->kernelInput.inputs[ndx]->mapped_buffer, (void *) (input + offset),
			context->kernelInput.inputs[ndx]->size);
	} else
	if (ndx == 1) {
		memcpy (context->kernelInput.inputs[ndx]->mapped_buffer, (void *) (startPtrs),
			context->kernelInput.inputs[ndx]->size);
	} else
	if (ndx == 2) {
		memcpy (context->kernelInput.inputs[ndx]->mapped_buffer, (void *) (endPtrs),
			context->kernelInput.inputs[ndx]->size);
	} else
	if (ndx == 3) {
		memcpy (context->kernelInput.inputs[ndx]->mapped_buffer, (void *) (x),
			context->kernelInput.inputs[ndx]->size);
	} else
	if (ndx == 4) {
		memcpy (context->kernelInput.inputs[ndx]->mapped_buffer, (void *) (y),
			context->kernelInput.inputs[ndx]->size);
	} else {
		fprintf(stderr, "fatal error: invalid buffer index\n");
		exit (1);
	}
}

void callback_readOutput (gpuContextP context,
	JNIEnv *env, jobject obj, int qid, int ndx, int offset) {

	(void) env;
	(void) obj;
	(void) qid;

	if (ndx == 0) {
		printf("Copying `contents`...\n");
		memcpy ((void *) (contents + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else
	if (ndx == 1) {
		printf("Copying `stashed`...\n");
		memcpy ((void *) (stashed + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else
	if (ndx == 2) {
		printf("Copying `failed`...\n");
		memcpy ((void *) (failed + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else
	if (ndx == 3) {
		printf("Copying `attempts`...\n");
		memcpy ((void *) (attempts + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else
	if (ndx == 4) {
		printf("Copying `indices`...\n");
		memcpy ((void *) (indices + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else
	if (ndx == 5) {
		printf("Copying `offsets`...\n");
		memcpy ((void *) (offsets + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else
	if (ndx == 6) {
		printf("Copying `output`...\n");
		memcpy ((void *) (output + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else {
		fprintf(stderr, "fatal error: invalid buffer index\n");
		exit (1);
	}
}
