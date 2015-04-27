#ifndef OPENCL_ERROR_CODE_H_
#define OPENCL_ERROR_CODE_H_

#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif

char *getErrorMessage (int error);

char *getCommandExecutionStatus (int status);

char *getCommandType (cl_command_type type);

#endif /* OPENCL_ERROR_CODE_H_ */
