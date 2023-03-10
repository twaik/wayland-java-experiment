/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_freedesktop_Wayland_EventLoop */

#ifndef _Included_org_freedesktop_Wayland_EventLoop
#define _Included_org_freedesktop_Wayland_EventLoop
#ifdef __cplusplus
extern "C" {
#endif
#undef org_freedesktop_Wayland_EventLoop_WL_EVENT_READABLE
#define org_freedesktop_Wayland_EventLoop_WL_EVENT_READABLE 1L
#undef org_freedesktop_Wayland_EventLoop_WL_EVENT_WRITABLE
#define org_freedesktop_Wayland_EventLoop_WL_EVENT_WRITABLE 2L
#undef org_freedesktop_Wayland_EventLoop_WL_EVENT_HANGUP
#define org_freedesktop_Wayland_EventLoop_WL_EVENT_HANGUP 4L
#undef org_freedesktop_Wayland_EventLoop_WL_EVENT_ERROR
#define org_freedesktop_Wayland_EventLoop_WL_EVENT_ERROR 8L
/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    create
 * Signature: ()Lorg/freedesktop/Wayland/EventLoop;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024EventLoop_create
  (JNIEnv *, jclass);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024EventLoop_destroy
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    addFd
 * Signature: (IILorg/freedesktop/Wayland/EventLoop/FdCallback;)Lorg/freedesktop/Wayland/EventLoop/EventSource;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024EventLoop_addFd
  (JNIEnv *, jobject, jint, jint, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    addTimer
 * Signature: (ILorg/freedesktop/Wayland/EventLoop/TimerCallback;)Lorg/freedesktop/Wayland/EventLoop/EventSource;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024EventLoop_addTimer
  (JNIEnv *, jobject, jint, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    addSignal
 * Signature: (ILorg/freedesktop/Wayland/EventLoop/SignalCallback;)Lorg/freedesktop/Wayland/EventLoop/EventSource;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024EventLoop_addSignal
  (JNIEnv *, jobject, jint, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    addIdle
 * Signature: (Lorg/freedesktop/Wayland/EventLoop/IdleCallback;)Lorg/freedesktop/Wayland/EventLoop/EventSource;
 */
JNIEXPORT jobject JNICALL Java_org_freedesktop_Wayland_00024EventLoop_addIdle
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    dispatch
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_org_freedesktop_Wayland_00024EventLoop_dispatch
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    dispatchIdle
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024EventLoop_dispatchIdle
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    getFd
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_freedesktop_Wayland_00024EventLoop_getFd
  (JNIEnv *, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    addDestroyCallback
 * Signature: (Lorg/freedesktop/Wayland/DestroyCallback;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024EventLoop_addDestroyCallback
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    removeDestroyCallback
 * Signature: (Lorg/freedesktop/Wayland/DestroyCallback;)V
 */
JNIEXPORT void JNICALL Java_org_freedesktop_Wayland_00024EventLoop_removeDestroyCallback
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_freedesktop_Wayland_EventLoop
 * Method:    haveDestroyCallback
 * Signature: (Lorg/freedesktop/Wayland/DestroyCallback;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_freedesktop_Wayland_00024EventLoop_haveDestroyCallback
  (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
