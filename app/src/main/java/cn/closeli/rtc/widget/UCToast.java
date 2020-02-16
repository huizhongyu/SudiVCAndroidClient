package cn.closeli.rtc.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.closeli.rtc.R;

//自定义样式 Toast
public class UCToast {
    private static Toast toast;

    public static void show(Context context, String str) {
        show(context, 0, str);
    }

    public static void show(Context context, SpannableString str) {
        show(context, 0, str);
    }

    public static void show(Context context, int resId, String str) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = new Toast(context);
        View view = View.inflate(context, R.layout.view_toast, null);

        ImageView iv = view.findViewById(R.id.iv_toast_icon);
        TextView tv = view.findViewById(R.id.tv_toast_msg);
        if (resId != 0) {
            iv.setVisibility(View.VISIBLE);
            iv.setImageResource(resId);
        } else {
            iv.setVisibility(View.GONE);
        }
        tv.setText(str);
        toast.setView(view);
        toast.show();
    }

    public static void show(Context context, int resId, SpannableString str) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = new Toast(context);
        View view = View.inflate(context, R.layout.view_toast, null);

        ImageView iv = view.findViewById(R.id.iv_toast_icon);
        TextView tv = view.findViewById(R.id.tv_toast_msg);
        if (resId != 0) {
            iv.setVisibility(View.VISIBLE);
            iv.setImageResource(resId);
        } else {
            iv.setVisibility(View.GONE);
        }
        tv.setText(str);
        toast.setView(view);
        toast.show();
    }

    //发言 toast 指定偏移量
    public static void showCustomToast(Context context, String string, int gravity, int xOffset, int yOffset) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = new Toast(context);
        View view = View.inflate(context, R.layout.view_toast_custom, null);

        TextView tv = view.findViewById(R.id.tv_toast_msg);
        tv.setText(string);
        toast.setView(view);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

    public static void showNormal(@NonNull Context context, @StringRes int strId) {
        showNormal(context,context.getString(strId));
    }

    public static void showNormal(@NonNull Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
