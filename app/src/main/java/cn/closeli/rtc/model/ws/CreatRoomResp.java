package cn.closeli.rtc.model.ws;

public class CreatRoomResp {
    private String roomId;
    private String sessionId;

    public CreatRoomResp() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
