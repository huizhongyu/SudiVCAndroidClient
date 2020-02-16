package cn.closeli.rtc.controller.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.controller.SettingsActivity;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ViewUtils;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.popwindow.DeviceSpinnerPopup;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * 终端设置 - 会议设置
 */
public class MeetSetFragment extends Fragment implements CustomAdapt, View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    public static final String TAG_MEETSET = "tag_meetset";
    private Button btn_save;
    private EditText et_meet_name;
    private TextView tv_meet_during;
    private TextView tv_meet_num;
    private RadioGroup rg_meet_id;
    private RadioButton rb_meet_id_self;
    private RadioButton rb_meet_id_auto;
    private CheckBox cb_meet_pwd;
    private EditText et_password;
    private RadioGroup rg_meet_into_id;
    private RadioButton rb_meet_into_host;
    private RadioButton rb_meet_into_all;
    private RadioGroup rg_meet_share;
    private RadioButton rb_meet_share_host;
    private RadioButton rb_meet_share_all;
    private CheckBox cb_meet_control_mic;
    private CheckBox cb_meet_control_share;
    private CheckBox cb_meet_into_open_mic;
    private CheckBox cb_meet_into_open_camera;
    private CheckBox cb_meet_into_auto;
    private RadioGroup rg_meet_mode;
    private RadioButton rb_meet_mode_mcu;
    private RadioButton rb_meet_mode_sfu;

    private DeviceSettingManager.DeviceModel deviceModel;
    private DeviceSpinnerPopup deviceSpinnerPopup;
    private List<String> listOfPerson;
    private List<String> listOfHour;
    private String meetNum = "12人";         //会议人数
    private String meetHour = "2.0小时";        //会议时长
    private boolean isSfu = true;               //是否是sfu


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meetsetting,container,false);
        tv_meet_during = view.findViewById(R.id.tv_meet_during);
        tv_meet_num = view.findViewById(R.id.tv_meet_num);
        btn_save = view.findViewById(R.id.btn_save);
        et_meet_name = view.findViewById(R.id.et_meet_name);
        rg_meet_id = view.findViewById(R.id.rg_meet_id);
        rb_meet_id_self = view.findViewById(R.id.rb_meet_id_self);
        rb_meet_id_auto = view.findViewById(R.id.rb_meet_id_auto);
        cb_meet_pwd = view.findViewById(R.id.cb_meet_pwd);
        rg_meet_into_id = view.findViewById(R.id.rg_meet_into_id);
        rb_meet_into_host = view.findViewById(R.id.rb_meet_into_host);
        rb_meet_into_all = view.findViewById(R.id.rb_meet_into_all);
        rg_meet_share = view.findViewById(R.id.rg_meet_share);
        rb_meet_share_host = view.findViewById(R.id.rb_meet_share_host);
        rb_meet_share_all = view.findViewById(R.id.rb_meet_share_all);
        cb_meet_control_mic = view.findViewById(R.id.cb_meet_control_mic);
        cb_meet_control_share = view.findViewById(R.id.cb_meet_control_share);
        cb_meet_into_open_mic = view.findViewById(R.id.cb_meet_into_open_mic);
        cb_meet_into_open_camera = view.findViewById(R.id.cb_meet_into_open_camera);
        cb_meet_into_auto = view.findViewById(R.id.cb_meet_into_auto);
        et_password = view.findViewById(R.id.et_password);
        rb_meet_mode_sfu = view.findViewById(R.id.rb_meet_mode_sfu);
        rb_meet_mode_mcu = view.findViewById(R.id.rb_meet_mode_mcu);
        rg_meet_mode = view.findViewById(R.id.rg_meet_mode);
//        ((SettingsActivity)getActivity()).childFragmentViewRequestFocus(this);
        initData();
        initView();
        initFocusLeft();
        return view;
    }

    private void initFocusLeft() {
        btn_save.setNextFocusLeftId(R.id.et_meet_name);
        btn_save.setNextFocusDownId(R.id.et_meet_name);
        et_meet_name.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        et_meet_name.setNextFocusRightId(R.id.et_meet_name);
        tv_meet_num.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        tv_meet_num.setNextFocusRightId(R.id.tv_meet_num);
        tv_meet_during.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        tv_meet_during.setNextFocusRightId(R.id.tv_meet_during);
        rb_meet_id_self.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        rb_meet_id_auto.setNextFocusRightId(R.id.rb_meet_id_auto);
        cb_meet_pwd.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        et_password.setNextFocusRightId(R.id.et_password);
        rb_meet_into_host.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        rb_meet_into_all.setNextFocusRightId(R.id.rb_meet_into_all);
        rb_meet_share_host.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        rb_meet_share_all.setNextFocusRightId(R.id.rb_meet_share_all);
        rb_meet_mode_mcu.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        rb_meet_mode_sfu.setNextFocusRightId(R.id.rb_meet_mode_sfu);
        cb_meet_control_mic.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        cb_meet_control_mic.setNextFocusRightId(R.id.cb_meet_control_mic);
        cb_meet_control_share.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        cb_meet_control_share.setNextFocusRightId(R.id.cb_meet_control_share);
        cb_meet_into_open_mic.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        cb_meet_into_open_mic.setNextFocusDownId(R.id.cb_meet_into_open_mic);
        cb_meet_into_open_camera.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        cb_meet_into_open_camera.setNextFocusDownId(R.id.cb_meet_into_open_camera);
        cb_meet_into_auto.setNextFocusLeftId(((SettingsActivity)getActivity()).getIdByPosition(1));
        cb_meet_into_auto.setNextFocusDownId(R.id.cb_meet_into_auto);

    }

    private void initData() {
        deviceModel = ((SettingsActivity)getActivity()).deviceModel;
        isSfu = deviceModel.isSFUMode();
        meetNum = deviceModel.getRoomCapacity() + "人";
        if(deviceModel.getDuration() != 9999f) {
            meetHour = deviceModel.getDuration() + "小时";
        } else {
            meetHour = "不限时";
        }
        rb_meet_id_self.setText("使用本人ID（" + SPEditor.instance().getAccount() + "）");
        listOfPerson = new ArrayList<>();
        listOfPerson = Arrays.asList(getResources().getStringArray(R.array.arrays_meet_num));
        listOfHour = new ArrayList<>();
        listOfHour = Arrays.asList(getResources().getStringArray(R.array.arrays_meet_hour));
        if (!TextUtils.isEmpty(deviceModel.getSubject())) {
            et_meet_name.setText(deviceModel.getSubject());
        } else {
            et_meet_name.setText(SPEditor.instance().getUserNick() == null ? SPEditor.instance().getAccount() + "的会议" : SPEditor.instance().getUserNick() + "的会议");
        }

        et_password.setText(deviceModel.getPassword());

        cb_meet_pwd.setChecked(deviceModel.isUserPsd());

        //入会开启麦克风
        if ("on".equals(deviceModel.getIsSettingMicroOn())) {
            cb_meet_into_open_mic.setChecked(true);
        } else {
            cb_meet_into_open_mic.setChecked(false);
        }

        //入会开启摄像头
        if ("on".equals(deviceModel.getIsSettingCameraOn())) {
            cb_meet_into_open_camera.setChecked(true);
        } else {
            cb_meet_into_open_camera.setChecked(false);
        }

        //会议id
        if (TextUtils.isEmpty(deviceModel.getMeetId())) {
            rg_meet_id.check(R.id.rb_meet_id_auto);
        } else {
            rg_meet_id.check(R.id.rb_meet_id_self);
        }

        //id入会
        if (deviceModel.isAllIdInto()) {
            rg_meet_into_id.check(R.id.rb_meet_into_all);
        } else {
            rg_meet_into_id.check(R.id.rb_meet_into_host);
        }

        if (!isSfu) {
            rg_meet_mode.check(R.id.rb_meet_mode_mcu);
        } else {
            rg_meet_mode.check(R.id.rb_meet_mode_sfu);
        }

        //共享权限
        if (deviceModel.isAllShare()) {
            rg_meet_share.check(R.id.rb_meet_share_all);
        } else {
            rg_meet_share.check(R.id.rb_meet_share_host);
        }
        changeSpinnerPerson(isSfu);

        cb_meet_into_auto.setChecked(deviceModel.isDeviceAuto());
        cb_meet_control_share.setChecked(deviceModel.isOpenShareSelf());
        cb_meet_control_mic.setChecked(deviceModel.isOpenMicroSelf());
    }

    private void initView() {
        tv_meet_num.setText(meetNum);
        tv_meet_during.setText(meetHour);
        tv_meet_during.setOnClickListener(this);
        tv_meet_num.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        cb_meet_pwd.setOnClickListener(this);
        rg_meet_mode.setOnCheckedChangeListener(this);
        rg_meet_id.setOnCheckedChangeListener(this);
        rg_meet_into_id.setOnCheckedChangeListener(this);
        cb_meet_control_mic.setOnClickListener(this);
        cb_meet_into_open_mic.setOnClickListener(this);
        cb_meet_into_open_camera.setOnClickListener(this);
        cb_meet_into_auto.setOnClickListener(this);

        et_meet_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (!isFocus) {
                    ((SettingsActivity)getActivity()).deviceModel.setSubject(et_meet_name.getText().toString());
                }
            }
        });

        et_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (cb_meet_pwd.isChecked()) {
                    if (!isFocus) {
                        ((SettingsActivity)getActivity()).deviceModel.setPassword(et_password.getText().toString());
                    }
                }
            }
        });
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_meet_during) {
            deviceSpinnerPopup = new DeviceSpinnerPopup(getActivity(), listOfHour, 0);
            deviceSpinnerPopup.setOnSpinnerItemClickListener((string, position) -> {
                tv_meet_during.setText(string);
                float time = 0f;
                if ("不限时".equals(string)) {
                    time = 9999f;
                } else {
                    time = Float.valueOf(string.substring(0, string.indexOf("小")));
                }
                ((SettingsActivity)getActivity()).deviceModel.setDuration(time);
                deviceSpinnerPopup.dismiss();
            });
            deviceSpinnerPopup.showAsDropDown(tv_meet_during);
        } else if (v.getId() == R.id.tv_meet_num) {
            deviceSpinnerPopup = new DeviceSpinnerPopup(getActivity(), listOfPerson, 0);
            deviceSpinnerPopup.setOnSpinnerItemClickListener((string, position) -> {
                tv_meet_num.setText(string);
                int roomCount = Integer.valueOf(string.substring(0, string.indexOf("人")));
                ((SettingsActivity)getActivity()).deviceModel.setRoomCapacity(roomCount);
                deviceSpinnerPopup.dismiss();
            });
            deviceSpinnerPopup.showAsDropDown(tv_meet_num);
        } else if (v.getId() == R.id.btn_save) {
            if (ViewUtils.isFastClick()) {
                return;
            }
            save();
        } else if (v.getId() == R.id.cb_meet_into_open_mic) {
            ((SettingsActivity)getActivity()).deviceModel.setIsSettingMicroOn(cb_meet_into_open_mic.isChecked() ? "on" : "off");
        } else if(v.getId() == R.id.cb_meet_into_open_camera){
            ((SettingsActivity)getActivity()).deviceModel.setIsSettingCameraOn(cb_meet_into_open_camera.isChecked() ? "on" : "off");
        } else if (v.getId() == R.id.cb_meet_into_auto) {
            ((SettingsActivity)getActivity()).deviceModel.setDeviceAuto(cb_meet_into_auto.isChecked());
        } else if (v.getId() == R.id.cb_meet_control_mic) {
            ((SettingsActivity)getActivity()).deviceModel.setOpenMicroSelf(cb_meet_control_mic.isChecked());
        } else if (v.getId() == R.id.cb_meet_pwd) {
            ((SettingsActivity)getActivity()).deviceModel.setUserPsd(cb_meet_pwd.isChecked());
            if (cb_meet_pwd.isChecked()) {
                ((SettingsActivity) getActivity()).deviceModel.setPassword(et_password.getText().toString());
            }
        }
    }

    private void save() {
        if (TextUtils.isEmpty(et_meet_name.getText().toString())) {
            UCToast.show(getActivity(),"请输入会议室名称");
            return;
        }

        if (TextUtils.isEmpty(et_password.getText().toString()) && cb_meet_pwd.isChecked()) {
            UCToast.show(getActivity(),"请输入会议密码");
            return;
        }
        deviceModel.setUserPsd(cb_meet_pwd.isChecked());
        deviceModel.setSubject(et_meet_name.getText().toString());
//        int roomCount = Integer.valueOf(meetNum.substring(0, meetNum.indexOf("人")));
//        if (isSfu) {
//            if (roomCount > Constract.SFU_CAPACITY) {
//                UCToast.show(getActivity(),"SFU模式下最多支持"+Constract.SFU_CAPACITY+"人");
//                return;
//            }
//        }
//        deviceModel.setRoomCapacity(roomCount);
//        float time = 0f;
//        if ("不限时".equals(meetHour)) {
//            time = 9999f;
//        } else {
//            time = Float.valueOf(meetHour.substring(0, meetHour.indexOf("小")));
//        }
//        deviceModel.setDuration(time);
//        if (rg_meet_id.getCheckedRadioButtonId() == R.id.rb_meet_id_self) {      //会议id      ""自动分配
//            deviceModel.setMeetId(SPEditor.instance().getAccount());
//        } else {
//            deviceModel.setMeetId("");
//        }
//        if (rg_meet_into_id.getCheckedRadioButtonId() == R.id.rb_meet_into_host) {    //ID入会 false:仅主持人
//            deviceModel.setAllIdInto(false);
//        } else {
//            deviceModel.setAllIdInto(true);
//        }

        if (rg_meet_share.getCheckedRadioButtonId() == R.id.rb_meet_share_host) { //共享权限 false： 仅主持人
            deviceModel.setAllShare(false);
        } else {
            deviceModel.setAllShare(true);
        }

        if (rg_meet_mode.getCheckedRadioButtonId() == R.id.rb_meet_mode_mcu) {
            deviceModel.setSFUMode(false);
        } else {
            deviceModel.setSFUMode(true);
        }

        deviceModel.setPassword(et_password.getText().toString().trim());
        deviceModel.setIsSettingMicroOn(cb_meet_into_open_mic.isChecked() ? "on" : "off");
        deviceModel.setIsSettingCameraOn(cb_meet_into_open_camera.isChecked() ? "on" : "off");
        deviceModel.setDeviceAuto(cb_meet_into_auto.isChecked());
        deviceModel.setOpenMicroSelf(cb_meet_control_mic.isChecked());
        deviceModel.setOpenShareSelf(cb_meet_control_share.isChecked());
        DeviceSettingManager.getInstance().setModel(deviceModel);
        DeviceSettingManager.getInstance().saveSp();
        UCToast.show(getActivity(),"保存成功");
    }

    public void findFirstFocus() {
        et_meet_name.requestFocus();
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


    /**
     * <p>Called when the checked radio button has changed. When the
     * selection is cleared, checkedId is -1.</p>
     *
     * @param group     the group in which the checked radio button has changed
     * @param checkedId the unique identifier of the newly checked radio button
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.rg_meet_mode) {
            if (checkedId == R.id.rb_meet_mode_mcu) {
                isSfu = false;
            } else {
                isSfu = true;
            }

            changeSpinnerPerson(isSfu);
        } else if (group.getId() == R.id.rg_meet_id) {
            if (checkedId == R.id.rb_meet_id_self) {      //会议id      ""自动分配
                ((SettingsActivity)getActivity()).deviceModel.setMeetId(SPEditor.instance().getAccount());
            } else {
                ((SettingsActivity)getActivity()).deviceModel.setMeetId("");
            }
        } else if (group.getId() == R.id.rg_meet_into_id) {
            if (checkedId == R.id.rb_meet_into_host) {    //ID入会 false:仅主持人
                ((SettingsActivity)getActivity()).deviceModel.setAllIdInto(false);
            } else {
                ((SettingsActivity)getActivity()).deviceModel.setAllIdInto(true);
            }
        }
    }

    public void changeSpinnerPerson(boolean isSfu) {
        int roomCount = Integer.valueOf(meetNum.substring(0, meetNum.indexOf("人")));

        if (isSfu ) {
            if (roomCount > 8) {
                meetNum = "8人";
                tv_meet_num.setText(meetNum);
            }
            listOfPerson = Arrays.asList(getResources().getStringArray(R.array.arrays_meet_num_sfu));
        } else {
            listOfPerson = Arrays.asList(getResources().getStringArray(R.array.arrays_meet_num));
        }
    }
}
