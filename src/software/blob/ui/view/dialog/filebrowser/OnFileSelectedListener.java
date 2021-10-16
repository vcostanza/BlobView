package software.blob.ui.view.dialog.filebrowser;

import java.io.File;

/**
 * On file selected interface
 */
public interface OnFileSelectedListener {

    /**
     * A file has been selected
     * @param file File
     */
    void onFileSelected(File file);
}
