package cn.closeli.rtc.model;
//邀请入会
public class InviteModel extends SlingModel {
    private String username;
    private String deviceName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
