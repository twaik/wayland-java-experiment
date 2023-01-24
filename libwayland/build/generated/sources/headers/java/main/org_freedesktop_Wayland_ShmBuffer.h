/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_freedesktop_Wayland_ShmBuffer */

#ifndef _Included_org_freedesktop_Wayland_ShmBuffer
#define _Included_org_freedesktop_Wayland_ShmBuffer
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_freedesktop_Wayland_ShmBuffer
 * Method:    getStride
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_freedesktop_Wayland_00024ShmBuffer_getStride
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_ShmBuffer
 * Method:    getData
 * Signature: ()Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024ShmBuffer_getData
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_ShmBuffer
 * Method:    getFormat
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_freedesktop_Wayland_00024ShmBuffer_getFormat
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_ShmBuffer
 * Method:    getWidth
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_freedesktop_Wayland_00024ShmBuffer_getWidth
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_ShmBuffer
 * Method:    getHeight
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_freedesktop_Wayland_00024ShmBuffer_getHeight
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_ShmBuffer
 * Method:    getResource
 * Signature: ()Lorg/freedesktop/Wayland/Resource;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024ShmBuffer_getResource
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_ShmBuffer
 * Method:    fromResource
 * Signature: (Lorg/freedesktop/Wayland/Resource;)Lorg/freedesktop/Wayland/ShmBuffer;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024ShmBuffer_fromResource
  (JNIEnv *, jclass, jobject);

#ifdef __cplusplus
}
#endif
#endif
