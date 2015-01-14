CLASS_PATH = ../target/classes
vpath %.class $(CLASS_PATH)

CC = gcc

CFL_BASE = -W -Wall -DWARNING -fpic

CFL_COMMON = $(CFL_BASE) -g

CFLAGS = $(CFL_COMMON)
CFLAGS += -I/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.55.x86_64/include
CFLAGS += -I/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.55.x86_64/include/linux
CFLAGS += -I/usr/include

LIBS = -lOpenCL

OBJS = GPU.o timer.o openclerrorcode.o inputbuffer.o outputbuffer.o gpucontext.o gpuquery.o

all: libGPU.so

libGPU.so: $(OBJS)
	$(CC) -shared -o libGPU.so $(OBJS) $(LIBS)

GPU.o: GPU.c uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU.h timer.h openclerrorcode.h
	$(CC) $(CFLAGS) -c $< -o $@
	
uk_ac_imperial_lsds_streamsql_op_gpu_TheGPU.h:
	javah -classpath $(CLASS_PATH) uk.ac.imperial.lsds.streamsql.op.gpu.TheGPU
	
test: test.c libGPU.so
	$(CC) $(CFLAGS) test.c -o test $(LIBS) -L/mnt/data/cccad3/akolious/SEEP/seep-streamsql/clib -lGPU

test_project: test_project.c libGPU.so
	$(CC) $(CFLAGS) test_project.c -o test_project $(LIBS) -L/mnt/data/cccad3/akolious/SEEP/seep-streamsql/clib -lGPU

timer.o: timer.c timer.h

openclerrorcode.o: openclerrorcode.c openclerrorcode.h

inputbuffer.o: inputbuffer.c inputbuffer.h

outputbuffer.o: outputbuffer.c outputbuffer.h

gpucontext.o: gpucontext.c gpucontext.h

gpuquery.o: gpuquery.c gpuquery.h

clean:
	rm -f *.o *.so
