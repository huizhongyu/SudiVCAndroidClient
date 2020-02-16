package cn.closeli.rtc.peer;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

/**
 * 在收到webRTC 回调 websocket 或者 UI 层 处理
 */
public interface RTCObserver {
    //SDP创建完成之后向socket 发送sdp信息
    void onLocalSdpOfferGenerated(SessionDescription localSdpOffer, CLPeerConnection connection);

    void onLocalSdpAnswerGenerated(SessionDescription localSdpAnswer, CLPeerConnection connection);
    //
    void onIceCandidate(IceCandidate localIceCandidate, CLPeerConnection connection);

    void onIceStatusChanged(PeerConnection.IceConnectionState state, CLPeerConnection connection);

    void onRemoteStreamAdded(MediaStream stream, CLPeerConnection connection);

    void onRemoteStreamRemoved(MediaStream stream, CLPeerConnection connection);

    void onPeerConnectionError(String error);

}
