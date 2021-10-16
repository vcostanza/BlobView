package software.blob.ui.view.layout;

import software.blob.ui.view.AttributeSet;

import javax.swing.*;
import java.awt.*;

/**
 * Scroll pane tweaked to update child viewport bounds properly
 */
public class ScrollLayout extends JScrollPane {

    public ScrollLayout() {
        super();
    }

    public ScrollLayout(Component view) {
        super(view);
    }

    public ScrollLayout(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
    }

    public ScrollLayout(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
    }

    public ScrollLayout(AttributeSet attrs) {
        setName(attrs.getString("name", ""));
        getVerticalScrollBar().setUnitIncrement(attrs.getInteger("unitIncrement", 16));

        Color background = attrs.getColor("background", null);
        if (background != null)
            setBackground(background);

        // Enabled scrollbars
        // If this field is omitted then both scrollbars are shown when needed
        String scrollbars = attrs.getString("scrollbars", null);
        if (scrollbars != null) {
            String[] bars = scrollbars.split("\\|");
            boolean horizontal = false, vertical = false;
            for (String b : bars) {
                if (b.equals("horizontal"))
                    horizontal = true;
                else if (b.equals("vertical"))
                    vertical = true;
            }
            if (!vertical)
                setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
            if (!horizontal)
                setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        }

        setVisible(attrs.getBoolean("visible", true));
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        getViewport().setBackground(color);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        boolean resized = getWidth() != width || getHeight() != height;
        super.setBounds(x, y, width, height);
        if (resized) {
            Component c = getViewport().getView();
            if (c != null)
                c.invalidate();
        }
    }
}
