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

all: GPU.so

GPU.so: GPU.o timer.o openclerrorcode.o
	$(CC) -shared -o GPU.so GPU.o timer.o openclerrorcode.o $(LIBS)

GPU.o: GPU.c uk_ac_imperial_lsds_streamsql_op_gpu_GPU.h timer.h openclerrorcode.h
	$(CC) $(CFLAGS) -c $< -o $@

uk_ac_imperial_lsds_streamsql_op_gpu_GPU.h:
	javah -classpath $(CLASS_PATH) uk.ac.imperial.lsds.streamsql.op.gpu.GPU

timer.o: timer.c timer.h

openclerrorcode.o: openclerrorcode.c openclerrorcode.h

clean:
	rm -f *.o *.so