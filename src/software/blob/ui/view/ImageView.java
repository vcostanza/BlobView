package software.blob.ui.view;

import software.blob.ui.res.Resources;

import java.awt.*;

/**
 * Simple view for rendering images
 */
public class ImageView extends View {

    protected Image image;

    private Image scaled;
    private int scWidth, scHeight;

    public ImageView(String src) {
        setImage(src);
    }

    public ImageView(AttributeSet attrs) {
        super(attrs);
        String image = attrs.getString("src", null);
        if (image != null)
            setImage(Resources.getImage(image));
    }

    /**
     * Set the image to be rendered
     * @param image Image
     */
    public void setImage(Image image) {
        if (this.image != image) {
            this.image = image;
            this.scaled = null;
            repaint();
        }
    }

    /**
     * Set the image to be rendered
     * @param imageId Image
     */
    public void setImage(String imageId) {
        setImage(Resources.getImage(imageId));
    }

    @Override
    public void paint(Graphics2D g) {
        super.paint(g);

        if (this.image == null)
            return;

        // Get image dimensions w/ padding
        int width = getWidth(), height = getHeight();
        Insets padding = getPadding();
        width -= padding.left + padding.right;
        height -= padding.top + padding.bottom;
        if (width < 0 || height < 0)
            return;

        // Update scaled version of the image if we need to
        // Scaling here gives much better results than using rendering hints
        if (this.scaled == null || this.scWidth != width || this.scHeight != height) {
            this.scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH | Image.SCALE_AREA_AVERAGING);
            this.scWidth = width;
            this.scHeight = height;
        }

        // Draw image
        g.drawImage(this.scaled, padding.left, padding.top, width, height, null);
    }
}
