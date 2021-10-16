package software.blob.ui.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Combo box with filter search bar at the top (using {@link ScrollPopupMenu})
 * This is literally just a button attached to a popup menu with some combo box-esque calls
 */
public class FilterComboBox extends SelectButton {

    private List<String> choices;
    private String selected;
    private Frame frame;
    private Window window;
    private ScrollPopupMenu popupMenu;
    private ActionListener listener;

    public FilterComboBox() {
        setFocusable(false);
        setClickListener((e) -> showMenu());
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    public void showMenu(Component owner, int x, int y) {
        if (this.choices == null)
            return;
        if (this.popupMenu != null && this.popupMenu.isShowing()) {
            this.popupMenu.dispose();
            this.popupMenu = null;
            return;
        }
        this.popupMenu = new ScrollPopupMenu(this.window, this.frame, this.choices, (ActionEvent e) -> {
            setSelectedItem(e.getActionCommand());
            if (this.listener != null)
                this.listener.actionPerformed(e);
        });
        this.popupMenu.show(owner, x, y);
        if (this.selected != null)
            this.popupMenu.scrollTo(this.selected);
    }

    public void showMenu(Component owner) {
        showMenu(owner, 0, getHeight());
    }

    public void showMenu() {
        showMenu(this);
    }

    public void setSelectedIndex(int index) {
        setText(this.selected = this.choices.get(index));
    }

    public void setSelectedItem(String item) {
        setText(this.selected = item);
    }

    public String getSelectedItem() {
        return this.selected;
    }

    public int getSelectedIndex() {
        String name = getSelectedItem();
        if (name == null)
            return -1;
        int idx = name.indexOf(':');
        if (idx == -1)
            return -1;
        try {
            return Integer.parseInt(name.substring(0, idx));
        } catch (Exception ignored) {
        }
        return -1;
    }

    public void select(int index) {
        setSelectedIndex(index);
        if (this.listener != null)
            this.listener.actionPerformed(new ActionEvent(this, index, getSelectedItem()));
    }
}
