package software.blob.ui.view.drag;

import software.blob.ui.util.ComponentFilter;

import java.awt.*;

/**
 * Listener for drag-and-drop events
 */
public interface DragListener extends ComponentFilter {

    /**
     * Called when a drag-and-drop {@link DragEvent} occurs
     * @param drag The component that is being dragged
     * @param event The current drag event
     * @return True if the drag event was handled
     */
    boolean onDrag(Component drag, DragEvent event);
}
