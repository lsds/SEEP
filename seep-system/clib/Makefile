CLASS_PATH = .
vpath %.class $(CLASS_PATH)

CC = gcc

CFL_BASE = -W -Wall -DWARNING -fpic

CFL_COMMON = $(CFL_BASE) -g

CFLAGS = $(CFL_COMMON)
CFLAGS += -I/usr/lib/jvm/java-1.7.0-openjdk-amd64/include
CFLAGS += -I/usr/lib/jvm/java-1.7.0-openjdk-amd64/include/linux
CFLAGS += -I/usr/include -D_GNU_SOURCE

# OpenCL
CFLAGS += -I/usr/local/cuda-6.5/include

LIBS = -lOpenCL -pthread

CLIB = -L/home/akolious/tests/saber-jni -lGPU

OBJS = GPU.o timer.o openclerrorcode.o inputbuffer.o outputbuffer.o gpucontext.o gpuquery.o

all: libGPU.so

libGPU.so: $(OBJS)
	$(CC) -shared -o libGPU.so $(OBJS) $(LIBS)

GPU.o: GPU.c GPU.h TheGPU.h timer.h openclerrorcode.h 
	$(CC) $(CFLAGS) -c $< -o $@

TheGPU.h:
	javah -classpath $(CLASS_PATH) TheGPU

timer.o: timer.c timer.h
openclerrorcode.o: openclerrorcode.c openclerrorcode.h
inputbuffer.o: inputbuffer.c inputbuffer.h
outputbuffer.o: outputbuffer.c outputbuffer.h
gpucontext.o: gpucontext.c gpucontext.h
gpuquery.o: gpuquery.c gpuquery.h

clean:
	rm -f *.o *.so
