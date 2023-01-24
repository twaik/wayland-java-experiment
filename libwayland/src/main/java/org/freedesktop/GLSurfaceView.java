package org.freedesktop;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengles.GLES;
import org.lwjgl.system.MemoryStack;

import javax.naming.Context;
import javax.print.attribute.AttributeSet;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.egl.EGL10.eglGetError;
import static org.lwjgl.egl.EGL10.eglInitialize;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeEGL.glfwGetEGLDisplay;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings({"GrazieInspection", "SpellCheckingInspection", "unused"})
public class GLSurfaceView {
    private final static String TAG = "GLSurfaceView";
    private final static boolean LOG_ATTACH_DETACH = true;
    private final static boolean LOG_THREADS = true;
    private final static boolean LOG_PAUSE_RESUME = true;
    private final static boolean LOG_SURFACE = true;
    private final static boolean LOG_RENDERER = true;
    private final static boolean LOG_RENDERER_DRAW_FRAME = true;
    private final static boolean LOG_EGL = true;
    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    public final static int DEBUG_CHECK_GL_ERROR = 1;
    public final static int DEBUG_LOG_GL_CALLS = 2;

    public GLSurfaceView(Context context, AttributeSet attrs) {
    }

    public GLSurfaceView(Context context) {
    }

    public void setDebugFlags(int debugFlags) {
        mDebugFlags = debugFlags;
    }
    public int getDebugFlags() {
        return mDebugFlags;
    }
    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        mPreserveEGLContextOnPause = preserveOnPause;
    }
    public boolean getPreserveEGLContextOnPause() {
        return mPreserveEGLContextOnPause;
    }
    public void setRenderer(Renderer renderer) {
        checkRenderThreadState();
        mRenderer = renderer;
        mGLThread = new GLThread(mThisWeakRef);
        mGLThread.start();
    }
    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
    }
    public void setRenderMode(int renderMode) {
        mGLThread.setRenderMode(renderMode);
    }
    public int getRenderMode() {
        return mGLThread.getRenderMode();
    }
    public void requestRender() {
        mGLThread.requestRender();
    }
    public void queueEvent(Runnable r) {
        mGLThread.queueEvent(r);
    }
    protected void onAttachedToWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow reattach =" + mDetached);
        }
        if (mDetached && (mRenderer != null)) {
            int renderMode = RENDERMODE_CONTINUOUSLY;
            if (mGLThread != null) {
                renderMode = mGLThread.getRenderMode();
            }
            mGLThread = new GLThread(mThisWeakRef);
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                mGLThread.setRenderMode(renderMode);
            }
            mGLThread.start();
        }
        mDetached = false;
    }
    // ----------------------------------------------------------------------

    public interface Renderer {
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onDrawFrame();
    }

    static class GLThread extends Thread {
        long window;
        static final int WIDTH  = 1024;
        static final int HEIGHT = 600;
        GLThread(WeakReference<GLSurfaceView> glSurfaceViewWeakRef) {
            super();
            mWidth = 0;
            mHeight = 0;
            mRequestRender = true;
            mRenderMode = RENDERMODE_CONTINUOUSLY;
            mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
        }
        private void initGlfw() {
            //GLFWErrorCallback.createPrint().set();
            if (!glfwInit())
                throw new IllegalStateException("Unable to initialize glfw");

            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

            // GLFW setup for EGL & OpenGL ES
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

            window = glfwCreateWindow(WIDTH, HEIGHT, "GLFW EGL/OpenGL ES Demo", NULL, NULL);
            if (window == NULL)
                throw new RuntimeException("Failed to create the GLFW window");

            glfwSetKeyCallback(window, (windowHnd, key, scancode, action, mods) -> {
                if (action == GLFW_RELEASE && key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(windowHnd, true);
                }
            });
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

            // EGL capabilities
            long dpy = glfwGetEGLDisplay();

            try (MemoryStack stack = stackPush()) {
                IntBuffer major = stack.mallocInt(1);
                IntBuffer minor = stack.mallocInt(1);

                if (!eglInitialize(dpy, major, minor)) {
                    throw new IllegalStateException(String.format("Failed to initialize EGL [0x%X]", eglGetError()));
                }
            }

            // OpenGL ES capabilities
            glfwMakeContextCurrent(window);
            GLES.createCapabilities();

            // Render with OpenGL ES
            glfwShowWindow(window);
            surfaceCreated();
            onWindowResize(WIDTH, HEIGHT);
        }
        void pollEvents() {
            glfwPollEvents();
        }
        boolean checkSizeChanged() {
            if (mSizeChanged)
                return true;
            boolean changed;
            int[] width = new int[1];
            int[] height = new int[1];
            glfwGetWindowSize(window, width, height);
            changed = (width[0] != mWidth) || (height[0] != mHeight);
            if (changed) {
                mWidth = width[0];
                mHeight = height[0];
            }
            return changed;
        }
        void swapBuffers() {
            glfwSwapBuffers(window);
        }
        void destroyWindow() {
            GLES.setCapabilities(null);

            glfwFreeCallbacks(window);
            glfwTerminate();
        }

        @Override
        public void run() {
            setName("GLThread " + getId());
            if (LOG_THREADS) {
                Log.i("GLThread", "starting tid=" + getId());
            }
            try {
                guardedRun();
            } catch (InterruptedException e) {
                // fall thru and exit normally
            } finally {
                sGLThreadManager.threadExiting(this);
            }
        }
        private void guardedRun() throws InterruptedException {
            initGlfw();
            try {
                Runnable event = null;
                Runnable finishDrawingRunnable = null;
                while (!glfwWindowShouldClose(window)) {
                    synchronized (sGLThreadManager) {
                        pollEvents();
                        while (true) {
                            if (! mEventQueue.isEmpty()) {
                                event = mEventQueue.remove(0);
                                break;
                            }
                            if (mFinishDrawingRunnable != null) {
                                finishDrawingRunnable = mFinishDrawingRunnable;
                                mFinishDrawingRunnable = null;
                            }
                            mSizeChanged = checkSizeChanged();
                            // Ready to draw?
                            if (readyToDraw()) {
                                sGLThreadManager.notifyAll();
                                break;
                            } else {
                                if (finishDrawingRunnable != null) {
                                    Log.w(TAG, "Warning, !readyToDraw() but waiting for " +
                                            "draw finished! Early reporting draw finished.");
                                    finishDrawingRunnable.run();
                                    finishDrawingRunnable = null;
                                }
                            }

                            // By design, this is the only place in a GLThread thread where we wait().
                            if (LOG_THREADS) {
                                Log.i("GLThread", "waiting tid=" + getId()
                                        + " mWidth: " + mWidth
                                        + " mHeight: " + mHeight
                                        + " mRequestRender: " + mRequestRender
                                        + " mRenderMode: " + mRenderMode);
                            }
                            sGLThreadManager.wait();
                        }
                    } // end of synchronized(sGLThreadManager)
                    if (event != null) {
                        event.run();
                        event = null;
                        continue;
                    }
                    if (mSizeChanged) {
                        GLSurfaceView view = mGLSurfaceViewWeakRef.get();
                        if (view != null) {
                            view.mRenderer.onSurfaceChanged(mWidth, mHeight);
                        }
                        mSizeChanged = false;
                    }
                    if (LOG_RENDERER_DRAW_FRAME) {
                        Log.w("GLThread", "onDrawFrame tid=" + getId());
                    }

                    if (mRequestRender || mRenderMode == RENDERMODE_CONTINUOUSLY){
                        GLSurfaceView view = mGLSurfaceViewWeakRef.get();
                        if (view != null) {
                            view.mRenderer.onDrawFrame();
                            mRequestRender = false;
                            if (finishDrawingRunnable != null) {
                                finishDrawingRunnable.run();
                                finishDrawingRunnable = null;
                            }
                        }
                    }
                    swapBuffers();
                }
            } finally {
                synchronized (sGLThreadManager) {
                    destroyWindow();
                }
            }
        }
        public boolean ableToDraw() {
            return readyToDraw();
        }
        private boolean readyToDraw() {
            return (mWidth > 0) && (mHeight > 0)
                    && (mRequestRender || (mRenderMode == RENDERMODE_CONTINUOUSLY));
        }
        public void setRenderMode(int renderMode) {
            if ( !((RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= RENDERMODE_CONTINUOUSLY)) ) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized(sGLThreadManager) {
                mRenderMode = renderMode;
                sGLThreadManager.notifyAll();
            }
        }
        public int getRenderMode() {
            synchronized(sGLThreadManager) {
                return mRenderMode;
            }
        }
        public void requestRender() {
            synchronized(sGLThreadManager) {
                mRequestRender = true;
                sGLThreadManager.notifyAll();
            }
        }
        public void requestRenderAndNotify(Runnable finishDrawing) {
            synchronized(sGLThreadManager) {
                // If we are already on the GL thread, this means a client callback
                // has caused reentrancy, for example via updating the SurfaceView parameters.
                // We will return to the client rendering code, so here we don't need to
                // do anything.
                if (Thread.currentThread() == this) {
                    return;
                }
                mRequestRender = true;
                mRenderComplete = false;
                mFinishDrawingRunnable = finishDrawing;
                sGLThreadManager.notifyAll();
            }
        }
        public void surfaceCreated() {
            synchronized(sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceCreated tid=" + getId());
                }
                sGLThreadManager.notifyAll();
                GLSurfaceView view = mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mRenderer.onSurfaceCreated();
                }
            }
        }
        public void surfaceDestroyed() {
            synchronized(sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceDestroyed tid=" + getId());
                }
                sGLThreadManager.notifyAll();
            }
        }
        public void onWindowResize(int w, int h) {
            synchronized (sGLThreadManager) {
                mWidth = w;
                mHeight = h;
                mSizeChanged = true;
                mRequestRender = true;
                mRenderComplete = false;
                // If we are already on the GL thread, this means a client callback
                // has caused reentrancy, for example via updating the SurfaceView parameters.
                // We need to process the size change eventually though and update our EGLSurface.
                // So we set the parameters and return so they can be processed on our
                // next iteration.
                if (Thread.currentThread() == this) {
                    return;
                }
                sGLThreadManager.notifyAll();
                // Wait for thread to react to resize and render a frame
                while (!mRenderComplete && ableToDraw()) {
                    if (LOG_SURFACE) {
                        Log.i("Main thread", "onWindowResize waiting for render complete from tid=" + getId());
                    }
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            synchronized(sGLThreadManager) {
                mEventQueue.add(r);
                sGLThreadManager.notifyAll();
            }
        }
        // Once the thread is started, all accesses to the following member
        // variables are protected by the sGLThreadManager monitor
        private int mWidth;
        private int mHeight;
        private int mRenderMode;
        private boolean mRequestRender;
        private boolean mRenderComplete;
        private final ArrayList<Runnable> mEventQueue = new ArrayList<>();
        private boolean mSizeChanged = true;
        private Runnable mFinishDrawingRunnable = null;
        // End of member variables protected by the sGLThreadManager monitor.
        private final WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;
    }
    static class LogWriter extends Writer {
        @Override public void close() {
            flushBuilder();
        }
        @Override public void flush() {
            flushBuilder();
        }
        @Override public void write(char[] buf, int offset, int count) {
            for(int i = 0; i < count; i++) {
                char c = buf[offset + i];
                if ( c == '\n') {
                    flushBuilder();
                }
                else {
                    mBuilder.append(c);
                }
            }
        }
        private void flushBuilder() {
            if (mBuilder.length() > 0) {
                Log.v("GLSurfaceView", mBuilder.toString());
                mBuilder.delete(0, mBuilder.length());
            }
        }
        private final StringBuilder mBuilder = new StringBuilder();
    }
    private void checkRenderThreadState() {
        if (mGLThread != null) {
            throw new IllegalStateException(
                    "setRenderer has already been called for this instance.");
        }
    }
    private static class GLThreadManager {
        private static final String TAG = "GLThreadManager";
        public synchronized void threadExiting(GLThread thread) {
            if (LOG_THREADS) {
                Log.i("GLThread", "exiting tid=" +  thread.getId());
            }
            notifyAll();
        }
        public void releaseEglContextLocked(GLThread thread) {
            notifyAll();
        }
    }
    private static final GLThreadManager sGLThreadManager = new GLThreadManager();
    private final WeakReference<GLSurfaceView> mThisWeakRef =
            new WeakReference<>(this);
    private GLThread mGLThread;
    private Renderer mRenderer;
    private boolean mDetached;
    private int mDebugFlags;
    private boolean mPreserveEGLContextOnPause;
}