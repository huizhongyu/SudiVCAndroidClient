package cn.closeli.rtc.model.rtc;

public class ParticipantLeft {
    private String connectionId;
    private String reason;
    private int raiseHandNum;

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

    public int getRaiseHandNum() {
        return raiseHandNum;
    }

    public void setRaiseHandNum(int raiseHandNum) {
        this.raiseHandNum = raiseHandNum;
    }
}
