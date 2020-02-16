package cn.closeli.rtc.utils

import android.content.Context
import android.support.v7.app.AppCompatDialog
import android.view.Window

/**
 * 加载框
 */
class LoadingDialog(context: Context) : AppCompatDialog(context) {

    init {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

}