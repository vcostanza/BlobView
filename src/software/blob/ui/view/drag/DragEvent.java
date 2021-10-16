package software.blob.ui.view.drag;

import java.awt.*;

/**
 * An event generated for use with a {@link DragListener}
 */
public class DragEvent {

    // The type of drag event
    public final DragEventType type;

    // The current point
    public final Point point;

    // The component that is under the cursor
    public final Component drop;

    public DragEvent(DragEventType type, Point point, Component drop) {
        this.type = type;
        this.point = point;
        this.drop = drop;
    }
}
