package software.blob.ui.view.resource;

import org.w3c.dom.Attr;
import software.blob.ui.view.AttributeSet;

import java.util.Map;

/**
 * Contains multiple resource attributes
 */
public class StyleResource extends AttributeSet {

    // Style name
    private final String name;

    public StyleResource(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Set the parent resource to inherit properies from
     * @param parent Parent resource
     */
    public void setParent(StyleResource parent) {
        if (parent != null)
            putAll(parent);
    }

    /**
     * Convert this style resource to an attribute set with resolved resource values
     * @return Attribute set
     */
    public AttributeSet getResolvedAttributeSet() {
        AttributeSet attrs = new AttributeSet();
        for (Map.Entry<String, String> e : entrySet()) {
            String sKey = e.getKey();
            String sVal = e.getValue();
            if (sVal.startsWith("@")) {
                String sRef = ResourceMap.getStringResource(sVal.substring(1));
                if (sRef != null)
                    sVal = sRef;
            }
            attrs.put(sKey, sVal);
        }
        return attrs;
    }
}
