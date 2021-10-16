package software.blob.ui.view.drag;

/**
 * Types of drag events
 */
public enum DragEventType {

    START, // Drag has been started
    DRAG, // Mouse is being moved
    ENTER, // Mouse has entered a new component
    EXIT, // Mouse has exited a component that was previously entered
    DROP // Component was dropped
}
