package cn.closeli.rtc.model.ws;

public class HandsStatusResp {
    @Override
    public String toString() {
        return "HandsStatusResp{" +
                "roomId='" + roomId + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", targetId='" + targetId + '\'' +
                '}';
    }

    /**
     * roomId : abcdefgh
     * sourceId : 1234
     * targetId : 1234
     */

    private String roomId;
    private String sourceId;
    private String targetId;
    private String raiseHandNum;
    private String username;
    private String appShowName;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getRaiseHandNum() {
        return raiseHandNum;
    }

    public void setRaiseHandNum(String raiseHandNum) {
        this.raiseHandNum = raiseHandNum;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAppShowName() {
        return appShowName;
    }

    public void setAppShowName(String appShowName) {
        this.appShowName = appShowName;
    }
}
