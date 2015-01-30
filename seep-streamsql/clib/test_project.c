#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <signal.h>

#include <unistd.h>

#include <fcntl.h>

#include <sys/stat.h>

#include "GPU.h"

#include "debug.h"

void callback_setKernels (cl_kernel, gpuContextP, int *);
void callback_writeInput (gpuContextP, JNIEnv *, jobject, int, int, int);
void callback_readOutput (gpuContextP, JNIEnv *, jobject, int, int, int);

static int  *input = NULL;
static int *output = NULL;

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
		fprintf(stderr, "./test_project [kernel filename]\n");
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
	int  _input_tuple_size = 32;
	int _output_tuple_size = 32;
	
	int _input_size = size;
	int tuples  = _input_size /  _input_tuple_size;
	int _output_size = tuples * _output_tuple_size;
	/* Configure #threads and threads group size */
	size_t threads = tuples;
	size_t threadsPerGroup = 128; /* 1048576 / 32 / 128 = 256 groups */
	/* Check for correctness */
	int groups = threads / threadsPerGroup;
	dbg ("[DBG] %zu threads %d groups\n", threads, groups);
	int tpg = tuples / groups;
	int bpg_in = tpg * _input_tuple_size;
	int bpg_out = tpg * _output_tuple_size;
	dbg ("[DBG] %d tuples/group %d bytes/group\n", tpg, bpg_in);
	/* Test correctness */
	int gid;
	for (gid = 0; gid < groups; gid++) {
		int input_idx = gid * threadsPerGroup * _input_tuple_size;
		int output_idx = gid * threadsPerGroup * _output_tuple_size;
		/* dbg ("[DBG] input @%7d output %7d\n", input_idx, output_idx); */
		if (
			(( input_idx +  bpg_in) >  _input_size) ||
			((output_idx + bpg_out) > _output_size)
		) {
			fprintf(stderr, "fatal error: invalid configuration\n");
			exit (1);
		}
	}
	
	/* Initialise input and output */
	input  = (int *) malloc ( _input_size);
	output = (int *) malloc (_output_size);
	int i;
	for (i = 0; i < tuples; i++)
		input[i] = 1;
	memset (output, 0, _output_size);
	gpu_init(1);
	int qid = gpu_getQuery (kernelcode, 1, 1, 1);
	printf("qid %d\n", qid);
	gpu_setInput  (qid, 0, _input_size);
	gpu_setOutput (qid, 0, _output_size, 1);
	int args[4];
	args[0] = tuples;
	args[1] = _input_size;
	args[2] = threadsPerGroup *  _input_tuple_size;
	args[3] = threadsPerGroup * _output_tuple_size;
	gpu_setKernel (qid, 0, "projectKernel", &callback_setKernels, &args[0]);
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
	while (! must_exit) {
	 	gpu_exec (qid, threads, threadsPerGroup, operator, NULL, NULL);
	}
	printf ("Bye.\n");
	gpu_free();
	free (input);
	free (output);
	free (kernelcode);
	exit (0);
}

void callback_setKernels (cl_kernel kernel, gpuContextP context, int *constants) {
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

void callback_writeInput (gpuContextP context,
	JNIEnv *env, jobject obj, int qid, int ndx, int offset) {

	(void) env;
	(void) obj;
	(void) qid;

	memcpy (context->kernelInput.inputs[ndx]->mapped_buffer, (void *) (input + offset),
			context->kernelInput.inputs[ndx]->size);
}

void callback_readOutput (gpuContextP context,
	JNIEnv *env, jobject obj, int qid, int ndx, int offset) {

	(void) env;
	(void) obj;
	(void) qid;

	memcpy ((void *) (output + offset), context->kernelOutput.outputs[ndx]->mapped_buffer,
			context->kernelOutput.outputs[ndx]->size);
}
