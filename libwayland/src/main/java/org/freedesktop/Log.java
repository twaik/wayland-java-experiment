package org.freedesktop;

public class Log {
    public static void e(String tag, String text) {
        System.out.println("ERROR/" + tag + ": " + text);
    }
    public static void e(String tag, String text, Throwable tr) {
        System.out.println("ERROR/" + tag + ": " + text);
        tr.printStackTrace();
    }
    public static void i(String tag, String text) {
        System.out.println("INFO/" + tag + ": " + text);
    }
    public static void d(String tag, String text) {
        System.out.println("DEBUG/" + tag + ": " + text);
    }
    public static void w(String tag, String text) {
        System.out.println("WARNING/" + tag + ": " + text);
    }
    public static void v(String tag, String text) {
        System.out.println("VERBOSE/" + tag + ": " + text);
    }
}
