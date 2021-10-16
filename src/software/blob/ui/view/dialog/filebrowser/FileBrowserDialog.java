package software.blob.ui.view.dialog.filebrowser;

import software.blob.ui.util.DialogUtils;
import software.blob.ui.view.*;
import software.blob.ui.view.dialog.LayoutDialog;
import software.blob.ui.view.layout.InflatedLayout;
import software.blob.ui.view.layout.LayoutInflater;
import software.blob.ui.view.layout.LinearLayout;
import software.blob.ui.view.layout.list.ListAdapter;
import software.blob.ui.view.layout.list.ListView;
import software.blob.ui.view.listener.ClickListener;
import software.blob.ui.view.listener.DoubleClickListener;
import software.blob.ui.view.listener.HoverListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Improved file browser dialog
 */
public class FileBrowserDialog extends LayoutDialog implements ClickListener, SelectButton.OnClickListener {

    // Constants
    public static final int SELECT_FILES_ONLY = 0;
    public static final int SELECT_DIRS_ONLY = 1;
    public static final int SELECT_ALL = 2;

    // File filters
    protected static final FileFilter VISIBLE_FILTER = file -> !file.getName().startsWith(".");
    protected static final FileFilter DIR_FILTER = file -> VISIBLE_FILTER.accept(file) && file.isDirectory();

    // Sort modes
    protected static final FileComparator SORT_NAME_ASC = (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName());
    protected static final FileComparator SORT_SIZE_ASC = (f1, f2) -> Long.compare(f1.length(), f2.length());
    protected static final FileComparator SORT_DATE_ASC = (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified());
    protected static final FileComparator SORT_NAME_DSC = (f1, f2) -> SORT_NAME_ASC.compareFiles(f2, f1);
    protected static final FileComparator SORT_SIZE_DSC = (f1, f2) -> SORT_SIZE_ASC.compareFiles(f2, f1);
    protected static final FileComparator SORT_DATE_DSC = (f1, f2) -> SORT_DATE_ASC.compareFiles(f2, f1);

    // Commonly used formats and colors
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd  hh:mm a");
    protected static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#,##0.#");
    protected static final Color SELECTED_COLOR = new Color(64, 64, 64);
    protected static final Color HOVER_COLOR = new Color(48, 48, 48);

    // Views
    protected final ListView fileTable;
    protected final FileListAdapter fileAdapter;
    protected final JComboBox<String> commonPlaces;
    protected final ImageButton backBtn, forwardBtn, upBtn, homeBtn, folderBtn;
    protected final SelectButton sortName, sortSize, sortDate;
    protected final EditText fileNameTxt;
    protected final JComboBox<String> fileTypeBox;

    // Set by external usage
    protected int selectMode = SELECT_FILES_ONLY;
    protected boolean checkFileExists = true;
    protected final List<FileTypeFilter> typeFilters = new ArrayList<>();
    protected OnFileSelectedListener listener;

    // Internal
    protected final Preferences prefs;
    protected File homeDir;
    protected File currentDir;
    protected File highlightFile, hoverFile;
    protected final List<File> commonDirs = new ArrayList<>();
    protected FileTypeFilter typeFilter;
    protected FileComparator sortMode = SORT_NAME_ASC;
    protected final Stack<File> backStack = new Stack<>();
    protected final Stack<File> forwardStack = new Stack<>();

    public FileBrowserDialog(Window window) {
        super(window);

        prefs = Preferences.userNodeForPackage(getClass());

        InflatedLayout inf = LayoutInflater.inflate("file_browser_dialog");
        commonPlaces = inf.findByName("common_places");
        fileTable = inf.findByName("file_table");
        backBtn = inf.findByName("nav_back");
        forwardBtn = inf.findByName("nav_forward");
        upBtn = inf.findByName("nav_up");
        homeBtn = inf.findByName("nav_home");
        folderBtn = inf.findByName("new_folder");
        sortName = inf.findByName("sort_name");
        sortSize = inf.findByName("sort_size");
        sortDate = inf.findByName("sort_date");
        fileNameTxt = inf.findByName("file_name");
        fileTypeBox = inf.findByName("file_type");

        // Setup listeners
        backBtn.setOnClickListener(this);
        forwardBtn.setOnClickListener(this);
        upBtn.setOnClickListener(this);
        homeBtn.setOnClickListener(this);
        folderBtn.setOnClickListener(this);
        sortName.setClickListener(this);
        sortSize.setClickListener(this);
        sortDate.setClickListener(this);
        fileNameTxt.addActionListener(this);
        fileTypeBox.addActionListener(this);
        commonPlaces.addActionListener(this);

        // Misc tweaks
        fileTable.setAdapter(fileAdapter = new FileListAdapter());

        setView(inf.getRoot());
        setTitle("File Browser");
        setSize(500, 500);
        setApproveButtonText("Open");
    }

    /**
     * Set the singular file type filter for this dialog
     * @param filter Type filter
     */
    public void setTypeFilter(FileTypeFilter filter) {
        typeFilters.clear();
        addTypeFilter(filter);
        refreshTypeFilterBox();
    }

    /**
     * Add a type filter
     * @param filter Type filter
     */
    public void addTypeFilter(FileTypeFilter filter) {
        if (typeFilters.add(filter)) {
            if (typeFilter == null)
                setCurrentTypeFilter(filter);
            else
                refreshTypeFilterBox();
        }
    }

    /**
     * Remove a type filter
     * @param filter Type filter
     */
    public void removeTypeFilter(FileTypeFilter filter) {
        if (typeFilters.remove(filter)) {
            if (typeFilter == filter)
                setCurrentTypeFilter(null);
            else
                refreshTypeFilterBox();
        }
    }

    /**
     * Set the select mode for this browser
     * @param mode Select mode
     */
    public void setSelectMode(int mode) {
        this.selectMode = mode;
    }

    /**
     * Set whether the dialog should check if a file exists before selecting it
     * @param existsCheck True to check if file exists when selecting
     */
    public void setFileExistsCheck(boolean existsCheck) {
        checkFileExists = existsCheck;
    }

    /**
     * Set the text shown for the "OK" button
     * @param text Text to show
     */
    public void setApproveButtonText(String text) {
        okBtn.setText(text);
    }

    /**
     * Set the listener to be invoked when a file has been selected
     * @param listener Listener
     */
    public void setOnFileSelectedListener(OnFileSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Set the system home directory
     * @param home Home directory
     */
    public void setHomeDirectory(File home) {
        this.homeDir = home;
    }

    /**
     * Get the home directory defined for this system
     * @return Home directory
     */
    protected File getHomeDirectory() {
        return this.homeDir;
    }

    /**
     * Select a file
     * @param file File to select
     * @param click True if clicking to select
     */
    protected void selectFile(File file, boolean click) {

        // Check if file exists first (if check is enabled)
        boolean exists = file.exists();
        if (!exists && checkFileExists) {
            DialogUtils.errorDialog(getTitle(), "\"" + file.getName() + "\" does not exist!");
            return;
        }

        boolean isDir = exists && file.isDirectory();
        if (isDir && (click || selectMode == SELECT_FILES_ONLY))
            openDir(file);
        else if (listener != null && (isDir || selectMode != SELECT_DIRS_ONLY)) {
            if (currentDir != null)
                prefs.put(getLastDirectoryPreference(), currentDir.getAbsolutePath());
            listener.onFileSelected(file);
        }
    }

    /**
     * Select file based on the contents of the file name input
     */
    protected void selectFile() {
        String fName = fileNameTxt.getText();
        if (fName.isEmpty())
            return;
        File file = new File(currentDir, fName);
        selectFile(file, false);
    }

    /**
     * Highlight a file in the browser
     * @param file File
     */
    protected void highlight(File file) {
        highlightFile = file;
        fileAdapter.notifyDatasetChanged();
    }

    /**
     * Set the currently active type filter
     * @param filter Type filter
     */
    protected void setCurrentTypeFilter(FileTypeFilter filter) {
        typeFilter = filter;
        refreshTypeFilterBox();
        refreshFileList();
    }

    /**
     * Set the file sort mode
     * @param fc File comparator
     */
    protected void setSortMode(FileComparator fc) {
        if (fc == null)
            fc = SORT_NAME_ASC;
        if (sortMode != fc) {
            sortMode = fc;
            refreshFileList();
        }
    }

    /**
     * Get the display icon for a given file
     * @param file File
     * @param isDir True if the file is a directory (saves a {@link File#isDirectory()} check)
     * @return Icon resource string
     */
    protected String getFileIcon(File file, boolean isDir) {
        return isDir ? "open" : "new_file";
    }

    /**
     * Set up the common directories shown in the top combo box
     */
    protected void refreshCommonPlaces() {
        File home = getHomeDirectory();
        refreshCommonPlaces(home.listFiles(DIR_FILTER));
    }

    /**
     * Set up the common directories shown in the top combo box
     * @param dirs Directories to show
     */
    protected void refreshCommonPlaces(File[] dirs) {
        commonDirs.clear();
        if (dirs != null)
            commonDirs.addAll(Arrays.asList(dirs));
        if (currentDir != null) {
            commonDirs.remove(currentDir);
            commonDirs.add(0, currentDir);
        }
        String[] dirNames = new String[commonDirs.size()];
        for (int i = 0; i < dirNames.length; i++)
            dirNames[i] = commonDirs.get(i).getName();
        commonPlaces.setModel(new DefaultComboBoxModel<>(dirNames));
    }

    /**
     * Refresh the choices in the type filter box
     */
    protected void refreshTypeFilterBox() {
        String[] typeDesc = new String[typeFilters.size()];
        int selected = -1;
        for (int i = 0; i < typeFilters.size(); i++) {
            FileTypeFilter filter = typeFilters.get(i);
            if (filter == typeFilter)
                selected = i;
            typeDesc[i] = typeFilters.get(i).getDescription();
        }
        fileTypeBox.setModel(new DefaultComboBoxModel<>(typeDesc));
        fileTypeBox.setSelectedIndex(selected);
    }

    /**
     * Refresh the nav buttons at the top of the dialog
     */
    protected void refreshNavButtons() {
        backBtn.setEnabled(!backStack.isEmpty());
        forwardBtn.setEnabled(!forwardStack.isEmpty());
    }

    /**
     * Set the current directory
     * @param dir Directory
     */
    protected void setCurrentDir(File dir) {
        if (!Objects.equals(dir, currentDir)) {
            currentDir = dir;
            highlightFile = null;
            fileTable.resetScroll();
            refreshCommonPlaces();
            refreshFileList();
        }
    }

    /**
     * Open a directory and display its contents
     * This is different from {@link #setCurrentDir(File)} because it uses the back stack
     * @param dir Directory
     */
    protected void openDir(File dir) {
        if (!Objects.equals(dir, currentDir)) {
            if (currentDir != null) {
                backStack.push(currentDir);
                forwardStack.clear();
                refreshNavButtons();
            }
            setCurrentDir(dir);
        }
    }

    /**
     * Get the preference key for the last used directory
     * @return Last directory preference key
     */
    protected String getLastDirectoryPreference() {
        return "lastDirectory";
    }

    /**
     * Refresh the file list UI
     */
    protected void refreshFileList() {
        // Query files
        FileFilter filter = typeFilter != null ? typeFilter : VISIBLE_FILTER;
        File[] fileArray = currentDir != null ? currentDir.listFiles(filter) : new File[0];
        if (fileArray == null)
            fileArray = new File[0];
        Arrays.sort(fileArray, sortMode);
        fileAdapter.setFiles(Arrays.asList(fileArray));
    }

    private class FileListAdapter extends ListAdapter implements
            ClickListener, DoubleClickListener, HoverListener {

        private final List<File> files = new ArrayList<>();

        void setFiles(List<File> files) {
            this.files.clear();
            this.files.addAll(files);
            notifyDatasetChanged();
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public File getItem(int position) {
            return files.get(position);
        }

        @Override
        public View getView(int position, View existing, ListView list) {

            LinearLayout row = existing instanceof LinearLayout ? (LinearLayout) existing : null;
            RowHolder h = row != null ? (RowHolder) row.getTag() : null;
            if (h == null) {
                // Create new layout
                InflatedLayout inf = LayoutInflater.inflate("file_browser_entry");
                fileTable.add(row = inf.getRoot());
                row.setTag(h = new RowHolder());
                h.root = row;
                h.icon = row.findChildByName("icon");
                h.name = row.findChildByName("name");
                h.size = row.findChildByName("size");
                h.date = row.findChildByName("date");
                row.setOnClickListener(this);
                row.setOnDoubleClickListener(this);
                row.setOnHoverListener(this);
            }

            File file = h.file = getItem(position);
            boolean isDir = file.isDirectory();
            h.icon.setImage(getFileIcon(file, isDir));
            h.name.setText(file.getName());
            h.size.setText(isDir ? "" : readableFileSize(file.length()) + "   ");
            h.date.setText(DATE_FORMAT.format(file.lastModified()));
            row.setBackground(highlightFile == file ? SELECTED_COLOR
                    : (hoverFile == file ? HOVER_COLOR : null));

            return row;
        }

        @Override
        public void onClick(View view, MouseEvent event) {
            RowHolder h = getRowHolder(view);
            if (h != null) {
                highlight(h.file);
                fileNameTxt.setText(h.file.getName());
            }
        }

        @Override
        public void onDoubleClick(View view, MouseEvent event) {
            RowHolder h = getRowHolder(view);
            if (h != null)
                selectFile(h.file, true);
        }

        @Override
        public void onHoverStart(View view, MouseEvent event) {
            RowHolder row = getRowHolder(view);
            if (row != null) {
                hoverFile = row.file;
                fileAdapter.notifyDatasetChanged();
            }
        }

        @Override
        public void onHoverEnd(View view, MouseEvent event) {
            RowHolder row = getRowHolder(view);
            if (row != null) {
                if (row.file == hoverFile)
                    hoverFile = null;
                fileAdapter.notifyDatasetChanged();
            }
        }

        private RowHolder getRowHolder(View view) {
            Object tag = view.getTag();
            return tag instanceof RowHolder ? (RowHolder) tag : null;
        }
    }

    private static class RowHolder {
        File file;
        LinearLayout root;
        ImageView icon;
        TextView name, size, date;
    }

    @Override
    public LayoutDialog showDialog(Runnable onOk) {
        // Go to last directory if current directory is not set
        if (currentDir == null) {
            String lastDirKey = getLastDirectoryPreference();
            String lastDir = prefs.get(lastDirKey, getHomeDirectory().getAbsolutePath());
            File dir = new File(lastDir);
            if (!dir.exists())
                dir = getHomeDirectory();
            setCurrentDir(dir);
        }

        // Populate common places with non-hidden directories in the home folder
        refreshCommonPlaces();
        refreshNavButtons();

        return super.showDialog(onOk);
    }

    @Override
    protected void onOK() {
        super.onOK();
        selectFile();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();

        // Open file when we press enter
        if (!confirmOnEnter && src == fileNameTxt && currentDir != null)
            selectFile();

        // Common places box
        else if (src == commonPlaces)
            openDir(commonDirs.get(commonPlaces.getSelectedIndex()));

        // Set type filter
        else if (src == typeFilters)
            setCurrentTypeFilter(typeFilters.get(fileTypeBox.getSelectedIndex()));

        else
            super.actionPerformed(e);
    }

    @Override
    public void onClick(View view, MouseEvent event) {
        String name = view.getName();
        if (name == null)
            return;

        switch (name) {

            // Navigate back or forward a directory
            case "nav_back":
            case "nav_forward": {
                Stack<File> stack = name.equals("nav_back") ? backStack : forwardStack;
                Stack<File> other = stack == backStack ? forwardStack : backStack;
                if (!stack.isEmpty()) {
                    File dir = stack.pop();
                    other.push(currentDir);
                    setCurrentDir(dir);
                    refreshNavButtons();
                }
                break;
            }

            // Navigate up a directory
            case "nav_up": {
                File parentDir = currentDir != null ? currentDir.getParentFile() : null;
                if (parentDir == null)
                    parentDir = getHomeDirectory();
                openDir(parentDir);
                break;
            }

            // Navigate to the home directory
            case "nav_home": {
                openDir(getHomeDirectory());
                break;
            }

            // Create a new folder
            case "new_folder": {
                if (currentDir != null) {
                    // TODO: Prompt for name - too lazy to code it now lol
                    File newDir = new File(currentDir, "New folder");
                    if (!newDir.exists() && !newDir.mkdir())
                        DialogUtils.errorDialog("New Folder", "Failed to make directory: " + newDir.getName());
                    else
                        refreshFileList();
                }
                break;
            }
        }
    }

    @Override
    public void onClick(MouseEvent e) {
        // Sort mode buttons
        Component c = e.getComponent();
        if (c == sortName)
            setSortMode(sortMode == SORT_NAME_ASC ? SORT_NAME_DSC : SORT_NAME_ASC);
        else if (c == sortSize)
            setSortMode(sortMode == SORT_SIZE_ASC ? SORT_SIZE_DSC : SORT_SIZE_ASC);
        else if (c == sortDate)
            setSortMode(sortMode == SORT_DATE_ASC ? SORT_DATE_DSC : SORT_DATE_ASC);

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        switch (kc) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN: {
                List<File> files = fileAdapter.files;
                int idx = files.indexOf(highlightFile);
                if (idx == -1)
                    break;
                int newIdx = Math.min(Math.max(idx + (kc == KeyEvent.VK_UP ? -1 : 1), 0), files.size() - 1);
                if (newIdx != idx) {
                    highlight(files.get(newIdx));
                    fileTable.scrollToPosition(newIdx);
                }
                break;
            }
        }
        super.keyPressed(e);
    }

    private static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return SIZE_FORMAT.format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
