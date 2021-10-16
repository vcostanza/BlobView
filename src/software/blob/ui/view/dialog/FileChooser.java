package software.blob.ui.view.dialog;

import software.blob.ui.view.dialog.filebrowser.FileBrowserDialog;

import javax.swing.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * File chooser with some built in helpers
 * For a nicer looking file browser with more features see {@link FileBrowserDialog}
 */
public class FileChooser extends JFileChooser {

    public interface SingleFileCallback {
        void onApprove(File selected);
    }

    public interface MultiFileCallback {
        void onApprove(File[] selected);
    }

    private SingleFileCallback singleCallback;
    private MultiFileCallback multiCallback;
    private boolean curDirSet;

    public FileChooser(String title) {
        super();
        setTitle(title);
        setFileHidingEnabled(false);
        setAcceptAllFileFilterUsed(false);
    }

    public FileChooser() {
        this("");
    }

    public FileChooser(File defaultDir) {
        this();
        setDirectory(defaultDir);
    }

    public FileChooser setTitle(String title) {
        setDialogTitle(title);
        return this;
    }

    public FileChooser setDirectory(File dir) {
        setCurrentDirectory(dir);
        curDirSet = true;
        return this;
    }

    public FileChooser setDirectoriesOnly(boolean dirsOnly) {
        setFileSelectionMode(dirsOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES);
        return this;
    }

    public FileChooser setFilesOnly(boolean filesOnly) {
        setFileSelectionMode(filesOnly ? JFileChooser.FILES_ONLY : JFileChooser.FILES_AND_DIRECTORIES);
        return this;
    }

    public FileChooser setSingleFileCallback(SingleFileCallback cb) {
        setMultiSelectionEnabled(false);
        this.singleCallback = cb;
        return this;
    }

    public FileChooser setMultiFileCallback(MultiFileCallback cb) {
        setMultiSelectionEnabled(true);
        this.multiCallback = cb;
        return this;
    }

    public int showSaveDialog() {
        setDialogType(SAVE_DIALOG);
        return showDialog();
    }

    public int showOpenDialog() {
        setDialogType(OPEN_DIALOG);
        return showDialog();
    }

    public int showDialog() {
        String lastDirKey = getLastDirectoryPreference();
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        if (!curDirSet) {
            String lastDir = prefs.get(lastDirKey, getDefaultDirectory().getAbsolutePath());
            setDirectory(new File(lastDir));
        }
        int retVal = showDialog(getParent(), null);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            prefs.put(lastDirKey, getCurrentDirectory().getAbsolutePath());
            if (isMultiSelectionEnabled()) {
                if (multiCallback != null)
                    multiCallback.onApprove(getSelectedFiles());
            } else {
                if (singleCallback != null)
                    singleCallback.onApprove(getSelectedFile());
            }
        }
        return retVal;
    }

    protected String getLastDirectoryPreference() {
        return "lastDirectory";
    }

    protected File getDefaultDirectory() {
        return new File(System.getProperty("user.home"));
    }
}
