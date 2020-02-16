package cn.closeli.rtc.controller.fragment;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.vhd.base.util.SystemProp;

import cn.closeli.rtc.App;
import cn.closeli.rtc.BuildConfig;
import cn.closeli.rtc.GroupActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.BundleKey;
import cn.closeli.rtc.constract.GlobalValue;
import cn.closeli.rtc.controller.PreviewActivity;
import cn.closeli.rtc.controller.SettingsActivity;
import cn.closeli.rtc.utils.CallUtil;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.LogExtraction;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.SystemUtil;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ViewUtils;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.dialog.InviteJoinDialog;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * 终端设置 - 通用
 */
public class CommonFragment extends Fragment implements CustomAdapt, View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private Button btn_save;
    private TextView tv_common_version;
    private Button btn_common_reset;
    private Button btn_setting_image_config;
    private CheckBox cb_setting_angle;
    private RadioGroup rg_setting_focus;
    private RadioButton rb_setting_focus_auto;
    private RadioButton rb_setting_focus_hand;
    private RadioGroup rg_audio_in;
    private RadioButton rv_audio_in_linein;
    private RadioButton rv_audio_in_mic;
    private RadioGroup rg_audio_out;
    private RadioButton rv_audio_out_hdmi1;
    private RadioButton rv_audio_out_usb;
    private RadioGroup rg_setting_second_screen;
    private RadioButton rb_setting_second_main;
    private RadioButton rb_setting_second_sub;
    private Button btn_common_commit;
    private DeviceSettingManager.DeviceModel deviceModel;
    private ProgressDialog pd;

    private boolean isInvertCamera;
    private int audioOutput;
    private int audioInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common, container, false);
        btn_save = view.findViewById(R.id.btn_save);
        tv_common_version = view.findViewById(R.id.tv_common_version);
        btn_common_reset = view.findViewById(R.id.btn_common_reset);
        cb_setting_angle = view.findViewById(R.id.cb_setting_angle);
        rg_setting_focus = view.findViewById(R.id.rg_setting_focus);
        rg_audio_in = view.findViewById(R.id.rg_audio_in);
        rg_audio_out = view.findViewById(R.id.rg_audio_out);
        btn_setting_image_config = view.findViewById(R.id.btn_setting_image_config);
        rb_setting_focus_auto = view.findViewById(R.id.rb_setting_focus_auto);
        rv_audio_in_linein = view.findViewById(R.id.rv_audio_in_linein);
        rv_audio_out_hdmi1 = view.findViewById(R.id.rv_audio_out_hdmi1);
        rb_setting_focus_hand = view.findViewById(R.id.rb_setting_focus_hand);
        rv_audio_in_mic = view.findViewById(R.id.rv_audio_in_mic);
        rv_audio_out_usb = view.findViewById(R.id.rv_audio_out_usb);
        rg_setting_second_screen = view.findViewById(R.id.rg_setting_second_screen);
        rb_setting_second_main = view.findViewById(R.id.rb_setting_second_main);
        rb_setting_second_sub = view.findViewById(R.id.rb_setting_second_sub);
        btn_common_commit = view.findViewById(R.id.btn_common_commit);
//        ((SettingsActivity) getActivity()).childFragmentViewRequestFocus(this);

        initData();
        initView();
        initFocusLeft();
        return view;
    }

    private void initFocusLeft() {
        btn_save.setNextFocusLeftId(R.id.btn_common_reset);
        btn_common_reset.setNextFocusLeftId(((SettingsActivity) getActivity()).getIdByPosition(0));
        btn_common_reset.setNextFocusRightId(R.id.btn_common_reset);
        btn_common_reset.setNextFocusUpId(R.id.btn_common_reset);
        cb_setting_angle.setNextFocusRightId(R.id.cb_setting_angle);
        cb_setting_angle.setNextFocusLeftId(((SettingsActivity) getActivity()).getIdByPosition(0));
        btn_setting_image_config.setNextFocusRightId(R.id.btn_setting_image_config);
        btn_setting_image_config.setNextFocusLeftId(((SettingsActivity) getActivity()).getIdByPosition(0));
        rb_setting_focus_auto.setNextFocusLeftId(((SettingsActivity) getActivity()).getIdByPosition(0));
        rb_setting_focus_hand.setNextFocusRightId(R.id.rb_setting_focus_hand);
        rv_audio_in_linein.setNextFocusLeftId(((SettingsActivity) getActivity()).getIdByPosition(0));
        rv_audio_in_mic.setNextFocusRightId(R.id.rv_audio_in_mic);
        rv_audio_out_hdmi1.setNextFocusLeftId(((SettingsActivity) getActivity()).getIdByPosition(0));
        rv_audio_out_usb.setNextFocusRightId(R.id.rv_audio_out_usb);
        rb_setting_second_main.setNextFocusLeftId(((SettingsActivity) getActivity()).getIdByPosition(0));
        rb_setting_second_sub.setNextFocusRightId(R.id.rb_setting_second_sub);
        btn_common_commit.setNextFocusLeftId(((SettingsActivity) getActivity()).getIdByPosition(0));
        btn_common_commit.setNextFocusRightId(R.id.btn_common_commit);
    }

    private void initView() {
        tv_common_version.setText(BuildConfig.VERSION_NAME);
        isInvertCamera = deviceModel.isInvertCamera();
        cb_setting_angle.setChecked(isInvertCamera);

        //摄像头聚焦
        if (deviceModel.isCameraFocusAuto()) {
            rg_setting_focus.check(R.id.rb_setting_focus_auto);
        } else {
            rg_setting_focus.check(R.id.rb_setting_focus_hand);
        }

        //音频输入
        if (deviceModel.getIndexAudioInput() == 0) {
            rg_audio_in.check(R.id.rv_audio_in_linein);
        } else if (deviceModel.getIndexAudioInput() == 1) {
            rg_audio_in.check(R.id.rv_audio_in_usb);
        } else {
            rg_audio_in.check(R.id.rv_audio_in_mic);
        }
        //音频输出

        if (deviceModel.getIndexAudioOutput() == 0) {
            rg_audio_out.check(R.id.rv_audio_out_hdmi1);
        } else if (deviceModel.getIndexAudioOutput() == 1) {
            rg_audio_out.check(R.id.rv_audio_out_hdmi2);
        } else if (deviceModel.getIndexAudioOutput() == 2) {
            rg_audio_out.check(R.id.rv_audio_out_lineout);
        } else {
            rg_audio_out.check(R.id.rv_audio_out_usb);
        }

        if (deviceModel.isScreenMain()) {
            rg_setting_second_screen.check(R.id.rb_setting_second_main);
        } else {
            rg_setting_second_screen.check(R.id.rb_setting_second_sub);
        }

        btn_common_commit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((SettingsActivity) getActivity()).setCommonItemChecked();
                }
            }
        });

        rv_audio_in_linein.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((SettingsActivity) getActivity()).setCommonItemChecked();
                }
            }
        });
    }

    private void initData() {
        deviceModel = ((SettingsActivity)getActivity()).deviceModel;


        rg_setting_second_screen.setOnCheckedChangeListener(this);
        rg_audio_in.setOnCheckedChangeListener(this);
        rg_audio_out.setOnCheckedChangeListener(this);

        btn_save.setOnClickListener(this);
        btn_common_reset.setOnClickListener(this);
        cb_setting_angle.setOnClickListener(this);
        btn_common_commit.setOnClickListener(this);
        btn_setting_image_config.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save) {
            if (ViewUtils.isFastClick()) {
                return;
            }
            save();
        } else if (v.getId() == R.id.btn_common_reset) {
            InviteJoinDialog dialog = InviteJoinDialog.newInstance();
            dialog.setTitle("是否要恢复出厂设置?");
            dialog.setStrComfirm("确定");
            dialog.setStrCancel("取消");
            dialog.setOnDialogClick(new InviteJoinDialog.OnDialogClick() {
                @Override
                public void onClick(InviteJoinDialog orderCancelDialog, int position) {
                    if (position == 1) {
                        DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_RESET, GlobalValue.RESET_START);
//                        DeviceSettingManager.DeviceModel dm = DeviceSettingManager.getInstance().getFromSp();
//                        dm.setCurrentPreset("0");
//                        DeviceSettingManager.getInstance().setModel(dm);
//                        DeviceSettingManager.getInstance().saveSp();
                        SPEditor.instance().clearAll();
                        startResetThread();
                        orderCancelDialog.dismiss();
                    } else {
                        orderCancelDialog.dismiss();
                    }
                }
            });
            dialog.show(getChildFragmentManager(),"comfirm");

        } else if (v.getId() == R.id.cb_setting_angle) {
            isInvertCamera = cb_setting_angle.isChecked();
            cb_setting_angle.setChecked(isInvertCamera);
            ((SettingsActivity)getActivity()).deviceModel.setInvertCamera(isInvertCamera);
        } else if (v.getId() == R.id.btn_common_commit) {
            pd = new ProgressDialog(getContext());
            pd.setTitle("日志提取");
            pd.setMessage("日志提取中，请稍后……");
//            pd = ProgressDialog.show(getActivity(), "日志提取", "日志提取中，请稍后……");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(100);
            pd.setCancelable(true);
            pd.show();
            LogExtraction.extractLog2UDisk(getContext(), new LogExtraction.OnExtractResult() {
                @Override
                public void onSuccess() {
                    getActivity().runOnUiThread(() -> {
                        UCToast.show(getContext(), "日志提取成功！");
                        pd.dismiss();
                    });

                }

                @Override
                public void onError(String msg) {
                    getActivity().runOnUiThread(() -> {
                        UCToast.show(getContext(), msg);
                        pd.dismiss();
                    });
                }

                @Override
                public void onProgress(int progress) {
                    pd.setProgress(progress);
                }
            });
        } else if (v.getId() == R.id.btn_setting_image_config) {
            if (!ViewUtils.isFastClick()) {
                Intent intent = new Intent(getActivity(), PreviewActivity.class);
                intent.putExtra(BundleKey.IS_PREVIEW_LOCAL, true);
                startActivity(intent);
            }
        }
    }

    private void save() {
//        if (rg_audio_in.getCheckedRadioButtonId() == R.id.rv_audio_in_linein) {           //音频输入
//            deviceModel.setIndexAudioInput(0);
//            audioInput = 0;
//        } else if (rg_audio_in.getCheckedRadioButtonId() == R.id.rv_audio_in_usb) {
//            deviceModel.setIndexAudioInput(1);
//            audioInput = 1;
//        } else {
//            deviceModel.setIndexAudioInput(2);
//            audioInput = 2;
//        }
//
//        if (rg_audio_out.getCheckedRadioButtonId() == R.id.rv_audio_out_hdmi1) {          //音频输出
//            deviceModel.setIndexAudioOutput(0);
//            audioOutput = 0;
//        } else if (rg_audio_out.getCheckedRadioButtonId() == R.id.rv_audio_out_hdmi2) {
//            deviceModel.setIndexAudioOutput(1);
//            audioOutput = 1;
//        } else if (rg_audio_out.getCheckedRadioButtonId() == R.id.rv_audio_out_lineout) {
//            deviceModel.setIndexAudioOutput(2);
//            audioOutput = 2;
//        } else {
//            deviceModel.setIndexAudioOutput(3);
//            audioOutput = 3;
//        }

        if (rg_setting_focus.getCheckedRadioButtonId() == R.id.rb_camera_focus_auto) {       //摄像头聚焦
            deviceModel.setCameraFocusAuto(true);
        } else {
            deviceModel.setCameraFocusAuto(false);
        }

//        if (rg_setting_second_screen.getCheckedRadioButtonId() == R.id.rb_setting_second_main) {    //设置辅流主屏显示
//            deviceModel.setScreenMain(true);
//        } else {
//            deviceModel.setScreenMain(false);
//        }

//        deviceModel.setInvertCamera(isInvertCamera);

        DeviceSettingManager.getInstance().setModel(deviceModel);
//        setConfigs();
        DeviceSettingManager.getInstance().saveSp();
        UCToast.show(getContext(), "保存成功！");
    }

    //执行相关设置 操作
//    private void setConfigs() {
////        DeviceSettingManager.getInstance().setCameraControl(KEY_PARAMETER, "default");
//        DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_INVERSION, isInvertCamera ? "2" : "3");     //open:2  close:3
//        if (audioInput == 0) {
//            SystemProp.setProperty("vhd.audio.micin.force", "3");
//        } else if (audioInput == 1) {
//            SystemProp.setProperty("vhd.audio.micin.force", "2");
//        } else {
//            if (SystemUtil.getMtkVersion().equals("v0.0.3")) {
//                SystemProp.setProperty("vhd.audio.micin.force", "1");
//            } else {
//                SystemProp.setProperty("vhd.audio.micin.force", "4");
//            }
//        }
//
//        if (audioOutput == 0) {
//            SystemProp.setProperty("vhd.hdmi1.audio.mute", "0");
//            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
//            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
//            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
//        } else if (audioOutput == 1) {
//            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
//            SystemProp.setProperty("vhd.hdmi2.audio.mute", "0");
//            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
//            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
//        } else if (audioOutput == 2) {
//            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
//            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
//            SystemProp.setProperty("vhd.lineout.audio.mute", "0");
//            SystemProp.setProperty("vhd.usbout.audio.mute", "1");
//        } else {
//            SystemProp.setProperty("vhd.hdmi1.audio.mute", "1");
//            SystemProp.setProperty("vhd.hdmi2.audio.mute", "1");
//            SystemProp.setProperty("vhd.lineout.audio.mute", "1");
//            SystemProp.setProperty("vhd.usbout.audio.mute", "0");
//        }
//    }

    //恢复出厂设置
    private void startResetThread() {
        pd = ProgressDialog.show(getActivity(), "恢复出厂设置", "恢复中，请稍后……");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pd.dismiss();
                DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_RESET, GlobalValue.RESET_END);
                SPEditor.instance().clearAll();
                Looper.prepare();
                App.post(()-> {
                    UCToast.show(getContext(),"设备即将重启...");
                });
                CallUtil.asyncCall(500,()-> {

                    Intent intent_reboot = new Intent();
                    intent_reboot.setAction("com.vhd.system.REBOOT");
                    getContext().sendBroadcast(intent_reboot);
                });
                Looper.loop();
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void findFitstFocus() {
        btn_common_reset.requestFocus();
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

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (radioGroup.getId() == R.id.rg_setting_second_screen) {
            if (rg_setting_second_screen.getCheckedRadioButtonId() == R.id.rb_setting_second_main) {    //设置辅流主屏显示
                ((SettingsActivity)getActivity()).deviceModel.setScreenMain(true);
            } else {
                ((SettingsActivity)getActivity()).deviceModel.setScreenMain(false);
            }
        } else if (radioGroup.getId() == R.id.rg_audio_in) {
            if (rg_audio_in.getCheckedRadioButtonId() == R.id.rv_audio_in_linein) {           //音频输入
                ((SettingsActivity)getActivity()).deviceModel.setIndexAudioInput(0);
            } else if (rg_audio_in.getCheckedRadioButtonId() == R.id.rv_audio_in_usb) {
                ((SettingsActivity)getActivity()).deviceModel.setIndexAudioInput(1);
            } else {
                ((SettingsActivity)getActivity()).deviceModel.setIndexAudioInput(2);
            }

        } else if (radioGroup.getId() == R.id.rg_audio_out) {
            if (rg_audio_out.getCheckedRadioButtonId() == R.id.rv_audio_out_hdmi1) {          //音频输出
                ((SettingsActivity)getActivity()).deviceModel.setIndexAudioOutput(0);
            } else if (rg_audio_out.getCheckedRadioButtonId() == R.id.rv_audio_out_hdmi2) {
                ((SettingsActivity)getActivity()).deviceModel.setIndexAudioOutput(1);
            } else if (rg_audio_out.getCheckedRadioButtonId() == R.id.rv_audio_out_lineout) {
                ((SettingsActivity)getActivity()).deviceModel.setIndexAudioOutput(2);
            } else {
                ((SettingsActivity)getActivity()).deviceModel.setIndexAudioOutput(3);
            }
        }
    }
}
