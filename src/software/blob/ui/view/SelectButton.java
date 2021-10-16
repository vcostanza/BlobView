package software.blob.ui.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Non-focusable button that shows a hand cursor when hovering over it
 */
public class SelectButton extends JButton implements MouseListener {

    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    private static final long DBL_CLICK_INTERVAL = 300;

    public interface OnClickListener {
        void onClick(MouseEvent e);
    }

    public interface OnDoubleClickListener {
        void onDoubleClick(MouseEvent e);
    }

    protected Color defaultBG, hoverBG, selectedBG;
    protected boolean hover;
    protected OnClickListener onClick;
    protected OnDoubleClickListener onDblClick;
    protected Map<Integer, Long> lastClickTimes = new HashMap<>();

    public SelectButton() {
        setCursor(HAND_CURSOR);
        setFocusable(false);
        addMouseListener(this);
        setDefaultBackground(getBackground());
    }

    public SelectButton(String text) {
        this();
        setText(text);
    }

    public SelectButton(AttributeSet attrs) {
        this();
        setName(attrs.getString("name", ""));
        setText(attrs.getString("text", ""));
    }

    public void setClickListener(OnClickListener l) {
        this.onClick = l;
    }

    public void setDoubleClickListener(OnDoubleClickListener l) {
        this.onDblClick = l;
    }

    public void setDefaultBackground(Color color) {
        this.defaultBG = color;
        this.selectedBG = color.darker();
        this.hoverBG = color.brighter();
        updateStyle();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        updateStyle();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        long time = System.currentTimeMillis();
        int button = e.getButton();
        Long lastTime = lastClickTimes.get(button);
        if (this.onDblClick != null && lastTime != null && time - lastTime < DBL_CLICK_INTERVAL)
            onDblClick.onDoubleClick(e);
        else if (this.onClick != null)
            onClick.onClick(e);
        lastClickTimes.put(button, time);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        hover = true;
        updateStyle();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hover = false;
        updateStyle();
    }

    protected void updateStyle() {
        Color bg = hover ? hoverBG : (isSelected() ? selectedBG : defaultBG);
        setBackground(bg);
    }
}
