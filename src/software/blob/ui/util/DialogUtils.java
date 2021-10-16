package software.blob.ui.util;

import javax.swing.*;

/**
 * Methods for creating a quick confirm/error dialog
 */
public class DialogUtils {

    /**
     * Create a Yes/No/Cancel confirm dialog
     * @param title Dialog title
     * @param message Dialog message
     * @return True if Yes was clicked
     */
    public static boolean confirmDialog(String title, String message) {
        int result = JOptionPane.showConfirmDialog(null, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return result == JFileChooser.APPROVE_OPTION;
    }

    /**
     * Create a generic error dialog
     * @param title Dialog title
     * @param message Dialog message
     */
    public static void errorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }
}
