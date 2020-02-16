package cn.closeli.rtc;

import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.closeli.rtc.service.AutoInstallService;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.SystemUtil;
import cn.closeli.rtc.utils.UtilsKt;
import cn.closeli.rtc.utils.ext.Act1;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UpgradeAppUtil {
    private static String[] paths = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/", "/su/bin/"};

    public static boolean isUpgrade(String versionName, String upgradeType) {
        L.d("cur versionCode:%1$s", SystemUtil.getVersionName(App.getInstance().getApplicationContext()));
        L.d("upgrade dst versionCode:%1$s", versionName);
        int compareRes = compareVersion(versionName, SystemUtil.getVersionName(App.getInstance().getApplicationContext()));
        if ((upgradeType.equalsIgnoreCase("forceUpgrade") && compareRes != 0) ||
                (upgradeType.equalsIgnoreCase("generalUpgrade") && compareRes > 0)) {
            L.d("upgradeType:%1$s", upgradeType);
            return true;
        }

        return false;
    }

    public static int compareVersion(String s1, String s2) {
        String[] s1Split = s1.split("\\.", -1);
        String[] s2Split = s2.split("\\.", -1);
        int len1 = s1Split.length;
        int len2 = s2Split.length;
        int lim = Math.min(len1, len2);
        int i = 0;
        while (i < lim) {
            int c1 = "".equals(s1Split[i]) ? 0 : Integer.parseInt(s1Split[i]);
            int c2 = "".equals(s2Split[i]) ? 0 : Integer.parseInt(s2Split[i]);
            if (c1 != c2) {
                return c1 - c2;
            }
            i++;
        }
        return len1 - len2;
    }

    /**
     * 检查无障碍服务
     *
     * @return
     */
    private static boolean checkAccessibility(Context cxt) {

        try {
            int enable = Settings.Secure.getInt(cxt.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
            if (enable != 1) {
                return false;
            }
            String services = Settings.Secure.getString(cxt.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (!TextUtils.isEmpty(services)) {
                TextUtils.SimpleStringSplitter split = new TextUtils.SimpleStringSplitter(':');
                split.setString(services);
                // 遍历所有已开启的辅助服务名
                while (split.hasNext()) {
                    if (split.next().equalsIgnoreCase(cxt.getPackageName() + "/" + AutoInstallService.class.getName())) {
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 静默安装
     */
    private static void slientInstall(Context context, String downloadUrl, UpgradCallback callback) {
        downLoadApk(downloadUrl, callback, file -> UtilsKt.install(context, context.getPackageName(), file));
    }

    /**
     * 是否root
     *
     * @return
     */
    private static boolean isRoot() {
        try {
            for (String path : paths) {
                File file = new File(path + "su");
                if (file.exists() && file.canExecute()) {
                    return true;
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return false;
    }

    /**
     * 更新升级
     * @param context
     * @param downloadUrl
     * @param callback
     * @return
     */
    public static boolean upgrade(Context context, String downloadUrl, UpgradCallback callback) {
        String packageName = context.getPackageName();
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (isRoot()) {
            if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                //静默安装
                slientInstall(context, downloadUrl, callback);
            } else if (checkAccessibility(context)) {
                installByAccessibility(context,downloadUrl,callback);
            } else {
                normalInstall(downloadUrl);
            }
        } else {
            if (checkAccessibility(context)) {
                installByAccessibility(context,downloadUrl,callback);
            } else {
                normalInstall(downloadUrl);
            }
        }
        return true;
    }

    private static void downLoadApk(String downloadUrl, UpgradCallback callback, Act1<String> act1) {

        String fileName = downloadUrl.split("/")[downloadUrl.split("/").length - 1];
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request
                .Builder()
                .get()
                .url(downloadUrl)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (callback != null) {
                    callback.onError("下载失败");
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                File dir = new File("/data/local/tmp");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                ResponseBody body;
                if ((body = response.body()) != null) {
                    InputStream is = body.byteStream();
                    File apkFile = new File(dir, fileName);
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int len;
                    byte[] buffer = new byte[2048];
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.flush();
                    fos.close();
                    act1.run(fileName);
                } else {
                    callback.onError("下载失败");
                }
            }
        });
    }

    /**
     * 通过无障碍服务安装
     */
    private static void installByAccessibility(Context cxt,String downloadURL,UpgradCallback callback) {
        downLoadApk(downloadURL,callback,apkFile -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(cxt, cxt.getPackageName() + ".fileProvider", new File(apkFile));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(new File(apkFile));
                }
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                cxt.startActivity(intent);
            } catch (Throwable e) {
                normalInstall(downloadURL);
            }
        });
    }

    private static void normalInstall(String downloadUrl) {
        String downLoadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/app-debug.apk";
        File file = new File(downLoadPath);
        if (file.exists()) {
            L.d("%1$s file exists", downLoadPath);
            file.delete();
        }

        L.d("downloadUrl %1$s", downloadUrl);
        L.d("downloadPath %1$s", downLoadPath);
        DownloadManager downloadManager;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        //设置在什么网络情况下进行下载
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        request.setVisibleInDownloadsUi(true);
        request.setTitle("云视讯");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setAllowedOverRoaming(true);
        request.setMimeType("application/vnd.android.package-archive");
        request.setDestinationUri(Uri.parse("file://" + downLoadPath));
        downloadManager = (DownloadManager) App.getInstance().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        long enqueue = downloadManager.enqueue(request);
        SharedPreferences usericonpreferencess = App.getInstance().getApplicationContext().getSharedPreferences("sudi", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = usericonpreferencess.edit();
        edit.putLong("refernece", enqueue);
        edit.apply();
    }

    public interface UpgradCallback {
        void onError(String msg);

        void onSuccess();
    }

}
