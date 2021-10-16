package software.blob.ui.view.layout.table;

import software.blob.ui.view.View;
import software.blob.ui.view.layout.LinearLayout;
import software.blob.ui.view.layout.LinearLayoutManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Table layout manager that measures table row column widths
 */
public class TableLayoutManager extends LinearLayoutManager {

    private final List<Integer> columnWidths = new ArrayList<>();

    public TableLayoutManager(LinearLayout subject) {
        super(subject);
    }

    public int getColumnWidth(int index) {
        return index < columnWidths.size() ? columnWidths.get(index) : -1;
    }

    public boolean onRowInvalidate(TableRowManager current) {
        // Find max column widths for each row
        List<Integer> columnWidths = new ArrayList<>();
        Component[] children = this.subject.getComponents();
        for (Component c : children) {
            if (!(c instanceof TableRow))
                continue;

            TableRow row = (TableRow) c;
            if (row.getVisibility() == View.GONE)
                continue;

            LayoutManager lm = row.getLayout();
            if (!(lm instanceof TableRowManager))
                continue;

            TableRowManager mgr = (TableRowManager) lm;
            List<Integer> widths = mgr.getColumnWidths();

            for (int i = 0; i < widths.size(); i++) {
                int colWidth;
                int cWidth = widths.get(i);
                if (i >= columnWidths.size())
                    columnWidths.add(colWidth = 0);
                else
                    colWidth = columnWidths.get(i);
                columnWidths.set(i, Math.max(cWidth, colWidth));
            }
        }

        // Check if any of the max column widths have changed
        boolean changed = true;
        if (this.columnWidths.size() == columnWidths.size()) {
            changed = false;
            for (int i = 0; i < columnWidths.size(); i++) {
                if (!this.columnWidths.get(i).equals(columnWidths.get(i))) {
                    changed = true;
                    break;
                }
            }
        }

        // Update widths
        if (changed) {
            this.columnWidths.clear();
            this.columnWidths.addAll(columnWidths);
            this.subject.invalidate();
        }

        return changed;
    }
}
