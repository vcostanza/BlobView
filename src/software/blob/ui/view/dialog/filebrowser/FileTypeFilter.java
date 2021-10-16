package software.blob.ui.view.dialog.filebrowser;

import java.io.FileFilter;

/**
 * A {@link FileFilter} with a description for use with {@link FileBrowserDialog}
 */
public interface FileTypeFilter extends FileFilter {

    /**
     * Get the description for this file filter
     * i.e. Portable Network Graphics (.png)
     * @return Description
     */
    String getDescription();
}
