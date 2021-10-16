package software.blob.ui.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Miscellaneous file methods
 */
public class FileUtils {

    /**
     * Get file extension from name
     * @param fileName File name
     * @return Extension (all lowercase; period omitted)
     */
    public static String getExtension(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        return dotIdx != -1 ? fileName.substring(dotIdx + 1).toLowerCase() : "";
    }

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    /**
     * Strip the extension from a file name
     * @param fileName File name
     * @return File name without extensino
     */
    public static String stripExtension(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        return dotIdx != -1 ? fileName.substring(0, dotIdx) : fileName;
    }

    public static String stripExtension(File file) {
        return stripExtension(file.getName());
    }

    /**
     * Append an extension to a file (if the extension isn't already present)
     * @param file File
     * @param ext Extension (dot optional)
     * @return File with extension
     */
    public static File appendExtension(File file, String ext) {
        String curExt = getExtension(file);
        if (curExt.equals(ext))
            return file;
        if (!ext.startsWith("."))
            ext = "." + ext;
        return new File(file.getParent(), file.getName() + ext);
    }

    /**
     * List all files in a directory with a given extension
     * @param dir Directory
     * @param ext Extension to match
     * @return File array
     */
    public static File[] listFiles(File dir, String ext) {
        final String dotExt = ext.startsWith(".") ? ext : "." + ext;
        return dir.listFiles((dir1, name) -> name.endsWith(dotExt));
    }

    /**
     * List all files recursively in a directory
     * @param dir Directory
     * @param filter Filename filter
     * @return List of files
     */
    public static List<File> listFilesRecursive(File dir, FilenameFilter filter) {
        List<File> ret = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null)
            return ret;
        for (File f : files) {
            if (f.isDirectory())
                ret.addAll(listFilesRecursive(f, filter));
            else if (filter.accept(null, f.getName()))
                ret.add(f);
        }
        return ret;
    }

    /**
     * Recursively delete an entire directory
     * @param dir Directory to delete
     * @return True if all contents deleted, false otherwise
     */
    public static boolean deleteDirectory(File dir) {
        boolean success = true;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    deleteDirectory(f);
                else
                    success &= f.delete();
            }
        }
        success &= dir.delete();
        return success;
    }

    /**
     * Get the proper file pointer given a potentially relative path
     * @param dir Directory
     * @param path Path string (can be relative or absolute)
     * @return File pointer
     */
    public static File getFile(File dir, String path) {
        if (path.startsWith("/"))
            return new File(path);
        return new File(dir, path);
    }

    /**
     * Get a relative path given a directory and sub-file
     * @param dir Directory
     * @param file File
     * @return Relative path or absolute path if no relative path exists
     */
    public static String getRelativePath(File dir, File file) {
        StringBuilder relPath = new StringBuilder();
        File parent = file;
        while (parent != null) {
            if (parent.equals(dir))
                return "." + relPath;
            String pName = parent.getName();
            if (!pName.equals(".")) {
                relPath.insert(0, pName);
                relPath.insert(0, File.separator);
            }
            parent = parent.getParentFile();
        }
        return file.getAbsolutePath();
    }
}
