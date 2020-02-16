package cn.closeli.rtc.model.rtc;

public class IceCandidateModel {
    private String senderConnectionId;
    private String endpointName;
    private int sdpMLineIndex;
    private String sdpMid;
    private String candidate;

    public String getSenderConnectionId() {
        return senderConnectionId;
    }

    public void setSenderConnectionId(String senderConnectionId) {
        this.senderConnectionId = senderConnectionId;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public int getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(int sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }
}
