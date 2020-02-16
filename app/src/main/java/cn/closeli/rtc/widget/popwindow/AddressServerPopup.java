package cn.closeli.rtc.widget.popwindow;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.closeli.rtc.R;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.widget.BasePopupWindow;
import cn.closeli.rtc.widget.UCToast;

public class AddressServerPopup extends BasePopupWindow implements View.OnClickListener {
    private String channel;
    private AddressServerPopup.ServerPopup layoutChoose;
    private TextView tv_comfirm, tv_cancel;
    private EditText et_address, et_port;
    private String address;

    public AddressServerPopup(Context context) {
        super(context);
    }


    @Override
    public int thisLayout() {
        return R.layout.dialog_address_server;
    }

    @Override
    public void doInitView() {
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
//        this.setAnimationStyle(R.style.PopupWindowLeftAnimStyle);
        tv_comfirm = view.findViewById(R.id.tv_comfirm);
        tv_cancel = view.findViewById(R.id.tv_cancel);
        et_address = view.findViewById(R.id.et_address);
        et_port = view.findViewById(R.id.et_port);


        tv_comfirm.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
    }


    @Override
    public void doInitData() {
        String url = SudiHttpClient.baseUrl;;
        boolean hasPort = false;
        if (url.contains("//")) {
            hasPort = url.substring(url.indexOf("//")).contains(":");
        }
        String address = hasPort ? url.substring(0, url.lastIndexOf(":")) : url;
        String port = hasPort ? url.substring(url.lastIndexOf(":") + 1) : url.startsWith("https://") ? "443" : "80";
        et_address.setText(address);
        et_port.setText(port);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_comfirm) {
            int port = Integer.valueOf(et_port.getText().toString().trim());
            String ipAddr = et_address.getText().toString().trim();
            if (!ipAddr.startsWith("http://") && !ipAddr.startsWith("https://")) {
                UCToast.show(mContext,"请输入正确的服务器地址！");
                return;
            }

            //如果替换成域名 ？
            if (ipAddr.startsWith("http://")) {
                String ip = ipAddr.substring(7,ipAddr.length());
                if (!checkAddress(ip)) {
                    UCToast.show(mContext,"请输入正确的服务器地址！");
                    return;
                }
            }

            if (ipAddr.startsWith("https://")) {
                String ip = ipAddr.substring(8,ipAddr.length());
                if (!checkAddress(ip)) {
                    UCToast.show(mContext,"请输入正确的服务器地址！");
                    return;
                }
            }

            if (port > 65535) {
                UCToast.show(mContext,"请输入正确的端口号！");
                return;
            }
            address = et_address.getText().toString().trim() + ":" + et_port.getText().toString().trim();
            layoutChoose.layout(1, address);
        } else if (i == R.id.tv_cancel) {
            layoutChoose.layout(0, address);
        }
        dismiss();
    }

    public interface ServerPopup {
        void layout(int position, String address);
    }

    public AddressServerPopup.ServerPopup getLayoutChoose() {
        return layoutChoose;
    }

    public void setLayoutChoose(AddressServerPopup.ServerPopup layoutChoose) {
        this.layoutChoose = layoutChoose;
    }

    public static boolean checkAddress(String s) {
        return s.matches("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])");
    }

}


