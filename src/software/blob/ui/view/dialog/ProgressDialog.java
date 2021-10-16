package software.blob.ui.view.dialog;

import software.blob.ui.view.TextView;
import software.blob.ui.view.layout.InflatedLayout;
import software.blob.ui.view.layout.LayoutInflater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog that displays progress
 */
public class ProgressDialog extends LayoutDialog implements ActionListener {

    private final TextView message;
    private final JProgressBar progressBar;

    private Runnable onCancel;
    private boolean cancelled;

    public ProgressDialog(Window window) {
        super(window, false);

        InflatedLayout inf = LayoutInflater.inflate("progress_dialog");
        this.message = inf.findByName("message");
        this.progressBar = inf.findByName("progressBar");

        this.okBtn.setVisible(false);

        setView(inf.getRoot());
        setSize(280, 120);
    }

    /**
     * Set the message that is displayed in the dialog
     * @param msg Dialog message
     * @return Dialog
     */
    public ProgressDialog setMessage(final String msg) {
        message.setVisible(!msg.isEmpty());
        message.setText(msg);
        return this;
    }

    /**
     * Set progress value
     * @param progress Progress
     * @param max Maximum progress
     */
    public void setProgress(int progress, int max) {
        int percent = Math.round(((float) progress / max) * 100);
        progressBar.setString(percent + "%");
        progressBar.setValue(progress);
        progressBar.setMaximum(max);
    }

    /**
     * Cancel this dialog
     */
    public void cancel() {
        cancelled = true;
        dismiss();
    }

    /**
     * This dialog has been cancelled
     * @return True if canceleld
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Set the callback invoked when the dialog is cancelled
     * @param onCancel Cancel callback
     */
    public void setOnCancelListener(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("Cancel")) {
            if (onCancel != null)
                onCancel.run();
            cancel();
        }
    }
}
