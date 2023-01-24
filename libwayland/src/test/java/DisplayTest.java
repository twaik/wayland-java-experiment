import org.junit.Test;
import org.freedesktop.Wayland;

import java.io.IOException;
import java.util.UUID;

public class DisplayTest {
  void setTempSocket() {
    setenv("WAYLAND_SOCKET","wayland-test-" + ProcessHandle.current().pid() + "-" + UUID.randomUUID(), true);
  }

  @Test
  public void displayDestroyCallback() {
    class DestroyCallback implements Wayland.DestroyCallback<Wayland.Display> {
      boolean done = false;
      @Override public void invoke(Wayland.Display object) {done = true;}
    }

    Wayland.Display display = Wayland.Display.create();
    DestroyCallback a = new DestroyCallback(), b = new DestroyCallback();

    display.addDestroyCallback(a);
    display.addDestroyCallback(b);

    display.removeDestroyCallback(a);

    display.destroy();

    assert !a.done;
    assert b.done;
  }

  @Test
  public void clientDestroyCallback() {
    class DestroyCallback implements Wayland.DestroyCallback<Wayland.Client> {
      @Override public void invoke(Wayland.Client client) { client.getDisplay().terminate(); }
    }

    Wayland.Display display = Wayland.Display.create();

    int[] pair = socketpair();
    Wayland.Client client = display.createClient(pair[0]);
    client.addDestroyCallback(new DestroyCallback());
    (new Thread(() -> {
      sleep(1000);
      close(pair[1]);
    })).start();
    display.run();
    close(pair[0]);
    display.destroy();
  }

  void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
  static native int[] socketpair();
  static native void close(int fd);
  static native void print(String s);
  static native void setenv(String var, String val, boolean overwrite);
  static {
    System.loadLibrary("waylandTest");
  }
}
