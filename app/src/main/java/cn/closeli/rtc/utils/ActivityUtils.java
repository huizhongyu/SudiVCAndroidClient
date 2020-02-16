package cn.closeli.rtc.utils;

import android.app.Activity;

import java.util.Stack;

public class ActivityUtils {

    public Stack<Activity> stack;
    private ActivityUtils() {
    }

    public static final ActivityUtils getInstance() {
        return Inner.inner;
    }

    //添加activity
    public void addActivity(Activity activity){
        if (stack == null) {
            stack = new Stack<>();
        }
        stack.add(activity);
    }

    //获取当前Activity
    public Activity getCurrent(){
        Activity a = stack.lastElement();
        return a;
    }

    public int getStackCount () {
        return stack.size();
    }

    //结束当前Activity
    public void finishCurrnet() {
        Activity a = stack.lastElement();
        if (a != null) {
            a.finish();
            a = null;
        }
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            stack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : stack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        if (stack == null || stack.isEmpty()){
            return;
        }
        for (int i = 0, size = stack.size(); i < size; i++) {
            if (null != stack.get(i)) {
                stack.get(i).finish();
            }
        }
        stack.clear();
    }

    public void finishUnitActivity(Class<?> cls) {
        for (int i = 0, size = stack.size(); i < size; i++) {
            L.d("do this --->>>>"+cls.getSimpleName()+" , "+stack.get(i).getClass().getSimpleName());
            if (!stack.get(i).getClass().getSimpleName().equals(cls.getSimpleName())) {
                stack.get(i).finish();
            } else {
//                break;
            }
        }
    }



    static class Inner {
        public static final ActivityUtils inner = new ActivityUtils();
    }
}
