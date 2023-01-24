package org.freedesktop;

import org.freedesktop.protocols.Wayland.*;
import org.freedesktop.protocols.XdgShell.*;

import java.nio.ByteBuffer;

public class App {
    private final static String XWAYLAND_IS_ALREADY_RUNNING =
            "Xwayland is already running here. You can use -kill option to restart compositor.";

    public static void main(String[] args) {
        new App().main();
    }

    LorieRenderer renderer = new LorieRenderer();
    GLSurfaceView view = new GLSurfaceView(null);

    class ClientData {
        Pointer pointer = new Pointer();
        Keyboard keyboard = new Keyboard();
        Touch touch = new Touch();
        LorieSeat seat = new LorieSeat();
        Output output = new Output();
    }

    ClientData data() {
        return (ClientData) Wayland.currentClient().data;
    }

    void main() {
        renderer.setView(view);

        Wayland.Display display = Wayland.Display.create();
        display.addSocketAuto();
        display.initShm();

        display.addClientCreatedCallback((client) -> client.data = new ClientData());

        display.createGlobal(LorieCompositor.iface, (id) -> new LorieCompositor(id, renderer));
        display.createGlobal(LorieWmBase.iface, (id) -> new LorieWmBase(id, renderer));
        display.createGlobal(Output.iface, (res) -> {
            res.setCallbacks(new Output.Callbacks() { @Override public void release() { res.destroy(); }});
            data().output.instance = res;
            reportMode();
        });
        display.createGlobal(LorieSeat.iface, (res) -> {
            LorieSeat seat = data().seat;
            seat.instance = res;
            res.setCallbacks(seat);
            seat.capabilities(Seat.Capability.POINTER.value | Seat.Capability.KEYBOARD.value /*|
                                    Seat.Capability.TOUCH.value*/);
            seat.name("default");
        });

        display.run();
    }

    void reportMode() {
        Output output = data().output;
        output.geometry(0, 0, 270, 158, 0, "weston-X11", "none", 0);
        output.scale(1);
        output.mode(3, 1024, 600, 60000);
        output.done();
    }

    class LorieSeat extends Seat implements Seat.Callbacks {
        @Override public void getPointer(Wayland.Resource id) {
            data().pointer.instance = id;
            id.setCallbacks(new Pointer.Callbacks() {
                @Override
                public void release() {
                    id.destroy();
                }

                @Override
                public void setCursor(int serial, Wayland.Resource surface, int hotspot_x, int hotspot_y) {
                    if (surface != null) {
                        LorieSurface sfc = (LorieSurface) surface.data;
                        renderer.setCursor(sfc.width, sfc.height, sfc.data, sfc);
                    } else
                        renderer.setCursor(0, 0, null, null);
                }
            });
        }

        @Override public void getKeyboard(Wayland.Resource id) {
            data().keyboard.instance = id;
            id.setCallbacks(new Keyboard.Callbacks() { @Override public void release() { id.destroy(); }});
        }

        @Override public void getTouch(Wayland.Resource id) {
            data().touch.instance = id;
            id.setCallbacks(new Touch.Callbacks() { @Override public void release() { id.destroy(); }});
        }

        @Override public void release() { if (instance != null) instance.destroy(); }
    }

    static class LorieSurface extends Surface implements Surface.Callbacks {
        int callback = -1;
        int width = 0;
        int height = 0;
        ByteBuffer data = null;
        LorieRenderer renderer;

        LorieSurface(Wayland.Resource id, LorieRenderer renderer) {
            this.renderer = renderer;
            id.data = this;
            id.setCallbacks(this);
            instance = id;
        }

        public XdgSurface xdgSurface = new XdgSurface();
        @Override
        public void attach(Wayland.Resource buffer, int x, int y) {
            Wayland.ShmBuffer buf = Wayland.ShmBuffer.fromResource(buffer);
            if (buf == null) {
                width = height = 0;
                data = null;
                return;
            }

            width = buf.getWidth();
            height = buf.getHeight();
            data = buf.getData();
            renderer.reinit(width, height, data, this);
            buffer.addDestroyCallback((res) -> {
                // App will segfault if server free pixmap while renderer still wants to draw it or draws it at the moment
                // So we simply wait for the moment renderer finished drawings
                if (renderer.uses(data)) {
                    renderer.queueAndWait(() -> {});
                    renderer.clear(LorieSurface.this);
                    renderer.commitScreen();
                }
            });
        }

        @Override
        public void frame(int callback) {
            this.callback = callback;
        }

        @Override
        public void commit() {
            renderer.commitScreen();
            // TODO: make this work in callback to working thread
            Wayland.Callback.sendDone(Wayland.currentClient(), callback, 1);
            Wayland.Callback.destroy(Wayland.currentClient(), callback);
        }

        @Override
        public void setOpaqueRegion(Wayland.Resource region) {
            if (xdgSurface != null)
                xdgSurface.configure(xdgSurface.instance.getId());
        }

        @Override public void destroy() {
            if (instance != null) instance.destroy();
            if (renderer != null) {
                renderer.clear(this);
            }
        }
    }

    static class LorieCompositor extends Compositor implements Compositor.Callbacks {
        private final LorieRenderer renderer;
        LorieCompositor(Wayland.Resource id, LorieRenderer renderer) {
            this.renderer = renderer;
            id.setCallbacks(this);
        }
        public void createSurface(Wayland.Resource id) {
            id.data = new LorieSurface(id, renderer);
        }
        public void createRegion(Wayland.Resource id) {
            id.setCallbacks(new Region.Callbacks() {
                @Override public void destroy() { id.destroy(); }
            });
        }
    }

    static class LorieWmBase extends XdgWmBase implements XdgWmBase.Callbacks {
        private final LorieRenderer renderer;
        LorieWmBase(Wayland.Resource id, LorieRenderer renderer) {
            this.renderer = renderer;
            instance = id;
            id.data = this;
            id.setCallbacks(this);
        }
        @Override
        public void getXdgSurface(Wayland.Resource id, Wayland.Resource surface) {
            LorieSurface sfc = ((LorieSurface) surface.data);
            sfc.xdgSurface.instance = id;
            id.setCallbacks(new XdgSurface.Callbacks() {
                @Override public void destroy() { id.destroy(); }
                @Override public void getToplevel(Wayland.Resource toplevel) {
                    if (renderer.hasToplevel()) {
                        Wayland.currentClient().postImplementationError(XWAYLAND_IS_ALREADY_RUNNING);
                        return;
                    }

                    renderer.setScreen(sfc.width, sfc.height, sfc.data, sfc);
                    renderer.commitScreen();
                    toplevel.setCallbacks(new XdgToplevel.Callbacks() {
                        @Override public void destroy() {
                            toplevel.destroy();
                        }
                    });
                }
            });
        }

        @Override public void destroy() { instance.destroy(); }
    }

}
