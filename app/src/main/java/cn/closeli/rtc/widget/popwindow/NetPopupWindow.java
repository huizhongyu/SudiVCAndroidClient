package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.view.ViewGroup;

import cn.closeli.rtc.R;
import cn.closeli.rtc.widget.BasePopupWindow;

public class NetPopupWindow extends BasePopupWindow {
    public NetPopupWindow(Context context) {
        super(context);
    }

    @Override
    public int thisLayout() {
        return R.layout.popup_net_change;
    }

    @Override
    public void doInitView() {
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void doInitData() {

    }
}
