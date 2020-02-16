package cn.closeli.rtc.model;

/**
 * 单点登录model
 */
public class SingleLoginModel {
    private String token;                   //重置后的token
    private String deviceName;              //设备名称
    private String applicantSessionId;      //申请者的websocket session id
    private boolean accept;                 //同意或拒绝
    private String sessionId;               //websocket session id
    private boolean loginAllowable;         //是否允许登录

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getApplicantSessionId() {
        return applicantSessionId;
    }

    public void setApplicantSessionId(String applicantSessionId) {
        this.applicantSessionId = applicantSessionId;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isLoginAllowable() {
        return loginAllowable;
    }

    public void setLoginAllowable(boolean loginAllowable) {
        this.loginAllowable = loginAllowable;
    }
}
