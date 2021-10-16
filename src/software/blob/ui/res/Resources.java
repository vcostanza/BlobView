package software.blob.ui.res;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load bundled resources
 */
public class Resources {

    // Filesystem resource directory
    public static File RES_DIR = new File("./res");

    // Classes to use for resource loading
    private static final List<Class<?>> resourceClasses = new ArrayList<>();
    static {
        resourceClasses.add(Resources.class);
    }

    // Cached image resources
    private static final Map<String, Image> icons = new HashMap<>();

    /**
     * Add class to use for resource loading
     * @param cl Class
     */
    public static void addResourceClass(Class<?> cl) {
        resourceClasses.add(0, cl);
    }

    /**
     * Remove class used for resource loading
     * @param cl Class
     */
    public static void removeResourceClass(Class<?> cl) {
        resourceClasses.remove(cl);
    }

    /**
     * Get input stream for an embedded resource
     * @param resPath Resource path (relative to software/blob/ui/res)
     * @param maxStreams Maximum number of streams to search for
     * @return Input streams
     * @throws IOException Resource not found
     */
    public static List<InputStream> getResourceStreams(String resPath, int maxStreams) throws IOException {
        List<InputStream> streams = new ArrayList<>();

        // Windows path fix
        resPath = resPath.replace("\\", "/");

        // Attempt to load a resource using each of the registered classes
        for (Class<?> cl : resourceClasses) {
            InputStream is = cl.getResourceAsStream(resPath);
            if (is != null) {
                streams.add(is);
                if (--maxStreams <= 0)
                    return streams;
            }
        }

        // Failed to load resource using any of the class loaders
        if (streams.isEmpty())
            throw new IOException("Failed to find resource: " + resPath, new Throwable());

        return streams;
    }

    /**
     * Get all input streams for files or embedded resources
     * @param resFile Resource file
     * @param maxStreams Maximum number of streams to search for
     * @return Input streams
     */
    public static List<InputStream> getResourceStreams(File resFile, int maxStreams) throws IOException {
        List<InputStream> streams = new ArrayList<>();
        if (resFile.exists()) {
            try {
                streams.add(new FileInputStream(resFile));
                if (--maxStreams <= 0)
                    return streams;
            } catch (Exception e) {
                throw new IOException("Failed to open resource file: " + resFile, e);
            }
        }
        String path = resFile.getAbsolutePath();
        String dirPath = RES_DIR.getAbsolutePath();
        if (path.startsWith(dirPath))
            path = path.substring(dirPath.length() + 1);
        streams.addAll(getResourceStreams(path, maxStreams));
        return streams;
    }

    public static List<InputStream> getResourceStreams(File resFile) throws IOException {
        return getResourceStreams(resFile, Integer.MAX_VALUE);
    }

    /**
     * Get input stream for a filesystem or embedded resource
     * Checks is file system resource exists first then checks embedded
     * @param resFile Resource file
     * @return Input stream
     */
    public static InputStream getResourceStream(File resFile) throws IOException {
        List<InputStream> streams = getResourceStreams(resFile, 1);
        return !streams.isEmpty() ? streams.get(0) : null;
    }

    /**
     * Load an image
     * @param imagePath Icon path relative to "/res/icons"
     * @return Icon
     */
    public static Image getImage(String imagePath) {
        if (!imagePath.contains("."))
            imagePath = imagePath + ".png";
        Image image = icons.get(imagePath);
        if (image != null)
            return image;
        try {
            image = ImageIO.read(getResourceStream(new File(RES_DIR, "icons/" + imagePath)));
        } catch (Exception e) {
            System.err.println("Failed to get image: " + imagePath);
            e.printStackTrace();
        }
        if (image != null)
            icons.put(imagePath, image);
        return image;
    }

    /**
     * Load an icon
     * @param iconPath Icon path relative to "/res/icons"
     * @return Icon
     */
    public static Icon getIcon(String iconPath) {
        Image img = getImage(iconPath);
        return img != null ? new ImageIcon(img) : null;
    }

    /**
     * Load an image as a cursor
     * @param imagePath Image path
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return Cursor
     */
    public static Cursor getCursor(String imagePath, int x, int y) {
        return Toolkit.getDefaultToolkit().createCustomCursor(getImage(imagePath), new Point(x, y), imagePath);
    }
}
