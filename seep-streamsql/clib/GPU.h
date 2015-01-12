#ifndef __GPU_H_
#define __GPU_H_

#include "gpucontext.h"
#include "openclerrorcode.h"

void gpu_init ();

void gpu_free ();

/* Set one context per query */
int gpu_getQuery (const char *, int, int, int);

int gpu_setInput (int, int, int);

int gpu_setOutput (int, int, int);

int gpu_setKernel (int, int, const char *, void (*callback)(cl_kernel, gpuContextP));

/* Execute task */
int gpu_exec (int, void *, void *, size_t, size_t);

#endif /* SEEP_GPU_H_ */
