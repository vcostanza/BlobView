package software.blob.ui.view.layout;

import software.blob.ui.view.AttributeSet;

import java.awt.*;

/**
 * Layout class which supports orientation and weights
 * Based on the Android class of the same name
 */
public class LinearLayout extends AbstractLayout {

    public static final int HORIZONTAL = 0, VERTICAL = 1;

    private int orientation;
    private float weightSum;

    public LinearLayout(LayoutParams lp, int orientation) {
        this.orientation = orientation;
        setLayoutParams(lp);
        setLayout(new LinearLayoutManager(this));
    }

    public LinearLayout(LayoutParams lp) {
        this(lp, HORIZONTAL);
    }

    public LinearLayout(int orientation) {
        this(new LayoutParams(), orientation);
    }

    public LinearLayout() {
        this(HORIZONTAL);
    }

    public LinearLayout(AttributeSet attrs) {
        super(attrs);
        setOrientation(attrs.getString("orientation", "horizontal").equals("vertical") ? VERTICAL : HORIZONTAL);
        setWeightSum(attrs.getFloat("weightSum", 1f));
        setLayout(new LinearLayoutManager(this));
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setWeightSum(float weightSum) {
        this.weightSum = weightSum;
    }

    public float getWeightSum() {
        return this.weightSum;
    }

    @Override
    public void removeAll() {
        LayoutManager lm = getLayout();
        if (lm instanceof LinearLayoutManager)
            ((LinearLayoutManager) lm).removeAll();
        super.removeAll();
    }
}
