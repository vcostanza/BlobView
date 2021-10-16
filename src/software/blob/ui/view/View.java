package software.blob.ui.view;

import software.blob.ui.util.Log;
import software.blob.ui.view.layout.InflatedLayout;
import software.blob.ui.view.layout.LayoutInflater;
import software.blob.ui.view.layout.LayoutParams;
import software.blob.ui.view.listener.ClickListener;
import software.blob.ui.view.listener.DoubleClickListener;
import software.blob.ui.view.listener.HoverListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Base view class that other view-specific components should extend off of
 */
public class View extends JComponent implements Scrollable, MouseListener {

    // Debug mode
    public static final boolean DEBUG_DRAW = false;
    private static final Stroke DEBUG_STROKE = new BasicStroke(2);

    // Visibility states
    public static final int VISIBLE = 1, INVISIBLE = 0, GONE = -1;

    // Double-click time threshold (ms)
    public static final long DBL_CLICK_INTERVAL = 300;

    protected AttributeSet attrs;
    protected Insets padding = new Insets(0, 0, 0, 0);
    protected LayoutParams layoutParams = new LayoutParams();
    protected Stroke borderStroke;
    protected Color borderColor;
    protected float borderWidth;
    protected Dimension preferredSize = new Dimension(0, 0);
    protected int visibility = VISIBLE;
    protected Object tag;

    // Listeners
    private ClickListener clickListener;
    private DoubleClickListener dblClickListener;
    private HoverListener hoverListener;
    private boolean mouseListenerOn;
    private MouseEvent hoverStart;
    protected final Map<Integer, Long> lastClickTimes = new HashMap<>();

    public View() {
    }

    public View(AttributeSet attrs) {
        this.attrs = attrs;

        setName(attrs.getString("name", ""));

        String viz = attrs.getString("visibility", attrs.getBoolean("visible", true) ? "visible" : "gone");
        setVisibility(viz.equals("visible") ? VISIBLE : (viz.equals("invisible") ? INVISIBLE : GONE));

        int p = attrs.getDimension("padding", 0);
        setPadding(p, p, p, p);

        int pLeft = attrs.getDimension("paddingLeft", Integer.MAX_VALUE);
        if (pLeft != Integer.MAX_VALUE) this.padding.left = pLeft;

        int pRight = attrs.getDimension("paddingRight", Integer.MAX_VALUE);
        if (pRight != Integer.MAX_VALUE) this.padding.right = pRight;

        int pTop = attrs.getDimension("paddingTop", Integer.MAX_VALUE);
        if (pTop != Integer.MAX_VALUE) this.padding.top = pTop;

        int pBottom = attrs.getDimension("paddingBottom", Integer.MAX_VALUE);
        if (pBottom != Integer.MAX_VALUE) this.padding.bottom = pBottom;

        Color bgColor = attrs.getColor("background", null);
        if (bgColor != null)
            setBackground(bgColor);

        Color fgColor = attrs.getColor("foreground", null);
        if (fgColor != null)
            setForeground(fgColor);

        Color borderColor = attrs.getColor("borderColor", null);
        if (borderColor != null)
            setBackgroundBorder(borderColor, attrs.getDimension("borderWeight", 2));

        String tooltip = attrs.getString("tip", attrs.getString("toolTipText", null));
        if (tooltip != null)
            setToolTipText(tooltip);
    }

    /**
     * This layout has finished being inflated by a {@link LayoutInflater}
     * It is now safe to process children
     * @param inf Inflated layout
     */
    public void onFinishInflate(InflatedLayout inf) {
    }

    /**
     * Set the layout parameters of this container
     * These dictate how the container is positioned within its parent
     * @param lp Layout parameters
     */
    public void setLayoutParams(LayoutParams lp) {
        this.layoutParams = lp;
    }

    public LayoutParams getLayoutParams() {
        return this.layoutParams;
    }

    /**
     * Set the padding (inset) in pixels
     * The padding is how many pixels to show between the layout's background and the children
     * @param top Top pixels
     * @param left Left pixels
     * @param bottom Bottom pixels
     * @param right Right pixels
     */
    public void setPadding(int top, int left, int bottom, int right) {
        this.padding.set(top, left, bottom, right);
    }

    public void setPadding(int p) {
        setPadding(p, p, p, p);
    }

    public Insets getPadding() {
        return this.padding;
    }

    @Override
    public Insets getInsets() {
        return this.padding;
    }

    /**
     * Set a border to be rendered over the background fill
     * @param color Color
     * @param width Width in device pixels (see Graphics2D)
     */
    public void setBackgroundBorder(Color color, float width) {
        this.borderColor = color;
        this.borderStroke = new BasicStroke(width);
        this.borderWidth = width;
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    @Override
    public void setPreferredSize(Dimension dim) {
        preferredSize.setSize(dim);
        super.setPreferredSize(dim);
    }

    /**
     * Set the visibility state of this view (VISIBLE, INVISIBLE, GONE)
     * VISIBLE: Show the view on screen at the current size
     * INVISIBLE: Hide the view and maintain the current size
     * GONE: Hide the view and ignore the size
     * @param vizState Visibility state
     */
    public void setVisibility(int vizState) {
        if (this.visibility != vizState) {
            this.visibility = vizState;

            // Check if the mouse hover state needs to be updated
            updateHover();

            // Invalidate views
            SwingUtilities.invokeLater(() -> {
                Container parent = getParent();
                if (parent != null) {
                    Rectangle r = getBounds();
                    parent.repaint(r.x, r.y, r.width, r.height);
                }
                revalidate();
            });
        }
    }

    public int getVisibility() {
        return this.visibility;
    }

    @Override
    public void setVisible(boolean visible) {
        setVisibility(visible ? VISIBLE : GONE);
    }

    @Override
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    /**
     * Set gravity flags
     * @param gravity Gravity flags
     */
    public void setGravity(int gravity) {
        layoutParams.setGravity(gravity);
    }

    public Gravity getGravity() {
        return layoutParams != null ? layoutParams.gravity : null;
    }

    /**
     * Set a generic tag associated with this view
     * @param tag Tag object
     */
    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    /**
     * Get the {@link AttributeSet} for this view
     * @return Attribute set or null if not used
     */
    public AttributeSet getAttributes() {
        return attrs;
    }

    /**
     * Get an attribute set on this view
     * @param attrName Attribute name
     * @param defaultValue Default value
     * @return Attribute value (in raw string form)
     */
    public String getAttribute(String attrName, String defaultValue) {
        return attrs != null ? attrs.getString(attrName, defaultValue) : defaultValue;
    }

    /**
     * Check if this view has a specific attribute set
     * @param attrName Attribute name
     * @return True if attribute exists
     */
    public boolean hasAttribute(String attrName) {
        return attrs != null && attrs.containsKey(attrName);
    }

    /**
     * Set an attribute on this view
     * Note: This will not affect any properties dependent on the attribute during inflation
     * @param attrName Attribute name
     * @param value Attribute value
     */
    public void setAttribute(String attrName, String value) {
        if (attrs == null)
            attrs = new AttributeSet();
        attrs.put(attrName, value);
    }

    /**
     * Set this view's click listener
     * @param clickListener Click listener
     */
    public void setOnClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
        updateMouseListener();
    }

    /**
     * Set this view's double-click listener
     * @param clickListener Click listener
     */
    public void setOnDoubleClickListener(DoubleClickListener clickListener) {
        this.dblClickListener = clickListener;
        updateMouseListener();
    }

    /**
     * Set this view's mouse hover listener
     * @param hoverListener Hover listener
     */
    public void setOnHoverListener(HoverListener hoverListener) {
        this.hoverListener = hoverListener;
        updateMouseListener();
        updateHover();
    }

    /**
     * Add/remove mouse listener depending on if we have dependency listeners set
     */
    protected void updateMouseListener() {
        // Add or remove mouse listener depending on if we need one or not
        boolean needListener = clickListener != null || dblClickListener != null || hoverListener != null;
        if (mouseListenerOn != needListener) {
            if (needListener)
                addMouseListener(this);
            else
                removeMouseListener(this);
            mouseListenerOn = needListener;
        }
    }

    /**
     * Check if an active hover event needs to be ended (i.e. view is hidden while hovering)
     */
    protected void updateHover() {
        Point mouse = getMousePosition();
        boolean hovering = hoverListener != null && isVisible() && mouse != null && super.contains(mouse);
        if (hovering == (hoverStart == null)) {
            int type = hovering ? MouseEvent.MOUSE_ENTERED : MouseEvent.MOUSE_EXITED;
            int x = 0, y = 0;
            if (mouse != null) {
                x = (int) mouse.getX();
                y = (int) mouse.getY();
            } else if (hoverStart != null) {
                x = hoverStart.getX();
                y = hoverStart.getY();
            }
            MouseEvent event = new MouseEvent(this, type, System.currentTimeMillis(), 0, x, y, 0, false);
            if (hovering)
                mouseEntered(event);
            else
                mouseExited(event);
        }
    }

    /**
     * Paint this component using the Graphics2D instance
     * It's recommended sub-classes extend this method rather than {@link #paint(Graphics)} directly
     * @param g 2D graphics
     */
    protected void paint(Graphics2D g) {
        Color bg = getBackground();
        g.setColor(bg);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void paint(Graphics g) {
        int vizState = getVisibility();
        if (vizState == View.GONE)
            return;

        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;

            // Draw container content
            if (vizState == View.VISIBLE) {
                paint(g2d);

                // Draw border on top
                if (borderStroke != null && borderColor != null) {
                    g2d.setColor(borderColor);
                    g2d.setStroke(borderStroke);
                    g2d.drawRect(0, 0, getWidth(), getHeight());
                }
            }

            // Draw debug outlines
            if (DEBUG_DRAW) {
                g2d.setColor(Color.RED);
                String name = getName();
                String text = getWidth() + ", " + getHeight();
                if (name != null && !name.isEmpty())
                    text = name + ", " + text;
                g2d.drawString(text, 5, 15);
                g2d.setStroke(DEBUG_STROKE);
                g2d.drawRect(0, 0, getWidth(), getHeight());
            }
        }

        super.paint(g);
    }

    // Click listener implementation

    @Override
    public boolean contains(int x, int y) {
        return isVisible() && isEnabled() && super.contains(x, y);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (isEnabled())
            super.processMouseEvent(e);
    }

    @Override
    public final void mouseClicked(MouseEvent e) {
        long time = System.currentTimeMillis();
        int button = e.getButton();
        Long lastTime = lastClickTimes.get(button);
        if (dblClickListener != null && lastTime != null && time - lastTime < DBL_CLICK_INTERVAL)
            dblClickListener.onDoubleClick(this, e);
        else if (this.clickListener != null)
            clickListener.onClick(this, e);
        lastClickTimes.put(button, time);
    }

    @Override
    public final void mousePressed(MouseEvent e) {
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
    }

    @Override
    public final void mouseEntered(MouseEvent e) {
        if (hoverListener != null) {
            hoverStart = e;
            hoverListener.onHoverStart(this, e);
        }
    }

    @Override
    public final void mouseExited(MouseEvent e) {
        if (hoverListener != null) {
            hoverStart = null;
            hoverListener.onHoverEnd(this, e);
        }
    }

    // Overrides to prevent JScrollPane from entering an infinite loop //

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    protected void validateTree() {
        if (getVisibility() != View.GONE)
            super.validateTree();
    }
}
