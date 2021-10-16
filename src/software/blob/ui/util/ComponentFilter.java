package software.blob.ui.util;

import java.awt.*;

/**
 * Component filtering interface
 */
public interface ComponentFilter {

    /**
     * Check whether a component is accepted by this filter
     * @param c Component
     * @return True if accepted
     */
    boolean acceptComponent(Component c);
}
