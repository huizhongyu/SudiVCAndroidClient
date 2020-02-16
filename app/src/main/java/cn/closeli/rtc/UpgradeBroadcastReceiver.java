package cn.closeli.rtc;

import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;

import cn.closeli.rtc.utils.L;

public class UpgradeBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent) {
        //判断是否下载完成的广播
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            L.d("download complete ?");
            long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            SharedPreferences usericonpreferencess = context.getSharedPreferences("sudi", Context.MODE_PRIVATE);
            long refernece = usericonpreferencess.getLong("refernece", 0);
            L.d("myDwonloadID:%1$s", myDwonloadID);
            L.d("reference:%1$s", refernece);
            if (refernece != myDwonloadID) {
                return;
            }

            DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadFileUri = dManager.getUriForDownloadedFile(myDwonloadID);
            installAPK(context, downloadFileUri);
        }
    }
    private void installAPK(Context context,Uri apk ) {
        L.d("Build.VERSION.SDK_INT:%1$s", Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT < 23) {
            Intent intents = new Intent();
            intents.setAction("android.intent.action.VIEW");
            intents.addCategory("android.intent.category.DEFAULT");
            intents.setType("application/vnd.android.package-archive");
            intents.setData(apk);
            intents.setDataAndType(apk, "application/vnd.android.package-archive");
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intents);
        } else {
            File file = queryDownloadedApk(context);
            if (file != null && file.exists()) {
                openFile(file, context);
            }

        }
    }

    /**
     * 通过downLoadId查询下载的apk，解决6.0以后安装的问题
     * @param context
     * @return
     */
    @Nullable
    public static File queryDownloadedApk(Context context) {
        File targetApkFile = null;
        DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        SharedPreferences usericonpreferencess = context.getSharedPreferences("sudi", Context.MODE_PRIVATE);
        long downloadId =  usericonpreferencess.getLong("refernece",-1);
        L.d("downloadId:%1$s", downloadId);
        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cur = downloader.query(query);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    if (!TextUtils.isEmpty(uriString)) {
                        targetApkFile = new File(Uri.parse(uriString).getPath());
                    }
                }
                cur.close();
            }
        }
        return targetApkFile;

    }

    private void openFile(File file, Context context) {
        L.d("file:%1$s", file.getName());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri photoURI;
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            photoURI = FileProvider.getUriForFile(context, "cn.closeli.rtc.fileprovider", file);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            photoURI = Uri.fromFile(file);
        }
        intent.setDataAndType(photoURI, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
