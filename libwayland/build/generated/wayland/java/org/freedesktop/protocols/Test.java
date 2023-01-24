// AUTO-GENERATED -- DO NOT EDIT
package org.freedesktop.protocols;

import org.freedesktop.Wayland.*;
import static org.freedesktop.Wayland.Interface.Message;
import org.freedesktop.protocols.XdgShell.*;
import org.freedesktop.protocols.Wayland.*;

@SuppressWarnings({"ALL"})
public class Test {
  public static class Pingpong {
    public interface Callbacks extends Resource.Callbacks {
      public default void ping(String message) {};
    }

    public void pong(String message) {
      if (instance != null)
        instance.postEvent(0, message);
    }

    public Pingpong() {}

    public Pingpong(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("pingpong", 1, new Message[] /* requests */ {
      new Message("ping", "ping", "s", new Interface[] { null })
    }, new Message[] /* events */ {
      new Message("pong", "pong", "s", new Interface[] { null })
    }, Callbacks.class);

    public Resource instance = null;
  }
}
