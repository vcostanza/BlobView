package software.blob.ui.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;

/**
 * Android-like log class
 */
public class Log {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String className = Log.class.getName();

    public static void d(String tag, String msg, Throwable e) {
        log(tag, msg, e, "DEBUG");
    }

    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    public static void d(String msg, Throwable e) {
        d(findTag(), msg, e);
    }

    public static void d(String msg) {
        d(msg, (Throwable) null);
    }

    public static void w(String tag, String msg, Throwable e) {
        log(tag, msg, e, "WARNING");
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public static void w(String msg, Throwable e) {
        w(findTag(), msg, e);
    }

    public static void w(String msg) {
        w(msg, (Throwable) null);
    }

    public static void e(String tag, String msg, Throwable e) {
        log(tag, msg, e, "ERROR");
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    public static void e(String msg, Throwable e) {
        e(findTag(), msg, e);
    }

    public static void e(String msg) {
        e(msg, (Throwable) null);
    }

    private static void log(String tag, String msg, Throwable e, String type) {
        PrintStream stream = type.equals("ERROR") ? System.err : System.out;
        stream.println(sdf.format(System.currentTimeMillis()) + " / " + tag + " [" + type.charAt(0) + "]: " + msg);
        if(e != null)
            e.printStackTrace(stream);
    }

    private static String findTag() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        for (StackTraceElement el : stack) {
            if (!el.getClassName().equals(className)) {
                String fName = el.getFileName();
                int lastDot = fName.lastIndexOf('.');
                if (lastDot != -1)
                    fName = fName.substring(0, lastDot);
                return fName + ":" + el.getLineNumber();
            }
        }
        return "<Unknown>";
    }
}
