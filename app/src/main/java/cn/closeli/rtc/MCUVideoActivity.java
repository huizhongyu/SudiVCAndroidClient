package cn.closeli.rtc;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.fs.fat32.FAT;
import com.jaeger.library.StatusBarUtil;
import com.neovisionaries.ws.client.WebSocketState;
import com.tencent.bugly.crashreport.CrashReport;
import com.vhd.base.util.LogUtil;
import com.vhd.camera.CameraEncoderv2;

import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.constract.GlobalValue;
import cn.closeli.rtc.constract.WsErrorCode;
import cn.closeli.rtc.model.PadModel;
import cn.closeli.rtc.model.ReplaceRollcallModel;
import cn.closeli.rtc.model.RoomTimeModel;
import cn.closeli.rtc.model.SlingModel;
import cn.closeli.rtc.model.StrategyModel;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.GroupInfoModel;
import cn.closeli.rtc.model.info.GroupListInfoModel;
import cn.closeli.rtc.model.info.GroupModel;
import cn.closeli.rtc.model.info.PartDeviceModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.UserDeviceModel;
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
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.StringUtils;
import cn.closeli.rtc.utils.SystemUtil;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.ViewUtils;
import cn.closeli.rtc.utils.data_manager.VideoControlManager;
import cn.closeli.rtc.utils.net.NetworkUtils;
import cn.closeli.rtc.widget.BoundShadowLayout;
import cn.closeli.rtc.widget.DifferentDislay;
import cn.closeli.rtc.widget.FocusLayout;
import cn.closeli.rtc.widget.ParticipantLayout;
import cn.closeli.rtc.widget.UCToast;
import cn.closeli.rtc.widget.VideoController;
import cn.closeli.rtc.widget.dialog.InviteJoinDialog;
import cn.closeli.rtc.widget.popwindow.MeetSettingPopupWindow;
import cn.closeli.rtc.widget.popwindow.NetPopupWindow;
import cn.closeli.rtc.widget.popwindow.VideoItemPopWindow;
import me.jessyan.autosize.internal.CustomAdapt;

import static cn.closeli.rtc.constract.Constract.MIX_MAJOR_AND_SHARING;
import static cn.closeli.rtc.constract.Constract.SFU_SHARING;
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

public class MCUVideoActivity extends BaseActivity implements DisplayManager.DisplayListener, VideoController.VideoPlayerControl {
    private String logTag = this.getClass().getCanonicalName();
    //key-value -> userId-FrameLayout

    //本地视频 ViewGroup
    @BindView(R.id.local_video_container)
    LinearLayout localVideoViewContainer;
    //本地视频 RelativeLayout
    @BindView(R.id.cl_main)
    ConstraintLayout cl_main;
    @BindView(R.id.shareHDMI)
    TextView shareHDMI;
    @BindView(R.id.iv_narrow)
    ImageView iv_narrow;
    @BindView(R.id.all_frameLayout)
    FrameLayout all_frameLayout;


    @BindView(R.id.video_controller)
    VideoController videoController;

    private WebRTCManager webRTCManager;
    //本地视频 SurfaceView 视频源 摄像头
    private SurfaceViewRenderer localVideoView;
    //本地视频 SurfaceView 视频源 摄像头
    private SurfaceViewRenderer allScreenView;

    private SurfaceViewRenderer localScreenView;
    //分屏dialog
    DifferentDislay mPresentation;
    //屏幕管理类
    DisplayManager mDisplayManager;
    //链接id 流的id
    private String connectId = "";
    //用户id
    private String userId = "";
    //房间id
    private String channelId = "";
    //角色
    private String channelRole = "";

    private int screenHeigth;
    private int screenWidth;
    private int screenSplitType = 4;
    private NetPopupWindow netPopupWindow;
    private InviteJoinDialog dialog;
    /*本地基本属性初始化*/
    private boolean enableShare = true; //共享
    private boolean lockRoom = false;
    private boolean isLocalHas = false; //本地屏幕是否有人，回调addRemote需要
    private boolean isShareHas = false; //分享屏幕是否有人，回调addRemote需要
    private boolean isAllScreen = false; //是否全屏
    private boolean isAllScreenByShare = false; //是否全屏
    private boolean isShareIng = false; //本人是否正在共享
    private boolean isMixMajor = false; //主混正在进行
    private boolean isMixShareMajor = false; //主辅混正在进行
    private boolean isShareSFU = false; //辅S 正在进行

    private String firstId = ""; //加入房间后的第一个的顺序Id.用于置顶
    private String screenId = ""; //加入房间后的主页面.用于换人
    private String currentConnectId = ""; //当前的主屏ID  （主持人是放大屏幕，与会者就是显示的ID）

    /**
     * 与会者 集合
     */
    private List<ParticipantInfoModel> participantModels;
    private ArrayList<ParticipantInfoModel> tempParticpantModels = new ArrayList<>();
    private FrameLayout.LayoutParams surfaceParams;
    private LinearLayout.LayoutParams localParams; //当前预览参数
    private FrameLayout localFrameLayout;
    private FrameLayout localScreenLayout;
    private FrameLayout locaHDMIFramenLayout;
    private final String MAIN_TAG = "main_tag";
    private final String PRESENT_TAG = "present_tag";
    private boolean supportHdmiIn = false;
    private int currentType = Constract.SETTING_NONE;         //会议设置 type
    private MeetSettingPopupWindow meetSettingPopupWindow;
    private boolean useCamEncoded = false;
    private String lastBreakLineUserId = "";
    private String currentSpeakUserId = "";
    private String currentShareUser = "";        //当前正在发言的人 名称
    private boolean isClickFinish;
    private SurfaceViewRenderer presentationHDMIVideoView;
    private SurfaceViewRenderer surfaceViewRenderer;
    private BoundShadowLayout boundShadowLayout;
    private boolean isCurrentAllScreenSelf = true;          //当前全屏操作的是视图，是否是自己
    private String strOfKeySet;
    private int inviteNum = 0;
    private Thread thread;

    private boolean isScreen = false;

    private BoundShadowLayout.OnItemClickListener onItemClickListener = new BoundShadowLayout.OnItemClickListener() {
        @Override
        public void onItemClick(ParticipantInfoModel item) {
            showVideoItem(item, false);
        }

        @Override
        public void onBottomBoundClick() {
            videoController.requestBottomFocus();
        }

        @Override
        public void onRightBoundClick() {
            videoController.requestRightFocus();
        }
    };
    private ProxyVideoSink mixShare;
    private ProxyVideoSink sfuShare;
    private ProxyVideoSink sfuPerson;
    private MediaStream localStream;
    private MediaStream hdmiStream;
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
        setContentView(R.layout.activity_mcu_video);
        ActivityUtils.getInstance().addActivity(this);
//        showStatusBar();
        StatusBarUtil.setColor(MCUVideoActivity.this, getResources().getColor(R.color.color_translate));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ButterKnife.bind(this);
//        Eventb

        Intent intent = getIntent();
        userId = SPEditor.instance().getUserId();
        connectId = RoomManager.get().getConnectionId();

        channelId = RoomClient.get().getRoomId();
        channelRole = intent.getStringExtra("channelRole");

        RoomManager.get().setUserId(userId);
        RoomManager.get().setRole(channelRole);

        //会议 回调
        RoomClient.get().addRoomEventCallback(MCUVideoActivity.class, roomEventCallback);
        ServerManager.get().setPadEvent(padEvent);
        //云台控制回调
        RoomClient.get().addRoomEventCallback(PTZControlUtil.class, PTZControlUtil.INSTANCE);
        L.d("do this ---channelId >>>>>" + channelId);
        RoomManager.get().setRoomId(channelId);
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        //屏幕高度 = 整个屏高 - top bar 高度 - bot bar 高度
        screenHeigth = dm.heightPixels;
        screenWidth = dm.widthPixels;

        //设置surfaceView 参数
        surfaceParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        initVideoControl();

        supportHdmiIn = SystemUtil.getSystemModel().equals("C9Z");
        if (supportHdmiIn) {
        } else {
            videoController.setHDMIStatus(View.GONE);
            useCamEncoded = false;
        }

        mDisplayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.registerDisplayListener(this, null);
        initPresentation(false);

        start();
//        registerFocusedIds();
        strOfKeySet = DeviceSettingManager.getInstance().getFromSp().getCurrentPreset();
        Message msg = Message.obtain();
        msg.what = 1;
        handler.sendMessage(msg);
        parseParticapantsInRooms();
    }

    private void registerFocusedIds() {

        if (RoomManager.get().getListOfInvited().size() > 0) {
            inviteNum = RoomManager.get().getListOfInvited().size();
            RoomClient.get().inviteParticipant(channelId, RoomManager.get().getUserId(), RoomManager.get().getListOfInvited());
            RoomManager.get().getListOfInvited().clear();
        }

    }

    public void showVideoItem(ParticipantInfoModel item, boolean isBySFU) {
        if (!RoomManager.get().isHost()) {
            return;
        }
        if (isAllScreen) {
            return;
        }
        if (isAllScreenByShare) {
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
        videoItemPopWindow.setVideoItemPop((connectId) -> {
            //如果是自己 , 本地云台控制，否则对面云台控制
            isCurrentAllScreenSelf = RoomManager.get().getConnectionId().equals(connectId);
            L.d("do this ---isCurrentAllScreenSelf>>>" + isCurrentAllScreenSelf);
            if (isCurrentAllScreenSelf) {
                DeviceSettingManager.getInstance().startCameraControl();
            }
//            if(isBySFUShare){
//                allScreenByShare();
//            }else {
            allScreen(connectId);
//            }
        });
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        videoItemPopWindow.getContentView().measure(measureSpec, measureSpec);
        int measuredWidth = videoItemPopWindow.getContentView().getMeasuredWidth();
        videoController.getMenuManager().registerWindows(videoItemPopWindow);
        videoItemPopWindow.showAtLocation(boundShadowLayout, Gravity.NO_GRAVITY, item.getLayoutX() + (item.getLayoutWidth() - measuredWidth) / 2, item.getLayoutY());
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


    /**
     * WebRtc
     * Web Real-Time Communication
     */
    public void start() {
        webRTCManager = WebRTCManager.get();
        webRTCManager.create();
        webRTCManager.initWebRtc(channelId, userId, channelRole, useCamEncoded);
        if (channelRole.equals(ROLE_PUBLISHER) || channelRole.equals(ROLE_PUBLISHER_SUBSCRIBER)) {
            ParticipantInfoModel localParticipant = RoomClient.get().getLocalParticipant();
            if (localParticipant != null) {
                webRTCManager.createOffer(localParticipant);
            }
        }
        webRTCManager.addViewCallback(MCUVideoActivity.class, new IViewCallback() {
            @Override
            public void onSetLocalStream(MediaStream stream) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        localStream = stream;
                        registerFocusedIds();
//                        addNewView(stream);
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
                        Log.i("addNewView", "onSetLocalScreenStream");
                        hdmiStream = stream;
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
//                        addhdmiView(userId + Constants.LOCAL_SCREEN, stream, true);

                    }
                });
            }

            @Override
            public void onAddRemoteStream(MediaStream stream, ParticipantInfoModel participant) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ServerManager.get().participantJoin(participant);
                        participantModels = RoomManager.get().getParticipants();

                        if (lastBreakLineUserId.equals(participant.getUserId())) {
                            lastBreakLineUserId = "";
                        }
                        L.d("do this ----onAddRemoteStream >>>" + userId + " , " + participant.getUserId() + " , streamType : " + participant.getStreamType());
                        if (userId.equals(participant.getUserId()) && !isAllScreen) {
                            if (MAJOR.equals(participant.getStreamType())) {
                                getAllParticipants(channelId); //
                                videoController.audioStatus(participant.isAudioActive());
                                videoController.videoStatus(participant.isVideoActive());
                                videoController.speakerStatus(true);
                                RoomManager.get().setMediaStream(stream);
                            }
                            return;
                        }

                        // 设置新入会者的音频状态（扬声器控制）
                        if (stream.audioTracks.size() > 0) {
                            stream.audioTracks.get(0).setEnabled(videoController.isEnabledSpeaker());
                        }
                        L.d("do this ---onAddRemote >>>" + participant.getStreamType());
                        if (!SHARING.equals(participant.getStreamType()) && !SFU_SHARING.equals(participant.getStreamType())) {
                            videoController.updateJoinPersons();
                        } else {
                            isShareHas = true;
                        }

                        if (SFU_SHARING.equals(participant.getStreamType())) {
                            isShareHas = true;
                            if (mPresentation != null && mPresentation.isShowing()) {
                                addOtherHDMIView(stream, participant, true);
                            } else {
                                if (!RoomManager.get().isHost()) {
                                    addOtherHDMIView(stream, participant, false);
                                }
                            }
                        } else if (MIX_MAJOR_AND_SHARING.equals(participant.getStreamType()) && RoomManager.get().isHost()) {
                            addMixDMIView(participant.getUserId(), stream, participant);
                            addNewView(localStream);
                            boundShadowLayout.addSelfStream(connectId, localScreenView);
                        } else {
                            if (RoomManager.get().isHost()) {
                                addScreenView(participant.getUserId(), stream, participant);
                            } else {
                                addOtherView(participant.getUserId(), stream, participant);
                            }
                        }
                        if (sfuShare != null && mixShare != null) {
                            isScreenMainReceive(DeviceSettingManager.getInstance().getFromSp().isScreenMain());
                        }
                    }
                });

            }

            @Override
            public void onMixStream(ParticipantInfoModel participant, boolean isHasShare) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (SFU_SHARING.equals(participant.getStreamType())) {
                            if (!(mPresentation != null && mPresentation.isShowing())) {
                                initPresentation(false);
                            }
                            Log.i("displays", "displays..." + (mPresentation != null && mPresentation.isShowing()));
                            if (mPresentation != null && mPresentation.isShowing()) {
                                if (sfuShare == null && !isShareSFU && !isShareHas) {
                                    Log.i("onMixStream", "onMixStream..." + SFU_SHARING);
                                    isShareSFU = true;
                                    webRTCManager.setCurrentStreamType(SFU_SHARING);
                                    webRTCManager.createOfferByMix(participant);
                                }
                            } else if (!RoomManager.get().isHost()) {
                                if (sfuShare == null && !isShareSFU && !isShareHas) {
                                    Log.i("onMixStream", "onMixStream..." + SFU_SHARING);
                                    isShareSFU = true;
                                    webRTCManager.setCurrentStreamType(SFU_SHARING);
                                    webRTCManager.createOfferByMix(participant);
                                }
                            }
                            isShareHas = true;
                        } else if (MIX_MAJOR_AND_SHARING.equals(participant.getStreamType()) && RoomManager.get().isHost()) {
                            if (mixShare == null && !isMixShareMajor) { //避免重复拉流

                                isMixShareMajor = true;
                                Log.i("onMixStream", "onMixStream..." + MIX_MAJOR_AND_SHARING);
                                webRTCManager.setCurrentStreamType(MIX_MAJOR_AND_SHARING);
                                webRTCManager.createOfferByMix(participant);
                            }
                        }
                    }
                });

            }

            @Override
            public void onCloseRemoteStream(MediaStream stream, ParticipantInfoModel participant) {

            }

            @Override
            public void publishParticipantInfo(ParticipantInfoModel participant) {

            }
        });
    }

    private void localView() {
        if (localVideoView == null) {
            removeParent(localFrameLayout);
            initLocalSurfaceView();
            initLocalFrameLayout();

            localVideoViewContainer.addView(localFrameLayout, 0);
        }
    }

    private void allView() {
        if (allScreenView == null) {
            initScreenSurfaceView();
        }
    }

    private void localScreenView() {
        if (localScreenView == null) {
            initLocalScreenSurfaceView();
        }
    }

    //添加到 主屏（大窗口）
    private void addScreenView(String id, MediaStream stream, ParticipantInfoModel participant) {
        isLocalHas = true;
        localStream.videoTracks.get(0).removeSink(sfuPerson);
        sfuPerson = new ProxyVideoSink();
        sfuPerson.setTarget(localScreenView);
        Log.i("addRemote", "addScreenView");
        if (stream.videoTracks.size() > 0) {
            stream.videoTracks.get(0).addSink(sfuPerson);
        }

    }

    private void addOtherView(String id, MediaStream stream, ParticipantInfoModel participant) {
        if (isShareIng) {
            return;
        }
        if (!TextUtils.isEmpty(currentConnectId)) {
            stopCurrentSFUMedia(currentConnectId);
        }


        currentConnectId = participant.getConnectionId();
        localView();
        Log.i("addRemote", "addOtherView" + currentConnectId + participant.getStreamType());
        mixShare = new ProxyVideoSink();
        mixShare.setTarget(localVideoView);
        participantModels = RoomManager.get().getParticipants();
        if (stream.videoTracks.size() > 0) {
            stream.videoTracks.get(0).addSink(mixShare);
        }

    }

    //添加到主窗口
    private void addMixDMIView(String id, MediaStream stream, ParticipantInfoModel participant) {
        Log.i("addRemote", "addNewMix" + id);
        localView();
        // set render
        mixShare = new ProxyVideoSink();
        participantModels = RoomManager.get().getParticipants();

        // 推 混叠流
        mixShare.setTarget(localVideoView);
        //需要添加的验证数据
        if (stream.videoTracks.size() > 0) {
            stream.videoTracks.get(0).addSink(mixShare);
        }

    }

    //添加到主窗口
    private void addNewView(MediaStream stream) {

        localScreenView();
        removeParent(localScreenView);
        Log.i("addNewView", "addNewView");
        // set render
        if (sfuPerson == null) {
            sfuPerson = new ProxyVideoSink();
            // 推 混叠流
            sfuPerson.setTarget(localScreenView);
            //需要添加的验证数据
            if (stream.videoTracks.size() > 0) {
                stream.videoTracks.get(0).addSink(sfuPerson);
            }
        }

    }

    /**
     * 解析: 房间内 已有 与会者
     */
    private void parseParticapantsInRooms() {
        Map<String, ParticipantInfoModel> participants = RoomClient.get().getRemoteParticipant();
        if (participants == null) {
            return;
        }
        if (RoomClient.get().isHasHands()) {
            if (!TextUtils.isEmpty(RoomManager.get().getConnectionMainId())) {
                if (participants.get(RoomManager.get().getConnectionMainId()) != null) {
                    if (RoomManager.get().getConnectionMainId().equals(connectId)) {

                    } else {
                        Log.i("parseParti", "parseParticapantsInRooms" + RoomManager.get().getConnectionMainId());
                        webRTCManager.onParticipantPublished(participants.get(RoomManager.get().getConnectionMainId()));
                    }
                }
            }
        } else {
            if (!TextUtils.isEmpty(RoomManager.get().getHostConnectId())) {
                if (participants.get(RoomManager.get().getHostConnectId()) != null) {
                    Log.i("parseParti", "parseParticapantsInRooms" + RoomManager.get().getHostConnectId());
                    webRTCManager.onParticipantPublished(participants.get(RoomManager.get().getHostConnectId()));
                }
            }
        }
    }

    /**
     * 解析: 房间内 已有 与会者
     */
    private void ZoomInRooms(String connectionId) {
        Map<String, ParticipantInfoModel> participants = RoomClient.get().getRemoteParticipant();
        if (participants == null) {
            return;
        }
        if (connectionId.equals(connectId)) {
            Log.i("allScreen", "allScreen...my");
//            addNewView(localStream);
            all_frameLayout.addView(localScreenView, 0);
            localScreenView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            Log.i("allScreen", "allScreen...other");

            localScreenView.setZOrderMediaOverlay(false);
            if (participants.get(connectionId) != null) {
                webRTCManager.onParticipantPublished(participants.get(connectionId));
            } else {
                UIUtils.toastMessage("用户不存在");
            }
            all_frameLayout.addView(allScreenView, 0);
        }
    }

    //添加到小窗口
    private void addOtherHDMIView(MediaStream stream, ParticipantInfoModel participant, boolean twoScreen) {
        if (isShareIng) {
            return;
        }
        isShareHas = true;
        Log.i("addRemote", "addNewShare");
        // set render
        sfuShare = new ProxyVideoSink();
        initPresentSurface(twoScreen);
        sfuShare.setTarget(presentationHDMIVideoView);
        //需要添加的验证数据
        if (stream.videoTracks.size() > 0) {

            stream.videoTracks.get(0).addSink(sfuShare);
        }

    }


    public void initLocalSurfaceView() {

        localVideoView = webRTCManager.createRendererView(getApplicationContext());
    }

    public void initScreenSurfaceView() {

        allScreenView = webRTCManager.createRendererView(getApplicationContext());
        allScreenView.setZOrderMediaOverlay(true);

    }

    public void initLocalScreenSurfaceView() {

        localScreenView = webRTCManager.createRendererView(getApplicationContext());
        localScreenView.setZOrderMediaOverlay(true);
    }

    public void initLocalFrameLayout() {
        localFrameLayout = new FrameLayout(this);
//        localFrameLayout.setBackground(getDrawable(R.drawable.maininterface_list_video_frame));
        localFrameLayout.setPadding(1, 1, 1, 1);
        localFrameLayout.setTag(MAIN_TAG);
        localParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        localFrameLayout.setLayoutParams(localParams);
        localParams.gravity = Gravity.CENTER;
        localVideoView.setLayoutParams(localParams);
        localFrameLayout.addView(localVideoView);
        //添加遮罩
        boundShadowLayout = new BoundShadowLayout(this);
//        registeNoFocusViewId(boundShadowLayout.getId());
//        mFocusLayout.addIngoreIds(boundShadowLayout.getId());
        boundShadowLayout.bindSurfaceView(localVideoView);
        boundShadowLayout.setData(RoomClient.get().getLayoutData());
        localFrameLayout.addView(boundShadowLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        boundShadowLayout.setOnItemClickListener(onItemClickListener);
    }


    private void finishAndRelease() {
        //二次确认弹窗
        VideoControlManager.getInstance().finishAndReleaseDialog(this, () -> {
            isClickFinish = true;
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
                L.d("do this ---->>>doHangup");
                webRTCManager.stopShareHdmi();
                RoomClient.get().leaveRoom(userId, channelId, STREAM_SHARING);
            }
            webRTCManager.doDestroy(MCUVideoActivity.class);
        }
        if (localVideoView != null) {
            localVideoView.release();
        }
        localVideoViewContainer.removeAllViews();
        closePresentation();
        RoomClient.get().setLocalHdmiParticipant(null);
        RoomClient.get().setLocalScreenParticipant(null);
        RoomClient.get().setLocalParticipant(null);
        RoomClient.get().getRemoteParticipant().clear();
        RoomClient.get().getMixFlows().clear();
        RoomClient.get().removeRoomEventCallback(MCUVideoActivity.class);
    }

    @Override
    public void roomSettingControl(View view) {
        roomSetting(view);
//        dispatchMovedToSecond();
    }

    @Override
    public void splitScreen0Control() {
        if (RoomManager.get().isHost()) {
            RoomClient.get().setConferenceLayout(4, false);
        }
    }

    @Override
    public void splitScreen4Control(int count, boolean b) {
        if (RoomManager.get().isHost()) {
            RoomClient.get().setConferenceLayout(count, b);
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

    public void switchCamera(View view) {
        webRTCManager.switchCamera();
    }

    public void leaveRoom() {
        if (!channelRole.equals(ROLE_PUBLISHER_SUBSCRIBER)) {
            RoomClient.get().leaveRoom(userId, channelId, STREAM_MAJOR);
        } else {
            RoomClient.get().closeRoom(channelId);
        }
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

    public void allScreen(String connectionId) {
        Log.i("allScreen", "allScreen" + connectionId + "MyconnectId" + connectId);
        isAllScreen = true;
        currentConnectId = connectionId;
        removeParent(localVideoView);
        if (mixShare != null) {
            stopCurrentMedia(MIX_MAJOR_AND_SHARING);
        }
        allView();
        removeParent(allScreenView);
        removeParent(localScreenView);
        ZoomInRooms(connectionId);
        iv_narrow.setVisibility(View.GONE);
        iv_narrow.requestFocus();
        iv_narrow.setNextFocusDownId(R.id.iv_narrow);
        iv_narrow.setNextFocusUpId(R.id.iv_narrow);
        iv_narrow.setNextFocusLeftId(R.id.iv_narrow);
        iv_narrow.setNextFocusRightId(R.id.iv_narrow);
        PTZControlUtil.INSTANCE.start(connectionId);
    }

    public void allScreenByShare() {
        isAllScreenByShare = true;
        removeParent(presentationHDMIVideoView);
        presentationHDMIVideoView.setZOrderMediaOverlay(true);
        if (RoomManager.get().isHost()) {
            localScreenView.setZOrderMediaOverlay(false);
        } else {
            localVideoView.setZOrderMediaOverlay(false);
        }
        all_frameLayout.addView(presentationHDMIVideoView, 0);
        iv_narrow.setVisibility(View.GONE);
        iv_narrow.requestFocus();
        iv_narrow.setNextFocusDownId(R.id.iv_narrow);
        iv_narrow.setNextFocusUpId(R.id.iv_narrow);
        iv_narrow.setNextFocusLeftId(R.id.iv_narrow);
        iv_narrow.setNextFocusRightId(R.id.iv_narrow);
    }

    public void resetScreenByShare() {
        isAllScreenByShare = false;
        removeParent(presentationHDMIVideoView);
        presentationHDMIVideoView.setZOrderMediaOverlay(false);
        if (RoomManager.get().isHost()) {
            if (mPresentation != null && mPresentation.isShowing()) {
                mPresentation.getFrameLayout().addView(presentationHDMIVideoView, 0);
            }
        } else {
            localFrameLayout.addView(presentationHDMIVideoView);
        }
        if (RoomManager.get().isHost()) {
            localScreenView.setZOrderMediaOverlay(true);
        } else {
            localVideoView.setZOrderMediaOverlay(true);
        }
        iv_narrow.setVisibility(View.GONE);
        iv_narrow.requestFocus();
        iv_narrow.setNextFocusDownId(R.id.iv_narrow);
        iv_narrow.setNextFocusUpId(R.id.iv_narrow);
        iv_narrow.setNextFocusLeftId(R.id.iv_narrow);
        iv_narrow.setNextFocusRightId(R.id.iv_narrow);
    }

    public void resetScreen(View view) {
        Log.i("allScreen", "resetScreen" + currentConnectId + "MyconnectId" + connectId);
        isAllScreen = false;

        iv_narrow.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(currentConnectId) && !currentConnectId.equals(connectId)) {
            stopCurrentSFUMedia(currentConnectId);
        }
        if (mixShare == null && !isMixShareMajor) {
            isMixShareMajor = true;
            getCurrentMedia(MIX_MAJOR_AND_SHARING);
        }
        removeParent(localVideoView);
        removeParent(allScreenView);
        removeParent(localScreenView);
        localFrameLayout.addView(localVideoView, 0);
        if (localScreenView != null) {
            localScreenView.setZOrderMediaOverlay(true);
        }
        resetScreenSink();
        PTZControlUtil.INSTANCE.release();
    }

    public void resetScreenSink() {

        if (sfuPerson != null) {

            sfuPerson.setTarget(null);
            sfuPerson = null;
            currentConnectId = "";
        }
    }


    public void removeParent(View view) {
        if (view != null) {
            ViewGroup parentViewGroup = (ViewGroup) view.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeView(view);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisplayManager.unregisterDisplayListener(this);
        handler.removeMessages(1);
        handler = null;
        thread = null;

    }

    @Override
    public void onBackPressed() {
        if (isAllScreen) {
            resetScreen(all_frameLayout);
        }
        if (isAllScreenByShare) {
            resetScreenByShare();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RoomClient.get().setCurrentActivity(this);

//        mFocusLayout = new FocusLayout(this);
//        bindListener();//绑定焦点变化事件
//        addContentView(mFocusLayout,
//                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT));//添加焦点层
        registeNoFocusViewId(R.id.iv_see, R.id.local_video_container, R.id.fl_contair);
//        mFocusLayout.addIngoreIds(R.id.iv_see);
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
        DeviceSettingManager.getInstance().endCameraControl();
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


    //某userId 掉线,画面处理
    public void setViewBreak(String screenUid) {

    }


    public void stopCurrentMedia(String streamModel) {
        Log.i("stopCurrentMedia", "stopCurrentMedia" + RoomClient.get().getMixFlow().size());
        if (RoomClient.get().getMixFlow() != null) {
            for (ParticipantInfoModel participantInfoModel : RoomClient.get().getMixFlow()) {
                if (streamModel.equals(participantInfoModel.getStreamType())) {
                    Log.i("stopCurrentMedia", "stopCurrentMedia" + participantInfoModel.getStreamType() + ".." + participantInfoModel.getConnectionId());
                    RoomClient.get().unsubscribeFromVideo(participantInfoModel.getConnectionId(), participantInfoModel.getStreamType());
                    webRTCManager.removePc(participantInfoModel);
                    break;
                }
            }
        }
        if (streamModel.equals(MIX_MAJOR_AND_SHARING)) {
            if (mixShare != null) {
                mixShare.setTarget(null);
                mixShare = null;
            }
            isMixShareMajor = false;
        } else if (streamModel.equals(SFU_SHARING)) {
            if (sfuShare != null) {
                sfuShare.setTarget(null);
                sfuShare = null;
                isShareSFU = false;
            }
        }
    }

    public void stopCurrentSFUMedia(String currentConnectId) {
        if (currentConnectId.equals(RoomManager.get().getConnectionId()) && !RoomManager.get().isHost()) {
            localStream.videoTracks.get(0).removeSink(mixShare);
            return;
        }

        if (RoomClient.get().getRemoteParticipants() != null) {
            for (ParticipantInfoModel participantInfoModel : RoomClient.get().getRemoteParticipants()) {
                if (currentConnectId.equals(participantInfoModel.getConnectionId())) {
                    Log.i("stopCurrentMedia", "stopCurrentMedia" + participantInfoModel.getStreamType() + ".." + participantInfoModel.getConnectionId());
                    RoomClient.get().unsubscribeFromVideo(participantInfoModel.getConnectionId(), participantInfoModel.getStreamType());
                    webRTCManager.removePc(participantInfoModel);
                    break;
                }
            }
        }
    }

    public void getCurrentMedia(String streamModel) {
        if (RoomClient.get().getMixFlow() != null) {
            for (ParticipantInfoModel participantInfoModel : RoomClient.get().getMixFlow()) {
                if (streamModel.equals(participantInfoModel.getStreamType())) {
                    webRTCManager.createOfferByMix(participantInfoModel);
                }
            }
        }
    }

    public void getMySmallMedia() {
        if (RoomManager.get().isHost()) {

        }
    }

    public void replaceSFUMedia(String userId) {
        stopCurrentSFUMedia(currentConnectId);
        String connectId = RoomClient.get().getQueueMaps().get(userId);
        Log.i("parseParti", "getSFUMedia" + connectId);
        if (!TextUtils.isEmpty(connectId)) {
            if (RoomClient.get().getRemoteParticipant().get(connectId) != null) {
                if (userId.equals(RoomManager.get().getUserId())) {
                    addOtherView(userId, localStream, RoomClient.get().getLocalParticipant());
                } else {
                    webRTCManager.onParticipantPublished(RoomClient.get().getRemoteParticipant().get(connectId));
                }
            }
        }
    }

    public void getSFUMedia(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        String connectId = RoomClient.get().getQueueMaps().get(userId);
        Log.i("parseParti", "getSFUMedia" + connectId);
        if (!TextUtils.isEmpty(connectId)) {
            if (RoomClient.get().getRemoteParticipant().get(connectId) != null) {
                if (userId.equals(RoomManager.get().getUserId())) {
                    addOtherView(userId, localStream, RoomClient.get().getLocalParticipant());
                } else {
                    webRTCManager.onParticipantPublished(RoomClient.get().getRemoteParticipant().get(connectId));
                }
            }
        }
    }

    public void removeShareView() {
        Log.i("closePresentMedia", "removeShareView" + isShareIng);
        if (mPresentation != null && mPresentation.isShowing()) {
            mPresentation.getFrameLayout().removeView(mPresentation.getFrameLayout().getChildAt(0));
            mPresentation.getImageView().setVisibility(View.VISIBLE);
            if (sfuShare != null) {
                Log.i("closePresentMedia", "stopCurrentMedia");
                stopCurrentMedia(SFU_SHARING);
            }
        } else if (!RoomManager.get().isHost()) {
            addShareByPartNoScreen(false);
        }
        closePresentation();

    }

    public void addPresentMedia() {
        Log.i("closePresentMedia", "addPresentMedia" + isShareIng);
        if (isShareIng) {
            if (mPresentation != null && mPresentation.isShowing()) {
                initPresentSurface(true);
            }
            if (mixShare.getTarget() == presentationHDMIVideoView) {
                webRTCManager.shareHdmi(localVideoView);
            } else {
                webRTCManager.shareHdmi(presentationHDMIVideoView);
            }
            isScreenMain(DeviceSettingManager.getInstance().getFromSp().isScreenMain());
        } else {
            if (mPresentation != null && mPresentation.isShowing()) {
                //接上辅屏，先展示图片
                mPresentation.getFrameLayout().removeView(mPresentation.getFrameLayout().getChildAt(0));
                mPresentation.getImageView().setVisibility(View.VISIBLE);
                if (isShareHas) {
                    getCurrentMedia(SFU_SHARING);
                }
            }
        }
    }

    public void closePresentMedia() {
        Log.i("closePresentMedia", "closePresentMedia" + isShareIng);
        if (mixShare.getTarget() == presentationHDMIVideoView) {
            Log.i("isScreenMain", "isScreenPrensent");
            mixShare.setTarget(localVideoView);
            webRTCManager.shareHdmi(presentationHDMIVideoView);
            boundShadowLayout.setVisibility(View.VISIBLE);
            localScreenView.setVisibility(View.VISIBLE);
        }


        if (isShareIng) {
            webRTCManager.shareHdmi(null);
        } else {
            if (isShareHas) {
                stopCurrentMedia(SFU_SHARING);
            }
        }
    }

    /********************************信令发起相关************************************/


    public void addTime(View view) {
        RoomClient.get().roomDelay(RoomManager.get().getRoomId());
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

    /********************************平板会控相关************************************/
    private PadEvent padEvent = new PadEvent() {
        @Override
        public void accessPadIn(PadModel padModel) {

        }

        @Override
        public void setAudio() {

            videoController.switchAudio();
        }

        @Override
        public void setVideo() {

            videoController.switchVideo();
        }

        @Override
        public void setSpeak() {

            videoController.switchSpeaker();
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
                Log.i("onParticipantJoined", "onParticipantJoined" + participant.getStreamType());
//                if (RoomManager.get().isHost()) {
                if (SHARING.equals(participant.getStreamType())) {
                    getAllParticipants(channelId);
                    return;
                }
                participantModels = RoomManager.get().getParticipants();
                videoController.updateJoinPersons();
//                }
            });
        }

        @Override
        public void closeRoomNotify() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("GroupActivity......", "closeRoomNotify");
                    if (MCUVideoActivity.this.isFinishing()) {
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
                    if (MCUVideoActivity.this.isFinishing()) {
                        return;
                    }
                    String userId = participant.getUserId();

                    if (RoomManager.get().getConnectionId().equals(connectId)) {
                        doHangup();
                        RoomClient.get().setHasHands(false);
                        finish();
                    } else {
                        L.d("do this --->>>>onParticipantEvicted  " + participant);

                        if (STREAM_SHARING.equals(participant.getStreamType())) {
                            onStopShareScreen(participant.getUserId());
                            isShareHas = false;
                            RoomClient.get().setCurrentShareUser("");
                            userId = userId + LOCAL_SCREEN;
                        }
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
                    L.d("do this ---onParticipantLeft>>>");
                    if (participant == null) {
                        return;
                    }
                    if (MCUVideoActivity.this.isFinishing()) {
                        return;
                    }
                    L.d("do this ----onParticipantLeft >>>" + "userId" + userId + ", " + participant.getStreamType());
                    String userId = participant.getUserId();

                    if (STREAM_SHARING.equals(participant.getStreamType())) {

                        onStopShareScreen(userId);
                        isShareHas = false;
                        RoomClient.get().setCurrentShareUser("");
                        userId = userId + LOCAL_SCREEN;
                        getAllParticipants(channelId);
                    } else {
                        videoController.setCurrentHandUpNum(RoomClient.get().getCurrentLeftHandsUp());
                        videoController.showDotNumber(String.valueOf(RoomClient.get().getCurrentLeftHandsUp()));
                        RoomClient.get().setShowRollCall(false);
                    }
                    L.d("do this ----leftSink()>>>" + userId + " . ");
                    webRTCManager.removePc(participant);
                    videoController.updateJoinPersons();
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
                        L.d("do this ---reconnectPartStopPublishSharing>>>");
                        onStopShareScreen(userId);
                        isShareHas = false;
                        RoomClient.get().setCurrentShareUser("");
                    }
                    webRTCManager.removePc(participant);
                    //更新文字
                    videoController.updateJoinPersons();
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
                onSetShareStatus(model);
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
                        videoController.bodyRollCallByHost(handsStatusResp);
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
                    videoController.onPutDownHand(handsStatusResp);
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
//            if (role.equals(SHARING)) {
//                onStopShareScreen(userId);
//            }

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
            if (RoomClient.get().getCurrentActivity() instanceof MCUVideoActivity) {
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
                            CrashReport.postCatchedException(new Throwable(wsErrorr.getMessage()));
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
                L.d("do this ---endRollcall>>>>");
                EndRollCall(slingModel);
            });
        }

        @Override
        public void getSubDevOrUser(PartDeviceModel orgList) {
            runOnUiThread(() -> {
                if (RoomClient.get().getCurrentActivity() instanceof MCUVideoActivity) {
                    videoController.notifyDeviceData(orgList.getDeviceList());
                }
            });
        }

        @Override
        public void getDepartmentTree(CompanyModel companyModel) {
            runOnUiThread(() -> {
                if (RoomClient.get().getCurrentActivity() instanceof MCUVideoActivity) {
                    videoController.notifyOrgTreeList(companyModel);
                }
            });
        }

        @Override
        public void getGroupList(GroupListInfoModel groupListInfoModel) {
            runOnUiThread(()->{
                if (RoomClient.get().getCurrentActivity() instanceof MCUVideoActivity) {
                    videoController.setSubGroupItem(groupListInfoModel);
                }
            });
        }

        @Override
        public void getGroupInfo(GroupInfoModel groupInfoModel) {
            runOnUiThread(()->{
                if (RoomClient.get().getCurrentActivity() instanceof MCUVideoActivity) {
                    videoController.notifyGroupDeviceData(groupInfoModel.getGroupInfo());
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
                videoController.roomDelay();
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
                L.d("do this ---->>>>>userBreakLine : " + lastBreakLineUserId + ", " + userId);
                if (!lastBreakLineUserId.equals(userId)) {
                    setViewBreak(userId);
                    lastBreakLineUserId = userId;
                }
                if (participantModels != null) {
                    for (ParticipantInfoModel infoModel : participantModels) {
                        if (infoModel.getUserId().equals(model.getUserId()) && MAJOR.equals(infoModel.getStreamType())) {
                            infoModel.setOnlineStatus(Constants.OFFLINE);
                            break;
                        }
                    }
                    boundShadowLayout.setData(RoomClient.get().getLayoutData());

                    if (localScreenView != null) {
                        boundShadowLayout.addSelfStream(connectId, localScreenView);
                    }
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
                    UCToast.show(MCUVideoActivity.this, "用户拒绝入会");
                }
            });

        }

        //发送成功
        @Override
        public void onInviteJoinCallback() {
            runOnUiThread(() -> {
                videoController.inviteJoin(inviteNum);
            });
        }

        @Override
        public void conferenceLayoutChanged(List<ParticipantInfoModel> participantInfoModels) {
            if (boundShadowLayout != null) {

                App.post(() -> {
                    getmFocusLayout().clearFocusView();
                    boundShadowLayout.setData(participantInfoModels);
                    if (RoomManager.get().isHost() && !isShareHas) {
                        if (localScreenView != null) {
                            boundShadowLayout.addSelfStream(connectId, localScreenView);
                        }
                    }
                });
            }
        }

        @Override
        public void distributeShareCastPlayStrategyNotify(StrategyModel strategyModel) {
            if (strategyModel != null) {

                App.post(() -> {

                    isScreen = strategyModel.getShareCastPlayStrategy().equals(WssMethodNames.MajorPlay);
                    if (!isShareIng) {
                        if (isShareHas) {
                            isScreenMainReceive(isScreen);
                        }
                    } else {
                        isScreenMain(isScreen);
                    }
                });
            }
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

        @Override
        public void mediaServerReconnect() {
            App.post(() -> {
                L.e("receive mediaServerReconnect");
                if (mixShare != null) {

                    stopCurrentMedia(MIX_MAJOR_AND_SHARING);
                }
                if (sfuShare != null) {

                    stopCurrentMedia(SFU_SHARING);
                }
                if (RoomClient.get().getLocalParticipant() != null) {
                    removeParent(localScreenView);
                    localScreenView.release();
                    localScreenView = null;
                    stopCurrentSFUMedia(RoomClient.get().getLocalParticipant().getConnectionId());
                    webRTCManager.setMediaNull();
                    webRTCManager.createOffer(RoomClient.get().getLocalParticipant());
                }
            });


        }
    };


    public void onGetParticipants(ArrayList<ParticipantInfoModel> models) {
        Log.i(logTag, "join...onGetParticipants:");
        //json 处理，所有与会者
        if (participantModels != null) {
            participantModels.clear();
        }
        if (tempParticpantModels != null) {
            tempParticpantModels.clear();
        }
        if (models != null) {
            RoomManager.get().setParticipants(models);
        }
        participantModels = RoomManager.get().getParticipants();
        tempParticpantModels.addAll(participantModels);
        videoController.refreshParticipant(participantModels);
        videoController.refreshRollCallPop();
        if (meetSettingPopupWindow != null) {
            int myIndex = -1;

            for (int i = 0; i < tempParticpantModels.size(); i++) {
                if (SPEditor.instance().getUserId().equals(tempParticpantModels.get(i).getUserId())) {
                    myIndex = i;
                }
            }
            L.d("do this --->>>1");
            if (myIndex != -1) {
                tempParticpantModels.remove(myIndex);
            }
            meetSettingPopupWindow.showNextPopup(tempParticpantModels);
        }

        if (boundShadowLayout != null) {
            boundShadowLayout.setData(RoomClient.get().getLayoutData());
        }
        if (RoomManager.get().isHost() && !isShareHas) {
            if (localScreenView != null) {
                boundShadowLayout.addSelfStream(connectId, localScreenView);
            }
        }

    }

    /**
     * 语音 状态 变更
     */

    public void onSetAudioStatus(AudioStstusResp audioStatus) {
        String sourceId = audioStatus.getSourceId();
        boolean status = getSlingStatus(audioStatus.getStatus(), CLRtcSinaling.VALUE_STATUS_ON);
        L.d("onSetAudioStatus " + audioStatus.toString() + RoomManager.get().getUserId());
        String[] targetIds = audioStatus.getTargetIds();
        //web 会控  targetIds 为空，默认全体操作
        if (targetIds == null || targetIds.length == 0) {
            if (!RoomManager.get().isHost()) {
                if (status == RoomClient.get().getRemoteParticipant().get(connectId).isAudioActive()) {
                    L.d("do this ----->>>" + RoomClient.get().getRemoteParticipant().get(connectId).isAudioActive() + " , " + status);
                    return;
                }
                videoController.audioStatus(status);
                UIUtils.toastMessage(status ? "主持人打开了您的麦克风" : "主持人关闭了您的麦克风");
                RoomManager.get().setHostByClose(!status);
            }

            for (ParticipantInfoModel participantModel : participantModels) {
                if (sourceId.equals(participantModel.getUserId())) {
                    continue;
                }
                participantModel.setAudioActive(status);

            }
            boundShadowLayout.setData(RoomClient.get().getLayoutData());
            if (localScreenView != null) {
                boundShadowLayout.addSelfStream(connectId, localScreenView);
            }
            return;
        }
        for (String targetId : targetIds) {
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
                    RoomManager.get().setHostByClose(!status);
                }

            }
            /**
             * 修改列表状态
             */
            if (participantModels == null) {
                return;
            }
            for (ParticipantInfoModel participantModel : participantModels) {
                if ((TextUtils.isEmpty(targetId) || participantModel.getUserId().equals(targetId))) {
                    participantModel.setAudioActive(status);
                }
            }
        }
        //更新每个视频窗口上显示的状态
        boundShadowLayout.setData(RoomClient.get().getLayoutData());
        if (localScreenView != null) {
            boundShadowLayout.addSelfStream(connectId, localScreenView);
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
            /**
             * 修改列表状态
             */
            if (participantModels == null) {
                return;
            }
            for (ParticipantInfoModel participantModel : participantModels) {
                if ((TextUtils.isEmpty(targetId) || participantModel.getUserId().equals(targetId))) {
                    participantModel.setVideoActive(status);
                }
            }
        }
        //更新每个视频窗口上显示的状态
        boundShadowLayout.setData(RoomClient.get().getLayoutData());
        if (localScreenView != null) {
            boundShadowLayout.addSelfStream(connectId, localScreenView);
        }
    }

    /**
     * 关闭共享流 （目前只有关闭）
     */
    public void onSetShareStatus(SlingModel model) {
        String targetId = model.getTargetId();
        if (TextUtils.isEmpty(targetId)) {
            return;
        }

        boolean status = false;
        L.d("onSetShareStatusV2: " + model.toString());
        if (userId.equals(targetId)) {
            videoController.shareStatus(status);
            if (isShareHas && isShareIng) {
                stopShareScreen2Other();
            }
        }
        if (participantModels == null) {
            return;
        }
        for (ParticipantInfoModel participantModel : participantModels) {
            if ((TextUtils.isEmpty(targetId) || participantModel.getUserId().equals(targetId))) {
                participantModel.setShareStatus(status ? "on" : "off");
            }
        }
        //更新每个视频窗口上显示的状态
        boundShadowLayout.setData(RoomClient.get().getLayoutData());
        if (localScreenView != null) {
            boundShadowLayout.addSelfStream(connectId, localScreenView);
        }
    }

    private void stopShareScreen2Other() {
        isShareIng = false;
        isShareHas = false;
        stopScreen();
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
        }
        if (participantModels == null) {
            return;
        }
        for (ParticipantInfoModel participantModel : participantModels) {
            if ((TextUtils.isEmpty(targetId) || participantModel.getUserId().equals(targetId))) {
                participantModel.setSpeakerActive(status);
            }
        }
        //更新每个视频窗口上显示的状态
        boundShadowLayout.setData(RoomClient.get().getLayoutData());
        if (localScreenView != null) {
            boundShadowLayout.addSelfStream(connectId, localScreenView);
        }
    }

    /**
     * @param videoStatus
     */
    public void OnRollCall(SlingModel videoStatus) {
        videoController.setCurrentSpeakNum(1);
        RoomClient.get().setHasHands(true);
        String targetId = videoStatus.targetId;
        String sourceId = videoStatus.sourceId;
        currentSpeakUserId = targetId;
        boolean status = getSlingStatus(videoStatus.status, CLRtcSinaling.VALUE_STATUS_ON);
        L.d("onSetAudioStatusV2: " + videoStatus.toString());

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
        for (ParticipantInfoModel participantInfoModel : participantModels) {
            if (targetId.equals(participantInfoModel.getUserId())) {
                participantInfoModel.setHandStatus("speaker");
            }
        }
        videoController.showDotNumber(videoStatus.getRaiseHandNum());
        if (!RoomManager.get().isHost()) {
            replaceSFUMedia(targetId);
        }

        if (targetId.equals(screenId)) {
            return;
        }
    }

    public void EndRollCall(SlingModel videoStatus) {
        videoController.setCurrentSpeakNum(0);
        RoomClient.get().setShowRollCall(false);
        RoomClient.get().setHasHands(false);
        String targetId = videoStatus.targetId;
        String sourceId = TextUtils.isEmpty(videoStatus.sourceId) ? RoomManager.get().getHostId() : videoStatus.sourceId;
        currentSpeakUserId = "";
        for (ParticipantInfoModel participantInfoModel : participantModels) {
            if (targetId.equals(participantInfoModel.getUserId())) {
                participantInfoModel.setHandStatus("down");
            }
        }
        boolean status = getSlingStatus(videoStatus.status, CLRtcSinaling.VALUE_STATUS_ON);
        L.d("onSetAudioStatusV2: " + videoStatus.toString());
        if (isAllScreen) { //先结束全屏再
            resetScreen(all_frameLayout);
        }
        //分辨率切换
        if (targetId.equals(userId)) {
            webRTCManager.changeCaptureFormat(false);
        } else if (RoomManager.get().getHostId().equals(userId)) {
            webRTCManager.changeCaptureFormat(true);
        }
        if (!RoomManager.get().isHost()) {
            replaceSFUMedia(sourceId);
        }
        if (sourceId.equals(screenId)) {
            return;
        }
        if (targetId.equals(userId)) {
            videoController.endCallByMe();
        }
        if (!TextUtils.isEmpty(videoStatus.raiseHandNum)) {
            videoController.setCurrentHandUpNum(Integer.parseInt(videoStatus.raiseHandNum));
            videoController.showDotNumber(videoStatus.raiseHandNum);

        }
        L.d("do this ---EndRollCall>>> " + sourceId + " , " + sourceId);

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
        isScreenMainRemove(DeviceSettingManager.getInstance().getFromSp().isScreenMain());
        removeShareView();
        Map<String, ParticipantInfoModel> participantMap = RoomClient.get().getRemoteParticipant();
        for (Map.Entry<String, ParticipantInfoModel> entry : participantMap.entrySet()) {
            if (entry.getValue().getUserId().equals(userId) && STREAM_SHARING.equals(entry.getValue().getStreamType())) {
                participantMap.remove(entry.getKey());
                break;
            }
        }
    }

    /************************************HDMI***********************************/
    public void shareScreenHDMI(View view) {
        if (!enableShare && shareHDMI.getText().equals("共享屏幕") && !RoomManager.get().isRoomAllowShare()) {
            UCToast.show(MCUVideoActivity.this, "主持人已关闭共享操作");
            return;
        }
        if (ViewUtils.isFastClickLong()) {
            return;
        }
        if (screenWidth < screenHeigth) {
            UIUtils.toastMessage("目前只支持vhd设备");
            return;
        }

        int[] idArray = new int[2];
        if (!CameraEncoderv2.hasValidCamera(idArray)) {
            Toast.makeText(MCUVideoActivity.this, "no vhd camera", Toast.LENGTH_LONG).show();
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
            UIUtils.toastMessage(RoomClient.get().getCurrentShareUser() + "正在共享屏幕，请稍后再试");
            return;
        }

        if (shareHDMI.getText().equals("共享屏幕")) {
            if (!SystemUtil.isHdmiinConnected()) {
                UIUtils.toastMessage("请检查您的HDMI IN连接");
                return;
            }
            initPresentation(false);
            isShareIng = true;
            isShareHas = true;
            if (mPresentation != null && mPresentation.isShowing()) {
                presentAdd(true);
            } else {
                if (!RoomManager.get().isHost()) {
                    presentAdd(false);
                }
            }
            if (RoomManager.get().isHost()) {
                getSFUMedia(currentSpeakUserId);
            }
            isScreenMain(DeviceSettingManager.getInstance().getFromSp().isScreenMain());
            webRTCManager.shareHdmiJoin();
            shareHDMI.setText("结束共享");
        } else {
            isShareIng = false;
            isShareHas = false;
            onStopShareScreen(userId);

            webRTCManager.stopShareHdmi();
            shareHDMI.setText("共享屏幕");
            RoomClient.get().leaveRoom(userId, channelId, STREAM_SHARING);
            getAllParticipants(channelId);
            closePresentation();
        }
    }

    public void isScreenMain(boolean isScreen) {
        Log.i("isScreenMain", "isScreenMain" + isScreen);
        if (mDisplayManager.getDisplays().length > 1) {
            if (!isScreen) {
                if (PRESENT_TAG.equals(localVideoViewContainer.getChildAt(0).getTag())) {
                    if (RoomManager.get().isHost()) {
                        Log.i("isScreenMain", "isScreenPrensent");
                        changeScreen(isScreen);
                    }
                }
            } else {
                if (MAIN_TAG.equals(localVideoViewContainer.getChildAt(0).getTag())) {
                    Log.i("isScreenMain", "isScreenMain");
                    changeScreen(isScreen);
                    if (RoomManager.get().isHost()) {
                        addShareByHost();
                    }
                }
            }
        }
    }

    public void isScreenMainRemove(boolean isScreen) {
        Log.i("isScreenMain", "isScreenMain" + isScreen);
        if (mDisplayManager.getDisplays().length > 1) {
            if (isScreen) {
                if (PRESENT_TAG.equals(localVideoViewContainer.getChildAt(0).getTag())) {
                    Log.i("isScreenMain", "isScreenPrensent");

                    changeScreen(false);
                    if (RoomManager.get().isHost()) {
                        if (!TextUtils.isEmpty(RoomClient.get().getQueueMaps().get(currentSpeakUserId))) {
                            stopCurrentSFUMedia(RoomClient.get().getQueueMaps().get(currentSpeakUserId));
                            addNewView(localStream);
                        }
                        removeParent(localScreenView);
                        boundShadowLayout.addSelfStream(connectId, localScreenView);
                    }
                }
            }
        }
    }

    public void changeScreen(boolean isScreen) {
        if (isScreen) {
            removeParent(localFrameLayout);
            removeParent(mPresentation.getFrameLayout());
            localVideoViewContainer.addView(mPresentation.getFrameLayout(), 0);
            mPresentation.getLinearLayout().addView(localFrameLayout);
        } else {
            removeParent(localFrameLayout);
            removeParent(mPresentation.getFrameLayout());
            localVideoViewContainer.addView(localFrameLayout, 0);
            mPresentation.getLinearLayout().addView(mPresentation.getFrameLayout());
        }
    }

    public void isScreenMainReceive(boolean isScreen) {
        Log.i("isScreenMain", "isScreenMain" + isScreen);
        if (mDisplayManager.getDisplays().length > 1) {
            if (!isScreen) {
                if (PRESENT_TAG.equals(localVideoViewContainer.getChildAt(0).getTag())) {
                    Log.i("isScreenMain", "isScreenPrensent");
                    changeScreen(isScreen);
                }
            } else {
                if (MAIN_TAG.equals(localVideoViewContainer.getChildAt(0).getTag())) {
                    Log.i("isScreenMain", "isScreenMain");
                    changeScreen(isScreen);
                    if (RoomManager.get().isHost() && localVideoView != null) {
                        addShareByHost();
                    }
                }
            }
        }
    }

    public void stopScreen() {
        if (mixShare != null && mixShare.getTarget() == presentationHDMIVideoView) {
            Log.i("isScreenMain", "stopScreen");
            mixShare.setTarget(localVideoView);
            boundShadowLayout.setVisibility(View.VISIBLE);
            localScreenView.setVisibility(View.VISIBLE);
            webRTCManager.shareHdmi(null);
        }
    }

    public void initPresentSurface(boolean twoScreen) {
        if (presentationHDMIVideoView == null) {
            presentationHDMIVideoView = webRTCManager.createRendererView(getApplicationContext());
            presentationHDMIVideoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            presentationHDMIVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            presentationHDMIVideoView.setFocusable(true);
            presentationHDMIVideoView.setOnClickListener(v -> {
                allScreenByShare();
            });
        }

        removeParent(presentationHDMIVideoView);
        if (twoScreen) {
            mPresentation.getFrameLayout().addView(presentationHDMIVideoView, 0);
            mPresentation.getImageView().setVisibility(View.GONE);
        } else {
            if (!RoomManager.get().isHost()) {
                addShareByPartNoScreen(true);
            }
        }
    }

    /**
     * 与会者单屏时候的 辅流 和发言者（主持人）布局参数修改
     */
    public void addShareByPartNoScreen(boolean hasShare) {
        if (hasShare) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UIUtils.dip2px(640), UIUtils.dip2px(360));
            presentationHDMIVideoView.setLayoutParams(params);
            localFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(UIUtils.dip2px(360), UIUtils.dip2px(180)));
            localVideoViewContainer.addView(presentationHDMIVideoView, 0);
        } else {
            removeParent(presentationHDMIVideoView);
            localFrameLayout.setLayoutParams(localParams);
        }
    }

    /**
     * 主持人双屏时候的 辅流 和发言者（主持人）布局参数修改
     */
    public void addShareByHost() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UIUtils.dip2px(640), UIUtils.dip2px(360));
        mPresentation.getFrameLayout().setLayoutParams(params);
        localScreenView.setLayoutParams(new LinearLayout.LayoutParams(UIUtils.dip2px(360), UIUtils.dip2px(180)));
        removeParent(localScreenView);
        localVideoViewContainer.addView(localScreenView);
    }

    public void presentAdd(boolean twoScreen) {
        if (isShareHas) {

            initPresentSurface(twoScreen);
            webRTCManager.shareHdmi(presentationHDMIVideoView);
        }
    }

    public void HDMIAdd() {

    }

    /************************************分屏相关***********************************/
    public void initPresentation(boolean insert) {
        Display[] displays;//屏幕数组

        displays = mDisplayManager.getDisplays();
        L.d("do this --->>>> displays: " + displays.length);
        if (displays.length > 1) { //链接设备大于1 开启分屏
            mPresentation = new DifferentDislay(this, displays[1]);//displays[1]是副屏

            mPresentation.getWindow().setType(

                    WindowManager.LayoutParams.TYPE_TOAST);

            mPresentation.show();

//            mPresentation.setOnclickForDifferent(new DifferentDislay.OnclickForDifferent() {
//                @Override
//                public void onclickDiff() {
//                    UIUtils.toastMessage("分屏显示");
//                }
//            });
            //插拔问题暂时 隐藏
//            changeShareView(insert, false, prensatationId);
//            presentAdd(insert, localHDMIVideoView);
            if (insert) {
                addPresentMedia();
            }
        }
    }

    public void closePresentation() {

        if (mPresentation != null) {
            mPresentation.dismiss();
        }

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
                    resetScreen(all_frameLayout);
                    break;
                }
                isCurrentAllScreenSelf = true;
                finishAndRelease();
                break;
            case KeyEvent.KEYCODE_MENU:
//                videoController.showMenu();
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
                        thread = new Thread(() -> {
                            DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_UP);
                        });
                        thread.start();
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
                        thread = new Thread(() -> {
                            DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_DOWN);
                        });
                        thread.start();
                        return true;
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
                        thread = new Thread(() -> {
                            DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_LEFT);
                        });
                        thread.start();
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
                        thread = new Thread(() -> {
                            DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_RIGHT);
                        });
                        thread.start();
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
        videoController.getMenuManager().scheduleHide();
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

    private void setMemoryConfig(int key) {
        if (isCurrentAllScreenSelf && StringUtils.isIndexEqual1(strOfKeySet, key)) {
            DeviceSettingManager.getInstance().setCameraControl(GlobalValue.MEMORY_RECALL, String.valueOf(key));
        }
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
                    if (ActivityUtils.getInstance().getCurrent() instanceof MCUVideoActivity) {
//                        dismissNetError();
                        VideoControlManager.getInstance().dismissNetErrorPopup();
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
                if (ActivityUtils.getInstance().getCurrent() instanceof MCUVideoActivity) {
                    showNetError();
                    CallUtil.asyncCall(1000, () -> {
                        RoomClient.get().setWebSocketStateListener(null);
                        VideoControlManager.getInstance().dismissNetErrorPopup();
//                        dismissNetError();
                        doHangup();
                        releaseView();
                        L.d("do this ---onSockedConnectFail >>>>>" + currentSpeakUserId + " , userId: " + userId);
                        if (currentSpeakUserId.equals(userId)) {
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
//        if (netPopupWindow == null) {
//            netPopupWindow = new NetPopupWindow(this);
//        }
//        if (netPopupWindow.isShowing()) {
//            return;
//        }
//        netPopupWindow.showAtLocation(cl_main, Gravity.CENTER, 0, 0);
    }

    private void dismissNetError() {
        if (netPopupWindow != null && netPopupWindow.isShowing()) {
            netPopupWindow.dismiss();
            netPopupWindow = null;
        }
    }

    private void releaseView() {
        if (localVideoView != null) {
            localVideoView.release();
        }
        localVideoViewContainer.removeAllViews();
        finish();
    }


    private void setDuringTime() {
        long time = new Date(System.currentTimeMillis() - RoomManager.get().getCreateAt()).getTime();           //获取的服务器时间戳 快了本地获取系统时间 3s
        long m = time / (1000);
        if (m < 0) {
            m = 0;
        }
        videoController.setMeetDuring(m);
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
        L.d("do this ----onDisplayAdded>>> " + displayId + ", " + mDisplayManager.getDisplays().length);
        initPresentation(true);
    }

    /**
     * Called whenever a logical display has been removed from the system.
     *
     * @param displayId The id of the logical display that was removed.
     */
    @Override
    public void onDisplayRemoved(int displayId) {
        L.d("do this ----onDisplayRemoved>>> " + displayId + ", " + mDisplayManager.getDisplays().length);
//        L.d("do this ---onDisplayRemoved>>>" + mPresentation.getWindow().isActive());
        isScreenMainRemove(DeviceSettingManager.getInstance().getFromSp().isScreenMain());
        closePresentation();
//        changeShareView(true, true, userId + LOCAL_SCREEN);
//        HDMIAdd(true, presentationHDMIVideoView);
        closePresentMedia();
    }

    /**
     * Called whenever the properties of a logical display have changed.
     *
     * @param displayId The id of the logical display that changed.
     */
    @Override
    public void onDisplayChanged(int displayId) {
        L.d("do this ----onDisplayChanged>>> " + displayId + ", " + mDisplayManager.getDisplays().length);
    }

    public int getCurrentType() {
        return currentType;
    }

    public void setCurrentType(int currentType) {
        this.currentType = currentType;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    public void onRemoteLoginNotify() {
        isClickFinish = true;

        doHangup();
        RoomClient.get().setShowRollCall(false);
        RoomClient.get().setHasHands(false);
        lastBreakLineUserId = "";
        CallUtil.asyncCall(500, () -> {
            RoomClient.get().accessOut();
        });
    }
}
