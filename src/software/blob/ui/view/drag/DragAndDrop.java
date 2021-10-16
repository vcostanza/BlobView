package software.blob.ui.view.drag;

import software.blob.ui.util.ComponentSearch;
import software.blob.ui.view.layout.AbstractLayout;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Handles drag-and-drop functionality
 */
public class DragAndDrop implements MouseListener, MouseMotionListener {

    private final AbstractLayout root;

    // Component that is currently being dragged
    private Component drag;

    // Component that is current being hovered over per each listener
    private List<Component> drop;

    // Views with their respective listeners
    private final WeakHashMap<Component, List<DragListener>> listenerMap = new WeakHashMap<>();

    // The list of drag listeners for the current component
    private List<DragListener> listeners;

    // Draw variables
    private Point dragOffset;
    private BufferedImage dragImgBuffer;
    private final AlphaComposite dragAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);

    public DragAndDrop(AbstractLayout root) {
        this.root = root;
    }

    /**
     * Add a drag-and-drop listener to the root layout
     * @param view Component to add the drag hook to
     * @param listener Drag-and-drop listener
     */
    public void addListener(Component view, DragListener listener) {
        List<DragListener> listeners = listenerMap.get(view);
        if (listeners == null) {
            listenerMap.put(view, listeners = new ArrayList<>());
            view.addMouseListener(this);
            view.addMouseMotionListener(this);
        }
        listeners.add(listener);
    }

    /**
     * Remove a drag-and-drop listener from the root layout
     * @param view Component to remove the drag hook from
     * @param listener Drag-and-drop listener
     */
    public void removeListener(Component view, DragListener listener) {
        List<DragListener> listeners = listenerMap.get(view);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listenerMap.remove(view);
                view.removeMouseListener(this);
                view.removeMouseMotionListener(this);
            }
        }
    }

    /**
     * Paint drag-and-drop view over the root view
     * This should be called last within {@link Component#paint(Graphics)}
     * @param gfx Graphics instance
     */
    public void paint(Graphics gfx) {
        if (drag == null || !(gfx instanceof Graphics2D))
            return;

        // Mouse must be on screen
        Point point = root.getMousePosition();
        if (point == null)
            return;

        // Create the image buffer that we draw the view to
        // This is better than calling paint w/ the main graphics context because
        // the alpha is blended flat instead of between each layer of the view
        int dWidth = drag.getWidth(), dHeight = drag.getHeight();
        if (dragImgBuffer == null || dragImgBuffer.getWidth() != dWidth
                || dragImgBuffer.getHeight() != dHeight)
            dragImgBuffer = new BufferedImage(dWidth, dHeight, BufferedImage.TYPE_4BYTE_ABGR);

        // Draw the view to the image buffer
        drag.paint(dragImgBuffer.getGraphics());

        // Apply alpha composite and render the image
        Graphics2D g = (Graphics2D) gfx;
        g.setComposite(dragAlpha);
        g.drawImage(dragImgBuffer, null, point.x - dragOffset.x, point.y - dragOffset.y);
    }

    private DragEvent createEvent(DragEventType type, DragListener listener) {
        Point pt = root.getMousePosition();
        ComponentSearch search = new ComponentSearch();
        search.point = pt;
        search.filter = listener;
        Component drop = search.findComponent(root);
        if (drop != null)
            pt = drop.getMousePosition();
        return new DragEvent(type, pt, drop);
    }

    private void fireEvent(DragEvent event, DragListener listener) {
        listener.onDrag(drag, event);
    }

    private void fireEvent(DragEventType type, DragListener listener) {
        fireEvent(createEvent(type, listener), listener);
    }

    private void fireEvents(DragEventType type) {
        if (listeners != null) {
            for (DragListener l : listeners)
                fireEvent(type, l);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Setup new drag-and-drop event
            this.drag = e.getComponent();
            this.dragOffset = drag.getMousePosition();
            this.listeners = listenerMap.get(this.drag);
            if (this.listeners != null) {
                this.drop = new ArrayList<>(listeners.size());
                for (DragListener ignored : listeners)
                    this.drop.add(null);
            }
            fireEvents(DragEventType.START);
            root.repaint();
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && this.drag == e.getComponent()) {
            fireEvents(DragEventType.DROP);
            this.drag = null;
            this.dragOffset = null;
            this.listeners = null;
            this.drop = null;
            root.repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.drag == e.getComponent() && listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                DragListener l = listeners.get(i);
                DragEvent event = createEvent(DragEventType.DRAG, l);
                if (this.drop != null) {
                    Component drop = this.drop.get(i);
                    if (drop != null)
                        fireEvent(new DragEvent(DragEventType.EXIT, event.point, drop), l);
                    this.drop.set(i, event.drop);
                    fireEvent(new DragEvent(DragEventType.ENTER, event.point, event.drop), l);
                }
                fireEvent(event, l);
            }
            root.repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
