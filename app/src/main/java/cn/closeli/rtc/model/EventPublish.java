package cn.closeli.rtc.model;

import org.webrtc.MediaStream;

import cn.closeli.rtc.model.info.ParticipantInfoModel;

public class EventPublish {
    private ParticipantInfoModel participantInfoModel;
    private MediaStream mediaStream;

    public EventPublish() {
    }

    public ParticipantInfoModel getParticipantInfoModel() {
        return participantInfoModel;
    }

    public void setParticipantInfoModel(ParticipantInfoModel participantInfoModel) {
        this.participantInfoModel = participantInfoModel;
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }
}
