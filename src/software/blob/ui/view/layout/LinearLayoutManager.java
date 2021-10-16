package software.blob.ui.view.layout;

import software.blob.ui.view.Gravity;
import software.blob.ui.view.View;
import software.blob.ui.view.layout.table.TableRow;
import software.blob.ui.view.layout.table.TableRowManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Layout manager meant to be used with {@link LinearLayout} exclusively
 */
public class LinearLayoutManager implements LayoutManager2 {

    protected final LinearLayout subject;
    protected final List<LayoutParams> childLayouts = new ArrayList<>();
    protected Dimension wrapDim = new Dimension(0, 0);
    protected float weightSum;

    public LinearLayoutManager(LinearLayout subject) {
        this.subject = subject;
    }

    /**
     * Get the size of the subject container
     * @return Subject size
     */
    public Dimension getSubjectSize() {
        LayoutParams rootLP = this.subject.getLayoutParams();
        Dimension pSize = getParentSize();
        Dimension size = new Dimension();

        if (rootLP.width == LayoutParams.WRAP_CONTENT)
            size.width = this.wrapDim.width;
        else if (pSize != null && rootLP.width == LayoutParams.MATCH_PARENT)
            size.width = pSize.width;
        else
            size.width = this.subject.getWidth();

        if (rootLP.height == LayoutParams.WRAP_CONTENT)
            size.height = this.wrapDim.height;
        else if (pSize != null && rootLP.height == LayoutParams.MATCH_PARENT)
            size.height = pSize.height;
        else
            size.height = this.subject.getHeight();

        return size;
    }

    @Override
    public void invalidateLayout(Container target) {
        if (this.subject.inBulkOperation())
            return;

        Dimension bla = getSubjectSize();
        String name = this.subject.getName();
        //System.out.println("invalidateLayout " + this.subject.getClass().getSimpleName() + "[" + name + "] -> " + bla);

        LayoutParams rootLP = this.subject.getLayoutParams();
        Insets padding = this.subject.getInsets();
        int orientation = this.subject.getOrientation();
        Component[] children = this.subject.getComponents();

        if (children.length != this.childLayouts.size())
            return;

        float weightSum = 0;
        int wrapWidth = 0, wrapHeight = 0;
        for (int i = 0; i < children.length; i++) {
            Component c = children[i];
            if (getVisibility(c) == View.GONE)
                continue;

            if (c instanceof AbstractLayout)
                ((AbstractLayout) c).invalidateLayout();
            else if (c instanceof ScrollLayout) {
                ScrollLayout sl = (ScrollLayout) c;
                JViewport viewport = sl.getViewport();
                if (viewport != null) {
                    Component vv = viewport.getView();
                    if (vv instanceof AbstractLayout)
                        ((AbstractLayout) vv).invalidateLayout();
                }
            }

            LayoutParams lp = this.childLayouts.get(i);
            Dimension d = getChildSize(c, i);

            d.width += lp.margins.left + lp.margins.right;
            d.height += lp.margins.top + lp.margins.bottom;
            if (orientation == LinearLayout.HORIZONTAL) {
                wrapWidth += d.width;
                wrapHeight = Math.max(wrapHeight, d.height);
            } else {
                wrapWidth = Math.max(wrapWidth, d.width);
                wrapHeight += d.height;
            }
            weightSum += lp.weight;

            // XXX - Hack to get mouse listener to fucking work
            /*if (c instanceof View)
                ((View) c).updateMouseListener();*/
        }
        wrapWidth += padding.left + padding.right;
        wrapHeight += padding.top + padding.bottom;
        weightSum = Math.max(weightSum, this.subject.getWeightSum());
        this.wrapDim.setSize(wrapWidth, wrapHeight);
        this.weightSum = weightSum;

        Dimension size = getSubjectSize();
        this.subject.setPreferredSize(size);

        // XXX - Yuck - Too exhausted to encapsulate this table logic properly
        Component parent = this.subject.getParent();
        if (parent instanceof TableRow) {
            TableRow row = (TableRow) parent;
            TableRowManager mgr = (TableRowManager) row.getLayout();
            Dimension override = mgr.getChildSize(this.subject);
            size.width = override.width;
        }

        this.subject.setSize(size);

        SwingUtilities.invokeLater(this.subject::validate);
        //System.out.println(this.subject.getName() + " size = [" + size.width + ", " + size.height + "] wrap = [" + wrapWidth + ", " + wrapHeight + "]");
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        int index = this.subject.getComponentZOrder(comp);
        if (index == -1)
            return;

        LayoutParams lp = constraints instanceof LayoutParams ? (LayoutParams) constraints : new LayoutParams();
        this.childLayouts.add(index, lp);
        //System.err.println("Modifying childLayouts on Thread = " + Thread.currentThread().getName());
        invalidateLayout(this.subject);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        int index = this.subject.getComponentZOrder(comp);
        if (index == -1)
            return;

        this.childLayouts.remove(index);
        //System.err.println("Modifying childLayouts on Thread = " + Thread.currentThread().getName());
        invalidateLayout(this.subject);
    }

    public void removeAll() {
        this.childLayouts.clear();
        //System.err.println("Modifying childLayouts on Thread = " + Thread.currentThread().getName());
        invalidateLayout(this.subject);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return subject.getPreferredSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return subject.getSize();
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return subject.getSize();
    }

    @Override
    public void layoutContainer(Container target) {
        if (this.subject.inBulkOperation())
            return;

        //System.out.println("layoutContainer " + this.subject.getClass().getSimpleName() + "[" + subject.getName() + "] -> " + getSubjectSize());

        Insets padding = this.subject.getInsets();
        int orientation = this.subject.getOrientation();
        Dimension rootDim = this.subject.getSize();
        Dimension wrapDim = new Dimension(0, 0);
        Dimension weightDim = new Dimension(rootDim.width, rootDim.height);
        if (orientation == LinearLayout.HORIZONTAL)
            weightDim.width -= this.wrapDim.width;
        else
            weightDim.height -= this.wrapDim.height;

        int left = padding.left, top = padding.top;
        int x = left, y = top;
        Component[] children = target.getComponents();
        for (int i = 0; i < children.length; i++) {
            Component c = children[i];
            if (getVisibility(c) == View.GONE)
                continue;

            LayoutParams lp = this.childLayouts.get(i);

            Dimension d = getChildSize(c, i);
            if (lp.weight > 0 && this.weightSum > 0) {
                float weightFactor = lp.weight / this.weightSum;
                if (d.width == 0)
                    d.width = (int) (weightDim.width * weightFactor);
                if (d.height == 0)
                    d.height = (int) (weightDim.height * weightFactor);
            }

            int fullWidth = d.width + lp.margins.left + lp.margins.right;
            int fullHeight = d.height + lp.margins.top + lp.margins.bottom;
            if (orientation == LinearLayout.HORIZONTAL) {
                x += lp.margins.left;
                y = top + lp.margins.top;
                wrapDim.width += fullWidth;
                wrapDim.height = Math.max(wrapDim.height, fullHeight);
            } else {
                y += lp.margins.top;
                x = left + lp.margins.left;
                wrapDim.width = Math.max(wrapDim.width, fullWidth);
                wrapDim.height += fullHeight;
            }

            c.setBounds(x, y, d.width, d.height);

            if (orientation == LinearLayout.HORIZONTAL)
                x += d.width + lp.margins.right;
            else
                y += d.height + lp.margins.bottom;
        }

        rootDim.width -= padding.left + padding.right;
        rootDim.height -= padding.top + padding.bottom;
        Gravity gravity = this.subject.getLayoutParams().gravity;
        for (Component c : children) {
            if (getVisibility(c) == View.GONE)
                continue;

            x = c.getX();
            y = c.getY();
            int width = c.getWidth();
            int height = c.getHeight();

            int gWidth = orientation == LinearLayout.HORIZONTAL ? wrapDim.width : width;
            int gHeight = orientation == LinearLayout.VERTICAL ? wrapDim.height : height;
            if (gravity.check(Gravity.END))
                x += rootDim.width - gWidth;
            else if (gravity.check(Gravity.CENTER_HORIZONTAL))
                x += (rootDim.width - gWidth) / 2;

            if (gravity.check(Gravity.BOTTOM))
                y += rootDim.height - gHeight;
            else if (gravity.check(Gravity.CENTER_VERTICAL))
                y += (rootDim.height - gHeight) / 2;

            c.setBounds(x, y, width, height);
        }
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0;
    }

    /**
     * Get the size of the parent container
     * @return Size dimension
     */
    protected Dimension getParentSize() {
        Container parent = this.subject.getParent();
        if (parent == null)
            return null;
        int w = parent.getWidth();
        int h = parent.getHeight();
        int orientation = LinearLayout.VERTICAL;
        if (parent instanceof LinearLayout)
            orientation = ((LinearLayout) parent).getOrientation();
        else {
            LayoutManager lm = parent.getLayout();
            if (lm instanceof FlowLayout)
                orientation = LinearLayout.HORIZONTAL;
        }
        Insets padding = parent.getInsets();
        w -= padding.left + padding.right;
        h -= padding.top + padding.bottom;
        for (Component c : parent.getComponents()) {
            if (c != this.subject && getVisibility(c) != View.GONE) {
                if (orientation == LinearLayout.HORIZONTAL)
                    w -= c.getWidth();
                else
                    h -= c.getHeight();
            }
        }
        return new Dimension(w, h);
    }

    /**
     * Get the preferred size + padding of a child component
     * @param c Child component
     * @param childIndex Child index (for layout lookup)
     * @return Size dimension
     */
    protected Dimension getChildSize(Component c, int childIndex) {
        LayoutParams lp = this.childLayouts.get(childIndex);
        Dimension d = c.getPreferredSize();
        Insets padding = this.subject.getInsets();

        Dimension size = new Dimension(lp.width, lp.height);
        if (size.width == LayoutParams.WRAP_CONTENT)
            size.width = d.width;
        else if (size.width == LayoutParams.MATCH_PARENT)
            size.width = this.subject.getWidth() - padding.left - padding.right;

        if (size.height == LayoutParams.WRAP_CONTENT)
            size.height = d.height;
        else if (size.height == LayoutParams.MATCH_PARENT)
            size.height = this.subject.getHeight() - padding.top - padding.bottom;

        return size;
    }

    protected Dimension getChildSize(Component c) {
        Component[] children = this.subject.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] == c)
                return getChildSize(c, i);
        }
        return new Dimension(0, 0);
    }

    /**
     * Get the visibility state of a component
     * @param c Component
     * @return Visibility state
     */
    protected static int getVisibility(Component c) {
        if (c instanceof View)
            return ((View) c).getVisibility();
        return c.isVisible() ? View.VISIBLE : View.GONE;
    }
}
