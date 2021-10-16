package software.blob.ui.view;

import software.blob.ui.listener.MouseClickListener;
import software.blob.ui.view.layout.InflatedLayout;
import software.blob.ui.view.layout.LayoutInflater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Popup menu with a search bar
 */
public class ScrollPopupMenu extends JDialog implements WindowFocusListener, KeyListener, MouseMotionListener {

    private final JTextField searchText;
    private final ActionListener listener;
    private JList<String> list;
    private final List<String> entries;

    private volatile String searchTerms;

    public ScrollPopupMenu(Window window, Frame frame, List<String> entries, ActionListener listener) {
        super(frame != null ? frame : window);

        this.entries = entries;
        this.listener = listener;

        InflatedLayout inf = LayoutInflater.inflate("scroll_popup");
        searchText = inf.findByName("searchText");
        searchText.addKeyListener(this);
        Dimension textSize = searchText.getPreferredSize();

        list = inf.findByName("list");
        list.setListData(entries.toArray(new String[0]));
        list.addMouseListener((MouseClickListener) e -> {
            Object selected = list.getSelectedValue();
            if (selected == null)
                return;
            if (listener != null)
                listener.actionPerformed(new ActionEvent(list, 0, String.valueOf(selected)));
            dispose();
        });
        list.addMouseMotionListener(this);
        Dimension listSize = list.getPreferredSize();

        setContentPane(inf.getRoot());
        setLocationRelativeTo(frame);
        setUndecorated(true);
        setSize(listSize.width + 30, Math.min(150, listSize.height + (int) (textSize.height * 1.5f)));
        addWindowFocusListener(this);
    }

    public ScrollPopupMenu(Frame frame, List<String> entries, ActionListener listener) {
        this(null, frame, entries, listener);
    }

    public void show(Component owner, int x, int y) {
        if (!owner.isShowing())
            return;
        Point p = owner.getLocationOnScreen();
        setLocation((int) p.getX() + x, (int) p.getY() + y);
        setVisible(true);
        searchText.requestFocus();
        searchThread.start();
    }

    public void scrollTo(String selected) {
        int sIndex = entries.indexOf(selected);
        if (sIndex != -1) {
            // Move the scroll up to center the value in the list
            sIndex = Math.min(sIndex + 3, entries.size() - 1);
            list.setSelectedValue(entries.get(sIndex), true);
        }
        list.setSelectedValue(selected, false);
    }

    private List<String> search(String terms) {
        terms = terms.toLowerCase();
        if (terms.trim().isEmpty())
            return entries;

        List<String> ret = new ArrayList<>();
        for (String str : entries) {
            String match = str.toLowerCase();
            if (match.contains(terms))
                ret.add(str);
        }
        return ret;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        SwingUtilities.invokeLater(() -> {
            searchTerms = searchText.getText();
        });
    }

    private final Thread searchThread = new Thread(() -> {
        while (isVisible()) {
            if (searchTerms == null)
                continue;
            String terms = searchTerms;
            searchTerms = null;
            try {
                final List<String> results = search(terms);
                SwingUtilities.invokeLater(() -> list.setListData(results.toArray(new String[0])));
                Thread.sleep(250);
            } catch (Exception ignore) {
            }
        }
    }, "ScrollPopupMenu - SearchThread");

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        dispose();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int index = list.locationToIndex(e.getPoint());
        list.setSelectedIndex(index);
    }
}
