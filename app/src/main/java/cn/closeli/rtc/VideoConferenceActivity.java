
package cn.closeli.rtc;

import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.neovisionaries.ws.client.WebSocketState;
import com.vhd.base.util.LogUtil;
import com.vhd.camera.CameraEncoderv2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.constract.GlobalValue;
import cn.closeli.rtc.constract.WsErrorCode;
import cn.closeli.rtc.model.EventPublish;
import cn.closeli.rtc.model.PadModel;
import cn.closeli.rtc.model.ParticipantModel;
import cn.closeli.rtc.model.ReplaceRollcallModel;
import cn.closeli.rtc.model.RoomLayoutModel;
import cn.closeli.rtc.model.RoomTimeModel;
import cn.closeli.rtc.model.SlingModel;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.GroupInfoModel;
import cn.closeli.rtc.model.info.GroupListInfoModel;
import cn.closeli.rtc.model.info.PartDeviceModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.rtc.UserDeviceList;
import cn.closeli.rtc.model.ws.AudioStstusResp;
import cn.closeli.rtc.model.ws.HandsStatusResp;
import cn.closeli.rtc.model.ws.PresetInfoResp;
import cn.closeli.rtc.model.ws.VideoStatusResp;
import cn.closeli.rtc.model.ws.WsError;
import cn.closeli.rtc.net.ServerManager;
import cn.closeli.rtc.room.PadEvent;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.RoomEventAdapter;
import cn.closeli.rtc.room.RoomEventCallback;
import cn.closeli.rtc.room.WssMethodNames;
import cn.closeli.rtc.sdk.CLRtcSinaling;
import cn.closeli.rtc.sdk.IViewCallback;
import cn.closeli.rtc.sdk.ProxyVideoSink;
import cn.closeli.rtc.sdk.WebRTCManager;
import cn.closeli.rtc.utils.ActivityUtils;
import cn.closeli.rtc.utils.CallUtil;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.Operation;
import cn.closeli.rtc.utils.PTZControlUtil;
import cn.closeli.rtc.utils.RoomControl;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.StringUtils;
import cn.closeli.rtc.utils.SystemUtil;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ViewUtils;
import cn.closeli.rtc.utils.data_manager.VideoControlManager;
import cn.closeli.rtc.widget.BoundShadowLayout;
import cn.closeli.rtc.widget.DifferentDislay;
import cn.closeli.rtc.widget.DynamicVideoLayout;
import cn.closeli.rtc.widget.DynamicVideoLayout_;
import cn.closeli.rtc.widget.FocusLayout;
import cn.closeli.rtc.widget.ParticipantLayout;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.VideoController;
import cn.closeli.rtc.widget.VideoLayout;
import cn.closeli.rtc.widget.dialog.InviteJoinDialog;
import cn.closeli.rtc.widget.popwindow.HostInfoPopupWindow;
import cn.closeli.rtc.widget.popwindow.InviteJoinInPopupWindow;
import cn.closeli.rtc.widget.popwindow.LayoutChoosePopupWindow;
import cn.closeli.rtc.widget.popwindow.MeetSettingPopupWindow;
import cn.closeli.rtc.widget.popwindow.NetPopupWindow;
import cn.closeli.rtc.widget.popwindow.ParticipantPopWindow;
import cn.closeli.rtc.widget.popwindow.RollCallPopupWindow;
import cn.closeli.rtc.widget.popwindow.TimeDownPopupWindow;
import cn.closeli.rtc.widget.popwindow.VideoItemPopWindow;
import me.jessyan.autosize.internal.CustomAdapt;

import static cn.closeli.rtc.constract.Constract.MIX_MAJOR_AND_SHARING;
import static cn.closeli.rtc.constract.Constract.change;
import static cn.closeli.rtc.constract.Constract.replace;
import static cn.closeli.rtc.constract.GlobalValue.KEY_PANTILT_CONTROL;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_DOWN;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_LEFT;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_RIGHT;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_STOP;
import static cn.closeli.rtc.constract.GlobalValue.PANTILT_UP;
import static cn.closeli.rtc.constract.GlobalValue.ZOOM_STOP;
import static cn.closeli.rtc.constract.GlobalValue.ZOOM_TELE;
import static cn.closeli.rtc.constract.GlobalValue.ZOOM_WIDE;
import static cn.closeli.rtc.sdk.WebRTCManager.MAJOR;
import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER;
import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER;
import static cn.closeli.rtc.sdk.WebRTCManager.SHARING;
import static cn.closeli.rtc.utils.Constants.LOCAL_SCREEN;
import static cn.closeli.rtc.utils.Constants.STREAM_MAJOR;
import static cn.closeli.rtc.utils.Constants.STREAM_SHARING;

public class VideoConferenceActivity extends BaseActivity implements DisplayManager.DisplayListener, VideoController.VideoPlayerControl, RoomClient.OnLocalShareJoinListener {
    //屏幕共享 requestCode
    private String logTag = this.getClass().getCanonicalName();
    //key-value -> userId-FrameLayout
    private Map<String, ProxyVideoSink> _sinks = new HashMap<>();

    //本地视频 ViewGroup
    @BindView(R.id.local_video_container)
    DynamicVideoLayout_ localVideoViewContainer;
    //本地视频 RelativeLayout
    @BindView(R.id.local_screen_container_out)
    RelativeLayout relativeLayout;


    @BindView(R.id.cl_main)
    ConstraintLayout cl_main;


    @BindView(R.id.shareHDMI)
    TextView shareHDMI;

    @BindView(R.id.all_frameLayout)
    FrameLayout all_frameLayout;

    @BindView(R.id.video_controller)
    VideoController videoController;

    private WebRTCManager webRTCManager;
    //本地视频 SurfaceView 视频源 摄像头
    private SurfaceViewRenderer localHDMIVideoView;
    //分屏dialog
    DifferentDislay mPresentation;
    //屏幕管理类
    DisplayManager mDisplayManager;
    private String connectId;
    private String userId;
    private String channelId;
    private String channelRole;

    private ParticipantPopWindow participantPopWindow;
    @Nullable
    private VideoItemPopWindow videoItemPopWindow;
    private NetPopupWindow netPopupWindow;
    /*本地基本属性初始化*/
    private boolean enableShare = true; //共享
    private boolean isLocalHas = false; //本地屏幕是否有人，回调addRemote需要
    private boolean isShareHas = false; //分享屏幕是否有人，回调addRemote需要
    private boolean isAllScreen = false; //是否全屏
    private boolean isShareIng = false; //本人是否正在共享
    private String screenId = ""; //加入房间后的主页面.用于换人
    private String prensatationId = ""; //分屏的异屏显示Id;

    /**
     * 与会者 集合
     */
    private List<ParticipantInfoModel> participantModels;
    private List<ParticipantInfoModel> participantInfoModelsNoShare;
    private ArrayList<ParticipantInfoModel> tempParticpantModels = new ArrayList<>();
    private FrameLayout.LayoutParams surfaceParams;
    private LinearLayout.LayoutParams localParams; //当前预览参数
    private FrameLayout locaHDMIFramenLayout;
    private boolean supportHdmiIn = false;
    private MeetSettingPopupWindow meetSettingPopupWindow;

    private int choosePosition = Constract.AUDIO_STAND;

    private boolean useCamEncoded = false;
    private String lastBreakLineUserId = "";
    private ArrayList<String> queues = new ArrayList<>();
    private String currentSpeakUserId = "";
    private String currentShareUser = "";        //当前正在发言的人 名称
    private RoomLayoutModel roomLayoutModel;
    private ArrayList<String> roomLayouts = new ArrayList<>();
    private String strOfKeySet;
    private boolean isCurrentAllScreenSelf = true;          //当前全屏操作的是视图，是否是自己
    private SurfaceViewRenderer presentationHDMIVideoView;
    private String currentAllscreenId = null;           //当前全屏的ConnectionId
    private ParticipantInfoModel currentShareModel = null;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    setDuringTime();
                    handler.sendMessageDelayed(obtainMessage(1), 1000);
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityUtils.getInstance().addActivity(this);

        mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.registerDisplayListener(this, null);
        initPresentation(false);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
//        showStatusBar();
        StatusBarUtil.setColor(VideoConferenceActivity.this, getResources().getColor(R.color.color_translate));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        userId = SPEditor.instance().getUserId();
        connectId = RoomManager.get().getConnectionId();

        channelId = RoomClient.get().getRoomId();
        channelRole = intent.getStringExtra("channelRole");

        RoomManager.get().setUserId(userId);
        RoomManager.get().setRole(channelRole);
        webRTCManager = WebRTCManager.get();
        start();
        //会议 回调
        RoomClient.get().addRoomEventCallback(VideoConferenceActivity.class, roomEventCallback);
        RoomClient.get().addRoomEventCallback(PTZControlUtil.class, PTZControlUtil.INSTANCE);
        RoomClient.get().setOnLocalShareJoinListener(this);
        ServerManager.get().setPadEvent(padEvent);
        participantModels = getParticipant();
        RoomManager.get().setRoomId(channelId);
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        //屏幕高度 = 整个屏高 - top bar 高度 - bot bar 高度

        initVideoControl();

        //设置surfaceView 参数
        surfaceParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        supportHdmiIn = SystemUtil.getSystemModel().equals("C9Z");
        if (supportHdmiIn) {
        } else {
            videoController.setHDMIStatus(View.GONE);
            useCamEncoded = false;
        }


//        localVideoViewContainer.setInfoModels(participantModels, DynamicVideoLayout_.MODE_1);
        localVideoViewContainer.setInfoModels(participantModels);
        localVideoViewContainer.setOnItemVideoClickListener(this::showVideoItem);


        strOfKeySet = DeviceSettingManager.getInstance().getFromSp().getCurrentPreset();
        Message msg = Message.obtain();
        msg.what = 1;
        handler.sendMessage(msg);

        if (!RoomManager.get().isHost()) {
            localVideoViewContainer.setDefaultLayout(false);
        }
        EventBus.getDefault().register(this);
    }

    //根据当前participantModels 的顺序，获取connectionId并广播
    private void firstLayoutParams() {
        if (RoomManager.get().isHost()) {
            ArrayList<String> connectIds = new ArrayList<>();
            for (ParticipantInfoModel model : participantModels) {
                connectIds.add(model.getConnectionId());
            }

            for (String s : connectIds) {
                Log.i("majorLayoutNotify", "firstLayoutParams......" + s);
            }

            if (mPresentation != null && mPresentation.isShowing() && currentShareModel != null && isShareHas == true) {
                connectIds.add(0, currentShareModel.getConnectionId());
            }

            RoomClient.get().getBroadcastMajorLayout(localVideoViewContainer.getMode(), connectIds, "change");
        }
    }

    /**
     * WebRtc
     * Web Real-Time Communication
     */
    public void start() {
        webRTCManager.create();
        webRTCManager.initWebRtc(channelId, userId, channelRole, useCamEncoded);
        if (channelRole.equals(ROLE_PUBLISHER) || channelRole.equals(ROLE_PUBLISHER_SUBSCRIBER)) {
            ParticipantInfoModel localParticipant = RoomClient.get().getLocalParticipant();

            webRTCManager.createOffer(localParticipant);
        }
        webRTCManager.addViewCallback(VideoConferenceActivity.class, new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("addNewView", "onSetLocalStream " + RoomClient.get().isHasHands() + " , " + channelRole + ", " + RoomClient.get().isHasHands());
//                        ServerManager.get().participantLocalJoin(RoomClient.get().getLocalParticipant());
                        //不是主持人， 就添加到小窗口
                        if (!channelRole.equals(ROLE_PUBLISHER_SUBSCRIBER)) {
                            addOtherView(userId, stream, true, RoomClient.get().getLocalParticipant());
                        } else {            //如果当前有人正在发言，就添加到小窗口
                            if (RoomClient.get().isHasHands()) {
                                addOtherView(userId, stream, true, RoomClient.get().getLocalParticipant());
                            } else {
                                addNewView(userId, stream, RoomClient.get().getLocalParticipant());
                            }
                        }

                        if (RoomManager.get().isHost()) {
                            RoomClient.get().getRoomLayout(RoomManager.get().getRoomId());
                        }

//                        if (_sinks.size() == participantModels.size()) {
//                            L.d("do this ---addOtherView ---addOtherView ------layoutChange");
//                            if (!RoomManager.get().isHost()) {
//                                layoutChange(RoomManager.get().getRoomLayoutModel());
//                            } else {
//                                layoutChangeHost();
//                            }
//                        }
                    }
                });
            }

            /**
             *
             * @param stream
             */
            @Override
            public void onSetLocalScreenStream(MediaStream stream) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }

            /**
             *
             * @param stream
             */
            @Override
            public void onSetLocalHDMIStream(MediaStream stream) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("addNewlocalhdmiView", "onSetLocalScreenStream");

                    }
                });
            }

            @Override
            public void onAddRemoteStream(MediaStream stream, ParticipantInfoModel participant) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });

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
        if (RoomManager.get().isHost()) {
            roomLayouts.clear();
        }
        parseParticapantsInRooms();
    }


    //添加到 主屏（大窗口）
    private void addNewView(String id, MediaStream stream, ParticipantInfoModel participant) {
        isLocalHas = true;
        if (TextUtils.isEmpty(screenId)) {
            screenId = id;
        }
        if (RoomManager.get().isHost()) {
            if (!queues.contains(id)) {
                queues.add(0, id);
                queueSend();
            }

        }
        L.d("onAddRemoteStreamsNew >>>" + userId + " , " + participant.getUserId() + " , streamType : " + participant.getAppShowName());
        // set render
        ProxyVideoSink sink = new ProxyVideoSink();
        participantModels = getParticipant();
        participant.setVideoSink(sink);
        localVideoViewContainer.notifyItemChanged(participant);
        if (stream.videoTracks.size() > 0) {
            stream.videoTracks.get(0).addSink(sink);
        }
//        localVideoViewContainer.notifyItemsChanged();
        _sinks.put(id, sink);

    }


    //添加到小窗口
    private void addOtherView(String id, MediaStream stream, boolean isLocal, ParticipantInfoModel participant) {
        if (RoomManager.get().isHost()) {
            if (!queues.contains(id)) {
                queues.add(id);
                queueSend();
            }

        }
        L.d("onAddRemoteStreams >>>" + userId + " , " + participant.getUserId() + " , streamType : " + participant.getAppShowName() + "connectId" + participant.getConnectionId());
        Log.i("addNewOtherView", "addNewOtherView" + participant.getConnectionId());
        ProxyVideoSink sink = new ProxyVideoSink();
        participantModels = getParticipant();
        /**
         * 画中画基本功能 切换， 细节需要调试
         */

        if (participantModels != null) {

            for (ParticipantInfoModel participantInfoModel : participantModels) {
                if (participant.getConnectionId().equals(participantInfoModel.getConnectionId())) {
                    participantInfoModel.setVideoSink(sink);
                    break;
                }
            }
        }
        localVideoViewContainer.notifyItemChanged(participant);
        if (stream.videoTracks.size() > 0) {
            stream.videoTracks.get(0).addSink(sink);
        }

//        localVideoViewContainer.notifyItemsChanged();
        Log.i("surfaceView", "addOther:" + id + ":" + participant.isVideoActive() + " , " + participant.getOnlineStatus());

        _sinks.put(id, sink);

    }

    private void addOtherHDMIView(String id, MediaStream stream, boolean isLocal, ParticipantInfoModel participant) {
        isShareHas = true;
        Log.i("addNewShare", "addNewShare" + id);
        if (participant != null) {
            currentShareUser = participant.getAppShowName();
        }
        prensatationId = id;
        if (RoomManager.get().isHost()) {
            queueSend();
        }
        // set render
        ProxyVideoSink sink = new ProxyVideoSink();

        /**
         * 画中画基本功能 切换， 细节需要调试
         */
        FrameLayout fl = new FrameLayout(this);

        LinearLayout.LayoutParams viewflp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        fl.setLayoutParams(viewflp);
        viewflp.gravity = Gravity.CENTER;

        SurfaceViewRenderer surfaceView = webRTCManager.createRendererView(getApplication());

        surfaceView.setTag(id);
        surfaceView.setLayoutParams(viewflp);

        //surfaceview 修改  自适配的时候得改回去
        surfaceView.setLayoutParams(surfaceParams);

        sink.setTarget(surfaceView);
        participant.setVideoSink(sink);
        participantModels = getParticipant();
        currentShareModel = participant;

        if (mPresentation != null && mPresentation.isShowing()) {
            surfaceView.setZOrderMediaOverlay(true);
//            presentationHDMIVideoView = surfaceView;
            mPresentation.getFrameLayout().addView(surfaceView, 0);
            mPresentation.getImageView().setVisibility(View.GONE);
        } else {
            localVideoViewContainer.notifyItemChanged(participant);
        }


        //更新当前正在共享的与会者
        for (ParticipantInfoModel m : participantModels) {
            if (participant.getUserId().equals(m.getUserId()) && !SHARING.equals(m.getStreamType())) {
                m.setShareStatus("on");
                localVideoViewContainer.notifyVideoStateChanged(m);
            }
        }

        //需要添加的验证数据
        if (stream.videoTracks.size() > 0) {
            stream.videoTracks.get(0).addSink(sink);
        }

        if (mPresentation != null && mPresentation.isShowing()) {

        } else {
            _sinks.put(id, sink);
        }
        L.d("do this ----addOtherHdmi --11111---- : " + RoomManager.get().getRoomLayoutModel().getLayout().size() + " , participantModels.size():" + participantModels.size() + " , " + RoomManager.get().isHost());
    }


    public void initHDMISurface() {
        localHDMIVideoView = webRTCManager.createRendererView(getApplicationContext());
//        localHDMIVideoView = webRTCManager.createNoEGLRendererView(getApplication());
        localHDMIVideoView.setLayoutParams(surfaceParams);
//        localHDMIVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//        localHDMIVideoView.setMirror(true);
    }

    public void initHDMISurface(SurfaceViewRenderer surfaceViewRenderer) {
        localHDMIVideoView = surfaceViewRenderer;
//        localHDMIVideoView = webRTCManager.createNoEGLRendererView(getApplication());
        localHDMIVideoView.setLayoutParams(surfaceParams);
//        localHDMIVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//        localHDMIVideoView.setMirror(true);
    }

    public void initHDMIFrameLayout() {
        localParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        locaHDMIFramenLayout = new FrameLayout(this);
        locaHDMIFramenLayout.setBackground(getDrawable(R.drawable.maininterface_list_video_frame));
        locaHDMIFramenLayout.setPadding(1, 1, 1, 1);
        localParams.gravity = Gravity.CENTER;
        locaHDMIFramenLayout.setLayoutParams(localParams);
        locaHDMIFramenLayout.addView(localHDMIVideoView);
    }

    public int[] measureView(final View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        int widthSpec = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
        int lpHeight = lp.height;
        int heightSpec;
        if (lpHeight > 0) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY);
        } else {
            heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }
        view.measure(widthSpec, heightSpec);
        return new int[]{view.getMeasuredWidth(), view.getMeasuredHeight()};
    }

    /**
     * 解析: 房间内 已有 与会者
     */
    private void parseParticapantsInRooms() {
        ArrayList<ParticipantInfoModel> participants = RoomClient.get().getRemoteParticipants();
        if (participants == null) {
            return;
        }
        for (ParticipantInfoModel tmpPart : participants) {
            if (!tmpPart.getConnectionId().equals(connectId)) {
                webRTCManager.onParticipantPublished(tmpPart);
            }

        }
    }

    /**
     * 离开 按钮 点击事件
     *
     * @param view
     */
    public void onHangup(View view) {
        finishAndRelease();
    }

    private void finishAndRelease() {
        VideoControlManager.getInstance().finishAndReleaseDialog(this, () -> {
            doHangup();
            leaveRoom();
            finish();
            RoomClient.get().setShowRollCall(false);
            RoomClient.get().setHasHands(false);
            lastBreakLineUserId = "";
        });
    }

    private void doHangup() {
        if (webRTCManager != null) {
            webRTCManager.leaveChannel();
            if (!shareHDMI.getText().equals("共享屏幕")) {
                webRTCManager.stopShareHdmi();
                RoomClient.get().leaveRoom(userId, channelId, STREAM_SHARING);
            }
        }
        webRTCManager.doDestroy(VideoConferenceActivity.class);
        localVideoViewContainer.removeAllViews();
        _sinks.clear();
        closePresentation();
        RoomClient.get().setLocalHdmiParticipant(null);
        RoomClient.get().setLocalScreenParticipant(null);
        RoomClient.get().setLocalParticipant(null);
        RoomClient.get().getRemoteParticipant().clear();
        RoomClient.get().removeRoomEventCallback(VideoConferenceActivity.class);
        RoomClient.get().setOnLocalShareJoinListener(null);
    }

    public void leaveRoom() {
        if (!channelRole.equals(ROLE_PUBLISHER_SUBSCRIBER)) {
            RoomClient.get().leaveRoom(userId, channelId, STREAM_MAJOR);
        } else {
            RoomClient.get().closeRoom(channelId);
        }
    }

    public void switchCamera(View view) {
        webRTCManager.switchCamera();
    }


    public void roomSetting(View view) {
        boolean isHost = SPEditor.instance().getUserId().equals(RoomManager.get().getHostId());
        meetSettingPopupWindow = new MeetSettingPopupWindow(this);
        meetSettingPopupWindow.showAsLeft(view);
        meetSettingPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                meetSettingPopupWindow = null;
                videoController.scheduleHide();
            }
        });
        CallUtil.asyncCall(400,()-> {
                videoController.reset();
        });

    }

    public void allScreen(View view) {
        isAllScreen = true;
        //SurfaceView 放大
        SurfaceViewRenderer surfaceViewRenderer = WebRTCManager.get().createRendererView(getApplicationContext());
        surfaceViewRenderer.setZOrderMediaOverlay(true);
        removeParent(surfaceViewRenderer);
        all_frameLayout.addView(surfaceViewRenderer, 0);

    }

    public void allScreen(String connectionId) {
        currentAllscreenId = connectionId;
        isAllScreen = true;
        //SurfaceView 放大
        SurfaceViewRenderer surfaceViewRenderer = localVideoViewContainer.getSurfaceViewRender(currentAllscreenId);
        surfaceViewRenderer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        surfaceViewRenderer.setZOrderMediaOverlay(true);
        all_frameLayout.addView(surfaceViewRenderer, 0);
        PTZControlUtil.INSTANCE.start(connectionId);
    }

    public void resetScreen(View view) {
        isAllScreen = false;
        SurfaceViewRenderer surfaceViewRenderer = null;
        if (all_frameLayout.getChildAt(0) instanceof SurfaceViewRenderer) {
            surfaceViewRenderer = (SurfaceViewRenderer) all_frameLayout.getChildAt(0);
            surfaceViewRenderer.setZOrderMediaOverlay(false);
            all_frameLayout.removeView(surfaceViewRenderer);
            localVideoViewContainer.resumeSurfaceViewRender(currentAllscreenId, surfaceViewRenderer);
            isAllScreen = false;
            currentAllscreenId = null;
        }

        PTZControlUtil.INSTANCE.release();

    }

    /**
     * 参会人
     *
     * @param view
     */
    public void participant(View view) {
        showQueuePop(view);
    }

    public void showQueuePop(View view) {
        participantPopWindow = new ParticipantPopWindow(this, channelId, true);
        participantPopWindow.showAsDropUp(view);
    }

    //展示视频下方操作选项（由于位置会发生变化，每次需遍历）
    public void showVideoItem(View view, String id) {
        ArrayList<ParticipantInfoModel> participantInfoModels = RoomClient.get().getRemoteParticipants();
        ParticipantInfoModel participantInfoModel = null;
        if (id.equals(RoomManager.get().getUserId())) {
            participantInfoModel = RoomClient.get().getLocalParticipant();
        } else {
            for (ParticipantInfoModel participantInfoModel1 : participantInfoModels) {
                if (participantInfoModel1.getUserId().equals(id) || (participantInfoModel1.getUserId() + Constants.LOCAL_SCREEN).equals(id)) {
                    participantInfoModel = participantInfoModel1;
                }
            }
        }
        if (participantInfoModel == null) {
            return;
        }
        videoItemPopWindow = new VideoItemPopWindow(this, participantInfoModel);
        videoItemPopWindow.initData();
        videoItemPopWindow.showAsDropDown(view);
    }

    public void showVideoItem(ParticipantInfoModel item) {
        if (!RoomManager.get().isHost()) {
            return;
        }
        if (isAllScreen) {
            return;
        }
        if (item == null) {
            return;
        }
        String userId = item.getUserId();
        ArrayList<ParticipantInfoModel> participantInfoModels = RoomClient.get().getRemoteParticipants();
        ParticipantInfoModel participantInfoModel = null;
        if (userId.equals(RoomManager.get().getUserId())) {
            participantInfoModel = RoomClient.get().getLocalParticipant();
        } else {
            for (ParticipantInfoModel participantInfoModel1 : participantInfoModels) {
                if (participantInfoModel1.getUserId().equals(userId) && (participantInfoModel1.getStreamType().equals(item.getStreamType()))) {
                    participantInfoModel = participantInfoModel1;
                    break;
                }
            }
        }
        if (participantInfoModel == null) {
            return;
        }
        VideoItemPopWindow videoItemPopWindow = new VideoItemPopWindow(this, participantInfoModel);
        videoItemPopWindow.initData();
        videoItemPopWindow.setVideoItemPop(connectId -> {
            //如果是自己 , 本地云台控制，否则对面云台控制
            isCurrentAllScreenSelf = RoomManager.get().getConnectionId().equals(connectId);
            if (isCurrentAllScreenSelf) {
                DeviceSettingManager.getInstance().startCameraControl();
            }
            allScreen(connectId);
        });
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        videoItemPopWindow.getContentView().measure(measureSpec, measureSpec);
        int measuredWidth = videoItemPopWindow.getContentView().getMeasuredWidth();
        videoItemPopWindow.showAtLocation(localVideoViewContainer, Gravity.NO_GRAVITY, item.getLayoutX() + (item.getLayoutWidth() - measuredWidth) / 2, item.getLayoutY());
        videoController.getMenuManager().registerWindows(videoItemPopWindow);
    }

    public void removeParent(View view) {
        if (view != null) {
            ViewGroup parentViewGroup = (ViewGroup) view.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeView(view);
            }
        }
    }

    public ViewGroup getParent(View view) {
        if (view != null) {
            ViewGroup parentViewGroup = (ViewGroup) view.getParent();
            return parentViewGroup;
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mDisplayManager.unregisterDisplayListener(this);
        RoomClient.get().setLocalHdmiParticipant(null);
        RoomClient.get().setLocalScreenParticipant(null);
        RoomClient.get().setLocalParticipant(null);
        RoomClient.get().getRemoteParticipant().clear();
        RoomClient.get().removeRoomEventCallback(VideoConferenceActivity.class);
        RoomClient.get().setOnLocalShareJoinListener(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void receiveSoundRecongnizedmsg(EventPublish eventPublish) {
        ParticipantInfoModel participant = eventPublish.getParticipantInfoModel();
        MediaStream stream = eventPublish.getMediaStream();
        EventBus.getDefault().removeStickyEvent(eventPublish);
        if (lastBreakLineUserId.equals(participant.getUserId())) {
            lastBreakLineUserId = "";
        }
        L.d("onRemoteStreamAdded >>>" + "onAddRemoteStream" + " , " + participant.getConnectionId() + " , streamType : " + participant.getAppShowName());
        L.d("onAddRemoteStreams >>>" + userId + " , " + participant.getUserId() + " , streamType : " + participant.getStreamType() + "connectId" + participant.getConnectionId());
        if (userId.equals(participant.getUserId())) {
            if (participant.getStreamType().equals(MAJOR)) {
                getAllParticipants(channelId); //
                videoController.audioStatus(participant.isAudioActive());
                videoController.videoStatus(participant.isVideoActive());
                videoController.speakerStatus(true);
                RoomManager.get().setMediaStream(stream);
                inviteParticipantNow();
            }
            return;
        }

        // 设置新入会者的音频状态（扬声器控制）
        if (stream.audioTracks.size() > 0) {
            stream.audioTracks.get(0).setEnabled(true);
        }

        if (!SHARING.equals(participant.getStreamType())) {
            videoController.updateJoinPersons();
        }

        Log.i("addNewView", "RemoteStream ishands " + RoomClient.get().isHasHands() + " handsId " + RoomClient.get().getHandsId() + " ,userId " + participant.getUserId());
        L.d("onAddRemoteStream: isHasHands  " + RoomClient.get().isHasHands() + " , role " + participant.getRole() + " , isLocalHas:" + isLocalHas
                + ", isShareHas:" + isShareHas);
        //有人发言， 且进来的用户是举手用户
        if (RoomClient.get().isHasHands() && participant.getUserId().equals(RoomClient.get().getHandsId()) && participant.getStreamType().equals(MAJOR)) {

            addNewView(participant.getUserId(), stream, participant);
        } else {
            L.d("onAddRemoteStream: isHasHands  " + RoomClient.get().isHasHands() + " , role " + participant.getRole() + " , isLocalHas:" + isLocalHas
                    + ", isShareHas:" + isShareHas);
            //无人发言且 进来的用户是主持人
            if (!RoomClient.get().isHasHands() && participant.getRole().equals(ROLE_PUBLISHER_SUBSCRIBER)) {
                if (isLocalHas) {
                    return;
                }
                addNewView(participant.getUserId(), stream, participant);
            } else {
                if (participant.getStreamType().equals(MAJOR)) {
                    addOtherView(participant.getUserId(), stream, false, participant);
                } else {
                    if (isShareHas) {       //不能同时有两个SHARE
                        return;
                    }
                    addOtherHDMIView(participant.getUserId() + Constants.LOCAL_SCREEN, stream, false, participant);
                }
            }
        }

        L.d("do this ----->>>>>>>>>>>>>" + participantModels.size() + " , " + _sinks.size());



        L.d("do this ---addOtherView ---addOtherView ------layoutChange");
        if (!RoomManager.get().isHost()) {
            layoutChange(RoomManager.get().getRoomLayoutModel());
        } else {
            if (_sinks.size() == participantModels.size()) {
                layoutChangeHost();
            }
        }

        if (_sinks.size() == participantModels.size()) {
            firstLayoutParams();
        }
    }


    @Override
    public void onBackPressed() {
//        doHangup();
//        stopShare();
//        leaveRoom();
//        finish();
        if (isAllScreen) {
            resetScreen(all_frameLayout);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPresentation != null && mPresentation.isShowing()) {
            mPresentation.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RoomClient.get().setCurrentActivity(this);
        registeNoFocusViewId(R.id.iv_see,R.id.fl_contair);
//        mFocusLayout = new FocusLayout(this);
//        bindListener();//绑定焦点变化事件
//        addContentView(mFocusLayout,
//                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT));//添加焦点层
//
//        mFocusLayout.addIngoreIds(R.id.iv_see);
//        mFocusLayout.addIngoreIds(R.id.more_menu);
//        mFocusLayout.addIngoreIds(R.id.remote_video_container2);
//        mFocusLayout.addIngoreIds(R.id.remote_top);
//        mFocusLayout.addIngoreIds(R.id.horizontal_scroll);
//        mFocusLayout.addIngoreIds(R.id.remote_video_container);
//        mFocusLayout.addIngoreIds(R.id.fl_contair);

    }

    @Override
    protected void onStop() {
//        doHangup();
        super.onStop();
    }

    /**
     * 是否 被 主持人 静音
     *
     * @param status
     * @param type
     * @return
     */
    private boolean getSlingStatus(String status, String type) {
        return status.equals(type);
    }

    public void removeView(String screenUid) {
//        FrameLayout frameLayout = remoteVideoViews.remove((screenUid));
//        if (frameLayout == null) {
//            return;
//        }
//        removeParent(frameLayout);
//        remoteVideoViewSplite.removeView(frameLayout);
//        if (frameLayout.getChildAt(0) instanceof SurfaceViewRenderer) {
//            ((SurfaceViewRenderer) frameLayout.getChildAt(0)).release();
//        }
    }

    //某userId 掉线,画面处理
    public void setViewBreak(String screenUid) {

        if (screenUid.equals((screenId))) {
            isLocalHas = false;
        }
        if (participantModels != null) {
            for (ParticipantInfoModel participantInfoModel : participantModels) {
                if (participantInfoModel.getUserId().equals(screenUid) && !SHARING.equals(participantInfoModel.getStreamType())) {
                    participantInfoModel.setOnlineStatus(Constract.offline);
                    localVideoViewContainer.notifyVideoStateChanged(participantInfoModel);
                }
            }
        }

    }


    public void removeShareView(String screenUid) {

        if (mPresentation != null && mPresentation.isShowing()) {
            mPresentation.getFrameLayout().removeView(mPresentation.getFrameLayout().getChildAt(0));
            mPresentation.getImageView().setVisibility(View.VISIBLE);
        } else {
            if (SHARING.equals(participantModels.get(0).getStreamType())) {
                localVideoViewContainer.stopShare(participantModels.get(0));
                localVideoViewContainer.notifyItemRemoved(participantModels.get(0));
                participantModels.remove(0);
            }
//            if (SHARING.equals(participantModels.get(0).getStreamType())) {
//
//            }
//            remoteVideoViewSplite.removeView(remoteVideoViews.remove((screenUid)));
        }

        currentShareModel = null;

        if (localHDMIVideoView != null) {
            localHDMIVideoView.release();
            localHDMIVideoView = null;
        }
    }

    /**
     * 获取所有与会的人
     *
     * @param channelId -> roomId
     */
    private void getAllParticipants(String channelId) {
//        clRtcAccess.getAllParticipants(channelId);
        RoomClient.get().getParticipants(channelId);
    }

    /**
     * 设置频道 音频 状态
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     * @param status
     */
    private void setAudioStatus(String roomId, String sourceId, List<String> targetId, boolean status) {
        RoomClient.get().setAudioStatus(roomId, sourceId, targetId, status);
    }

    /**
     * 设置 频道 视频 状态
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     * @param status
     */
    private void setVideoStatus(String roomId, String sourceId, List<String> targetId, boolean status) {
        RoomClient.get().setVideoStatus(roomId, sourceId, targetId, status);
    }

    /**
     * 举手
     *
     * @param roomId
     * @param sourceId
     */
    private void raiseHand(String roomId, String sourceId) {
        RoomClient.get().raiseHand(roomId, sourceId);
    }

    /**
     * 手放下
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     */
    private void putDownHand(String roomId, String sourceId, String targetId) {
        RoomClient.get().putDownHand(roomId, sourceId, targetId);
    }

    /**
     * bot bar 锁定
     *
     * @param sessionId
     * @param sourceId
     */
    private void lockConference(String sessionId, String sourceId) {
//        clRtcAccess.lockConference(sessionId, sourceId, enable);
        RoomClient.get().lockSession(sessionId, sourceId);
    }

    /**
     * bot bar 锁定
     *
     * @param sessionId
     * @param sourceId
     */
    private void unlockConference(String sessionId, String sourceId) {
//        clRtcAccess.lockConference(sessionId, sourceId, enable);
        RoomClient.get().unlockSession(sessionId, sourceId);
    }

    /********************************平板会控相关************************************/
    private PadEvent padEvent = new PadEvent() {
        @Override
        public void accessPadIn(PadModel padModel) {

        }

        @Override
        public void setAudio() {
        }

        @Override
        public void setVideo() {
        }

        @Override
        public void setSpeak() {
        }

        @Override
        public void leavePadRoom() {
            runOnUiThread(() -> {

                doHangup();
                leaveRoom();
                finish();
            });
        }

        @Override
        public void shareHDMI() {
            shareScreenHDMI(shareHDMI);
        }

    };


    /********************************信令回调相关（包含会控）************************************/
    private RoomEventCallback roomEventCallback = new RoomEventAdapter() {

        @Override
        public void onParticipantJoined(ParticipantInfoModel participant) {
            runOnUiThread(() -> {
                if (RoomManager.get().isHost()) {
                    if (SHARING.equals(participant.getStreamType()) || queues.contains(participant.getUserId())) {
                        return;
                    }
                    queues.add(participant.getUserId());
                    queueSend();
                }
            });
        }

        @Override
        public void closeRoomNotify() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("GroupActivity......", "closeRoomNotify");
                    if (VideoConferenceActivity.this.isFinishing()) {
                        return;
                    }
                    doHangup();
                    RoomClient.get().setHasHands(false);
                    finish();
                }
            });
        }

        @Override
        public void onParticipantEvicted(ParticipantInfoModel participant, String connectId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (participant == null) {
                        return;
                    }
                    if (VideoConferenceActivity.this.isFinishing()) {
                        return;
                    }
                    String userId = participant.getUserId();

                    if (connectId.equals(RoomManager.get().getConnectionId())) {
                        doHangup();
                        RoomClient.get().setHasHands(false);
                        finish();
                    } else {
                        if (participant == null) {
                            return;
                        }
                        if (!(screenId).equals(participant.getUserId())) {
                            leftToChangeView(participant.getUserId());
                        }

                        if (participant.getStreamType().equals(STREAM_SHARING)) {
                            onStopShareScreen(participant.getUserId());
                            isShareHas = false;
                            currentShareUser = "";
                            userId = userId + LOCAL_SCREEN;
                        } else {
                            removeView(userId);
                        }
                        if (RoomManager.get().isHost()) {
                            if (queues.contains(userId)) {
                                queues.remove(userId);
                                queueSend();
                            }
                        }
                        leftSink(userId);
                        webRTCManager.removePc(participant);
                        videoController.updateJoinPersons();
                    }
                }
            });
        }

        @Override
        public void onParticipantLeft(ParticipantInfoModel participant) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (participant == null) {
                        return;
                    }
                    if (VideoConferenceActivity.this.isFinishing()) {
                        return;
                    }
                    String userId = participant.getUserId();
                    if (!STREAM_SHARING.equals(participant.getStreamType())) {
                        participantModels.remove(participant);
                        leftToChangeView(userId);
                    }
                    if (STREAM_SHARING.equals(participant.getStreamType())) {

                        onStopShareScreen(userId);
                        isShareHas = false;
                        currentShareUser = "";
                        userId = userId + LOCAL_SCREEN;
                    } else {
                        videoController.setCurrentHandUpNum(RoomClient.get().getCurrentLeftHandsUp());
                        videoController.showDotNumber(String.valueOf(RoomClient.get().getCurrentLeftHandsUp()));
                        RoomClient.get().setShowRollCall(false);
                        removeView(userId);
                    }
                    if (RoomManager.get().isHost()) {
                        if (queues.contains(userId)) {
                            queues.remove(userId);
                            queueSend();
                        }
                    }
                    leftSink(userId);
                    webRTCManager.removePc(participant);

                    videoController.updateJoinPersons();
                    localVideoViewContainer.notifyItemRemoved(participant);
                    firstLayoutParams();
                }
            });
        }

        @Override
        public void reconnectPartStopPublishSharing(ParticipantInfoModel participant) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (participant == null) {
                        return;
                    }
                    String userId = participant.getUserId();
                    if (STREAM_SHARING.equals(participant.getStreamType())) {

                        onStopShareScreen(userId);
                        isShareHas = false;
                        currentShareUser = "";
                        userId = userId + LOCAL_SCREEN;
                    } else {
                        removeView(participant.getUserId());
                    }
                    if (RoomManager.get().isHost()) {
                        if (queues.contains(userId)) {
                            queues.remove(userId);
                            queueSend();
                        }
                    }
                    leftSink(userId);
                    webRTCManager.removePc(participant);
                    //更新文字
                    videoController.updateJoinPersons();
                    if (participantModels.size() > 0 && SHARING.equals(participantModels.get(0).getStreamType())) {
                        participantModels.remove(0);
                    }
                    currentShareModel = null;
                    firstLayoutParams();
                }
            });
        }

        @Override
        public void onRaiseHandsSended() {
            L.d("会控 VideoConfAct onRaiseHandsSended()");
        }


        @Override
        public void onPutDownHandsSended() {
            L.d("会控 VideoConfAct onPutDownHandsSended()");
        }


        @Override
        public void onAudioStatusSwitchSuccess() {
            L.d("会控 VideoConfAct onAudioStatusSwitchSuccess()");
        }


        @Override
        public void onVideoStatusSwitchSuccess() {
            L.d("会控 VideoConfAct onVideoStatusSwitchSuccess()");
        }


        @Override
        public void onReceiveAudioStatusChange(AudioStstusResp audioStstusResp) {
            L.d("会控 VideoConfAct  onReceiveAudioStatusChange()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onSetAudioStatus(audioStstusResp);
                }
            });
        }

        @Override
        public void onReceiveVideoStatusChange(VideoStatusResp videoStatusResp) {
            L.d("会控 VideoConfAct  onReceiveVideoStatusChange()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onSetVideoStatus(videoStatusResp);
                }
            });

        }

        /**
         * 设置共享权限
         *
         * @param model
         */
        @Override
        public void onSharePower(SlingModel model) {
            L.d("会控 --->>> 共享权限设置");
            runOnUiThread(() -> {
//                onSetShareStatus(model);
            });
        }

        @Override
        public void onReceiveSpeakerStatusChange(SlingModel slingModel) {
            L.d("会控 VideoConfAct  onReceiveSpeakerStatusChange()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onSetSpeakerStatus(slingModel);
                }
            });
        }

        @Override
        public void onReceiveHandsUp(HandsStatusResp handsStatusResp) {
            L.d("会控 VideoConfAct  onReceiveHandsUp()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (SPEditor.instance().getUserId().equals(RoomManager.get().getHostId())) {            //主持人端
                        String showName = !TextUtils.isEmpty(handsStatusResp.getUsername()) ? handsStatusResp.getUsername() : handsStatusResp.getAppShowName();
                        UCToast.showCustomToast(VideoConferenceActivity.this, showName + "申请发言", Gravity.BOTTOM, 200, 120);
                        onRaiseHand(handsStatusResp);
                    }
                }
            });

        }

        @Override
        public void onReceiveHandsDown(HandsStatusResp handsStatusResp) {
            L.d("会控 VideoConfAct  onReceiveHandsDown()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onPutDownHand(handsStatusResp);
                }
            });

        }

        @Override
        public void onGetParticipantsResult(ArrayList<ParticipantInfoModel> participantsList) {
            runOnUiThread(() -> {
                onGetParticipants(participantsList);
            });
        }

        @Override
        public void onLeaveRoomSuccess(String role) {
            //辅流 离开房间回调没有 sessionId
            App.post(() -> {
                if (role.equals(SHARING)) {
                    onStopShareScreen(userId);
                }
            });

        }

        @Override
        public void onTransferModerator(SlingModel model) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    onLockConference(true, model);
                }
            });
        }

        @Override
        public void onLockSession(SlingModel slingModel) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onLockConference(true, slingModel);
                }
            });

        }

        @Override
        public void unLockSession(SlingModel slingModel) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onLockConference(false, slingModel);
                }
            });

        }

        @Override
        public void onWebSocketFailMessage(WsError wsErrorr, String method) {
            if (RoomClient.get().getCurrentActivity() instanceof VideoConferenceActivity) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (wsErrorr.getCode() == WsErrorCode.ERROR_ROOM_CAPACITY_LIMITED) {         //邀请入会 人数已达上限
                            UCToast.show(App.getInstance().getApplicationContext(), getString(R.string.str_error_room_capacity_limit));
                        } else if (wsErrorr.getCode() == WsErrorCode.ERROR_CONFERENCE_NOT_EXIST) {          //会议已经关闭
                            UIUtils.toastMessage("会议已经关闭");
                        } else if (wsErrorr.getCode() == WsErrorCode.ERROR_ALREADY_ONLINE) {
                            RoomClient.get().setAccessInSuccess(false);
                        } else {
                            UIUtils.toastMessage(wsErrorr.getMessage());
                        }

                    }
                });
            }
        }

        /**
         * 获取会议预设信息
         *
         * @param resp
         */
        @Override
        public void onGetPresetInfo(PresetInfoResp resp) {
            runOnUiThread(() -> {
                L.d("获取会议预设信息  " + resp.getSubject());
            });
        }

        @Override
        public void rollcall(SlingModel slingModel) {
            runOnUiThread(() -> {
                OnRollCall(slingModel);
            });
        }

        /**
         * 替换发言
         *
         * @param slingModel
         */
        @Override
        public void replaceRollCall(ReplaceRollcallModel slingModel) {
            runOnUiThread(() -> {
//                replaceRollcall(slingModel);
            });
        }

        @Override
        public void endRollcall(SlingModel slingModel) {
            runOnUiThread(() -> {
                EndRollCall(slingModel);
            });
        }

        @Override
        public void getOrgList(OrgList orgList) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void getUserDeviceList(UserDeviceList userDeviceList) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void getRoomTimeCountDown(RoomTimeModel model) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (model.getRemainTime() == 1) {
                        videoController.showTimeDownPopup(1);
                        if (RoomManager.get().getHostId().equals(SPEditor.instance().getUserId())) {
                            show1mDialog();
                        }

                    } else if (model.getRemainTime() == 10) {
                        videoController.showTimeDownPopup(10);
                    }

                }
            });
        }

        @Override
        public void onRoomDelay() {
            runOnUiThread(() -> {

            });
        }

        /**
         * 广播参会者掉线
         *
         * @param model
         */
        @Override
        public void userBreakLine(ParticipantInfoModel model) {
            runOnUiThread(() -> {
                if (model == null) {
                    return;
                }

                String userId = model.getUserId();
                if (!lastBreakLineUserId.equals(userId)) {
                    leftToChangeView(userId);
                    setViewBreak(userId);
                    leftSink(userId);
                    lastBreakLineUserId = userId;
                }
            });
        }

        /**
         * 被邀请人 拒绝
         *
         * @param model
         */
        @Override
        public void refuseInvite(SlingModel model) {
            runOnUiThread(() -> {
                if (SPEditor.instance().getUserId().equals(RoomManager.get().getHostId())) {
                    UCToast.show(VideoConferenceActivity.this, "用户拒绝入会");
                }
            });

        }

        //发送成功
        @Override
        public void onInviteJoinCallback() {
            runOnUiThread(() -> {

            });
        }

        @Override
        public void majorLayoutNotify(RoomLayoutModel roomLayoutModel) {
            runOnUiThread(() -> {
                L.d("do this ------majorLayoutNotify>>>>>222222");
                if (RoomManager.get().isHost()) {
                    RoomManager.get().setRoomLayoutModel(roomLayoutModel);
                    L.d("do this  -------------------RoomManager.get() " + RoomManager.get().getRoomLayoutModel().getLayout());
                    return;
                }
                if (roomLayoutModel.getLayout().size() < participantModels.size()) {
                    return;
                }

                layoutChange(roomLayoutModel);
            });
        }

        @Override
        public void getSubDevOrUser(PartDeviceModel orgList) {
            runOnUiThread(() -> {
                if (RoomClient.get().getCurrentActivity() instanceof VideoConferenceActivity) {
                    videoController.notifyDeviceData(orgList.getDeviceList());
                }
            });
        }

        @Override
        public void getDepartmentTree(CompanyModel companyModel) {
            runOnUiThread(() -> {
                if (RoomClient.get().getCurrentActivity() instanceof VideoConferenceActivity) {
                    videoController.notifyOrgTreeList(companyModel);
                }
            });
        }

        @Override
        public void getGroupList(GroupListInfoModel groupListInfoModel) {
            runOnUiThread(()->{
                if (RoomClient.get().getCurrentActivity() instanceof VideoConferenceActivity) {
                    videoController.setSubGroupItem(groupListInfoModel);
                }
            });
        }

        @Override
        public void getGroupInfo(GroupInfoModel groupInfoModel) {
            runOnUiThread(()->{
                if (RoomClient.get().getCurrentActivity() instanceof VideoConferenceActivity) {
                    videoController.notifyGroupDeviceData(groupInfoModel.getGroupInfo());
                }
            });
        }

        @Override
        public void getRoomLayoutCallback(RoomLayoutModel model) {
            runOnUiThread(() -> {
                if (RoomManager.get().getRoomLayoutModel() != null) {
                    if (RoomManager.get().isHost()) {
                        if (_sinks.size() == participantModels.size()) {
                            layoutChangeHost();
                        }
                    }
                }
            });
        }

        @Override
        public void sharingControlNotify(SlingModel slingModel) {
            if (slingModel != null) {
                App.post(() -> {
                    if (userId.equals(slingModel.targetId) && slingModel.sourceId.equals(slingModel.targetId)) {
                        if (videoController != null) {
                            videoController.shareHDMI();
                        }
                    } else {
                        onSetShareStatus(slingModel);
                    }

                });
            }
        }
    };

    //主持人同步布局（断线重连时）
    private void layoutChangeHost() {
        roomLayoutModel = RoomManager.get().getRoomLayoutModel();
        if (roomLayoutModel.getLayout() == null || roomLayoutModel.getLayout().size() == 0) {
            return;
        }

        if (roomLayoutModel.getModeratorIndex() != -1) {        //如果是断线重连
            if (RoomManager.get().isHostFirst() && !RoomClient.get().isHasHands()) {       //当前房间没有共享或发言的 人
                if (roomLayoutModel.getLayout().contains(RoomManager.get().getConnectionId())) {
                    roomLayoutModel.getLayout().remove(RoomManager.get().getConnectionId());
                }
                roomLayoutModel.getLayout().add(0, RoomManager.get().getConnectionId());
                roomLayoutModel.setMode(roomLayoutModel.getMode() + 1);
                RoomManager.get().setRoomLayoutModel(roomLayoutModel);
                acceptRoomLayoutMode(roomLayoutModel);
                L.d("do this ------------------layoutChangeHost0000--------------" + RoomManager.get().getConnectionId() + " ----" + RoomManager.get().getRoomLayoutModel().getLayout() + "  ----- " + roomLayoutModel.getLayout());
                return;
            }
            roomLayoutModel.setMode(participantModels.size());
            if (roomLayoutModel.getModeratorIndex() >= participantModels.size()) {
                roomLayoutModel.setModeratorIndex(participantModels.size() - 1);
            }
            if (!roomLayoutModel.getLayout().contains(RoomManager.get().getConnectionId())) {
                roomLayoutModel.getLayout().add(roomLayoutModel.getModeratorIndex(), RoomManager.get().getConnectionId());
            }
            RoomManager.get().setRoomLayoutModel(roomLayoutModel);
        } else {
            return;
        }

        L.d("do this ------------------layoutChangeHost--------------" + RoomManager.get().getRoomLayoutModel().getLayout());
        acceptRoomLayoutMode(roomLayoutModel);
    }

    //普通与会者更改同步布局
    private void layoutChange(RoomLayoutModel roomLayoutModel) {
        if (roomLayoutModel != null) {
            if (roomLayoutModel.getLayout() == null || roomLayoutModel.getLayout().size() == 0) {
                return;
            }
            Log.i("roomLayoutModel","roomLayoutModel..."+roomLayoutModel.getLayout().size());
            if (roomLayoutModel.getLayout().size() == 1 && roomLayoutModel.getMode() == 0 && replace.equals(roomLayoutModel.getType())) {
                if (roomLayoutModel.getLayout().get(0).equals(userId)) {
                    webRTCManager.changeCaptureFormat(true);
                } else {
                    webRTCManager.changeCaptureFormat(false);
                }
                if (isAllScreen) { //先结束全屏再
                    resetScreen(all_frameLayout);
                }
                return;
            }

            queues.clear();
            if (roomLayoutModel.getLayout() != null) {
                queues.addAll(roomLayoutModel.getLayout());
            }

            if (isAllScreen) { //先结束全屏再
                resetScreen(all_frameLayout);
            }
            acceptRoomLayoutMode(roomLayoutModel);
        }
    }

    //同步布局
    private void acceptRoomLayoutMode(RoomLayoutModel roomLayoutModel) {
        swapParticipantModels(roomLayoutModel);
        localVideoViewContainer.acceptRoomLayoutMode(roomLayoutModel);
    }

    //主持人 接收到与会者信息后 按布局layout中的顺序 同步与会者信息
    private void swapModels(RoomLayoutModel roomLayoutModel) {
        ParticipantInfoModel shareModel = null;
        if (!RoomManager.get().isHost()) {
            return;
        }
        if (roomLayoutModel == null) {
            return;
        }

        if (roomLayoutModel.getLayout() == null || roomLayoutModel.getLayout().size() == 0) {
            return;
        }

        if (mPresentation != null && mPresentation.isShowing()) {
            if (RoomClient.get().getRemoteParticipant().get(roomLayoutModel.getLayout().get(0)) != null) {
                if (roomLayoutModel.getLayout().size() > 0 && SHARING.equals(RoomClient.get().getRemoteParticipant().get(roomLayoutModel.getLayout().get(0)).getStreamType())) {
                    roomLayoutModel.getLayout().remove(0);
                }
            }
        }
        //如果第0个是分享，先取出，再放回
        if (participantModels.size() > 0 && SHARING.equals(participantModels.get(0).getStreamType())) {
            if (!roomLayoutModel.getLayout().contains(participantModels.get(0).getConnectionId())) {
                shareModel = participantModels.get(0);
                participantModels.remove(0);
            }
        }
        //todo 12301652
        //按照roomLayoutModel中的顺序，排列participantModels
        List<String> connections = roomLayoutModel.getLayout();

        L.d("do this ----------------swapModels   "+roomLayoutModel.getLayout());
        for (int i = 0; i < connections.size(); i++) {
            for (int j = 0; j < participantModels.size(); j++) {
                if (connections.get(i).equals(participantModels.get(j).getConnectionId())) {
                    if (i != j) {
                        if (i < participantModels.size()) {
                            Collections.swap(participantModels, i, j);
                        }
                    }
                }
            }
        }

        for (ParticipantInfoModel m : participantModels) {
            L.d("do this ---------------swap ----- "+m.getAppShowName()+" , "+m.getConnectionId());
        }
        if (shareModel != null) {
            participantModels.add(0, shareModel);
            shareModel = null;
        }
    }

    // 按照RoomLayoutModel中的顺序  交换 participantModels
    private void swapParticipantModels(RoomLayoutModel roomLayoutModel) {

        if (roomLayoutModel == null) {
            return;
        }

        if (roomLayoutModel.getLayout() == null || roomLayoutModel.getLayout().size() == 0) {
            return;
        }
        if (mPresentation != null && mPresentation.isShowing()) {
            if (RoomClient.get().getRemoteParticipant().get(roomLayoutModel.getLayout().get(0)) != null) {
                if (roomLayoutModel.getLayout().size() > 0 && SHARING.equals(RoomClient.get().getRemoteParticipant().get(roomLayoutModel.getLayout().get(0)).getStreamType())) {
                    roomLayoutModel.getLayout().remove(0);
                }
            }
        }
        //todo 12301652
        //按照roomLayoutModel中的顺序，排列participantModels
        List<String> connections = roomLayoutModel.getLayout();
        if (connections.size() < participantModels.size()) {
            return;
        }

        L.d("do this --------------------------------------swapParticipantModels");
        for (int i = 0; i < connections.size(); i++) {
            for (int j = 0; j < participantModels.size(); j++) {
                if (connections.get(i).equals(participantModels.get(j).getConnectionId())) {
                    if (i != j) {
                        if (i < participantModels.size()) {
                            Collections.swap(participantModels, i, j);
                        }
                    }
                }
            }
        }
    }

    //掉线，被踢出，离开统一处理逻辑
    public void leftToChangeView(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        L.d("do this ---leftToChangeView>>>" + userId + ", " + (screenId) + " , hostId : " + RoomManager.get().getHostId());
        if (!userId.equals(RoomManager.get().getHostId())) {
            L.d("do this ---leftToChangeView>>>" + userId + ", " + (screenId));
            if (userId.equals((screenId))) {
//                newChangeFrame((RoomManager.get().getHostId()), screenId);
                SlingModel slingModel = new SlingModel();
                slingModel.sourceId = RoomManager.get().getHostId();
                slingModel.targetId = userId;
                EndRollCall(slingModel);
            }
        }
    }

    //掉线，被踢出，离开统一处理逻辑 移除流
    public void leftSink(String userId) {
        ProxyVideoSink sink = _sinks.get(userId);
        if (sink != null) {
            sink.setTarget(null);
        }
        _sinks.remove(userId);

    }

    /**
     * 收到 与会者 信息
     */
    public void onGetParticipants(ArrayList<ParticipantInfoModel> models) {
        Log.i(logTag, "join...onGetParticipants:");
        //json 处理，所有与会者

        if (participantInfoModelsNoShare != null) {
            participantInfoModelsNoShare.clear();
        }
        if (tempParticpantModels != null) {
            tempParticpantModels.clear();
        }
        if (models != null) {
            RoomManager.get().setParticipants(models);
        }
        L.d("do this --------------------onGetParticipants");
        participantModels = getParticipant();

        participantInfoModelsNoShare = RoomManager.get().getFilterParticipants(participantModels);
        tempParticpantModels.addAll(participantInfoModelsNoShare);
        videoController.refreshParticipant(participantInfoModelsNoShare);
        videoController.refreshRollCallPop();
//        swapParticipantModels(RoomManager.get().getRoomLayoutModel());

        if (meetSettingPopupWindow != null) {
            int myIndex = -1;

            for (int i = 0; i < tempParticpantModels.size(); i++) {
                if (SPEditor.instance().getUserId().equals(tempParticpantModels.get(i).getUserId())) {
                    myIndex = i;
                }
            }
            if (myIndex != -1) {
                tempParticpantModels.remove(myIndex);
            }
            meetSettingPopupWindow.showNextPopup(tempParticpantModels);
        }
    }

    /**
     * 语音 状态 变更
     */

    public void onSetAudioStatus(AudioStstusResp audioStatus) {
        String sourceId = audioStatus.getSourceId();
        boolean status = getSlingStatus(audioStatus.getStatus(), CLRtcSinaling.VALUE_STATUS_ON);
        String[] targetIds = audioStatus.getTargetIds();
        //web 会控  targetIds 为空，默认全体操作
        if (targetIds == null || targetIds.length == 0) {
            if (!RoomManager.get().isHost()) {
                if (status == RoomClient.get().getRemoteParticipant().get(connectId).isAudioActive()) {
                    L.d("do this ----->>>" + RoomClient.get().getRemoteParticipant().get(connectId).isAudioActive() + " , " + status);
                    return;
                }
                UIUtils.toastMessage(status ? "主持人打开了您的麦克风" : "主持人关闭了您的麦克风");
                RoomManager.get().setHostByClose(!status);
                videoController.audioStatus(status);

            }

            for (ParticipantInfoModel participantModel : participantModels) {
                if (sourceId.equals(participantModel.getUserId())) {
                    continue;
                }
                if (!SHARING.equals(participantModel.getStreamType())) {
                    participantModel.setAudioActive(status);
                }

            }
            // TODO: 2019/12/19
//            boundShadowLayout.setData(RoomClient.get().getLayoutData());
            return;
        }
        for (String targetId : targetIds) {

            L.d("onSetAudioStatus " + audioStatus.toString() + RoomManager.get().getUserId());
            /**
             * pop打开更新popupwindow状态
             */
            if (participantPopWindow != null && participantPopWindow.isShowing()) {
//                participantPopWindow.setAudioStatus(targetId, status); //改变与会者状态
            }
            /**
             * 底部控件（自己的状态更新）
             */
            if (RoomManager.get().isAllMute(targetId) || userId.equals(targetId)) {

                videoController.audioStatus(status);

                if (!sourceId.equals(targetId)) {
                    if (status == RoomClient.get().getRemoteParticipant().get(connectId).isAudioActive()) {
                        L.d("do this ----->>>" + RoomClient.get().getRemoteParticipant().get(connectId).isAudioActive() + " , " + status);
                        return;
                    }
                    UIUtils.toastMessage(status ? "主持人打开了您的麦克风" : "主持人关闭了您的麦克风");

                }
            }
            /**
             * 修改加入房间
             */
            for (ParticipantInfoModel participantModel : participantModels) {
                if ((TextUtils.isEmpty(targetId) || participantModel.getUserId().equals(targetId) && !SHARING.equals(participantModel.getStreamType()))) {
                    participantModel.setAudioActive(status);
                    localVideoViewContainer.notifyVideoStateChanged(participantModel);
                }
            }
        }
    }

    /**
     * 视频 状态 变更
     */

    public void onSetVideoStatus(VideoStatusResp videoStatus) {
        String[] targetIds = videoStatus.getTargetIds();
        if (targetIds == null) {
            return;
        }

        for (String targetId : targetIds) {
            boolean status = getSlingStatus(videoStatus.getStatus(), CLRtcSinaling.VALUE_STATUS_ON);
            L.d("onSetAudioStatusV2: " + videoStatus.toString());
            if (userId.equals(targetId)) {
                videoController.videoStatus(status);

            }
            for (ParticipantInfoModel participantModel : participantModels) {
                if ((TextUtils.isEmpty(targetId) || participantModel.getUserId().equals(targetId) && !SHARING.equals(participantModel.getStreamType()))) {
                    participantModel.setVideoActive(status);
                    localVideoViewContainer.notifyVideoStateChanged(participantModel);
                }
            }
        }

    }

    /**
     * 共享状态变更
     */
    public void onSetShareStatus(SlingModel model) {
//        String[] targetIds = model.getTargetIds();
//        if (targetIds == null) {
//            return;
//        }
        String targetId = model.getTargetId();
        if (TextUtils.isEmpty(targetId)) {
            return;
        }
//        for (String targetId : targetIds) {
        boolean status = getSlingStatus(model.getStatus(), CLRtcSinaling.VALUE_STATUS_ON);
        L.d("onSetShareStatusV2: " + model.toString());
        if (userId.equals(targetId)) {
            stopShareScreen2Other();
//                enableShare = status;
        }
//            for (Map.Entry<String, FrameLayout> entry : remoteVideoViews.entrySet()) {
//                if (entry.getKey().equals((targetId))) {
//                    if (status) {
//                        entry.getValue().getChildAt(0).setVisibility(View.VISIBLE);
//                        if (entry.getValue().getChildCount() > 2) {
//                            entry.getValue().getChildAt(2).setVisibility(View.INVISIBLE);
//                        }
//                    } else {
//                        entry.getValue().getChildAt(0).setVisibility(View.INVISIBLE);
//                        if (entry.getValue().getChildCount() > 2) {
//                            entry.getValue().getChildAt(2).setVisibility(View.VISIBLE);
//                        }
//                    }
//                }
//            }
        for (ParticipantInfoModel participantModel : participantModels) {
            if ((TextUtils.isEmpty(targetId) || participantModel.getUserId().equals(targetId)) && SHARING.equals(participantModel.getStreamType())) {
                participantModel.setShareStatus("off");
            }
        }
//        }

    }

    private void stopShareScreen2Other() {
        isShareIng = false;
        isShareHas = false;
        onStopShareScreen(userId);

        webRTCManager.stopShareHdmi();
        shareHDMI.setText("共享屏幕");
        RoomClient.get().leaveRoom(userId, channelId, STREAM_SHARING);
    }

    /**
     * @param videoStatus
     */
    public void onSetSpeakerStatus(SlingModel videoStatus) {
        String targetId = videoStatus.targetId;
        boolean status = getSlingStatus(videoStatus.status, CLRtcSinaling.VALUE_STATUS_ON);
        L.d("onSetAudioStatusV2: " + videoStatus.toString());
        if (userId.equals(targetId)) {
            videoController.speakerStatus(status);
//            speakerStatus(status);

        }
        for (ParticipantInfoModel participantModel : participantModels) {
            if ((TextUtils.isEmpty(targetId) || participantModel.getUserId().equals(targetId) && !SHARING.equals(participantModel.getStreamType()))) {
                participantModel.setSpeakerActive(status);
                localVideoViewContainer.notifyVideoStateChanged(participantModel);
            }
        }
    }

    /**
     * @param videoStatus
     */
    public void OnRollCall(SlingModel videoStatus) {
        ParticipantInfoModel infoModel = null;
        int index = -1;
        videoController.setCurrentSpeakNum(1);
        RoomClient.get().setHasHands(true);
        String targetId = videoStatus.targetId;
        String sourceId = videoStatus.sourceId;
        currentSpeakUserId = targetId;
        boolean status = getSlingStatus(videoStatus.status, CLRtcSinaling.VALUE_STATUS_ON);
        L.d("onSetAudioStatusV2:OnRollCall  " + videoStatus.toString());

        if (targetId.equals(userId)) {
            webRTCManager.changeCaptureFormat(true);
        } else if (RoomManager.get().getHostId().equals(userId)) {
            webRTCManager.changeCaptureFormat(false);
        }

        if (isAllScreen) { //先结束全屏再
            resetScreen(all_frameLayout);
        }
        if (targetId.equals(userId)) {
            videoController.rollCallByMe();
        }

        //获取targetID 对应的model，下标
        for (int i = 0; i < participantModels.size(); i++) {
            L.d("do this ------rollcall  ---- " + participantModels.get(i).getAppShowName());
            if (targetId.equals(participantModels.get(i).getUserId()) && !SHARING.equals(participantModels.get(i).getStreamType())) {
                participantModels.get(i).setHandStatus("speaker");
                infoModel = participantModels.get(i);
                index = i;
            }
        }

        videoController.showDotNumber(videoStatus.getRaiseHandNum());

//        if ((targetId).equals(screenId)) {
//            return;
//        }
        if (RoomManager.get().isHost()) {
            swapQueue(targetId, (screenId));
        }

        /**
         * 画中画基本功能 切换， 细节需要调试
         */

        //如果开启辅流，则当点名发言时，与第一位交换，否则与主窗口交换
        if (RoomManager.get().isHost()) {

            if (isShareHas || isShareIng) {
                if (participantModels.size() > 1) {
                    //双屏
                    if (mPresentation != null && mPresentation.isShowing()) {
                        if (infoModel != null && index != -1) {
                            Collections.swap(participantModels, 0, index);
                            localVideoViewContainer.swap2Main(infoModel);
                        }
                    } else {
                        ParticipantInfoModel firstModel = participantModels.get(1);
                        L.d("do this ---rollcall >>>>" + participantModels.size());
                        L.d("do this --->>>>> >" + infoModel.getUserId() + " , " + firstModel.getUserId() + ", first model StreamType:" + firstModel.getStreamType());
                        if (!SHARING.equals(firstModel.getStreamType()) && index != -1) {
                            Collections.swap(participantModels, 1, index);
                            localVideoViewContainer.swap(firstModel, infoModel);
                        }
                    }
                }
            } else {
                if (infoModel != null && index != -1) {
                    L.d("do this  -----rollcall  -------------------------" + index);
                    Collections.swap(participantModels, 0, index);
                    localVideoViewContainer.swap2Main(infoModel);
                }
            }


            firstLayoutParams();
        }

        localVideoViewContainer.notifyVideoStateChanged(infoModel);
    }

    /**
     * 交换
     */
    private void swapQueue(String targetId, String sourceId) {
        for (String s : queues) {
            Log.i("swapQueue", "swapQueue...." + s);
        }
        //定义第三方变量
        int index1 = 0;
        int index2 = 0;
        for (int i = 0; i < queues.size(); i++) {
            if (sourceId.equals(queues.get(i))) {
                index1 = i;
                continue;
            }
            if (targetId.equals(queues.get(i))) {
                index2 = i;
                continue;
            }
        }
        Collections.swap(queues, index1, index2);
        for (String s : queues) {
            Log.i("swapQueue", "swapQueue...." + s);
        }
    }

    public void replaceRollcall(ReplaceRollcallModel replaceRollcallModel) {
        String endUserId = replaceRollcallModel.getEndTargetId();           //被替换者
        String startUserId = replaceRollcallModel.getStartTargetId();       //替换人员

        if (isAllScreen) { //先结束全屏再
            resetScreen(all_frameLayout);
        }
        if (endUserId.equals(userId)) {
            webRTCManager.changeCaptureFormat(false);
        } else if (RoomManager.get().getHostId().equals(userId)) {
            webRTCManager.changeCaptureFormat(true);
        }

        if (startUserId.equals(userId)) {
            webRTCManager.changeCaptureFormat(true);
        } else if (RoomManager.get().getHostId().equals(userId)) {
            webRTCManager.changeCaptureFormat(false);
        }

        if ((startUserId).equals(screenId) || (endUserId).equals(screenId)) {
            return;
        }

        if (endUserId.equals(userId)) {
//            raise_hand.setCompoundDrawablesWithIntrinsicBounds(null,
//                    getResources().getDrawable(R.drawable.maininterface_menu_icon_speak, null), null, null);
//            putDownHand(RoomClient.get().getRoomId(),
//                    SPEditor.instance().getUserId(),
//                    SPEditor.instance().getUserId());
//            raise_hand.setText("申请发言");
        }

        if (startUserId.equals(userId)) {
            videoController.rollCallByMe();
        }


        if (!TextUtils.isEmpty(replaceRollcallModel.raiseHandNum)) {
            videoController.setCurrentHandUpNum(Integer.parseInt(replaceRollcallModel.raiseHandNum));
            videoController.showDotNumber(replaceRollcallModel.raiseHandNum);
        }

    }

    public void EndRollCall(SlingModel videoStatus) {
        ParticipantInfoModel infoModel = null;
        ParticipantInfoModel hostModel = null;
        int index = 0;
        int hostIndex = 0;
        videoController.setCurrentSpeakNum(0);
        RoomClient.get().setShowRollCall(false);
        RoomClient.get().setHasHands(false);
        String targetId = videoStatus.targetId;
        String sourceId = TextUtils.isEmpty(videoStatus.sourceId) ? RoomManager.get().getHostId() : videoStatus.sourceId;
        currentSpeakUserId = "";
        //找到当前正在发言的与会者
        for (int i = 0; i < participantModels.size(); i++) {
            if (RoomManager.get().getHostId().equals(participantModels.get(i).getUserId()) && !SHARING.equals(participantModels.get(i).getStreamType())) {
                hostModel = participantModels.get(i);
                hostIndex = i;
            }
            if (targetId.equals(participantModels.get(i).getUserId()) && !SHARING.equals(participantModels.get(i).getStreamType())) {
                participantModels.get(i).setHandStatus("down");
                infoModel = participantModels.get(i);
                index = i;
            }
        }
        boolean status = getSlingStatus(videoStatus.status, CLRtcSinaling.VALUE_STATUS_ON);
        L.d("onSetAudioStatusV2: " + videoStatus.toString());
        if (isAllScreen) { //先结束全屏再
            resetScreen(all_frameLayout);
        }
        if (targetId.equals(userId)) {
            webRTCManager.changeCaptureFormat(false);
        } else if (RoomManager.get().getHostId().equals(userId)) {
            webRTCManager.changeCaptureFormat(true);
        }

//        if ((sourceId).equals(screenId)) {
//            return;
//        }
        if (targetId.equals(userId)) {
            videoController.endCallByMe();
        }
        if (!TextUtils.isEmpty(videoStatus.raiseHandNum)) {
            videoController.setCurrentHandUpNum(Integer.parseInt(videoStatus.raiseHandNum));
            videoController.showDotNumber(videoStatus.raiseHandNum);
        }
        if (RoomManager.get().isHost()) {
            swapQueue(sourceId, (screenId));
        }
        L.d("do this -------EndRollCall>>> " + hostIndex + " , " + index);
        if (RoomManager.get().isHost()) {
            if (hostModel != null && infoModel != null) {
                Collections.swap(participantModels, hostIndex, index);
                localVideoViewContainer.swap(hostModel, infoModel);
            }

            firstLayoutParams();
        }

        if (infoModel != null) {
            localVideoViewContainer.notifyVideoStateChanged(infoModel);
        }
    }

    /**
     * 发起 举手 后  回调
     */

    public void onRaiseHand(HandsStatusResp handsStatus) {
        L.d("onRaiseHandV2: " + handsStatus.toString());
        videoController.setCurrentHandUpNum(Integer.parseInt(handsStatus.getRaiseHandNum()));
        videoController.showDotNumber(handsStatus.getRaiseHandNum());

    }


    /**
     * 手放下 回调
     */

    public void onPutDownHand(HandsStatusResp videoStatus) {
        videoController.setCurrentHandUpNum(Integer.parseInt(videoStatus.getRaiseHandNum()));
        RoomClient.get().setShowRollCall(false);
        if (!TextUtils.isEmpty(videoStatus.getRaiseHandNum())) {
            videoController.showDotNumber(videoStatus.getRaiseHandNum());
        }
        if (!userId.equals(videoStatus.getTargetId())) {
            return;
        }
        videoController.endCallByMe();
        String targetId = videoStatus.getTargetId();
        String sourceId = videoStatus.getSourceId();
        L.d("onSetAudioStatusV2: " + videoStatus.toString());

        if (sourceId.equals(targetId)) {
            return;
        }

        /* A更换B*/
//        newChangeFrame(sourceId, targetId);
    }

    /**
     * 会议锁定 回调
     *
     * @param slingModel
     */
    public void onLockConference(boolean lock, SlingModel slingModel) {
        String sessionId = slingModel.sessionId;
        Log.e(logTag, "onLockConference: sessionId = " + sessionId);

    }


    /**
     * 停止共享 回调
     */
    public void onStopShareScreen(String userId) {

        String sessionId = userId;
        Log.e(logTag, "onStopShareScreen: sessionId = " + sessionId + ", sourceId = " + userId);

        String screenUid = sessionId + Constants.LOCAL_SCREEN;

        for (ParticipantInfoModel m : participantModels) {
            if (userId.equals(m.getUserId()) && !SHARING.equals(m.getStreamType())) {
                m.setShareStatus("off");
                localVideoViewContainer.notifyVideoStateChanged(m);
            }
        }
        Map<String, ParticipantInfoModel> participantMap = RoomClient.get().getRemoteParticipant();
        if (currentShareModel != null) {
            participantMap.remove(currentShareModel.getConnectionId());
            _sinks.remove(currentShareModel.getUserId() + Constants.LOCAL_SCREEN);
        }
        L.d("do this ------.>>>websocket broad : " + participantMap.keySet().size() + " , " + _sinks.size());
        removeShareView(screenUid);

//        for (Map.Entry<String, ParticipantInfoModel> entry : participantMap.entrySet()) {
//            if (SHARING.equals(entry.getValue().getStreamType())) {
//                participantMap.remove(entry.getKey());
//                break;
//            }
//        }

        firstLayoutParams();
    }

    /************************************HDMI***********************************/
    public void shareScreenHDMI(View view) {
        if (!enableShare && shareHDMI.getText().equals("共享屏幕") && !RoomManager.get().isRoomAllowShare()) {
            UCToast.show(VideoConferenceActivity.this, "主持人已关闭共享操作");
            return;
        }
        if (ViewUtils.isFastClickLong()) {
            return;
        }

//        RoomClient.get().inviteParticipant(channelId,SPEditor.instance().getAccount(),list);
        if (!supportHdmiIn) {
            UIUtils.toastMessage("目前只支持vhd设备");
            return;
        }

//        initLocalScreen();

        int[] idArray = new int[2];
        if (!CameraEncoderv2.hasValidCamera(idArray)) {
            Toast.makeText(VideoConferenceActivity.this, "no vhd camera", Toast.LENGTH_LONG).show();
            LogUtil.LOG_WARN("shareScreenHDMI", this.getClass().toString(),
                    "not find vhd camera");
            return;
        } else {
            LogUtil.LOG_WARN("shareScreenHDMI", this.getClass().toString(),
                    "find vhd camera vid:pid(" +
                            String.format("%04x", idArray[0]) + ":" +
                            String.format("%04x", idArray[1]) + ")");
        }
        Log.i("shareScreenHDMI", "shareScreenHDMI..." + isShareHas + "...." + isShareIng);
        if (isShareHas && !isShareIng) {
            UIUtils.toastMessage(currentShareUser + "正在共享屏幕，请稍后再试");
            return;
        }

        if (shareHDMI.getText().equals("共享屏幕")) {
            if (!SystemUtil.isHdmiinConnected()) {
                UIUtils.toastMessage("请检查您的HDMI IN连接");
                return;
            }
            isShareIng = true;
            isShareHas = true;
            if (mPresentation != null && mPresentation.isShowing()) {
                presentAdd(false, null);
            }
            webRTCManager.shareHdmiJoin();
            shareHDMI.setText("结束共享");
        } else {
            isShareIng = false;
            isShareHas = false;
            if (mPresentation != null && mPresentation.isShowing()) {
                ((SurfaceViewRenderer) mPresentation.getFrameLayout().getChildAt(0)).release();
                mPresentation.getFrameLayout().removeView(mPresentation.getFrameLayout().getChildAt(0));
                mPresentation.getImageView().setVisibility(View.VISIBLE);
            } else {
//                onStopShareScreen(userId);
                if (RoomManager.get().isHost()) {
                    if (queues.contains(userId + LOCAL_SCREEN)) {
                        queues.remove(userId + LOCAL_SCREEN);
//                        RoomClient.get().getBroadcastMajorLayout(screenSplitType, queues, change);
                    }
                }

                //移除共享
//                removeShareSingleScreenLocal();
            }
            webRTCManager.stopShareHdmi();
            shareHDMI.setText("共享屏幕");
            RoomClient.get().leaveRoom(userId, channelId, STREAM_SHARING);
        }
    }

    public void presentAdd(boolean insert, SurfaceViewRenderer surfaceViewRenderer) {
        if (isShareHas) {
            if (insert) {
                removeParent(surfaceViewRenderer);
                presentationHDMIVideoView = surfaceViewRenderer;
            } else {
                presentationHDMIVideoView = webRTCManager.createRendererView(getApplicationContext());
            }
            presentationHDMIVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            presentationHDMIVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            mPresentation.getFrameLayout().addView(presentationHDMIVideoView, 0);
            mPresentation.getImageView().setVisibility(View.GONE);

            if (isShareIng) {
                prensatationId = userId + LOCAL_SCREEN;
            }
            if (RoomManager.get().isHost()) {
                queueSend();
            }

            webRTCManager.shareHdmi(presentationHDMIVideoView);
        }
    }

    public void HDMIAdd(boolean insert, SurfaceViewRenderer surfaceViewRenderer) {
        if (isShareHas) {

            if (insert) {
                removeParent(surfaceViewRenderer);
                initHDMISurface(surfaceViewRenderer);
            } else {
                initHDMISurface();
            }
            initHDMIFrameLayout();


            if (isShareIng) {
                prensatationId = userId + LOCAL_SCREEN;
            }
            if (RoomManager.get().isHost()) {
                queueSend();
            }

            localVideoViewContainer.addView(locaHDMIFramenLayout);

            webRTCManager.shareHdmi(localHDMIVideoView);
        }
    }

    /**
     *
     */
    public void queueSend() {
        if (isShareHas) {
            /**
             * 有分享加上分享
             */
            if (!TextUtils.isEmpty(prensatationId) && !queues.contains(prensatationId)) {
                if (queues.size() > 1) {
                    queues.add(1, prensatationId);
                } else {
                    queues.add(prensatationId);
                }
//                RoomClient.get().getBroadcastMajorLayout(screenSplitType, queues, change);
            } else {
//                RoomClient.get().getBroadcastMajorLayout(screenSplitType, queues, change);
            }
            /**
             * 先发送正确的列表，然后移除分屏Id
             */
            for (int i = 0; i < queues.size(); i++) {
                Log.i("splitequeues", "s....." + queues.get(i));
                if (queues.get(i).contains(LOCAL_SCREEN) && mPresentation != null && mPresentation.isShowing()) {
                    queues.remove(i);
                    break;
                }

            }
            for (int i = 0; i < queues.size(); i++) {
                Log.i("splitequeuesAfter", "s....." + queues.get(i));
            }
        } else {
//            RoomClient.get().getBroadcastMajorLayout(screenSplitType, queues, change);
        }

    }


    /************************************分屏相关***********************************/
    public void initPresentation(boolean insert) {
        Display[] displays;//屏幕数组

        displays = mDisplayManager.getDisplays();
        L.d("initPresentation: " + displays.length);
        if (displays.length > 1) { //链接设备大于1 开启分屏
            mPresentation = new DifferentDislay(this, displays[1]);//displays[1]是副屏
            mPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            mPresentation.show();
            if (insert) {
                addPresentMedia();
            }

//            mPresentation.setOnclickForDifferent(new DifferentDislay.OnclickForDifferent() {
//                @Override
//                public void onclickDiff() {
//                    UIUtils.toastMessage("分屏显示");
//                }
//            });

        }
    }

    public void addPresentMedia() {
        Log.i("closePresentMedia", "addPresentMedia" + isShareIng);
        if (isShareIng) {
            if (mPresentation != null && mPresentation.isShowing()) {
                initPresentSurface();
            }

            if (participantModels.size() > 0 && SHARING.equals(participantModels.get(0).getStreamType())) {
                localVideoViewContainer.stopShare(participantModels.get(0));
            }
            webRTCManager.shareHdmi(presentationHDMIVideoView);
        } else {
            if (mPresentation != null && mPresentation.isShowing()) {
                //接上辅屏，先展示图片
                initPresentSurface();
                if (isShareHas) {
                    //移除主屏的共享流画面initPresentSurface
                    if (participantModels.size() > 0 && SHARING.equals(participantModels.get(0).getStreamType())) {
                        localVideoViewContainer.stopShare(participantModels.get(0));
                    }
                    participantModels.get(0).getVideoSink().setTarget(presentationHDMIVideoView);
                }
            }
        }
    }

    public void closePresentation() {
        if (mPresentation != null) {
            mPresentation.dismiss();
        }

    }


    public void initPresentSurface() {
        if (presentationHDMIVideoView == null) {
            Log.i("initPresentSurface", "initPresentSurface");
            presentationHDMIVideoView = webRTCManager.createRendererView(getApplicationContext());
            presentationHDMIVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            presentationHDMIVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        }
        removeParent(presentationHDMIVideoView);
        mPresentation.getFrameLayout().addView(presentationHDMIVideoView, 0);
        mPresentation.getImageView().setVisibility(View.GONE);
    }


    public void closePresentMedia() {
        Log.i("closePresentMedia", "closePresentMedia" + isShareIng);
//        if (isShareIng) {
//            webRTCManager.shareHdmi(null);
//        } else {
//            if (isShareHas) {
        //将分享流重新显示到主屏第0个
        if (participantModels.size() > 0 && SHARING.equals(participantModels.get(0).getStreamType())) {
            localVideoViewContainer.shareScreen(participantModels.get(0));
        }

//            }
//        }
    }

    /****************************************屏幕适配相关*******************************/
    /**
     * 万能适配
     *
     * @return
     */
    @Override
    public Resources getResources() {
//        AutoSizeCompat.autoConvertDensityOfGlobal((super.getResources()));
        return super.getResources();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        videoController.showMenu();
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().endCameraControl();
                    }
                    isCurrentAllScreenSelf = true;
                    resetScreen(all_frameLayout);
                    break;
                }
                finishAndRelease();
                break;

            case KeyEvent.KEYCODE_ZOOM_IN:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_ZOOM_CONTROL, ZOOM_TELE);
                    } else {
                        PTZControlUtil.INSTANCE.operate(Operation.SMALL);
                    }
                }
                break;

            case KeyEvent.KEYCODE_ZOOM_OUT:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_ZOOM_CONTROL, ZOOM_WIDE);
                    } else {
                        PTZControlUtil.INSTANCE.operate(Operation.LARGE);
                    }
                }
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                videoController.showMenu();
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf && event.getRepeatCount() == 0) {
                        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_UP);
                        return true;
                    } else if (!isCurrentAllScreenSelf && event.getRepeatCount() == 0) {
                        PTZControlUtil.INSTANCE.operate(Operation.UP);
                    } else {

                    }
                }
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                videoController.showMenu();
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf && event.getRepeatCount() == 0) {
                        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_DOWN);
                    } else if (!isCurrentAllScreenSelf && event.getRepeatCount() == 0) {
                        PTZControlUtil.INSTANCE.operate(Operation.DOWN);
                    } else {

                    }
                }
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                videoController.showMenu();
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf && event.getRepeatCount() == 0) {
                        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_LEFT);
                        return true;
                    } else if (!isCurrentAllScreenSelf && event.getRepeatCount() == 0) {
                        PTZControlUtil.INSTANCE.operate(Operation.LEFT);
                    } else {

                    }
                }
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                videoController.showMenu();
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf && event.getRepeatCount() == 0) {
                        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_RIGHT);
                        return true;
                    } else if (!isCurrentAllScreenSelf && event.getRepeatCount() == 0) {
                        PTZControlUtil.INSTANCE.operate(Operation.RIGHT);
                    } else {

                    }
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

            default:
//                videoController.reset();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP);
                    } else {
                        PTZControlUtil.INSTANCE.stop();
                    }
                }
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP);
                    } else {
                        PTZControlUtil.INSTANCE.stop();
                    }
                }
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP);
                    } else {
                        PTZControlUtil.INSTANCE.stop();
                    }
                }
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP);
                    } else {
                        PTZControlUtil.INSTANCE.stop();
                    }
                }
                break;

            case KeyEvent.KEYCODE_ZOOM_IN:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_ZOOM_CONTROL, ZOOM_STOP);
                    } else {
                        PTZControlUtil.INSTANCE.stop();
                    }
                }
                break;

            case KeyEvent.KEYCODE_ZOOM_OUT:
                if (isAllScreen) {
                    if (isCurrentAllScreenSelf) {
                        DeviceSettingManager.getInstance().setCameraControl(GlobalValue.KEY_ZOOM_CONTROL, ZOOM_STOP);
                    } else {
                        PTZControlUtil.INSTANCE.stop();
                    }
                }
                break;

            case KeyEvent.KEYCODE_CUT:
                shareScreenHDMI(shareHDMI);
                break;

            default:
//                videoController.getMenuManager().scheduleHide();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    /**
     * 会控 设置 重写fv
     *
     * @param id
     * @param <T>
     * @return
     */
    @Override
    public <T extends View> T findViewById(int id) {
        if (id == R.id.fl_contair && meetSettingPopupWindow != null) {
            return meetSettingPopupWindow.getContentView().findViewById(R.id.fl_contair);
        }
        return super.findViewById(id);
    }

    //展示倒计时1m 弹框提醒
    private void show1mDialog() {
        if (isFinishing()) {
            return;
        }
        VideoControlManager.getInstance().show1mDialog(this);
    }

    //WebSocket 网络状态变化监听
    @Override
    public void onStateChange(WebSocketState socketState) {
        if (socketState == WebSocketState.OPEN) {
            runOnUiThread(() -> {
                if (ActivityUtils.getInstance().getStackCount() > 0) {
                    if (ActivityUtils.getInstance().getCurrent() instanceof VideoConferenceActivity) {
                        VideoControlManager.getInstance().dismissNetErrorPopup();
//                        dismissNetError();
                    }
                }
            });

        }
    }

    //socket 连接次数
    @Override
    protected void onSockedConnectFail() {
        super.onSockedConnectFail();

        runOnUiThread(() -> {
            UIUtils.toastMessage("连接已断开");
            if (ActivityUtils.getInstance().getStackCount() > 0) {
                if (ActivityUtils.getInstance().getCurrent() instanceof VideoConferenceActivity) {
                    showNetError();
                    CallUtil.asyncCall(1000, () -> {
                        RoomClient.get().setWebSocketStateListener(null);
                        VideoControlManager.getInstance().dismissNetErrorPopup();
//                        dismissNetError();
                        doHangup();
                        releaseView();
                        L.d("onSockedConnectFail >>>>>" + currentSpeakUserId + " , userId: " + userId);
                        if (userId.equals(currentSpeakUserId)) {
                            RoomClient.get().setHasHands(false);
                        }
                    });
                }
            }
        });
    }

    //网络异常弹窗
    private void showNetError() {
        if (isFinishing()) {
            return;
        }

        VideoControlManager.getInstance().showNetErrorPopup(this, cl_main);
    }


    private void releaseView() {
        localVideoViewContainer.removeAllViews();
        _sinks.clear();
        queues.clear();
        finish();
//        ActivityUtils.getInstance().finishUnitActivity(GroupActivity.class);
    }

    private List<ParticipantInfoModel> getParticipant() {
        if (mPresentation != null && mPresentation.isShowing()) {
            participantModels = RoomManager.get().getParticipants();
        } else {
            participantModels = RoomManager.get().getParticipantsWithShare();
        }

        for (ParticipantInfoModel model : participantModels) {
            L.d("do this ----getParticipant >>>>>>> " + model.getAppShowName() + " , " + model.getConnectionId());
        }

        L.d("do this ----------getParticipant-------" + RoomManager.get().getRoomLayoutModel().getLayout());
        swapModels(RoomManager.get().getRoomLayoutModel());
        return participantModels;
    }

    /**
     * Called whenever a logical display has been added to the system.
     * Use {@link DisplayManager#getDisplay} to get more information about
     * the display.
     *
     * @param displayId The id of the logical display that was added.
     */
    @Override
    public void onDisplayAdded(int displayId) {
        L.d("onDisplayAdded>>> " + displayId + ", " + mDisplayManager.getDisplays().length);
        initPresentation(true);
        //todo  添加辅屏， 主屏显示主流，辅屏显示辅流（主辅混叠能否显示）
    }

    /**
     * Called whenever a logical display has been removed from the system.
     *
     * @param displayId The id of the logical display that was removed.
     */
    @Override
    public void onDisplayRemoved(int displayId) {
        L.d("onDisplayRemoved>>> " + displayId + ", " + mDisplayManager.getDisplays().length);
        L.d("onDisplayRemoved>>>" + mPresentation.getWindow().isActive());
        closePresentation();
        closePresentMedia();
        if (presentationHDMIVideoView != null) {
            presentationHDMIVideoView.release();
            presentationHDMIVideoView = null;
        }
        //todo 主屏拉取 主辅混叠

//        changeShareView(true, true, userId + LOCAL_SCREEN);
//        HDMIAdd(true, presentationHDMIVideoView);
    }

    /**
     * Called whenever the properties of a logical display have changed.
     *
     * @param displayId The id of the logical display that changed.
     */
    @Override
    public void onDisplayChanged(int displayId) {
        L.d("onDisplayChanged>>> " + displayId + ", " + mDisplayManager.getDisplays().length);
    }

    /*************************************************    SFU新UI    *********************************************/

    //单屏接入共享(本地)
    private void addShareSingleScreenLocal(ParticipantInfoModel participantInfoModel) {
        L.d("do this ---addShareSingleScreenLocal>>>>");
        ProxyVideoSink pvs = new ProxyVideoSink();
        participantInfoModel.setVideoSink(pvs);
        if (mPresentation != null && mPresentation.isShowing()) {
        } else {
            _sinks.put(participantInfoModel.getUserId() + Constants.LOCAL_SCREEN, pvs);
        }
        currentShareModel = participantInfoModel;
        //更新当前正在共享的与会者
        for (ParticipantInfoModel m : participantModels) {
            if (participantInfoModel.getUserId().equals(m.getUserId()) && !SHARING.equals(m.getStreamType())) {
                localVideoViewContainer.notifyVideoStateChanged(m);
            }
        }
        if (mPresentation != null && mPresentation.isShowing()) {
            firstLayoutParams();
            return;
        }
        participantModels.add(0, participantInfoModel);
        localVideoViewContainer.shareScreen(participantInfoModel);
        firstLayoutParams();

    }

    //单屏移除共享（本地）
    private void removeShareSingleScreenLocal() {
        int currentMode = localVideoViewContainer.getMode();
        if (SHARING.equals(participantModels.get(0).getStreamType())) {
            localVideoViewContainer.stopShare(participantModels.get(0));
            participantModels.remove(0);
        }


    }

    public void initVideoControl() {
        videoController.setVideoPlayControl(this);
        videoController.setViewData(channelId);
        videoController.initBasis();
        videoController.initData();
        videoController.setBaseActivity(this);
        videoController.setRoleViewStatus();
        try {
            getLifecycle().addObserver(videoController);
        } catch (Exception e) {

        }
    }

    private void inviteParticipantNow() {

        if (RoomManager.get().getListOfInvited().size() > 0) {
            RoomClient.get().inviteParticipant(channelId, RoomManager.get().getUserId(), RoomManager.get().getListOfInvited());
            RoomManager.get().getListOfInvited().clear();
        }

    }

    //设置会议时间
    private void setDuringTime() {
        long time = new Date(System.currentTimeMillis() - RoomManager.get().getCreateAt()).getTime();           //获取的服务器时间戳 快了本地获取系统时间 3s
        long m = time / (1000);
        if (m < 0) {
            m = 0;
        }
        videoController.setMeetDuring(m);
    }

    //重新读取预置点
    private void setMemoryConfig(int key) {
        if (isCurrentAllScreenSelf && StringUtils.isIndexEqual1(strOfKeySet, key)) {
            DeviceSettingManager.getInstance().setCameraControl(GlobalValue.MEMORY_RECALL, String.valueOf(key));
        }
    }

    public void onRemoteLoginNotify() {

        doHangup();
        RoomClient.get().setShowRollCall(false);
        RoomClient.get().setHasHands(false);
        lastBreakLineUserId = "";
        CallUtil.asyncCall(500, () -> {
            RoomClient.get().accessOut();
        });
    }

    @Override
    public void roomSettingControl(View view) {
        roomSetting(view);
    }

    @Override
    public void splitScreen0Control() {
        if (RoomManager.get().isHost()) {
            RoomClient.get().setConferenceLayout(4, false);
        }
    }

    @Override
    public void splitScreen4Control(int count, boolean b) {
        L.d("do this ----splitScreen4Control");
        if (RoomManager.get().isHost()) {
            if (b) {
                localVideoViewContainer.autoLayout();
            } else {
                localVideoViewContainer.setMode(count);
            }
            ArrayList<String> connectIds = new ArrayList<>();
            for (ParticipantInfoModel model : participantModels) {
                connectIds.add(model.getConnectionId());
            }
            if (mPresentation != null && mPresentation.isShowing() && currentShareModel != null) {
                connectIds.add(0, currentShareModel.getConnectionId());
            }
            RoomClient.get().getBroadcastMajorLayout(localVideoViewContainer.getMode(), connectIds, "change");
        }

    }

    @Override
    public void roomClosed() {
        finishAndRelease();
    }

    @Override
    public void shareControl(View view) {
        shareHDMI = (TextView) view;
        shareScreenHDMI(view);

    }

    @Override
    public void OnLocalShareJoin(ParticipantInfoModel participantInfoModel) {
        runOnUiThread(() -> {
            addShareSingleScreenLocal(participantInfoModel);
        });
    }
}