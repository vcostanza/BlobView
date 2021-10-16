package software.blob.ui.util;

import software.blob.ui.util.Log;

import java.awt.*;

/**
 * Helper methods related to color
 */
public class ColorUtils {

    /**
     * Get a color with a new alpha value
     * @param rgb RGB color
     * @param alpha Alpha value (0 - 255)
     * @return Color w/ supplied alpha
     */
    public static Color getColor(Color rgb, int alpha) {
        return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
    }

    public static Color desaturate(Color color) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1] * 0.5f, hsb[2]);
        return new Color(rgb | (color.getAlpha() << 24));
    }

    public static Color fromHexString(String hex) {
        try {
            if (hex.startsWith("#") && hex.length() > 7)
                hex = "#" + hex.substring(hex.length() - 7);
            return Color.decode(hex);
        } catch (Exception e) {
            Log.e("Failed to parse color: " + hex, e);
            return Color.WHITE;
        }
    }
}
