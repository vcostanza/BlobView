package software.blob.ui.view.listener;

import software.blob.ui.view.View;

import java.awt.event.MouseEvent;

/**
 * Click listener for views
 */
public interface ClickListener {

    /**
     * View has been clicked
     * @param view View
     * @param event Mouse event
     */
    void onClick(View view, MouseEvent event);
}
