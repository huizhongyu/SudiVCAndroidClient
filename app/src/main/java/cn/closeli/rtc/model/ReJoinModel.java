package cn.closeli.rtc.model;

/**
 * 断线重连 model
 */
public class ReJoinModel {
    private String roomId;
    private String subject;
    private String password;
    private long remainTime;            //会议剩余时间，单位为秒
    private String role;                //SUBSCRIBER：订阅者  PUBLISHER：发布者 + 订阅者   MODERATOR：发布者 + 订阅者 + 主持人
    private boolean audioActive;        //Mic状态 Mic开启：true, Mic关闭：false
    private boolean videoActive;        //视频状态 视频开启：true, 视频关闭：false
    private String shareStatus;         //共享状态 共享开启：on, 共享关闭：off
    private String speakerStatus;       //	扬声器状态 扬声器开启：on 扬声器关闭：off
    private String handStatus;          //举手状态，up:举手，speaker:发言, endSpeaker:结束发言, down:未举手

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getRemainTime() {
        return remainTime;
    }

    public void setRemainTime(long remainTime) {
        this.remainTime = remainTime;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAudioActive() {
        return audioActive;
    }

    public void setAudioActive(boolean audioActive) {
        this.audioActive = audioActive;
    }

    public boolean isVideoActive() {
        return videoActive;
    }

    public void setVideoActive(boolean videoActive) {
        this.videoActive = videoActive;
    }

    public String getShareStatus() {
        return shareStatus;
    }

    public void setShareStatus(String shareStatus) {
        this.shareStatus = shareStatus;
    }

    public String getSpeakerStatus() {
        return speakerStatus;
    }

    public void setSpeakerStatus(String speakerStatus) {
        this.speakerStatus = speakerStatus;
    }

    public String getHandStatus() {
        return handStatus;
    }

    public void setHandStatus(String handStatus) {
        this.handStatus = handStatus;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
