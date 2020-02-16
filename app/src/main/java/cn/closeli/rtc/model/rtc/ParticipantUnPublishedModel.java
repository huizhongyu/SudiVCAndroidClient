package cn.closeli.rtc.model.rtc;

public class ParticipantUnPublishedModel {
    private String connectionId;
    private String reason;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
