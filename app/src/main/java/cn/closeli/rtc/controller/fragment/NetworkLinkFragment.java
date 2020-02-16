package cn.closeli.rtc.controller.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.EthernetManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.closeli.rtc.R;
import cn.closeli.rtc.controller.SettingsActivity;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.utils.L;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * 终端设置 - 网络连接
 */
public class NetworkLinkFragment extends Fragment implements CustomAdapt {
    public static final String ETHERNET_SERVICE = "ethernet";
    private Button btn_net_config;
    private Button btn_wifi;
    private EditText et_address;
    private EditText et_port;
    private String initUrl;
    private String initPort;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_link,container,false);

        btn_net_config = view.findViewById(R.id.btn_net_config);
        btn_wifi = view.findViewById(R.id.btn_wifi);
        et_address = view.findViewById(R.id.et_address);
        et_port = view.findViewById(R.id.et_port);

        doInit();

        et_address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (!isFocus) {

                    if (!initUrl.equals(et_address.getText().toString()) || !initPort.equals(et_port.getText().toString())) {
                        ((SettingsActivity)getActivity()).setNetConfigChange(true);
                        ((SettingsActivity)getActivity()).setServerAddress(et_address.getText().toString());
                        ((SettingsActivity)getActivity()).setServerPort(et_port.getText().toString());
                    } else {
                        ((SettingsActivity)getActivity()).setNetConfigChange(false);
                    }
                }
            }
        });

        et_port.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (!isFocus) {
                    if (!initUrl.equals(et_address.getText().toString()) || !initPort.equals(et_port.getText().toString())) {
                        ((SettingsActivity)getActivity()).setNetConfigChange(true);
                        ((SettingsActivity)getActivity()).setServerAddress(et_address.getText().toString());
                        ((SettingsActivity)getActivity()).setServerPort(et_port.getText().toString());
                    } else {
                        ((SettingsActivity)getActivity()).setNetConfigChange(false);
                    }
                }
            }
        });

        btn_wifi.setOnClickListener(v-> {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });

        btn_net_config.setOnClickListener(v-> {
            @SuppressLint("WrongConstant") EthernetManager manager = (EthernetManager) getActivity().getSystemService(ETHERNET_SERVICE);
            if (manager != null ) {
                startActivity(new Intent("android.settings.ETHERNET_SETTINGS"));
            }
        });

//        ((SettingsActivity)getActivity()).childFragmentViewRequestFocus(this);
        initFocusLeft();
        btn_net_config.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    ((SettingsActivity)getActivity()).setNetLinkItemChecked();
                }
            }
        });

        return view;
    }

    private void initFocusLeft() {
        btn_net_config.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(3));
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

        initUrl = address;
        initPort = port;
        ((SettingsActivity)getActivity()).setServerAddress(et_address.getText().toString());
        ((SettingsActivity)getActivity()).setServerPort(et_port.getText().toString());
    }

    public void findFitstFocus() {
        btn_net_config.requestFocus();
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
