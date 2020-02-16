package cn.closeli.rtc.model;

public class SlingModel {
    public String sessionId = "";
    public String sourceId = "";
    public String targetId = "";
    public String[] targetIds;
    public String status = "";
    public String action = "";
    public String roomId = "";
    public String raiseHandNum;
    public String operation = "";

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRaiseHandNum() {
        return raiseHandNum;
    }

    public void setRaiseHandNum(String raiseHandNum) {
        this.raiseHandNum = raiseHandNum;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
