// AUTO-GENERATED -- DO NOT EDIT
/*
 * Copyright © 2008-2011 Kristian Høgsberg
 *   Copyright © 2010-2011 Intel Corporation
 *   Copyright © 2012-2013 Collabora, Ltd.
 * 
 *   Permission is hereby granted, free of charge, to any person
 *   obtaining a copy of this software and associated documentation files
 *   (the "Software"), to deal in the Software without restriction,
 *   including without limitation the rights to use, copy, modify, merge,
 *   publish, distribute, sublicense, and/or sell copies of the Software,
 *   and to permit persons to whom the Software is furnished to do so,
 *   subject to the following conditions:
 * 
 *   The above copyright notice and this permission notice (including the
 *   next paragraph) shall be included in all copies or substantial
 *   portions of the Software.
 * 
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */
package org.freedesktop.protocols;

import org.freedesktop.Wayland.*;
import static org.freedesktop.Wayland.Interface.Message;
import org.freedesktop.protocols.Test.*;
import org.freedesktop.protocols.XdgShell.*;

@SuppressWarnings({"ALL"})
public class Wayland {
  /** The compositor singleton
   * A compositor.  This object is a singleton global.  The
   * compositor is in charge of combining the contents of multiple
   * surfaces into one displayable output.
   */
  public static class Compositor {
    public interface Callbacks extends Resource.Callbacks {

      /** Create new surface
       * Ask the compositor to create a new surface.
       */
      public default void createSurface(/* Surface */ Resource id) {};

      /** Create new region
       * Ask the compositor to create a new region.
       */
      public default void createRegion(/* Region */ Resource id) {};
    }

    public Compositor() {}

    public Compositor(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_compositor", 5, new Message[] /* requests */ {
      new Message("create_surface", "createSurface", "n", new Interface[] { Surface.iface }), 
      new Message("create_region", "createRegion", "n", new Interface[] { Region.iface })
    }, new Message[] /* events */ {
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** A shared memory pool
   * The wl_shm_pool object encapsulates a piece of memory shared
   * between the compositor and client.  Through the wl_shm_pool
   * object, the client can allocate shared memory wl_buffer objects.
   * All objects created through the same pool share the same
   * underlying mapped memory. Reusing the mapped memory avoids the
   * setup/teardown overhead and is useful when interactively resizing
   * a surface or for many small buffers.
   */
  public static class ShmPool {
    public interface Callbacks extends Resource.Callbacks {

      /** Create a buffer from the pool
       * Create a wl_buffer object from the pool.
       * 
       * The buffer is created offset bytes into the pool and has
       * width and height as specified.  The stride argument specifies
       * the number of bytes from the beginning of one row to the beginning
       * of the next.  The format is the pixel format of the buffer and
       * must be one of those advertised through the wl_shm.format event.
       * 
       * A buffer will keep a reference to the pool it was created from
       * so it is valid to destroy the pool immediately after creating
       * a buffer from it.
       */
      public default void createBuffer(/* ShmBuffer */ Resource id, int offset, int width, int height, int stride, int format) {};

      /** Destroy the pool
       * Destroy the shared memory pool.
       * 
       * The mmapped memory will be released when all
       * buffers that have been created from this pool
       * are gone.
       */
      public default void destroy() {};

      /** Change the size of the pool mapping
       * This request will cause the server to remap the backing memory
       * for the pool from the file descriptor passed when the pool was
       * created, but using the new size.  This request can only be
       * used to make the pool bigger.
       * 
       *        This request only changes the amount of bytes that are mmapped
       *        by the server and does not touch the file corresponding to the
       *        file descriptor passed at creation time. It is the client's
       *        responsibility to ensure that the file is at least as big as
       *        the new pool size.
       */
      public default void resize(int size) {};
    }

    public ShmPool() {}

    public ShmPool(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_shm_pool", 1, new Message[] /* requests */ {
      new Message("create_buffer", "createBuffer", "niiiiu", new Interface[] { ShmBuffer.iface, null, null, null, null, null }), 
      new Message("destroy", "destroy", "", new Interface[] {  }), 
      new Message("resize", "resize", "i", new Interface[] { null })
    }, new Message[] /* events */ {
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Content for a wl_surface
   * A buffer provides the content for a wl_surface. Buffers are
   * created through factory interfaces such as wl_shm, wp_linux_buffer_params
   * (from the linux-dmabuf protocol extension) or similar. It has a width and
   * a height and can be attached to a wl_surface, but the mechanism by which a
   * client provides and updates the contents is defined by the buffer factory
   * interface.
   * 
   * If the buffer uses a format that has an alpha channel, the alpha channel
   * is assumed to be premultiplied in the color channels unless otherwise
   * specified.
   */
  public static class Buffer {
    public interface Callbacks extends Resource.Callbacks {

      /** Destroy a buffer
       * Destroy a buffer. If and how you need to release the backing
       * storage is defined by the buffer factory interface.
       * 
       * For possible side-effects to a surface, see wl_surface.attach.
       */
      public default void destroy() {};
    }

    /** Compositor releases buffer
     * Sent when this wl_buffer is no longer used by the compositor.
     * The client is now free to reuse or destroy this buffer and its
     * backing storage.
     * 
     * If a client receives a release event before the frame callback
     * requested in the same wl_surface.commit that attaches this
     * wl_buffer to a surface, then the client is immediately free to
     * reuse the buffer and its backing storage, and does not need a
     * second buffer for the next surface content update. Typically
     * this is possible, when the compositor maintains a copy of the
     * wl_surface contents, e.g. as a GL texture. This is an important
     * optimization for GL(ES) compositors with wl_shm clients.
     */
    public void release() {
      if (instance != null)
        instance.postEvent(0);
    }

    public Buffer() {}

    public Buffer(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_buffer", 1, new Message[] /* requests */ {
      new Message("destroy", "destroy", "", new Interface[] {  })
    }, new Message[] /* events */ {
      new Message("release", "release", "", new Interface[] {  })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Offer to transfer data
   * A wl_data_offer represents a piece of data offered for transfer
   * by another client (the source client).  It is used by the
   * copy-and-paste and drag-and-drop mechanisms.  The offer
   * describes the different mime types that the data can be
   * converted to and provides the mechanism for transferring the
   * data directly from the source client.
   */
  public static class DataOffer {
    public interface Callbacks extends Resource.Callbacks {

      /** Accept one of the offered mime types
       * Indicate that the client can accept the given mime type, or
       * NULL for not accepted.
       * 
       * For objects of version 2 or older, this request is used by the
       * client to give feedback whether the client can receive the given
       * mime type, or NULL if none is accepted; the feedback does not
       * determine whether the drag-and-drop operation succeeds or not.
       * 
       * For objects of version 3 or newer, this request determines the
       * final result of the drag-and-drop operation. If the end result
       * is that no mime types were accepted, the drag-and-drop operation
       * will be cancelled and the corresponding drag source will receive
       * wl_data_source.cancelled. Clients may still use this event in
       * conjunction with wl_data_source.action for feedback.
       */
      public default void accept(int serial, String mime_type) {};

      /** Request that the data is transferred
       * To transfer the offered data, the client issues this request
       * and indicates the mime type it wants to receive.  The transfer
       * happens through the passed file descriptor (typically created
       * with the pipe system call).  The source client writes the data
       * in the mime type representation requested and then closes the
       * file descriptor.
       * 
       * The receiving client reads from the read end of the pipe until
       * EOF and then closes its end, at which point the transfer is
       * complete.
       * 
       * This request may happen multiple times for different mime types,
       * both before and after wl_data_device.drop. Drag-and-drop destination
       * clients may preemptively fetch data or examine it more closely to
       * determine acceptance.
       */
      public default void receive(String mime_type, int fd) {};

      /** Destroy data offer
       * Destroy the data offer.
       */
      public default void destroy() {};

      /** The offer will no longer be used
       * Notifies the compositor that the drag destination successfully
       * finished the drag-and-drop operation.
       * 
       * Upon receiving this request, the compositor will emit
       * wl_data_source.dnd_finished on the drag source client.
       * 
       * It is a client error to perform other requests than
       * wl_data_offer.destroy after this one. It is also an error to perform
       * this request after a NULL mime type has been set in
       * wl_data_offer.accept or no action was received through
       * wl_data_offer.action.
       * 
       * If wl_data_offer.finish request is received for a non drag and drop
       * operation, the invalid_finish protocol error is raised.
       */
      public default void finish() {};

      /** Set the available/preferred drag-and-drop actions
       * Sets the actions that the destination side client supports for
       * this operation. This request may trigger the emission of
       * wl_data_source.action and wl_data_offer.action events if the compositor
       * needs to change the selected action.
       * 
       * This request can be called multiple times throughout the
       * drag-and-drop operation, typically in response to wl_data_device.enter
       * or wl_data_device.motion events.
       * 
       * This request determines the final result of the drag-and-drop
       * operation. If the end result is that no action is accepted,
       * the drag source will receive wl_data_source.cancelled.
       * 
       * The dnd_actions argument must contain only values expressed in the
       * wl_data_device_manager.dnd_actions enum, and the preferred_action
       * argument must only contain one of those values set, otherwise it
       * will result in a protocol error.
       * 
       * While managing an "ask" action, the destination drag-and-drop client
       * may perform further wl_data_offer.receive requests, and is expected
       * to perform one last wl_data_offer.set_actions request with a preferred
       * action other than "ask" (and optionally wl_data_offer.accept) before
       * requesting wl_data_offer.finish, in order to convey the action selected
       * by the user. If the preferred action is not in the
       * wl_data_offer.source_actions mask, an error will be raised.
       * 
       * If the "ask" action is dismissed (e.g. user cancellation), the client
       * is expected to perform wl_data_offer.destroy right away.
       * 
       * This request can only be made on drag-and-drop offers, a protocol error
       * will be raised otherwise.
       */
      public default void setActions(int dnd_actions, int preferred_action) {};
    }

    /** Advertise offered mime type
     * Sent immediately after creating the wl_data_offer object.  One
     * event per offered mime type.
     */
    public void offer(String mime_type) {
      if (instance != null)
        instance.postEvent(0, mime_type);
    }

    /** Notify the source-side available actions
     * This event indicates the actions offered by the data source. It
     * will be sent right after wl_data_device.enter, or anytime the source
     * side changes its offered actions through wl_data_source.set_actions.
     */
    public void sourceActions(int source_actions) {
      if (instance != null && instance.getVersion() >= 3)
        instance.postEvent(1, source_actions);
    }

    public boolean canSourceActions() {
      return instance != null && instance.getVersion() >= 3;
    }

    /** Notify the selected action
     * This event indicates the action selected by the compositor after
     * matching the source/destination side actions. Only one action (or
     * none) will be offered here.
     * 
     * This event can be emitted multiple times during the drag-and-drop
     * operation in response to destination side action changes through
     * wl_data_offer.set_actions.
     * 
     * This event will no longer be emitted after wl_data_device.drop
     * happened on the drag-and-drop destination, the client must
     * honor the last action received, or the last preferred one set
     * through wl_data_offer.set_actions when handling an "ask" action.
     * 
     * Compositors may also change the selected action on the fly, mainly
     * in response to keyboard modifier changes during the drag-and-drop
     * operation.
     * 
     * The most recent action received is always the valid one. Prior to
     * receiving wl_data_device.drop, the chosen action may change (e.g.
     * due to keyboard modifiers being pressed). At the time of receiving
     * wl_data_device.drop the drag-and-drop destination must honor the
     * last action received.
     * 
     * Action changes may still happen after wl_data_device.drop,
     * especially on "ask" actions, where the drag-and-drop destination
     * may choose another action afterwards. Action changes happening
     * at this stage are always the result of inter-client negotiation, the
     * compositor shall no longer be able to induce a different action.
     * 
     * Upon "ask" actions, it is expected that the drag-and-drop destination
     * may potentially choose a different action and/or mime type,
     * based on wl_data_offer.source_actions and finally chosen by the
     * user (e.g. popping up a menu with the available options). The
     * final wl_data_offer.set_actions and wl_data_offer.accept requests
     * must happen before the call to wl_data_offer.finish.
     */
    public void action(int dnd_action) {
      if (instance != null && instance.getVersion() >= 3)
        instance.postEvent(2, dnd_action);
    }

    public boolean canAction() {
      return instance != null && instance.getVersion() >= 3;
    }

    public enum Error {
      /** Finish request was called untimely */
      INVALID_FINISH(0),
      /** Action mask contains invalid values */
      INVALID_ACTION_MASK(1),
      /** Action argument has an invalid value */
      INVALID_ACTION(2),
      /** Offer doesn't accept this request */
      INVALID_OFFER(3);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    public DataOffer() {}

    public DataOffer(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_data_offer", 3, new Message[] /* requests */ {
      new Message("accept", "accept", "u?s", new Interface[] { null, null }), 
      new Message("receive", "receive", "sh", new Interface[] { null, null }), 
      new Message("destroy", "destroy", "", new Interface[] {  }), 
      new Message("finish", "finish", "3", new Interface[] {  }), 
      new Message("set_actions", "setActions", "3uu", new Interface[] { null, null })
    }, new Message[] /* events */ {
      new Message("offer", "offer", "s", new Interface[] { null }), 
      new Message("source_actions", "sourceActions", "3u", new Interface[] { null }), 
      new Message("action", "action", "3u", new Interface[] { null })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Offer to transfer data
   * The wl_data_source object is the source side of a wl_data_offer.
   * It is created by the source client in a data transfer and
   * provides a way to describe the offered data and a way to respond
   * to requests to transfer the data.
   */
  public static class DataSource {
    public interface Callbacks extends Resource.Callbacks {

      /** Add an offered mime type
       * This request adds a mime type to the set of mime types
       * advertised to targets.  Can be called several times to offer
       * multiple types.
       */
      public default void offer(String mime_type) {};

      /** Destroy the data source
       * Destroy the data source.
       */
      public default void destroy() {};

      /** Set the available drag-and-drop actions
       * Sets the actions that the source side client supports for this
       * operation. This request may trigger wl_data_source.action and
       * wl_data_offer.action events if the compositor needs to change the
       * selected action.
       * 
       * The dnd_actions argument must contain only values expressed in the
       * wl_data_device_manager.dnd_actions enum, otherwise it will result
       * in a protocol error.
       * 
       * This request must be made once only, and can only be made on sources
       * used in drag-and-drop, so it must be performed before
       * wl_data_device.start_drag. Attempting to use the source other than
       * for drag-and-drop will raise a protocol error.
       */
      public default void setActions(int dnd_actions) {};
    }

    /** A target accepts an offered mime type
     * Sent when a target accepts pointer_focus or motion events.  If
     * a target does not accept any of the offered types, type is NULL.
     * 
     * Used for feedback during drag-and-drop.
     */
    public void target(String mime_type) {
      if (instance != null)
        instance.postEvent(0, mime_type);
    }

    /** Send the data
     * Request for data from the client.  Send the data as the
     * specified mime type over the passed file descriptor, then
     * close it.
     */
    public void send(String mime_type, int fd) {
      if (instance != null)
        instance.postEvent(1, mime_type, fd);
    }

    /** Selection was cancelled
     * This data source is no longer valid. There are several reasons why
     * this could happen:
     * 
     * - The data source has been replaced by another data source.
     * - The drag-and-drop operation was performed, but the drop destination
     *   did not accept any of the mime types offered through
     *   wl_data_source.target.
     * - The drag-and-drop operation was performed, but the drop destination
     *   did not select any of the actions present in the mask offered through
     *   wl_data_source.action.
     * - The drag-and-drop operation was performed but didn't happen over a
     *   surface.
     * - The compositor cancelled the drag-and-drop operation (e.g. compositor
     *   dependent timeouts to avoid stale drag-and-drop transfers).
     * 
     * The client should clean up and destroy this data source.
     * 
     * For objects of version 2 or older, wl_data_source.cancelled will
     * only be emitted if the data source was replaced by another data
     * source.
     */
    public void cancelled() {
      if (instance != null)
        instance.postEvent(2);
    }

    /** The drag-and-drop operation physically finished
     * The user performed the drop action. This event does not indicate
     * acceptance, wl_data_source.cancelled may still be emitted afterwards
     * if the drop destination does not accept any mime type.
     * 
     * However, this event might however not be received if the compositor
     * cancelled the drag-and-drop operation before this event could happen.
     * 
     * Note that the data_source may still be used in the future and should
     * not be destroyed here.
     */
    public void dndDropPerformed() {
      if (instance != null && instance.getVersion() >= 3)
        instance.postEvent(3);
    }

    public boolean canDndDropPerformed() {
      return instance != null && instance.getVersion() >= 3;
    }

    /** The drag-and-drop operation concluded
     * The drop destination finished interoperating with this data
     * source, so the client is now free to destroy this data source and
     * free all associated data.
     * 
     * If the action used to perform the operation was "move", the
     * source can now delete the transferred data.
     */
    public void dndFinished() {
      if (instance != null && instance.getVersion() >= 3)
        instance.postEvent(4);
    }

    public boolean canDndFinished() {
      return instance != null && instance.getVersion() >= 3;
    }

    /** Notify the selected action
     * This event indicates the action selected by the compositor after
     * matching the source/destination side actions. Only one action (or
     * none) will be offered here.
     * 
     * This event can be emitted multiple times during the drag-and-drop
     * operation, mainly in response to destination side changes through
     * wl_data_offer.set_actions, and as the data device enters/leaves
     * surfaces.
     * 
     * It is only possible to receive this event after
     * wl_data_source.dnd_drop_performed if the drag-and-drop operation
     * ended in an "ask" action, in which case the final wl_data_source.action
     * event will happen immediately before wl_data_source.dnd_finished.
     * 
     * Compositors may also change the selected action on the fly, mainly
     * in response to keyboard modifier changes during the drag-and-drop
     * operation.
     * 
     * The most recent action received is always the valid one. The chosen
     * action may change alongside negotiation (e.g. an "ask" action can turn
     * into a "move" operation), so the effects of the final action must
     * always be applied in wl_data_offer.dnd_finished.
     * 
     * Clients can trigger cursor surface changes from this point, so
     * they reflect the current action.
     */
    public void action(int dnd_action) {
      if (instance != null && instance.getVersion() >= 3)
        instance.postEvent(5, dnd_action);
    }

    public boolean canAction() {
      return instance != null && instance.getVersion() >= 3;
    }

    public enum Error {
      /** Action mask contains invalid values */
      INVALID_ACTION_MASK(0),
      /** Source doesn't accept this request */
      INVALID_SOURCE(1);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    public DataSource() {}

    public DataSource(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_data_source", 3, new Message[] /* requests */ {
      new Message("offer", "offer", "s", new Interface[] { null }), 
      new Message("destroy", "destroy", "", new Interface[] {  }), 
      new Message("set_actions", "setActions", "3u", new Interface[] { null })
    }, new Message[] /* events */ {
      new Message("target", "target", "?s", new Interface[] { null }), 
      new Message("send", "send", "sh", new Interface[] { null, null }), 
      new Message("cancelled", "cancelled", "", new Interface[] {  }), 
      new Message("dnd_drop_performed", "dndDropPerformed", "3", new Interface[] {  }), 
      new Message("dnd_finished", "dndFinished", "3", new Interface[] {  }), 
      new Message("action", "action", "3u", new Interface[] { null })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Data transfer device
   * There is one wl_data_device per seat which can be obtained
   * from the global wl_data_device_manager singleton.
   * 
   * A wl_data_device provides access to inter-client data transfer
   * mechanisms such as copy-and-paste and drag-and-drop.
   */
  public static class DataDevice {
    public interface Callbacks extends Resource.Callbacks {

      /** Start drag-and-drop operation
       * This request asks the compositor to start a drag-and-drop
       * operation on behalf of the client.
       * 
       * The source argument is the data source that provides the data
       * for the eventual data transfer. If source is NULL, enter, leave
       * and motion events are sent only to the client that initiated the
       * drag and the client is expected to handle the data passing
       * internally. If source is destroyed, the drag-and-drop session will be
       * cancelled.
       * 
       * The origin surface is the surface where the drag originates and
       * the client must have an active implicit grab that matches the
       * serial.
       * 
       * The icon surface is an optional (can be NULL) surface that
       * provides an icon to be moved around with the cursor.  Initially,
       * the top-left corner of the icon surface is placed at the cursor
       * hotspot, but subsequent wl_surface.attach request can move the
       * relative position. Attach requests must be confirmed with
       * wl_surface.commit as usual. The icon surface is given the role of
       * a drag-and-drop icon. If the icon surface already has another role,
       * it raises a protocol error.
       * 
       * The current and pending input regions of the icon wl_surface are
       * cleared, and wl_surface.set_input_region is ignored until the
       * wl_surface is no longer used as the icon surface. When the use
       * as an icon ends, the current and pending input regions become
       * undefined, and the wl_surface is unmapped.
       */
      public default void startDrag(/* DataSource */ Resource source, /* Surface */ Resource origin, /* Surface */ Resource icon, int serial) {};

      /** Copy data to the selection
       * This request asks the compositor to set the selection
       * to the data from the source on behalf of the client.
       * 
       * To unset the selection, set the source to NULL.
       */
      public default void setSelection(/* DataSource */ Resource source, int serial) {};

      /** Destroy data device
       * This request destroys the data device.
       */
      public default void release() {};
    }

    /** Introduce a new wl_data_offer
     * The data_offer event introduces a new wl_data_offer object,
     * which will subsequently be used in either the
     * data_device.enter event (for drag-and-drop) or the
     * data_device.selection event (for selections).  Immediately
     * following the data_device.data_offer event, the new data_offer
     * object will send out data_offer.offer events to describe the
     * mime types it offers.
     */
    public void dataOffer(/* DataOffer */ Resource id) {
      if (instance != null)
        instance.postEvent(0, id);
    }

    /** Initiate drag-and-drop session
     * This event is sent when an active drag-and-drop pointer enters
     * a surface owned by the client.  The position of the pointer at
     * enter time is provided by the x and y arguments, in surface-local
     * coordinates.
     */
    public void enter(int serial, /* Surface */ Resource surface, Fixed x, Fixed y, /* DataOffer */ Resource id) {
      if (instance != null)
        instance.postEvent(1, serial, surface, x, y, id);
    }

    /** End drag-and-drop session
     * This event is sent when the drag-and-drop pointer leaves the
     * surface and the session ends.  The client must destroy the
     * wl_data_offer introduced at enter time at this point.
     */
    public void leave() {
      if (instance != null)
        instance.postEvent(2);
    }

    /** Drag-and-drop session motion
     * This event is sent when the drag-and-drop pointer moves within
     * the currently focused surface. The new position of the pointer
     * is provided by the x and y arguments, in surface-local
     * coordinates.
     */
    public void motion(int time, Fixed x, Fixed y) {
      if (instance != null)
        instance.postEvent(3, time, x, y);
    }

    /** End drag-and-drop session successfully
     * The event is sent when a drag-and-drop operation is ended
     * because the implicit grab is removed.
     * 
     * The drag-and-drop destination is expected to honor the last action
     * received through wl_data_offer.action, if the resulting action is
     * "copy" or "move", the destination can still perform
     * wl_data_offer.receive requests, and is expected to end all
     * transfers with a wl_data_offer.finish request.
     * 
     * If the resulting action is "ask", the action will not be considered
     * final. The drag-and-drop destination is expected to perform one last
     * wl_data_offer.set_actions request, or wl_data_offer.destroy in order
     * to cancel the operation.
     */
    public void drop() {
      if (instance != null)
        instance.postEvent(4);
    }

    /** Advertise new selection
     * The selection event is sent out to notify the client of a new
     * wl_data_offer for the selection for this device.  The
     * data_device.data_offer and the data_offer.offer events are
     * sent out immediately before this event to introduce the data
     * offer object.  The selection event is sent to a client
     * immediately before receiving keyboard focus and when a new
     * selection is set while the client has keyboard focus.  The
     * data_offer is valid until a new data_offer or NULL is received
     * or until the client loses keyboard focus.  Switching surface with
     * keyboard focus within the same client doesn't mean a new selection
     * will be sent.  The client must destroy the previous selection
     * data_offer, if any, upon receiving this event.
     */
    public void selection(/* DataOffer */ Resource id) {
      if (instance != null)
        instance.postEvent(5, id);
    }

    public enum Error {
      /** Given wl_surface has another role */
      ROLE(0);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    public DataDevice() {}

    public DataDevice(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_data_device", 3, new Message[] /* requests */ {
      new Message("start_drag", "startDrag", "?oo?ou", new Interface[] { DataSource.iface, Surface.iface, Surface.iface, null }), 
      new Message("set_selection", "setSelection", "?ou", new Interface[] { DataSource.iface, null }), 
      new Message("release", "release", "2", new Interface[] {  })
    }, new Message[] /* events */ {
      new Message("data_offer", "dataOffer", "n", new Interface[] { DataOffer.iface }), 
      new Message("enter", "enter", "uoff?o", new Interface[] { null, Surface.iface, null, null, DataOffer.iface }), 
      new Message("leave", "leave", "", new Interface[] {  }), 
      new Message("motion", "motion", "uff", new Interface[] { null, null, null }), 
      new Message("drop", "drop", "", new Interface[] {  }), 
      new Message("selection", "selection", "?o", new Interface[] { DataOffer.iface })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Data transfer interface
   * The wl_data_device_manager is a singleton global object that
   * provides access to inter-client data transfer mechanisms such as
   * copy-and-paste and drag-and-drop.  These mechanisms are tied to
   * a wl_seat and this interface lets a client get a wl_data_device
   * corresponding to a wl_seat.
   * 
   * Depending on the version bound, the objects created from the bound
   * wl_data_device_manager object will have different requirements for
   * functioning properly. See wl_data_source.set_actions,
   * wl_data_offer.accept and wl_data_offer.finish for details.
   */
  public static class DataDeviceManager {
    public interface Callbacks extends Resource.Callbacks {

      /** Create a new data source
       * Create a new data source.
       */
      public default void createDataSource(/* DataSource */ Resource id) {};

      /** Create a new data device
       * Create a new data device for a given seat.
       */
      public default void getDataDevice(/* DataDevice */ Resource id, /* Seat */ Resource seat) {};
    }

    /** Drag and drop actions
     * This is a bitmask of the available/preferred actions in a
     * drag-and-drop operation.
     * 
     * In the compositor, the selected action is a result of matching the
     * actions offered by the source and destination sides.  "action" events
     * with a "none" action will be sent to both source and destination if
     * there is no match. All further checks will effectively happen on
     * (source actions ∩ destination actions).
     * 
     * In addition, compositors may also pick different actions in
     * reaction to key modifiers being pressed. One common design that
     * is used in major toolkits (and the behavior recommended for
     * compositors) is:
     * 
     * - If no modifiers are pressed, the first match (in bit order)
     *   will be used.
     * - Pressing Shift selects "move", if enabled in the mask.
     * - Pressing Control selects "copy", if enabled in the mask.
     * 
     * Behavior beyond that is considered implementation-dependent.
     * Compositors may for example bind other modifiers (like Alt/Meta)
     * or drags initiated with other buttons than BTN_LEFT to specific
     * actions (e.g. "ask").
     */
    public enum DndAction {
      /** No action */
      NONE(0),
      /** Copy action */
      COPY(1),
      /** Move action */
      MOVE(2),
      /** Ask action */
      ASK(4);

      public final int value;
      DndAction(int value) {
        this.value = value;
      }
    }

    public DataDeviceManager() {}

    public DataDeviceManager(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_data_device_manager", 3, new Message[] /* requests */ {
      new Message("create_data_source", "createDataSource", "n", new Interface[] { DataSource.iface }), 
      new Message("get_data_device", "getDataDevice", "no", new Interface[] { DataDevice.iface, Seat.iface })
    }, new Message[] /* events */ {
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Create desktop-style surfaces
   * This interface is implemented by servers that provide
   * desktop-style user interfaces.
   * 
   * It allows clients to associate a wl_shell_surface with
   * a basic surface.
   * 
   * Note! This protocol is deprecated and not intended for production use.
   * For desktop-style user interfaces, use xdg_shell. Compositors and clients
   * should not implement this interface.
   */
  public static class Shell {
    public interface Callbacks extends Resource.Callbacks {

      /** Create a shell surface from a surface
       * Create a shell surface for an existing surface. This gives
       * the wl_surface the role of a shell surface. If the wl_surface
       * already has another role, it raises a protocol error.
       * 
       * Only one shell surface can be associated with a given surface.
       */
      public default void getShellSurface(/* ShellSurface */ Resource id, /* Surface */ Resource surface) {};
    }

    public enum Error {
      /** Given wl_surface has another role */
      ROLE(0);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    public Shell() {}

    public Shell(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_shell", 1, new Message[] /* requests */ {
      new Message("get_shell_surface", "getShellSurface", "no", new Interface[] { ShellSurface.iface, Surface.iface })
    }, new Message[] /* events */ {
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Desktop-style metadata interface
   * An interface that may be implemented by a wl_surface, for
   * implementations that provide a desktop-style user interface.
   * 
   * It provides requests to treat surfaces like toplevel, fullscreen
   * or popup windows, move, resize or maximize them, associate
   * metadata like title and class, etc.
   * 
   * On the server side the object is automatically destroyed when
   * the related wl_surface is destroyed. On the client side,
   * wl_shell_surface_destroy() must be called before destroying
   * the wl_surface object.
   */
  public static class ShellSurface {
    public interface Callbacks extends Resource.Callbacks {

      /** Respond to a ping event
       * A client must respond to a ping event with a pong request or
       * the client may be deemed unresponsive.
       */
      public default void pong(int serial) {};

      /** Start an interactive move
       * Start a pointer-driven move of the surface.
       * 
       * This request must be used in response to a button press event.
       * The server may ignore move requests depending on the state of
       * the surface (e.g. fullscreen or maximized).
       */
      public default void move(/* Seat */ Resource seat, int serial) {};

      /** Start an interactive resize
       * Start a pointer-driven resizing of the surface.
       * 
       * This request must be used in response to a button press event.
       * The server may ignore resize requests depending on the state of
       * the surface (e.g. fullscreen or maximized).
       */
      public default void resize(/* Seat */ Resource seat, int serial, int edges) {};

      /** Make the surface a toplevel surface
       * Map the surface as a toplevel surface.
       * 
       * A toplevel surface is not fullscreen, maximized or transient.
       */
      public default void setToplevel() {};

      /** Make the surface a transient surface
       * Map the surface relative to an existing surface.
       * 
       * The x and y arguments specify the location of the upper left
       * corner of the surface relative to the upper left corner of the
       * parent surface, in surface-local coordinates.
       * 
       * The flags argument controls details of the transient behaviour.
       */
      public default void setTransient(/* Surface */ Resource parent, int x, int y, int flags) {};

      /** Make the surface a fullscreen surface
       * Map the surface as a fullscreen surface.
       * 
       * If an output parameter is given then the surface will be made
       * fullscreen on that output. If the client does not specify the
       * output then the compositor will apply its policy - usually
       * choosing the output on which the surface has the biggest surface
       * area.
       * 
       * The client may specify a method to resolve a size conflict
       * between the output size and the surface size - this is provided
       * through the method parameter.
       * 
       * The framerate parameter is used only when the method is set
       * to "driver", to indicate the preferred framerate. A value of 0
       * indicates that the client does not care about framerate.  The
       * framerate is specified in mHz, that is framerate of 60000 is 60Hz.
       * 
       * A method of "scale" or "driver" implies a scaling operation of
       * the surface, either via a direct scaling operation or a change of
       * the output mode. This will override any kind of output scaling, so
       * that mapping a surface with a buffer size equal to the mode can
       * fill the screen independent of buffer_scale.
       * 
       * A method of "fill" means we don't scale up the buffer, however
       * any output scale is applied. This means that you may run into
       * an edge case where the application maps a buffer with the same
       * size of the output mode but buffer_scale 1 (thus making a
       * surface larger than the output). In this case it is allowed to
       * downscale the results to fit the screen.
       * 
       * The compositor must reply to this request with a configure event
       * with the dimensions for the output on which the surface will
       * be made fullscreen.
       */
      public default void setFullscreen(int method, int framerate, /* Output */ Resource output) {};

      /** Make the surface a popup surface
       * Map the surface as a popup.
       * 
       * A popup surface is a transient surface with an added pointer
       * grab.
       * 
       * An existing implicit grab will be changed to owner-events mode,
       * and the popup grab will continue after the implicit grab ends
       * (i.e. releasing the mouse button does not cause the popup to
       * be unmapped).
       * 
       * The popup grab continues until the window is destroyed or a
       * mouse button is pressed in any other client's window. A click
       * in any of the client's surfaces is reported as normal, however,
       * clicks in other clients' surfaces will be discarded and trigger
       * the callback.
       * 
       * The x and y arguments specify the location of the upper left
       * corner of the surface relative to the upper left corner of the
       * parent surface, in surface-local coordinates.
       */
      public default void setPopup(/* Seat */ Resource seat, int serial, /* Surface */ Resource parent, int x, int y, int flags) {};

      /** Make the surface a maximized surface
       * Map the surface as a maximized surface.
       * 
       * If an output parameter is given then the surface will be
       * maximized on that output. If the client does not specify the
       * output then the compositor will apply its policy - usually
       * choosing the output on which the surface has the biggest surface
       * area.
       * 
       * The compositor will reply with a configure event telling
       * the expected new surface size. The operation is completed
       * on the next buffer attach to this surface.
       * 
       * A maximized surface typically fills the entire output it is
       * bound to, except for desktop elements such as panels. This is
       * the main difference between a maximized shell surface and a
       * fullscreen shell surface.
       * 
       * The details depend on the compositor implementation.
       */
      public default void setMaximized(/* Output */ Resource output) {};

      /** Set surface title
       * Set a short title for the surface.
       * 
       * This string may be used to identify the surface in a task bar,
       * window list, or other user interface elements provided by the
       * compositor.
       * 
       * The string must be encoded in UTF-8.
       */
      public default void setTitle(String title) {};

      /** Set surface class
       * Set a class for the surface.
       * 
       * The surface class identifies the general class of applications
       * to which the surface belongs. A common convention is to use the
       * file name (or the full path if it is a non-standard location) of
       * the application's .desktop file as the class.
       */
      public default void setClass(String class_) {};
    }

    /** Ping client
     * Ping a client to check if it is receiving events and sending
     * requests. A client is expected to reply with a pong request.
     */
    public void ping(int serial) {
      if (instance != null)
        instance.postEvent(0, serial);
    }

    /** Suggest resize
     * The configure event asks the client to resize its surface.
     * 
     * The size is a hint, in the sense that the client is free to
     * ignore it if it doesn't resize, pick a smaller size (to
     * satisfy aspect ratio or resize in steps of NxM pixels).
     * 
     * The edges parameter provides a hint about how the surface
     * was resized. The client may use this information to decide
     * how to adjust its content to the new size (e.g. a scrolling
     * area might adjust its content position to leave the viewable
     * content unmoved).
     * 
     * The client is free to dismiss all but the last configure
     * event it received.
     * 
     * The width and height arguments specify the size of the window
     * in surface-local coordinates.
     */
    public void configure(int edges, int width, int height) {
      if (instance != null)
        instance.postEvent(1, edges, width, height);
    }

    /** Popup interaction is done
     * The popup_done event is sent out when a popup grab is broken,
     * that is, when the user clicks a surface that doesn't belong
     * to the client owning the popup surface.
     */
    public void popupDone() {
      if (instance != null)
        instance.postEvent(2);
    }

    /** Edge values for resizing
     * These values are used to indicate which edge of a surface
     * is being dragged in a resize operation. The server may
     * use this information to adapt its behavior, e.g. choose
     * an appropriate cursor image.
     */
    public enum Resize {
      /** No edge */
      NONE(0),
      /** Top edge */
      TOP(1),
      /** Bottom edge */
      BOTTOM(2),
      /** Left edge */
      LEFT(4),
      /** Top and left edges */
      TOP_LEFT(5),
      /** Bottom and left edges */
      BOTTOM_LEFT(6),
      /** Right edge */
      RIGHT(8),
      /** Top and right edges */
      TOP_RIGHT(9),
      /** Bottom and right edges */
      BOTTOM_RIGHT(10);

      public final int value;
      Resize(int value) {
        this.value = value;
      }
    }

    /** Details of transient behaviour
     * These flags specify details of the expected behaviour
     * of transient surfaces. Used in the set_transient request.
     */
    public enum Transient {
      /** Do not set keyboard focus */
      INACTIVE(0x1);

      public final int value;
      Transient(int value) {
        this.value = value;
      }
    }

    /** Different method to set the surface fullscreen
     * Hints to indicate to the compositor how to deal with a conflict
     * between the dimensions of the surface and the dimensions of the
     * output. The compositor is free to ignore this parameter.
     */
    public enum FullscreenMethod {
      /** No preference, apply default policy */
      _DEFAULT(0),
      /** Scale, preserve the surface's aspect ratio and center on output */
      SCALE(1),
      /** Switch output mode to the smallest mode that can fit the surface, add black borders to compensate size mismatch */
      DRIVER(2),
      /** No upscaling, center on output and add black borders to compensate size mismatch */
      FILL(3);

      public final int value;
      FullscreenMethod(int value) {
        this.value = value;
      }
    }

    public ShellSurface() {}

    public ShellSurface(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_shell_surface", 1, new Message[] /* requests */ {
      new Message("pong", "pong", "u", new Interface[] { null }), 
      new Message("move", "move", "ou", new Interface[] { Seat.iface, null }), 
      new Message("resize", "resize", "ouu", new Interface[] { Seat.iface, null, null }), 
      new Message("set_toplevel", "setToplevel", "", new Interface[] {  }), 
      new Message("set_transient", "setTransient", "oiiu", new Interface[] { Surface.iface, null, null, null }), 
      new Message("set_fullscreen", "setFullscreen", "uu?o", new Interface[] { null, null, Output.iface }), 
      new Message("set_popup", "setPopup", "ouoiiu", new Interface[] { Seat.iface, null, Surface.iface, null, null, null }), 
      new Message("set_maximized", "setMaximized", "?o", new Interface[] { Output.iface }), 
      new Message("set_title", "setTitle", "s", new Interface[] { null }), 
      new Message("set_class", "setClass", "s", new Interface[] { null })
    }, new Message[] /* events */ {
      new Message("ping", "ping", "u", new Interface[] { null }), 
      new Message("configure", "configure", "uii", new Interface[] { null, null, null }), 
      new Message("popup_done", "popupDone", "", new Interface[] {  })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** An onscreen surface
   * A surface is a rectangular area that may be displayed on zero
   * or more outputs, and shown any number of times at the compositor's
   * discretion. They can present wl_buffers, receive user input, and
   * define a local coordinate system.
   * 
   * The size of a surface (and relative positions on it) is described
   * in surface-local coordinates, which may differ from the buffer
   * coordinates of the pixel content, in case a buffer_transform
   * or a buffer_scale is used.
   * 
   * A surface without a "role" is fairly useless: a compositor does
   * not know where, when or how to present it. The role is the
   * purpose of a wl_surface. Examples of roles are a cursor for a
   * pointer (as set by wl_pointer.set_cursor), a drag icon
   * (wl_data_device.start_drag), a sub-surface
   * (wl_subcompositor.get_subsurface), and a window as defined by a
   * shell protocol (e.g. wl_shell.get_shell_surface).
   * 
   * A surface can have only one role at a time. Initially a
   * wl_surface does not have a role. Once a wl_surface is given a
   * role, it is set permanently for the whole lifetime of the
   * wl_surface object. Giving the current role again is allowed,
   * unless explicitly forbidden by the relevant interface
   * specification.
   * 
   * Surface roles are given by requests in other interfaces such as
   * wl_pointer.set_cursor. The request should explicitly mention
   * that this request gives a role to a wl_surface. Often, this
   * request also creates a new protocol object that represents the
   * role and adds additional functionality to wl_surface. When a
   * client wants to destroy a wl_surface, they must destroy this 'role
   * object' before the wl_surface.
   * 
   * Destroying the role object does not remove the role from the
   * wl_surface, but it may stop the wl_surface from "playing the role".
   * For instance, if a wl_subsurface object is destroyed, the wl_surface
   * it was created for will be unmapped and forget its position and
   * z-order. It is allowed to create a wl_subsurface for the same
   * wl_surface again, but it is not allowed to use the wl_surface as
   * a cursor (cursor is a different role than sub-surface, and role
   * switching is not allowed).
   */
  public static class Surface {
    public interface Callbacks extends Resource.Callbacks {

      /** Delete surface
       * Deletes the surface and invalidates its object ID.
       */
      public default void destroy() {};

      /** Set the surface contents
       * Set a buffer as the content of this surface.
       * 
       * The new size of the surface is calculated based on the buffer
       * size transformed by the inverse buffer_transform and the
       * inverse buffer_scale. This means that at commit time the supplied
       * buffer size must be an integer multiple of the buffer_scale. If
       * that's not the case, an invalid_size error is sent.
       * 
       * The x and y arguments specify the location of the new pending
       * buffer's upper left corner, relative to the current buffer's upper
       * left corner, in surface-local coordinates. In other words, the
       * x and y, combined with the new surface size define in which
       * directions the surface's size changes. Setting anything other than 0
       * as x and y arguments is discouraged, and should instead be replaced
       * with using the separate wl_surface.offset request.
       * 
       * When the bound wl_surface version is 5 or higher, passing any
       * non-zero x or y is a protocol violation, and will result in an
       * 'invalid_offset' error being raised. To achieve equivalent semantics,
       * use wl_surface.offset.
       * 
       * Surface contents are double-buffered state, see wl_surface.commit.
       * 
       * The initial surface contents are void; there is no content.
       * wl_surface.attach assigns the given wl_buffer as the pending
       * wl_buffer. wl_surface.commit makes the pending wl_buffer the new
       * surface contents, and the size of the surface becomes the size
       * calculated from the wl_buffer, as described above. After commit,
       * there is no pending buffer until the next attach.
       * 
       * Committing a pending wl_buffer allows the compositor to read the
       * pixels in the wl_buffer. The compositor may access the pixels at
       * any time after the wl_surface.commit request. When the compositor
       * will not access the pixels anymore, it will send the
       * wl_buffer.release event. Only after receiving wl_buffer.release,
       * the client may reuse the wl_buffer. A wl_buffer that has been
       * attached and then replaced by another attach instead of committed
       * will not receive a release event, and is not used by the
       * compositor.
       * 
       * If a pending wl_buffer has been committed to more than one wl_surface,
       * the delivery of wl_buffer.release events becomes undefined. A well
       * behaved client should not rely on wl_buffer.release events in this
       * case. Alternatively, a client could create multiple wl_buffer objects
       * from the same backing storage or use wp_linux_buffer_release.
       * 
       * Destroying the wl_buffer after wl_buffer.release does not change
       * the surface contents. Destroying the wl_buffer before wl_buffer.release
       * is allowed as long as the underlying buffer storage isn't re-used (this
       * can happen e.g. on client process termination). However, if the client
       * destroys the wl_buffer before receiving the wl_buffer.release event and
       * mutates the underlying buffer storage, the surface contents become
       * undefined immediately.
       * 
       * If wl_surface.attach is sent with a NULL wl_buffer, the
       * following wl_surface.commit will remove the surface content.
       */
      public default void attach(/* ShmBuffer */ Resource buffer, int x, int y) {};

      /** Mark part of the surface damaged
       * This request is used to describe the regions where the pending
       * buffer is different from the current surface contents, and where
       * the surface therefore needs to be repainted. The compositor
       * ignores the parts of the damage that fall outside of the surface.
       * 
       * Damage is double-buffered state, see wl_surface.commit.
       * 
       * The damage rectangle is specified in surface-local coordinates,
       * where x and y specify the upper left corner of the damage rectangle.
       * 
       * The initial value for pending damage is empty: no damage.
       * wl_surface.damage adds pending damage: the new pending damage
       * is the union of old pending damage and the given rectangle.
       * 
       * wl_surface.commit assigns pending damage as the current damage,
       * and clears pending damage. The server will clear the current
       * damage as it repaints the surface.
       * 
       * Note! New clients should not use this request. Instead damage can be
       * posted with wl_surface.damage_buffer which uses buffer coordinates
       * instead of surface coordinates.
       */
      public default void damage(int x, int y, int width, int height) {};

      /** Request a frame throttling hint
       * Request a notification when it is a good time to start drawing a new
       * frame, by creating a frame callback. This is useful for throttling
       * redrawing operations, and driving animations.
       * 
       * When a client is animating on a wl_surface, it can use the 'frame'
       * request to get notified when it is a good time to draw and commit the
       * next frame of animation. If the client commits an update earlier than
       * that, it is likely that some updates will not make it to the display,
       * and the client is wasting resources by drawing too often.
       * 
       * The frame request will take effect on the next wl_surface.commit.
       * The notification will only be posted for one frame unless
       * requested again. For a wl_surface, the notifications are posted in
       * the order the frame requests were committed.
       * 
       * The server must send the notifications so that a client
       * will not send excessive updates, while still allowing
       * the highest possible update rate for clients that wait for the reply
       * before drawing again. The server should give some time for the client
       * to draw and commit after sending the frame callback events to let it
       * hit the next output refresh.
       * 
       * A server should avoid signaling the frame callbacks if the
       * surface is not visible in any way, e.g. the surface is off-screen,
       * or completely obscured by other opaque surfaces.
       * 
       * The object returned by this request will be destroyed by the
       * compositor after the callback is fired and as such the client must not
       * attempt to use it after that point.
       * 
       * The callback_data passed in the callback is the current time, in
       * milliseconds, with an undefined base.
       */
      public default void frame(int callback) {};

      /** Set opaque region
       * This request sets the region of the surface that contains
       * opaque content.
       * 
       * The opaque region is an optimization hint for the compositor
       * that lets it optimize the redrawing of content behind opaque
       * regions.  Setting an opaque region is not required for correct
       * behaviour, but marking transparent content as opaque will result
       * in repaint artifacts.
       * 
       * The opaque region is specified in surface-local coordinates.
       * 
       * The compositor ignores the parts of the opaque region that fall
       * outside of the surface.
       * 
       * Opaque region is double-buffered state, see wl_surface.commit.
       * 
       * wl_surface.set_opaque_region changes the pending opaque region.
       * wl_surface.commit copies the pending region to the current region.
       * Otherwise, the pending and current regions are never changed.
       * 
       * The initial value for an opaque region is empty. Setting the pending
       * opaque region has copy semantics, and the wl_region object can be
       * destroyed immediately. A NULL wl_region causes the pending opaque
       * region to be set to empty.
       */
      public default void setOpaqueRegion(/* Region */ Resource region) {};

      /** Set input region
       * This request sets the region of the surface that can receive
       * pointer and touch events.
       * 
       * Input events happening outside of this region will try the next
       * surface in the server surface stack. The compositor ignores the
       * parts of the input region that fall outside of the surface.
       * 
       * The input region is specified in surface-local coordinates.
       * 
       * Input region is double-buffered state, see wl_surface.commit.
       * 
       * wl_surface.set_input_region changes the pending input region.
       * wl_surface.commit copies the pending region to the current region.
       * Otherwise the pending and current regions are never changed,
       * except cursor and icon surfaces are special cases, see
       * wl_pointer.set_cursor and wl_data_device.start_drag.
       * 
       * The initial value for an input region is infinite. That means the
       * whole surface will accept input. Setting the pending input region
       * has copy semantics, and the wl_region object can be destroyed
       * immediately. A NULL wl_region causes the input region to be set
       * to infinite.
       */
      public default void setInputRegion(/* Region */ Resource region) {};

      /** Commit pending surface state
       * Surface state (input, opaque, and damage regions, attached buffers,
       * etc.) is double-buffered. Protocol requests modify the pending state,
       * as opposed to the current state in use by the compositor. A commit
       * request atomically applies all pending state, replacing the current
       * state. After commit, the new pending state is as documented for each
       * related request.
       * 
       * On commit, a pending wl_buffer is applied first, and all other state
       * second. This means that all coordinates in double-buffered state are
       * relative to the new wl_buffer coming into use, except for
       * wl_surface.attach itself. If there is no pending wl_buffer, the
       * coordinates are relative to the current surface contents.
       * 
       * All requests that need a commit to become effective are documented
       * to affect double-buffered state.
       * 
       * Other interfaces may add further double-buffered surface state.
       */
      public default void commit() {};

      /** Sets the buffer transformation
       * This request sets an optional transformation on how the compositor
       * interprets the contents of the buffer attached to the surface. The
       * accepted values for the transform parameter are the values for
       * wl_output.transform.
       * 
       * Buffer transform is double-buffered state, see wl_surface.commit.
       * 
       * A newly created surface has its buffer transformation set to normal.
       * 
       * wl_surface.set_buffer_transform changes the pending buffer
       * transformation. wl_surface.commit copies the pending buffer
       * transformation to the current one. Otherwise, the pending and current
       * values are never changed.
       * 
       * The purpose of this request is to allow clients to render content
       * according to the output transform, thus permitting the compositor to
       * use certain optimizations even if the display is rotated. Using
       * hardware overlays and scanning out a client buffer for fullscreen
       * surfaces are examples of such optimizations. Those optimizations are
       * highly dependent on the compositor implementation, so the use of this
       * request should be considered on a case-by-case basis.
       * 
       * Note that if the transform value includes 90 or 270 degree rotation,
       * the width of the buffer will become the surface height and the height
       * of the buffer will become the surface width.
       * 
       * If transform is not one of the values from the
       * wl_output.transform enum the invalid_transform protocol error
       * is raised.
       */
      public default void setBufferTransform(int transform) {};

      /** Sets the buffer scaling factor
       * This request sets an optional scaling factor on how the compositor
       * interprets the contents of the buffer attached to the window.
       * 
       * Buffer scale is double-buffered state, see wl_surface.commit.
       * 
       * A newly created surface has its buffer scale set to 1.
       * 
       * wl_surface.set_buffer_scale changes the pending buffer scale.
       * wl_surface.commit copies the pending buffer scale to the current one.
       * Otherwise, the pending and current values are never changed.
       * 
       * The purpose of this request is to allow clients to supply higher
       * resolution buffer data for use on high resolution outputs. It is
       * intended that you pick the same buffer scale as the scale of the
       * output that the surface is displayed on. This means the compositor
       * can avoid scaling when rendering the surface on that output.
       * 
       * Note that if the scale is larger than 1, then you have to attach
       * a buffer that is larger (by a factor of scale in each dimension)
       * than the desired surface size.
       * 
       * If scale is not positive the invalid_scale protocol error is
       * raised.
       */
      public default void setBufferScale(int scale) {};

      /** Mark part of the surface damaged using buffer coordinates
       * This request is used to describe the regions where the pending
       * buffer is different from the current surface contents, and where
       * the surface therefore needs to be repainted. The compositor
       * ignores the parts of the damage that fall outside of the surface.
       * 
       * Damage is double-buffered state, see wl_surface.commit.
       * 
       * The damage rectangle is specified in buffer coordinates,
       * where x and y specify the upper left corner of the damage rectangle.
       * 
       * The initial value for pending damage is empty: no damage.
       * wl_surface.damage_buffer adds pending damage: the new pending
       * damage is the union of old pending damage and the given rectangle.
       * 
       * wl_surface.commit assigns pending damage as the current damage,
       * and clears pending damage. The server will clear the current
       * damage as it repaints the surface.
       * 
       * This request differs from wl_surface.damage in only one way - it
       * takes damage in buffer coordinates instead of surface-local
       * coordinates. While this generally is more intuitive than surface
       * coordinates, it is especially desirable when using wp_viewport
       * or when a drawing library (like EGL) is unaware of buffer scale
       * and buffer transform.
       * 
       * Note: Because buffer transformation changes and damage requests may
       * be interleaved in the protocol stream, it is impossible to determine
       * the actual mapping between surface and buffer damage until
       * wl_surface.commit time. Therefore, compositors wishing to take both
       * kinds of damage into account will have to accumulate damage from the
       * two requests separately and only transform from one to the other
       * after receiving the wl_surface.commit.
       */
      public default void damageBuffer(int x, int y, int width, int height) {};

      /** Set the surface contents offset
       * The x and y arguments specify the location of the new pending
       * buffer's upper left corner, relative to the current buffer's upper
       * left corner, in surface-local coordinates. In other words, the
       * x and y, combined with the new surface size define in which
       * directions the surface's size changes.
       * 
       * Surface location offset is double-buffered state, see
       * wl_surface.commit.
       * 
       * This request is semantically equivalent to and the replaces the x and y
       * arguments in the wl_surface.attach request in wl_surface versions prior
       * to 5. See wl_surface.attach for details.
       */
      public default void offset(int x, int y) {};
    }

    /** Surface enters an output
     * This is emitted whenever a surface's creation, movement, or resizing
     * results in some part of it being within the scanout region of an
     * output.
     * 
     * Note that a surface may be overlapping with zero or more outputs.
     */
    public void enter(/* Output */ Resource output) {
      if (instance != null)
        instance.postEvent(0, output);
    }

    /** Surface leaves an output
     * This is emitted whenever a surface's creation, movement, or resizing
     * results in it no longer having any part of it within the scanout region
     * of an output.
     * 
     * Clients should not use the number of outputs the surface is on for frame
     * throttling purposes. The surface might be hidden even if no leave event
     * has been sent, and the compositor might expect new surface content
     * updates even if no enter event has been sent. The frame event should be
     * used instead.
     */
    public void leave(/* Output */ Resource output) {
      if (instance != null)
        instance.postEvent(1, output);
    }

    /** Wl_surface error values
     * These errors can be emitted in response to wl_surface requests.
     */
    public enum Error {
      /** Buffer scale value is invalid */
      INVALID_SCALE(0),
      /** Buffer transform value is invalid */
      INVALID_TRANSFORM(1),
      /** Buffer size is invalid */
      INVALID_SIZE(2),
      /** Buffer offset is invalid */
      INVALID_OFFSET(3);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    public Surface() {}

    public Surface(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_surface", 5, new Message[] /* requests */ {
      new Message("destroy", "destroy", "", new Interface[] {  }), 
      new Message("attach", "attach", "?oii", new Interface[] { ShmBuffer.iface, null, null }), 
      new Message("damage", "damage", "iiii", new Interface[] { null, null, null, null }), 
      new Message("frame", "frame", "n", new Interface[] { Callback.iface }), 
      new Message("set_opaque_region", "setOpaqueRegion", "?o", new Interface[] { Region.iface }), 
      new Message("set_input_region", "setInputRegion", "?o", new Interface[] { Region.iface }), 
      new Message("commit", "commit", "", new Interface[] {  }), 
      new Message("set_buffer_transform", "setBufferTransform", "2i", new Interface[] { null }), 
      new Message("set_buffer_scale", "setBufferScale", "3i", new Interface[] { null }), 
      new Message("damage_buffer", "damageBuffer", "4iiii", new Interface[] { null, null, null, null }), 
      new Message("offset", "offset", "5ii", new Interface[] { null, null })
    }, new Message[] /* events */ {
      new Message("enter", "enter", "o", new Interface[] { Output.iface }), 
      new Message("leave", "leave", "o", new Interface[] { Output.iface })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Group of input devices
   * A seat is a group of keyboards, pointer and touch devices. This
   * object is published as a global during start up, or when such a
   * device is hot plugged.  A seat typically has a pointer and
   * maintains a keyboard focus and a pointer focus.
   */
  public static class Seat {
    public interface Callbacks extends Resource.Callbacks {

      /** Return pointer object
       * The ID provided will be initialized to the wl_pointer interface
       * for this seat.
       * 
       * This request only takes effect if the seat has the pointer
       * capability, or has had the pointer capability in the past.
       * It is a protocol violation to issue this request on a seat that has
       * never had the pointer capability. The missing_capability error will
       * be sent in this case.
       */
      public default void getPointer(/* Pointer */ Resource id) {};

      /** Return keyboard object
       * The ID provided will be initialized to the wl_keyboard interface
       * for this seat.
       * 
       * This request only takes effect if the seat has the keyboard
       * capability, or has had the keyboard capability in the past.
       * It is a protocol violation to issue this request on a seat that has
       * never had the keyboard capability. The missing_capability error will
       * be sent in this case.
       */
      public default void getKeyboard(/* Keyboard */ Resource id) {};

      /** Return touch object
       * The ID provided will be initialized to the wl_touch interface
       * for this seat.
       * 
       * This request only takes effect if the seat has the touch
       * capability, or has had the touch capability in the past.
       * It is a protocol violation to issue this request on a seat that has
       * never had the touch capability. The missing_capability error will
       * be sent in this case.
       */
      public default void getTouch(/* Touch */ Resource id) {};

      /** Release the seat object
       * Using this request a client can tell the server that it is not going to
       * use the seat object anymore.
       */
      public default void release() {};
    }

    /** Seat capabilities changed
     * This is emitted whenever a seat gains or loses the pointer,
     * keyboard or touch capabilities.  The argument is a capability
     * enum containing the complete set of capabilities this seat has.
     * 
     * When the pointer capability is added, a client may create a
     * wl_pointer object using the wl_seat.get_pointer request. This object
     * will receive pointer events until the capability is removed in the
     * future.
     * 
     * When the pointer capability is removed, a client should destroy the
     * wl_pointer objects associated with the seat where the capability was
     * removed, using the wl_pointer.release request. No further pointer
     * events will be received on these objects.
     * 
     * In some compositors, if a seat regains the pointer capability and a
     * client has a previously obtained wl_pointer object of version 4 or
     * less, that object may start sending pointer events again. This
     * behavior is considered a misinterpretation of the intended behavior
     * and must not be relied upon by the client. wl_pointer objects of
     * version 5 or later must not send events if created before the most
     * recent event notifying the client of an added pointer capability.
     * 
     * The above behavior also applies to wl_keyboard and wl_touch with the
     * keyboard and touch capabilities, respectively.
     */
    public void capabilities(int capabilities) {
      if (instance != null)
        instance.postEvent(0, capabilities);
    }

    /** Unique identifier for this seat
     * In a multi-seat configuration the seat name can be used by clients to
     * help identify which physical devices the seat represents.
     * 
     * The seat name is a UTF-8 string with no convention defined for its
     * contents. Each name is unique among all wl_seat globals. The name is
     * only guaranteed to be unique for the current compositor instance.
     * 
     * The same seat names are used for all clients. Thus, the name can be
     * shared across processes to refer to a specific wl_seat global.
     * 
     * The name event is sent after binding to the seat global. This event is
     * only sent once per seat object, and the name does not change over the
     * lifetime of the wl_seat global.
     * 
     * Compositors may re-use the same seat name if the wl_seat global is
     * destroyed and re-created later.
     */
    public void name(String name) {
      if (instance != null && instance.getVersion() >= 2)
        instance.postEvent(1, name);
    }

    public boolean canName() {
      return instance != null && instance.getVersion() >= 2;
    }

    /** Seat capability bitmask
     * This is a bitmask of capabilities this seat has; if a member is
     * set, then it is present on the seat.
     */
    public enum Capability {
      /** The seat has pointer devices */
      POINTER(1),
      /** The seat has one or more keyboards */
      KEYBOARD(2),
      /** The seat has touch devices */
      TOUCH(4);

      public final int value;
      Capability(int value) {
        this.value = value;
      }
    }

    /** Wl_seat error values
     * These errors can be emitted in response to wl_seat requests.
     */
    public enum Error {
      /** Get_pointer, get_keyboard or get_touch called on seat without the matching capability */
      MISSING_CAPABILITY(0);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    public Seat() {}

    public Seat(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_seat", 8, new Message[] /* requests */ {
      new Message("get_pointer", "getPointer", "n", new Interface[] { Pointer.iface }), 
      new Message("get_keyboard", "getKeyboard", "n", new Interface[] { Keyboard.iface }), 
      new Message("get_touch", "getTouch", "n", new Interface[] { Touch.iface }), 
      new Message("release", "release", "5", new Interface[] {  })
    }, new Message[] /* events */ {
      new Message("capabilities", "capabilities", "u", new Interface[] { null }), 
      new Message("name", "name", "2s", new Interface[] { null })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Pointer input device
   * The wl_pointer interface represents one or more input devices,
   * such as mice, which control the pointer location and pointer_focus
   * of a seat.
   * 
   * The wl_pointer interface generates motion, enter and leave
   * events for the surfaces that the pointer is located over,
   * and button and axis events for button presses, button releases
   * and scrolling.
   */
  public static class Pointer {
    public interface Callbacks extends Resource.Callbacks {

      /** Set the pointer surface
       * Set the pointer surface, i.e., the surface that contains the
       * pointer image (cursor). This request gives the surface the role
       * of a cursor. If the surface already has another role, it raises
       * a protocol error.
       * 
       * The cursor actually changes only if the pointer
       * focus for this device is one of the requesting client's surfaces
       * or the surface parameter is the current pointer surface. If
       * there was a previous surface set with this request it is
       * replaced. If surface is NULL, the pointer image is hidden.
       * 
       * The parameters hotspot_x and hotspot_y define the position of
       * the pointer surface relative to the pointer location. Its
       * top-left corner is always at (x, y) - (hotspot_x, hotspot_y),
       * where (x, y) are the coordinates of the pointer location, in
       * surface-local coordinates.
       * 
       * On surface.attach requests to the pointer surface, hotspot_x
       * and hotspot_y are decremented by the x and y parameters
       * passed to the request. Attach must be confirmed by
       * wl_surface.commit as usual.
       * 
       * The hotspot can also be updated by passing the currently set
       * pointer surface to this request with new values for hotspot_x
       * and hotspot_y.
       * 
       * The current and pending input regions of the wl_surface are
       * cleared, and wl_surface.set_input_region is ignored until the
       * wl_surface is no longer used as the cursor. When the use as a
       * cursor ends, the current and pending input regions become
       * undefined, and the wl_surface is unmapped.
       * 
       * The serial parameter must match the latest wl_pointer.enter
       * serial number sent to the client. Otherwise the request will be
       * ignored.
       */
      public default void setCursor(int serial, /* Surface */ Resource surface, int hotspot_x, int hotspot_y) {};

      /** Release the pointer object
       * Using this request a client can tell the server that it is not going to
       * use the pointer object anymore.
       * 
       * This request destroys the pointer proxy object, so clients must not call
       * wl_pointer_destroy() after using this request.
       */
      public default void release() {};
    }

    /** Enter event
     * Notification that this seat's pointer is focused on a certain
     * surface.
     * 
     * When a seat's focus enters a surface, the pointer image
     * is undefined and a client should respond to this event by setting
     * an appropriate pointer image with the set_cursor request.
     */
    public void enter(int serial, /* Surface */ Resource surface, Fixed surface_x, Fixed surface_y) {
      if (instance != null)
        instance.postEvent(0, serial, surface, surface_x, surface_y);
    }

    /** Leave event
     * Notification that this seat's pointer is no longer focused on
     * a certain surface.
     * 
     * The leave notification is sent before the enter notification
     * for the new focus.
     */
    public void leave(int serial, /* Surface */ Resource surface) {
      if (instance != null)
        instance.postEvent(1, serial, surface);
    }

    /** Pointer motion event
     * Notification of pointer location change. The arguments
     * surface_x and surface_y are the location relative to the
     * focused surface.
     */
    public void motion(int time, Fixed surface_x, Fixed surface_y) {
      if (instance != null)
        instance.postEvent(2, time, surface_x, surface_y);
    }

    /** Pointer button event
     * Mouse button click and release notifications.
     * 
     * The location of the click is given by the last motion or
     * enter event.
     * The time argument is a timestamp with millisecond
     * granularity, with an undefined base.
     * 
     * The button is a button code as defined in the Linux kernel's
     * linux/input-event-codes.h header file, e.g. BTN_LEFT.
     * 
     * Any 16-bit button code value is reserved for future additions to the
     * kernel's event code list. All other button codes above 0xFFFF are
     * currently undefined but may be used in future versions of this
     * protocol.
     */
    public void button(int serial, int time, int button, int state) {
      if (instance != null)
        instance.postEvent(3, serial, time, button, state);
    }

    /** Axis event
     * Scroll and other axis notifications.
     * 
     * For scroll events (vertical and horizontal scroll axes), the
     * value parameter is the length of a vector along the specified
     * axis in a coordinate space identical to those of motion events,
     * representing a relative movement along the specified axis.
     * 
     * For devices that support movements non-parallel to axes multiple
     * axis events will be emitted.
     * 
     * When applicable, for example for touch pads, the server can
     * choose to emit scroll events where the motion vector is
     * equivalent to a motion event vector.
     * 
     * When applicable, a client can transform its content relative to the
     * scroll distance.
     */
    public void axis(int time, int axis, Fixed value) {
      if (instance != null)
        instance.postEvent(4, time, axis, value);
    }

    /** End of a pointer event sequence
     * Indicates the end of a set of events that logically belong together.
     * A client is expected to accumulate the data in all events within the
     * frame before proceeding.
     * 
     * All wl_pointer events before a wl_pointer.frame event belong
     * logically together. For example, in a diagonal scroll motion the
     * compositor will send an optional wl_pointer.axis_source event, two
     * wl_pointer.axis events (horizontal and vertical) and finally a
     * wl_pointer.frame event. The client may use this information to
     * calculate a diagonal vector for scrolling.
     * 
     * When multiple wl_pointer.axis events occur within the same frame,
     * the motion vector is the combined motion of all events.
     * When a wl_pointer.axis and a wl_pointer.axis_stop event occur within
     * the same frame, this indicates that axis movement in one axis has
     * stopped but continues in the other axis.
     * When multiple wl_pointer.axis_stop events occur within the same
     * frame, this indicates that these axes stopped in the same instance.
     * 
     * A wl_pointer.frame event is sent for every logical event group,
     * even if the group only contains a single wl_pointer event.
     * Specifically, a client may get a sequence: motion, frame, button,
     * frame, axis, frame, axis_stop, frame.
     * 
     * The wl_pointer.enter and wl_pointer.leave events are logical events
     * generated by the compositor and not the hardware. These events are
     * also grouped by a wl_pointer.frame. When a pointer moves from one
     * surface to another, a compositor should group the
     * wl_pointer.leave event within the same wl_pointer.frame.
     * However, a client must not rely on wl_pointer.leave and
     * wl_pointer.enter being in the same wl_pointer.frame.
     * Compositor-specific policies may require the wl_pointer.leave and
     * wl_pointer.enter event being split across multiple wl_pointer.frame
     * groups.
     */
    public void frame() {
      if (instance != null && instance.getVersion() >= 5)
        instance.postEvent(5);
    }

    public boolean canFrame() {
      return instance != null && instance.getVersion() >= 5;
    }

    /** Axis source event
     * Source information for scroll and other axes.
     * 
     * This event does not occur on its own. It is sent before a
     * wl_pointer.frame event and carries the source information for
     * all events within that frame.
     * 
     * The source specifies how this event was generated. If the source is
     * wl_pointer.axis_source.finger, a wl_pointer.axis_stop event will be
     * sent when the user lifts the finger off the device.
     * 
     * If the source is wl_pointer.axis_source.wheel,
     * wl_pointer.axis_source.wheel_tilt or
     * wl_pointer.axis_source.continuous, a wl_pointer.axis_stop event may
     * or may not be sent. Whether a compositor sends an axis_stop event
     * for these sources is hardware-specific and implementation-dependent;
     * clients must not rely on receiving an axis_stop event for these
     * scroll sources and should treat scroll sequences from these scroll
     * sources as unterminated by default.
     * 
     * This event is optional. If the source is unknown for a particular
     * axis event sequence, no event is sent.
     * Only one wl_pointer.axis_source event is permitted per frame.
     * 
     * The order of wl_pointer.axis_discrete and wl_pointer.axis_source is
     * not guaranteed.
     */
    public void axisSource(int axis_source) {
      if (instance != null && instance.getVersion() >= 5)
        instance.postEvent(6, axis_source);
    }

    public boolean canAxisSource() {
      return instance != null && instance.getVersion() >= 5;
    }

    /** Axis stop event
     * Stop notification for scroll and other axes.
     * 
     * For some wl_pointer.axis_source types, a wl_pointer.axis_stop event
     * is sent to notify a client that the axis sequence has terminated.
     * This enables the client to implement kinetic scrolling.
     * See the wl_pointer.axis_source documentation for information on when
     * this event may be generated.
     * 
     * Any wl_pointer.axis events with the same axis_source after this
     * event should be considered as the start of a new axis motion.
     * 
     * The timestamp is to be interpreted identical to the timestamp in the
     * wl_pointer.axis event. The timestamp value may be the same as a
     * preceding wl_pointer.axis event.
     */
    public void axisStop(int time, int axis) {
      if (instance != null && instance.getVersion() >= 5)
        instance.postEvent(7, time, axis);
    }

    public boolean canAxisStop() {
      return instance != null && instance.getVersion() >= 5;
    }

    /** Axis click event
     * Discrete step information for scroll and other axes.
     * 
     * This event carries the axis value of the wl_pointer.axis event in
     * discrete steps (e.g. mouse wheel clicks).
     * 
     * This event is deprecated with wl_pointer version 8 - this event is not
     * sent to clients supporting version 8 or later.
     * 
     * This event does not occur on its own, it is coupled with a
     * wl_pointer.axis event that represents this axis value on a
     * continuous scale. The protocol guarantees that each axis_discrete
     * event is always followed by exactly one axis event with the same
     * axis number within the same wl_pointer.frame. Note that the protocol
     * allows for other events to occur between the axis_discrete and
     * its coupled axis event, including other axis_discrete or axis
     * events. A wl_pointer.frame must not contain more than one axis_discrete
     * event per axis type.
     * 
     * This event is optional; continuous scrolling devices
     * like two-finger scrolling on touchpads do not have discrete
     * steps and do not generate this event.
     * 
     * The discrete value carries the directional information. e.g. a value
     * of -2 is two steps towards the negative direction of this axis.
     * 
     * The axis number is identical to the axis number in the associated
     * axis event.
     * 
     * The order of wl_pointer.axis_discrete and wl_pointer.axis_source is
     * not guaranteed.
     */
    public void axisDiscrete(int axis, int discrete) {
      if (instance != null && instance.getVersion() >= 5)
        instance.postEvent(8, axis, discrete);
    }

    public boolean canAxisDiscrete() {
      return instance != null && instance.getVersion() >= 5;
    }

    /** Axis high-resolution scroll event
     * Discrete high-resolution scroll information.
     * 
     * This event carries high-resolution wheel scroll information,
     * with each multiple of 120 representing one logical scroll step
     * (a wheel detent). For example, an axis_value120 of 30 is one quarter of
     * a logical scroll step in the positive direction, a value120 of
     * -240 are two logical scroll steps in the negative direction within the
     * same hardware event.
     * Clients that rely on discrete scrolling should accumulate the
     * value120 to multiples of 120 before processing the event.
     * 
     * The value120 must not be zero.
     * 
     * This event replaces the wl_pointer.axis_discrete event in clients
     * supporting wl_pointer version 8 or later.
     * 
     * Where a wl_pointer.axis_source event occurs in the same
     * wl_pointer.frame, the axis source applies to this event.
     * 
     * The order of wl_pointer.axis_value120 and wl_pointer.axis_source is
     * not guaranteed.
     */
    public void axisValue120(int axis, int value120) {
      if (instance != null && instance.getVersion() >= 8)
        instance.postEvent(9, axis, value120);
    }

    public boolean canAxisValue120() {
      return instance != null && instance.getVersion() >= 8;
    }

    public enum Error {
      /** Given wl_surface has another role */
      ROLE(0);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    /** Physical button state
     * Describes the physical state of a button that produced the button
     * event.
     */
    public enum ButtonState {
      /** The button is not pressed */
      RELEASED(0),
      /** The button is pressed */
      PRESSED(1);

      public final int value;
      ButtonState(int value) {
        this.value = value;
      }
    }

    /** Axis types
     * Describes the axis types of scroll events.
     */
    public enum Axis {
      /** Vertical axis */
      VERTICAL_SCROLL(0),
      /** Horizontal axis */
      HORIZONTAL_SCROLL(1);

      public final int value;
      Axis(int value) {
        this.value = value;
      }
    }

    /** Axis source types
     * Describes the source types for axis events. This indicates to the
     * client how an axis event was physically generated; a client may
     * adjust the user interface accordingly. For example, scroll events
     * from a "finger" source may be in a smooth coordinate space with
     * kinetic scrolling whereas a "wheel" source may be in discrete steps
     * of a number of lines.
     * 
     * The "continuous" axis source is a device generating events in a
     * continuous coordinate space, but using something other than a
     * finger. One example for this source is button-based scrolling where
     * the vertical motion of a device is converted to scroll events while
     * a button is held down.
     * 
     * The "wheel tilt" axis source indicates that the actual device is a
     * wheel but the scroll event is not caused by a rotation but a
     * (usually sideways) tilt of the wheel.
     */
    public enum AxisSource {
      /** A physical wheel rotation */
      WHEEL(0),
      /** Finger on a touch surface */
      FINGER(1),
      /** Continuous coordinate space */
      CONTINUOUS(2),
      /** A physical wheel tilt */
      WHEEL_TILT(3);

      public final int value;
      AxisSource(int value) {
        this.value = value;
      }
    }

    public Pointer() {}

    public Pointer(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_pointer", 8, new Message[] /* requests */ {
      new Message("set_cursor", "setCursor", "u?oii", new Interface[] { null, Surface.iface, null, null }), 
      new Message("release", "release", "3", new Interface[] {  })
    }, new Message[] /* events */ {
      new Message("enter", "enter", "uoff", new Interface[] { null, Surface.iface, null, null }), 
      new Message("leave", "leave", "uo", new Interface[] { null, Surface.iface }), 
      new Message("motion", "motion", "uff", new Interface[] { null, null, null }), 
      new Message("button", "button", "uuuu", new Interface[] { null, null, null, null }), 
      new Message("axis", "axis", "uuf", new Interface[] { null, null, null }), 
      new Message("frame", "frame", "5", new Interface[] {  }), 
      new Message("axis_source", "axisSource", "5u", new Interface[] { null }), 
      new Message("axis_stop", "axisStop", "5uu", new Interface[] { null, null }), 
      new Message("axis_discrete", "axisDiscrete", "5ui", new Interface[] { null, null }), 
      new Message("axis_value120", "axisValue120", "8ui", new Interface[] { null, null })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Keyboard input device
   * The wl_keyboard interface represents one or more keyboards
   * associated with a seat.
   */
  public static class Keyboard {
    public interface Callbacks extends Resource.Callbacks {
      /** Release the keyboard object */
      public default void release() {};
    }

    /** Keyboard mapping
     * This event provides a file descriptor to the client which can be
     * memory-mapped in read-only mode to provide a keyboard mapping
     * description.
     * 
     * From version 7 onwards, the fd must be mapped with MAP_PRIVATE by
     * the recipient, as MAP_SHARED may fail.
     */
    public void keymap(int format, int fd, int size) {
      if (instance != null)
        instance.postEvent(0, format, fd, size);
    }

    /** Enter event
     * Notification that this seat's keyboard focus is on a certain
     * surface.
     * 
     * The compositor must send the wl_keyboard.modifiers event after this
     * event.
     */
    public void enter(int serial, /* Surface */ Resource surface, Object keys) {
      if (instance != null)
        instance.postEvent(1, serial, surface, keys);
    }

    /** Leave event
     * Notification that this seat's keyboard focus is no longer on
     * a certain surface.
     * 
     * The leave notification is sent before the enter notification
     * for the new focus.
     * 
     * After this event client must assume that all keys, including modifiers,
     * are lifted and also it must stop key repeating if there's some going on.
     */
    public void leave(int serial, /* Surface */ Resource surface) {
      if (instance != null)
        instance.postEvent(2, serial, surface);
    }

    /** Key event
     * A key was pressed or released.
     * The time argument is a timestamp with millisecond
     * granularity, with an undefined base.
     * 
     * The key is a platform-specific key code that can be interpreted
     * by feeding it to the keyboard mapping (see the keymap event).
     * 
     * If this event produces a change in modifiers, then the resulting
     * wl_keyboard.modifiers event must be sent after this event.
     */
    public void key(int serial, int time, int key, int state) {
      if (instance != null)
        instance.postEvent(3, serial, time, key, state);
    }

    /** Modifier and group state
     * Notifies clients that the modifier and/or group state has
     * changed, and it should update its local state.
     */
    public void modifiers(int serial, int mods_depressed, int mods_latched, int mods_locked, int group) {
      if (instance != null)
        instance.postEvent(4, serial, mods_depressed, mods_latched, mods_locked, group);
    }

    /** Repeat rate and delay
     * Informs the client about the keyboard's repeat rate and delay.
     * 
     * This event is sent as soon as the wl_keyboard object has been created,
     * and is guaranteed to be received by the client before any key press
     * event.
     * 
     * Negative values for either rate or delay are illegal. A rate of zero
     * will disable any repeating (regardless of the value of delay).
     * 
     * This event can be sent later on as well with a new value if necessary,
     * so clients should continue listening for the event past the creation
     * of wl_keyboard.
     */
    public void repeatInfo(int rate, int delay) {
      if (instance != null && instance.getVersion() >= 4)
        instance.postEvent(5, rate, delay);
    }

    public boolean canRepeatInfo() {
      return instance != null && instance.getVersion() >= 4;
    }

    /** Keyboard mapping format
     * This specifies the format of the keymap provided to the
     * client with the wl_keyboard.keymap event.
     */
    public enum KeymapFormat {
      /** No keymap; client must understand how to interpret the raw keycode */
      NO_KEYMAP(0),
      /** Libxkbcommon compatible, null-terminated string; to determine the xkb keycode, clients must add 8 to the key event keycode */
      XKB_V1(1);

      public final int value;
      KeymapFormat(int value) {
        this.value = value;
      }
    }

    /** Physical key state
     * Describes the physical state of a key that produced the key event.
     */
    public enum KeyState {
      /** Key is not pressed */
      RELEASED(0),
      /** Key is pressed */
      PRESSED(1);

      public final int value;
      KeyState(int value) {
        this.value = value;
      }
    }

    public Keyboard() {}

    public Keyboard(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_keyboard", 8, new Message[] /* requests */ {
      new Message("release", "release", "3", new Interface[] {  })
    }, new Message[] /* events */ {
      new Message("keymap", "keymap", "uhu", new Interface[] { null, null, null }), 
      new Message("enter", "enter", "uoa", new Interface[] { null, Surface.iface, null }), 
      new Message("leave", "leave", "uo", new Interface[] { null, Surface.iface }), 
      new Message("key", "key", "uuuu", new Interface[] { null, null, null, null }), 
      new Message("modifiers", "modifiers", "uuuuu", new Interface[] { null, null, null, null, null }), 
      new Message("repeat_info", "repeatInfo", "4ii", new Interface[] { null, null })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Touchscreen input device
   * The wl_touch interface represents a touchscreen
   * associated with a seat.
   * 
   * Touch interactions can consist of one or more contacts.
   * For each contact, a series of events is generated, starting
   * with a down event, followed by zero or more motion events,
   * and ending with an up event. Events relating to the same
   * contact point can be identified by the ID of the sequence.
   */
  public static class Touch {
    public interface Callbacks extends Resource.Callbacks {
      /** Release the touch object */
      public default void release() {};
    }

    /** Touch down event and beginning of a touch sequence
     * A new touch point has appeared on the surface. This touch point is
     * assigned a unique ID. Future events from this touch point reference
     * this ID. The ID ceases to be valid after a touch up event and may be
     * reused in the future.
     */
    public void down(int serial, int time, /* Surface */ Resource surface, int id, Fixed x, Fixed y) {
      if (instance != null)
        instance.postEvent(0, serial, time, surface, id, x, y);
    }

    /** End of a touch event sequence
     * The touch point has disappeared. No further events will be sent for
     * this touch point and the touch point's ID is released and may be
     * reused in a future touch down event.
     */
    public void up(int serial, int time, int id) {
      if (instance != null)
        instance.postEvent(1, serial, time, id);
    }

    /** Update of touch point coordinates
     * A touch point has changed coordinates.
     */
    public void motion(int time, int id, Fixed x, Fixed y) {
      if (instance != null)
        instance.postEvent(2, time, id, x, y);
    }

    /** End of touch frame event
     * Indicates the end of a set of events that logically belong together.
     * A client is expected to accumulate the data in all events within the
     * frame before proceeding.
     * 
     * A wl_touch.frame terminates at least one event but otherwise no
     * guarantee is provided about the set of events within a frame. A client
     * must assume that any state not updated in a frame is unchanged from the
     * previously known state.
     */
    public void frame() {
      if (instance != null)
        instance.postEvent(3);
    }

    /** Touch session cancelled
     * Sent if the compositor decides the touch stream is a global
     * gesture. No further events are sent to the clients from that
     * particular gesture. Touch cancellation applies to all touch points
     * currently active on this client's surface. The client is
     * responsible for finalizing the touch points, future touch points on
     * this surface may reuse the touch point ID.
     */
    public void cancel() {
      if (instance != null)
        instance.postEvent(4);
    }

    /** Update shape of touch point
     * Sent when a touchpoint has changed its shape.
     * 
     * This event does not occur on its own. It is sent before a
     * wl_touch.frame event and carries the new shape information for
     * any previously reported, or new touch points of that frame.
     * 
     * Other events describing the touch point such as wl_touch.down,
     * wl_touch.motion or wl_touch.orientation may be sent within the
     * same wl_touch.frame. A client should treat these events as a single
     * logical touch point update. The order of wl_touch.shape,
     * wl_touch.orientation and wl_touch.motion is not guaranteed.
     * A wl_touch.down event is guaranteed to occur before the first
     * wl_touch.shape event for this touch ID but both events may occur within
     * the same wl_touch.frame.
     * 
     * A touchpoint shape is approximated by an ellipse through the major and
     * minor axis length. The major axis length describes the longer diameter
     * of the ellipse, while the minor axis length describes the shorter
     * diameter. Major and minor are orthogonal and both are specified in
     * surface-local coordinates. The center of the ellipse is always at the
     * touchpoint location as reported by wl_touch.down or wl_touch.move.
     * 
     * This event is only sent by the compositor if the touch device supports
     * shape reports. The client has to make reasonable assumptions about the
     * shape if it did not receive this event.
     */
    public void shape(int id, Fixed major, Fixed minor) {
      if (instance != null && instance.getVersion() >= 6)
        instance.postEvent(5, id, major, minor);
    }

    public boolean canShape() {
      return instance != null && instance.getVersion() >= 6;
    }

    /** Update orientation of touch point
     * Sent when a touchpoint has changed its orientation.
     * 
     * This event does not occur on its own. It is sent before a
     * wl_touch.frame event and carries the new shape information for
     * any previously reported, or new touch points of that frame.
     * 
     * Other events describing the touch point such as wl_touch.down,
     * wl_touch.motion or wl_touch.shape may be sent within the
     * same wl_touch.frame. A client should treat these events as a single
     * logical touch point update. The order of wl_touch.shape,
     * wl_touch.orientation and wl_touch.motion is not guaranteed.
     * A wl_touch.down event is guaranteed to occur before the first
     * wl_touch.orientation event for this touch ID but both events may occur
     * within the same wl_touch.frame.
     * 
     * The orientation describes the clockwise angle of a touchpoint's major
     * axis to the positive surface y-axis and is normalized to the -180 to
     * +180 degree range. The granularity of orientation depends on the touch
     * device, some devices only support binary rotation values between 0 and
     * 90 degrees.
     * 
     * This event is only sent by the compositor if the touch device supports
     * orientation reports.
     */
    public void orientation(int id, Fixed orientation) {
      if (instance != null && instance.getVersion() >= 6)
        instance.postEvent(6, id, orientation);
    }

    public boolean canOrientation() {
      return instance != null && instance.getVersion() >= 6;
    }

    public Touch() {}

    public Touch(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_touch", 8, new Message[] /* requests */ {
      new Message("release", "release", "3", new Interface[] {  })
    }, new Message[] /* events */ {
      new Message("down", "down", "uuoiff", new Interface[] { null, null, Surface.iface, null, null, null }), 
      new Message("up", "up", "uui", new Interface[] { null, null, null }), 
      new Message("motion", "motion", "uiff", new Interface[] { null, null, null, null }), 
      new Message("frame", "frame", "", new Interface[] {  }), 
      new Message("cancel", "cancel", "", new Interface[] {  }), 
      new Message("shape", "shape", "6iff", new Interface[] { null, null, null }), 
      new Message("orientation", "orientation", "6if", new Interface[] { null, null })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Compositor output region
   * An output describes part of the compositor geometry.  The
   * compositor works in the 'compositor coordinate system' and an
   * output corresponds to a rectangular area in that space that is
   * actually visible.  This typically corresponds to a monitor that
   * displays part of the compositor space.  This object is published
   * as global during start up, or when a monitor is hotplugged.
   */
  public static class Output {
    public interface Callbacks extends Resource.Callbacks {

      /** Release the output object
       * Using this request a client can tell the server that it is not going to
       * use the output object anymore.
       */
      public default void release() {};
    }

    /** Properties of the output
     * The geometry event describes geometric properties of the output.
     * The event is sent when binding to the output object and whenever
     * any of the properties change.
     * 
     * The physical size can be set to zero if it doesn't make sense for this
     * output (e.g. for projectors or virtual outputs).
     * 
     * The geometry event will be followed by a done event (starting from
     * version 2).
     * 
     * Note: wl_output only advertises partial information about the output
     * position and identification. Some compositors, for instance those not
     * implementing a desktop-style output layout or those exposing virtual
     * outputs, might fake this information. Instead of using x and y, clients
     * should use xdg_output.logical_position. Instead of using make and model,
     * clients should use name and description.
     */
    public void geometry(int x, int y, int physical_width, int physical_height, int subpixel, String make, String model, int transform) {
      if (instance != null)
        instance.postEvent(0, x, y, physical_width, physical_height, subpixel, make, model, transform);
    }

    /** Advertise available modes for the output
     * The mode event describes an available mode for the output.
     * 
     * The event is sent when binding to the output object and there
     * will always be one mode, the current mode.  The event is sent
     * again if an output changes mode, for the mode that is now
     * current.  In other words, the current mode is always the last
     * mode that was received with the current flag set.
     * 
     * Non-current modes are deprecated. A compositor can decide to only
     * advertise the current mode and never send other modes. Clients
     * should not rely on non-current modes.
     * 
     * The size of a mode is given in physical hardware units of
     * the output device. This is not necessarily the same as
     * the output size in the global compositor space. For instance,
     * the output may be scaled, as described in wl_output.scale,
     * or transformed, as described in wl_output.transform. Clients
     * willing to retrieve the output size in the global compositor
     * space should use xdg_output.logical_size instead.
     * 
     * The vertical refresh rate can be set to zero if it doesn't make
     * sense for this output (e.g. for virtual outputs).
     * 
     * The mode event will be followed by a done event (starting from
     * version 2).
     * 
     * Clients should not use the refresh rate to schedule frames. Instead,
     * they should use the wl_surface.frame event or the presentation-time
     * protocol.
     * 
     * Note: this information is not always meaningful for all outputs. Some
     * compositors, such as those exposing virtual outputs, might fake the
     * refresh rate or the size.
     */
    public void mode(int flags, int width, int height, int refresh) {
      if (instance != null)
        instance.postEvent(1, flags, width, height, refresh);
    }

    /** Sent all information about output
     * This event is sent after all other properties have been
     * sent after binding to the output object and after any
     * other property changes done after that. This allows
     * changes to the output properties to be seen as
     * atomic, even if they happen via multiple events.
     */
    public void done() {
      if (instance != null && instance.getVersion() >= 2)
        instance.postEvent(2);
    }

    public boolean canDone() {
      return instance != null && instance.getVersion() >= 2;
    }

    /** Output scaling properties
     * This event contains scaling geometry information
     * that is not in the geometry event. It may be sent after
     * binding the output object or if the output scale changes
     * later. If it is not sent, the client should assume a
     * scale of 1.
     * 
     * A scale larger than 1 means that the compositor will
     * automatically scale surface buffers by this amount
     * when rendering. This is used for very high resolution
     * displays where applications rendering at the native
     * resolution would be too small to be legible.
     * 
     * It is intended that scaling aware clients track the
     * current output of a surface, and if it is on a scaled
     * output it should use wl_surface.set_buffer_scale with
     * the scale of the output. That way the compositor can
     * avoid scaling the surface, and the client can supply
     * a higher detail image.
     * 
     * The scale event will be followed by a done event.
     */
    public void scale(int factor) {
      if (instance != null && instance.getVersion() >= 2)
        instance.postEvent(3, factor);
    }

    public boolean canScale() {
      return instance != null && instance.getVersion() >= 2;
    }

    /** Name of this output
     * Many compositors will assign user-friendly names to their outputs, show
     * them to the user, allow the user to refer to an output, etc. The client
     * may wish to know this name as well to offer the user similar behaviors.
     * 
     * The name is a UTF-8 string with no convention defined for its contents.
     * Each name is unique among all wl_output globals. The name is only
     * guaranteed to be unique for the compositor instance.
     * 
     * The same output name is used for all clients for a given wl_output
     * global. Thus, the name can be shared across processes to refer to a
     * specific wl_output global.
     * 
     * The name is not guaranteed to be persistent across sessions, thus cannot
     * be used to reliably identify an output in e.g. configuration files.
     * 
     * Examples of names include 'HDMI-A-1', 'WL-1', 'X11-1', etc. However, do
     * not assume that the name is a reflection of an underlying DRM connector,
     * X11 connection, etc.
     * 
     * The name event is sent after binding the output object. This event is
     * only sent once per output object, and the name does not change over the
     * lifetime of the wl_output global.
     * 
     * Compositors may re-use the same output name if the wl_output global is
     * destroyed and re-created later. Compositors should avoid re-using the
     * same name if possible.
     * 
     * The name event will be followed by a done event.
     */
    public void name(String name) {
      if (instance != null && instance.getVersion() >= 4)
        instance.postEvent(4, name);
    }

    public boolean canName() {
      return instance != null && instance.getVersion() >= 4;
    }

    /** Human-readable description of this output
     * Many compositors can produce human-readable descriptions of their
     * outputs. The client may wish to know this description as well, e.g. for
     * output selection purposes.
     * 
     * The description is a UTF-8 string with no convention defined for its
     * contents. The description is not guaranteed to be unique among all
     * wl_output globals. Examples might include 'Foocorp 11" Display' or
     * 'Virtual X11 output via :1'.
     * 
     * The description event is sent after binding the output object and
     * whenever the description changes. The description is optional, and may
     * not be sent at all.
     * 
     * The description event will be followed by a done event.
     */
    public void description(String description) {
      if (instance != null && instance.getVersion() >= 4)
        instance.postEvent(5, description);
    }

    public boolean canDescription() {
      return instance != null && instance.getVersion() >= 4;
    }

    /** Subpixel geometry information
     * This enumeration describes how the physical
     * pixels on an output are laid out.
     */
    public enum Subpixel {
      /** Unknown geometry */
      UNKNOWN(0),
      /** No geometry */
      NONE(1),
      /** Horizontal RGB */
      HORIZONTAL_RGB(2),
      /** Horizontal BGR */
      HORIZONTAL_BGR(3),
      /** Vertical RGB */
      VERTICAL_RGB(4),
      /** Vertical BGR */
      VERTICAL_BGR(5);

      public final int value;
      Subpixel(int value) {
        this.value = value;
      }
    }

    /** Transform from framebuffer to output
     * This describes the transform that a compositor will apply to a
     * surface to compensate for the rotation or mirroring of an
     * output device.
     * 
     * The flipped values correspond to an initial flip around a
     * vertical axis followed by rotation.
     * 
     * The purpose is mainly to allow clients to render accordingly and
     * tell the compositor, so that for fullscreen surfaces, the
     * compositor will still be able to scan out directly from client
     * surfaces.
     */
    public enum Transform {
      /** No transform */
      NORMAL(0),
      /** 90 degrees counter-clockwise */
      _90(1),
      /** 180 degrees counter-clockwise */
      _180(2),
      /** 270 degrees counter-clockwise */
      _270(3),
      /** 180 degree flip around a vertical axis */
      FLIPPED(4),
      /** Flip and rotate 90 degrees counter-clockwise */
      FLIPPED_90(5),
      /** Flip and rotate 180 degrees counter-clockwise */
      FLIPPED_180(6),
      /** Flip and rotate 270 degrees counter-clockwise */
      FLIPPED_270(7);

      public final int value;
      Transform(int value) {
        this.value = value;
      }
    }

    /** Mode information
     * These flags describe properties of an output mode.
     * They are used in the flags bitfield of the mode event.
     */
    public enum Mode {
      /** Indicates this is the current mode */
      CURRENT(0x1),
      /** Indicates this is the preferred mode */
      PREFERRED(0x2);

      public final int value;
      Mode(int value) {
        this.value = value;
      }
    }

    public Output() {}

    public Output(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_output", 4, new Message[] /* requests */ {
      new Message("release", "release", "3", new Interface[] {  })
    }, new Message[] /* events */ {
      new Message("geometry", "geometry", "iiiiissi", new Interface[] { null, null, null, null, null, null, null, null }), 
      new Message("mode", "mode", "uiii", new Interface[] { null, null, null, null }), 
      new Message("done", "done", "2", new Interface[] {  }), 
      new Message("scale", "scale", "2i", new Interface[] { null }), 
      new Message("name", "name", "4s", new Interface[] { null }), 
      new Message("description", "description", "4s", new Interface[] { null })
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Region interface
   * A region object describes an area.
   * 
   * Region objects are used to describe the opaque and input
   * regions of a surface.
   */
  public static class Region {
    public interface Callbacks extends Resource.Callbacks {

      /** Destroy region
       * Destroy the region.  This will invalidate the object ID.
       */
      public default void destroy() {};

      /** Add rectangle to region
       * Add the specified rectangle to the region.
       */
      public default void add(int x, int y, int width, int height) {};

      /** Subtract rectangle from region
       * Subtract the specified rectangle from the region.
       */
      public default void subtract(int x, int y, int width, int height) {};
    }

    public Region() {}

    public Region(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_region", 1, new Message[] /* requests */ {
      new Message("destroy", "destroy", "", new Interface[] {  }), 
      new Message("add", "add", "iiii", new Interface[] { null, null, null, null }), 
      new Message("subtract", "subtract", "iiii", new Interface[] { null, null, null, null })
    }, new Message[] /* events */ {
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Sub-surface compositing
   * The global interface exposing sub-surface compositing capabilities.
   * A wl_surface, that has sub-surfaces associated, is called the
   * parent surface. Sub-surfaces can be arbitrarily nested and create
   * a tree of sub-surfaces.
   * 
   * The root surface in a tree of sub-surfaces is the main
   * surface. The main surface cannot be a sub-surface, because
   * sub-surfaces must always have a parent.
   * 
   * A main surface with its sub-surfaces forms a (compound) window.
   * For window management purposes, this set of wl_surface objects is
   * to be considered as a single window, and it should also behave as
   * such.
   * 
   * The aim of sub-surfaces is to offload some of the compositing work
   * within a window from clients to the compositor. A prime example is
   * a video player with decorations and video in separate wl_surface
   * objects. This should allow the compositor to pass YUV video buffer
   * processing to dedicated overlay hardware when possible.
   */
  public static class Subcompositor {
    public interface Callbacks extends Resource.Callbacks {

      /** Unbind from the subcompositor interface
       * Informs the server that the client will not be using this
       * protocol object anymore. This does not affect any other
       * objects, wl_subsurface objects included.
       */
      public default void destroy() {};

      /** Give a surface the role sub-surface
       * Create a sub-surface interface for the given surface, and
       * associate it with the given parent surface. This turns a
       * plain wl_surface into a sub-surface.
       * 
       * The to-be sub-surface must not already have another role, and it
       * must not have an existing wl_subsurface object. Otherwise a protocol
       * error is raised.
       * 
       * Adding sub-surfaces to a parent is a double-buffered operation on the
       * parent (see wl_surface.commit). The effect of adding a sub-surface
       * becomes visible on the next time the state of the parent surface is
       * applied.
       * 
       * This request modifies the behaviour of wl_surface.commit request on
       * the sub-surface, see the documentation on wl_subsurface interface.
       */
      public default void getSubsurface(/* Subsurface */ Resource id, /* Surface */ Resource surface, /* Surface */ Resource parent) {};
    }

    public enum Error {
      /** The to-be sub-surface is invalid */
      BAD_SURFACE(0);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    public Subcompositor() {}

    public Subcompositor(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_subcompositor", 1, new Message[] /* requests */ {
      new Message("destroy", "destroy", "", new Interface[] {  }), 
      new Message("get_subsurface", "getSubsurface", "noo", new Interface[] { Subsurface.iface, Surface.iface, Surface.iface })
    }, new Message[] /* events */ {
    }, Callbacks.class);

    public Resource instance = null;
  }

  /** Sub-surface interface to a wl_surface
   * An additional interface to a wl_surface object, which has been
   * made a sub-surface. A sub-surface has one parent surface. A
   * sub-surface's size and position are not limited to that of the parent.
   * Particularly, a sub-surface is not automatically clipped to its
   * parent's area.
   * 
   * A sub-surface becomes mapped, when a non-NULL wl_buffer is applied
   * and the parent surface is mapped. The order of which one happens
   * first is irrelevant. A sub-surface is hidden if the parent becomes
   * hidden, or if a NULL wl_buffer is applied. These rules apply
   * recursively through the tree of surfaces.
   * 
   * The behaviour of a wl_surface.commit request on a sub-surface
   * depends on the sub-surface's mode. The possible modes are
   * synchronized and desynchronized, see methods
   * wl_subsurface.set_sync and wl_subsurface.set_desync. Synchronized
   * mode caches the wl_surface state to be applied when the parent's
   * state gets applied, and desynchronized mode applies the pending
   * wl_surface state directly. A sub-surface is initially in the
   * synchronized mode.
   * 
   * Sub-surfaces also have another kind of state, which is managed by
   * wl_subsurface requests, as opposed to wl_surface requests. This
   * state includes the sub-surface position relative to the parent
   * surface (wl_subsurface.set_position), and the stacking order of
   * the parent and its sub-surfaces (wl_subsurface.place_above and
   * .place_below). This state is applied when the parent surface's
   * wl_surface state is applied, regardless of the sub-surface's mode.
   * As the exception, set_sync and set_desync are effective immediately.
   * 
   * The main surface can be thought to be always in desynchronized mode,
   * since it does not have a parent in the sub-surfaces sense.
   * 
   * Even if a sub-surface is in desynchronized mode, it will behave as
   * in synchronized mode, if its parent surface behaves as in
   * synchronized mode. This rule is applied recursively throughout the
   * tree of surfaces. This means, that one can set a sub-surface into
   * synchronized mode, and then assume that all its child and grand-child
   * sub-surfaces are synchronized, too, without explicitly setting them.
   * 
   * If the wl_surface associated with the wl_subsurface is destroyed, the
   * wl_subsurface object becomes inert. Note, that destroying either object
   * takes effect immediately. If you need to synchronize the removal
   * of a sub-surface to the parent surface update, unmap the sub-surface
   * first by attaching a NULL wl_buffer, update parent, and then destroy
   * the sub-surface.
   * 
   * If the parent wl_surface object is destroyed, the sub-surface is
   * unmapped.
   */
  public static class Subsurface {
    public interface Callbacks extends Resource.Callbacks {

      /** Remove sub-surface interface
       * The sub-surface interface is removed from the wl_surface object
       * that was turned into a sub-surface with a
       * wl_subcompositor.get_subsurface request. The wl_surface's association
       * to the parent is deleted, and the wl_surface loses its role as
       * a sub-surface. The wl_surface is unmapped immediately.
       */
      public default void destroy() {};

      /** Reposition the sub-surface
       * This schedules a sub-surface position change.
       * The sub-surface will be moved so that its origin (top left
       * corner pixel) will be at the location x, y of the parent surface
       * coordinate system. The coordinates are not restricted to the parent
       * surface area. Negative values are allowed.
       * 
       * The scheduled coordinates will take effect whenever the state of the
       * parent surface is applied. When this happens depends on whether the
       * parent surface is in synchronized mode or not. See
       * wl_subsurface.set_sync and wl_subsurface.set_desync for details.
       * 
       * If more than one set_position request is invoked by the client before
       * the commit of the parent surface, the position of a new request always
       * replaces the scheduled position from any previous request.
       * 
       * The initial position is 0, 0.
       */
      public default void setPosition(int x, int y) {};

      /** Restack the sub-surface
       * This sub-surface is taken from the stack, and put back just
       * above the reference surface, changing the z-order of the sub-surfaces.
       * The reference surface must be one of the sibling surfaces, or the
       * parent surface. Using any other surface, including this sub-surface,
       * will cause a protocol error.
       * 
       * The z-order is double-buffered. Requests are handled in order and
       * applied immediately to a pending state. The final pending state is
       * copied to the active state the next time the state of the parent
       * surface is applied. When this happens depends on whether the parent
       * surface is in synchronized mode or not. See wl_subsurface.set_sync and
       * wl_subsurface.set_desync for details.
       * 
       * A new sub-surface is initially added as the top-most in the stack
       * of its siblings and parent.
       */
      public default void placeAbove(/* Surface */ Resource sibling) {};

      /** Restack the sub-surface
       * The sub-surface is placed just below the reference surface.
       * See wl_subsurface.place_above.
       */
      public default void placeBelow(/* Surface */ Resource sibling) {};

      /** Set sub-surface to synchronized mode
       * Change the commit behaviour of the sub-surface to synchronized
       * mode, also described as the parent dependent mode.
       * 
       * In synchronized mode, wl_surface.commit on a sub-surface will
       * accumulate the committed state in a cache, but the state will
       * not be applied and hence will not change the compositor output.
       * The cached state is applied to the sub-surface immediately after
       * the parent surface's state is applied. This ensures atomic
       * updates of the parent and all its synchronized sub-surfaces.
       * Applying the cached state will invalidate the cache, so further
       * parent surface commits do not (re-)apply old state.
       * 
       * See wl_subsurface for the recursive effect of this mode.
       */
      public default void setSync() {};

      /** Set sub-surface to desynchronized mode
       * Change the commit behaviour of the sub-surface to desynchronized
       * mode, also described as independent or freely running mode.
       * 
       * In desynchronized mode, wl_surface.commit on a sub-surface will
       * apply the pending state directly, without caching, as happens
       * normally with a wl_surface. Calling wl_surface.commit on the
       * parent surface has no effect on the sub-surface's wl_surface
       * state. This mode allows a sub-surface to be updated on its own.
       * 
       * If cached state exists when wl_surface.commit is called in
       * desynchronized mode, the pending state is added to the cached
       * state, and applied as a whole. This invalidates the cache.
       * 
       * Note: even if a sub-surface is set to desynchronized, a parent
       * sub-surface may override it to behave as synchronized. For details,
       * see wl_subsurface.
       * 
       * If a surface's parent surface behaves as desynchronized, then
       * the cached state is applied on set_desync.
       */
      public default void setDesync() {};
    }

    public enum Error {
      /** Wl_surface is not a sibling or the parent */
      BAD_SURFACE(0);

      public final int value;
      Error(int value) {
        this.value = value;
      }
    }

    public Subsurface() {}

    public Subsurface(Resource id) {
      id.setCallbacks(new Callbacks() {});
    }
    public static final Interface iface = Interface.create("wl_subsurface", 1, new Message[] /* requests */ {
      new Message("destroy", "destroy", "", new Interface[] {  }), 
      new Message("set_position", "setPosition", "ii", new Interface[] { null, null }), 
      new Message("place_above", "placeAbove", "o", new Interface[] { Surface.iface }), 
      new Message("place_below", "placeBelow", "o", new Interface[] { Surface.iface }), 
      new Message("set_sync", "setSync", "", new Interface[] {  }), 
      new Message("set_desync", "setDesync", "", new Interface[] {  })
    }, new Message[] /* events */ {
    }, Callbacks.class);

    public Resource instance = null;
  }
}
