package cn.closeli.rtc.model.ws;

public class AudioStstusResp {
    @Override
    public String toString() {
        return "AudioStstusResp{" +
                "roomId='" + roomId + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", targetId='" + targetIds + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    /**
     * roomId : abcdefgh
     * sourceId : 1234
     * targetId : 1234
     * status : off
     */

    private String roomId;
    private String sourceId;
    private String[] targetIds;
    private String status;

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

    public String[] getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(String[] targetIds) {
        this.targetIds = targetIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
