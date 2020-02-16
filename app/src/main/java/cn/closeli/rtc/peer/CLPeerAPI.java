package cn.closeli.rtc.peer;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.sdk.IViewCallback;
import cn.closeli.rtc.utils.LooperExecutor;
import cn.closeli.rtc.utils.RoomManager;

public class CLPeerAPI {
    private String logTag = this.getClass().getCanonicalName();
    private static final String FIELD_TRIAL_VP9 = "WebRTC-SupportVP9/Enabled/";
    private static final String FIELD_TRIAL_AUTOMATIC_RESIZE = "WebRTC-MediaCodecVideoEncoder-AutomaticResize/Enabled/";
    private static final String FIELD_TRIAL_DISABLED_FRAME_DROP = "WebRTC-FrameDropper/Disabled/";

    private Context context;
    private EglBase eglBase;
    private PeerConnectionFactory peerConnectionFactorySwDecPrimary;
    private PeerConnectionFactory peerConnectionFactoryHwDecPrimary;
    private PeerConnectionFactory peerConnectionFactoryCamera;
    private PeerConnectionFactory peerConnectionFactoryHdmiIn;
    private RTCObserver observer;
    private CLLocalMediaManager localMediaManager;
    private LooperExecutor executor;
    private MediaConstraints sdpConstraints;
    private static String STUN = "stun:49.4.55.12:19302";
    private ArrayList<PeerConnection.IceServer> ICEServers;
    private CLPeerConnection pc;
    private Map<ParticipantInfoModel, CLPeerConnection> clPeerConnetions; //以与会者对象为key?
    private FakeVideoEncoderFactory fakeVideoEncoderFactoryCamera;
    private FakeVideoEncoderFactory fakeVideoEncoderFactoryHdmiIn;

    public CLPeerAPI(Context context, EglBase eglBase, RTCObserver observer) {
        this.context = context;
        this.eglBase = eglBase;
        this.observer = observer;
//        this.peerConnectionFactory = createPeerConnectionFactory();
        this.peerConnectionFactoryHwDecPrimary = createPeerConnectionFactory(true);
        this.peerConnectionFactorySwDecPrimary = createPeerConnectionFactory(false);
        this.fakeVideoEncoderFactoryCamera = new FakeVideoEncoderFactory();
        this.peerConnectionFactoryCamera = createPeerConnectionFactoryExt(this.fakeVideoEncoderFactoryCamera);
        this.fakeVideoEncoderFactoryHdmiIn = new FakeVideoEncoderFactory();
        this.peerConnectionFactoryHdmiIn = createPeerConnectionFactoryExt(this.fakeVideoEncoderFactoryHdmiIn);
        this.localMediaManager = new CLLocalMediaManager(context, eglBase);
        this.executor = new LooperExecutor();
        this.clPeerConnetions = new HashMap<>();
        initICEServers();
        offerOrAnswerConstraint();
        executor.requestStart();
        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO);
    }

    private void initICEServers() {
        ICEServers = new ArrayList<>();
        PeerConnection.IceServer iceServer = PeerConnection.IceServer.builder(STUN).createIceServer();
        ICEServers.add(iceServer);
//        ICEServers.add(PeerConnection.IceServer.builder("stun:119.23.239.146:3478").createIceServer());
    }

    /**
     * 建立通道发送offer （原本延迟去除）
     * 本端每次需要分三步，第一createPeerConnection，第二addStream，第三createOffer
     *
     * @param participant
     * @param includeLocalMedia
     */
    public synchronized void generateOffer(ParticipantInfoModel participant, boolean includeLocalMedia, boolean useCamEncoded) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i("addNewView", "generateOffer" + participant.getConnectionId());
                CLPeerConnection connection = getPeerConnection(participant);
                if (connection == null) {
                    Log.i("addNewView", "connection" + participant.getConnectionId());
                    connection = createPeerConnection(participant, useCamEncoded);

                    connection.addObserver(observer);
                    if (participant.getLocalStreamType() == 2) {
                        if (localMediaManager.getLocalMediaStreamHDMI() == null) {
                            localMediaManager.startShareHDMI(peerConnectionFactoryHdmiIn, fakeVideoEncoderFactoryHdmiIn);
                        }
                        connection.getPc().addStream(localMediaManager.getLocalMediaStreamHDMI());
                        CLLocalMediaManager.EncodeParam param = localMediaManager.getShareParam();
                        connection.getPc().setBitrate(param.minBitrate, param.currentBitrate, param.maxBitrate);
                    } else if (participant.getLocalStreamType() == 1) {
                        if (localMediaManager.getLocalScreenMediaStream() == null) {
                            localMediaManager.startShareScreen(peerConnectionFactoryHwDecPrimary);
                        }
                        connection.getPc().addStream(localMediaManager.getLocalScreenMediaStream());
                    } else if (includeLocalMedia) {
                        boolean isHost = participant.getUserId().equals(RoomManager.get().getHostId());
                        if (localMediaManager.getLocalMediaStream() == null) {
                            if (useCamEncoded) {
                                localMediaManager.createCamEncoded(peerConnectionFactoryCamera, fakeVideoEncoderFactoryCamera, isHost);
                                localMediaManager.cameraPreview(peerConnectionFactoryCamera, isHost);
                                localMediaManager.start();
                            } else {
                                localMediaManager.create(peerConnectionFactoryHwDecPrimary, isHost);
                                localMediaManager.start();
                            }
                        }
                        connection.getPc().addStream(localMediaManager.getLocalMediaStream());
                        if (isHost) {
                            CLLocalMediaManager.EncodeParam param = localMediaManager.getMainParam();
                            connection.getPc().setBitrate(param.minBitrate, param.currentBitrate, param.maxBitrate);
                        } else {
                            CLLocalMediaManager.EncodeParam param = localMediaManager.getSubParam();
                            connection.getPc().setBitrate(param.minBitrate, param.currentBitrate, param.maxBitrate);
                        }
                    }
                    connection.createOffer(sdpConstraints);
                } else {
                    connection.createOffer(sdpConstraints);
                }
            }
        });
    }

    public synchronized void startPreview(ParticipantInfoModel participant, boolean includeLocalMedia) {
        executor.execute(() -> {
            if (participant.getLocalStreamType() == 2) {
                localMediaManager.hdmiPreview(true);
            } else if (participant.getLocalStreamType() == 1) {
                if (localMediaManager.getLocalScreenMediaStream() == null) {
                    localMediaManager.startShareScreen(peerConnectionFactoryHwDecPrimary);
                }
            } else if (includeLocalMedia) {
                localMediaManager.cameraPreview(peerConnectionFactoryCamera, true);
                localMediaManager.start();
            }
        });
    }

    public MediaStream getLocationMediaStream() {
        return localMediaManager.getLocalMediaStream();
    }

    public void setMediaNull() {
        localMediaManager.setMediaNull();
    }

    /**
     * offer or answer params
     *
     * @return
     */
    private MediaConstraints offerOrAnswerConstraint() {
        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        return sdpConstraints;
    }

    /**
     * answer 处理
     *
     * @param remoteAnswer
     * @param participant
     */
    public void processAnswer(SessionDescription remoteAnswer, ParticipantInfoModel participant) {
        CLPeerConnection connection = getPeerConnection(participant);

        if (connection != null) {
            connection.processAnswer(remoteAnswer);
        } else {
            observer.onPeerConnectionError("Connection for id " + participant.getConnectionId() + " cannot be found!");
        }
    }

    /**
     * UI层回调
     *
     * @param viewCallBack
     */
    public void setIViewCallBack(IViewCallback viewCallBack) {
        localMediaManager.setViewCallback(viewCallBack);
    }

    public void addRemoteIceCandidate(IceCandidate remoteIceCandidate, ParticipantInfoModel participant) {

        CLPeerConnection connection = getPeerConnection(participant);
        if (connection != null) {
            connection.addRemoteIceCandidate(remoteIceCandidate);
        } else {
            observer.onPeerConnectionError("Connection for id " + participant.getConnectionId() + " cannot be found!");
        }
    }

    // test strategy 1
//    private int hardwareDecMax = 4; // 小分辨率最多硬解个数
//    private int hardwareDecCount = 0;
//    private int softwareDecCount = 0;
//    private int hostIn = 0;
//    private int shareIn = 0;
    private synchronized boolean useHwDec(ParticipantInfoModel participant) {
        return false;

        // test strategy 1
//        boolean isHost = participant.getUserId().equals(RoomManager.get().getHostId());
//        boolean isShare = participant.getLocalStreamType() == 2;
//        if (isHost || isShare) {
//            hostIn = isHost ? 1 : hostIn;
//            shareIn = isShare ? 1 : shareIn;
//            Log.e("CPeerAPI", "use hardware decoder, hardwareDecMax = " + hardwareDecMax + ", hardwareDecCount = " + hardwareDecCount + ", softwareDecCount = " + softwareDecCount
//                    + ", hostIn = " + hostIn + ", shareIn = " + shareIn);
//            return true;
//        } else if (hardwareDecCount < hardwareDecMax) {
//            hardwareDecCount++;
//            Log.e("CPeerAPI", "use hardware decoder, hardwareDecMax = " + hardwareDecMax + ", hardwareDecCount = " + hardwareDecCount + ", softwareDecCount = " + softwareDecCount
//                    + ", hostIn = " + hostIn + ", shareIn = " + shareIn);
//            return true;
//        }
//        softwareDecCount++;
//        Log.e("CPeerAPI", "use software decoder, hardwareDecMax = " + hardwareDecMax + ", hardwareDecCount = " + hardwareDecCount + ", softwareDecCount = " + softwareDecCount
//                + ", hostIn = " + hostIn + ", shareIn = " + shareIn);
//        return false;

        // test strategy 2
//        int offset = DeviceSettingManager.getInstance().getFromSp().getRoomCapacity();
//        int mod = offset % 2;
//        int curSize = clPeerConnetions.size();
//        if (curSize <= offset || curSize % 2 == mod) {
//            Log.e("CPeerAPI", "use hardware decoder, offset = " + offset + ", curSize = " + curSize);
//            return true;
//        }
//
//        Log.e("CPeerAPI", "use software decoder, offset = " + offset + ", curSize = " + curSize);
//        return false;
    }

    public CLPeerConnection createPeerConnection(ParticipantInfoModel participant, boolean useCamEncoded) {
        Log.e("CPeerAPI", "sshsun log createPC");
        CLPeerConnection clPeerConnection = new CLPeerConnection(participant, executor);
        PeerConnection pc = participant.getLocalStreamType() == 2
                ? this.peerConnectionFactoryHdmiIn.createPeerConnection(initRTCConfig(), clPeerConnection)
                : useCamEncoded
                ? peerConnectionFactoryCamera.createPeerConnection(initRTCConfig(), clPeerConnection)
                : useHwDec(participant)
                ? peerConnectionFactoryHwDecPrimary.createPeerConnection(initRTCConfig(), clPeerConnection)
                : peerConnectionFactorySwDecPrimary.createPeerConnection(initRTCConfig(), clPeerConnection);
        clPeerConnection.setPc(pc);
        participant.setPeerConnection(clPeerConnection);
        clPeerConnetions.put(participant, clPeerConnection);
        Log.i("CLPeerAPI", "createPeerConnection: clPeerConnetions.size = " + clPeerConnetions.size());
        return clPeerConnection;
    }

    public void closeConnection(ParticipantInfoModel participant) {
        if (getPeerConnection(participant) == null) {
            Log.e("CLPeerAPI", "closeConnection: no pc, clPeerConnetions.size = " + clPeerConnetions.size());
            return;
        }
        CLPeerConnection connection = clPeerConnetions.remove(participant);
        connection.close(false);
    }

    public void closeVideo() {
        closeAllConnections();
        localMediaManager.close();
    }

    public void closeALL() {
        localMediaManager.close();
        localMediaManager = null;
        peerConnectionFactoryHwDecPrimary.dispose();
        peerConnectionFactorySwDecPrimary.dispose();
        this.peerConnectionFactoryCamera.dispose();
        this.peerConnectionFactoryHdmiIn.dispose();
        peerConnectionFactoryHwDecPrimary = null;
        peerConnectionFactoryHwDecPrimary = null;
        this.peerConnectionFactoryCamera = null;
        this.peerConnectionFactoryHdmiIn = null;
    }

    public void switchCamera() {
        localMediaManager.switchCamera();
    }

    public void changeCaptureFormat(boolean isGoUp) {
        localMediaManager.changeCaptureFormat(isGoUp);
        CLPeerConnection connection = getPeerConnection(RoomClient.get().getLocalParticipant());
        if (connection == null) {
            return;
        }
        if (isGoUp) {
            CLLocalMediaManager.EncodeParam param = localMediaManager.getMainParam();
            connection.getPc().setBitrate(param.minBitrate, param.currentBitrate, param.maxBitrate);
        } else {
            CLLocalMediaManager.EncodeParam param = localMediaManager.getSubParam();
            connection.getPc().setBitrate(param.minBitrate, param.currentBitrate, param.maxBitrate);
        }
    }

    public void shareScreen(Intent data, SurfaceView localScreenVideoView) {
        localMediaManager.shareScreen(data, localScreenVideoView);
    }

    public void stopLocalPreview() {
        if (localMediaManager != null) {
            localMediaManager.stopLocalPreview();
        }
    }

    public void stopHDMIPreview() {
        localMediaManager.stopHDMIPreview();
    }

    public void stopShareScreen() {
        for (CLPeerConnection c : getPeerConnections()) {
            c.getPc().removeStream(localMediaManager.getLocalScreenMediaStream());
        }
        localMediaManager.stopShareScreen();
    }

    private PeerConnectionFactory createPeerConnectionFactoryExt(FakeVideoEncoderFactory fakeVideoEncoderFactory) {
        String field_trials = FIELD_TRIAL_AUTOMATIC_RESIZE;
        field_trials += FIELD_TRIAL_DISABLED_FRAME_DROP;
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials(field_trials).createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        return PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(fakeVideoEncoderFactory)
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext(), true))
                .createPeerConnectionFactory();
    }

    public void shareHdmi(SurfaceViewRenderer surfaceHDMI) {
        localMediaManager.shareHdmi(surfaceHDMI);
    }

    public void stopShareHdmi() {
        localMediaManager.stopShareHDMI();
        closeConnection(RoomClient.get().getLocalHdmiParticipant());
    }

    public void enableLocalVideo(boolean enabled) {
        localMediaManager.enableVideo(enabled);
    }

    public void enableLocalSpeaker(boolean enabled) {
//        localMediaManager.enableSpeaker(enabled);
//        lock.lock();
        for (CLPeerConnection c : getPeerConnections()) {
            if (c.getMediaStream() != null && c.getMediaStream().audioTracks != null) {
                c.getMediaStream().audioTracks.get(0).setEnabled(enabled);
            }
        }
//        lock.unlock();
    }

    public void enableLocalAudio(boolean enabled) {
        localMediaManager.enableAudio(enabled);
    }

    public void setAudioValue(int value) {
        localMediaManager.setAudioValue(value);
    }

    public void muteLocalVideoStream(boolean muted) {
        localMediaManager.muteVideoStream(muted);
    }

    public void muteLocalAudioStream(boolean muted) {
        localMediaManager.muteAudioStream(muted);
    }


    public CLPeerConnection getPeerConnection(ParticipantInfoModel participant) {
        return clPeerConnetions.get(participant);
    }

    public Collection<CLPeerConnection> getPeerConnections() {
        return clPeerConnetions.values();
    }

    public void closeAllConnections() {
//        lock.lock();
        for (CLPeerConnection connection : clPeerConnetions.values()) {
            connection.close(true);
            connection = null;
        }
//        lock.unlock();
        clPeerConnetions.clear();

    }

    //初始化 RTCPeerConnection 连接管道
    private PeerConnection.RTCConfiguration initRTCConfig() {
//        if (peerConnectionFactory == null) {
//            peerConnectionFactory = createPeerConnectionFactory();
//        }

        if (peerConnectionFactoryHwDecPrimary == null) {
            peerConnectionFactoryHwDecPrimary = createPeerConnectionFactory(true);
        }

        // 管道连接抽象类实现方法
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(ICEServers);
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED; //TCP候选策略控制开关 (禁用)
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE; //捆绑的策略  (最大约束)
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE; //实时传输控制协议多路策略
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;  //加密类型
        rtcConfig.enableCpuOveruseDetection = false;
        return rtcConfig;
    }

    private PeerConnectionFactory createPeerConnectionFactory(boolean hwDecPrimary) {
        String field_trials = FIELD_TRIAL_AUTOMATIC_RESIZE;
        field_trials += FIELD_TRIAL_VP9;
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials(field_trials).createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true);
//        defaultVideoEncoderFactory.setDefaultCodecsH264();
        return PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext(), hwDecPrimary))
                .createPeerConnectionFactory();
    }

    public FakeVideoEncoder getFakeVideoEncoderHdmiIn() {
        if (fakeVideoEncoderFactoryHdmiIn == null) {
            Log.e("CLPeerAPI", "getFakeVideoEncoderHdmiIn: fakeVideoEncoderFactory is null");
            return null;
        }
        return fakeVideoEncoderFactoryHdmiIn.getFakeVideoEncoder();
    }
}
