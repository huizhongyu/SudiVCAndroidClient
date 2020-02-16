package cn.closeli.rtc.controller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.EthernetManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocketState;
import com.vhd.base.util.SystemProp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.closeli.rtc.BaseActivity;
import cn.closeli.rtc.BuildConfig;
import cn.closeli.rtc.R;
import cn.closeli.rtc.UpgradeAppUtil;
import cn.closeli.rtc.constract.GlobalValue;
import cn.closeli.rtc.model.http.SudiHttpCallback;
import cn.closeli.rtc.model.http.UpgradeResp;
import cn.closeli.rtc.net.SudiHttpClient;
import cn.closeli.rtc.utils.ActivityUtils;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.widget.FocusLayout;
import cn.closeli.rtc.widget.popwindow.DeviceSpinnerPopup;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * 终端设置
 */
public class DeviceSettingActivity extends BaseActivity {
    public static final String ETHERNET_SERVICE = "ethernet";
    public static final int SETTING_CODE = 100;

    @BindView(R.id.btn_img_config)
    Button btn_img_config;
    @BindView(R.id.et_room_name)
    EditText et_room_name;
    @BindView(R.id.cb_close_psw)
    CheckBox cb_close_psw;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.iv_pwd_eye)
    ImageView iv_pwd_eye;
    @BindView(R.id.rg_meet_id)
    RadioGroup rg_meet_id;
    @BindView(R.id.rg_meet_into)
    RadioGroup rg_meet_into;
    @BindView(R.id.rg_meet_share)
    RadioGroup rg_meet_share;
    @BindView(R.id.cb_close_micro)
    CheckBox cb_close_micro;
    @BindView(R.id.cb_open_camera)
    CheckBox cb_open_camera;
    @BindView(R.id.cb_auto_into)
    CheckBox cb_auto_into;
    @BindView(R.id.cb_open_micro_self)
    CheckBox cb_open_micro_self;
    @BindView(R.id.cb_open_share_self)
    CheckBox cb_open_share_self;
    @BindView(R.id.iv_angle_switch)
    ImageView iv_angle_switch;
    @BindView(R.id.rg_camera_focus)
    RadioGroup rg_camera_focus;
    @BindView(R.id.rg_audio_input)
    RadioGroup rg_audio_input;
    @BindView(R.id.rg_audio_output)
    RadioGroup rg_audio_output;
    @BindView(R.id.iv_switch_zimu)
    ImageView iv_switch_zimu;
    @BindView(R.id.et_zimu)
    EditText et_zimu;
    @BindView(R.id.rg_window)
    RadioGroup rg_window;
    @BindView(R.id.rb_id_self)
    RadioButton rb_id_self;
    @BindView(R.id.tv_spinner_hour)
    TextView tv_spinner_hour;
    @BindView(R.id.tv_spinner_num)
    TextView tv_spinner_num;
    @BindView(R.id.tv_version_content)
    TextView tv_version_content;
    FocusLayout mFocusLayout;

    private List<String> listOfPerson;
    private List<String> listOfHour;
    private String meetNum = "16人";         //会议人数
    private String meetHour = "2.0小时";        //会议时长
    private int audioInput = 0;
    private int audioOutput = 0;
    private boolean isCameraInvert = false;      //摄像头角度
    private boolean isZimuShow = true;          //显示字幕
    private boolean isEyeSelected = false;
    private DeviceSettingManager.DeviceModel deviceModel;
    private DeviceSpinnerPopup deviceSpinnerPopup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicesetting);
        ActivityUtils.getInstance().addActivity(this);
        ButterKnife.bind(this);
//        showStatusBar();
        listOfPerson = new ArrayList<>();
        listOfPerson = Arrays.asList(getResources().getStringArray(R.array.arrays_meet_num));
        listOfHour = new ArrayList<>();
        listOfHour = Arrays.asList(getResources().getStringArray(R.array.arrays_meet_hour));

        deviceModel = DeviceSettingManager.getInstance().getFromSp();

        initData();
        initView();

    }

    private void bindListener() {
        //获取根元素
        View mContainerView = this.getWindow().getDecorView();//.findViewById(android.R.id.content);
        //得到整个view树的viewTreeObserver
        ViewTreeObserver viewTreeObserver = mContainerView.getViewTreeObserver();
        //给观察者设置焦点变化监听
        viewTreeObserver.addOnGlobalFocusChangeListener(mFocusLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFocusLayout = new FocusLayout(this);
        bindListener();//绑定焦点变化事件
        addContentView(mFocusLayout,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));//添加焦点层
    }

    private void initView() {
        tv_version_content.setText(BuildConfig.VERSION_NAME);
        iv_pwd_eye.setSelected(isEyeSelected);
        isCameraInvert = deviceModel.isInvertCamera();
        isZimuShow = deviceModel.isZimuOpen();
        iv_angle_switch.setImageResource(isCameraInvert ? R.drawable.icon_switch_on : R.drawable.icon_switch_off);
        iv_switch_zimu.setImageResource(isZimuShow ? R.drawable.icon_switch_on : R.drawable.icon_switch_off);
        meetNum = deviceModel.getRoomCapacity() + "人";
        meetHour = deviceModel.getDuration() + "小时";
        tv_spinner_num.setText(deviceModel.getRoomCapacity() + "人");
        tv_spinner_hour.setText(deviceModel.getDuration() + "小时");
        rb_id_self.setText("本人ID（" + SPEditor.instance().getAccount() + "）");
        cb_close_psw.setChecked(deviceModel.isUserPsd());
        if (!TextUtils.isEmpty(deviceModel.getPassword())) {
            password.setText(deviceModel.getPassword());
        }
        if (!TextUtils.isEmpty(deviceModel.getSubject())) {
            et_room_name.setText(deviceModel.getSubject());
        } else {
            et_room_name.setText(SPEditor.instance().getUserNick() == null ? SPEditor.instance().getAccount() + "的会议" : SPEditor.instance().getUserNick() + "的会议");
        }

        //入会开启麦克风
        if ("on".equals(deviceModel.getIsSettingMicroOn())) {
            cb_close_micro.setChecked(true);
        } else {
            cb_close_micro.setChecked(false);
        }

        //入会开启摄像头
        if ("on".equals(deviceModel.getIsSettingCameraOn())) {
            cb_open_camera.setChecked(true);
        } else {
            cb_open_camera.setChecked(false);
        }

        //会议id
        if (TextUtils.isEmpty(deviceModel.getMeetId())) {
            rg_meet_id.check(R.id.rb_id_auto);
        } else {
            rg_meet_id.check(R.id.rb_id_self);
        }

        //id入会
        if (deviceModel.isAllIdInto()) {
            rg_meet_into.check(R.id.rb_id_all);
        } else {
            rg_meet_into.check(R.id.rb_id_host);
        }

        //共享权限
        if (deviceModel.isAllShare()) {
            rg_meet_share.check(R.id.rb_id_share_all);
        } else {
            rg_meet_share.check(R.id.rb_id_share_host);
        }

        cb_auto_into.setChecked(deviceModel.isDeviceAuto());
        cb_open_share_self.setChecked(deviceModel.isOpenShareSelf());
        cb_open_micro_self.setChecked(deviceModel.isOpenMicroSelf());

        //摄像头聚焦
        if (deviceModel.isCameraFocusAuto()) {
            rg_camera_focus.check(R.id.rb_camera_focus_auto);
        } else {
            rg_camera_focus.check(R.id.rb_camera_focus_hand);
        }
        //音频输入
        if (deviceModel.getIndexAudioInput() == 0) {
            rg_audio_input.check(R.id.rb_audio_input_linein);
        } else if (deviceModel.getIndexAudioInput() == 1) {
            rg_audio_input.check(R.id.rb_audio_input_usb);
        } else {
            rg_audio_input.check(R.id.rb_audio_input_micin);
        }
        //音频输出
        if (deviceModel.getIndexAudioOutput() == 0) {
            rg_audio_output.check(R.id.rb_audio_output_hdmi1);
        } else if (deviceModel.getIndexAudioOutput() == 1) {
            rg_audio_output.check(R.id.rb_audio_output_hdmi2);
        } else if (deviceModel.getIndexAudioOutput() == 2) {
            rg_audio_output.check(R.id.rb_audio_output_linein);
        } else {
            rg_audio_output.check(R.id.rb_audio_output_usb);
        }

        if (deviceModel.getIndexWindow() == 0) {
            rg_window.check(R.id.rb_window_1920);
        } else {
            rg_window.check(R.id.rb_window_3840);
        }


    }

    private void initData() {
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, R.id.tv_item, listOfPerson);
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, R.id.tv_item, listOfHour);

        tv_spinner_hour.setOnClickListener(v -> {
            deviceSpinnerPopup = new DeviceSpinnerPopup(this, listOfHour, 0);
            deviceSpinnerPopup.setOnSpinnerItemClickListener((string, position) -> {
                tv_spinner_hour.setText(string);
                meetHour = string;
                deviceSpinnerPopup.dismiss();
            });
            deviceSpinnerPopup.showAsDropDown(tv_spinner_hour);
        });

        tv_spinner_num.setOnClickListener(v -> {
            deviceSpinnerPopup = new DeviceSpinnerPopup(this, listOfPerson, 0);
            deviceSpinnerPopup.setOnSpinnerItemClickListener((string, position) -> {
                tv_spinner_num.setText(string);
                meetNum = string;
                deviceSpinnerPopup.dismiss();
            });
            deviceSpinnerPopup.showAsDropDown(tv_spinner_num);
        });

    }

    //状态栏全屏
    private void showStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    @OnClick(R.id.btn_net_config_wifi)
    public void gotoSettingNetWifi(View view) {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    @OnClick(R.id.btn_net_config)
    public void gotoSettingNet(View view) {
        @SuppressLint("WrongConstant") EthernetManager manager = (EthernetManager) getSystemService(ETHERNET_SERVICE);
        if (manager != null && manager.isAvailable()) {
            startActivity(new Intent("android.settings.ETHERNET_SETTINGS"));
        }
    }

    @OnClick(R.id.btn_img_config)
    public void gotoImgConfig(View view) {
        startActivity(new Intent(DeviceSettingActivity.this, PreviewHdmiActivity.class));
    }

    @OnClick(R.id.btn_sava)
    public void save(View view) {
        if (TextUtils.isEmpty(et_room_name.getText().toString())) {
            Toast.makeText(this, "请输入会议室名称", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password.getText().toString()) && cb_close_psw.isChecked()) {
            Toast.makeText(this, "请输入入会密码", Toast.LENGTH_SHORT).show();
            return;
        }

        deviceModel.setSubject(et_room_name.getText().toString());
        deviceModel.setRoomCapacity(Integer.valueOf(meetNum.substring(0, meetNum.indexOf("人"))));
        float time = Float.valueOf(meetHour.substring(0, meetHour.indexOf("小")));
        deviceModel.setDuration(time);
        if (rg_meet_id.getCheckedRadioButtonId() == R.id.rb_id_self) {      //会议id      ""自动分配
            deviceModel.setMeetId(SPEditor.instance().getAccount());
        } else {
            deviceModel.setMeetId("");
        }
        if (rg_meet_into.getCheckedRadioButtonId() == R.id.rb_id_host) {    //ID入会 false:仅主持人
            deviceModel.setAllIdInto(false);
        } else {
            deviceModel.setAllIdInto(true);
        }

        if (rg_meet_share.getCheckedRadioButtonId() == R.id.rb_id_share_host) { //共享权限 false： 仅主持人
            deviceModel.setAllShare(false);
        } else {
            deviceModel.setAllShare(true);
        }

        if (rg_camera_focus.getCheckedRadioButtonId() == R.id.rb_camera_focus_auto) {       //摄像头聚焦
            deviceModel.setCameraFocusAuto(true);
        } else {
            deviceModel.setCameraFocusAuto(false);
        }

        if (rg_audio_input.getCheckedRadioButtonId() == R.id.rb_audio_input_linein) {           //音频输入
            deviceModel.setIndexAudioInput(0);
            audioInput = 0;
        } else if (rg_audio_input.getCheckedRadioButtonId() == R.id.rb_audio_input_usb) {
            deviceModel.setIndexAudioInput(1);
            audioInput = 1;
        } else {
            deviceModel.setIndexAudioInput(2);
            audioInput = 2;
        }

        if (rg_audio_output.getCheckedRadioButtonId() == R.id.rb_audio_output_hdmi1) {          //音频输出
            deviceModel.setIndexAudioOutput(0);
            audioOutput = 0;
        } else if (rg_audio_output.getCheckedRadioButtonId() == R.id.rb_audio_output_hdmi2) {
            deviceModel.setIndexAudioOutput(1);
            audioOutput = 1;
        } else if (rg_audio_output.getCheckedRadioButtonId() == R.id.rb_audio_output_linein) {
            deviceModel.setIndexAudioOutput(2);
            audioOutput = 2;
        } else {
            deviceModel.setIndexAudioOutput(3);
            audioOutput = 3;
        }

        if (rg_window.getCheckedRadioButtonId() == R.id.rb_window_1920) {           //显示器
            deviceModel.setIndexWindow(0);
        } else {
            deviceModel.setIndexWindow(1);
        }


        deviceModel.setIsSettingMicroOn(cb_close_micro.isChecked() ? "on" : "off");
        deviceModel.setIsSettingCameraOn(cb_open_camera.isChecked() ? "on" : "off");
        deviceModel.setDeviceAuto(cb_auto_into.isChecked());
        deviceModel.setOpenMicroSelf(cb_open_micro_self.isChecked());
        deviceModel.setOpenShareSelf(cb_open_share_self.isChecked());
        deviceModel.setInvertCamera(isCameraInvert);
        deviceModel.setZimuOpen(isZimuShow);
        if (!TextUtils.isEmpty(et_zimu.getText().toString())) {
            deviceModel.setZimu(et_zimu.getText().toString());
        }

        deviceModel.setUserPsd(cb_close_psw.isChecked());
        if (!TextUtils.isEmpty(password.getText().toString())) {
            deviceModel.setPassword(password.getText().toString());
        }

        DeviceSettingManager.getInstance().setModel(deviceModel);
        setConfigs();
        DeviceSettingManager.getInstance().saveSp();
        setResult(100);
        finish();
    }

    //执行相关设置 操作
    private void setConfigs() {
//        DeviceSettingManager.getInstance().setCameraControl(KEY_PARAMETER, "default");
        DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_INVERSION, isCameraInvert ? "2" : "3");     //open:2  close:3
        if (audioInput == 0) {
            SystemProp.setProperty("vhd.audio.micin.force", "3");
        } else if (audioInput == 1) {
            SystemProp.setProperty("vhd.audio.micin.force", "2");
        } else {
            SystemProp.setProperty("vhd.audio.micin.force", "1");
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

    //摄像头角度
    @OnClick(R.id.iv_angle_switch)
    public void clickCameraInvert(View view) {
        if (isCameraInvert) {
            iv_angle_switch.setImageResource(R.drawable.icon_switch_off);
        } else {
            iv_angle_switch.setImageResource(R.drawable.icon_switch_on);
        }
        isCameraInvert = !isCameraInvert;
    }

    //字幕开关
    @OnClick(R.id.iv_switch_zimu)
    public void clickZimuOpen(View view) {
        if (isZimuShow) {
            iv_switch_zimu.setImageResource(R.drawable.icon_switch_off);
        } else {
            iv_switch_zimu.setImageResource(R.drawable.icon_switch_on);
        }

        isZimuShow = !isZimuShow;
    }

    public void clickEye(View view) {
        isEyeSelected = !isEyeSelected;
        iv_pwd_eye.setSelected(isEyeSelected);
        if (isEyeSelected) {
            password.setInputType(InputType.TYPE_CLASS_NUMBER | InputType
                    .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            password.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        password.setSelection(password.getText().length());
    }

    @Override
    public void onStateChange(WebSocketState socketState) {

    }

    @OnClick(R.id.btn_update)
    public void checkVersion(View view) {
        SudiHttpClient.get().httpCheckVersion(new SudiHttpCallback<UpgradeResp>() {
            @Override
            public void onSuccess(UpgradeResp response) {
                L.d("httpCheckVersion onSuccess %1$s", response.toString());
                if (UpgradeAppUtil.isUpgrade(String.valueOf(response.getVersionName()), response.getUpgradeType())) {
                    UpgradeAppUtil.upgrade(DeviceSettingActivity.this, response.getDownloadUrl(), new UpgradeAppUtil.UpgradCallback() {
                        @Override
                        public void onError(String msg) {

                        }

                        @Override
                        public void onSuccess() {

                        }
                    });
                }
            }

            @Override
            public void onFailed(Throwable e) {
                L.d("httpCheckVersion onFailed %1$s", e.getMessage());
            }
        });
    }

}
