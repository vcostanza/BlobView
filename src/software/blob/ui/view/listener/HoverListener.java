package software.blob.ui.view.listener;

import software.blob.ui.view.View;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Listens for mouse events when the cursor is hovered over a view
 */
public interface HoverListener {

    /**
     * Mouse has started hovering over a view
     * @param view View that is being hovered over
     * @param event Mouse event
     */
    void onHoverStart(View view, MouseEvent event);

    /**
     * Mouse has finished hovering over a view
     * @param view View that is no longer being hovered over
     * @param event Mouse event
     */
    void onHoverEnd(View view, MouseEvent event);
}
