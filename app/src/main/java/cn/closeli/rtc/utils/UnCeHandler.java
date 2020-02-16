package cn.closeli.rtc.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import cn.closeli.rtc.App;
import cn.closeli.rtc.LoginActivity;

import static cn.closeli.rtc.ActivityCollector.finishAll;

/*处理崩溃重叠*/
public class UnCeHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public static final String TAG = "CatchExcep";
    App application;

    public UnCeHandler(App application){
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        DeviceSettingManager.getInstance().endCameraControl();
        if(!handleException(ex) && mDefaultHandler != null){
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }else{
            try{
                Thread.sleep(2000);
            }catch (InterruptedException e){
                Log.e(TAG, "error : ", e);
            }
//            Intent intent = new Intent(application.getApplicationContext(), LoginActivity.class);
//            @SuppressLint("WrongConstant") PendingIntent restartIntent = PendingIntent.getActivity(
//                    application.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
//            //退出程序
//            AlarmManager mgr = (AlarmManager)application.getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
//                    restartIntent); // 1秒钟后重启应用
            exit();
        }
    }
    /**
     * 退出应用
     */
    public static void exit() {
        try {
            ActivityUtils.getInstance().finishAllActivity();
            //杀死该应用程序
            android.os.Process.killProcess(Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.getMessage();
        }
    }
    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则没有处理返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        return true;
    }
}
