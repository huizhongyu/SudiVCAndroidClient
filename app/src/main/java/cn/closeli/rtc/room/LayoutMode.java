package cn.closeli.rtc.room;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Administrator
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({LayoutMode.MODEL_0, LayoutMode.MODEL_4, LayoutMode.MODEL_6, LayoutMode.MODEL_9, LayoutMode.MODEL_12})
public @interface LayoutMode {
    /**
     * 默认布局
     */
    int MODEL_0 = 0;
    int MODEL_4 = 4;
    int MODEL_6 = 6;
    int MODEL_9 = 9;
    int MODEL_12 = 12;

}
