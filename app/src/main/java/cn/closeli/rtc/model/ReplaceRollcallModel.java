package cn.closeli.rtc.model;

public class ReplaceRollcallModel extends SlingModel {
    private String endTargetId;
    private String startTargetId;

    public String getEndTargetId() {
        return endTargetId;
    }

    public void setEndTargetId(String endTargetId) {
        this.endTargetId = endTargetId;
    }

    public String getStartTargetId() {
        return startTargetId;
    }

    public void setStartTargetId(String startTargetId) {
        this.startTargetId = startTargetId;
    }
}
