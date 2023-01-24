/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_freedesktop_Wayland_Resource */

#ifndef _Included_org_freedesktop_Wayland_Resource
#define _Included_org_freedesktop_Wayland_Resource
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    postEvent
 * Signature: (I[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_postEvent
  (JNIEnv *, jobject, jint, jobjectArray);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    queueEvent
 * Signature: (I[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_queueEvent
  (JNIEnv *, jobject, jint, jobjectArray);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    postError
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_postError
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    postNoMemory
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_postNoMemory
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    setCallbacks
 * Signature: (Lorg/freedesktop/Wayland/Resource/Callbacks;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_setCallbacks
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_destroy
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    getId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_freedesktop_Wayland_00024Resource_getId
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    getClient
 * Signature: ()Lorg/freedesktop/Wayland/Client;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024Resource_getClient
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    getVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_freedesktop_Wayland_00024Resource_getVersion
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    setDestructor
 * Signature: (Lorg/freedesktop/Wayland/DestroyCallback;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_setDestructor
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    getWaylandClass
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_freedesktop_Wayland_00024Resource_getWaylandClass
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    addDestroyCallback
 * Signature: (Lorg/freedesktop/Wayland/DestroyCallback;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_addDestroyCallback
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    removeDestroyCallback
 * Signature: (Lorg/freedesktop/Wayland/DestroyCallback;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024Resource_removeDestroyCallback
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_freedesktop_Wayland_Resource
 * Method:    haveDestroyCallback
 * Signature: (Lorg/freedesktop/Wayland/DestroyCallback;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_freedesktop_Wayland_00024Resource_haveDestroyCallback
  (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif