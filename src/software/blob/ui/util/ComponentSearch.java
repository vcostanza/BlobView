package software.blob.ui.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Deep component searcher
 */
public class ComponentSearch {

    public Point point;
    public ComponentFilter filter;
    public int limit;

    /**
     * Find all components in a hierarchy that match the set parameters
     * @param c Component to begin search from
     * @return List of found components
     */
    public List<Component> findComponents(Component c) {
        ArrayList<Component> ret = new ArrayList<>();
        if (point == null)
            return ret;
        searchImpl(c, point.x, point.y, ret);
        return ret;
    }

    /**
     * Find a component in a hierarchy that matches the set parameters
     * @param c Component to begin search from
     * @return Component or null if none found
     */
    public Component findComponent(Component c) {
        limit = 1;
        List<Component> ret = findComponents(c);
        return !ret.isEmpty() ? ret.get(0) : null;
    }

    private void searchImpl(Component c, int x, int y, Collection<Component> ret) {
        if (hitLimit(ret))
            return;

        if (!c.isVisible() || !c.contains(x, y))
            return;

        if (filter == null || filter.acceptComponent(c))
            ret.add(c);

        if (c instanceof Container) {
            Container con = (Container) c;
            for (final Component child : con.getComponents()) {
                final int x1 = x - child.getX();
                final int y1 = y - child.getY();
                searchImpl(child, x1, y1, ret);
                if (hitLimit(ret))
                    return;
            }
        }
    }

    private boolean hitLimit(Collection<Component> list) {
        return limit > 0 && list.size() >= limit;
    }
}
