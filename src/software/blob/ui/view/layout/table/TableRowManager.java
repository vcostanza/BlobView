package software.blob.ui.view.layout.table;

import software.blob.ui.view.View;
import software.blob.ui.view.layout.LinearLayoutManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Table row layout manager that maintains a consistent column width across sibling table rows
 */
public class TableRowManager extends LinearLayoutManager {

    private final TableRow subject;
    private final List<Integer> columnWidths = new ArrayList<>();

    public TableRowManager(TableRow subject) {
        super(subject);
        this.subject = subject;
    }

    private TableLayoutManager getTableLayout() {
        Container parent = subject.getParent();
        if (parent != null) {
            LayoutManager lm = parent.getLayout();
            if (lm instanceof TableLayoutManager)
                return (TableLayoutManager) lm;
        }
        return null;
    }

    public int getWidth(Component c, int childIndex) {
        return super.getChildSize(c, childIndex).width;
    }

    @Override
    public Dimension getChildSize(Component c, int childIndex) {
        Dimension dim = super.getChildSize(c, childIndex);

        // Apply column widths
        TableLayoutManager tlm = getTableLayout();
        if (tlm != null) {
            int width = tlm.getColumnWidth(childIndex);
            if (width != -1)
                dim.width = width;
        }

        return dim;
    }

    @Override
    public void invalidateLayout(Container target) {
        if (this.subject.inBulkOperation())
            return;

        Component[] children = this.subject.getComponents();
        if (children.length != this.childLayouts.size())
            return;

        columnWidths.clear();
        for (int i = 0; i < children.length; i++) {
            Component c = children[i];

            // Item is invisible - skip it
            if (getVisibility(c) == View.GONE)
                continue;

            Dimension dim = super.getChildSize(c, i);
            columnWidths.add(dim.width);
        }

        TableLayoutManager tlm = getTableLayout();
        if (tlm != null && tlm.onRowInvalidate(this))
            return;

        super.invalidateLayout(target);
    }

    public List<Integer> getColumnWidths() {
        return columnWidths;
    }
}
