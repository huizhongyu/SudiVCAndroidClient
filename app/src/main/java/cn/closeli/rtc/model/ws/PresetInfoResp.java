package cn.closeli.rtc.model.ws;

/**
 * 获取会议预设置信息
 */
public class PresetInfoResp {
    private String micStatusInRoom;     //入会时Mic状态设置 on：打开  off：关闭
    private String sharePowerInRoom;    //入会时共享权限设置
    private String videoStatusInRoom;   //入会时摄像头设置
    private String subject;             //会议主题

    public String getMicStatusInRoom() {
        return micStatusInRoom;
    }

    public void setMicStatusInRoom(String micStatusInRoom) {
        this.micStatusInRoom = micStatusInRoom;
    }

    public String getSharePowerInRoom() {
        return sharePowerInRoom;
    }

    public void setSharePowerInRoom(String sharePowerInRoom) {
        this.sharePowerInRoom = sharePowerInRoom;
    }

    public String getVideoStatusInRoom() {
        return videoStatusInRoom;
    }

    public void setVideoStatusInRoom(String videoStatusInRoom) {
        this.videoStatusInRoom = videoStatusInRoom;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
