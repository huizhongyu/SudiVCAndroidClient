package cn.closeli.rtc.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileStreamFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cn.closeli.rtc.App;
import cn.closeli.rtc.widget.UCToast;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 日志提取类
 */
public class LogExtraction {

    private final String TAG = "LogExtraction";
    private static final String LOG_PATH = "mtklog";
    private static final String ACTION_USB_PERMISSION = "cn.closeli.rtc.USB_PERMISSION";
    private static UsbManager usbManager;
    private static UsbMassStorageDevice[] massStorageDevices;
    private static Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 提取日志
     *
     * @param context         .
     * @param onExtractResult 回调
     * @return
     */
    public static void extractLog2UDisk(Context context, OnExtractResult onExtractResult) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (!checkUDisk(context)) {
            onExtractResult.onError("u盘未找到");
            return;
        }
        boolean hasPermission = obtainPermission(context);
        if (hasPermission && !isLogEmpty(context)) {
            Completable.create(e -> {
                File file = zipLocalLog(onExtractResult);
                if (file == null) {
                    e.onError(new Throwable("没有日志文件"));
                    return;
                }
                boolean ret = copy2UDisk(file, onExtractResult);
                if (ret) {
                    e.onComplete();
                } else {
                    e.onError(new Throwable("拷贝异常,请插入U盘重试"));
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            handler.postDelayed(onExtractResult::onSuccess, 3000);
                        }

                        @Override
                        public void onError(Throwable e) {
                            onExtractResult.onError(e.getMessage());
                        }
                    });
        } else {
            onExtractResult.onError("没有获得权限");
        }
    }

    /**
     * 将压缩后的日志文件写入u盘
     *
     * @param file            压缩后的文件
     * @param onExtractResult .
     */
    private static boolean copy2UDisk(File file, OnExtractResult onExtractResult) {
        UsbMassStorageDevice massStorageDevice = massStorageDevices[0];
        try {
            massStorageDevice.init();
            FileSystem fileSystem = massStorageDevice.getPartitions().get(0).getFileSystem();
            UsbFile rootDirectory = fileSystem.getRootDirectory();
            UsbFile devicelog = rootDirectory.search("devicelog");
            if (devicelog == null) {
                devicelog = rootDirectory.createDirectory("devicelog");
            }
            UsbFile search = devicelog.search("log.zip");
            if (search != null) {
                search.delete();
            }
            search = devicelog.createFile("log.zip");
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = UsbFileStreamFactory.createBufferedOutputStream(search, fileSystem);
            byte[] bytes = new byte[1337];
            int count;
            long total = 0;
            int size = (int) file.length();
            search.setLength(size);
            while ((count = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, count);
                if (size > 0) {
                    total += count;
                    int progress = (int) total;
                    int finalProgress = (int) (progress * 1.0 / size * 50 + 50);
                    Log.e("finalProgress", finalProgress + "");
                    App.post(() -> onExtractResult.onProgress(finalProgress));
                }
            }
            outputStream.close();
            inputStream.close();
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 是否有日志文件
     *
     * @param context
     * @return
     */
    private static boolean isLogEmpty(Context context) {
        String path = Environment.getExternalStorageDirectory().getPath();
        File logFile = new File(path, LOG_PATH);
        if (!logFile.exists() || !logFile.isDirectory()) {
            UCToast.showNormal(context, "日志文件不存在");
            return true;
        }
        return false;
    }

    /**
     * 检查并获取权限
     *
     * @param context
     * @return
     */
    private static boolean obtainPermission(Context context) {
        UsbDevice usbDevice = massStorageDevices[0].getUsbDevice();
        if (usbManager.hasPermission(usbDevice)) {
            return true;
        } else {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                    ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(massStorageDevices[0].getUsbDevice(), permissionIntent);
            return false;
        }
    }

    /**
     * 检查U盘是否插入
     *
     * @param context
     * @return
     */
    private static boolean checkUDisk(Context context) {
        massStorageDevices = UsbMassStorageDevice.getMassStorageDevices(context);
        return massStorageDevices.length != 0;
    }

    /**
     * 打包本地日志
     *
     * @param onExtractResult
     * @return
     */
    private static File zipLocalLog(OnExtractResult onExtractResult) {
        String path = Environment.getExternalStorageDirectory().getPath();
        File logDir = new File(path, LOG_PATH);
        File zipFile = new File(path, "temp.zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zipFiles(logDir, zos, onExtractResult);
            return zipFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 压缩文件
     *
     * @param logDir
     * @param zos
     * @param onExtractResult
     */
    private static void zipFiles(File logDir, ZipOutputStream zos, OnExtractResult onExtractResult) {
        File[] files = logDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                int finalI = i;
                int progress = (int) ((finalI + 1) * 1.0 / files.length * 50);
                App.post(() -> onExtractResult.onProgress(progress));
                ZipEntry entry = new ZipEntry(file.getName());
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(entry);
                    int len;
                    byte[] buffer = new byte[4096];
                    while ((len = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface OnExtractResult {
        void onSuccess();

        void onError(String msg);

        void onProgress(int progress);
    }

}
