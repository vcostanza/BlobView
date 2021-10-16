package software.blob.ui.theme;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.*;
import java.awt.*;
import java.util.*;

/**
 * Dark theme for window applications
 */
public class DarkTheme extends OceanTheme {

    public static final Color BACKGROUND = new Color(47, 47, 47),
            GRAY_32 = new Color(32, 32, 32),
            GRAY_43 = new Color(43, 43, 43),
            GRAY_64 = new Color(64, 64, 64),
            GRAY_85 = new Color(85, 85, 85),
            GRAY_96 = new Color(96, 96, 96),
            GRAY_160 = new Color(160, 160, 160),
            GRAY_192 = new Color(192, 192, 192),
            GRAY_224 = new Color(224, 224, 224),
            DK_BLUE = new Color(33, 66, 131),
            RED_160 = new Color(255, 160, 160),
            WHITE = Color.WHITE;

    private static final Map<Color, Color> COLOR_MAP = new HashMap<>();
    static {
        COLOR_MAP.put(new Color(99, 130, 191), GRAY_43);
        COLOR_MAP.put(new Color(200, 221, 242), GRAY_64);
        COLOR_MAP.put(new Color(238, 238, 238), GRAY_43);
        COLOR_MAP.put(new Color(122, 138, 153), GRAY_43);
        COLOR_MAP.put(new Color(163, 184, 204), GRAY_85);
        COLOR_MAP.put(Color.WHITE, GRAY_43);
    }

    private static final ColorUIResource PRIMARY1 = new ColorUIResource(BACKGROUND);
    private static final ColorUIResource PRIMARY2 = new ColorUIResource(GRAY_64);
    private static final ColorUIResource PRIMARY3 = new ColorUIResource(GRAY_85);
    private static final ColorUIResource SECONDARY1 = new ColorUIResource(GRAY_64);
    private static final ColorUIResource SECONDARY2 = new ColorUIResource(GRAY_96);
    private static final ColorUIResource SECONDARY3 = new ColorUIResource(Color.GRAY);
    private static final ColorUIResource THEME_WHITE = new ColorUIResource(GRAY_32);
    private static final ColorUIResource THEME_BLACK = new ColorUIResource(GRAY_160);

    private static final String[] FONT_DEFAULT_CHAIN = {"Ubuntu", "sans-serif"};
    private static final int[] FONT_DEFAULT_SIZES = {14, 12};
    public static final String FONT_DEFAULT_NAME;
    public static final int FONT_DEFAULT_SIZE;
    static {
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Set<String> fonts = new HashSet<>(Arrays.asList(g.getAvailableFontFamilyNames()));
        String defaultName = "Default";
        int fontSize = 12;
        for (int i = 0; i < FONT_DEFAULT_CHAIN.length; i++) {
            String font = FONT_DEFAULT_CHAIN[i];
            if (fonts.contains(font)) {
                defaultName = font;
                fontSize = FONT_DEFAULT_SIZES[i];
            }
        }
        FONT_DEFAULT_NAME = defaultName;
        FONT_DEFAULT_SIZE = fontSize;
    }

    public static final Font FONT_DEFAULT = new Font(FONT_DEFAULT_NAME, Font.PLAIN, FONT_DEFAULT_SIZE);
    private static final FontUIResource FONT_DEFAULT_PLAIN = new FontUIResource(FONT_DEFAULT_NAME, Font.PLAIN, FONT_DEFAULT_SIZE);
    private static final FontUIResource FONT_DEFAULT_BOLD = new FontUIResource(FONT_DEFAULT_NAME, Font.BOLD, FONT_DEFAULT_SIZE);

    private static final Set<String> modified = new HashSet<>();

    @Override
    protected ColorUIResource getPrimary1() {
        return PRIMARY1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return PRIMARY2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return PRIMARY3;
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return SECONDARY1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return SECONDARY2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return SECONDARY3;
    }

    @Override
    protected ColorUIResource getWhite() { return THEME_WHITE; }

    @Override
    protected ColorUIResource getBlack() { return THEME_BLACK; }

    @Override
    public ColorUIResource getControl() {
        // This is the default background loading color
        return THEME_WHITE;
    }

    @Override
    public FontUIResource getControlTextFont() {
        return FONT_DEFAULT_BOLD;
    }

    @Override
    public FontUIResource getSystemTextFont() {
        return FONT_DEFAULT_PLAIN;
    }

    @Override
    public FontUIResource getUserTextFont() {
        return FONT_DEFAULT_PLAIN;
    }

    @Override
    public FontUIResource getMenuTextFont() {
        return FONT_DEFAULT_BOLD;
    }

    @Override
    public FontUIResource getWindowTitleFont() {
        return FONT_DEFAULT_BOLD;
    }

    @Override
    public FontUIResource getSubTextFont() {
        return FONT_DEFAULT_PLAIN;
    }

    public static void apply() {
        MetalLookAndFeel.setCurrentTheme(new DarkTheme());

        UIDefaults defs = UIManager.getLookAndFeelDefaults();
        for (Map.Entry<Object, Object> e : defs.entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();

            if (!(k instanceof String))
                continue;

            String key = (String) k;
            int lastDot = key.lastIndexOf('.');
            String endKey = lastDot > -1 ? key.substring(lastDot + 1) : "";

            if (endKey.equals("border")) {
                set(key, new FrameBorder());
                continue;
            }

            if (endKey.equals("gradient")) {
                String startKey = key.substring(0, lastDot);
                Color color = GRAY_64;
                if (startKey.equals("CheckBox") || startKey.equals("RadioButton"))
                    color = GRAY_85;
                ArrayList<Object> gradient = new ArrayList<>();
                gradient.add(0.0);
                gradient.add(0.3);
                gradient.add(color);
                gradient.add(color);
                gradient.add(color);
                set(key, gradient);
            }

            if (!(v instanceof Color))
                continue;

            Color c;
            if (endKey.equals("background"))
                c = BACKGROUND;
            else if (endKey.equals("foreground"))
                c = GRAY_224;
            else if (endKey.equals("selectionBackground"))
                c = DK_BLUE;
            else if (endKey.equals("selectionForeground"))
                c = WHITE;
            else if (endKey.equals("caretForeground"))
                c = WHITE;
            else if (endKey.equals("focus"))
                c = GRAY_85;
            else if (endKey.equals("buttonHighlight") || endKey.equals("buttonShadow") || endKey.equals("buttonDarkShadow"))
                c = GRAY_85;
            else if (key.contains("accelerator"))
                c = GRAY_192;
            else if (key.endsWith("Text"))
                c = GRAY_224;
            else {
                // Color replacement fallback
                Color rep = COLOR_MAP.get((Color) v);
                if (rep != null)
                    set(key, rep);
                continue;
            }

            set(key, c);
        }

        // Backgrounds
        set("Button.background", GRAY_43);
        set("TextArea.background", GRAY_43);
        set("TextField.background", GRAY_43);
        set("List.background", GRAY_43);
        set("ComboBox.background", GRAY_43);
        set("Button.select", GRAY_64);

        // Test
        set("OptionPane.messageForeground", GRAY_224);

        // Progress bar
        set("ProgressBar.selectionBackground", WHITE);
        set("ProgressBar.selectionForeground", DK_BLUE);

        // Scroll bars
        set("ScrollBar.thumbHighlight", GRAY_85);
        set("ScrollBar.thumbShadow", GRAY_85);
        set("ScrollBar.highlight", GRAY_85);
        set("ScrollBar.shadow", GRAY_85);
        set("ScrollBar.thumb", GRAY_85);
        set("ScrollBar.darkShadow", GRAY_85);
        set("ScrollBar.thumbDarkShadow", GRAY_85);
        set("ScrollBar.track", GRAY_85);
        set("scrollbar", GRAY_85);

        // Sliders
        set("Slider.highlight", GRAY_85);
        set("Slider.altTrackColor", GRAY_85);
        set("Slider.shadow", GRAY_85);
        set("Slider.darkShadow", GRAY_85);
        set("Slider.thumb", GRAY_85);

        // Spinner
        set("Spinner.background", GRAY_43);

        // Find shit
        /*for (Map.Entry<Object, Object> e : defs.entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();

            if (v instanceof Color) {
                Color c = (Color) v;
                if (c.getRed() == 122 && c.getGreen() == 138 && c.getBlue() == 153)
                    System.out.println(k + "    =    " + v);
            }

            if (k instanceof String) {
                String key = (String) k;
                //if (modified.contains(key))
                //    continue;
                if (key.contains("Spinner"))
                    System.out.println(key + "    =    " + v);
            }
        }*/
    }

    private static void set(String key, Object value) {
        UIManager.put(key, value);
        modified.add(key);
    }

    private static final ColorUIResource[] TEST_COLORS = new ColorUIResource[] {
            new ColorUIResource(255, 0, 0),
            new ColorUIResource(0, 255, 0),
            new ColorUIResource(0 ,0, 255),
            new ColorUIResource(255, 255, 0),
            new ColorUIResource(0, 255, 255),
            new ColorUIResource(255, 0, 255)
    };

    private static ColorUIResource getRandomTestColor() {
        ColorUIResource random = TEST_COLORS[(int) (Math.random() * TEST_COLORS.length)];
        StackTraceElement el = new Throwable().getStackTrace()[1];
        System.out.println(el.getMethodName() + " -> " + random);
        return random;
    }
}
