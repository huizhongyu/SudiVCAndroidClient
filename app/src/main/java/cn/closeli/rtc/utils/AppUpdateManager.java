package cn.closeli.rtc.utils;

import android.annotation.Nullable;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.Locale;

import cn.closeli.rtc.BuildConfig;
import cn.closeli.rtc.widget.UCToast;

/**
 * 应用更新管理器
 */
public class AppUpdateManager {

    private static final String PREFIX_APK = "Sudi";
    private static String DOWNLOAD_ID = "download_id";

    /**
     * 检查更新
     *
     * @param context 上下文
     */
    public static void checkUpdate(Context context) {
        // TODO: 2019/11/25 检查是否有更新
        int newVersionCode = 1;
        int currentVersionCode = BuildConfig.VERSION_CODE;
        if (newVersionCode > currentVersionCode) {
            showUpdateDialog(context, newVersionCode);
        }
    }

    private static void showUpdateDialog(Context context, int newVersionCode) {
        boolean exist = existLocalApk(context, newVersionCode);
        if (exist) {
            long downloadId = SPEditor.instance().getLong(DOWNLOAD_ID);
            installApk(context, downloadId);
        } else {
            downloadApk(context, newVersionCode);
        }
    }

    /**
     * 下载更新的apk
     *
     * @param context        .
     * @param newVersionCode 新的apk版本
     */
    private static void downloadApk(Context context, int newVersionCode) {
        boolean ret = checkDownloadManagerEnable(context);
        if (!ret) {
            UCToast.show(context, "下载器不可用");
            return;
        }
        download(context, newVersionCode);
    }

    private static void download(Context context, int newVersionCode) {
        DownloadReceiver receiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.DOWNLOAD_COMPLETE");
        intentFilter.addAction("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED");
        context.registerReceiver(receiver, intentFilter);

        Uri uri = Uri.parse("");
        DownloadManager.Request req = new DownloadManager.Request(uri);
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, String.format(Locale.getDefault(), "%s_%d.apk", PREFIX_APK, newVersionCode));
        req.setTitle("速递科技云视讯");
        req.setDescription("新版本下载中");
        req.setMimeType("application/vnd.android.package-archive");
        req.allowScanningByMediaScanner();
        req.setVisibleInDownloadsUi(true);
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            long downloadId = manager.enqueue(req);
            SPEditor.instance().setLong("download_id", downloadId);
        } catch (Exception e) {
            UCToast.show(context, "获取下载的文件失败");
        }
    }

    /**
     * 检查下载管理器是否可用
     *
     * @param context .
     * @return true 可用 false不可用
     */
    private static boolean checkDownloadManagerEnable(Context context) {
        String downloadPackageName = "com.android.providers.downloads";
        try {
            int state = context.getPackageManager().getApplicationEnabledSetting(downloadPackageName);
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + downloadPackageName));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 安装下载号的apk
     *
     * @param context    .
     * @param downloadId 下载id
     */
    public static void installApk(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uriForDownloadedFile = downloadManager.getUriForDownloadedFile(downloadId);
        if (uriForDownloadedFile != null) {
            intent.setDataAndType(uriForDownloadedFile, "application/vnd.android.package-archive");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                UCToast.show(context, "自动安装失败");
            }
        }
    }

    /**
     * 本地是已经否存在apk包
     *
     * @param context        上下文
     * @param newVersionCode 版本号
     * @return true 本地已有 false 本地没有，需要下载
     */
    private static boolean existLocalApk(Context context, int newVersionCode) {
        String apkPath = getApkPath(context, newVersionCode);
        if (apkPath == null) {
            return false;
        }
        return new File(apkPath).exists();
    }

    /**
     * 下载的apk的存储路径
     *
     * @param context        .
     * @param newVersionCode 新的版本号
     * @return 路径
     */
    @Nullable
    private static String getApkPath(Context context, int newVersionCode) {
        File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir == null) {
            UCToast.show(context, "下载路径获取失败");
            // TODO: 2019/11/25 设置新的下载路径
            return null;
        }
        return String.format(Locale.getDefault(), "%s/%s_%d.apk", downloadDir.getAbsolutePath(), PREFIX_APK, newVersionCode);
    }

}

class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            AppUpdateManager.installApk(context, downloadId);
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
            //任务取消下载
            UCToast.show(context, "取消下载");
        }
    }
}
