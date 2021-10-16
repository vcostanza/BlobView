package software.blob.ui.view.menu;

import software.blob.ui.res.Resources;
import software.blob.ui.view.AttributeSet;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Menu item that supports layout attributes
 */
public class MenuItemView extends JMenuItem {

    public MenuItemView(String title) {
        super(title);
    }

    public MenuItemView(AttributeSet attrs) {
        super(attrs.getString("title", ""));
        setName(attrs.getString("name", ""));

        String iconUri = attrs.getString("icon", null);
        if (iconUri != null)
            setIcon(Resources.getIcon(iconUri));

        String hotkey = attrs.getString("hotkey", null);
        if (hotkey != null) {
            String[] keys = hotkey.toLowerCase().split("\\+");
            int modifiers = 0;
            int keyCode = 0;
            for (String key : keys) {
                switch (key) {
                    case "ctrl":
                        modifiers |= InputEvent.CTRL_DOWN_MASK;
                        break;
                    case "alt":
                        modifiers |= InputEvent.ALT_DOWN_MASK;
                        break;
                    case "shift":
                        modifiers |= InputEvent.SHIFT_DOWN_MASK;
                        break;
                    default:
                        keyCode = getKeyCode(key);
                        break;
                }
            }
            if (keyCode != 0)
                setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        }
    }

    private static int getKeyCode(String key) {
        if (key.isEmpty())
            return 0;

        char c = key.charAt(0);
        int len = key.length();

        // Characters
        if (len == 1) {
            if (c >= 'a' && c <= 'z')
                return KeyEvent.VK_A + (c - 'a');
            if (c >= '0' && c <= '9')
                return KeyEvent.VK_0 + (c - '0');
            switch (c) {
                case '=':
                    return KeyEvent.VK_EQUALS;
                case '-':
                    return KeyEvent.VK_MINUS;
                case '`':
                    return KeyEvent.VK_DEAD_TILDE;
                case ';':
                    return KeyEvent.VK_SEMICOLON;
                case '.':
                    return KeyEvent.VK_PERIOD;
                case ',':
                    return KeyEvent.VK_COMMA;
                case ']':
                    return KeyEvent.VK_CLOSE_BRACKET;
                case '[':
                    return KeyEvent.VK_OPEN_BRACKET;
                case '\\':
                    return KeyEvent.VK_BACK_SLASH;
            }
            return 0;
        }

        // Function keys
        if (len == 2 && c == 'f')
            return KeyEvent.VK_F1 + Integer.parseInt(key.substring(1)) - 1;

        // Special keys
        switch (key) {
            case "space":
                return KeyEvent.VK_SPACE;
            case "tab":
                return KeyEvent.VK_TAB;
            case "backspace":
                return KeyEvent.VK_BACK_SPACE;
            case "enter":
                return KeyEvent.VK_ENTER;
            case "left":
                return KeyEvent.VK_LEFT;
            case "up":
                return KeyEvent.VK_UP;
            case "right":
                return KeyEvent.VK_RIGHT;
            case "down":
                return KeyEvent.VK_DOWN;
            case "pgup":
                return KeyEvent.VK_PAGE_UP;
            case "pgdn":
                return KeyEvent.VK_PAGE_DOWN;
            case "home":
                return KeyEvent.VK_HOME;
            case "end":
                return KeyEvent.VK_END;
            case "delete":
                return KeyEvent.VK_DELETE;
            case "escape":
                return KeyEvent.VK_ESCAPE;
        }

        return 0;
    }
}
