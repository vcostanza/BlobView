package software.blob.ui.view.dialog.filebrowser;

import java.io.File;
import java.util.Comparator;

/**
 * A {@link Comparator} intended for sorting files and directories
 */
public interface FileComparator extends Comparator<File> {

    /**
     * Compare two objects which are either both directories or files
     * @param f1 File 1
     * @param f2 File 2
     * @return See {@link #compare(File, File)}
     */
    int compareFiles(File f1, File f2);

    default int compare(File f1, File f2) {
        boolean d1 = f1.isDirectory();
        boolean d2 = f2.isDirectory();
        if (d1 == d2)
            return compareFiles(f1, f2);
        else
            return Boolean.compare(d2, d1);
    }
}
