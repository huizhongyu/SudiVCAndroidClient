package cn.closeli.rtc.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.neovisionaries.ws.client.WebSocketState;
import com.vhd.base.util.LogUtil;
import com.vhd.camera.CameraEncoderv2;

import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.closeli.rtc.BaseActivity;
import cn.closeli.rtc.R;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.sdk.IViewCallback;
import cn.closeli.rtc.sdk.ProxyVideoSink;
import cn.closeli.rtc.sdk.WebRTCManager;
import cn.closeli.rtc.utils.ActivityUtils;
import cn.closeli.rtc.utils.SystemUtil;
import me.jessyan.autosize.internal.CustomAdapt;

public class PreviewHdmiActivity extends BaseActivity {
    public static String HDMI_DISCONNECT = "com.vhd.state.hdmiin.disconnect";
    public static String HDMI_CONNECT = "com.vhd.state.hdmiin.connect";
    public static String HDMI_UNKNOWN = "com.vhd.state.hdmiin.unknown";
    @BindView(R.id.fl_preview)
    FrameLayout fl_preview;
    @BindView(R.id.sfv_preview)
    SurfaceViewRenderer sfv_preview;
    @BindView(R.id.iv_support_scale)
    ImageView iv_support_scale;

    private MediaStream mediaStream;
    ProxyVideoSink localSink;
    private int contrast_value, brightness_value, sharpness_value, flicker_index;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictureconfig);
        ActivityUtils.getInstance().addActivity(this);
//        showStatusBar();
        ButterKnife.bind(this);
    }

    //状态栏全屏
    private void showStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    private void initViewCallBack() {
        WebRTCManager.get().addViewCallback(PreviewActivity.class, new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream) {
                if (RoomClient.get().getCurrentActivity() instanceof PreviewHdmiActivity) {
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

    }

    private void inithdmiPreview() {
        int[] idArray = new int[2];
        if (!CameraEncoderv2.hasValidCamera(idArray)) {
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

    private void initHDMI() {
        Log.i("receiverHdmiIn", "receiverHdmiIn" + SystemUtil.isHdmiinConnected());
        if (SystemUtil.isHdmiinConnected()) {
            iv_support_scale.setVisibility(View.GONE);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HDMI_DISCONNECT);
        intentFilter.addAction(HDMI_CONNECT);
        intentFilter.addAction(HDMI_UNKNOWN);
        registerReceiver(receiverHdmiIn, intentFilter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        RoomClient.get().setCurrentActivity(this);
        initViewCallBack();
        initsetSurface();
        inithdmiPreview();
        initHDMI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebRTCManager.get().stopHDMIPreview();
        sfv_preview.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverHdmiIn);
    }

    //时钟接收广播
    private final BroadcastReceiver receiverHdmiIn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("receiverHdmiIn", action);
            //系统每分钟发出
            if (action == HDMI_DISCONNECT) {
                iv_support_scale.setVisibility(View.VISIBLE);
            } else if (action == HDMI_CONNECT) {
                iv_support_scale.setVisibility(View.GONE);
            } else if (action == HDMI_UNKNOWN) {

            }
        }
    };


    @Override
    public void onStateChange(WebSocketState socketState) {

    }
}
