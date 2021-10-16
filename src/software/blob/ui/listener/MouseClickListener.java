package software.blob.ui.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public interface MouseClickListener extends MouseListener {

    @Override
    default void mousePressed(MouseEvent evt) {
    }

    @Override
    default void mouseReleased(MouseEvent evt) {
    }

    @Override
    default void mouseEntered(MouseEvent evt) {
    }

    @Override
    default void mouseExited(MouseEvent evt) {
    }
}
