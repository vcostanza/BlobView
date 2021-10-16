package software.blob.ui.view.layout;

import software.blob.ui.view.AttributeSet;
import software.blob.ui.view.Gravity;

import java.awt.*;

public class LayoutParams {

    public static final int MATCH_PARENT = -2, WRAP_CONTENT = -1;

    public int width;
    public int height;
    public float weight;
    public Gravity gravity = new Gravity(Gravity.START | Gravity.TOP);
    public Insets margins = new Insets(0, 0, 0, 0);

    public LayoutParams(int width, int height, float weight) {
        this.width = width;
        this.height = height;
        this.weight = weight;
    }

    public LayoutParams(int width, int height) {
        this(width, height, 0);
    }

    public LayoutParams() {
        this(WRAP_CONTENT, WRAP_CONTENT);
    }

    public LayoutParams(AttributeSet attrs) {
        this.width = attrs.getDimension("width", WRAP_CONTENT);
        this.height = attrs.getDimension("height", WRAP_CONTENT);
        this.weight = attrs.getFloat("weight", 0);

        Gravity gravity = Gravity.fromString(attrs.getString("gravity", null));
        if (gravity != null)
            this.gravity = gravity;

        int m = attrs.getDimension("margins", 0);
        setMargins(m, m, m, m);

        int mLeft = attrs.getDimension("marginLeft", Integer.MAX_VALUE);
        if (mLeft != Integer.MAX_VALUE) this.margins.left = mLeft;

        int mRight = attrs.getDimension("marginRight", Integer.MAX_VALUE);
        if (mRight != Integer.MAX_VALUE) this.margins.right = mRight;

        int mTop = attrs.getDimension("marginTop", Integer.MAX_VALUE);
        if (mTop != Integer.MAX_VALUE) this.margins.top = mTop;

        int mBottom = attrs.getDimension("marginBottom", Integer.MAX_VALUE);
        if (mBottom != Integer.MAX_VALUE) this.margins.bottom = mBottom;
    }

    public LayoutParams setMargins(int top, int left, int bottom, int right) {
        this.margins.set(top, left, bottom, right);
        return this;
    }

    public LayoutParams setMargins(int m) {
        return setMargins(m, m, m, m);
    }

    public LayoutParams setGravity(int gravity) {
        this.gravity = new Gravity(gravity);
        return this;
    }
}
