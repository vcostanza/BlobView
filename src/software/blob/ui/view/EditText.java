package software.blob.ui.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Editable text field with hint support
 */
public class EditText extends JTextField implements DocumentListener {

    private static final Color HINT_COLOR = new Color(96, 96, 96);

    public interface OnTextChangedListener {

        /**
         * Called when the contents of an EditText have been changed
         * @param et Input
         * @param text Text
         */
        void onTextChanged(EditText et, String text);
    }

    private String hint;
    private final ConcurrentLinkedQueue<OnTextChangedListener> textListeners = new ConcurrentLinkedQueue<>();

    public EditText() {
    }

    public EditText(AttributeSet attrs) {
        setName(attrs.getString("name", ""));
        setToolTipText(attrs.getString("tip", attrs.getString("toolTipText", null)));
        this.hint = attrs.getString("hint", null);
        getDocument().addDocumentListener(this);
    }

    @Override
    public void setText(String text) {
        if (text == null)
            text = "";
        super.setText(text);
    }

    public void setText(float value) {
        setText(!Float.isNaN(value) ? String.valueOf(value) : "");
    }

    public void setText(double value) {
        setText(!Double.isNaN(value) ? String.valueOf(value) : "");
    }

    public void setText(int value) {
        setText(String.valueOf(value));
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void addTextChangedListener(OnTextChangedListener l) {
        textListeners.add(l);
    }

    public void removeTextChangedListener(OnTextChangedListener l) {
        textListeners.remove(l);
    }

    private void onTextChanged() {
        String text = getText();
        for (OnTextChangedListener l : textListeners)
            l.onTextChanged(this, text);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onTextChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onTextChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onTextChanged();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (g instanceof Graphics2D && this.hint != null && !this.hint.isEmpty() && getDocument().getLength() <= 0) {
            Insets padding = getInsets();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(HINT_COLOR);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.drawString(this.hint, padding.left, getHeight() - padding.top * 1.5f);
        }
    }
}
