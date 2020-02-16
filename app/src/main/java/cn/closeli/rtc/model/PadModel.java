package cn.closeli.rtc.model;

public class PadModel {
    private String padMethod;
    private String status;
    private String sessionId;

    public String getPadMethod() {
        return padMethod;
    }

    public void setPadMethod(String padMethod) {
        this.padMethod = padMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
