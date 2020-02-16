package cn.closeli.rtc.model.rtc;

import cn.closeli.rtc.model.Metadata;

public class ParticipantJoinedModel {
    private String id;
    private Metadata metadata;
    private boolean isReconnected;      //断线重连标志

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public boolean isReconnected() {
        return isReconnected;
    }

    public void setReconnected(boolean reconnected) {
        isReconnected = reconnected;
    }
}
