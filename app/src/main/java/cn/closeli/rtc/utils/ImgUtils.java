package cn.closeli.rtc.utils;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import cn.closeli.rtc.App;
import cn.closeli.rtc.R;

public class ImgUtils {
    public static void set(String url, ImageView view) {
        Glide.with(App.getInstance().getApplicationContext()).load(url).into(view);
    }

    public static void set(Drawable drawable, ImageView view){
        Glide.with(App.getInstance().getApplicationContext()).load(drawable).into(view);
    }

    public static void setCircle(String url, ImageView view) {
        Glide.with(App.getInstance().getApplicationContext())
                .load(url)
                .placeholder(R.drawable.setup_maillist_icon_company)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(view);
    }

}
