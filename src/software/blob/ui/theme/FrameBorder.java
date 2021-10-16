package software.blob.ui.theme;

import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;
import java.awt.*;

public class FrameBorder extends AbstractBorder implements UIResource {

    private static final Color COLOR_BORDER = DarkTheme.GRAY_85;

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        g.setColor(COLOR_BORDER);
        g.drawLine(x+1, y, x+w-2, y);
        g.drawLine(x, y+1, x, y +h-2);
        g.drawLine(x+w-1, y+1, x+w-1, y+h-2);
        g.drawLine(x+1, y+h-1, x+w-2, y+h-1);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets newInsets) {
        newInsets.set(5, 5, 5, 5);
        return newInsets;
    }
}
