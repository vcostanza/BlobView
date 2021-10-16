package software.blob.ui.view.layout.table;

import software.blob.ui.view.AttributeSet;
import software.blob.ui.view.layout.LinearLayout;

/**
 * A row in a {@link TableLayout}
 */
public class TableRow extends LinearLayout {

    public TableRow() {
        super();
        init();
    }

    public TableRow(AttributeSet attrs) {
        super(attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL); // Table rows must be horizontal
        setLayout(new TableRowManager(this));
    }
}
