package cn.closeli.rtc.room;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import cn.closeli.rtc.model.info.ParticipantInfoModel;

/**
 * 介于仅仅在收到webSocket 需要传递到webRTC 做处理
 */
public interface RoomRTCEvent {
    void onShareJoinRoomSuccess(ParticipantInfoModel participant);

    void onParticipantPublished(ParticipantInfoModel participant);

    void onMixPublished(ParticipantInfoModel participant,boolean isHasShare);

    void onParticipantUnPublished(ParticipantInfoModel participant);

    void onRemoteSdpAnswerReceived(ParticipantInfoModel participant, SessionDescription sdp);

    void onRemoteIceCandidateReceived(ParticipantInfoModel participant, IceCandidate iceCandidate);

    void onParticipant(ParticipantInfoModel participant);
}
