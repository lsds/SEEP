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
	int _1;
	int _2;
	int _3;
	int _4;
	int _5;
	int _6;
} input_tuple_t  __attribute__((aligned(1)));

typedef struct {
	long t;
	int _1;
	int _2;
	int _3;
	int _4;
	int _5;
	int _6;
} output_tuple_t  __attribute__((aligned(1)));

static unsigned char  *input = NULL;
static unsigned char *output = NULL;

static int *flags = NULL;
static int *offsets = NULL;

static int must_exit = 0;
static int sig_received;

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
	int _output_tuple_size =  32;
	size_t threadsPerGroup = 256;
	int tuplesPerThread = 1;
	/* int windows = 32; */

	int _input_size = size;
	int tuples = _input_size / _input_tuple_size;
	/* Configure #threads, #groups and threads group size */
	size_t threads = tuples / tuplesPerThread;
	/* int groups = threads / threadsPerGroup; */

	int _bundle = tuplesPerThread * threadsPerGroup;
	int bundles = tuplesPerThread;

	int _output_size = tuples * _output_tuple_size;

	/* Initialise input and output */

	input  = (unsigned char *) malloc ( _input_size);
	output = (unsigned char *) malloc (_output_size);

	int _flags_size = tuples * sizeof (int);
	flags = (int *) malloc (_flags_size);

	int _offsets_size = tuples * sizeof (int);
	offsets = (int *) malloc (_offsets_size);

	int i;
	int idx;
	for (i = 0; i < tuples; i++) {
		idx = i * sizeof(input_tuple_t);
		input_tuple_t *tpl = (input_tuple_t *) &input[idx];
		tpl->t  = 1L;
		tpl->_1 = 1;
		tpl->_2 = 1;
		tpl->_3 = 1;
		tpl->_4 = 1;
		tpl->_5 = 1;
		tpl->_6 = 1;
	}

	memset (output, 0, _output_size);

	memset (flags, 0, _flags_size);
	memset (offsets, 0, _offsets_size);

	gpu_init(1);
	int qid = gpu_getQuery (kernelcode, 2, 1, 3);
	printf("qid %d\n", qid);

	gpu_setInput  (qid, 0, _input_size);

	gpu_setOutput (qid, 0, _flags_size, 0);
	gpu_setOutput (qid, 1, _offsets_size, 0);
	gpu_setOutput (qid, 2, _output_size, 1);

	int args[5];
	args[0] = _input_size; /* #bytes */
	args[1] = tuples;
	args[2] = _bundle;
	args[3] = bundles;
	args[4] = sizeof(int) * threadsPerGroup;

	gpu_setKernel (qid, 0, "selectKernel",  &callback_setKernels, &args[0]);
	gpu_setKernel (qid, 1, "compactKernel", &callback_setKernels, &args[0]);
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
	while (! must_exit && runs++ < 100) {
	 	gpu_exec (qid, threads, threadsPerGroup, operator, NULL, NULL);
	}

	/* for (i = 0; i < tuples; i++) {
		printf("%6d: flag %3d offset %6d\n", i, flags[i], offsets[i]);
	} */

	if (memcmp(input, output, _input_size) != 0) {
		fprintf(stderr, "Error\n");
	} else {
		printf("OK\n");
	}

	printf ("Bye.\n");

	gpu_free();
	free (input);
	free (output);
	free (flags);
	free (offsets);
	free (kernelcode);
	exit (0);
}

void callback_setKernels (cl_kernel kernel, gpuContextP context, int *constants) {
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
	 * __global uchar *output,
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
	error |= clSetKernelArg (
		kernel,
		7, /* 8th argument */
		sizeof(cl_mem),
		(void *) &(context->kernelOutput.outputs[2]->device_buffer));
	/* Set local memory */
	error |= clSetKernelArg (kernel, 8, (size_t) buffer_size, (void *) NULL);

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
		printf("Copying `flags`...\n");
		memcpy ((void *) (flags + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else
	if (ndx == 1) {
		printf("Copying `offsets`...\n");
		memcpy ((void *) (offsets + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else
	if (ndx == 2) {
		printf("Copying `output`...\n");
		memcpy ((void *) (output + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
	} else {
		fprintf(stderr, "fatal error: invalid buffer index\n");
		exit (1);
	}
}
