package software.blob.ui.view.layout;

import software.blob.ui.view.AttributeSet;
import software.blob.ui.view.View;
import software.blob.ui.view.drag.DragAndDrop;
import software.blob.ui.view.drag.DragListener;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Base layout that other layouts should extend off of
 */
public abstract class AbstractLayout extends View implements Scrollable, PropertyChangeListener {

    protected boolean bulkOperation;

    // Map child name -> child - see findChildByName()
    protected final Map<String, Component> childMap = new HashMap<>();

    // Drag-and-drop handling
    protected final DragAndDrop dnd = new DragAndDrop(this);

    protected AbstractLayout() {
        super();
    }

    protected AbstractLayout(AttributeSet attrs) {
        super(attrs);
    }

    /**
     * Set whether the layout manager should take a break while we do some bulk operation (optimizes performance)
     * @param enabled True if enabled
     */
    public void setBulkOperation(boolean enabled) {
        this.bulkOperation = enabled;
    }

    public boolean inBulkOperation() {
        return this.bulkOperation;
    }

    /**
     * Find a child component by its name
     * @param componentName Component name
     * @param <T> Inferred by left-side assignment type
     * @return Child component or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T findChildByName(String componentName) {
        // Check current container's children
        T ret = null;
        try {
            synchronized (childMap) {
                ret = (T) childMap.get(componentName);
            }
        } catch (Exception ignored) {
        }
        // Check descendants
        if (ret == null) {
            for (Component child : getComponents()) {
                if (child instanceof AbstractLayout) {
                    T match = ((AbstractLayout) child).findChildByName(componentName);
                    if (match != null)
                        return match;
                }
            }
        }
        return ret;
    }

    /**
     * Call {@link #invalidate()} on the layout manager directly without triggering Swing base UI code
     */
    public void invalidateLayout() {
        LayoutManager lm = getLayout();
        if (lm instanceof LayoutManager2)
            ((LayoutManager2) lm).invalidateLayout(this);
    }

    /**
     * Set gravity flags
     * @param gravity Gravity flags
     */
    public void setGravity(int gravity) {
        super.setGravity(gravity);
        invalidateLayout();
    }

    /**
     * Add a drag-and-drop listener to a root layout
     * @param view Component to add the drag hook to
     * @param listener Drag-and-drop listener
     */
    public void addOnDragListener(Component view, DragListener listener) {
        this.dnd.addListener(view, listener);
    }

    /**
     * Remove a drag-and-drop listener from a root layout
     * @param view Component to remove the drag hook from
     * @param listener Drag-and-drop listener
     */
    public void removeOnDragListener(Component view, DragListener listener) {
        this.dnd.removeListener(view, listener);
    }

    @Override
    public Component add(Component c) {
        LayoutParams lp;
        if (c instanceof AbstractLayout)
            lp = ((AbstractLayout) c).getLayoutParams();
        else
            lp = new LayoutParams();
        super.add(c, lp);
        return c;
    }

    @Override
    protected void addImpl(Component c, Object constraints, int index) {
        // Register child by name
        registerChildName(c);
        c.addPropertyChangeListener("name", this);

        if (c instanceof AbstractLayout && constraints instanceof LayoutParams) {
            // Layout params override
            ((AbstractLayout) c).setLayoutParams((LayoutParams) constraints);
        }

        super.addImpl(c, constraints, index);
    }

    @Override
    public void remove(int index) {
        // Unregister child by name
        Component child = getComponent(index);
        if (child != null) {
            synchronized (childMap) {
                childMap.remove(child.getName());
            }
        }

        super.remove(index);
    }

    @Override
    public void removeAll() {
        synchronized (childMap) {
            childMap.clear();
        }
        super.removeAll();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Check for name change
        Object source = evt.getSource();
        if (source instanceof Component && "name".equals(evt.getPropertyName())) {
            Component c = (Component) source;
            String oldName = String.valueOf(evt.getOldValue());
            synchronized (childMap) {
                Component oldComp = childMap.get(oldName);
                if (oldComp == c)
                    childMap.remove(oldName);
                registerChildName(c);
            }
        }
    }

    @Override
    public void paint(Graphics gfx) {
        super.paint(gfx);

        // Drag-and-drop renderer
        this.dnd.paint(gfx);
    }

    private void registerChildName(Component c) {
        String cName = c.getName();
        if (cName != null && !cName.isEmpty()) {
            synchronized (childMap) {
                childMap.put(cName, c);
            }
        }
    }
}
