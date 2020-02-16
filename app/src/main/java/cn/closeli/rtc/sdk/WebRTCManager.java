package cn.closeli.rtc.sdk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.EventPublish;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.peer.CLPeerAPI;
import cn.closeli.rtc.peer.CLPeerConnection;
import cn.closeli.rtc.peer.RTCObserver;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.RoomRTCEvent;
import cn.closeli.rtc.room.SudiRole;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.LooperExecutor;
import cn.closeli.rtc.utils.SPEditor;

import static cn.closeli.rtc.constract.Constract.MIX_MAJOR;

/**
 * webRTC 管理类
 */
public class WebRTCManager implements RTCObserver, RoomRTCEvent {
    private String logTag = this.getClass().getCanonicalName();
    //创建房间者
    public static final String ROLE_PUBLISHER_SUBSCRIBER = "MODERATOR";
    //加入房间者
    public static final String ROLE_PUBLISHER = "PUBLISHER";
    //未用
    public static final String ROLE_SUBSCRIBER = "SUBSCRIBER";
    //推流类型 主流
    public static final String MAJOR = "MAJOR";
    //辅流
    public static final String MINOR = "MINOR";
    //分享码流 辅屏
    public static final String SHARING = "SHARING";

    private Context context;
    private EglBase rootEglBase;
    private LooperExecutor executor;
    private CLPeerAPI clPeerAPI;
    private RoomClient roomClient;
    private String sessionId;
    private String userId;
    private String role;
    private Context mContext;
    private IViewCallBackWrapper viewCallback = new IViewCallBackWrapper();
    private boolean useCamEncoded;
    private String currentStreamType = MIX_MAJOR;           //当前码流模式

    public WebRTCManager() {
    }

    private static class SingletonHolder {
        private static WebRTCManager instance = new WebRTCManager();
    }


    public void addViewCallback(Class clazz, IViewCallback viewCallback) {
        this.viewCallback.addIViewCallBac(clazz, viewCallback);
        if (clPeerAPI != null) {
            clPeerAPI.setIViewCallBack(viewCallback);
        }
    }

    public static WebRTCManager get() {
        return WebRTCManager.SingletonHolder.instance;
    }

    public void initEgl(EglBase rootEglBase) {
        this.rootEglBase = rootEglBase;

        RoomClient.get().setRoomRTCEvent(this);

    }

    public void initExecutor() {
        this.executor = new LooperExecutor();
    }

    public void init(Context context) {
        this.context = context;
    }

    public void create() {
        if (executor == null) {
            executor = new LooperExecutor();
        }
        executor.requestStart();
    }

    public synchronized SurfaceViewRenderer createRendererView(Context context) {
        SurfaceViewRenderer view = new SurfaceViewRenderer(context);
        if (rootEglBase != null) {
            view.init(rootEglBase.getEglBaseContext(), null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            view.setVisibility(SurfaceView.VISIBLE);
        }
        return view;
    }

    public synchronized SurfaceViewRenderer createNoEGLRendererView(Context context) {
        SurfaceViewRenderer view = new SurfaceViewRenderer(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        view.setVisibility(SurfaceView.VISIBLE);
        return view;
    }

    public SurfaceViewRenderer setSurface(SurfaceViewRenderer view) {
        if (rootEglBase != null) {
            view.init(rootEglBase.getEglBaseContext(), null);
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            view.setVisibility(SurfaceView.VISIBLE);
        }
        return view;
    }


    public int initWebRtc(String channelId, String userId, String role, boolean useCamEncoded) {
        this.sessionId = channelId;
        this.userId = userId;
        this.role = role;
        this.useCamEncoded = useCamEncoded;

//        RoomClient.get().prepare("wss://" + Uri.parse(token).getAuthority() + "/openvidu", this);

//        RoomClient.get().prepare("wss://" + Uri.parse(token).getAuthority() + "/openvidu");
//        RoomClient.get().setRoomEventCallback(this);
        this.roomClient = RoomClient.get();

        //roomClient.connect();

        return 0;
    }

    public void initClPeer() {
        this.clPeerAPI = new CLPeerAPI(context, rootEglBase, this);
    }

    public int leaveChannel() {
        return 0;
    }


    public void enableLocalVideo(boolean enabled) {
        clPeerAPI.enableLocalVideo(enabled);
    }

    public void enableLocalSpeaker(boolean enabled) {
        clPeerAPI.enableLocalSpeaker(enabled);
    }

    public void enableLocalAudio(boolean enabled) {
        clPeerAPI.enableLocalAudio(enabled);
    }

    public void setAudioValue(int value) {
        clPeerAPI.setAudioValue(value);
    }

    public void switchCamera() {
        clPeerAPI.switchCamera();
    }

    public void changeCaptureFormat(boolean isGoUp) {
        if (clPeerAPI != null) {
            clPeerAPI.changeCaptureFormat(isGoUp);
        }
    }

    public int shareScreen(Intent data, SurfaceView localScreenVideoView) {
        String screenToken = SPEditor.instance().getToken();
        roomClient.joinRoom(sessionId, "", SudiRole.publisher, 1, Constants.STREAM_SHARING, Constract.JOIN_ACTIVE);

        clPeerAPI.shareScreen(data, localScreenVideoView);

        //offer 发送问题，在加入房间回调中
//        clPeerAPI.generateOffer(roomClient.getLocalScreenParticipant(), true);
        return 0;
    }

    public int stopShareScreen() {
        if (clPeerAPI != null) {
            clPeerAPI.stopShareScreen();
        }
        return 0;
    }

    public MediaStream getLocationMediaStream() {
        if (clPeerAPI != null) {
            return clPeerAPI.getLocationMediaStream();
        }
        return null;
    }
    public void setMediaNull(){
        if (clPeerAPI != null) {
             clPeerAPI.setMediaNull();
        }
    }

    public void shareHdmi(SurfaceViewRenderer surfaceHDMI) {
        if (clPeerAPI != null) {
            clPeerAPI.shareHdmi(surfaceHDMI);
        }
    }

    public void shareHdmiJoin() {
        roomClient.joinRoom(sessionId, "", SudiRole.publisher, 2, Constants.STREAM_SHARING, Constract.JOIN_ACTIVE);
    }

    public void stopLocalPreview() {
        if (clPeerAPI != null) {
            clPeerAPI.stopLocalPreview();
        }
    }

    public void stopHDMIPreview() {
        if (clPeerAPI != null) {
            clPeerAPI.stopHDMIPreview();
        }
    }

    public void stopShareHdmi() {
        if (clPeerAPI != null) {
            clPeerAPI.stopShareHdmi();
        }
    }

    public void createOffer(ParticipantInfoModel participant) {
        Log.i("addNewView", "createOffer");
        clPeerAPI.generateOffer(participant, true, useCamEncoded);
    }

    public void createOfferByMix(ParticipantInfoModel participant) {
        Log.i("addNewView", "createOfferByMix");
        clPeerAPI.generateOffer(participant, false, false);
    }

    public void startPreView(ParticipantInfoModel participant) {
        if (clPeerAPI != null) {
            clPeerAPI.startPreview(participant, true);
        }
    }

    public void removePc(ParticipantInfoModel participant) {
        if (clPeerAPI != null) {
            clPeerAPI.closeConnection(participant);
        }
    }

    /**
     * doDestroy 时解绑对应的class
     *
     * @param clazz
     */
    public void doDestroy(Class clazz) {
        this.viewCallback.removeIViewCallBac(clazz);
        if (clPeerAPI != null) {
            clPeerAPI.closeVideo();
        }
    }

    public void doRelease() {
        if (clPeerAPI != null) {
            clPeerAPI.closeALL();
            clPeerAPI = null;
        }
    }
    //<editor-fold desc="RTCObserver">

    /**
     * CLPeerConnectionManager.Observer implement
     */
    @Override
    public void onLocalSdpOfferGenerated(SessionDescription localSdpOffer, CLPeerConnection connection) {
        ParticipantInfoModel participant = connection.getParticipant();
        if (participant == null) {
            return;
        }
        String connectionId = participant.getConnectionId();
        String streamType = participant.getStreamType();
        //pulish local

        if (roomClient.getLocalParticipant() == null) {
            return;
        }
        if (roomClient.getLocalParticipant() != null && connectionId.equalsIgnoreCase(roomClient.getLocalParticipant().getConnectionId())
                || (roomClient.getLocalScreenParticipant() != null && connectionId.equalsIgnoreCase(roomClient.getLocalScreenParticipant().getConnectionId()))
                || (roomClient.getLocalHdmiParticipant() != null && connectionId.equalsIgnoreCase(roomClient.getLocalHdmiParticipant().getConnectionId()))) {
            roomClient.publishVideo(localSdpOffer.description, participant.getLocalStreamType());
        }
        //subscribe remote
        else {
            roomClient.receiveVideoFrom(streamType, connectionId, localSdpOffer.description);
        }

    }

    @Override
    public void onLocalSdpAnswerGenerated(SessionDescription localSdpAnswer, CLPeerConnection connection) {

    }

    @Override
    public void onIceCandidate(IceCandidate localIceCandidate, CLPeerConnection connection) {
        ParticipantInfoModel participant = connection.getParticipant();
        if (participant == null) {
            return;
        }
        roomClient.sendOnIceCandidate(participant.getConnectionId(), localIceCandidate, participant.getStreamType());
    }

    @Override
    public void onIceStatusChanged(PeerConnection.IceConnectionState state, CLPeerConnection connection) {

    }

    @Override
    public void onRemoteStreamAdded(MediaStream stream, CLPeerConnection connection) {
        ParticipantInfoModel participant = connection.getParticipant();
//        participant.setMediaStream(stream);
        if (viewCallback != null && participant != null) {
            Log.i(logTag, "<<<<<<<< onRemoteStreamAdded:id=" + connection.getParticipant().getConnectionId());
            EventPublish eventPublish = new EventPublish();
            eventPublish.setParticipantInfoModel(participant);
            eventPublish.setMediaStream(stream);
            EventBus.getDefault().postSticky(eventPublish);
            viewCallback.onAddRemoteStream(stream, participant);
        }
    }

    @Override
    public void onRemoteStreamRemoved(MediaStream stream, CLPeerConnection connection) {
        ParticipantInfoModel participant = connection.getParticipant();
        if (viewCallback != null && participant != null) {
            Log.i(logTag, "<<<<<<<< onRemoteStreamRemoved:id=" + participant.getConnectionId());
            viewCallback.onCloseRemoteStream(stream, participant);
        }
    }

    @Override
    public void onPeerConnectionError(String error) {

    }


    @Override
    public void onShareJoinRoomSuccess(ParticipantInfoModel participant) {
        Log.i("addNewView", "onJoinRoomSuccess");
        // 只有在辅流和共享流 才发送 ，主流在VideoConflence主动发送
//        if (!participant.getStreamType().equals(Constants.STREAM_MAJOR)) {
        if (clPeerAPI != null) {
            clPeerAPI.generateOffer(participant, true, false);
        }

//        }

    }

    @Override
    public void onParticipantPublished(ParticipantInfoModel participant) {
        //有新的推流者加入房间发送
        Log.i("addNewView", "onParticipantPublished  " + participant.getStreamType());
        if (ROLE_PUBLISHER.equals(role) || ROLE_PUBLISHER_SUBSCRIBER.equals(role)) {
            currentStreamType = participant.getStreamType();
            clPeerAPI.generateOffer(participant, false, false);

        }
    }

    @Override
    public void onMixPublished(ParticipantInfoModel participant, boolean isHasShare) {
        //有新的推流者加入房间发送
        Log.i("addNewView", "onMixPublished  " + participant.getStreamType());
        if (ROLE_PUBLISHER.equals(role) || ROLE_PUBLISHER_SUBSCRIBER.equals(role)) {
            viewCallback.onMixStream(participant, isHasShare);
        }
    }

    @Override
    public void onParticipant(ParticipantInfoModel participant) {

        //有新的推流者加入房间发送
        Log.i("addNewView", "onMixPublished  " + participant.getStreamType());
        if (ROLE_PUBLISHER.equals(role) || ROLE_PUBLISHER_SUBSCRIBER.equals(role)) {
            viewCallback.publishParticipantInfo(participant);
        }
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public void onParticipantUnPublished(ParticipantInfoModel participant) {

        clPeerAPI.closeConnection(participant);
    }

    @Override
    public void onRemoteSdpAnswerReceived(ParticipantInfoModel participant, SessionDescription sdp) {
        String connectionId = participant.getConnectionId();
        clPeerAPI.processAnswer(sdp, participant);
    }

    @Override
    public void onRemoteIceCandidateReceived(ParticipantInfoModel participant, IceCandidate iceCandidate) {
        clPeerAPI.addRemoteIceCandidate(iceCandidate, participant);
    }

    /**
     * 请求接受新与会者音视频码流
     *
     * @param mixMode      码流模式
     * @param connectionId
     * @param participant
     */
    public void getReceiveVideo(String mixMode, String connectionId, ParticipantInfoModel participant) {
        roomClient.receiveVideoFrom(mixMode, connectionId, clPeerAPI.getPeerConnection(participant).getPc().getLocalDescription().description);
    }

    public String getCurrentStreamType() {
        return currentStreamType;
    }

    public void setCurrentStreamType(String currentStreamType) {
        this.currentStreamType = currentStreamType;
    }
}
