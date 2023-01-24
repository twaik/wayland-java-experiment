#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <jni.h>
#include <wayland-client.h>

extern "C"
JNIEXPORT jintArray JNICALL Java_DisplayTest_socketpair (JNIEnv* env, jclass) {
	int fds[2];
	socketpair(AF_UNIX, SOCK_STREAM, 0, fds);
	jintArray result;
	result = env->NewIntArray(2);
	if (result == NULL)
		return NULL; /* out of memory error thrown */
	env->SetIntArrayRegion(result, 0, 2, fds);
	return result;
}

extern "C"
JNIEXPORT void JNICALL Java_DisplayTest_close(JNIEnv* env, jclass, jint fd) {
	close(fd);
}

extern "C"
JNIEXPORT void JNICALL Java_DisplayTest_print(JNIEnv* env, jclass, jstring str) {
    const char *s = env->GetStringUTFChars(str, nullptr);
    dprintf(2, "%s\n", s);
    env->ReleaseStringUTFChars(str, s);
}

extern "C"
JNIEXPORT void JNICALL Java_DisplayTest_setenv(JNIEnv* env, jclass, jstring jvar, jstring jval, jboolean overwrite) {
    const char* var = env->GetStringUTFChars(jvar, nullptr);
    const char* val = env->GetStringUTFChars(jval, nullptr);
    setenv(var, val, overwrite);
    env->ReleaseStringUTFChars(jvar, var);
    env->ReleaseStringUTFChars(jval, val);
}