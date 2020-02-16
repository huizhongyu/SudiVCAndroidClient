package cn.closeli.rtc.controller.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;

import cn.closeli.rtc.BaseActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.controller.SettingsActivity;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.AccountUtil;
import cn.closeli.rtc.utils.ActivityUtils;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.widget.UCToast;
import me.jessyan.autosize.internal.CustomAdapt;

import static cn.closeli.rtc.utils.SPEditor.USER_ID;

public class ServerSettingFragment extends Fragment implements CustomAdapt, View.OnClickListener {
    private EditText et_address;
    private EditText et_port;
    private Button btn_comfirm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_serversetting,container,false);
        et_address = view.findViewById(R.id.et_address);
        et_port = view.findViewById(R.id.et_port);
        btn_comfirm = view.findViewById(R.id.btn_comfirm);
        btn_comfirm.setOnClickListener(this);

//        ((SettingsActivity) getActivity()).childFragmentViewRequestFocus(this);
        doInit();
        initData();
        initFocusMove();
        return view;
    }

    private void doInit() {
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

    private void initData() {
        et_address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((SettingsActivity) getActivity()).setContactItemChecked();
                }
            }
        });
        et_port.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((SettingsActivity) getActivity()).setContactItemChecked();
                }
            }
        });
    }

    private void initFocusMove() {
        et_port.setNextFocusDownId(((SettingsActivity)getActivity()).getIdByPosition(2));
        et_port.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(2));
        et_address.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(2));
    }
    public void findFirstFocus() {
        et_address.requestFocus();
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_comfirm) {
            int port = Integer.valueOf(et_port.getText().toString().trim());
            String ipAddr = et_address.getText().toString().trim();
            if (!ipAddr.startsWith("http://") && !ipAddr.startsWith("https://")) {
                UCToast.show(getContext(),"请输入正确的服务器地址！");
                return;
            }

            //如果替换成域名 ？
            if (ipAddr.startsWith("http://")) {
                String ip = ipAddr.substring(7,ipAddr.length());
                if (!checkAddress(ip)) {
                    UCToast.show(getContext(),"请输入正确的服务器地址！");
                    return;
                }
            }

            if (ipAddr.startsWith("https://")) {
                String ip = ipAddr.substring(8,ipAddr.length());
                if (!checkAddress(ip)) {
                    UCToast.show(getContext(),"请输入正确的服务器地址！");
                    return;
                }
            }

            if (port > 65535) {
                UCToast.show(getContext(),"请输入正确的端口号！");
                return;
            }
            RoomClient.get().onlyCloseWebsocket();

            String address = et_address.getText().toString().trim() + ":" + et_port.getText().toString().trim();
            SudiHttpClient.baseUrl = address;
            SPEditor.instance().setString(SPEditor.BASE_URL, address);
            UCToast.show(getContext(),getString(R.string.str_save_success));
            SPEditor.instance().setString(USER_ID,"");
            //重新登陆
            AccountUtil.INSTANCE.login();
        }
    }

    public static boolean checkAddress(String s) {
        return s.matches("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])");
    }













    /**
     * 是否按照宽度进行等比例适配 (为了保证在高宽比不同的屏幕上也能正常适配, 所以只能在宽度和高度之中选一个作为基准进行适配)
     *
     * @return {@code true} 为按照宽度适配, {@code false} 为按照高度适配
     */
    @Override
    public boolean isBaseOnWidth() {
        return false;
    }

    /**
     * 返回设计图上的设计尺寸, 单位 dp
     * {@link #getSizeInDp} 须配合 {@link #isBaseOnWidth()} 使用, 规则如下:
     * 如果 {@link #isBaseOnWidth()} 返回 {@code true}, {@link #getSizeInDp} 则应该返回设计图的总宽度
     * 如果 {@link #isBaseOnWidth()} 返回 {@code false}, {@link #getSizeInDp} 则应该返回设计图的总高度
     * 如果您不需要自定义设计图上的设计尺寸, 想继续使用在 AndroidManifest 中填写的设计图尺寸, {@link #getSizeInDp} 则返回 {@code 0}
     *
     * @return 设计图上的设计尺寸, 单位 dp
     */
    @Override
    public float getSizeInDp() {
        return 0;
    }
}
