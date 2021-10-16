package software.blob.ui.view.layout;

import software.blob.ui.res.Resources;
import software.blob.ui.util.Log;
import software.blob.ui.view.AttributeSet;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Convert a layout XML file to GUI code
 */
public class LayoutInflater {

    private static final File LAYOUT_DIR = new File("./res/layout");
    private static final Set<String> PACKAGE_ROOTS = new HashSet<>(Arrays.asList(
            // Default package roots
            "software.blob.ui.view",
            "software.blob.ui.view.layout",
            "software.blob.ui.view.layout.table",
            "software.blob.ui.view.layout.list",
            "software.blob.ui.view.menu",
            "javax.swing"
    ));

    // Cache layout XML, so we don't have to deserialize over and over
    private static final Map<String, Document> xmlCache = new HashMap<>();

    // Cached base name -> class
    private static final Map<String, Class<?>> baseNameCache = new HashMap<>();

    /**
     * Register a package root for finding layout classes from their base name
     * @param packageRoot Package root
     */
    public static void addPackageRoot(String packageRoot) {
        PACKAGE_ROOTS.add(packageRoot);
    }

    /**
     * Inflate an XML layout to usable components
     * @param xmlName XML file name (may omit .xml)
     * @param attrs Root attributes override (null to ignore)
     * @return Inflated layout or null if not found/failed
     */
    public static InflatedLayout inflate(String xmlName, AttributeSet attrs) {
        if (!xmlName.endsWith(".xml"))
            xmlName = xmlName + ".xml";

        InflatedLayout inf = new InflatedLayout(xmlName);

        Document doc = xmlCache.get(xmlName);
        if (doc == null) {
            File xmlFile = new File(LAYOUT_DIR, xmlName);
            try (InputStream is = Resources.getResourceStream(xmlFile)){
                // Deserialize XML document
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = dBuilder.parse(is);
                doc.getDocumentElement().normalize();
            } catch (Exception e) {
                Log.e("Failed to find XML layout named " + xmlName, e);
                return inf;
            }
        }

        try {
            // Build component hierarchy
            inflate(inf, null, doc, attrs);

            // Cache document
            xmlCache.put(xmlName, doc);
        } catch (Exception e) {
            Log.e("Failed to inflate layout named" + xmlName, e);
        }

        return inf;
    }

    /**
     * Inflate an XML layout to usable components
     * @param xmlName XML file name (may omit .xml)
     * @return Inflated layout or null if not found/failed
     */
    public static InflatedLayout inflate(String xmlName) {
        return inflate(xmlName, null);
    }

    private static void inflate(InflatedLayout inf, Container parent, Node pNode, AttributeSet rootAttrs) {
        NodeList nList = pNode.getChildNodes();
        int nLength = nList.getLength();
        for (int i = 0; i < nLength; i++) {

            Node node = nList.item(i);
            String name = node.getNodeName();
            if (name.equals("#text") || node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            // Read attributes from XML
            AttributeSet attrs = new AttributeSet((Element) node);

            // Attribute override
            if (rootAttrs != null)
                attrs.putAll(rootAttrs);

            Component comp;
            if (name.equals("include")) {
                // Include another layout in this one
                comp = inflateSubLayout(attrs);
            } else {
                // Create component from tag name and attributes
                comp = createComponent(name, attrs);
            }

            // Failed to create component
            if (comp == null)
                continue;

            LayoutParams lp = new LayoutParams(attrs);

            if (comp instanceof AbstractLayout) {
                AbstractLayout ll = (AbstractLayout) comp;
                ll.setLayoutParams(lp);
            }

            if (parent != null) {
                if (parent instanceof AbstractLayout)
                    parent.add(comp, lp);
                else if (parent instanceof ScrollLayout)
                    ((ScrollLayout) parent).setViewportView(comp);
                else
                    parent.add(comp);
            }

            if (comp instanceof Container)
                inflate(inf, (Container) comp, node, null);

            if (parent == null)
                inf.setRoot(comp);

            inf.addComponent(comp);
        }

        if (parent instanceof AbstractLayout)
            ((AbstractLayout) parent).onFinishInflate(inf);
    }

    private static Component inflateSubLayout(AttributeSet attrs) {
        String layoutName = attrs.getString("layout", null);
        if (layoutName == null)
            return null;
        if (layoutName.startsWith("@layout/"))
            layoutName = layoutName.substring("@layout/".length());
        InflatedLayout inc = LayoutInflater.inflate(layoutName, attrs);
        return inc.getRoot();
    }

    private static Class<?> findClass(String baseName) {
        Class<?> c = baseNameCache.get(baseName);
        if (c != null)
            return c;
        try {
            if (baseName.contains(".")) {
                try {
                    return c = Class.forName(baseName);
                } catch (Exception ignore) {
                }
            }
            for (String pkgRoot : PACKAGE_ROOTS) {
                try {
                    return c = Class.forName(pkgRoot + "." + baseName);
                } catch (Exception ignore) {
                }
            }
            try {
                return c = Class.forName("javax.swing.J" + baseName);
            } catch (Exception ignore) {
            }
            return null;
        } finally {
            baseNameCache.put(baseName, c);
        }
    }

    private static Component createComponent(String name, AttributeSet attrs) {
        Class<?> viewClass = findClass(name);
        if (viewClass == null || !Component.class.isAssignableFrom(viewClass)) {
            Log.e("Failed to find Component class " + name);
            return null;
        }

        // Attempt to instantiate using attribute set
        try {
            Constructor<?> ctor = viewClass.getConstructor(AttributeSet.class);
            if (ctor != null)
                return (Component) ctor.newInstance(attrs);
        } catch (NoSuchMethodException ignore) {
        } catch (Exception e) {
            Log.e("Failed to create new instance of " + viewClass, e);
        }

        // Fallback initialization
        return createFallback(viewClass, attrs);
    }

    private static final Class<?>[] ARGUMENT_CLASSES = {
            String.class, boolean.class, int.class, float.class, Color.class
    };

    private static Component createFallback(Class<?> cl, AttributeSet attrs) {
        Component comp = null;
        try {
            Constructor<?> ctor = cl.getConstructor();
            if (ctor == null)
                return null;
            comp = (Component) ctor.newInstance();
            for (Map.Entry<String, String> e : attrs.entrySet()) {
                String name = e.getKey();
                String mName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                for (Class<?> argClass : ARGUMENT_CLASSES) {
                    if (invokeMethod(comp, e.getKey(), attrs, mName, argClass))
                        break;
                }
            }
        } catch (Exception e) {
            Log.e("Failed to initialize Component fallback: " + cl, e);
        }
        return comp;
    }

    private static boolean invokeMethod(Component comp, String key, AttributeSet attrs, String methodName, Class<?> argClass) {
        try {
            Object arg = attrs.get(key);
            if (argClass == boolean.class)
                arg = attrs.getBoolean(key, false);
            else if (argClass == int.class)
                arg = attrs.getDimension(key, 0);
            else if (argClass == float.class)
                arg = attrs.getFloat(key, 0f);
            else if (argClass == Color.class)
                arg = attrs.getColor(key, Color.WHITE);
            Method method = comp.getClass().getMethod(methodName, argClass);
            if (method != null) {
                method.invoke(comp, arg);
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }
}
