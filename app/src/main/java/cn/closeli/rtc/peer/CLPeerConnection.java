package cn.closeli.rtc.peer;

import android.support.annotation.Nullable;
import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RTCStats;
import org.webrtc.RTCStatsCollectorCallback;
import org.webrtc.RTCStatsReport;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.closeli.rtc.App;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.LooperExecutor;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.UIUtils;

/**
 * 管道通讯，实现管道接口和SDP，主要通过WebRTC的回调对WebSocket进行通讯
 */
public class CLPeerConnection implements PeerConnection.Observer, SdpObserver {
    private String logTag = this.getClass().getCanonicalName();

    private ParticipantInfoModel participant;
    private PeerConnection pc;
    private MediaStream mediaStream;
    private LooperExecutor executor;
    private SessionDescription localSdp;
    private boolean isInitiator;
    private CopyOnWriteArrayList<RTCObserver> observers;
    private final Timer timer = new Timer();
    private final TimerTask tickTask = new TimerTask() {
        public void run() {
            CLPeerConnection.this.tick();
        }
    };
    private final long statsPeriodSec = 10;
    private long statsCount = 0;
    private final long callbackPeriodSec = 180;
    private final long cbPeriodStatsCount = callbackPeriodSec / statsPeriodSec;
    private double cbPeriodTotalLostRate = 0.0;
    private long lastVideoFrameSend = 0;
    private long lastVideoFrameRecv = 0;
    private long lastVideoFrameDec = 0;
    private long lastVideoByte = 0;
    private long lastAudioByte = 0;
    private boolean isVideoTimeout = false;
    private boolean isAudioTimeout = false;

    public void tick() {
        if (this.pc == null) {
            return;
        }
        this.pc.getStats(new RTCStatsCollectorCallback() {
            @Override
            public void onStatsDelivered(RTCStatsReport rtcStatsReport) {
                boolean isLocal = participant.getUserId().equals(SPEditor.instance().getUserId());
                String pcId = participant.getConnectionId() + "-" + participant.getUserId() + " - " + participant.getAccount() + " - " + participant.getDeviceName();
                Log.d(logTag, "onStatsDelivered: >>>>>>>>>> stats： " + pcId + " >>>>>>>>>>");
                Map<String, RTCStats> stats = rtcStatsReport.getStatsMap();
                StringBuilder trackVideoOutBuilder = new StringBuilder();
                StringBuilder outboundRtpVideoBuilder = new StringBuilder();
                StringBuilder outboundRtpAudioBuilder = new StringBuilder();
                StringBuilder trackVideoInBuilder = new StringBuilder();
                StringBuilder inboundRtpVideoBuilder = new StringBuilder();
                StringBuilder inboundRtpAudioBuilder = new StringBuilder();
                for (Iterator var3 = stats.values().iterator(); var3.hasNext(); ) {
                    RTCStats stat = (RTCStats) var3.next();
                    String type = stat.getType();
                    if (type.equals("remote-candidate") || type.equals("local-candidate") || type.equals("media-source") || type.equals("track")
                            || type.equals("inbound-rtp") || type.equals("outbound-rtp") || type.equals("remote-inbound-rtp")) {
                        Log.d(logTag, "onStatsDelivered: " + stat.toString());
                    }

                    if (isLocal) {
                        if (isLocal && type.equals("track") && get(stat, "kind").equals("video") && get(stat, "remoteSource").equals("false")) {
                            trackVideoOutBuilder.append("videoOut: " + "res: " + get(stat, "frameWidth") + "x" + get(stat, "frameHeight"));
                            trackVideoOutBuilder.append(", ");
//                            trackVideoOutBuilder.append("framesSent: " + get(stat, "framesSent"));
//                            trackVideoOutBuilder.append(", ");

                            long framesSent = Long.parseLong(get(stat, "framesSent"));
                            double fps = (double) (framesSent - CLPeerConnection.this.lastVideoFrameSend) / statsPeriodSec;
                            trackVideoOutBuilder.append("fps: " + String.format("%.1f", fps));
                            CLPeerConnection.this.lastVideoFrameSend = framesSent;
                        }
                        if (type.equals("outbound-rtp") && get(stat, "kind").equals("video")) {
                            outboundRtpVideoBuilder.append("videoOut: ");
//                            outboundRtpVideoBuilder.append("bytes: " + get(stat, "bytesSent"));
//                            outboundRtpVideoBuilder.append(", ");

                            long bytesSent = Long.parseLong(get(stat, "bytesSent"));
                            CLPeerConnection.this.isVideoTimeout = bytesSent <= CLPeerConnection.this.lastVideoByte;
                            long bitrateKbs = (bytesSent - CLPeerConnection.this.lastVideoByte) * 8 / statsPeriodSec / 1024;
                            outboundRtpVideoBuilder.append("v-bitrate: " + bitrateKbs + " kbs");
                            CLPeerConnection.this.lastVideoByte = bytesSent;
                        }
                        if (type.equals("outbound-rtp") && get(stat, "kind").equals("audio")) {
                            outboundRtpAudioBuilder.append("audioOut: ");
//                            outboundRtpAudioBuilder.append("bytes: " + get(stat, "bytesSent"));
//                            outboundRtpAudioBuilder.append(", ");

                            long bytesSent = Long.parseLong(get(stat, "bytesSent"));
                            CLPeerConnection.this.isAudioTimeout = bytesSent <= CLPeerConnection.this.lastAudioByte;
                            long bitrateKbs = (bytesSent - CLPeerConnection.this.lastAudioByte) * 8 / statsPeriodSec / 1024;
                            outboundRtpAudioBuilder.append("a-bitrate: " + bitrateKbs + " kbs");
                            CLPeerConnection.this.lastAudioByte = bytesSent;
                        }
                    } else {
                        if (type.equals("track") && get(stat, "kind").equals("video") && get(stat, "remoteSource").equals("true") && !get(stat, "frameWidth").equals("")) {
                            trackVideoInBuilder.append("videoIn: " + "res: " + get(stat, "frameWidth") + "x" + get(stat, "frameHeight"));
                            trackVideoInBuilder.append(", ");
                            trackVideoInBuilder.append("frame(recv/dec): " + get(stat, "framesReceived") + "/" + get(stat, "framesDecoded"));
                            trackVideoInBuilder.append(", ");

                            long framesReceived = Long.parseLong(get(stat, "framesReceived"));
                            long framesDecoded = Long.parseLong(get(stat, "framesDecoded"));
                            double fpsRecv = (double) (framesReceived - CLPeerConnection.this.lastVideoFrameRecv) / statsPeriodSec;
                            double fpsDec = (double) (framesDecoded - CLPeerConnection.this.lastVideoFrameDec) / statsPeriodSec;
                            trackVideoInBuilder.append("fps(recv/dec): " + String.format("%.1f", fpsRecv) + "/" + String.format("%.1f", fpsDec));
                            CLPeerConnection.this.lastVideoFrameRecv = framesReceived;
                            CLPeerConnection.this.lastVideoFrameDec = framesDecoded;
                        }
                        if (type.equals("inbound-rtp") && get(stat, "kind").equals("video")) {
                            inboundRtpVideoBuilder.append("videoIn: ");
//                            inboundRtpVideoBuilder.append("bytes: " + get(stat, "bytesReceived"));
//                            inboundRtpVideoBuilder.append(", ");

                            long bytesReceived = Long.parseLong(get(stat, "bytesReceived"));
                            CLPeerConnection.this.isVideoTimeout = bytesReceived <= CLPeerConnection.this.lastVideoByte;
                            long bitrateKbs = (bytesReceived - CLPeerConnection.this.lastVideoByte) * 8 / statsPeriodSec / 1024;
                            inboundRtpVideoBuilder.append("v-bitrate: " + bitrateKbs + " kbs");
                            inboundRtpVideoBuilder.append(", ");
                            CLPeerConnection.this.lastVideoByte = bytesReceived;

//                            inboundRtpVideoBuilder.append("rtp(recv/lost): " + get(stat, "packetsReceived") + "/" + get(stat, "packetsLost"));
//                            inboundRtpVideoBuilder.append(", ");
                            long packetsReceived = Long.parseLong(get(stat, "packetsReceived"));
                            long packetsLost = Long.parseLong(get(stat, "packetsLost"));
                            double rate = (packetsLost + packetsReceived) == 0 ? 0.0 : (double) packetsLost / (double) (packetsLost + packetsReceived);
                            inboundRtpVideoBuilder.append("rtpLostRate: " + String.format("%.3f%%", rate * 100));
                            cbPeriodTotalLostRate += rate;
                            if (++statsCount % cbPeriodStatsCount == 0) {
                                double cbPeriodAvgRate = cbPeriodTotalLostRate / cbPeriodStatsCount;
                                cbPeriodTotalLostRate = 0.0;
                                App.post(() -> {
                                    if (cbPeriodAvgRate > 0.3) {
                                        UIUtils.toastMessage("网络状况极差");
                                    } else if (cbPeriodAvgRate > 0.2) {
                                        UIUtils.toastMessage("网络状况差");
                                    }
                                });
                            }
                        }
                        if (type.equals("inbound-rtp") && get(stat, "kind").equals("audio")) {
                            inboundRtpAudioBuilder.append("audioIn: ");
//                            inboundRtpVideoBuilder.append("bytes: " + get(stat, "bytesReceived"));
//                            inboundRtpVideoBuilder.append(", ");

                            long bytesReceived = Long.parseLong(get(stat, "bytesReceived"));
                            CLPeerConnection.this.isAudioTimeout = bytesReceived <= CLPeerConnection.this.lastAudioByte;
                            long bitrateKbs = (bytesReceived - CLPeerConnection.this.lastAudioByte) * 8 / statsPeriodSec / 1024;
                            inboundRtpAudioBuilder.append("a-bitrate: " + bitrateKbs + " kbs");
                            inboundRtpAudioBuilder.append(", ");
                            CLPeerConnection.this.lastAudioByte = bytesReceived;

//                            inboundRtpVideoBuilder.append("rtp(recv/lost): " + get(stat, "packetsReceived") + "/" + get(stat, "packetsLost"));
//                            inboundRtpVideoBuilder.append(", ");
                            long packetsReceived = Long.parseLong(get(stat, "packetsReceived"));
                            long packetsLost = Long.parseLong(get(stat, "packetsLost"));
                            double rate = (packetsLost + packetsReceived) == 0 ? 0.0 : (double) packetsLost / (double) (packetsLost + packetsReceived);
                            inboundRtpAudioBuilder.append("rtpLostRate: " + String.format("%.3f%%", rate * 100));
                        }
                    }
                }
                if (isLocal) {
                    Log.i(logTag, "onStatsDelivered: pcId: " + pcId + " | " + trackVideoOutBuilder.toString() + " | " + outboundRtpVideoBuilder.toString() + " | " + outboundRtpAudioBuilder.toString());
                } else {
                    Log.i(logTag, "onStatsDelivered: pcId: " + pcId + " | " + trackVideoInBuilder.toString() + " | " + inboundRtpVideoBuilder.toString() + " | " + inboundRtpAudioBuilder.toString());
                }
                Log.d(logTag, "onStatsDelivered: <<<<<<<<<< stats： " + pcId + " <<<<<<<<<<");
            }
        });
    }

    private String get(RTCStats stat, String name) {
        Map<String, Object> mems = stat.getMembers();
        if (mems.containsKey(name)) {
            return String.valueOf(mems.get(name));
        }
        return "";
    }

    public CLPeerConnection(ParticipantInfoModel participant, LooperExecutor executor) {
        this.participant = participant;
        this.executor = executor;
        this.isInitiator = false;
        this.observers = new CopyOnWriteArrayList<>();
    }

    @Nullable
    public ParticipantInfoModel getParticipant() {
        return participant;
    }

    public PeerConnection getPc() {
        return pc;
    }

    public void setPc(PeerConnection pc) {
        this.pc = pc;
        this.timer.schedule(this.tickTask, 0L, statsPeriodSec * 1000);
    }

    public MediaStream getMediaStream() {
        return this.mediaStream;
    }

    public void addObserver(RTCObserver observer) {
        observers.add(observer);
    }

    public void createOffer(MediaConstraints sdpMediaConstraints) {
        if (pc != null) {
            isInitiator = true;
            pc.createOffer(this, sdpMediaConstraints);
        }
    }

    public void createAnswer(final MediaConstraints sdpMediaConstraints) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc != null) {
                    isInitiator = false;
                    pc.createAnswer(CLPeerConnection.this, sdpMediaConstraints);
                }
            }
        });
    }

    public void processAnswer(SessionDescription remoteAnswer) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc != null) {
                    pc.setRemoteDescription(CLPeerConnection.this, remoteAnswer);
                }
            }
        });
    }

    public void addRemoteIceCandidate(final IceCandidate candidate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc != null) {
                    pc.addIceCandidate(candidate);
                }
            }
        });
    }

    public void close(boolean byCloseAll) {
        if (pc != null) {
            try {
                if(byCloseAll) {
                    tickTask.cancel();
                }
                pc.dispose();
            } catch (Exception e) {
                Log.w(logTag, "close() pc.dispose error!");
                e.printStackTrace();
            }
            pc = null;
        }
    }

    public boolean isStreamTimeout() {
        return this.isVideoTimeout && this.isAudioTimeout;
    }

    /**
     * PeerConnection.Observer Implement
     */
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(logTag, "onSignalingChange() called with: signalingState = [" + signalingState + "]");
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(logTag, "onIceConnectionChange() called with: iceConnectionState = [" + iceConnectionState + "]");
        for (RTCObserver o : observers) {
            o.onIceStatusChanged(iceConnectionState, this);
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(logTag, "onIceConnectionReceivingChange() called with: b = [" + b + "]");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(logTag, "onIceGatheringChange() called with: iceGatheringState = [" + iceGatheringState + "]");
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(logTag, "onIceCandidate() called with: iceCandidate = [" + iceCandidate + "]");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (RTCObserver observer : observers) {
                    observer.onIceCandidate(iceCandidate, CLPeerConnection.this);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(logTag, "onIceCandidatesRemoved() called with: iceCandidates = [" + Arrays.toString(iceCandidates) + "]");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(logTag, "onAddStream() called with: mediaStream = [" + mediaStream + "]");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc == null) {
                    return;
                }
                if (mediaStream.audioTracks.size() > 1 || mediaStream.videoTracks.size() > 1) {
                    for (RTCObserver observer : observers) {
                        observer.onPeerConnectionError("Weird-looking stream: " + mediaStream);
                    }
                    return;
                }
                for (RTCObserver observer : observers) {
                    CLPeerConnection.this.mediaStream = mediaStream;
                    observer.onRemoteStreamAdded(mediaStream, CLPeerConnection.this);
                }
            }
        });
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(logTag, "onRemoveStream() called with: mediaStream = [" + mediaStream + "]");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc == null) {
                    return;
                }
                if (mediaStream.audioTracks.size() > 1 || mediaStream.videoTracks.size() > 1) {
                    for (RTCObserver observer : observers) {
                        observer.onPeerConnectionError("Weird-looking stream: " + mediaStream);
                    }
                    return;
                }
                for (RTCObserver observer : observers) {
                    observer.onRemoteStreamRemoved(mediaStream, CLPeerConnection.this);
                }
            }
        });
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(logTag, "onRenegotiationNeeded() called");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(logTag, "onAddTrack() called with: mediaStreams = [" + Arrays.toString(mediaStreams) + "]");
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {

    }


    /**
     *  Sdpbserver Implement
     *
     */
    /**
     * sdp 创建成功
     *
     * @param sessionDescription
     */
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(logTag, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription + "]");

        localSdp = sessionDescription;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc != null) {
                    pc.setLocalDescription(CLPeerConnection.this, sessionDescription);
                }
            }
        });
    }

    @Override
    public void onSetSuccess() {
        Log.d(logTag, "onSetSuccess() called");
        Log.d("addNewView", "onSetSuccess()" + pc.signalingState());
        L.d("do this ---onSetSuccess>>> " + localSdp.description.equals(pc.getLocalDescription().description));
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc == null) {
                    return;
                }
                /**
                 * 云端做answer 处理，客户端都走offer
                 */
                if (isInitiator) {
                    if (pc.getRemoteDescription() == null) {
                        for (RTCObserver observer : observers) {
                            observer.onLocalSdpOfferGenerated(localSdp, CLPeerConnection.this);
                        }
                    }
                } else {
                    if (pc.getLocalDescription() != null) {
                        for (RTCObserver observer : observers) {
                            observer.onLocalSdpAnswerGenerated(localSdp, CLPeerConnection.this);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(logTag, "onCreateFailure() called with: s = [" + s + "]");
        for (RTCObserver observer : observers) {
            observer.onPeerConnectionError(s);
        }
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(logTag, "onSetFailure() called with: s = [" + s + "]");
        for (RTCObserver observer : observers) {
            observer.onPeerConnectionError(s);
        }
    }
}
