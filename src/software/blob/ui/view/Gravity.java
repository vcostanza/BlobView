package software.blob.ui.view;

import java.util.HashMap;
import java.util.Map;

public class Gravity {

    public static final int START = 0, CENTER_HORIZONTAL = 1, END = 2;
    public static final int TOP = 4, CENTER_VERTICAL = 8, BOTTOM = 16;
    public static final int CENTER = CENTER_HORIZONTAL | CENTER_VERTICAL;

    private static final Map<String, Integer> STRING_MAP = new HashMap<>();
    static {
        STRING_MAP.put("start", START);
        STRING_MAP.put("center_horizontal", CENTER_HORIZONTAL);
        STRING_MAP.put("end", END);
        STRING_MAP.put("top", TOP);
        STRING_MAP.put("center_vertical", CENTER_VERTICAL);
        STRING_MAP.put("bottom", BOTTOM);
        STRING_MAP.put("center", CENTER);
    }

    private int value;

    public Gravity(int value) {
        this.value = value;
    }

    public boolean check(int gravity) {
        return (this.value & gravity) == gravity;
    }

    public static Gravity fromString(String gravity) {
        if (gravity == null)
            return null;

        int ret = 0;
        String[] parts = gravity.split("\\|");
        for (String p : parts) {
            Integer v = STRING_MAP.get(p);
            if (v != null)
                ret |= v;
        }
        return new Gravity(ret);
    }
}
