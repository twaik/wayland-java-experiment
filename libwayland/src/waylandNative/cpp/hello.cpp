//#include <algorithm>
//#include <vector>
#define WL_HIDE_DEPRECATED
#include <string>
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <wayland-server.h>
#define WL_CLOSURE_MAX_ARGS 20
#pragma GCC diagnostic ignored "-Winvalid-offsetof"

#define D dprintf(2, "HERE! %d\n", __LINE__)

static thread_local JNIEnv* env;

/*
 * It is simple. Those wrappers get native pointer from Java
 * object (from it's `jlong peer` field) and call given function `f`.
 * There are two almost identical sets. One for simple functions and one more for
 * class member functions.
 * In class member function wrapper pointer to wayland object is available as `this`
 */
template<class F, F f, auto defaultValue> struct wrapper_impl;
template<class R, class A0, class... A, R(*f)(A0*, A...), auto defaultValue>
struct wrapper_impl<R(*)(A0*, A...), f, defaultValue> {
    // Simple wrapper. Function is invoked as is, being definied with 2 additional JNI arguments.
    static R wrap(JNIEnv* jenv, jobject obj, A0* instance, A... args) {
        env = jenv;
        A0* native = A0::fromJava(obj);
        if (native != nullptr)
            return f(instance, args...);
        return static_cast<R>(defaultValue);
    }
    // Same as simple wrap, but returns JNI_TRUE if return value is non-null.
    static jboolean wrapBoolean(JNIEnv* jenv, jobject obj, A0* instance, A... args) {
        env = jenv;
        A0* native = A0::fromJava(obj);
        if (native != nullptr)
            return !!(f(instance, args...));
        return defaultValue;
    }
    // Same as simple wrap, but returns JNI_FALSE if return value is non-null.
    static jboolean wrapInvertBoolean(JNIEnv* jenv, jobject obj, A0* instance, A... args) {
        env = jenv;
        A0* native = A0::fromJava(obj);
        if (native != nullptr)
            return (f(instance, args...) == 0);
        return defaultValue;
    }
    // Same as simple wrap but return value is ignored.
    static void wrapVoid(JNIEnv* jenv, jobject obj, A0* instance, A... args) {
        env = jenv;
        A0* native = A0::fromJava(obj);
        if (native != nullptr)
            f(instance, args...);
    }
};
template<class R, class C, class... A, R(C::*f)(A...), auto defaultValue>
struct wrapper_impl<R(C::*)(A...), f, defaultValue> {
// I do not need to change results of functions I wrote so I will use them as is.
    static R wrap(JNIEnv* jenv, jobject obj, A... args) {
        env = jenv;
        C* native = C::fromJava(obj);
        if (native != nullptr)
            return (native->*f)(args...);
        return static_cast<R>(defaultValue);
    }
};
template<auto f, auto defaultValue = 0>
void* wrap = (void*)&wrapper_impl<decltype(f), f, defaultValue>::wrap;
template<auto f, auto defaultValue = 0>
void* wrapBoolean = (void*)&wrapper_impl<decltype(f), f, defaultValue>::wrapBoolean;
template<auto f, auto defaultValue = 0>
void* wrapInvertBoolean = (void*)&wrapper_impl<decltype(f), f, defaultValue>::wrapInvertBoolean;
template<auto f, auto defaultValue = 0>
void* wrapVoid = (void*)&wrapper_impl<decltype(f), f, defaultValue>::wrapVoid;

void explain(jobject obj, std::string msg) {
    jclass cls = env->GetObjectClass(obj);
    jmethodID toString = env->GetMethodID(cls, "toString", "()Ljava/lang/String;");
    jstring jstr = (jstring) env->CallObjectMethod(obj, toString);
    const char *str = env->GetStringUTFChars(jstr, nullptr);
    printf("explain: %s | %s\n", msg.c_str(), str);
    env->ReleaseStringUTFChars(jstr, str);
}

#define FindClassOrReturn(var, name) \
    var = env->FindClass(name); \
        if (var == nullptr) \
            return false; \
        else \
            var = static_cast<jclass>(env->NewGlobalRef(static_cast<jobject>(var)));
#define GetMethodIDOrReturn(var, cls, name, signature) \
    var = env->GetMethodID(env->FindClass(cls), name, signature); \
       if (var == nullptr) \
            return false;
#define GetFieldIDOrReturn(var, cls, name, signature) \
    var = env->GetFieldID(env->FindClass(cls), name, signature); \
    if (var == nullptr) \
        return false;

static inline void ThrowNew(std::string msg) {
    env->ThrowNew(env->FindClass("java/lang/RuntimeException"), msg.c_str());
}

struct wl_destroyable_listener: wl_listener {
    using wl_destroy_func_t = void (*)(wl_destroyable_listener* listener);
    wl_destroy_func_t const destroy;
    wl_destroyable_listener(wl_notify_func_t notify, wl_destroy_func_t destroy);
};

wl_destroyable_listener::wl_destroyable_listener(wl_notify_func_t notify, wl_destroy_func_t destroy):
        wl_listener{{}, notify}, destroy(destroy) {}

struct JavaCallback: wl_destroyable_listener {
    typedef jobject (*getInstanceMethod)(void*);
    const jobject callback;
    const jmethodID method;
    const getInstanceMethod getInstance;
    JavaCallback(jobject callback, jmethodID method, getInstanceMethod getInstance);
    static void invoke(struct wl_listener *listener, void *data);
    static void destroy(wl_destroyable_listener* l) { delete (JavaCallback*) l; }
    ~JavaCallback();

    static jmethodID createCallbackMethod;
    static jmethodID destroyCallbackMethod;
    static jmethodID consumerAcceptMethod;
	static bool registerNatives();
};

JavaCallback::JavaCallback(jobject callback, jmethodID method, getInstanceMethod getInstance):
    callback(env->NewGlobalRef(callback)), method(method), getInstance(getInstance),
    wl_destroyable_listener{&invoke, &destroy}  {}

void JavaCallback::invoke(struct wl_listener *listener, void *data) {
    auto cb = (JavaCallback*) listener;
    env->CallVoidMethod(cb->callback, cb->method, cb->getInstance(data));
}

JavaCallback::~JavaCallback() {
    env->DeleteGlobalRef(callback);
}

jmethodID JavaCallback::createCallbackMethod = nullptr;
jmethodID JavaCallback::destroyCallbackMethod = nullptr;
jmethodID JavaCallback::consumerAcceptMethod;

bool JavaCallback::registerNatives() {
    GetMethodIDOrReturn(createCallbackMethod, "org/freedesktop/Wayland$CreateCallback", "invoke", "(Ljava/lang/Object;)V");
    GetMethodIDOrReturn(destroyCallbackMethod, "org/freedesktop/Wayland$DestroyCallback", "invoke", "(Ljava/lang/Object;)V");
    GetMethodIDOrReturn(consumerAcceptMethod, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V");
    return true;
}

// Non-static data fields are not allowed in this class. Place them in Data struct.
struct wl_display {
    struct Data: wl_listener {
        wl_display* const display;
        jobject const instance;
        wl_signal destroySignal;
        wl_signal createClientSignal;
        JavaCallback* globalFilterCallback = nullptr;
        wl_listener clientCreatedListener;
        Data(wl_display* display, jobject instance);
        static void clientCreated(wl_listener *l, void *client);
        ~Data();

        static void destroy(wl_listener *l, void *d){ D;delete (Data*) l; }
    };

    inline Data* getData();
    static inline jobject getJavaInstance(void* display);
    static inline wl_display* fromJava(jobject instance);

	static jobject create();
	inline jobject getEventLoop();
	inline void addDestroyCallback(jobject callback);
	inline void removeDestroyCallback(jobject callback);
	inline void addClientCreatedCallback(jobject callback);
	inline void removeClientCreatedCallback(jobject callback);
	inline void setGlobalFilterCallback(jobject callback);
	inline jobject createClient(jint fd);
	inline void forEachClient(jobject iterator);
	inline void bind(struct wl_client *client, void *data, uint32_t version, uint32_t id);
    inline jobject createGlobal0(jobject iface, jint version, jobject callback);
    inline jobject createGlobal1(jobject iface, jint version);
    inline jobject createGlobal2(jobject iface, jobject callback);
    inline jobject createGlobal3(jobject iface);

	static jclass clazz;
	static jmethodID constructor;
	static jmethodID globalFilterMethod;
	static jfieldID peer;

	static inline bool registerNatives();
};

// Non-static data fields are not allowed in this class. Place them in Data struct.
struct wl_client {
    struct Data: wl_listener {
        wl_client* const client;
        jobject const instance;
        wl_signal destroySignal;
        wl_signal createResourceSignal;
        wl_listener resourceCreatedListener;
        Data(wl_client* client, jobject instance);
        static void resourceCreated(wl_listener *l, void *resource);
        ~Data();

        static void destroy(wl_listener *l, void *d){ delete (Data*) l; }
    };

    inline Data* getData();
    static inline jobject getJavaInstance(void* client);
    static inline wl_client* fromJava(jobject instance);

    inline jint getPid();
    inline jint getUid();
    inline jint getGid();
    inline void addDestroyCallback(jobject callback);
    inline void removeDestroyCallback(jobject callback);
    inline jobject getObject(jint id);
    inline void postImplementationError(jstring msg);
    inline void addResourceCreatedCallback(jobject callback);
    inline void removeResourceCreatedCallback(jobject callback);
    inline void forEachResource(jobject iterator);
    inline jobject getDisplay();

	static jclass clazz;
	static jmethodID constructor;
	static jfieldID peer;

	static inline bool registerNatives();
};

// Non-static data fields are not allowed in this class. Place them in Data struct.
struct wl_event_loop {
    struct Data: wl_listener {
        wl_event_loop* const loop;
        jobject const instance;
        wl_signal destroySignal;
        // all callbacks added with addFd, addTimer, addSignal and addIdle are stored here
        // and destroyed during wl_event_loop destroy sequence...
        wl_list sources;
        Data(wl_event_loop* client, jobject instance);
        ~Data();

        static void destroy(wl_listener *l, void *d){ delete (Data*) l; }
    };

    inline Data* getData();
    static inline jobject getJavaInstance(void* loop);
    static inline wl_event_loop* fromJava(jobject instance);

    static inline jobject create();
    inline jobject addFd(jint fd, jint mask, jobject callback);
    inline jobject addTimer(jint msDelay, jobject callback);
    inline jobject addSignal(jint signalNumber, jobject callback);
    inline jobject addIdle(jobject callback);
    inline void addDestroyCallback(jobject callback);
    inline void removeDestroyCallback(jobject callback);

	static jclass clazz;
	static jmethodID constructor;
	static jfieldID peer;

	static inline bool registerNatives();
};

// This class is special. wl_event_source does not have *set_user_data or *_add_destroy_listener
// so we can not store any data there. But we can store pointer to instance of this class in `peer`
// field of Java representation class.
struct wl_event_source {
    wl_event_loop* const loop;
    wl_event_source* const source;
    jobject const instance;
    wl_list link;

    inline wl_event_source* getData();
    static inline jobject getJavaInstance(void* source);
    static inline wl_event_source* fromJava(jobject instance);

    wl_event_source(wl_event_loop* loop, wl_event_source* source);
    inline void remove();
    inline void check();
    inline jboolean fdUpdate(jint mask);
    inline jboolean timerUpdate(jint msDelay);

    static int fdCallback(int fd, uint32_t mask, void *data);
    static int timerCallback(void *data);
    static int signalCallback(int signalNumber, void *data);
    static void idleCallback(void *data);

	static jclass clazz;
	static jmethodID constructor;
	static jmethodID fdCallbackMethod;
	static jmethodID timerCallbackMethod;
	static jmethodID signalCallbackMethod;
	static jmethodID idleCallbackMethod;
	static jfieldID peer;

	static inline bool registerNatives();
};

// Non-static data fields are not allowed in this class. Place them in Data struct.
// This class is special. wl_global does not have *_add_destroy_listener, but have *_set_user_data
// so we can store data there and place destroy listener in display's list.
struct wl_global {
    struct Data: wl_listener {
        wl_global* const global;
        jobject const instance;
        Data(wl_global* global, jobject instance);
        ~Data();

        static void destroy(wl_listener *l, void *d){ delete (Data*) l; }
    };

    inline Data* getData();
    static inline jobject getJavaInstance(void* global);
    static inline wl_global* fromJava(jobject instance);

    inline void destroy();
    inline jobject getDisplay();
    inline jobject getInterface();

	static jclass clazz;
	static jmethodID constructor;
	static jfieldID peer;

	static inline bool registerNatives();
};

// Non-static data fields are not allowed in this class. Place them in Data struct.
struct wl_resource {
    struct Data: wl_listener {
        wl_resource* const res;
        jobject const instance;
        wl_signal destroySignal;
        JavaCallback* destructor = nullptr;
        jobject implementation;
        Data(wl_resource* res, jobject instance);
        ~Data();

        static void destruct(wl_resource *res){ delete static_cast<wl_resource*>(res)->getData(); }
        static void destroy(wl_listener *l, void *d){ delete (Data*) l; }
    };

    inline Data* getData();
    static inline jobject getJavaInstance(void* res);
    static inline wl_resource* fromJava(jobject instance);

	inline void sendEvent(jint opcode, jobjectArray jargs,
            void (*sendFunc)(struct wl_resource*,uint32_t,wl_argument*));
    inline void postEvent(jint opcode, jobjectArray jargs);
    inline void queueEvent(jint opcode, jobjectArray jargs);
    inline void postError(jint code, jstring msg);
    //inline void postNoMemory();
    static int dispatch(const void *implementation, void *target, uint32_t opcode, const struct wl_message *message,
                       				   union wl_argument *args);
    inline void setCallbacks(jobject implementation);
    //inline void destroy();
    //inline jint getId();
    inline jobject getClient();
    //inline jint getVersion();
    inline void setDestructor(jobject callback);
    inline jstring getWaylandClass();
    inline void addDestroyCallback(jobject callback);
    inline void removeDestroyCallback(jobject callback);

	static jclass clazz;
	static jclass implementationExceptionClass;
	static jclass integerClass;
	static jclass fixedClass;
	static jclass stringClass;
	static jclass arrayClass;
	static jmethodID constructor;
	static jmethodID intValueMethod;
	static jmethodID fixedValueMethod;
	static jmethodID fixedConstructorMethod;
	static jfieldID implementationExceptionCodeField;
	static jfieldID implementationExceptionMessageField;
	static jfieldID peer;

	static bool registerNatives();

};

// wl_interface is defined in headers so we can not simply use this id so we will use wl_java_interface
struct wl_java_interface: private wl_interface {
	const jobject instance;
	const jclass callbacksClass;
	const jmethodID* javaMethods;
	wl_java_interface(JNIEnv* env, const char* name, int version, int methodCount, const struct wl_message *methods, int eventCount,
	            const struct wl_message *events, jclass callbacksClass, jobject instance, jmethodID* javaMethods):
	            wl_interface{name, version, methodCount, methods, eventCount, events}, callbacksClass(callbacksClass),
	            instance(instance), javaMethods(javaMethods) {}

    static inline jobject getJavaInstance(void* res);
    static inline wl_resource* fromJava(jobject instance);
    static inline void processMessageArray(jobjectArray jmessages, jsize messageCount, const struct wl_message *messages);
	static inline void create(jobject obj, jstring jname, jint version, jobjectArray jrequests,
	                            jobjectArray jevents, jclass callbacksClass);

	static jclass ifaceClass;
	static jclass messageClass;
	static jfieldID messageNameField;
	static jfieldID messageSignatureField;
	static jfieldID messageTypesField;
	static jfieldID peerField;

	static bool registerNatives();
};

// wl_display methods

wl_display::Data::Data(wl_display* display, jobject instance):
display(display), instance(instance), wl_listener{{}, &destroy},
clientCreatedListener{{}, &clientCreated} {
    wl_signal_init(&destroySignal);
    wl_signal_init(&createClientSignal);
    wl_display_add_destroy_listener(display, this);
    wl_display_add_client_created_listener(display, &clientCreatedListener);

    // TODO: Here we should create Java instance for event loop accessible via wl_display_get_event_loop()
}

void wl_display::Data::clientCreated(wl_listener *l, void *client) {
    Data* data = wl_container_of(l, data, clientCreatedListener);
    JavaCallback* cb;

    // TODO: Call wl_client::createJavaInstance

    wl_list_for_each(cb, &data->createClientSignal.listener_list, link)
        cb->notify(cb, client);
}

wl_display::Data::~Data() {D;
    env->SetLongField(instance, peer, 0);D;
    wl_destroyable_listener *l;D;
    struct wl_list *pos;D;
    while (!wl_list_empty(&destroySignal.listener_list)) {
        pos = destroySignal.listener_list.next;
        l = wl_container_of(pos, l, link);
        wl_list_remove(pos);
        l->notify(l, display);
        l->destroy(l);
    };D;
    while (!wl_list_empty(&createClientSignal.listener_list)) {
        pos = createClientSignal.listener_list.next;
        l = wl_container_of(pos, l, link);
        wl_list_remove(pos);
        l->destroy(l);
    };D;
    if (globalFilterCallback != nullptr)
        globalFilterCallback->destroy(globalFilterCallback);
D;
    env->DeleteGlobalRef(instance);D;
}

wl_display::Data* wl_display::getData() {
    return (Data*) wl_display_get_destroy_listener(this, Data::destroy);
}

jobject wl_display::getJavaInstance(void* display) {
    return static_cast<wl_display*>(display)->getData()->instance;
}

wl_display* wl_display::fromJava(jobject instance) {
    return (wl_display*) env->GetLongField(instance, peer);
}

jobject wl_display::create() {
	wl_display *display = wl_display_create();
	jobject newInstance = env->NewObject(clazz, constructor);
	env->SetLongField(newInstance, peer, (jlong) display);
    new Data(display, newInstance);
    return newInstance;
}

jobject wl_display::getEventLoop() {
    // TODO: implement this
    return nullptr;
}

void wl_display::addDestroyCallback(jobject callback) {
    removeDestroyCallback(callback);
    wl_signal_add(&getData()->destroySignal,
        new JavaCallback(callback, JavaCallback::destroyCallbackMethod, &getJavaInstance));
}

void wl_display::removeDestroyCallback(jobject callback) {
    JavaCallback *c, *d;
	wl_list_for_each_safe(c, d, &getData()->destroySignal.listener_list, link) {
		if (env->IsSameObject(c->callback, callback)) {
		    wl_list_remove(&c->link);
		    c->destroy(c);
		}
	}
}

void wl_display::addClientCreatedCallback(jobject callback){
    removeDestroyCallback(callback);
    wl_signal_add(&getData()->createClientSignal,
        new JavaCallback(callback, JavaCallback::createCallbackMethod, &wl_client::getJavaInstance));
}

void wl_display::removeClientCreatedCallback(jobject callback) {
    JavaCallback *c;
	wl_list_for_each(c, &getData()->createClientSignal.listener_list, link)
		if (env->IsSameObject(c->callback, callback)) {
		    wl_list_remove(&c->link);
		    c->destroy(c);
		}
}

void wl_display::setGlobalFilterCallback(jobject callback) {
    Data* data = getData();
    if (data->globalFilterCallback != nullptr) {
        data->globalFilterCallback->destroy(data->globalFilterCallback);
        data->globalFilterCallback = nullptr;
    }
    if (callback != nullptr)
        data->globalFilterCallback =
            new JavaCallback(callback, globalFilterMethod, &wl_global::getJavaInstance);
}

jobject wl_display::createClient(jint fd) {
    return wl_client::getJavaInstance(wl_client_create(getData()->display, fd));
}

void wl_display::forEachClient(jobject iterator) {
    struct wl_list *list = wl_display_get_client_list(getData()->display);
    wl_client* client;
    wl_client_for_each(client, list)
        env->CallVoidMethod(iterator, JavaCallback::consumerAcceptMethod, wl_client::getJavaInstance(client));
}

void wl_display::bind(struct wl_client *client, void *data, uint32_t version, uint32_t id) {
    // TODO: implement this
}

jobject wl_display::createGlobal0(jobject iface, jint version, jobject callback) {
    // TODO: implement this
    return nullptr;
}

jobject wl_display::createGlobal1(jobject iface, jint version) {
    // TODO: implement this
    return nullptr;
}

jobject wl_display::createGlobal2(jobject iface, jobject callback) {
    // TODO: implement this
    return nullptr;
}

jobject wl_display::createGlobal3(jobject iface) {
    // TODO: implement this
    return nullptr;
}

jclass wl_display::clazz = nullptr;
jmethodID wl_display::constructor = nullptr;
jmethodID wl_display::globalFilterMethod = nullptr;
jfieldID wl_display::peer = nullptr;

bool wl_display::registerNatives() {
    FindClassOrReturn(clazz, "org/freedesktop/Wayland$Display");
    GetMethodIDOrReturn(constructor, "org/freedesktop/Wayland$Display", "<init>", "()V");
    GetMethodIDOrReturn(globalFilterMethod, "org/freedesktop/Wayland$Global$FilterCallback", "filter", "(Lorg/freedesktop/Wayland$Client;Lorg/freedesktop/Wayland$Global;)Z");
    GetFieldIDOrReturn(peer, "org/freedesktop/Wayland$Display", "peer", "J");

	static JNINativeMethod methods[] = {
	#define m(name, signature, func) { (char*) #name, (char*) signature, (void*) func }
	    m(create, "()Lorg/freedesktop/Wayland$Display;", &create),
	    m(destroy, "()V", wrap<wl_display_destroy>),
	    m(getEventLoop, "()Lorg/freedesktop/Wayland$EventLoop;", wrap<&wl_display::getEventLoop>),
	    m(addSocket, "(Ljava/lang/String;)Z", wrapInvertBoolean<wl_display_add_socket>),
	    m(addSocketAuto, "()V", wrapVoid<wl_display_add_socket_auto>),
	    m(addSocketFd, "(I)Z", wrapInvertBoolean<wl_display_add_socket_fd>),
	    m(terminate, "()V", wrap<wl_display_terminate>),
	    m(run, "()V", wrap<wl_display_run>),
	    m(destroyClients, "()V", wrap<wl_display_destroy_clients>),
	    m(getSerial, "()I", wrap<wl_display_get_serial>),
	    m(nextSerial, "()I", wrap<wl_display_next_serial>),
	    m(addDestroyCallback, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_display::addDestroyCallback>),
	    m(removeDestroyCallback, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_display::removeDestroyCallback>),
	    m(addClientCreatedCallback, "(Lorg/freedesktop/Wayland$CreateCallback;)V", wrap<&wl_display::addClientCreatedCallback>),
	    m(removeClientCreatedCallback, "(Lorg/freedesktop/Wayland$CreateCallback;)V", wrap<&wl_display::removeClientCreatedCallback>),
	    m(setGlobalFilterCallback, "(Lorg/freedesktop/Wayland$Global$FilterCallback;)V", wrap<&wl_display::setGlobalFilterCallback>),
	    m(initShm, "()I", wrapInvertBoolean<wl_display_init_shm>),
	    m(addShmFormat, "(I)Z", wrapBoolean<wl_display_add_shm_format>),
	    m(createClient, "(I)Lorg/freedesktop/Wayland$Client;", wrap<&wl_display::createClient>),
	    m(forEachClient, "(Ljava/util/function/Consumer;)V", wrap<&wl_display::forEachClient>),
	    m(createGlobal, "(Lorg/freedesktop/Wayland$Interface;ILorg/freedesktop/Wayland$Global$BindCallback;)Lorg/freedesktop/Wayland$Global;", wrap<&wl_display::createGlobal0>),
	    m(createGlobal, "(Lorg/freedesktop/Wayland$Interface;I)Lorg/freedesktop/Wayland$Global;", wrap<&wl_display::createGlobal1>),
	    m(createGlobal, "(Lorg/freedesktop/Wayland$Interface;Lorg/freedesktop/Wayland$Global$BindCallback;)Lorg/freedesktop/Wayland$Global;", wrap<&wl_display::createGlobal2>),
	    m(createGlobal, "(Lorg/freedesktop/Wayland$Interface;)Lorg/freedesktop/Wayland$Global;", wrap<&wl_display::createGlobal3>)
	    #undef m
    };

	return (0 == env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod)));
}

// wl_client methods

wl_client::Data::Data(wl_client* client, jobject instance):
client(client), instance(instance), wl_listener{{}, &destroy},
resourceCreatedListener{{}, &resourceCreated} {
    wl_signal_init(&destroySignal);
    wl_signal_init(&createResourceSignal);
    wl_client_add_destroy_listener(client, this);
    wl_client_add_resource_created_listener(client, &resourceCreatedListener);
}

void wl_client::Data::resourceCreated(wl_listener *l, void *client) {
    Data* data = wl_container_of(l, data, resourceCreatedListener);
    JavaCallback* cb;

    // TODO: Call wl_client::createJavaInstance

    wl_list_for_each(cb, &data->createResourceSignal.listener_list, link)
        cb->notify(cb, client);
}

wl_client::Data::~Data() {
    env->SetLongField(instance, peer, 0);
    wl_destroyable_listener *l;
    struct wl_list *pos;
    while (!wl_list_empty(&destroySignal.listener_list)) {
        pos = destroySignal.listener_list.next;
        l = wl_container_of(pos, l, link);
        wl_list_remove(pos);
        l->notify(l, client);
        l->destroy(l);
    }
    while (!wl_list_empty(&createResourceSignal.listener_list)) {
        pos = createResourceSignal.listener_list.next;
        l = wl_container_of(pos, l, link);
        wl_list_remove(pos);
        l->destroy(l);
    }

    env->DeleteGlobalRef(instance);
}

wl_client::Data* wl_client::getData() {
    return (Data*) wl_client_get_destroy_listener(this, Data::destroy);
}

jobject wl_client::getJavaInstance(void* client) {
    return static_cast<wl_client*>(client)->getData()->instance;
}

wl_client* wl_client::fromJava(jobject instance) {
    return (wl_client*) env->GetLongField(instance, peer);
}

jint wl_client::getPid() {
    pid_t pid = 0;
    wl_client_get_credentials(getData()->client, &pid, nullptr, nullptr);
    return pid;
}

jint wl_client::getUid() {
    uid_t uid = 0;
    wl_client_get_credentials(getData()->client, nullptr, &uid, nullptr);
    return uid;
}

jint wl_client::getGid() {
    gid_t gid = 0;
    wl_client_get_credentials(getData()->client, nullptr, nullptr, &gid);
    return gid;
}

void wl_client::addDestroyCallback(jobject callback) {
    removeDestroyCallback(callback);
    wl_signal_add(&getData()->destroySignal,
        new JavaCallback(callback, JavaCallback::destroyCallbackMethod, &getJavaInstance));
}

void wl_client::removeDestroyCallback(jobject callback) {
    JavaCallback *c;
	wl_list_for_each(c, &getData()->destroySignal.listener_list, link)
		if (env->IsSameObject(c->callback, callback)) {
		    wl_list_remove(&c->link);
		    c->destroy(c);
		}
}

jobject wl_client::getObject(jint id) {
    return wl_resource::getJavaInstance(wl_client_get_object(getData()->client, id));
}

void wl_client::postImplementationError(jstring jmsg) {
    const char* msg = env->GetStringUTFChars(jmsg, nullptr);
    wl_client_post_implementation_error(getData()->client, "%s", msg);
    env->ReleaseStringUTFChars(jmsg, msg);
}

void wl_client::addResourceCreatedCallback(jobject callback) {
    removeDestroyCallback(callback);
    wl_signal_add(&getData()->createResourceSignal,
        new JavaCallback(callback, JavaCallback::createCallbackMethod, &wl_client::getJavaInstance));
}

void wl_client::removeResourceCreatedCallback(jobject callback) {
    JavaCallback *c;
	wl_list_for_each(c, &getData()->createResourceSignal.listener_list, link)
		if (env->IsSameObject(c->callback, callback)) {
		    wl_list_remove(&c->link);
		    c->destroy(c);
		}
}

void wl_client::forEachResource(jobject iterator) {
    wl_client_for_each_resource(getData()->client, +[](wl_resource *res, void* iterator) {
        env->CallVoidMethod((jobject) iterator, JavaCallback::consumerAcceptMethod, wl_resource::getJavaInstance(res));
        return WL_ITERATOR_CONTINUE;
    }, iterator);
}

jobject wl_client::getDisplay() {
    return wl_display::getJavaInstance(wl_client_get_display(getData()->client));
}

jclass wl_client::clazz = nullptr;
jmethodID wl_client::constructor = nullptr;
jfieldID wl_client::peer = nullptr;

bool wl_client::registerNatives() {
    FindClassOrReturn(clazz, "org/freedesktop/Wayland$Client");
    GetMethodIDOrReturn(constructor, "org/freedesktop/Wayland$Client", "<init>", "()V");
    GetFieldIDOrReturn(peer, "org/freedesktop/Wayland$Client", "peer", "J");

	static JNINativeMethod methods[] = {
	#define m(name, signature, func) { (char*) #name, (char*) signature, (void*) func }
	    m(destroy, "()V", wrap<wl_client_destroy>),
	    m(flush, "()V", wrap<wl_client_flush>),
	    m(getPid, "()I", wrap<&wl_client::getPid>),
	    m(getUid, "()I", wrap<&wl_client::getUid>),
	    m(getGid, "()I", wrap<&wl_client::getGid>),
	    m(getFd, "()I", wrap<wl_client_get_fd>),
	    m(addDestroyCallback, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_client::addDestroyCallback>),
	    m(removeDestroyCallback, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_client::removeDestroyCallback>),
	    m(getObject, "(I)Lorg/freedesktop/Wayland$Resource;", wrap<&wl_client::getObject>),
	    m(postNoMemory, "()V", wrap<wl_client_post_no_memory>),
	    m(postImplementationError, "(Ljava/lang/String;)V", wrap<&wl_client::postImplementationError>),
	    m(addResourceCreatedCallback, "(Lorg/freedesktop/Wayland$CreateCallback;)V", wrap<&wl_client::addResourceCreatedCallback>),
	    m(removeResourceCreatedCallback, "(Lorg/freedesktop/Wayland$CreateCallback;)V", wrap<&wl_client::removeResourceCreatedCallback>),
	    m(forEachResource, "(Ljava/util/function/Consumer;)V", wrap<&wl_client::forEachResource>),
	    m(getDisplay, "()Lorg/freedesktop/Wayland$Display;", wrap<&wl_client::getDisplay>)
	    #undef m
    };

	return (0 == env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod)));
}

// wl_event_loop methods

wl_event_loop::Data::Data(wl_event_loop* loop, jobject instance):
loop(loop), instance(instance), wl_listener{{}, &destroy} {
    wl_signal_init(&destroySignal);
    wl_list_init(&sources);
    wl_event_loop_add_destroy_listener(loop, this);
}

wl_event_loop::Data::~Data() {
    env->SetLongField(instance, peer, 0);
    wl_destroyable_listener *l;
    struct wl_list *pos;
    while (!wl_list_empty(&destroySignal.listener_list)) {
        pos = destroySignal.listener_list.next;
        l = wl_container_of(pos, l, link);
        wl_list_remove(pos);
        l->notify(l, loop);
        l->destroy(l);
    }
    wl_event_source *s;
    while (!wl_list_empty(&sources)) {
        pos = sources.next;
        s = wl_container_of(pos, s, link);
        s->remove();
    }

    env->DeleteGlobalRef(instance);
}

wl_event_loop::Data* wl_event_loop::getData() {
    return (Data*) wl_event_loop_get_destroy_listener(this, Data::destroy);
}

jobject wl_event_loop::getJavaInstance(void* event_loop) {
    return static_cast<wl_event_loop*>(event_loop)->getData()->instance;
}

wl_event_loop* wl_event_loop::fromJava(jobject instance) {
    return (wl_event_loop*) env->GetLongField(instance, peer);
}

jobject wl_event_loop::create() {
	wl_event_loop *loop = wl_event_loop_create();
	jobject newInstance = env->NewObject(clazz, constructor);
	env->SetLongField(newInstance, peer, (jlong) loop);
    new Data(loop, newInstance);
    return newInstance;
}

jobject wl_event_loop::addFd(jint fd, jint mask, jobject javaCallback) {
    Data* data = getData();
    JavaCallback *callback = new JavaCallback(javaCallback, nullptr, nullptr);
    wl_event_loop_add_destroy_listener(data->loop, callback);

    return (new wl_event_source(data->loop,
        wl_event_loop_add_fd(data->loop, fd, mask, wl_event_source::fdCallback, callback)))->instance;
}

jobject wl_event_loop::addTimer(jint msDelay, jobject javaCallback) {
    Data* data = getData();
    JavaCallback *callback = new JavaCallback(javaCallback, nullptr, nullptr);
    wl_event_loop_add_destroy_listener(data->loop, callback);

    wl_event_source *src = wl_event_loop_add_timer(data->loop, wl_event_source::timerCallback, callback);
    wl_event_source_timer_update(src, msDelay);
    return (new wl_event_source(data->loop, src))->instance;
}

jobject wl_event_loop::addSignal(jint signalNumber, jobject javaCallback) {
    Data* data = getData();
    JavaCallback *callback = new JavaCallback(javaCallback, nullptr, nullptr);
    wl_event_loop_add_destroy_listener(data->loop, callback);

    return (new wl_event_source(data->loop,
        wl_event_loop_add_signal(data->loop, signalNumber, wl_event_source::signalCallback, callback)))->instance;
}

jobject wl_event_loop::addIdle(jobject javaCallback) {
    Data* data = getData();
    JavaCallback *callback = new JavaCallback(javaCallback, nullptr, nullptr);
    wl_event_loop_add_destroy_listener(data->loop, callback);

    return (new wl_event_source(data->loop,
        wl_event_loop_add_idle(data->loop, wl_event_source::idleCallback, callback)))->instance;
}

void wl_event_loop::addDestroyCallback(jobject callback) {
    removeDestroyCallback(callback);
    wl_signal_add(&getData()->destroySignal,
        new JavaCallback(callback, JavaCallback::destroyCallbackMethod, &getJavaInstance));
}

void wl_event_loop::removeDestroyCallback(jobject callback) {
    JavaCallback *c;
	wl_list_for_each(c, &getData()->destroySignal.listener_list, link)
		if (env->IsSameObject(c->callback, callback)) {
		    wl_list_remove(&c->link);
		    c->destroy(c);
		}
}

jclass wl_event_loop::clazz = nullptr;
jmethodID wl_event_loop::constructor = nullptr;
jfieldID wl_event_loop::peer = nullptr;

bool wl_event_loop::registerNatives() {
	FindClassOrReturn(clazz, "org/freedesktop/Wayland$EventLoop");
	GetMethodIDOrReturn(constructor, "org/freedesktop/Wayland$EventLoop", "<init>", "()V");
    GetFieldIDOrReturn(peer, "org/freedesktop/Wayland$EventLoop", "peer", "J");

	static JNINativeMethod methods[] = {
	#define m(name, signature, func) { (char*) #name, (char*) signature, (void*) func }
	    m(create, "()Lorg/freedesktop/Wayland$EventLoop;", &create),
	    m(destroy, "()V", wrap<wl_event_loop_destroy>),
	    m(addFd, "(IILorg/freedesktop/Wayland$EventLoop$FdCallback;)Lorg/freedesktop/Wayland$EventLoop$EventSource;", wrap<&wl_event_loop::addFd>),
	    m(addTimer, "(ILorg/freedesktop/Wayland$EventLoop$TimerCallback;)Lorg/freedesktop/Wayland$EventLoop$EventSource;", wrap<&wl_event_loop::addTimer>),
	    m(addSignal, "(ILorg/freedesktop/Wayland$EventLoop$SignalCallback;)Lorg/freedesktop/Wayland$EventLoop$EventSource;", wrap<&wl_event_loop::addSignal>),
	    m(addIdle, "(Lorg/freedesktop/Wayland$EventLoop$IdleCallback;)Lorg/freedesktop/Wayland$EventLoop$EventSource;", wrap<&wl_event_loop::addIdle>),
	    m(dispatch, "(I)Z", wrap<wl_event_loop_dispatch>),
	    m(dispatchIdle, "()V", wrap<wl_event_loop_dispatch_idle>),
	    m(getFd, "()I", (wrap<wl_event_loop_get_fd, -1>)),
	    m(addDestroyCallback, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_event_loop::addIdle>),
	    m(removeDestroyCallback, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_event_loop::addIdle>)
	    #undef m
    };

	return (0 == env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod)));
}

// wl_event_source methods

wl_event_source* wl_event_source::getData() {
    return this;
}

jobject wl_event_source::getJavaInstance(void* source) {
    return static_cast<wl_event_source*>(source)->instance;
}

wl_event_source* wl_event_source::fromJava(jobject instance) {
    return (wl_event_source*) env->GetLongField(instance, peer);
}

wl_event_source::wl_event_source(wl_event_loop* loop, wl_event_source* source):
    loop(loop), source(source), instance(env->NewObject(clazz, constructor, loop->getData()->instance)) {
    env->SetLongField(instance, peer, (jlong) source);
    wl_list_insert(&loop->getData()->sources, &link);
}

void wl_event_source::remove() {
    env->SetLongField(instance, peer, 0);
    wl_event_source_remove(source);
    wl_list_remove(&link);
}

void wl_event_source::check() {
    wl_event_source_check(source);
}

jboolean wl_event_source::fdUpdate(jint mask) {
    return (wl_event_source_fd_update(source, mask) == 0);
}

jboolean wl_event_source::timerUpdate(jint msDelay) {
    return (wl_event_source_timer_update(source, msDelay) == 0);
}

int wl_event_source::fdCallback(int fd, uint32_t mask, void *data) {
    return env->CallIntMethod(((JavaCallback*) data)->callback, fdCallbackMethod, fd, mask);
}

int wl_event_source::timerCallback(void *data) {
    return env->CallIntMethod(((JavaCallback*) data)->callback, timerCallbackMethod);
}

int wl_event_source::signalCallback(int signalNumber, void *data) {
    return env->CallIntMethod(((JavaCallback*) data)->callback, fdCallbackMethod, signalNumber);
}

void wl_event_source::idleCallback(void *data) {
    JavaCallback* c = (JavaCallback*) data;
    env->CallVoidMethod(c->callback, idleCallbackMethod);
    wl_list_remove(&c->link);
    c->destroy(c);
}

jclass wl_event_source::clazz = nullptr;
jmethodID wl_event_source::constructor = nullptr;
jmethodID wl_event_source::fdCallbackMethod = nullptr;
jmethodID wl_event_source::timerCallbackMethod = nullptr;
jmethodID wl_event_source::signalCallbackMethod = nullptr;
jmethodID wl_event_source::idleCallbackMethod = nullptr;
jfieldID wl_event_source::peer = nullptr;

bool wl_event_source::registerNatives() {
	FindClassOrReturn(clazz, "org/freedesktop/Wayland$EventLoop$EventSource");
	GetMethodIDOrReturn(constructor, "org/freedesktop/Wayland$EventLoop$EventSource", "<init>", "(Lorg/freedesktop/Wayland$EventLoop;)V");
    GetMethodIDOrReturn(fdCallbackMethod, "org/freedesktop/Wayland$EventLoop$FdCallback", "invoke", "(II)I");
    GetMethodIDOrReturn(timerCallbackMethod, "org/freedesktop/Wayland$EventLoop$TimerCallback", "invoke", "()I");
    GetMethodIDOrReturn(signalCallbackMethod, "org/freedesktop/Wayland$EventLoop$SignalCallback", "invoke", "(I)I");
    GetMethodIDOrReturn(idleCallbackMethod, "org/freedesktop/Wayland$EventLoop$IdleCallback", "invoke", "()V");
	GetFieldIDOrReturn(peer, "org/freedesktop/Wayland$EventLoop$EventSource", "peer", "J");

	static JNINativeMethod methods[] = {
	#define m(name, signature, func) { (char*) #name, (char*) signature, (void*) func }
	    m(remove, "()V", wrap<&wl_event_source::remove>),
	    m(check, "()V", wrap<&wl_event_source::check>),
	    m(fdUpdate, "(I)Z", wrap<&wl_event_source::fdUpdate>),
	    m(timerUpdate, "(I)Z", wrap<&wl_event_source::timerUpdate>),
	    #undef m
    };

	return (0 == env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod)));
}

// wl_global methods

wl_global::Data::Data(wl_global* global, jobject instance):
global(global), instance(instance), wl_listener{{}, &destroy} {
    wl_display_add_destroy_listener(wl_global_get_display(global), this);
    wl_global_set_user_data(global, this);
}

wl_global::Data::~Data() {
    env->SetLongField(instance, peer, 0);
    env->DeleteGlobalRef(instance);
}

wl_global::Data* wl_global::getData() {
    return (Data*) wl_global_get_user_data(this);
}

jobject wl_global::getJavaInstance(void* global) {
    return static_cast<wl_global*>(global)->getData()->instance;
}

wl_global* wl_global::fromJava(jobject instance) {
    return (wl_global*) env->GetLongField(instance, peer);
}

void wl_global::destroy() {
    Data* data = getData();
    wl_global_destroy(this);
    wl_list_remove(&data->link);
    delete data;
}

jobject wl_global::getDisplay() {
    return wl_display::getJavaInstance(wl_global_get_display(this));
}

jobject wl_global::getInterface() {
    return wl_java_interface::getJavaInstance(const_cast<wl_interface*>(wl_global_get_interface(this)));
}

jclass wl_global::clazz = nullptr;
jmethodID wl_global::constructor = nullptr;
jfieldID wl_global::peer = nullptr;

bool wl_global::registerNatives() {
	FindClassOrReturn(clazz, "org/freedesktop/Wayland$Global");
	GetMethodIDOrReturn(constructor, "org/freedesktop/Wayland$Global", "<init>", "()V");
	GetFieldIDOrReturn(peer, "org/freedesktop/Wayland$Global", "peer", "J");

	static JNINativeMethod methods[] = {
	#define m(name, signature, func) { (char*) #name, (char*) signature, (void*) func }
	    m(destroy, "()V", wrap<&wl_global::destroy>),
	    m(remove, "()V", wrap<wl_global_remove>),
	    // TODO: update wayland library and uncomment this
	    //m(getVersion, "()I", wrap<wl_global_get_version>),
	    m(getDisplay, "()Lorg/freedesktop/Wayland$Display;", wrap<&wl_global::getDisplay>),
	    m(getInterface, "()Lorg/freedesktop/Wayland$Interface;", wrap<&wl_global::getInterface>),
	    #undef m
    };

	return (0 == env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod)));
}

// wl_resource methods

wl_resource::Data::Data(wl_resource* res, jobject instance):
res(res), instance(instance), wl_listener{{}, &destroy} {
    wl_resource_set_destructor(res, &destruct);
    wl_resource_set_user_data(res, this);
    wl_signal_init(&destroySignal);
}

wl_resource::Data::~Data() {
    env->SetLongField(instance, peer, 0);
    wl_destroyable_listener *l;
    struct wl_list *pos;
    while (!wl_list_empty(&destroySignal.listener_list)) {
        pos = destroySignal.listener_list.next;
        l = wl_container_of(pos, l, link);
        wl_list_remove(pos);
        l->notify(l, res);
        l->destroy(l);
    }
    if (implementation != nullptr)
        env->DeleteGlobalRef(implementation);
    if (destructor != nullptr) {
        destructor->notify(destructor, res);
        destructor->destroy(destructor);
    }
    env->DeleteGlobalRef(instance);
}

wl_resource::Data* wl_resource::getData() {
    return (Data*) wl_resource_get_user_data(this);
}

jobject wl_resource::getJavaInstance(void* res) {
    return static_cast<wl_resource*>(res)->getData()->instance;
}

wl_resource* wl_resource::fromJava(jobject instance) {
    return (wl_resource*) env->GetLongField(instance, peer);
}

void wl_resource::sendEvent(jint opcode, jobjectArray jargs, void (*sendFunc)(struct wl_resource*,uint32_t,wl_argument*)) {
	union wl_argument args[WL_CLOSURE_MAX_ARGS];
	struct { jstring str; const char* chars;}
        stringsToBeReleased[WL_CLOSURE_MAX_ARGS] = {0};
    // in current implementation pointer to wl_interface is stored at offset 0 of wl_resource.
    const wl_interface* iface = *((wl_interface**) this);
    const char* signature = iface->events[opcode].signature;
    jsize i, len = env->GetArrayLength(jargs);
    for (int j=0, i=0; i<len; i++, j++) {
        if (j==0 && isdigit(signature[j])) j++;
        if (signature[j] == '?') j++;
            jobject jobj = env->GetObjectArrayElement(jargs, i);

        switch(signature[j]) {
            case 'i':
                // It is definitely int value.
                args[i].i = env->CallIntMethod(jobj, intValueMethod);
                break;
            case 'u':
                // It is definitely int value.
                args[i].u = env->CallIntMethod(jobj, intValueMethod);
                break;
            case 'f':
                // It is definitely org.freedesktop.Wayland$Fixed object.
                args[i].f = env->CallIntMethod(jobj, fixedValueMethod);
                break;
            case 's':
                // It is definitely java.lang.String object.
                if (jobj != nullptr) {
                    stringsToBeReleased[i].str = (jstring) jobj;
                    stringsToBeReleased[i].chars = env->GetStringUTFChars((jstring) jobj, nullptr);
                        if (stringsToBeReleased[i].chars != nullptr)
                            args[i].s = stringsToBeReleased[i].chars;
                }
                break;
            case 'o':
                if (jobj != nullptr) {
                    // It is definitely instance of org.freedesktop.Wayland$Resource object.
                    args[i].o = (wl_object*) env->GetLongField(jobj, peer);
                }
                break;
            case 'n':
                // It is definitely int value.
                args[i].i = env->CallIntMethod(jobj, intValueMethod);
                break;
            case 'a':
                ThrowNew("wl_array arguments are currently not supported");
                args[i].a = nullptr;
                return;
            case 'h':
                // It is definitely int value.
                args[i].i = env->CallIntMethod(jobj, intValueMethod);
                break;
        }
    }

    sendFunc(this, opcode, args);

    for (auto v: stringsToBeReleased)
        if (v.str != nullptr)
            env->ReleaseStringUTFChars(v.str, v.chars);
}

void wl_resource::postEvent(jint opcode, jobjectArray jargs) {
    sendEvent(opcode, jargs, wl_resource_post_event_array);
}

void wl_resource::queueEvent(jint opcode, jobjectArray jargs) {
    sendEvent(opcode, jargs, wl_resource_queue_event_array);
}

void wl_resource::postError(jint code, jstring jmsg) {
    const char *msg = (jmsg == nullptr) ? nullptr : env->GetStringUTFChars(jmsg, nullptr);
    wl_resource_post_error(this, code, "%s", msg);
    if (msg != nullptr)
        env->ReleaseStringUTFChars(jmsg, msg);
}

int wl_resource::wl_resource::dispatch(const void *implementation, void *target, uint32_t opcode, const struct wl_message *message,
                    union wl_argument *args) {
    if (implementation == nullptr || target == nullptr || message == nullptr || args == nullptr)
        return -1;
	// TODO: implement this

    // implementation always points to instance of callbacksClass so we can simply invoke it
    // target always points to wl_object which is located at offset 0 in wl_resource
    // so we can safely extract both wl_resource* and wl_interface* objects
    wl_resource *res = (wl_resource*) target;
    // in current implementation pointer to wl_interface is stored at offset 0 of wl_resource.
    wl_java_interface *iface =  *((wl_java_interface**) target);

    jvalue jargs[WL_CLOSURE_MAX_ARGS];
    const char* signature = message->signature;
  	jsize i;
    for (int j=0, i=0; j<strlen(signature); i++, j++) {
    	if (j==0 && isdigit(signature[j])) j++;
    	if (signature[j] == '?') j++;

    	switch(signature[j]) {
    	    case 'i': //uint32_t
    	        /* jint */ jargs[i].i = args[i].i;
    	        break;
    	    case 'u': //uint32_t
    	        /* jint */ jargs[i].i = args[i].u;
    	        break;
    	    case 'f': //wl_fixed_t
    	        break;
    	    case 's': //const char*
    	        /* jobject */ jargs[i].l = env->NewStringUTF(args[i].s);
    	        break;
    	    case 'o': //wl_object*
    	        break;
    	    case 'n': //new_id
    	        break;
    	    case 'a': //wl_array
    	        ThrowNew("dispatcher: wl_array arguments are currently not supported");
    	        /* jobject */ jargs[i].l = nullptr;
    	        return -1;
    	    case 'h': //int (fd)
    	        /* jint */ jargs[i].i = args[i].h;
    	        break;
        }
	}

	env->CallVoidMethodA((jobject) implementation, iface->javaMethods[opcode], jargs);

	// Check if we have pending ImplementationException and rethrow it via wl_resource_post_error
	if (env->ExceptionCheck() == JNI_TRUE) {
	    jthrowable jexception = env->ExceptionOccurred();
	    env->ExceptionClear();
	    if (!env->IsInstanceOf(jexception, implementationExceptionClass)) {
	        env->Throw(jexception);
	        return -1;
	    }
	    int code = env->GetIntField(jexception, implementationExceptionCodeField);
	    jstring jmsg = (jstring) env->GetObjectField(jexception, implementationExceptionMessageField);
	    const char* msg = env->GetStringUTFChars(jmsg, nullptr);
	    wl_resource_post_error(res, code, "%s", msg);
	    env->ReleaseStringUTFChars(jmsg, msg);
	}
	return 0;
}

void wl_resource::setCallbacks(jobject implementation) {
    Data *data = getData();
    if (data->implementation != nullptr)
        env->DeleteGlobalRef(data->implementation);
    data->implementation = env->NewGlobalRef(implementation);
    wl_resource_set_dispatcher(this, &dispatch, data->implementation, data, &Data::destruct);
}

jobject wl_resource::getClient() {
    return wl_client::getJavaInstance(wl_resource_get_client(this));
}

void wl_resource::setDestructor(jobject callback) {
    Data* data = getData();
    if (data->destructor != nullptr) {
        data->destructor->destroy(data->destructor);
        data->destructor = nullptr;
    }
    if (callback != nullptr)
        data->destructor =
            new JavaCallback(callback, JavaCallback::destroyCallbackMethod, &getJavaInstance);
}

jstring wl_resource::getWaylandClass() {
    return env->NewStringUTF(wl_resource_get_class(this));
}

void wl_resource::addDestroyCallback(jobject callback) {
    removeDestroyCallback(callback);
    wl_signal_add(&getData()->destroySignal,
        new JavaCallback(callback, JavaCallback::destroyCallbackMethod, &getJavaInstance));
}

void wl_resource::removeDestroyCallback(jobject callback) {
    JavaCallback *c;
	wl_list_for_each(c, &getData()->destroySignal.listener_list, link)
		if (env->IsSameObject(c->callback, callback)) {
		    wl_list_remove(&c->link);
		    c->destroy(c);
		}
}

jclass wl_resource::clazz = nullptr;
jclass wl_resource::implementationExceptionClass = nullptr;
jclass wl_resource::integerClass = nullptr;
jclass wl_resource::fixedClass = nullptr;
jclass wl_resource::stringClass = nullptr;
jclass wl_resource::arrayClass = nullptr;
jmethodID wl_resource::constructor = nullptr;
jmethodID wl_resource::intValueMethod = nullptr;
jmethodID wl_resource::fixedValueMethod = nullptr;
jmethodID wl_resource::fixedConstructorMethod = nullptr;
jfieldID wl_resource::implementationExceptionCodeField = nullptr;
jfieldID wl_resource::implementationExceptionMessageField = nullptr;
jfieldID wl_resource::peer = nullptr;

bool wl_resource::registerNatives() {
	FindClassOrReturn(clazz, "org/freedesktop/Wayland$Resource");
	FindClassOrReturn(implementationExceptionClass, "org/freedesktop/Wayland$Resource$Callbacks$ImplementationException");
	FindClassOrReturn(integerClass, "java/lang/Integer");
	FindClassOrReturn(fixedClass, "org/freedesktop/Wayland$Fixed");
    FindClassOrReturn(stringClass, "java/lang/String");
    //FindClassOrReturn(arrayClass, "org/freedesktop/Wayland$Array");

	GetMethodIDOrReturn(constructor, "org/freedesktop/Wayland$Resource", "<init>", "()V");
    GetMethodIDOrReturn(intValueMethod, "java/lang/Integer", "intValue", "()I");
    GetMethodIDOrReturn(fixedValueMethod, "org/freedesktop/Wayland$Fixed", "rawValue", "()I");
    GetMethodIDOrReturn(fixedConstructorMethod, "org/freedesktop/Wayland$Fixed", "<init>", "(I)V");

	GetFieldIDOrReturn(peer, "org/freedesktop/Wayland$Resource", "peer", "J");
	GetFieldIDOrReturn(implementationExceptionCodeField, "org/freedesktop/Wayland$Resource$Callbacks$ImplementationException", "code", "I");
	GetFieldIDOrReturn(implementationExceptionMessageField, "org/freedesktop/Wayland$Resource$Callbacks$ImplementationException", "message", "Ljava/lang/String;");

	static JNINativeMethod methods[] = {
	#define m(name, signature, func) { (char*) #name, (char*) signature, (void*) func }
	    m(postEvent, "(I[Ljava/lang/Object;)V", wrap<&wl_resource::postEvent>),
	    m(queueEvent, "(I[Ljava/lang/Object;)V", wrap<&wl_resource::queueEvent>),
	    m(postError, "(ILjava/lang/String;)V", wrap<&wl_resource::postError>),
	    m(postNoMemory, "()V", wrap<wl_resource_post_no_memory>),
	    m(setCallbacks, "(Lorg/freedesktop/Wayland$Resource$Callbacks;)V", wrap<&wl_resource::setCallbacks>),
	    m(destroy, "()V", wrap<wl_resource_destroy>),
	    m(getId, "()I", wrap<wl_resource_get_id>),
	    m(getClient, "()Lorg/freedesktop/Wayland$Client;", wrap<&wl_resource::getClient>),
	    m(getVersion, "()I", wrap<wl_resource_get_version>),
	    m(setDestructor, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_resource::setDestructor>),
	    m(getWaylandClass, "()Ljava/lang/String;", wrap<&wl_resource::getWaylandClass>),
	    m(addDestroyCallback, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_resource::addDestroyCallback>),
	    m(removeDestroyCallback, "(Lorg/freedesktop/Wayland$DestroyCallback;)V", wrap<&wl_resource::removeDestroyCallback>),
	    #undef m
    };

	return (0 == env->RegisterNatives(clazz, methods,
				sizeof(methods)/sizeof(JNINativeMethod)));
}

static const char* jstrdup(jstring jstr) {
    if (env->IsSameObject(jstr, nullptr))
        return nullptr;
    const char *str, *tmp = env->GetStringUTFChars(jstr, nullptr);
    str = strdup(tmp);
    env->ReleaseStringUTFChars(jstr, tmp);
    return str;
}

void wl_java_interface::processMessageArray(jobjectArray jmessages, jsize messageCount, const struct wl_message *messages) {
    for(jsize i=0; i<messageCount; i++) {
        jobject jmessage = env->GetObjectArrayElement(jmessages,i);
        const char *methodName = jstrdup((jstring) env->GetObjectField(jmessage, messageNameField));
        const char *methodSignature = jstrdup((jstring) env->GetObjectField(jmessage, messageSignatureField));
        const jobjectArray jmethodTypes = (jobjectArray) env->GetObjectField(jmessage, messageTypesField);
        const jsize methodTypesSize = env->GetArrayLength(jmethodTypes);
        const wl_interface** methodTypes = new const wl_interface*[methodTypesSize];
        for(jsize j=0; j<methodTypesSize; j++) {
            const char *type = jstrdup((jstring) env->GetObjectArrayElement(jmethodTypes, j));
            methodTypes[j] = (type == nullptr) ? nullptr : new wl_interface {type, 0, 0, nullptr, 0, nullptr};
        }
        wl_message msg {methodName, methodSignature, methodTypes};
        memcpy((void*) &messages[i], &msg, sizeof(msg));
    }
}

void wl_java_interface::create(jobject obj, jstring jname, jint version, jobjectArray jrequests,
                                jobjectArray jevents, jclass callbacksClass) {
/*    const char* name = jstrdup(env, jname);
    jsize methodCount = env->GetArrayLength(jrequests);
    const struct wl_message *methods = new wl_message[methodCount];
    jmethodID *jmethods = new jmethodID[methodCount];
    jsize eventCount = env->GetArrayLength(jevents);
    const struct wl_message *events = new wl_message[eventCount];
    processMessageArray(env, jrequests, methodCount, methods);
    processMessageArray(env, jevents, eventCount, events);

    for (int i=0; i<methodCount; i++) {
        char signature[512] = "(";
        for(char* j=(char*)methods[i].signature; *j!='\0'; j++) {
            if (isdigit(*j) || *j == '?')
                j++;
            switch (*j) {
    	    case 'i': //uint32_t
    	    case 'u': //uint32_t
    	    case 'h': //int (fd)
    	        strcat(signature, "I");
    	        break;
    	    case 'f': //wl_fixed_t
    	        strcat(signature, "Lorg/freedesktop/Wayland$Fixed;");
    	        break;
    	    case 's': //const char*
    	        strcat(signature, "Ljava/lang/String;");
    	        break;
    	    case 'o': //wl_object*
    	    case 'n': //new_id
    	        strcat(signature, "Lorg/freedesktop/Wayland$Resource;");
    	        break;
    	    case 'a': //wl_array
    	        strcat(signature, "Ljava/lang/Object;");
    	        break;
            }
        }
        jmethods[i] = env->GetMethodID(callbacksClass, methods[i].name, signature);
    }

    Interface* iface = new Interface(env, name, version, methodCount, methods, eventCount, events, callbacksClass, obj, jmethods);
    env->SetLongField(obj, peerField, (jlong) iface);*/
}

jclass wl_java_interface::ifaceClass = nullptr;
jclass wl_java_interface::messageClass = nullptr;
jfieldID wl_java_interface::messageNameField = nullptr;
jfieldID wl_java_interface::messageSignatureField = nullptr;
jfieldID wl_java_interface::messageTypesField = nullptr;
jfieldID wl_java_interface::peerField = nullptr;

bool wl_java_interface::registerNatives() {
	FindClassOrReturn(ifaceClass, "org/freedesktop/Wayland$Interface");
	FindClassOrReturn(messageClass, "org/freedesktop/Wayland$Interface$Message");
	GetFieldIDOrReturn(messageNameField, "org/freedesktop/Wayland$Interface$Message", "name", "Ljava/lang/String;");
	GetFieldIDOrReturn(messageSignatureField, "org/freedesktop/Wayland$Interface$Message", "signature", "Ljava/lang/String;");
	GetFieldIDOrReturn(messageTypesField, "org/freedesktop/Wayland$Interface$Message", "types", "[Ljava/lang/String;");
	GetFieldIDOrReturn(peerField, "org/freedesktop/Wayland$Interface", "peer", "J");

	static JNINativeMethod methods[] = {
	#define m(name, signature) { (char*) #name, (char*) signature, (void*) &name }
	    m(create, "(Ljava/lang/String;I[Lorg/freedesktop/Wayland$Interface$Message;[Lorg/freedesktop/Wayland$Interface$Message;Ljava/lang/Class;)Lorg/freedesktop/Wayland$Interface;")
	    #undef m
    };

	return (0 == env->RegisterNatives(ifaceClass, methods, sizeof(methods)/sizeof(JNINativeMethod)));
}

#include <unistd.h>
extern "C" JNIEXPORT void JNICALL
print(JNIEnv *env, jobject obj) {
    dprintf(2,"Hello World!(%d)\n", getpid());
    wl_display_destroy(wl_display_create());
    return;
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	vm->GetEnv((void**)&env, JNI_VERSION_1_6);

	if (!JavaCallback::registerNatives()            ||
	    !wl_display::registerNatives()              ||
	    !wl_client::registerNatives()               ||
		!wl_event_loop::registerNatives()           ||
		!wl_event_source::registerNatives()         ||
		!wl_global::registerNatives()               ||
		!wl_resource::registerNatives()             ||
		!wl_java_interface::registerNatives())
		return -1;

	JNINativeMethod method = { (char*) "print", (char*) "()V", (void*) &print };
	env->RegisterNatives(env->FindClass("org/freedesktop/Wayland"), &method, 1);

	return JNI_VERSION_1_6;
}