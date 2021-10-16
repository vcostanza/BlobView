package software.blob.ui.view.dialog;

import software.blob.ui.listener.ComponentHiddenListener;
import software.blob.ui.listener.ComponentResizeListener;
import software.blob.ui.theme.DarkTheme;
import software.blob.ui.util.Log;
import software.blob.ui.view.SelectButton;
import software.blob.ui.view.View;
import software.blob.ui.view.layout.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog specifically tailored to {@link AbstractLayout}
 */
public class LayoutDialog extends JDialog implements ActionListener, KeyListener {

    // Arbitrary default dialog size
    protected static final int DEFAULT_WIDTH = 350, DEFAULT_HEIGHT = 250;

    protected final Window window;
    protected final LinearLayout contentPane, rootContainer, footer;
    protected final SelectButton okBtn;
    protected final SelectButton cancelBtn;

    protected Container view;
    protected boolean fitToView = true;
    protected boolean confirmOnEnter;
    protected Runnable onOk, onCancel;

    private boolean resizing = false;

    public LayoutDialog(Window window, boolean modal) {
        super(window, "", modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        this.window = window;

        InflatedLayout inf = LayoutInflater.inflate("layout_dialog");
        rootContainer = inf.findByName("rootContainer");
        footer = inf.findByName("footer");
        okBtn = inf.findByName("okBtn");
        cancelBtn = inf.findByName("cancelBtn");

        okBtn.addActionListener(this);
        cancelBtn.addActionListener(this);

        setBackground(DarkTheme.BACKGROUND);
        setContentPane(contentPane = inf.getRoot());
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public LayoutDialog(Window window) {
        this(window, true);
    }

    /**
     * Set the root view/layout of this dialog
     * @param c Root component
     * @return Dialog
     */
    public LayoutDialog setView(Container c) {
        // Clear existing listeners
        if (view != null) {
            setConfirmOnEnter(false);
            rootContainer.remove(view);
        }

        // Set view
        view = c;

        // Add the view to the root
        rootContainer.add(view);

        // Enter listeners
        setConfirmOnEnter(true);

        return this;
    }

    /**
     * Set whether this dialog's "OK" button should be pressed when the user hits the enter key
     * @param confirmOnEnter True to press "OK" on enter
     * @return Layout dialog
     */
    public LayoutDialog setConfirmOnEnter(boolean confirmOnEnter) {
        this.confirmOnEnter = confirmOnEnter;

        List<JTextField> textFields = findTextFields(this.rootContainer);
        for (JTextField txt : textFields) {
            if (confirmOnEnter)
                txt.addKeyListener(this);
            else
                txt.removeKeyListener(this);
        }

        return this;
    }

    /**
     * Set whether this dialog should resize itself depending on the view's size
     * Note: If the view is set to {@link LayoutParams#MATCH_PARENT} this flag will have no effect
     * @param fitToView True to fit to view
     * @return Layout dialog
     */
    public LayoutDialog setFitToView(boolean fitToView) {
        this.fitToView = fitToView;
        return this;
    }

    /**
     * Set callback to be invoked when cancel button is clicked
     * @param onCancel Cancel action
     * @return Layout dialog
     */
    public LayoutDialog setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    /**
     * Position and show the dialog on screen
     * @param onOk Callback when OK is pressed
     * @return Dialog
     */
    public LayoutDialog showDialog(Runnable onOk) {
        this.onOk = onOk;
        setLocationRelativeTo(window);
        SwingUtilities.invokeLater(() -> {
            view.addComponentListener((ComponentResizeListener) e -> updateSize());
            contentPane.addComponentListener((ComponentResizeListener) e -> updateSize());
            updateSize();
            setLocationRelativeTo(window);
        });
        setVisible(true);
        addComponentListener((ComponentHiddenListener) e -> {
            dismiss();
        });
        return this;
    }

    /**
     * Position and show the dialog on screen
     * @return Dialog
     */
    public LayoutDialog showDialog() {
        return showDialog(null);
    }

    /**
     * Show the dialog in an {@link SwingUtilities#invokeLater(Runnable)} call
     * Useful for situations where the rest of the call stack must execute
     * before showing a modal dialog
     * @return Dialog
     */
    public LayoutDialog showDialogLater() {
        SwingUtilities.invokeLater(this::showDialog);
        return this;
    }

    public void dismiss() {
        dispose();
    }

    protected void updateSize() {
        if (resizing || !fitToView)
            return;

        Insets windowInsets = getInsets();
        Insets padding = contentPane.getInsets();
        Dimension dSize = getSize();
        Dimension size = getLayoutSize(view);
        LayoutParams fParams = footer.getLayoutParams();

        //Log.d("View size = " + size + " | root container = " + rootContainer.getSize() + " | content = " + contentPane.getSize() + " | dialog = " + dSize);

        if (size.height == 0)
            size.height = dSize.height;
        size.width += padding.left + padding.right;
        size.height += footer.getHeight() + fParams.margins.top + fParams.margins.bottom
                + padding.top + padding.bottom + windowInsets.top;

        if (view instanceof View) {
            LayoutParams lp = ((View) view).getLayoutParams();
            boolean matchWidth = lp.width == LayoutParams.MATCH_PARENT || lp.width == 0 && lp.weight == 1;
            boolean matchHeight = lp.height == LayoutParams.MATCH_PARENT || lp.height == 0 && lp.weight == 1;
            if (matchWidth)
                size.width = dSize.width;
            if (matchHeight)
                size.height = dSize.height;
        }

        resizing = true;
        setSize(size);
        contentPane.revalidate();
        resizing = false;
    }

    protected void onOK() {
        if (this.onOk != null)
            this.onOk.run();
    }

    protected void onCancel() {
        if (onCancel != null)
            onCancel.run();
        dismiss();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == okBtn)
            onOK();
        else if (src == cancelBtn)
            onCancel();
    }

    private Dimension getLayoutSize(Container c) {
        if (c instanceof AbstractLayout) {
            AbstractLayout ll = (AbstractLayout) c;
            LayoutManager lm = ll.getLayout();
            if (lm instanceof LinearLayoutManager) {
                ll.invalidateLayout();
                return ((LinearLayoutManager) lm).getSubjectSize();
            }
        } else
            c.invalidate();
        return c.getPreferredSize();
    }

    @Override
    public void keyPressed(KeyEvent evt) {
        if (confirmOnEnter && evt.getKeyCode() == KeyEvent.VK_ENTER)
            onOK();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private static List<JTextField> findTextFields(Container parent) {
        List<JTextField> ret = new ArrayList<>();
        for (Component c : parent.getComponents()) {
            if (c instanceof JTextField)
                ret.add((JTextField) c);
            else if (c instanceof Container)
                ret.addAll(findTextFields((Container) c));
        }
        return ret;
    }
}
