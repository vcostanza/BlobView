package software.blob.ui.view.layout;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inflated layout mapping
 */
@SuppressWarnings("unchecked")
public class InflatedLayout {

    private final String name;
    private final Map<String, Component> componentMap = new HashMap<>();

    private Component root;

    public InflatedLayout(String name) {
        this.name = name;
    }

    public void setRoot(Component root) {
        this.root = root;
    }


    public <T extends Component> T getRoot() {
        try {
            return (T) this.root;
        } catch (Exception ignored) {
            return null;
        }
    }

    public void addComponent(Component comp) {
        componentMap.put(comp.getName(), comp);
    }

    public <T extends Component> T findByName(String componentName) {
        try {
            return (T) componentMap.get(componentName);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Find and return all components that match or inherit the given class name
     * @param clazz Base class to check
     * @param <T> Base class type
     * @return List of components
     */
    public <T extends Component> List<T> findByClass(Class<T> clazz) {
        List<T> ret = new ArrayList<>();
        for (Component c : componentMap.values()) {
            if (clazz.isInstance(c))
                ret.add((T) c);
        }
        return ret;
    }
}
