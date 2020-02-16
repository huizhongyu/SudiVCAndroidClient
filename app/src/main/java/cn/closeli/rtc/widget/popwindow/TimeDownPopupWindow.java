package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.closeli.rtc.R;
import cn.closeli.rtc.widget.BasePopupWindow;

public class TimeDownPopupWindow extends BasePopupWindow {
    private ConstraintLayout constaint_root;
    private TextView tv_time_title;
    private TextView tv_time_down;
    private ImageView iv_close;
    private String strTitle;
    private CountDownTimer countDownTimer;
    public TimeDownPopupWindow(Context context) {
        super(context);
    }

    @Override
    public int thisLayout() {
        return R.layout.popup_timedown;
    }

    @Override
    public void doInitView() {
        constaint_root = fv(R.id.constaint_root);
        tv_time_title = fv(R.id.tv_time_title);
        tv_time_down = fv(R.id.tv_time_down);
        iv_close = fv(R.id.iv_close);
    }

    @Override
    public void doInitData() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        iv_close.setOnClickListener(v-> {
            dismiss();
        });
    }

    public void setBackground(int resId) {
        constaint_root.setBackgroundResource(resId);
    }

    public void setTimeTitle(String strTitle) {
        this.strTitle = strTitle;
        tv_time_title.setText(strTitle);
    }

    public void startTimeDown(int time) {
        countDownTimer = new CountDownTimer(time*60*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String m = formatTime(millisUntilFinished/1000/60);
                String s = formatTime(millisUntilFinished/1000%60);
                tv_time_down.setText(m+":"+s);
            }

            @Override
            public void onFinish() {
                dismiss();
            }
        };
        countDownTimer.start();
    }

    @Override
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        super.setOnDismissListener(onDismissListener);
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer=null;
        }
    }

    private String formatTime(long time) {
        if (time<10) {
            return "0"+time;
        }
        return String.valueOf(time);
    }

    public void showAtRight(View view) {
            //获取需要在其上方显示的控件的位置信息
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            //在控件上方显示
            showAtLocation(view, Gravity.NO_GRAVITY,
                    location[0] + popupWidth, (location[1]  )+popupHeight/2 );


    }
}
