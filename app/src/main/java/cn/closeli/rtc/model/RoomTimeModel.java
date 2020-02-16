package cn.closeli.rtc.model;

//会议延长 model
public class RoomTimeModel {
    private String roomId;
    private int remainTime;         //剩余时长， 单位：分钟

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getRemainTime() {
        return remainTime;
    }

    public void setRemainTime(int remainTime) {
        this.remainTime = remainTime;
    }
}
