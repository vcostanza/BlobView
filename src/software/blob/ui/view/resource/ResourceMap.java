package software.blob.ui.view.resource;

import org.w3c.dom.NamedNodeMap;
import software.blob.ui.res.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Layout resources
 */
public class ResourceMap {

    private static final File RES_DIR = new File("./res/values");

    private static final Map<String, Object> RES_MAP = new HashMap<>();

    static {
        scan();
    }

    private static void scan() {
        // Default resources
        loadResourceFile(new File(RES_DIR, "color.xml"));
        loadResourceFile(new File(RES_DIR, "dimen.xml"));
        loadResourceFile(new File(RES_DIR, "style.xml"));

        File[] files = RES_DIR.listFiles();
        if (files == null)
            return;
        for (File f : files) {
            if (!f.getName().endsWith(".xml"))
                continue;
            loadResourceFile(f);
        }
    }

    private static void loadResourceFile(File xmlFile) {
        List<InputStream> streams = null;
        try {
            streams = Resources.getResourceStreams(xmlFile);
            for (int i = 0; i < streams.size(); i++) {
                InputStream is = streams.remove(i--);
                loadResourceFile(is);
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (streams != null) {
                for (InputStream is : streams) {
                    try {
                        is.close();
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private static void loadResourceFile(InputStream is) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            doc.getDocumentElement().normalize();

            NodeList res = doc.getElementsByTagName("resources");
            if (res == null)
                return;

            int rLength = res.getLength();
            for (int i = 0; i < rLength; i++) {

                Node rNode = res.item(i);
                NodeList children = rNode.getChildNodes();
                int nLength = children.getLength();

                for (int j = 0; j < nLength; j++) {

                    Node n = children.item(j);
                    String elName = n.getNodeName();
                    if (elName.equals("#text"))
                        continue;

                    NamedNodeMap attrs = n.getAttributes();

                    // Name attribute
                    String atName = attrs.getNamedItem("name").getNodeValue();

                    if (elName.equals("style")) {
                        // Style resource parsing
                        StyleResource style = new StyleResource(atName);

                        // Parent resource to inherit from
                        Node parentName = attrs.getNamedItem("parent");
                        if (parentName != null) {
                            StyleResource parent = getStyleResource(parentName.getNodeValue());
                            style.setParent(parent);
                        }

                        // Read attributes
                        NodeList sChildren = n.getChildNodes();
                        int sLength = sChildren.getLength();
                        for (int s = 0; s < sLength; s++) {
                            n = sChildren.item(s);
                            elName = n.getNodeName();
                            if (elName.equals("item")) {
                                String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
                                String itemValue = n.getFirstChild().getNodeValue();
                                style.put(itemName, itemValue);
                            }
                        }

                        RES_MAP.put("style/" + style.getName(), style);
                    } else {
                        // String resource
                        Node value = n.getFirstChild();
                        if (value != null)
                            RES_MAP.put(elName + "/" + atName, value.getNodeValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getResource(String key) {
        return RES_MAP.get(key);
    }

    public static StyleResource getStyleResource(String key) {
        Object o = getResource(key);
        return o instanceof StyleResource ? (StyleResource) o : null;
    }

    public static String getStringResource(String key) {
        Object o = getResource(key);
        return o instanceof String ? (String) o : null;
    }

    public static Color getColorResource(String key) {
        String value = getStringResource("color/" + key);
        // Hex color code
        try {
            return Color.decode(value);
        } catch (Exception e) {
            return null;
        }
    }
}
