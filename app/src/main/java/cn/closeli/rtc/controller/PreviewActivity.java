package cn.closeli.rtc.controller;

import android.app.admin.DevicePolicyManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocketState;
import com.vhd.base.util.LogUtil;
import com.vhd.camera.CameraEncoderv2;

import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.closeli.rtc.BaseActivity;
import cn.closeli.rtc.LoginActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.constract.BundleKey;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.constract.GlobalValue;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.sdk.IViewCallback;
import cn.closeli.rtc.sdk.ProxyVideoSink;
import cn.closeli.rtc.sdk.WebRTCManager;
import cn.closeli.rtc.utils.ActivityUtils;
import cn.closeli.rtc.utils.CallUtil;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.StringUtils;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ViewUtils;
import cn.closeli.rtc.utils.ext.WorkThreadFactory;
import cn.closeli.rtc.widget.FocusLayout;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.popwindow.MeetSettingPopupWindow;
import me.jessyan.autosize.internal.CustomAdapt;

import static cn.closeli.rtc.constract.GlobalValue.KEY_PANTILT_CONTROL;
import static cn.closeli.rtc.constract.GlobalValue.KEY_PARAMETER;
import static cn.closeli.rtc.constract.GlobalValue.KEY_ROLE_CONTROL;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_DOWN;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_LEFT;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_RIGHT;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_STOP;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_UP;
import static cn.closeli.rtc.constract.GlobalValue.ROLE_DIRECT;
import static cn.closeli.rtc.constract.GlobalValue.ROLE_INDIRECT;
import static cn.closeli.rtc.constract.GlobalValue.ZOOM_STOP;
import static cn.closeli.rtc.constract.GlobalValue.ZOOM_TELE;
import static cn.closeli.rtc.constract.GlobalValue.ZOOM_WIDE;

// 预览页面
public class PreviewActivity extends BaseActivity implements View.OnClickListener {
    private static int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    @BindView(R.id.fl_preview)
    FrameLayout fl_preview;
    @BindView(R.id.sfv_preview)
    SurfaceViewRenderer sfv_preview;
    @BindView(R.id.iv_top)
    ImageView iv_top;
    @BindView(R.id.iv_left)
    ImageView iv_left;
    @BindView(R.id.iv_right)
    ImageView iv_right;
    @BindView(R.id.iv_bottom)
    ImageView iv_bottom;
    @BindView(R.id.constaint)
    ConstraintLayout constaint;
    @BindView(R.id.iv_guide)
    ImageView iv_guide;
    @BindView(R.id.tv_title_pre)
    TextView tv_title_pre;
    @BindView(R.id.constaint_show)
    ConstraintLayout constaint_show;
    @BindView(R.id.tv_key_0)
    TextView tv_key_0;
    @BindView(R.id.tv_key_1)
    TextView tv_key_1;
    @BindView(R.id.tv_key_2)
    TextView tv_key_2;
    @BindView(R.id.tv_key_3)
    TextView tv_key_3;
    @BindView(R.id.tv_key_4)
    TextView tv_key_4;
    @BindView(R.id.tv_key_5)
    TextView tv_key_5;
    @BindView(R.id.tv_key_6)
    TextView tv_key_6;
    @BindView(R.id.tv_key_7)
    TextView tv_key_7;
    @BindView(R.id.tv_key_8)
    TextView tv_key_8;
    @BindView(R.id.tv_key_9)
    TextView tv_key_9;

    private MediaStream mediaStream;
    ProxyVideoSink localSink;
    private boolean isPreviewLocal;     //来源是否是本地预览
    private boolean isControlState = true;     //是否是相机控制状态
    private FocusLayout mFocusLayout;
    private String strOfKeySet;         //当前已经设置的预置位
    private boolean isPreviewSetModel = true;  //是否是预置位设置模式
    private MyHandler h = new MyHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ActivityUtils.getInstance().addActivity(this);
//        StatusBarUtil.setColor(PreviewActivity.this, getResources().getColor(R.color.color_translate));
//        showStatusBar();
        ButterKnife.bind(this);

        isPreviewLocal = getIntent().getBooleanExtra(BundleKey.IS_PREVIEW_LOCAL, true);
        if (isPreviewLocal) {
            DeviceSettingManager.getInstance().setCameraControlSynchro(KEY_PARAMETER, "default");
        }
        strOfKeySet = DeviceSettingManager.getInstance().getFromSp().getCurrentPreset();
    }

    private void initViewCallBack() {
        WebRTCManager.get().addViewCallback(PreviewActivity.class, new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream) {
                if (RoomClient.get().getCurrentActivity() instanceof PreviewActivity) {
                    mediaStream = stream;
                    localSink = new ProxyVideoSink();
                    localSink.setTarget(sfv_preview);
                    mediaStream.videoTracks.get(0).addSink(localSink);
                }

            }

            @Override
            public void onSetLocalScreenStream(MediaStream stream) {

            }

            @Override
            public void onSetLocalHDMIStream(MediaStream stream) {

            }

            @Override
            public void onAddRemoteStream(MediaStream stream, ParticipantInfoModel participant) {

            }

            @Override
            public void onCloseRemoteStream(MediaStream stream, ParticipantInfoModel participant) {

            }

            @Override
            public void onMixStream(ParticipantInfoModel participant, boolean isHasShare) {

            }

            @Override
            public void publishParticipantInfo(ParticipantInfoModel participant) {

            }
        });
    }

    private void initsetSurface() {
        sfv_preview = WebRTCManager.get().setSurface(sfv_preview);
//        surfaceLocal.setZOrderMediaOverlay(true);
        sfv_preview.setMirror(false);
//        if (isPreviewLocal) {
//            sfv_preview.setMirror(true);
//        }

    }

    private void initLocalPreview() {
        ParticipantInfoModel participant = new ParticipantInfoModel();
        participant.setLocalStreamType(0);
        WebRTCManager.get().startPreView(participant);
    }

    private void inithdmiPreview() {
        int[] idArray = new int[2];
        if (!CameraEncoderv2.hasValidCamera(idArray)) {
            Toast.makeText(PreviewActivity.this, "no vhd camera", Toast.LENGTH_LONG).show();
            LogUtil.LOG_WARN("shareScreenHDMI", this.getClass().toString(),
                    "not find vhd camera");
            return;
        } else {
            LogUtil.LOG_WARN("shareScreenHDMI", this.getClass().toString(),
                    "find vhd camera vid:pid(" +
                            String.format("%04x", idArray[0]) + ":" +
                            String.format("%04x", idArray[1]) + ")");
        }

        ParticipantInfoModel participant = new ParticipantInfoModel();
        participant.setLocalStreamType(2);
        WebRTCManager.get().shareHdmi(sfv_preview);
        WebRTCManager.get().startPreView(participant);
    }

    //状态栏全屏
    private void showStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    MeetSettingPopupWindow w;

    /**
     * test code
     **/
    // Fragment 操作 Popup
    public void methodEvent(int type, List<ParticipantInfoModel> list) {
        if (w != null) {
            w.showNextPopup(list);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        L.d("do this ----onKeyDown>>> " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (constaint.getVisibility() == View.VISIBLE && event.getRepeatCount() == 0 && isControlState && isPreviewSetModel) {
                    iv_top.setPressed(true);
                    DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_UP);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (constaint.getVisibility() == View.VISIBLE && event.getRepeatCount() == 0 && isControlState && isPreviewSetModel) {
                    iv_left.setPressed(true);
                    DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_LEFT);


                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (constaint.getVisibility() == View.VISIBLE && event.getRepeatCount() == 0 && isControlState && isPreviewSetModel) {
                    iv_right.setPressed(true);
                    DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_RIGHT);

                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (constaint.getVisibility() == View.VISIBLE && event.getRepeatCount() == 0 && isControlState && isPreviewSetModel) {
                    iv_bottom.setPressed(true);
                    DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_DOWN);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ZOOM_IN:
                if (constaint.getVisibility() == View.VISIBLE) {
                    DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_ZOOM_CONTROL, ZOOM_TELE);
                }
                break;

            case KeyEvent.KEYCODE_ZOOM_OUT:
                if (constaint.getVisibility() == View.VISIBLE) {
                    DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_ZOOM_CONTROL, ZOOM_WIDE);
                }
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    //处理遥控器 按键事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        L.d("do this ---->>>" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DeviceSettingManager.DeviceModel d = DeviceSettingManager.getInstance().getFromSp();
            d.setCurrentPreset(strOfKeySet);
            DeviceSettingManager.getInstance().setModel(d);
            DeviceSettingManager.getInstance().saveSp();
            L.d("do this ---KEYCODE_BACK>>> " + strOfKeySet);
            if (isControlState) {
                isControlState = false;
                DeviceSettingManager.getInstance().setCameraControlSynchro(KEY_ROLE_CONTROL, ROLE_INDIRECT);
            } else {
                DeviceSettingManager.getInstance().setCameraControlSynchro(GlobalValue.KEY_MEMU, "false");
                DeviceSettingManager.getInstance().setCameraControlSynchro(KEY_ROLE_CONTROL, ROLE_INDIRECT);
                finish();
            }
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (constaint.getVisibility() == View.VISIBLE && event.getRepeatCount() == 0 && isControlState && isPreviewSetModel) {
                    iv_top.setPressed(false);
                    DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP);

                    return true;
                }
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (constaint.getVisibility() == View.VISIBLE && event.getRepeatCount() == 0 && isControlState && isPreviewSetModel) {
                    iv_bottom.setPressed(false);
                    DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (constaint.getVisibility() == View.VISIBLE && event.getRepeatCount() == 0 && isControlState && isPreviewSetModel) {
                    iv_left.setPressed(false);
                    DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (constaint.getVisibility() == View.VISIBLE && event.getRepeatCount() == 0 && isControlState && isPreviewSetModel) {
                    iv_right.setPressed(false);
                    DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP);
                    return true;
                }
                break;


            case KeyEvent.KEYCODE_ZOOM_IN:
                if (constaint.getVisibility() == View.VISIBLE) {
                    DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_ZOOM_CONTROL, ZOOM_STOP);
                }
                break;

            case KeyEvent.KEYCODE_ZOOM_OUT:
                if (constaint.getVisibility() == View.VISIBLE) {
                    DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_ZOOM_CONTROL, ZOOM_STOP);
                }
                break;

            case KeyEvent.KEYCODE_MENU:
                if (!isPreviewSetModel) {
                    break;
                }
                if (!isControlState) {
                    isControlState = true;
                    constaint.setVisibility(View.VISIBLE);
                } else {
//                    DeviceSettingManager.getInstance().setCameraControl(KEY_ROLE_CONTROL, ROLE_INDIRECT);
                    DeviceSettingManager.getInstance().setCameraControlSynchro(GlobalValue.KEY_MEMU, "true");
                    DeviceSettingManager.getInstance().setCameraControlSynchro(KEY_ROLE_CONTROL, ROLE_DIRECT);
                    isControlState = false;
//                    constaint.setVisibility(View.GONE);
//                    btn_image_config.setVisibility(View.GONE);
//                    tv_tip_menu.setVisibility(View.VISIBLE);
                }

                break;

            case KeyEvent.KEYCODE_0:
                setMemoryConfig(0);
                break;
            case KeyEvent.KEYCODE_1:
                setMemoryConfig(1);
                break;

            case KeyEvent.KEYCODE_2:
                setMemoryConfig(2);
                break;

            case KeyEvent.KEYCODE_3:
                setMemoryConfig(3);
                break;

            case KeyEvent.KEYCODE_4:
                setMemoryConfig(4);
                break;

            case KeyEvent.KEYCODE_5:
                setMemoryConfig(5);
                break;

            case KeyEvent.KEYCODE_6:
                setMemoryConfig(6);
                break;

            case KeyEvent.KEYCODE_7:
                setMemoryConfig(7);
                break;

            case KeyEvent.KEYCODE_8:
                setMemoryConfig(8);
                break;

            case KeyEvent.KEYCODE_9:
                setMemoryConfig(9);
                break;

            case KeyEvent.KEYCODE_POUND:
                isPreviewSetModel = !isPreviewSetModel;
                tv_title_pre.setVisibility(isPreviewSetModel ? View.GONE : View.VISIBLE);
                constaint_show.setVisibility(isPreviewSetModel ? View.GONE : View.VISIBLE);
                if (!isPreviewSetModel) {
                    showSetPre();
                }
                iv_guide.setImageResource(isPreviewSetModel ? R.drawable.bg_guide_first : R.drawable.bg_guide_second);
                break;

            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    //展示已经设置好的预置位
    private void showSetPre() {
        tv_key_0.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 0) ? View.VISIBLE : View.INVISIBLE);
        tv_key_1.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 1) ? View.VISIBLE : View.INVISIBLE);
        tv_key_2.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 2) ? View.VISIBLE : View.INVISIBLE);
        tv_key_3.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 3) ? View.VISIBLE : View.INVISIBLE);
        tv_key_4.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 4) ? View.VISIBLE : View.INVISIBLE);
        tv_key_5.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 5) ? View.VISIBLE : View.INVISIBLE);
        tv_key_6.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 6) ? View.VISIBLE : View.INVISIBLE);
        tv_key_7.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 7) ? View.VISIBLE : View.INVISIBLE);
        tv_key_8.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 8) ? View.VISIBLE : View.INVISIBLE);
        tv_key_9.setVisibility(StringUtils.isIndexEqual1(strOfKeySet, 9) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RoomClient.get().setCurrentActivity(this);
        initViewCallBack();
        initsetSurface();
        if (isPreviewLocal) {
            initLocalPreview();
        } else {
            inithdmiPreview();
        }
        DeviceSettingManager.getInstance().setCameraControlSynchro(KEY_ROLE_CONTROL, ROLE_INDIRECT);

        mFocusLayout = new FocusLayout(this);
        bindListener();//绑定焦点变化事件
        addContentView(mFocusLayout,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));//添加焦点层
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
    protected void onPause() {
        super.onPause();
        WebRTCManager.get().stopLocalPreview();
        WebRTCManager.get().stopHDMIPreview();
        sfv_preview.release();
        DeviceSettingManager.getInstance().setCameraControlSynchro(KEY_ROLE_CONTROL, ROLE_INDIRECT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        h.removeMessages(100);
        h = null;
    }

    @Override
    public void onStateChange(WebSocketState socketState) {

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.btn_image_config) {
//            btn_image_config.setOnClickListener(null);
//            DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_MEMU, "true");
//            DeviceSettingManager.getInstance().setCameraControl(KEY_ROLE_CONTROL, ROLE_DIRECT);
//            CallUtil.asyncCall(500, () -> {
//                constaint.setVisibility(View.GONE);
//                btn_image_config.setVisibility(View.GONE);
//            });
//        }
    }

    //对对应按键 设置/读取预置位
    private void setMemoryConfig(int key) {
        if (isPreviewSetModel) {
            if (StringUtils.isIndexEqual1(strOfKeySet, key)) {
//                DeviceSettingManager.getInstance().setCameraControl(GlobalValue.MEMORY_RESET, String.valueOf(key));
                DeviceSettingManager.getInstance().setCameraControl(GlobalValue.MEMORY_SET, String.valueOf(key));
            } else {
                DeviceSettingManager.getInstance().setCameraControl(GlobalValue.MEMORY_SET, String.valueOf(key));
                //如果没有，在已有预置位标志上添加此按键键值
                strOfKeySet = String.valueOf(Integer.valueOf(strOfKeySet) + DeviceSettingManager.getInstance().getSaveKeyData(key));
            }
//                showKeyMemoryToast(key);
            Message msg = Message.obtain();
            msg.what = 100;
            msg.arg1 = key;
            h.sendMessage(msg);
        } else {
            if (StringUtils.isIndexEqual1(strOfKeySet, key)) {
                DeviceSettingManager.getInstance().setCameraControl(GlobalValue.MEMORY_RECALL, String.valueOf(key));
            }
        }
    }

    //预置位toast
    private void showKeyMemoryToast(int key) {
        UCToast.show(this, "预置位" + key + "设置成功");
    }

    public class MyHandler extends Handler {
        private WeakReference<PreviewActivity> weakReference;

        public MyHandler(PreviewActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            L.d("do this  --->>>" + msg.what);
            if (msg.what == 100) {
                int key = msg.arg1;
                showKeyMemoryToast(key);
            }
        }
    }

}
