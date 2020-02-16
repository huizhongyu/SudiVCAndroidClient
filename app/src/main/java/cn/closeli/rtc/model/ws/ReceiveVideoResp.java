package cn.closeli.rtc.model.ws;

public class ReceiveVideoResp {

    private String sdpAnswer;
    private String sessionId;

    public ReceiveVideoResp() {
    }

    public String getSdpAnswer() {
        return sdpAnswer;
    }

    public void setSdpAnswer(String sdpAnswer) {
        this.sdpAnswer = sdpAnswer;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
