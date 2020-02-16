package cn.closeli.rtc.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import cn.closeli.rtc.App;

public class SystemUtil {
    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return 语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机序列号
     *
     * @return 手机厂商
     */
    public static String getSerial() {
        String serial = null;
        try {

            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {

            e.printStackTrace();

        }

        return serial;

    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return 手机IMEI
     */

    @SuppressLint("MissingPermission")
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        }
        return null;
    }

    /**
     * 获取版本名称
     */
    public static String getVersionName(Context context) {
        String versionName = "1.0";
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
            if (TextUtils.isEmpty(versionName)) {
                versionName = "";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取版本号
     * 注：该函数在有些Android版本会抛异常，提示NoSuchMethodError
     */
    public static long getVersionCode() {
        long versionCode = 1;
        PackageManager packageManager = App.getInstance().getApplicationContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(App.getInstance().getApplicationContext().getPackageName(), 0);
            versionCode = packageInfo.getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        }
        return versionCode;
    }

//    public static String getMacAddress() {
//        String macAddress = null;
//        WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());
//        if (!wifiManager.isWifiEnabled()) {
//            //必须先打开，才能获取到MAC地址
//            wifiManager.setWifiEnabled(true);
//            if (null != info) {
//                macAddress = info.getMacAddress();
//            }
//            wifiManager.setWifiEnabled(false);
//        }
//        if (null != info && macAddress == null) {
//            macAddress = info.getMacAddress();
//        }
//        return macAddress;
//    }

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    public static String getMacAddress() {
        String mac = SPEditor.instance().getString(SPEditor.MAC_ADDR, "");
        if (!mac.isEmpty()) {
            return mac;
        }
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    public static Boolean isC9Z() {
        return SystemUtil.getSystemModel().equals("C9Z");
    }

    public static boolean isHisiNormal() {
        try {
            Process process = Runtime.getRuntime().exec("getprop vhd.hi.status");
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            Log.i("SystemUtil", "hisiStatus: result = " + result);
//            while ((result = br.readLine()) != null) Log.i("SystemUtil", "hisiStatus: result = " + result);;
            try {
                int exitValue = process.waitFor();
                Log.i("SystemUtil", "hisiStatus: exitValue = " + exitValue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result != null && result.equals("normal");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isHdmiinConnected() {
        try {
            Process process = Runtime.getRuntime().exec("getprop vhd.hdmin.connect");
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            Log.i("SystemUtil", "hdmin.connect: result = " + result);
//            while ((result = br.readLine()) != null) Log.i("SystemUtil", "hisiStatus: result = " + result);;
            try {
                int exitValue = process.waitFor();
                Log.i("SystemUtil", "hdmin.connect: exitValue = " + exitValue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result != null && result.equals("connect"); // "disconnect"
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getMtkVersion() {
        try {
            Process process = Runtime.getRuntime().exec("getprop ro.mediatek.version.release");
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            Log.i("SystemUtil", "version.release: result = " + result);
//            while ((result = br.readLine()) != null) Log.i("SystemUtil", "version.release: result = " + result);;
            try {
                int exitValue = process.waitFor();
                Log.i("SystemUtil", "version.release: exitValue = " + exitValue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}

