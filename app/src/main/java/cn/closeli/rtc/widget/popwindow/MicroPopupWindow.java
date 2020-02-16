package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.sdk.WebRTCManager;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.widget.BasePopupWindow;

//麦克风设置popup
public class MicroPopupWindow extends BasePopupWindow {
    private int choosePosition = Constract.AUDIO_STAND;
    private LinearLayout ll_first;
    private LinearLayout ll_second;
    private LinearLayout ll_third;
    private ImageView iv_switch_stand;
    private ImageView iv_switch_max;
    private ImageView iv_switch_min;

    public MicroPopupWindow(Context context, int choosePosition) {
        super(context);
        this.choosePosition = choosePosition;
        changeChecked(choosePosition);
    }

    @Override
    public int thisLayout() {
        return R.layout.popup_micro;
    }

    @Override
    public void doInitView() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        iv_switch_stand = fv(R.id.iv_switch_stand);
        iv_switch_max = fv(R.id.iv_switch_max);
        iv_switch_min = fv(R.id.iv_switch_min);

        ll_first = fv(R.id.ll_first);
        ll_second = fv(R.id.ll_second);
        ll_third = fv(R.id.ll_third);


    }

    @Override
    public void doInitData() {
        ll_first.setOnClickListener(v -> {
            changeChecked(Constract.AUDIO_STAND);
            choosePosition = Constract.AUDIO_STAND;
            WebRTCManager.get().setAudioValue(3);
            SPEditor.instance().setInt("audio_set",choosePosition);
            dismiss();
        });

        ll_second.setOnClickListener(v -> {
            changeChecked(Constract.AUDIO_MAX);
            choosePosition = Constract.AUDIO_MAX;
            WebRTCManager.get().setAudioValue(7);
            SPEditor.instance().setInt("audio_set",choosePosition);
            dismiss();
        });

        ll_third.setOnClickListener(v -> {
            changeChecked(Constract.AUDIO_MIN);
            choosePosition = Constract.AUDIO_MIN;
            WebRTCManager.get().setAudioValue(0);
            SPEditor.instance().setInt("audio_set",choosePosition);
            dismiss();
        });

    }

    //选择选中的item
    public void changeChecked(int index) {
//        if (index != choosePosition) {
            switch (index) {
                case Constract.AUDIO_STAND:
                    iv_switch_stand.setImageResource(R.drawable.icon_circle_checked);
                    iv_switch_max.setImageResource(R.drawable.icon_circle_unchecked);
                    iv_switch_min.setImageResource(R.drawable.icon_circle_unchecked);
                    break;
                case Constract.AUDIO_MAX:
                    iv_switch_stand.setImageResource(R.drawable.icon_circle_unchecked);
                    iv_switch_max.setImageResource(R.drawable.icon_circle_checked);
                    iv_switch_min.setImageResource(R.drawable.icon_circle_unchecked);
                    break;

                case Constract.AUDIO_MIN:
                    iv_switch_stand.setImageResource(R.drawable.icon_circle_unchecked);
                    iv_switch_max.setImageResource(R.drawable.icon_circle_unchecked);
                    iv_switch_min.setImageResource(R.drawable.icon_circle_checked);
                    break;

            }
        }
//    }
}
