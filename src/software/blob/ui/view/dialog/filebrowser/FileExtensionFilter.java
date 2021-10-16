package software.blob.ui.view.dialog.filebrowser;

import java.io.File;
import java.util.Locale;

/**
 * Filter for file extensions in the file browser dialog
 */
public class FileExtensionFilter implements FileTypeFilter {

    private final String ext;
    private final String description;

    public FileExtensionFilter(String ext, String description) {
        // Standardize the extension for easier checking
        if (ext == null || ext.equals("*"))
            ext = "";
        if (!ext.isEmpty()) {
            ext = ext.toLowerCase(Locale.ROOT);
            if (!ext.startsWith("."))
                ext = "." + ext;
        }

        // Include the file type wildcard in the description
        String fmtExt = ext.isEmpty() ? "(*)" : "(*" + ext + ")";
        if (description == null)
            description = (ext.isEmpty() ? "All" : ext.substring(1).toUpperCase(Locale.ROOT)) + " files";

        this.ext = ext;
        this.description = description + " " + fmtExt;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory())
            return true; // Always show directories
        return file.getName().toLowerCase(Locale.ROOT).endsWith(ext);
    }
}
