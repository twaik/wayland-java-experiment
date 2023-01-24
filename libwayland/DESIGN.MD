### Design

Target of this project is to define Java Wayland API which completely
relies on native implementation. It should own all Java objects to 
avoid mistaken object deallocation.

#### Java part

There are two purposes for Java part of this wrapper:

1. Declare classes containing functional interfaces (for callbacks) 
and classes containing native functions.
2. Javadocs.

Java part should not have any method implemented in Java.
Also classes implemented in Java should not have any fields
except `jlong peer` (to store native pointer) and `Object data`
to store some userdata. `data` field is not processed in JNI, it
should help user store some auxiliary data. 

#### Native/JNI part

This part is a bit more complicated.

##### Passing Java callbacks to native.

Java callbacks can not be represented as function pointers like
callbacks in C/C++ and we can not call them directly.
But Java have `Runnable` which can be represented as lambda without arguments,
`Consumer/BiConsumer` which can be represented as lambdas with 1 (for `Consumer`) 
and 2 (for `BiConsumer`) args, and single method interfaces which can be represented as
lambda with the number of arguments corresponding to its function.
For uniformity I chose interfaces.

To invoke this callback from JNI we should have three pointers:

1. JNIEnv*.
2. jmethodID pointing to function that should be invoked.
3. jobject pointing to callback (strong/global reference).

I am using `wl_listener` based class `JavaCallback` to store all of them.
This way we can pass callback to wayland and add this callback to destroy list
of corresponding Wayland object (see `*_add_destroy_listener()` in Wayland API).

In the case of `wl_display_global_filter_func_t`, `wl_event_loop_fd_func_t`, 
`wl_event_loop_idle_func_t`, `wl_global_bind_func_t` which have no `wl_listener` argument in
their argument list we are creating the same `JavaCallback`, and storing it in
native object's `userdata`. It will be invoked by some wrapper function passed to Wayland API.

##### Garbage collecting.

Every single callback or native class instance is owning it Java representation (see JNI's `NewGlobalRef`).
When native object is being destroyed it also destroys all Java references.
After destroying native instance (see `*_destroy()` in Wayland API) its Java representation becomes invalid (`peer` field equals `0`).
All operations on invalid objects are noop.

`Wayland.Display` and `Wayland.EventLoop` (created using `Wayland.EventLoop#create()`) must be destroyed manually using
`destroy()` call to avoid memory leaks.

`Interface` is the only class that can not be garbage collected.

##### Handling pointers to Wayland objects.

In Wayland API structures are declared, but not defined. This means we can try to define 
those structures in code with no non-static data members (to avoid overwriting some internal wayland data).
It will allow us to write some methods working with wayland API pointers without disrupting it.
In those methods Wayland API pointers will be available as `this`.

##### Storing data and data destructing.

Only `wl_global` and `wl_resource` classes have `*_get_user_data()`, `*_set_user_data()` functions
and only `wl_resource` has `*_set_destructor()` function. That means we can not store our data using
existing API. But we can create a single `wl_listener` based class which will serve as out destructor object.
It will contain `wl_signal` pointing to destroy callbacks, `wl_signal` pointing to create callbacks and
other specific data. This `wl_listener` will be the only listener added to wayland's destruction sequence
via `*_add_destroy_listener`.

##### wayland-scanner

We can not use classic `wayland-scanner` to generate Java code. 
There is java scanner in `scanner` folder that can be used to generate compatible java code.
Also it allows us avoid using libffi so we can comment out the code using it in native wayland.
