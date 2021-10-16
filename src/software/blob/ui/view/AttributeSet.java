package software.blob.ui.view;

import software.blob.ui.view.layout.LayoutParams;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import software.blob.ui.view.resource.ResourceMap;
import software.blob.ui.view.resource.StyleResource;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * View attributes inherited from XML layout
 */
public class AttributeSet extends HashMap<String, String> {

    public AttributeSet() {
    }

    public AttributeSet(Element el) {
        NamedNodeMap attrs = el.getAttributes();
        int attrCount = attrs.getLength();
        for (int j = 0; j < attrCount; j++) {
            Node n = attrs.item(j);
            String key = n.getNodeName();
            String value = n.getNodeValue();
            if (value.startsWith("@")) {
                Object ref = ResourceMap.getResource(value.substring(1));
                if (ref != null) {
                    if (ref instanceof String)
                        value = (String) ref;
                    else if (ref instanceof StyleResource) {
                        StyleResource style = (StyleResource) ref;
                        putAll(style.getResolvedAttributeSet());
                    }
                }
            }
            put(key, value);
        }
    }

    public AttributeSet(AttributeSet other) {
        super(other);
    }

    public AttributeSet(AttributeSet other, String... keysToCopy) {
        for (String k : keysToCopy)
            put(k, other.get(k));
    }

    @Override
    public String put(String key, String value) {
        if (value == null)
            return null;
        return super.put(key, value);
    }

    public void setString(String key, String value) {
        put(key, value);
    }

    public void setInteger(String key, int value) {
        put(key, String.valueOf(value));
    }

    public void setBoolean(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    public String getString(String key, String defVal) {
        return containsKey(key) ? get(key) : defVal;
    }

    public int getInteger(String key, int defVal) {
        return parseInt(get(key), defVal);
    }

    public float getFloat(String key, float defVal) {
        return parseFloat(get(key), defVal);
    }

    public boolean getBoolean(String key, boolean defVal) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value) : defVal;
    }

    public Color getColor(String key, Color defVal) {
        String value = get(key);

        // Hex color code
        try {
            return Color.decode(value);
        } catch (Exception e) {
            return defVal;
        }
    }

    public int getDimension(String key, int defVal) {
        String value = get(key);
        if (value == null)
            return defVal;

        // Constants
        if (value.equals("match_parent"))
            return LayoutParams.MATCH_PARENT;
        if (value.equals("wrap_content"))
            return LayoutParams.WRAP_CONTENT;

        // Raw pixel value
        int pxIdx = value.indexOf("px");
        if (pxIdx > -1)
            return parseInt(value.substring(0, pxIdx), defVal);

        return parseInt(value, defVal);
    }

    private static int parseInt(String value, int defVal) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignore) {}
        return defVal;
    }

    private static float parseFloat(String value, float defVal) {
        try {
            return Float.parseFloat(value);
        } catch (Exception ignore) {}
        return defVal;
    }
}
