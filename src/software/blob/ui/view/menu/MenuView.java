package software.blob.ui.view.menu;

import software.blob.ui.res.Resources;
import software.blob.ui.view.AttributeSet;

import javax.swing.*;

/**
 * JMenu with support for layout attributes
 */
public class MenuView extends JMenu {

    public MenuView(AttributeSet attrs) {
        super(attrs.getString("title", ""));
        setName(attrs.getString("name", ""));

        String iconUri = attrs.getString("icon", null);
        if (iconUri != null)
            setIcon(Resources.getIcon(iconUri));
    }
}
