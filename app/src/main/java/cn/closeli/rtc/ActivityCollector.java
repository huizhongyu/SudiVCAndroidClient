package cn.closeli.rtc;

import android.support.v7.app.AppCompatActivity;

public class ActivityCollector {
    public static java.util.List<AppCompatActivity> activities = new java.util.ArrayList<>();

    public static void addActivity(AppCompatActivity activity) {
        activities.add(activity);
    }

    public static void removeActivity(AppCompatActivity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (AppCompatActivity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
