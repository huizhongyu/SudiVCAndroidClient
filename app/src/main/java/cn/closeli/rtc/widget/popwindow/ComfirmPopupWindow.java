package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.closeli.rtc.R;
import cn.closeli.rtc.widget.BasePopupWindow;

//确认弹窗 popup
public class ComfirmPopupWindow extends BasePopupWindow {
    private Button btn_comfirm;
    private Button btn_cancel;
    private OnButtonClickListener listener;
    private boolean enableAudio;

    public ComfirmPopupWindow(Context context, boolean enableAudio) {
        super(context);
        this.enableAudio = enableAudio;
        setButton();
    }

    @Override
    public int thisLayout() {
        return R.layout.popup_comfirm;
    }

    @Override
    public void doInitView() {
        btn_comfirm = view.findViewById(R.id.btn_comfirm);
        btn_cancel = view.findViewById(R.id.btn_cancel);
    }

    @Override
    public void doInitData() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        btn_comfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComfirmClick(v);
            }
        });

        btn_cancel.setOnClickListener(v -> {
            if (listener != null && enableAudio) {
                listener.onCancelClick(v);
            }
        });
    }

    public void setButton() {
        if (enableAudio) {
            btn_comfirm.setText("关闭");
        } else {
            btn_comfirm.setText("开启");
        }
        btn_cancel.setText("音量");
    }

    public OnButtonClickListener getListener() {
        return listener;
    }

    public void setListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnButtonClickListener {
        void onComfirmClick(View view);

        void onCancelClick(View view);
    }
}
