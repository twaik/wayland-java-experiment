package org.freedesktop;

import static org.lwjgl.opengles.GLES20.*;
import java.nio.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"SameParameterValue", "SpellCheckingInspection"})
public class LorieRenderer implements GLSurfaceView.Renderer {
    GLSurfaceView view = null;
    int gTextureProgram = 0;
    int gvPos = 0;
    int gvCoords = 0;
    static final String gSimpleVS = """
        attribute vec4 position;
        attribute vec2 texCoords;
        varying vec2 outTexCoords;

        void main(void) {
           outTexCoords = texCoords;
           gl_Position = position;
        }
    """;
    static final String gSimpleFS = """
        precision mediump float;

        varying vec2 outTexCoords;
        uniform sampler2D texture;

        void main(void) {
           gl_FragColor = texture2D(texture, outTexCoords).bgra;
        }
    """;

    final Texture screen = new Texture();
    final Cursor cursor = new Cursor();

    public void setView(GLSurfaceView view) {
        if (view == null)
            return;
        this.view = view;
        view.setRenderer(this);
        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setScreen(int width, int height, ByteBuffer data, Object cookie) {
        queue(() -> screen.set(width, height, data, cookie));
    }

    public void setCursor(int width, int height, ByteBuffer data, Object cookie) {
        queue(() -> cursor.set(width, height, data, cookie));
    }

    public void commitScreen() {
        queue(() -> {
            screen.damaged = true;
            if (view != null)
                view.requestRender();
        });
    }

    public boolean uses(ByteBuffer data) {
        return screen.data == data || cursor.data == data;
    }

    public boolean hasToplevel() {
        AtomicBoolean flag = new AtomicBoolean(false);
        queueAndWait(() -> flag.set(screen.valid()));
        return flag.get();
    }

    public void clear(Object cookie) {
        if (view != null)
            view.queueEvent(() -> {
                for (Texture t: new Texture[]{ screen, cursor }) {
                  if (t.cookie == cookie) {
                      t.set(0, 0, null, null);
                   }
                }
        });
    }

    // It is simple. Run async if view is available and run synchronous if not...
    public void queue(Runnable runnable) {
        if (view != null) {
            view.queueEvent(runnable);
        } else
            runnable.run();
    }

    public void queueAndWait(Runnable runnable) {
        AtomicBoolean flag = new AtomicBoolean(false);
        queue(() -> {
            runnable.run();
            flag.set(true);
        });
        while (!flag.get()) {
            try { Thread.sleep(1); } catch (Exception ignored) {}
        }
    }

    public void reinit(int width, int height, ByteBuffer data, Object cookie) {
        queue(() -> {
            for (Texture t: new Texture[]{ screen, cursor }) {
                if (t.cookie == cookie) {
                    t.width = width;
                    t.height = height;
                    t.data = data;
                    t.damaged = true;
                    t.reinit();
                }
            }
        });
    }

    @Override
    public void onSurfaceCreated() {
        //getHolder().setFormat(PixelFormat.RGBA_8888);

        gTextureProgram = Utils.createProgram(gSimpleVS, gSimpleFS);
        if (gTextureProgram != 0) {
            gvPos = glGetAttribLocation(gTextureProgram, "position");
            gvCoords = glGetAttribLocation(gTextureProgram, "texCoords");
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        //WindowManager wm = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE));
        //DisplayMetrics dm = new DisplayMetrics();
        //int mmWidth, mmHeight;
        //wm.getDefaultDisplay().getMetrics(dm);

        //if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        //    mmWidth = (int) Math.round((width * 25.4) / dm.xdpi);
        //    mmHeight = (int) Math.round((height * 25.4) / dm.ydpi);
        //} else {
        //    mmWidth = (int) Math.round((width * 25.4) / dm.ydpi);
        //    mmHeight = (int) Math.round((height * 25.4) / dm.xdpi);
        //}

        //svc.windowChanged(width, height, mmWidth, mmHeight);

        glViewport(0, 0, width, height);
        glClearColor(0.0f, 0.5f, 1.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Log.d("Renderer", "onSurfaceChanged width " + width + " height " + height);
        screen.reinit();
        cursor.reinit();
    }

    //@Override
    public void onDrawFrame() {
        glClearColor(0.0f, 0.0f, 1.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        screen.draw(-1.0f, -1.0f, 1.0f, 1.0f);
        cursor.draw();

        //System.out.println("frameCallback = " + frameCallback);

        //if (frameCallback != null)
        //    frameCallback.run();
    }


    class Cursor extends Texture {
        boolean visible = false;
        PointF coord = new PointF(0, 0);
        PointF hotspot = new PointF(0, 0);

        protected void draw() {
            if (!visible)
                return;

            float x, y, width, height, hs_x, hs_y;
            hs_x = hotspot.x/screen.width*2;
            hs_y = hotspot.y/screen.height*2;
            x = coord.x/screen.width*2 - 1.0f - hs_x;
            y = coord.y/screen.height*2 - 1.0f - hs_y;
            width = 2*((float)cursor.width)/screen.width;
            height = 2*((float)cursor.height)/screen.height;
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            super.draw(x, y, x + width, y + height);
            glDisable(GL_BLEND);
        }
    }

    private class Texture {
        int id = Integer.MAX_VALUE;
        int width = 0;
        int height = 0;
        boolean damaged = false;
        ByteBuffer data = null;
        Object cookie = null;

        public void set(int width, int height, ByteBuffer data, Object cookie) {
            this.width = width;
            this.height = height;
            this.data = data;
            this.damaged = true;
            this.cookie = cookie;
            reinit();
        }

        protected void reinit() {
            if (!Utils.ableToDraw())
                return;

            glActiveTexture(GL_TEXTURE0);
            id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, id);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height,
                    0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

            damaged = true;
            //requestRender();
        }

        protected void draw(float x0, float y0, float x1, float y1) {
            if (!valid() || !Utils.ableToDraw()) {
                return;
            }

            float[] coords = {
                    x0, -y0, 0.f, 0.f, 0.f,
                    x1, -y0, 0.f, 1.f, 0.f,
                    x0, -y1, 0.f, 0.f, 1.f,
                    x1, -y1, 0.f, 1.f, 1.f,
            };

            ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer buffer = bb.asFloatBuffer();
            buffer.put(coords);

            glActiveTexture(GL_TEXTURE0);
            glUseProgram(gTextureProgram);
            glBindTexture(GL_TEXTURE_2D, id);

            if (damaged)
                glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0,
                        width, height, GL_RGBA, GL_UNSIGNED_BYTE, data);

            glVertexAttribPointer(gvPos, 3, GL_FLOAT, false, 20, buffer.position(0));
            glVertexAttribPointer(gvCoords, 2, GL_FLOAT, false, 20, buffer.position(3));
            glEnableVertexAttribArray(gvPos);
            glEnableVertexAttribArray(gvCoords);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);
            damaged = false;
        }

        private boolean valid() {
            return width != 0 && height != 0 && id != Integer.MAX_VALUE && data != null;
        }
    }

    static class Utils {
        private static int loadShader(int shaderType, String source) {
            int shader = glCreateShader(shaderType);
            if (shader != GL_FALSE) {
                glShaderSource(shader, source);
                glCompileShader(shader);
                int[] compiled = new int[1];
                glGetShaderiv(shader, GL_COMPILE_STATUS, compiled);
                if (compiled[0] == GL_FALSE) {
                    String error = glGetShaderInfoLog(shader);
                    System.err.println("Could not compile shader " + shaderType + ": " + error);
                    glDeleteShader(shader);
                    shader = 0;
                }
            }
            return shader;
        }

        @SuppressWarnings("SameParameterValue")
        public static int createProgram(String vertexSource, String fragmentSource) {
            int vertexShader = loadShader(GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }

            int pixelShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == GL_FALSE) {
                glDeleteShader(vertexShader);
                return 0;
            }

            int program = glCreateProgram();
            if (program == 0) {
                glDeleteShader(vertexShader);
                glDeleteShader(pixelShader);
                return 0;
            }

            glAttachShader(program, vertexShader);
            glAttachShader(program, pixelShader);
            glLinkProgram(program);

            int[] linkStatus = new int[1];
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus);
            if (linkStatus[0] != GL_TRUE) {
                String error = glGetProgramInfoLog(program);
                System.err.println("Could not link program: " + error);
                glDeleteShader(vertexShader);
                glDeleteShader(pixelShader);
                glDeleteProgram(program);
                program = 0;
            }
            return program;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private static boolean ableToDraw() {
            return true;
        }
    }
}
