package software.blob.ui.view.layout.table;

import software.blob.ui.view.AttributeSet;
import software.blob.ui.view.layout.LinearLayout;

/**
 * Vertical layout that keeps children widths within each {@link TableRow} consistent
 */
public class TableLayout extends LinearLayout {

    public TableLayout() {
        super();
        init();
    }

    public TableLayout(AttributeSet attrs) {
        super(attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL); // Table layouts must be vertical
        setLayout(new TableLayoutManager(this));
    }
}
