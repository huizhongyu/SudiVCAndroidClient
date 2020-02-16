package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.closeli.rtc.R;
import cn.closeli.rtc.widget.BasePopupWindow;

public class DevicePopupWindow extends BasePopupWindow {
    private TextView tv_loginout;
    private TextView tv_shutdown;
    private TextView tv_sleep;
    private TextView tv_reboot;
    private TextView tv_cancel;
    private OnPopupItemListener onPopupItemListener;
    private boolean isShowLogout = false;

    public DevicePopupWindow(Context context,boolean isShowLogout) {
        super(context);
        this.isShowLogout = isShowLogout;
        isShowLogout();
    }

    public void isShowLogout() {
        tv_loginout.setVisibility(isShowLogout? View.VISIBLE:View.GONE);
    }

    @Override
    public int thisLayout() {
        return R.layout.popup_device;
    }

    @Override
    public void doInitView() {
        tv_loginout = fv(R.id.tv_loginout);
        tv_shutdown = fv(R.id.tv_shutdown);
        tv_sleep = fv(R.id.tv_sleep);
        tv_reboot = fv(R.id.tv_reboot);
        tv_cancel = fv(R.id.tv_cancel);

    }



    @Override
    public void doInitData() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        tv_cancel.setOnClickListener(v-> {
            dismiss();
        });

        tv_loginout.setOnClickListener(v-> {
            if (onPopupItemListener != null) {
                onPopupItemListener.onLoginOut();
            }
        });

        tv_shutdown.setOnClickListener(v-> {
            dismiss();
            Intent intent_shutdown = new Intent();
            intent_shutdown.setAction("com.vhd.system.SHUTDOWN");
            mContext.sendBroadcast(intent_shutdown);
        });

        tv_sleep.setOnClickListener(v-> {
            dismiss();
            Intent intent_sleep = new Intent();
            intent_sleep.setAction("com.vhd.system.SLEEP");
            mContext.sendBroadcast(intent_sleep);
        });

        tv_reboot.setOnClickListener(v-> {
            dismiss();
            Intent intent_reboot = new Intent();
            intent_reboot.setAction("com.vhd.system.REBOOT");
            mContext.sendBroadcast(intent_reboot);
        });
    }

    public OnPopupItemListener getOnPopupItemListener() {
        return onPopupItemListener;
    }

    public void setOnPopupItemListener(OnPopupItemListener onPopupItemListener) {
        this.onPopupItemListener = onPopupItemListener;
    }

    public interface OnPopupItemListener {
        void onLoginOut();
    }
}
