package cn.closeli.rtc.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import static cn.closeli.rtc.constract.Constract.SFU_SHARING;

public class StringUtils {
    /**
     * model : Spanned.SPAN_INCLUSIVE_INCLUSIVE(包含起始位置)
     *
     * @param string
     * @param start
     * @param end
     * @param color
     * @return
     */
    public static SpannableString getSpannableString(String string, int start, int end, int color) {
        SpannableString spString = new SpannableString(string);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);
        spString.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return spString;
    }

    /**
     * 判断当前index 是否为 1
     */
    public static boolean isIndexEqual1(String str, int index) {
        str = Integer.toBinaryString(Integer.valueOf(str));
        int length = str.length();
        if (length - index > 0) {
            if (str.charAt(length - index - 1) == '1') {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前index 是否为 1
     */
    public static String streamIdToConnect(String st,String streamType) {
        if(SFU_SHARING.equals(streamType)){
            String[] strings = st.split("_");
            if (strings != null && strings.length > 0) {
                return strings[0];
            }
        }
        return st;
    }
}
