package software.blob.ui.view;

import software.blob.ui.theme.DarkTheme;

import javax.swing.*;
import java.awt.*;

import static javax.swing.SwingConstants.*;

/**
 * View implementation similar to JLabel, but with more options and nicer rendering
 */
public class TextView extends View {

    // Attributes
    private String text = "";
    private String[] textLines = new String[0];
    private Font font;
    private int rotation;
    private Gravity textGravity = new Gravity(Gravity.START);

    // Calculated
    private final Dimension textSize = new Dimension();
    private Dimension[] lineSizes;
    private double rotCos = 1, rotSin = 0;

    public TextView(AttributeSet attrs) {
        super(attrs);

        String fontName = attrs.getString("textFont", DarkTheme.FONT_DEFAULT_NAME);
        int fontSize = attrs.getDimension("textSize", DarkTheme.FONT_DEFAULT_SIZE);
        int fontStyle = getFontStyle(attrs.getString("textStyle", "bold"));

        Font font;
        if (fontSize == DarkTheme.FONT_DEFAULT_SIZE && fontStyle == Font.PLAIN
                && fontName.equalsIgnoreCase(DarkTheme.FONT_DEFAULT_NAME))
            font = DarkTheme.FONT_DEFAULT;
        else
            font = new Font(fontName, fontStyle, fontSize);
        this.font = font;

        Color textColor = attrs.getColor("textColor", DarkTheme.GRAY_224);
        if (textColor != null)
            setForeground(textColor);

        this.rotation = attrs.getInteger("rotation", 0);
        Gravity textGravity = Gravity.fromString(attrs.getString("gravity", null));
        if (textGravity != null)
            this.textGravity = textGravity;

        setText(attrs.getString("text", ""));
    }

    /**
     * Set the text displayed in this view
     * @param text Text to display
     */
    public void setText(String text) {
        if (text == null)
            text = "";

        if (!text.equals(this.text)) {
            this.text = text;
            this.textLines = text.split("\\\\n");
            recalculateSize();
        }
    }

    public String getText() {
        return this.text;
    }

    @Override
    public void setFont(Font font) {
        if (this.font != font) {
            this.font = font;
            recalculateSize();
        }
    }

    @Override
    public Font getFont() {
        return this.font;
    }

    /**
     * Set the text rotation
     * @param rotation Rotation in degrees
     */
    public void setRotation(int rotation) {
        if (this.rotation != rotation) {
            this.rotation = rotation;
            recalculateSize();
        }
    }

    /**
     * Set the gravity of the text within its box
     * @param gravity Gravity enum
     */
    public void setTextGravity(Gravity gravity) {
        if (this.textGravity != gravity) {
            this.textGravity = gravity;
            recalculateSize();
        }
    }

    @Override
    public void setPadding(int top, int left, int bottom, int right) {
        super.setPadding(top, left, bottom, right);
        recalculateSize();
    }

    @Override
    public void paint(Graphics2D g) {
        super.paint(g);

        Insets padding = getPadding();

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(font);
        g.setColor(getForeground());

        float ascent = g.getFontMetrics().getAscent();
        g.translate(padding.left, padding.top);

        if (this.rotation != 0) {
            double theta = Math.toRadians(this.rotation);
            g.rotate(theta);
            switch (rotation) {
                case 90:
                    g.translate(0, -textSize.height);
                    break;
                case 270: // XXX - Kerning changes when rotation is >180, for some reason
                    g.translate(-textSize.width, 0);
                    break;
            }
        }

        int y = 0;
        for (int i = 0; i < textLines.length; i++) {
            String line = textLines[i];
            Dimension lineSize = lineSizes[i];
            if (lineSize == null)
                continue;
            int x = 0;
            if (textGravity.check(Gravity.CENTER_HORIZONTAL))
                x = (textSize.width - lineSize.width) / 2;
            else if (textGravity.check(Gravity.END))
                x = textSize.width - lineSize.width;
            g.drawString(line, x, y + ascent);
            y += lineSize.height;
        }
    }

    /**
     * Recalculate the preferred size of this text view
     */
    protected void recalculateSize() {
        if (font == null || textLines == null)
            return;

        // Measure the text
        FontMetrics fm = getFontMetrics(font);
        Rectangle viewR = new Rectangle();
        Rectangle textR = new Rectangle();
        int tWidth = 0, tHeight = 0;
        lineSizes = new Dimension[textLines.length];
        for (int i = 0; i < textLines.length; i++) {
            String line = textLines[i];
            viewR.setBounds(padding.left, padding.top, Short.MAX_VALUE, Short.MAX_VALUE);
            SwingUtilities.layoutCompoundLabel(this, fm, line, null, CENTER, LEADING, CENTER, TRAILING,
                    viewR, new Rectangle(), textR, 0);
            lineSizes[i] = new Dimension(textR.width, textR.height);
            tWidth = Math.max(tWidth, textR.width);
            tHeight += textR.height;
        }

        textSize.setSize(tWidth, tHeight);

        // Rotate dimensions
        if (rotation != 0) {
            double a = Math.toRadians(rotation);
            rotSin = Math.sin(a);
            rotCos = Math.cos(a);
            int rWidth = (int) Math.round(tWidth * rotCos + tHeight * rotSin);
            int rHeight = (int) Math.round(tHeight * rotCos + tWidth * rotSin);
            tWidth = Math.abs(rWidth);
            tHeight = Math.abs(rHeight);
        } else {
            rotCos = 1;
            rotSin = 0;
        }

        // Update size
        tWidth += padding.left + padding.right;
        tHeight += padding.top + padding.bottom;
        setPreferredSize(new Dimension(tWidth, tHeight));
        revalidate();
    }

    public static int getFontStyle(String style) {
        String[] styles = style.split("\\|");
        int ret = Font.PLAIN;
        for (String s : styles) {
            if (s.equals("bold"))
                ret |= Font.BOLD;
            else if (s.equals("italic"))
                ret |= Font.ITALIC;
        }
        return ret;
    }
}
