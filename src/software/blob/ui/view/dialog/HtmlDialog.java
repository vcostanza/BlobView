package software.blob.ui.view.dialog;

import software.blob.ui.res.Resources;
import software.blob.ui.view.SelectButton;
import software.blob.ui.view.TextView;
import software.blob.ui.view.layout.InflatedLayout;
import software.blob.ui.view.layout.LayoutInflater;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;

/**
 * Generic dialogue for displaying an HTML page
 */
public class HtmlDialog {

    protected static final File RES_DIR = new File(Resources.RES_DIR, "html");
    private static final Font FONT_TITLE = new Font("sans-serif", Font.BOLD, 18);
    private static final Font FONT_PARAGRAPH = new Font("sans-serif", Font.PLAIN, 12);

    public HtmlDialog(Window window, String title, File htmlFile) {

        InflatedLayout layout = LayoutInflater.inflate("html_dialog");

        buildHtml(layout.findByName("htmlText"), htmlFile);

        final JDialog dialog = new JDialog(window, title);
        dialog.setSize(700, 700);
        dialog.setContentPane(layout.getRoot());
        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);

        SelectButton okButton = layout.findByName("okButton");
        okButton.setClickListener((e) -> dialog.dispose());
    }

    public HtmlDialog(Window window, String title, String htmlName) {
        this(window, title, new File(RES_DIR, htmlName));
    }

    protected void buildHtml(TextView htmlText, File htmlFile) {
        byte[] b = new byte[4096];
        StringBuilder html = new StringBuilder();
        try (InputStream is = Resources.getResourceStream(htmlFile)) {
            int read;
            while ((read = is.read(b)) != -1)
                html.append(new String(b, 0, read));
        } catch (Exception e) {
            System.err.println("Failed to load HTML Doc " + htmlFile);
            e.printStackTrace();
        }
        htmlText.setText(html.toString());
    }
}
