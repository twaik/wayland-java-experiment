package org.freedesktop;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/** <strong>Wayland Server API Java bindings</strong>
 * <p>
 * Wayland is an object-oriented display protocol, which features request and events.
 * Requests can be seen as method calls on certain objects, whereas events can be seen as signals of an object.
 * This makes the Wayland protocol a perfect candidate for a Java binding.
 * <p>
 * The goal of this library is to create a Java binding for Wayland keeping original
 * design (as close as it possible) but the same time using object-oriented features of Java.
 * <p>
 * <strong>Garbage collecting. </strong> All JNI implementations of these objects own a strong
 * global reference to its object. Java instances of these objects are valid for the lifetime
 * of native instances even if they are not referenced anywhere in code.
 * <p>
 * All {@link Resource} instances are valid for the lifecycle of the corresponding {@link Client} instance.
 * All {@link Global} instances are valid for the lifecycle of the corresponding {@link Display} instance.
 * All {@link Client} instances are valid for the lifecycle of the corresponding {@link Display} instance.
 * <p>
 * <strong>Listeners/callbacks. </strong> JNI does not allow us to create function pointers
 * pointing directly to Java functions. C even does not allow us to create some closures
 * to be used as function pointers. But luckily wayland accepts struct containing function pointer
 * and this struct can be extended to keep Java callback handle and pointer to function that can dispatch it.
 * Also, In Java there is no function pointers and function prototypes (C-like).
 * The closest thing that can implement such a behaviour in Java is interface, which can
 * implement both wl_listener and wl_notify_func_t behaviour.
 */

// TODO: write about resources implementation and it's usage
@SuppressWarnings("unused")
public class Wayland {
    /** Name of shared library */
    private static final String libName = "/home/twaik/build-WaylandJNI-Desktop-Debug/libWaylandJNI.so";

    /** Instantiation of this object is not permitted */
    private Wayland() {}

    /** Get current Display. Returns object only during *.Callbacks run */
    public static native Display currentDisplay();

    /** Get current Client. Returns object only during *.Callbacks run */
    public static native Client currentClient();

    /** Holds a callback to be used with {@link Display#addClientCreatedCallback},
     * {@link Client#addResourceCreatedCallback}.
     * <p>
     * Implements wl_listener with defined wl_notify_func_t.
     */
    public interface CreateCallback<T> {
        /** Modified wl_notify_func_t prototype
         *
         * @param object object being created
         */
        void invoke(T object);
    }
    /** Holds a callback to be used with {@link Display#addDestroyCallback},
     * {@link Client#addDestroyCallback},  {@link Resource#addDestroyCallback},
     * {@link EventLoop#addDestroyCallback}.
     * <p>
     * Implements wl_listener with defined wl_notify_func_t.
     */
    public interface DestroyCallback<T> {
        /** Modified wl_notify_func_t prototype
         *
         * @param object object being destroyed
         */
        void invoke(T object);
    }

    /** Java representation of wl_display */
    public static class Display {

        /** Instantiation of this class is not permitted.
         * <p>
         * If you want to create Display you should
         * use {@link Display#create}
         */
        private Display() {}

        /** Native instance allocator.
         * <p>
         * Wraps wl_display_create()
         */
        public static native Display create();

        /** Destroy Wayland display object.
         * <p>
         * This function emits the wl_display destroy signal, releases
         * all the sockets added to this display, free's all the globals associated
         * with this display, free's memory of additional shared memory formats and
         * destroy the display object.
         * <p>
         * Wraps wl_display_destroy().
         *
         * @see #addDestroyCallback
         */
        public native void destroy();

        /** Wraps wl_display_get_event_loop()
         *
         * @return main EventLoop associated with Display
         */
        public native EventLoop getEventLoop();

        /** Add a socket to Wayland display for the clients to connect.
         * <p>
         * This adds a Unix socket to Wayland display which can be used by clients to
         * connect to Wayland display.
         * <p>
         * If null is passed as name, then it would look for WAYLAND_DISPLAY env
         * variable for the socket name. If WAYLAND_DISPLAY is not set, then default
         * wayland-0 is used.
         * <p>
         * If the socket name is a relative path, the Unix socket will be created in
         * the directory pointed to by environment variable XDG_RUNTIME_DIR. If
         * XDG_RUNTIME_DIR is invalid or not set, then this function fails and returns false.
         * <p>
         * If the socket name is an absolute path, then it is used as-is for
         * the Unix socket.
         * <p>
         * The length of the computed socket path must not exceed the maximum length
         * of a Unix socket path.
         * The function also fails if the user does not have write permission in the
         * directory or if the path is already in use.
         * <p>
         * Wraps wl_display_add_socket()
         *
         * @param name Name of the Unix socket.
         * @return true if success. false if failed.
         */
        public native boolean addSocket(String name);

        /** Add a socket with default name to Wayland display for the clients to connect.
         * <p>
         * Wraps wl_display_add_socket_auto().
         */
        public native void addSocketAuto();

        /** Add a socket with an existing fd to Wayland display for the clients to connect.
         * <p>
         * The existing socket fd must already be created, opened, and locked.
         * The fd must be properly set to CLOEXEC and bound to a socket file
         * with both bind() and listen() already called.
         * <p>
         * Wraps wl_display_add_socket_fd().
         *
         * @param sockFd The existing socket file descriptor to be used.
         * @return true if success. false if failed.
         */
        public native boolean addSocketFd(int sockFd);

        /** Interrupts current run() sequence.
         * <p>
         * Wraps wl_display_terminate()
         *
         * @see #run()
         */
        public native void terminate();

        /** Dispatches main event loop until {@link #terminate()} is called.
         * <p>
         * Wraps wl_display_run().
         *
         * @see #terminate()
         */
        public native void run();

        /** Flush pending events to the clients.
         * <p>
         * Wraps wl_display_flush_clients().
         *
         * @see Client#flush()
         */
        public native void flushClients();

        /** Destroy all clients connected to the display
         * <p>
         * This function should be called right before {@link #destroy} to ensure
         * all client resources are closed properly. Destroying a client from within
         * destroyClients is safe, but creating one will leak resources
         * and raise a warning.
         * <p>
         * Wraps wl_display_destroy_clients().
         *
         * @see Client#destroy()
         */
        public native void destroyClients();

        /** Get the current pending serial number.
         * <p>
         * This function returns the most recent serial number, but does not
         * increment it.
         * <p>
         * Wraps wl_display_get_serial().
         *
         * @return Current pending serial number.
         */
        public native int getSerial();

        /** Get the next pending serial number.
         * <p>
         * This function increments the display serial number and returns the
         * new value.
         * <p>
         * Wraps wl_display_next_serial().
         *
         * @return Next pending serial number.
         */
        public native int nextSerial();

        /** Adds given callback to display's destroy callbacks list.
         * <p>
         * Wraps wl_display_add_destroy_listener().
         *
         * @param callback Callback object to be added to the list.
         * @see DestroyCallback
         * @see #removeDestroyCallback
         */
        public native void addDestroyCallback(DestroyCallback<Display> callback);

        /** Removes given callback from display's destroy callbacks list.
         * <p>
         * Not a part of Wayland API.
         *
         * @param callback Callback object to be removed from the list.
         * @see DestroyCallback
         * @see #addDestroyCallback
         */
        public native void removeDestroyCallback(DestroyCallback<Display> callback);

        /** Registers a listener for the client connection signal.
         * <p>
         * When a new client object is created, callback will be invoked, carrying
         * the new Client instance.
         * <p>
         * Wraps wl_display_add_client_created_listener().
         *
         * @param callback Signal handler callback
         * @see CreateCallback
         * @see #removeClientCreatedCallback
         */
        public native void addClientCreatedCallback(CreateCallback<Client> callback);
        
        /** Removes given callback from display's client created callbacks list.
         * <p>
         * Not a part of Wayland API.
         *
         * @param callback Callback object to be removed from the list.
         * @see CreateCallback
         * @see #addClientCreatedCallback(CreateCallback) 
         */
        public native void removeClientCreatedCallback(CreateCallback<Client> callback);

        /** Set a filter function for global objects.
         * <p>
         * Set a filter for the wl_display to advertise or hide global objects
         * to clients.
         * The set filter will be used during wl_global advertisement to
         * determine whether a global object should be advertised to a
         * given client, and during wl_global binding to determine whether
         * a given client should be allowed to bind to a global.
         * <p>
         * Clients that try to bind to a global that was filtered out will
         * have an error raised.
         * <p>
         * Setting the filter null will result in all globals being
         * advertised to all clients. The default is no filter.
         * <p>
         * The filter should be installed before any client connects and should always
         * take the same decision given a client and a global. Not doing so will result
         * in inconsistent filtering and broken wl_registry event sequences.
         * <p>
         * Wraps wl_display_set_global_filter().
         *
         * @param callback  The global filter callback.
         * @see Global.FilterCallback
         */
        public native void setGlobalFilterCallback(Global.FilterCallback callback);

        /** Associates global for wl_shm interface with given display.
         * <p>
         * Wraps wl_display_init_shm().
         *
         * @return true if success. false if failed.
         */
        public native boolean initShm();

        /** Add support for a wl_shm pixel format.
         * <p>
         * Add the specified wl_shm format to the list of formats the wl_shm
         * object advertises when a client binds to it. Adding a format to
         * the list means that clients will know that the compositor supports
         * this format and may use it for creating wl_shm buffers. The
         * compositor must be able to handle the pixel format when a client
         * requests it.
         * <p>
         * The compositor by default supports WL_SHM_FORMAT_ARGB8888 and
         * WL_SHM_FORMAT_XRGB8888.
         * <p>
         * Wraps wl_display_add_shm_format().
         *
         * @param format The wl_shm pixel format to advertise.
         * @return true if success. false if failed.
         */
        public native boolean addShmFormat(int format);

        /** Create a client for the given file descriptor.
         * <p>
         * Given a file descriptor corresponding to one end of a socket, this
         * function will create a wl_client struct and add the new client to
         * the compositors client list.  At that point, the client is
         * initialized and ready to run, as if the client had connected to the
         * servers listening socket.  When the client eventually sends
         * requests to the compositor, the wl_client argument to the request
         * handler will be the wl_client returned from this function.
         * <p>
         * The other end of the socket can be passed to
         * wl_display_connect_to_fd() on the client side or used with the
         * WAYLAND_SOCKET environment variable on the client side.
         * <p>
         * Listeners added with {@link #addClientCreatedCallback} will
         * be notified by this function after the client is fully constructed.
         * <p>
         * On failure this function sets errno accordingly and returns null.
         * <p>
         * Wraps wl_client_create().
         *
         * @param fd The file descriptor for the socket to the client.
         * @return The new client object or null on failure.
         */
        public native Client createClient(int fd);

        /** Iterate over all available clients.
         * <p>
         * Wraps wl_client_for_each().
         *
         * @param iterator An iterator function.
         */
        public native void forEachClient(Consumer<Client> iterator);

        /** Create Global of given interface and associate it with a display
         * <p>
         * Wraps wl_display_add_global()/wl_global_create().
         *
         * @param iface interface to be published as a Global
         * @param version version of an interface
         * @param callback callback to be invoked while binding
         * @return new Global instance
         */
        public native Global createGlobal(Interface iface, int version, Global.BindCallback callback);

        /** Create Global of given interface and associate it with a display
         * <p>
         * In this method callback defaults to null.
         * <p>
         * Wraps wl_display_add_global()/wl_global_create().
         *
         * @param iface interface to be published as a Global
         * @param version version of an interface
         * @return new Global instance
         */
        public native Global createGlobal(Interface iface, int version);

        /** Create Global of given interface and associate it with a display
         * <p>
         * In this method version defaults to interface version.
         * <p>
         * Wraps wl_display_add_global()/wl_global_create().
         *
         * @param iface interface to be published as a Global
         * @param callback callback to be invoked while binding
         * @return new Global instance
         */
        public native Global createGlobal(Interface iface, Global.BindCallback callback);

        /** Create Global of given interface and associate it with a display
         * <p>
         * In this method version defaults to interface version and callback defaults to null.
         * <p>
         * Wraps wl_display_add_global()/wl_global_create().
         *
         * @param iface interface to be published as a Global
         * @return new Global instance
         */
        public native Global createGlobal(Interface iface);

        /** Handle for some user-defined data */
        public Object data;

        /** Handle for native instance pointer */
        private long peer;

        static {
            System.load(libName);
        }
    }

    /** Java representation of wl_client */
    public static class Client {

        /** Instantiation of this class is not permitted.
         * <p>
         * Java instance is created automatically in JNI.
         * <p>
         * If you want to fire some action right after instantiating Client
         * use {@link Display#addClientCreatedCallback}
         * <p>
         * If you want to create Client with specific file descriptor
         * use {@link Display#createClient}
         */
        private Client() {}

        /** Destroy Wayland client object.
         * <p>
         *  Wraps wl_client_destroy().
         *
         * @see #addDestroyCallback
         */
        public native void destroy();

        /** Flush pending events to the client.
         * <p>
         * Events sent to clients are queued in a buffer and written to the
         * socket later - typically when the compositor has handled all
         * requests and goes back to block in the event loop.  This function
         * flushes all queued up events for a client immediately.
         * <p>
         *  Wraps wl_client_flush().
         */
        public native void flush();

        /** Return Unix PID for the client.
         * <p>
         * This function returns the process ID for the given client.
         * The credentials come from getsockopt() with SO_PEERCRED, on the client socket fd.
         * <p>
         * Note, process IDs are subject to race conditions and are not a reliable way
         * to identify a client.
         * <p>
         * Be aware that for clients that a compositor forks and execs and
         * then connects using socketpair(), this function will return the
         * credentials for the compositor.  The credentials for the socketpair
         * are set at creation time in the compositor.
         * <p>
         *  Wraps wl_client_get_credentials().
         *
         * @return PID of the client
         * @see #getUid
         * @see #getGid
         */
        public native int getPid();

        /** Return Unix UID for the client.
         * <p>
         * This function returns the user ID for the given client.
         * The credentials come from getsockopt() with SO_PEERCRED, on the client socket fd.
         * <p>
         * Note, process IDs are subject to race conditions and are not a reliable way
         * to identify a client.
         * <p>
         * Be aware that for clients that a compositor forks and execs and
         * then connects using socketpair(), this function will return the
         * credentials for the compositor.  The credentials for the socketpair
         * are set at creation time in the compositor.
         * <p>
         *  Wraps wl_client_get_credentials().
         *
         * @return UID of the client
         * @see #getPid
         * @see #getGid
         */
        public native int getUid();

        /** Return Unix GID for the client.
         * <p>
         * This function returns the group ID for the given client.
         * The credentials come from getsockopt() with SO_PEERCRED, on the client socket fd.
         * <p>
         * Note, process IDs are subject to race conditions and are not a reliable way
         * to identify a client.
         * <p>
         * Be aware that for clients that a compositor forks and execs and
         * then connects using socketpair(), this function will return the
         * credentials for the compositor.  The credentials for the socketpair
         * are set at creation time in the compositor.
         * <p>
         *  Wraps wl_client_get_credentials().
         *
         * @return GID of the client
         * @see #getPid
         * @see #getUid
         */
        public native int getGid();

        /** Get the file descriptor for the client.
         * <p>
         * This function returns the file descriptor for the given client.
         * <p>
         * Be sure to use the file descriptor from the client for inspection only.
         * If the caller does anything to the file descriptor that changes its state,
         * it will likely cause problems.
         * <p>
         * It is recommended that you evaluate whether wl_client_get_credentials()
         * can be applied to your use case instead of this function.
         * <p>
         * If you would like to distinguish just between the client and the compositor
         * itself from the client's request, it can be done by getting the client
         * credentials and by checking the PID of the client and the compositor's PID.
         * Regarding the case in which the socketpair() is being used, you need to be
         * careful. Please note the documentation for {@link #getPid}, {@link #getUid}, {@link #getGid}.
         * <p>
         * This function can be used for a compositor to validate a request from
         * a client if there are additional information provided from the client's
         * file descriptor. For instance, suppose you can get the security contexts
         * from the client's file descriptor. The compositor can validate the client's
         * request with the contexts and make a decision whether it permits or deny it.
         * <p>
         * Wraps wl_client_get_fd().
         *
         * @return The file descriptor to use for the connection.
         * @see #getPid #getUid #getGid
         */
        public native int getFd();

        /** Adds given callback to client's destroy callbacks list.
         * <p>
         * Wraps wl_client_add_destroy_listener().
         *
         * @param callback Callback object to be added to the list.
         * @see DestroyCallback
         * @see #removeDestroyCallback
         */
        public native void addDestroyCallback(DestroyCallback<Client> callback);

        /** Removes given callback from client's destroy callbacks list.
         * <p>
         * Not a part of Wayland API.
         *
         * @param callback Callback object to be removed from the list.
         * @see DestroyCallback
         * @see #addDestroyCallback
         */
        public native void removeDestroyCallback(DestroyCallback<Client> callback);

        /** Look up an object in the client name space.
         * <p>
         * This looks up an object in the client object name space by its
         * object ID.
         * <p>
         * Wraps wl_client_get_object().
         *
         * @param id The object id.
         * @return The object or NULL if there is no object for the given ID.
         */
        public native Resource getObject(int id);

        /** Report a memory allocation error.
         * <p>
         * Report a memory allocation error and disconnect the client.
         * <p>
         * Wraps wl_client_post_no_memory().
         */
        public native void postNoMemory();

        /** Report an internal server error.
         * <p>
         * Report an unspecified internal implementation error and disconnect
         * the client.
         * <p>
         * Wraps wl_client_post_implementation_error().
         *
         * @param msg String containing error explanation.
         */
        public native void postImplementationError(String msg);

        /** Add a callback for the client's resource creation signal.
         * <p>
         * When a new resource is created for this client the listener
         * will be notified, carrying the new resource as the data argument.
         * <p>
         * Wraps wl_client_add_resource_created_listener().
         *
         * @param callback The callback to be added.
         */
        public native void addResourceCreatedCallback(CreateCallback<Resource> callback);

        /** Removes given callback from client's resource created callbacks list.
         * <p>
         * Not a part of Wayland API.
         *
         * @param callback Callback object to be removed from the list.
         * @see CreateCallback
         * @see #addResourceCreatedCallback
         */
        public native void removeResourceCreatedCallback(CreateCallback<Resource> callback);

        /** Iterate over all the resources of a client.
         * <p>
         * The function pointed by iterator will be called for each
         * resource owned by the client. The user_data will be passed
         * as the second argument of the iterator function.
         * If the iterator function returns WL_ITERATOR_CONTINUE the iteration
         * will continue, if it returns WL_ITERATOR_STOP it will stop.
         * <p>
         * Creating and destroying resources while iterating is safe, but new
         * resources may or may not be picked up by the iterator.
         * <p>
         * Wraps wl_client_for_each_resource().
         *
         * @param iterator The iterator function.
         */
        public native void forEachResource(Consumer<Resource> iterator);

        /** Get the display object for the given client
         * <p>
         * Wraps wl_client_get_display().
         *
         * @return The display object the client is associated with.
         */
        public native Display getDisplay();

        /** Create a new resource object
         * <p>
         * Listeners added with {@link #addResourceCreatedCallback} will be
         * notified at the end of this function.
         * <p>
         * Wraps wl_resource_create().
         *
         * @param iface The interface of the new resource.
         * @param version The version of the new resource.
         * @param id The id of the new resource. If 0, an available id will be used.
         * @return New resource instance.
         */
        public native Resource createResource(Interface iface, int version, int id);

        /** Create a new resource object
         * <p>
         * Listeners added with {@link #addResourceCreatedCallback} will be
         * notified at the end of this function.
         * <p>
         * Wraps wl_resource_create().
         *
         * @param iface The interface of the new resource.
         * @param id The id of the new resource. If 0, an available id will be used.
         * @return New resource instance.
         */
        public native Resource createResource(Interface iface, int id);

        /** Handle for some user-defined data */
        public Object data;

        /** Handle for native instance pointer */
        private long peer;

        static {
            System.load(libName);
        }
    }

    /** Java representation of wl_event_loop */
    public static class EventLoop {
        /** Synced with C constant */
        public static final int WL_EVENT_READABLE = 0x01;
        /** Synced with C constant */
        public static final int WL_EVENT_WRITABLE = 0x02;
        /** Synced with C constant */
        public static final int WL_EVENT_HANGUP = 0x04;
        /** Synced with C constant */
        public static final int WL_EVENT_ERROR = 0x08;

        /** File descriptor dispatch callback type.
         * <p>
         * Callback of this type are used as callbacks for file descriptor events.
         * <p>
         * Implements wl_event_loop_fd_func_t.
         *
         * @see #addFd
         */
        public interface FdCallback {
            /** Modified wl_event_loop_fd_func_t prototype
             *
             * @param fd The file descriptor delivering the event.
             * @param mask Describes the kind of the event as a bitwise-or of:
             * <p>
             * WL_EVENT_READABLE, WL_EVENT_WRITABLE, WL_EVENT_HANGUP, WL_EVENT_ERROR.
             * @return If the event source is registered for re-check with
             * {@link EventSource#check()}: 0 for all done, 1 for needing a re-check.
             * If not registered, the return value is ignored and should be zero.
             */
            int invoke(int fd, int mask);
        }

        /** Timer dispatch callback type.
         * <p>
         * Callbacks of this type are used as callbacks for timer expiry.
         * <p>
         * Implements wl_event_loop_timer_func_t.
         *
         * @see #addTimer
         */
        public interface TimerCallback {
            /** Modified wl_event_loop_timer_func_t prototype.
             *
             * @return If the event source is registered for re-check with
             * {@link EventSource#check()}: 0 for all done, 1 for needing a re-check.
             * If not registered, the return value is ignored and should be zero.
             */
            int invoke();
        }

        /** Signal dispatch callback type.
         * <p>
         * Functions of this type are used as callbacks for (POSIX) signals.
         * <p>
         * Implements wl_event_loop_signal_func_t
         *
         * @see #addSignal
         */
        public interface SignalCallback {
            /** Modified wl_event_loop_signal_func_t prototype
             *
             * @param signalNumber Number of signal being processed
             * @return If the event source is registered for re-check with
             * {@link EventSource#check()}: 0 for all done, 1 for needing a re-check.
             * If not registered, the return value is ignored and should be zero.
             */
            int invoke(int signalNumber);
        }

        /** Idle task callback type.
         * <p>
         * Functions of this type are used as callbacks before blocking in {@link #dispatch}.
         * <p>
         * Implements wl_event_loop_idle_func_t.
         *
         * @see #addIdle
         * @see #dispatch
         */
        public interface IdleCallback {
            /** Modified wl_event_loop_idle_func_t prototype. */
            void invoke();
        }

        /** Java representation of wl_event_source */
        @SuppressWarnings("InnerClassMayBeStatic")
        public class EventSource {
            /** Instantiation of this object is not permitted
             *
             * @see #addFd
             * @see #addTimer
             * @see #addSignal
             * @see #addIdle
             */
            private EventSource() {}

            /** Remove an event source from its event loop
             * <p>
             * The event source is removed from the event loop it was created for,
             * and is effectively destroyed. This invalidates source.
             * The dispatch function of the source will no longer be called through this
             * source.
             * <p>
             * Wraps wl_event_source_remove().
             */
            public native void remove();

            /** Mark event source to be re-checked.
             * <p>
             * This function permanently marks the event source to be re-checked after
             * the normal dispatch of sources in {@link #dispatch}. Re-checking
             * will keep iterating over all such event sources until the dispatch
             * function for them all returns zero.
             * <p>
             * Re-checking is used on sources that may become ready to dispatch as a
             * side effect of dispatching themselves or other event sources, including idle
             * sources. Re-checking ensures all the incoming events have been fully drained
             * before {@link #dispatch} returns.
             * <p>
             * Wraps wl_event_source_check().
             *
             * @see #dispatch
             */
            public native void check();

            /** Update a file descriptor source's event mask
             * <p>
             * This changes which events, readable and/or writable, cause the dispatch
             * callback to be called on.
             * <p>
             * File descriptors are usually writable to begin with, so they do not need to
             * be polled for writable until a write actually fails. When a write fails,
             * the event mask can be changed to poll for readable and writable, delivering
             * a dispatch callback when it is possible to write more. Once all data has
             * been written, the mask can be changed to poll only for readable to avoid
             * busy-looping on dispatch.
             * <p>
             * Wraps wl_event_source_fd_update().
             *
             * @param mask The new mask, a bitwise-or of: WL_EVENT_READABLE, WL_EVENT_WRITABLE.
             * @return true on success, false on failure.
             * @see #addFd
             */
            public native boolean fdUpdate(int mask);

            /** Arm or disarm a timer
             * <p>
             * If the timeout is zero, the timer is disarmed.
             * <p>
             * If the timeout is non-zero, the timer is set to expire after the given
             * timeout in milliseconds. When the timer expires, the dispatch function
             * set with wl_event_loop_add_timer() is called once from
             * wl_event_loop_dispatch(). If another dispatch is desired after another
             * expiry, wl_event_source_timer_update() needs to be called again.
             * <p>
             * Wraps wl_event_source_timer_update().
             *
             * @param msDelay The timeout in milliseconds.
             * @return true on success, false on failure.
             */
            public native boolean timerUpdate(int msDelay);

            /** Handle for native instance pointer */
            private long peer;
        }

        /**
         /** Instantiation of this class is not permitted.
         * <p>
         * If you want to create EventLoop you should
         * use {@link #create}
         * <p>
         * This creates a new event loop context. Initially this context is empty.
         * Event sources need to be explicitly added to it.
         * <p>
         * Normally the event loop is run by calling {@link #dispatch} in
         * a loop until the program terminates. Alternatively, an event loop can be
         * embedded in another event loop by its file descriptor, see {@link #getFd}.
         */
        private EventLoop() {}

        /** Native instance allocator.
         * <p>
         * This creates a new event loop context. Initially this context is empty.
         * Event sources need to be explicitly added to it.
         * <p>
         * Wraps wl_event_loop_create()
         */
        public static native EventLoop create();

        /** Destroy an event loop context
         * <p>
         * This emits the event loop destroy signal, closes the event loop file
         * descriptor, and frees loop.
         * <p>
         * If the event loop has existing sources, those cannot be safely removed
         * afterwards. Therefore one must call {@link EventSource#remove()} on all
         * event sources before destroying the event loop context.
         *
         * <p>
         * Wraps wl_event_loop_destroy().
         */
        public native void destroy();

        /** Create a file descriptor event source
         * <p>
         * The given file descriptor is initially watched for the events given in
         * mask. This can be changed as needed with {@link EventSource#fdUpdate}.
         * <p>
         * If it is possible that program execution causes the file descriptor to be
         * read while leaving the data in a buffer without actually processing it,
         * it may be necessary to register the file descriptor source to be re-checked,
         * see {@link EventSource#check}. This will ensure that the dispatch function
         * gets called even if the file descriptor is not readable or writable
         * anymore. This is especially useful with IPC libraries that automatically
         * buffer incoming data, possibly as a side effect of other operations.
         * <p>
         * Wraps wl_event_loop_add_fd().
         *
         * @param fd The file descriptor to watch.
         * @param mask A bitwise-or of which events to watch for: WL_EVENT_READABLE, WL_EVENT_WRITABLE.
         * @param callback The file descriptor dispatch callback.
         * @return A new file descriptor event source.
         * @see FdCallback
         */
        public native EventSource addFd(int fd, int mask, FdCallback callback);

        /** Create a timer event source.
         * <p>
         * Wraps wl_event_loop_add_timer() with following wl_event_source_timer_update() call.
         *
         * @param msDelay The timeout in milliseconds.
         * @param callback The timer dispatch callback.
         * @return A new timer event source.
         * @see TimerCallback
         */
        public native EventSource addTimer(int msDelay, TimerCallback callback);

        /** Create a POSIX signal event source.
         * <p>
         * This function blocks the normal delivery of the given signal in the calling
         * thread, and creates a "watch" for it. Signal delivery no longer happens
         * asynchronously, but by {@link #dispatch} calling the dispatch
         * callback function func.
         * <p>
         * It is the caller's responsibility to ensure that all other threads have
         * also blocked the signal.
         * <p>
         * Wraps wl_event_loop_add_signal().
         *
         * @param signalNumber Number of the signal to watch for.
         * @param callback The signal dispatch callback.
         * @return A new signal event source.
         * @see SignalCallback
         */
        public native EventSource addSignal(int signalNumber, SignalCallback callback);

        /** Create an idle task.
         * <p>
         * Idle tasks are dispatched before {@link #dispatch} goes to sleep.
         * See {@link #dispatch} for more details.
         * <p>
         * Idle tasks fire once, and are automatically destroyed right after the
         * callback function has been called.
         * <p>
         * An idle task can be cancelled before the callback has been called by
         * {@link EventSource#remove()}. Calling {@link EventSource#remove()} after or from
         * within the callback results in undefined behaviour.
         * <p>
         * Wraps wl_event_loop_add_idle().
         *
         * @param callback The idle task dispatch callback.
         * @return A new idle task (an event source).
         * @see IdleCallback
         */
        public native EventSource addIdle(IdleCallback callback);

        /** Wait for events and dispatch them.
         * <p>
         * All the associated event sources are polled. This function blocks until
         * any event source delivers an event (idle sources excluded), or the timeout
         * expires. A timeout of -1 disables the timeout, causing the function to block
         * indefinitely. A timeout of zero causes the poll to always return immediately.
         * <p>
         * All idle sources are dispatched before blocking. An idle source is destroyed
         * when it is dispatched. After blocking, all other ready sources are
         * dispatched. Then, idle sources are dispatched again, in case the dispatched
         * events created idle sources. Finally, all sources marked with
         * {@link EventSource#check()} are dispatched in a loop until their dispatch
         * functions all return zero.
         * <p>
         * Wraps wl_event_loop_dispatch().
         *
         * @param timeout The polling timeout in milliseconds.
         * @return true for success, false for polling (or timer update) error.
         */
        public native boolean dispatch(int timeout);

        /** Dispatch the idle sources.
         * <p>
         * Wraps wl_event_loop_dispatch_idle().
         *
         * @see #addIdle
         */
        public native void dispatchIdle();

        /** Get the event loop file descriptor.
         * <p>
         * This function returns the aggregate file descriptor, that represents all
         * the event sources (idle sources excluded) associated with the given event
         * loop context. When any event source makes an event available, it will be
         * reflected in the aggregate file descriptor.
         * <p>
         * When the aggregate file descriptor delivers an event, one can call
         * wl_event_loop_dispatch() on the event loop context to dispatch all the
         * available events.
         * <p>
         * Wraps wl_event_loop_get_fd()
         *
         * @return The aggregate file descriptor.
         */
        public native int getFd();

        /** Register a destroy listener for an event loop context
         * <p>
         * Wraps wl_event_loop_add_destroy_listener()
         *
         * @param callback The callback to be called.
         * @see DestroyCallback
         */
        public native void addDestroyCallback(DestroyCallback<EventLoop> callback);

        /** Removes given callback from client's destroy callbacks list.
         * <p>
         * Not a part of Wayland API.
         *
         * @param callback Callback object to be removed from the list.
         * @see DestroyCallback
         * @see #addDestroyCallback
         * @see #haveDestroyCallback
         */
        public native void removeDestroyCallback(DestroyCallback<EventLoop> callback);

        /** Checks if given callback object is present in client's destroy callbacks list.
         * <p>
         * Not a part of Wayland C API.
         *
         * @param callback Callback object to be checked.
         * @return true if it is present in the list. false if it is not.
         */
        public native boolean haveDestroyCallback(DestroyCallback<EventLoop> callback);

        /** Handle for some user-defined data */
        public Object data;

        /** Handle for native instance pointer */
        private long peer;

        static {
            System.load(libName);
        }
    }

    /** Java representation of wl_global */
    public static class Global {
        /**
         * Holds a callback to be used with {@link Display#createGlobal(Interface, BindCallback)}
         * and {@link Display#createGlobal(Interface, int, BindCallback)}
         */
        public interface BindCallback {
            /** Modified wl_global_bind_func_t prototype.
             *
             * @param res The instance of new resource.
             */
            void invoke(Resource res);
        }

        /** A filter callback for Global objects
         * <p>
         * A filter callback enables the server to decide which globals to
         * advertise to each client.
         * <p>
         * When a Global filter is set, the given callback will be
         * invoked during Global advertisement and binding.
         * <p>
         * Implements wl_display_global_filter_func_t.
         */
        public interface FilterCallback {
            /** Modified wl_display_global_filter_func_t prototype
             *
             * @param client The client object
             * @param global The global object to show or hide
             * @return true if the global object should be made
             * visible to the client or false otherwise.
             */
            boolean filter(Client client, Global global);
        }

        /** Instantiation of this object is not permitted
         *
         * @see Display#createGlobal
         */
        private Global() {}

        /** Remove the global
         * <p>
         * Broadcast a global remove event to all clients without destroying the
         * global. This function can only be called once per global.
         * <p>
         * wl_global_destroy() removes the global and immediately destroys it. On
         * the other end, this function only removes the global, allowing clients
         * that have not yet received the global remove event to continue to bind to
         * it.
         * <p>
         * This can be used by compositors to mitigate clients being disconnected
         * because a global has been added and removed too quickly. Compositors can call
         * wl_global_remove(), then wait an implementation-defined amount of time, then
         * call wl_global_destroy(). Note that the destruction of a global is still
         * racy, since clients have no way to acknowledge that they received the remove
         * event.
         * <p>
         * Wraps wl_global_remove().
         *
         * @see #destroy
         */
        public native void remove();

        /** Remove the global and immediately destroy it.
         * <p>
         * Wraps wl_global_destroy().
         *
         * @see #remove
         */
        public native void destroy();

        /** Get interface associated with this Global
         * <p>
         * Wraps wl_global_get_interface().
         *
         * @return The interface object the global is associated with.
         */
        public native Interface getInterface();

        /** Get the version of the given global.
         * <p>
         * Wraps wl_global_get_version().
         *
         * @return The version advertised by the global.
         */
        public native int getVersion();

        /** Get the display object for the given global
         * <p>
         * Wraps wl_global_get_display().
         *
         * @return The display object the global is associated with.
         */
        public native Display getDisplay();

        /** Handle for some user-defined data */
        public Object data;

        /** Handle for native instance pointer */
        private long peer;

        static {
            System.load(libName);
        }
    }

    /** Java representation of wl_resource */
    public static class Resource {

        /** Prototype for Resource implementation callbacks.
         *
         * @see #setCallbacks
         */
        public interface Callbacks {
            /** Prototype for any exception can be thrown by Callbacks instances.
             * Exceptions are rethrowed by {@link #postError} in runtime.
             *
             * @see #postError
             */
            class ImplementationException extends RuntimeException {
                /* Code of error. */
                final int code;
                /* Message of error. */
                final String message;
                /* Default constructor. */
                ImplementationException(int code, String message) {
                    super(message);
                    this.message = message;
                    this.code = code;
                }
            }
        }

        /** Instantiation of this object is not permitted
         *
         * @see Client#createResource
         */
        private Resource() {}

        /** Queue event to client's buffer and flush it.
         * <p>
         * Wraps wl_resource_post_event_array().
         *
         * @param opcode Opcode of event
         * @param args Arguments to be dispatched with event
         */
        public native void postEvent(int opcode, Object ...args);

        /** Queue event to client's buffer but not flush it.
         * <p>
         * Wraps wl_resource_queue_event_array().
         *
         * @param opcode Opcode of event
         * @param args Arguments to be dispatched with event
         */
        public native void queueEvent(int opcode, Object ...args);

        /** Send error to client.
         * <p>
         * Wraps wl_resource_post_error().
         *
         * @param code The code of error.
         * @param msg The message explaining the error.
         */
        public native void postError(int code, String msg);

        /** Send memory allocating error to client.
         * <p>
         * Wraps wl_resource_post_no_memory().
         */
        public native void postNoMemory();

        /** Add a Callback interface for this holder.
         * <p>
         * There can be only Callbacks interface associated with a resource.
         * <p>
         * Wraps wl_resource_set_implementation().
         *
         * @param implementation The new Callbacks interface.
         * @see Callbacks
         */
        public native void setCallbacks(Callbacks implementation);

        /** Destroy Wayland resource object.
         * <p>
         *  Wraps wl_resource_destroy().
         *
         * @see #addDestroyCallback
         */
        public native void destroy();

        /** Get the id of this resource.
         * <p>
         * Wraps wl_resource_get_id().
         *
         * @return The id this resource is associated with.
         */
        public native int getId();

        /** Get the client this resource is associated with.
         * <p>
         * Wraps wl_resource_get_client().
         *
         * @return The client this resource is associated with.
         */
        public native Client getClient();

        /** Get version this resource is associated with.
         * <p>
         * Wraps wl_resource_get_version().
         *
         * @return The version this resource is associated with.
         */
        public native int getVersion();

        /** Set given callback as resource's destruct callback.
         * <p>
         * Unlike {@link #addDestroyCallback} callback set by this
         * function is dispatched last, right before resource's destruction
         * <p>
         * Wraps wl_resource_set_destructor().
         *
         * @param callback Callback object to be added to the list.
         * @see #addDestroyCallback
         * @see DestroyCallback
         */
        public native void setDestructor(DestroyCallback<Resource> callback);

        /** Get class this resource is associated with.
         * <p>
         * Wraps wl_resource_get_version().
         *
         * @return The class this resource is associated with.
         */
        public native String getWaylandClass();

        /** Adds given callback to resource's destroy callbacks list.
         * <p>
         * Wraps wl_display_add_destroy_listener().
         *
         * @param callback Callback object to be added to the list.
         * @see #setDestructor
         * @see DestroyCallback
         * @see #removeDestroyCallback
         * @see #haveDestroyCallback
         */
        public native void addDestroyCallback(DestroyCallback<Resource> callback);

        /** Removes given callback from resource's destroy callbacks list.
         * <p>
         * Not a part of Wayland API.
         *
         * @param callback Callback object to be removed from the list.
         * @see DestroyCallback
         * @see #addDestroyCallback
         * @see #haveDestroyCallback
         */
        public native void removeDestroyCallback(DestroyCallback<Resource> callback);

        /** Checks if given callback object is present in resource's destroy callbacks list.
         * <p>
         * Not a part of Wayland C API.
         *
         * @param callback Callback object to be checked.
         * @return true if it is present in the list. false if it is not.
         */
        public native boolean haveDestroyCallback(DestroyCallback<Resource> callback);

        /** Handle for some user-defined data */
        public Object data;

        /** Handle for native instance pointer */
        private long peer;

        static {
            System.load(libName);
        }
    }

    /** Protocol object interface
     * <p>
     * An Interface describes the API of a protocol object defined in the Wayland
     * protocol specification. The protocol implementation uses a wl_interface
     * (Interface native representation) within its marshalling machinery for
     * encoding client requests.
     * <p>
     * Wraps wl_interface struct.
     */
    public static class Interface {

        /** Protocol message signature
         * <p>
         * A Message describes the signature of an actual protocol message, such as a
         * request or event, that adheres to the Wayland protocol wire format. The
         * protocol implementation uses a Message within its demarshal machinery for
         * decoding messages between a compositor and its clients. In a sense, a
         * wl_message is to a protocol message like a class is to an object.
         * <p>
         * The `name` of a Message is the name of the corresponding protocol message.
         * <p>
         * The `signature` is an ordered list of symbols representing the data types
         * of message arguments and, optionally, a protocol version and indicators for
         * nullability. A leading integer in the `signature` indicates the `since`
         * version of the protocol message. A `?` preceding a data type symbol indicates
         * that the following argument type is nullable. While it is a protocol violation
         * to send messages with non-nullable arguments set to `null`, event handlers in
         * clients might still get called with non-nullable object arguments set to
         * `null`. This can happen when the client destroyed the object being used as
         * argument on its side and an event referencing that object was sent before the
         * server knew about its destruction. As this race cannot be prevented, clients
         * should - as a general rule - program their event handlers such that they can
         * handle object arguments declared non-nullable being `null` gracefully.
         * <p>
         * When no arguments accompany a message, `signature` is an empty string.
         * <p>
         * Symbols:
         * <p>
         * * `i`: int
         * * `u`: uint
         * * `f`: fixed
         * * `s`: string
         * * `o`: object
         * * `n`: new_id
         * * `a`: array
         * * `h`: fd
         * * `?`: following argument (`o` or `s`) is nullable
         * <p>
         * While demarshaling primitive arguments is straightforward, when demarshaling
         * messages containing `object` or `new_id` arguments, the protocol
         * implementation often must determine the type of the object. The `types` of a
         * Message is an array of Interface references that correspond to `o` and
         * `n` arguments in `signature`.
         * <p>
         * Consider the protocol event wl_display `delete_id` that has a single `uint`
         * argument. The Message is:
         *
         * <pre>{@code
         * new Message("delete_id", "u", new Class<?>[] { Integer.class })
         * }</pre>
         *
         * Here, the message `name` is `"delete_id"`, the `signature` is `"u"`, and the
         * argument `types` is `[Integer.class]`. Note that primitive types such as
         * Integer or String has no corresponding Interface since it is a primitive argument.
         * <p>
         * Wraps wl_message struct.
         */
        @SuppressWarnings("FieldCanBeLocal")
        static public class Message {
            /** The name of the corresponding protocol message. */
            private final String name;
            /** The name of the corresponding function. */
            private final String function;
            /** The signature of the corresponding protocol message. */
            private final String signature;
            /** The types of the corresponding protocol message. */
            private final Interface[] types;

            /** Default constructor
             *
             * @param name The name of the corresponding protocol message.
             * @param function The name of the corresponding function.
             * @param signature The signature of the corresponding protocol message.
             * @param types The types of the corresponding protocol message.
             */
            public Message(String name, String function, String signature, Interface[] types) {
                this.name = name;
                this.function = function;
                this.signature = signature;
                this.types = types;
            }
        }

        /** Instantiation of this object is not permitted
         *
         * @see #create
         */
        private Interface(){}

        /** Create native instance of Interface and return a handle.
         * <p>
         * Used only by automatically generated code, should not be touched by user.
         * Not a part of Wayland C API.
         *
         * @param name Interface's name.
         * @param version Interface's version.
         * @param events Events can be produced by this interface.
         * @param requests Requests can be processed by this interface.
         * @return New Interface instance handle.
         */
        public static native Interface create(String name, int version, Message[] requests, Message[] events, Class<?> callbacks);

        /** Get native instance handle of predefined Interface.
         * <p>
         * Some Interfaces, such as ShmBuffer and Callback are predefined and should be used as is
         *
         * @param name Interface's name.
         * @return Interface instance handle.
         */
        public static native Interface get(String name);

        /** Handle for native instance pointer */
        private long peer;

        static {
            System.load(libName);
        }
    }

    public static class ShmBuffer {
        public native int getStride();
        public native ByteBuffer getData();
        public native int getFormat();
        public native int getWidth();
        public native int getHeight();
        public native Resource getResource();
        public static native ShmBuffer fromResource(Resource res);

        /** Handle for some user-defined data */
        public Object data;

        /** Handle for native instance pointer */
        private long peer;

        public static final Interface iface = Interface.get("ShmBuffer");
    }

    public static class Callback {
        public static native void sendDone(Client client, int id, int serial);
        public static native void destroy(Client client, int id);

        public static final Interface iface = Interface.get("Callback");
    }

    /** Java representation of wl_fixed_t */
    public static class Fixed {
        private final int raw;
        private Fixed(int raw) { this.raw = raw; }
        public static Fixed createRaw(int raw) { return new Fixed(raw); }
        public static Fixed create(int value) { return new Fixed(value<<8); }
        public static Fixed create(float value) { return new Fixed((int) (value * 256 + 0.5)); }
        public int intValue() { return raw >> 8; }
        public float floatValue() { return raw / 256.0f; }
        public int rawValue() { return raw; }
    }

    /** Some method... */
    public static native void print();

}
