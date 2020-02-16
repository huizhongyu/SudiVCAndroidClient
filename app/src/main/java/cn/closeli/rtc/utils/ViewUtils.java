package cn.closeli.rtc.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by yangfeng01 on 2017/12/8.
 */

public class ViewUtils {

    private static final long INTERVAL = 800;  //2000 -> 600
    private static final long INTERVALLONG = 1500;  //2000 -> 600
    private static long sLastClickTime;

    /**
     * 是否快速点击
     *
     * @return true:快速点击 false:非快速点击
     */
    public static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - sLastClickTime < INTERVAL) {
            return true;
        }
        sLastClickTime = currentTime;
        return false;
    }

    /**
     * 是否快速点击
     *
     * @return true:快速点击 false:非快速点击
     */
    public static boolean isFastClickLong() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - sLastClickTime < INTERVALLONG) {
            return true;
        }
        sLastClickTime = currentTime;
        return false;
    }

    public static boolean isFastClick(long time) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - sLastClickTime < time) {
            return true;
        }
        sLastClickTime = currentTime;
        return false;
    }
}