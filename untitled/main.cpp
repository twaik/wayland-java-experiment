#include <iostream>
#include <string>
#include <vector>
#include <jni.h>

using namespace std;

extern "C" JNIIMPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved);

int main()
{
    cout << "Hello World!" << endl;
    JavaVM *jvm;       /* denotes a Java VM */
    JNIEnv *env;       /* pointer to native method interface */
    std::string options[] = {
        "-Dfile.encoding=UTF-8",
        "-Duser.country=IL",
        "-Duser.language=en",
        "-Duser.variant",
        "-Djava.class.path=/home/twaik/libwayland/build/classes/java/main",
    };

    JavaVMInitArgs jvmArgs;
    std::vector<JavaVMOption> jvmOptions;

    for (const std::string &str : options) {
        JavaVMOption option;
        option.extraInfo = nullptr;
        option.optionString = (char *)str.c_str();
        jvmOptions.push_back(option);
    }

    jvmArgs.nOptions = jvmOptions.size();
    jvmArgs.options = &jvmOptions.front();
    jvmArgs.version = JNI_VERSION_1_6;
    jvmArgs.ignoreUnrecognized = JNI_FALSE;

    JNI_CreateJavaVM(&jvm, (void**)&env, &jvmArgs);
    JNI_OnLoad(jvm, nullptr);

    /* invoke the Main.test method using the JNI */
    jclass cls = env->FindClass("org.freedesktop.App");
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        jobject exc = env->ExceptionOccurred();
        jclass cls = env->GetObjectClass(exc);
        //jmethodID mid = env->GetMethodID(cls, "printStackTrace", "()V");
        //env->CallVoidMethod(exc, mid);
    }
    jmethodID mid = nullptr;
    if (cls != nullptr)
        mid = env->GetStaticMethodID(cls, "main", "(I)V");
    if (mid != nullptr)
        env->CallStaticVoidMethod(cls, mid, 100);
    /* We are done. */
    jvm->DestroyJavaVM();
    return 0;

}
