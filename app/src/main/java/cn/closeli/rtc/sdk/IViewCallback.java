package cn.closeli.rtc.sdk;

import org.webrtc.MediaStream;

import cn.closeli.rtc.model.info.ParticipantInfoModel;

/**
 * webRTC 回调到页面层处理，
 */

public interface IViewCallback {

    void onSetLocalStream(MediaStream stream);

    void onSetLocalScreenStream(MediaStream stream);

    void onSetLocalHDMIStream(MediaStream stream);

    void onAddRemoteStream(MediaStream stream, ParticipantInfoModel participant);

    void onCloseRemoteStream(MediaStream stream, ParticipantInfoModel participant);

    void onMixStream( ParticipantInfoModel participant,boolean isHasShare);

    void publishParticipantInfo(ParticipantInfoModel participant);

}
