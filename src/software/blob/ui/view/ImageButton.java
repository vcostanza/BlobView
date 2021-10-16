package software.blob.ui.view;

import software.blob.ui.view.listener.HoverListener;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Image-based clickable button
 */
public class ImageButton extends ImageView implements HoverListener {

    protected Color defaultBG, hoverBG, disabledBG;
    protected boolean hovering;

    public ImageButton(AttributeSet attrs) {
        super(attrs);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setOnHoverListener(this);
    }

    public ImageButton() {
        this(new AttributeSet());
    }

    /**
     * Set the current background color without affecting the base color
     * @param bg Background color
     */
    protected void setCurrentBackground(Color bg) {
        super.setBackground(bg);
    }

    /**
     * Update the current displayed background based on the button state
     */
    protected void updateBackground() {
        setCurrentBackground(!isEnabled() ? disabledBG : (hovering ? hoverBG : defaultBG));
    }

    public Color getDefaultBackground() {
        return this.defaultBG;
    }

    @Override
    public void setBackground(Color c) {
        this.defaultBG = c;
        this.hoverBG = c != null ? c.brighter() : null;
        this.disabledBG = c != null ? hoverBG.brighter() : null;
        updateBackground();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateBackground();
    }

    @Override
    public void onHoverStart(View v, MouseEvent e) {
        hovering = true;
        updateBackground();
    }

    @Override
    public void onHoverEnd(View v, MouseEvent e) {
        hovering = false;
        updateBackground();
    }
}
