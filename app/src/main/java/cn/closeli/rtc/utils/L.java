package cn.closeli.rtc.utils;

import android.util.Log;

public class L {
    public static final String TAG = "SudiTech";
    private static boolean enable = true;

    public static void d(String format, Object... args) {
        print(Log.DEBUG, format, args);
    }

    public static void d(String log) {
        print(Log.DEBUG, log, (Object) null);
    }

    public static void e(String msg) {
        print(Log.ERROR, msg, (Object) null);
    }

    public static void e(String format, Object... args) {
        print(Log.ERROR, format, args);
    }

    private static void print(int level, String format, Object... args) {
        if (!enable) {
            return;
        }
        String msg = args == null ? format : String.format(format, args);
        switch (level) {
            case Log.INFO:
                Log.i(TAG, msg);
                break;
            case Log.DEBUG:
                Log.d(TAG, msg);
                break;
            case Log.WARN:
                Log.w(TAG, msg);
                break;
            case Log.ERROR:
                Log.e(TAG, msg);
                break;
            default:
        }
    }

    /**
     * 是否打印日志
     *
     * @param enable true 打印 false 不打印
     */
    public static void setEnable(boolean enable) {
        L.enable = enable;
    }
}
