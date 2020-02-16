package cn.closeli.rtc.widget.popwindow;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.EthernetManager;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.widget.BasePopupWindow;

public class NetSettingsPopupWindow extends BasePopupWindow implements View.OnClickListener {
    private Button btn_net;
    private Button btn_wifi;
    private Button btn_cancel;

    public NetSettingsPopupWindow(Context context) {
        super(context);
    }

    @Override
    public int thisLayout() {
        return R.layout.popup_netsettings;
    }

    @Override
    public void doInitView() {
        btn_net = fv(R.id.btn_net);
        btn_wifi = fv(R.id.btn_wifi);
        btn_cancel = fv(R.id.btn_cancel);

        btn_net.setOnClickListener(this);
        btn_wifi.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void doInitData() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_wifi) {
            mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        } else if (v.getId() == R.id.btn_net) {
            @SuppressLint("WrongConstant") EthernetManager manager = (EthernetManager) mContext.getSystemService(Constract.ETHERNET_SERVICE);
            if (manager != null) {
                Intent intent = new Intent("android.settings.ETHERNET_SETTINGS");
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(intent);
                } else {
                    UIUtils.toastMessage("未找到该页面");
                }
            }
        } else if (v.getId() == R.id.btn_cancel) {
            dismiss();
        }
    }
}
