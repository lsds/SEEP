/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class uk_ac_imperial_lsds_seep_multi_TheGPU */

#ifndef _Included_uk_ac_imperial_lsds_seep_multi_TheGPU
#define _Included_uk_ac_imperial_lsds_seep_multi_TheGPU
#ifdef __cplusplus
extern "C" {
#endif
#undef uk_ac_imperial_lsds_seep_multi_TheGPU_maxQueries
#define uk_ac_imperial_lsds_seep_multi_TheGPU_maxQueries 5L
#undef uk_ac_imperial_lsds_seep_multi_TheGPU_maxBuffers
#define uk_ac_imperial_lsds_seep_multi_TheGPU_maxBuffers 10L
/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    init
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_init
  (JNIEnv *, jobject, jint);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    getQuery
 * Signature: (Ljava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_getQuery
  (JNIEnv *, jobject, jstring, jint, jint, jint);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setInput
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setInput
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setOutput
 * Signature: (IIIIIII)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setOutput
  (JNIEnv *, jobject, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    execute
 * Signature: (I[I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_execute
  (JNIEnv *, jobject, jint, jintArray, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    free
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_free
  (JNIEnv *, jobject);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setKernelDummy
 * Signature: (I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelDummy
  (JNIEnv *, jobject, jint, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setKernelProject
 * Signature: (I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelProject
  (JNIEnv *, jobject, jint, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setKernelSelect
 * Signature: (I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelSelect
  (JNIEnv *, jobject, jint, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setKernelReduce
 * Signature: (I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelReduce
  (JNIEnv *, jobject, jint, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setKernelAggregate
 * Signature: (I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelAggregate
  (JNIEnv *, jobject, jint, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setKernelThetaJoin
 * Signature: (I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelThetaJoin
  (JNIEnv *, jobject, jint, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    executePartialReduce
 * Signature: (I[I[I[J)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_executePartialReduce
  (JNIEnv *, jobject, jint, jintArray, jintArray, jlongArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setKernelPartialReduce
 * Signature: (I[I[J)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelPartialReduce
  (JNIEnv *, jobject, jint, jintArray, jlongArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setKernelAggregateIStream
 * Signature: (I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setKernelAggregateIStream
  (JNIEnv *, jobject, jint, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    allocateBuffer
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_allocateBuffer
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    getDirectByteBuffer
 * Signature: (I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_getDirectByteBuffer
  (JNIEnv *, jobject, jint);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setDirectInput
 * Signature: (IIII)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setDirectInput
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    setDirectOutput
 * Signature: (IIIIIIII)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_setDirectOutput
  (JNIEnv *, jobject, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    executeDirect
 * Signature: (I[I[I[I[I)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_executeDirect
  (JNIEnv *, jobject, jint, jintArray, jintArray, jintArray, jintArray);

/*
 * Class:     uk_ac_imperial_lsds_seep_multi_TheGPU
 * Method:    configurePartialReduce
 * Signature: (I[J)I
 */
JNIEXPORT jint JNICALL Java_uk_ac_imperial_lsds_seep_multi_TheGPU_configurePartialReduce
  (JNIEnv *, jobject, jint, jlongArray);

#ifdef __cplusplus
}
#endif
#endif
