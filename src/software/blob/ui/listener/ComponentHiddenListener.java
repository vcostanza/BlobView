package software.blob.ui.listener;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Shortcut interface for the {@link #componentHidden(ComponentEvent)} method
 */
public interface ComponentHiddenListener extends ComponentListener {
    @Override
    default void componentResized(ComponentEvent e) {
    }

    @Override
    default void componentMoved(ComponentEvent e) {
    }

    @Override
    default void componentShown(ComponentEvent e) {
    }
}
