package software.blob.ui.view.layout.list;

import software.blob.ui.util.Log;
import software.blob.ui.view.AttributeSet;
import software.blob.ui.view.View;
import software.blob.ui.view.layout.LayoutParams;
import software.blob.ui.view.layout.LinearLayout;
import software.blob.ui.view.layout.ScrollLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Arrays;

/**
 * Performance-friendly list layout
 * TODO: Horizontal scrolling support (not needed at the moment)
 */
public class ListView extends ScrollLayout implements AdjustmentListener {

    private final ListContainer list;
    private final JScrollBar scrollbar;

    private ListAdapter adapter;
    private int width, height;
    private int scrollValue;
    private int topPos, bottomPos;
    private int rowHeight;

    public ListView(AttributeSet attrs) {
        super(attrs);
        this.list = new ListContainer();
        this.scrollbar = getVerticalScrollBar();
        this.scrollbar.addAdjustmentListener(this);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setViewportView(this.list);
    }

    /**
     * Set the view adapter for this list
     * This is used to track and generate views for each entry in the list
     * @param adapter List adapter
     */
    public void setAdapter(ListAdapter adapter) {
        this.adapter = adapter;
        if (adapter != null)
            adapter.setListView(this);
        refresh();
    }

    /**
     * Scroll to a specific position in the list
     *
     * Conditions:
     * - Row height must be greater than zero
     * - Position must be valid
     * - Scroll is only performed if the row isn't completely on screen
     *
     * @param pos Position to scroll to
     */
    public void scrollToPosition(int pos) {
        int count = adapter != null ? adapter.getCount() : 0;
        if (rowHeight <= 0 || pos < 0 || pos >= count || pos > topPos && pos < bottomPos)
            return;
        if (pos <= topPos)
            scrollbar.setValue(pos * rowHeight);
        else
            scrollbar.setValue((pos + 1) * rowHeight - getHeight());
    }

    /**
     * Reset the scroll to its default value
     */
    public void resetScroll() {
        scrollbar.setValue(0);
    }

    /**
     * Refresh the entries in the list view
     *
     * Much like the ListView/RecyclerView in Android, this will create or update
     * rows depending on the visible portion of the list using a set {@link ListAdapter}.
     *
     * For implementations of {@link ListAdapter#getView(int, View, ListView)}
     * it's recommended to keep the rows the same height, otherwise you will run
     * into some graphical quirks when scrolling.
     */
    public void refresh() {
        if (isVisible())
            list.refresh();
    }

    @Override
    public void setVisible(boolean visible) {
        boolean wasVisible = isVisible();
        super.setVisible(visible);
        if (visible != wasVisible)
            refresh();
    }

    @Override
    public void setViewportView(Component view) {
        if (!(view instanceof ListContainer)) {
            Log.w("Attempting to override list view container with " + view);
            return;
        }
        super.setViewportView(view);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        // Check if the bounds were updated and invoke refresh
        if (width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        int scroll = e.getValue();
        if (scroll != scrollValue) {
            scrollValue = scroll;
            refresh();
        }
    }

    private class ListContainer extends LinearLayout {

        private final View topPad, bottomPad;

        ListContainer() {
            super(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT), VERTICAL);
            this.topPad = new View();
            this.bottomPad = new View();
        }

        /**
         * Get all components excluding the top and bottom padding
         * @return Array of row components
         */
        private Component[] getRowComponents() {
            return Arrays.copyOfRange(getComponents(), 1, getComponentCount() - 1);
        }

        private void refresh() {
            setBulkOperation(true);

            Rectangle vp = getVisibleRect();
            Container parent = getParent();
            int scrollHeight = parent != null ? parent.getHeight() : 0;

            // Add the top/bottom padding if needed
            int topPadding = 0;
            int bottomPadding = 0;
            if (topPad.getParent() != this)
                add(topPad, 0);
            if (bottomPad.getParent() != this)
                add(bottomPad, getComponentCount());

            // Get all row components (excludes top and bottom padding)
            Component[] components = getRowComponents();

            int cIdx = 1; // Component index
            int itemCount = adapter != null ? adapter.getCount() : 0;
            topPos = -1;
            bottomPos = 0;
            rowHeight = 0;
            if (itemCount > 0 && scrollHeight > 0) {
                int visibleHeight = 0;
                int eIdx = 0; // Existing component index
                for (int pos = 0; pos < itemCount; pos++) {

                    // Get existing component using existing index
                    Component c = eIdx < components.length ? components[eIdx] : null;

                    // Create or update the existing view
                    View existing = c instanceof View ? (View) c : null;
                    View v = adapter.getView(pos, existing, ListView.this);

                    // Skip row if the view is invalid or has no height
                    int height = v != null ? v.getHeight() : 0;
                    if (height <= 0) {
                        itemCount--;
                        continue;
                    }

                    // Check if we need to remove or skip rows
                    if (pos == 0) {

                        // Use the height of the first row for upcoming calculations
                        rowHeight = height;

                        // Check if the first row is completely outside the viewport
                        if (vp.y >= height) {

                            // Calculate how many rows we need to skip ahead
                            int skip = vp.y / height;
                            pos += Math.max(0, skip - 1);

                            // Calculate new top padding and continue
                            topPadding = skip * rowHeight;
                            continue;
                        }
                    }

                    // Remember the first visible row
                    if (topPos == -1)
                        topPos = pos;

                    // Add new view if needed (and remove incompatible row first)
                    if (existing == null) {
                        if (c != null)
                            remove(cIdx);
                        add(v, cIdx);
                    }

                    // Track visible height so we know when to stop
                    if (vp.y > topPadding && eIdx == 0)
                        height -= vp.y - topPadding;
                    visibleHeight += height;

                    // Position tracking
                    bottomPos = pos;
                    cIdx++;
                    eIdx++;

                    // Stop loop if the visible portion of the list exceeds the available parent height
                    if (visibleHeight > scrollHeight)
                        break;
                }

                // Calculate bottom padding
                bottomPadding = Math.max(0, (itemCount - (bottomPos + 1)) * rowHeight);
            }

            if (topPos == -1)
                topPos = 0;

            // Update padding placeholders
            topPad.setPreferredSize(new Dimension(0, topPadding));
            bottomPad.setPreferredSize(new Dimension(0, bottomPadding));

            // Remove extra views
            //Log.d("Component count = " + getComponentCount());
            for (int i = getComponentCount() - 2; i >= cIdx; i--)
                remove(i);

            // Update layout
            setBulkOperation(false);
            revalidate();
        }
    }
}
