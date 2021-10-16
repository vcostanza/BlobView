package software.blob.ui.view.listener;

import software.blob.ui.view.View;

import java.awt.event.MouseEvent;

/**
 * Double-click listener for views
 */
public interface DoubleClickListener {

    /**
     * View has been double-clicked
     * @param view View
     * @param event Mouse event
     */
    void onDoubleClick(View view, MouseEvent event);
}
