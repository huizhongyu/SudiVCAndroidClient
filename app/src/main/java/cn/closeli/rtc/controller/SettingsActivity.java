package cn.closeli.rtc.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocketState;
import com.vhd.base.util.SystemProp;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.closeli.rtc.BaseActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.GlobalValue;
import cn.closeli.rtc.controller.fragment.CommonFragment;
import cn.closeli.rtc.controller.fragment.MeetSetFragment;
import cn.closeli.rtc.controller.fragment.NetworkLinkFragment;
import cn.closeli.rtc.controller.fragment.ServerSettingFragment;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.PartDeviceModel;
import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.ws.WsError;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.RoomEventAdapter;
import cn.closeli.rtc.room.WssMethodNames;
import cn.closeli.rtc.utils.AccountUtil;
import cn.closeli.rtc.utils.ActivityUtils;
import cn.closeli.rtc.utils.CallUtil;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.SystemUtil;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.popwindow.MeetSettingPopupWindow;
import me.jessyan.autosize.internal.CustomAdapt;

import static cn.closeli.rtc.constract.GlobalValue.KEY_PARAMETER;
import static cn.closeli.rtc.utils.SPEditor.USER_ID;

/**
 * 新版终端设置 11-25
 */
public class SettingsActivity extends BaseActivity {
    @BindView(R.id.fl_contair)
    FrameLayout fl_contair;
    @BindView(R.id.btn_net_link)
    Button btn_net_link;
    @BindView(R.id.btn_common)
    Button btn_common;
    @BindView(R.id.btn_meetsetting)
    Button btn_meetsetting;
    @BindView(R.id.btn_server)
    Button btn_server;
    @BindView(R.id.tv_save)
    TextView tv_save;


    private FragmentManager fragmentManager;
    private NetworkLinkFragment networkLinkFragment;
    private CommonFragment commonFragment;
    private MeetSetFragment meetSetFragment;
    private ServerSettingFragment serverSettingFragment;
    private boolean isFirstLoad = true;
    //是否有修改
    private boolean isChanged = false;
    //统一处理，统一保存
    public DeviceSettingManager.DeviceModel deviceModel;
    public DeviceSettingManager.DeviceModel tempModel;
    public boolean isNetConfigChange = false;
    public String serverAddress;
    public String serverPort;
    private String meetName;
    private String meetPsd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        ActivityUtils.getInstance().addActivity(this);
        fragmentManager = getSupportFragmentManager();

        if (commonFragment == null) {
            commonFragment = new CommonFragment();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fl_contair, commonFragment);
        ft.commitAllowingStateLoss();

        btn_common.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);
        RoomClient.get().addRoomEventCallback(SettingsActivity.class, roomEventCallback);
        initFocus();
        btn_common.requestFocus();
        tv_save.setNextFocusRightId(R.id.tv_save);
        btn_common.setNextFocusRightId(R.id.btn_common_reset);
        btn_meetsetting.setNextFocusRightId(R.id.et_meet_name);
        btn_net_link.setNextFocusRightId(R.id.ll_title_contanir);

//        addContentView(getmFocusLayout(),
//                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT));//添加焦点层

        deviceModel = DeviceSettingManager.getInstance().getFromSp();
        tempModel = DeviceSettingManager.getInstance().getFromSp();
        meetName = deviceModel.getSubject();
        meetPsd = deviceModel.getPassword();
    }

    private void initFocus() {
        btn_common.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    initOtherBackground();
                    clickCommon(v);
                }
            }
        });

        btn_meetsetting.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                L.d("do this ---  >>>" + btn_meetsetting.getNextFocusRightId());
                if (hasFocus) {
                    initOtherBackground();
                    clickMeetsetting(v);
                }
            }
        });

        btn_server.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    initOtherBackground();
                }
            }
        });

        btn_net_link.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    initOtherBackground();
                    clickNetLink(v);
                }
            }
        });

        tv_save.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    initOtherBackground();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        RoomClient.get().setCurrentActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RoomClient.get().removeRoomEventCallback(SettingsActivity.class);
    }

    @OnClick(R.id.tv_save)
    public void clickSave(View view) {
        L.d("do this -------clicksave = " + isHasChanged());
//        if(isHasChanged()) {
        if (!inputJudge()) {
            return;
        }
        DeviceSettingManager.getInstance().setModel(deviceModel);
        DeviceSettingManager.getInstance().saveSp();
        UCToast.show(this, "保存成功");
        setAudioConfig();
//        }
        if (isNetConfigChange) {
            RoomClient.get().onlyCloseWebsocket();

            String address = serverAddress + ":" + serverPort;
            SudiHttpClient.baseUrl = address;
            SPEditor.instance().setString(SPEditor.BASE_URL, address);
            SPEditor.instance().setString(USER_ID, "");
            //重新登陆
            AccountUtil.INSTANCE.login();
            CallUtil.asyncCall(1000, () -> {
                finish();
            });

        } else {
            finish();
        }
    }

    @OnClick(R.id.btn_net_link)
    public void clickNetLink(View view) {
//        if (networkLinkFragment!= null && "networkLinkFragment".equals(fl_contair.getTag())) {
//            return;
//        }
        initOtherBackground();
        btn_net_link.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);

        if (networkLinkFragment != null && networkLinkFragment.isVisible()) {
//            networkLinkFragment.findFitstFocus();
            return;
        }

        if (networkLinkFragment == null) {
            networkLinkFragment = new NetworkLinkFragment();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fl_contair, networkLinkFragment);
        ft.commitAllowingStateLoss();
    }

    @OnClick(R.id.btn_server)
    public void clickContact(View view) {
        initOtherBackground();
        btn_server.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);
        if (serverSettingFragment != null && serverSettingFragment.isVisible()) {
//            serverSettingFragment.findFirstFocus();
            return;
        }
        if (serverSettingFragment == null) {
            serverSettingFragment = new ServerSettingFragment();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fl_contair, serverSettingFragment);
        ft.commitAllowingStateLoss();

    }

    @OnClick(R.id.btn_meetsetting)
    public void clickMeetsetting(View view) {
        initOtherBackground();
        btn_meetsetting.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);
        if (meetSetFragment != null && meetSetFragment.isVisible()) {
//            meetSetFragment.findFirstFocus();
            return;
        }
        if (meetSetFragment == null) {
            meetSetFragment = new MeetSetFragment();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fl_contair, meetSetFragment);
        ft.commitAllowingStateLoss();
    }

    @OnClick(R.id.btn_common)
    public void clickCommon(View view) {
        initOtherBackground();
        btn_common.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);
        if (commonFragment != null && commonFragment.isVisible()) {
//            commonFragment.findFitstFocus();
            return;
        }
        if (commonFragment == null) {
            commonFragment = new CommonFragment();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fl_contair, commonFragment);
        ft.commitAllowingStateLoss();
    }


    public RoomEventAdapter roomEventCallback = new RoomEventAdapter() {

        @Override
        public void onWebSocketFailMessage(WsError wsErrorr, String method) {
            runOnUiThread(() -> {
                if (WssMethodNames.getDepartmentTree.equals(method)) {
//                    if (serverSettingFragment != null) {
//                        serverSettingFragment.showError();
//                    }
                }
            });
        }

        @Override
        public void getSubDevOrUser(PartDeviceModel orgList) {
            runOnUiThread(() -> {
                if (serverSettingFragment != null && orgList.getDeviceList() != null && orgList.getDeviceList().size() > 0) {
//                    serverSettingFragment.notifyRcvData(orgList.getDeviceList());
                }
            });
        }

        @Override
        public void getDepartmentTree(CompanyModel companyModel) {
            runOnUiThread(() -> {
                if (serverSettingFragment != null) {
//                    serverSettingFragment.notifyOrgList(companyModel);
                }
            });
        }
    };

    public void childFragmentViewRequestFocus(Fragment fragment) {
        if (fragment instanceof CommonFragment) {
            if (!isFirstLoad) {
                ((CommonFragment) fragment).findFitstFocus();
            }
            isFirstLoad = false;
        } else if (fragment instanceof MeetSetFragment) {
            ((MeetSetFragment) fragment).findFirstFocus();
        } else if (fragment instanceof ServerSettingFragment) {
            ((ServerSettingFragment) fragment).findFirstFocus();
        } else if (fragment instanceof NetworkLinkFragment) {
            ((NetworkLinkFragment) fragment).findFitstFocus();
        }
    }

    //Fragment 返回时回到之前点击
    public int getIdByPosition(int position) {
        int viewId = 0;
        if (position == 3) {
            viewId = R.id.btn_net_link;
        } else if (position == 2) {
            viewId = R.id.btn_server;
        } else if (position == 1) {
            viewId = R.id.btn_meetsetting;
        } else if (position == 0) {
            viewId = R.id.btn_common;
        }
        return viewId;
    }

    public void setCommonItemChecked() {
        btn_common.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);
    }

    public void setContactItemChecked() {
        btn_server.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);
    }

    public void setNetLinkItemChecked() {
        btn_net_link.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);
    }

    public void setMeetItemCheked() {
        btn_meetsetting.setBackgroundResource(R.drawable.bg_shape_r2_c687df4);
    }

    private void initOtherBackground() {
        btn_common.setBackgroundResource(R.drawable.bg_selector_settings_item);
        btn_meetsetting.setBackgroundResource(R.drawable.bg_selector_settings_item);
        btn_server.setBackgroundResource(R.drawable.bg_selector_settings_item);
        btn_net_link.setBackgroundResource(R.drawable.bg_selector_settings_item);
    }

    @Override
    public void onStateChange(WebSocketState socketState) {
    }

    //是否有修改
    private boolean isHasChanged() {

        if (!meetName.equals(deviceModel.getSubject())) {
            return true;
        }

        if (!meetPsd.equals(deviceModel.getPassword())) {
            return true;
        }

        //辅流显示
        if (deviceModel.isScreenMain() != tempModel.isScreenMain()) {
            return true;
        }
        //摄像头倒置
        if (deviceModel.isInvertCamera() != tempModel.isInvertCamera()) {
            return true;
        }
        //音频输入
        if (deviceModel.getIndexAudioInput() != tempModel.getIndexAudioInput()) {
            return true;
        }
        //音频输出
        if (deviceModel.getIndexAudioOutput() != tempModel.getIndexAudioOutput()) {
            return true;
        }
        //sfu模式
        if (deviceModel.isSFUMode() != tempModel.isSFUMode()) {
            return true;
        }
        //会议最大人数
        if (deviceModel.getRoomCapacity() != tempModel.getRoomCapacity()) {
            return true;
        }
        //会议时长
        if (deviceModel.getDuration() != tempModel.getDuration()) {
            return true;
        }
        //会议id
        if (!deviceModel.getMeetId().equals(tempModel.getMeetId())) {
            return true;
        }
        //会议密码
        if (deviceModel.isUserPsd() != tempModel.isUserPsd()) {
            return true;
        }
        //id 入会
        if (deviceModel.isAllIdInto() != tempModel.isAllIdInto()) {
            return true;
        }
        //创会设置
        if (deviceModel.isOpenMicroSelf() != tempModel.isOpenMicroSelf()) {
            return true;
        }
        //入会设置
        if (!deviceModel.getIsSettingMicroOn().equals(tempModel.getIsSettingMicroOn())) {
            return true;
        }
        if (!deviceModel.getIsSettingCameraOn().equals(tempModel.getIsSettingCameraOn())) {
            return true;
        }
        if (deviceModel.isDeviceAuto() != tempModel.isDeviceAuto()) {
            return true;
        }


        return false;
    }

    //判断输入的是否为空并处理
    public boolean inputJudge() {
        //todo 会议名称空不保存
        if (TextUtils.isEmpty(deviceModel.getSubject())) {
            UCToast.show(this, "请输入会议室名称");
            return false;
        }
        if (deviceModel.isUserPsd()) {
            if (TextUtils.isEmpty(deviceModel.getPassword())) {
                UCToast.show(this, "请输入会议密码");
                return false;
            }
        }
        int port = 0;
        if (!TextUtils.isEmpty(serverPort)) {
            port = Integer.valueOf(serverPort.trim());
        }
        String ipAddr = serverAddress.trim();
        if (!ipAddr.startsWith("http://") && !ipAddr.startsWith("https://")) {
            UCToast.show(this, "请输入正确的服务器地址！");
            return false;
        }

        //如果替换成域名 ？
        if (ipAddr.startsWith("http://")) {
            String ip = ipAddr.substring(7, ipAddr.length());
            if (!checkAddress(ip)) {
                UCToast.show(this, "请输入正确的服务器地址！");
                return false;
            }
        }

        if (ipAddr.startsWith("https://")) {
            String ip = ipAddr.substring(8, ipAddr.length());
            if (!checkAddress(ip)) {
                UCToast.show(this, "请输入正确的服务器地址！");
                return false;
            }
        }

        if (port > 65535) {
            UCToast.show(this, "请输入正确的端口号！");
            return false;
        }

        return true;
    }

    //设置相关参数
    private void setAudioConfig() {
        DeviceSettingManager.getInstance().setCameraControl(KEY_PARAMETER, "default");
        boolean isInvertCamera = deviceModel.isInvertCamera();
        int audioInput = deviceModel.getIndexAudioInput();
        int audioOutput = deviceModel.getIndexAudioOutput();

        DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_INVERSION, isInvertCamera ? "2" : "3");     //open:2  close:3
        if (audioInput == 0) {
            SystemProp.setProperty("vhd.audio.micin.force", "3");
        } else if (audioInput == 1) {
            SystemProp.setProperty("vhd.audio.micin.force", "2");
        } else {
            if (SystemUtil.getMtkVersion().equals("v0.0.3")) {
                SystemProp.setProperty("vhd.audio.micin.force", "1");
            } else {
                SystemProp.setProperty("vhd.audio.micin.force", "4");
            }
        }

        if (audioOutput == 0) {
            SystemProp.setProperty("vhd.hdmi1.audio.mute", "0");
            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
        } else if (audioOutput == 1) {
            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
            SystemProp.setProperty("vhd.hdmi2.audio.mute", "0");
            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
        } else if (audioOutput == 2) {
            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
            SystemProp.setProperty("vhd.lineout.audio.mute", "0");
            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
        } else {
            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
            SystemProp.setProperty("vhd.usbout.audio.mute", "0");
        }


    }

    public static boolean checkAddress(String s) {
        return s.matches("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])");
    }


    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isNetConfigChange() {
        return isNetConfigChange;
    }

    public void setNetConfigChange(boolean netConfigChange) {
        isNetConfigChange = netConfigChange;
    }
}
