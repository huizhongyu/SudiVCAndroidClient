package cn.closeli.rtc.utils.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cn.closeli.rtc.App;

public class NetworkUtils {
    @SuppressLint("MissingPermission")
    public static NetType getNetType() {
        ConnectivityManager manager = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null)  {
            return NetType.NULL;
        }
        //获取当前网络连接状况
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return NetType.NULL;
        }

        int type = networkInfo.getType();
        if (type == ConnectivityManager.TYPE_MOBILE) {
            if (networkInfo.getExtraInfo().equalsIgnoreCase("cmnet")) {
                return NetType.CMNET;
            } else {
                return NetType.CMWAP;
            }
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            return NetType.WIFI;
        } else if (type == ConnectivityManager.TYPE_ETHERNET) {
            return NetType.ETHERNET;
        }

        return NetType.NULL;
    }

    //网络是否可用
    @SuppressLint("MissingPermission")
    public static boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;

        NetworkInfo[] arraysOfNetwork = manager.getAllNetworkInfo();
        for (NetworkInfo net: arraysOfNetwork) {
            if (net.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }


}
