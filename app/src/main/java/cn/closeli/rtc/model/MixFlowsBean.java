package cn.closeli.rtc.model;

public class MixFlowsBean {
    private String streamId;
    private String streamMode;  //码流模式，枚举值如下 MIX_MAJOR_AND_SHARING：主辅混叠    MIX_MAJOR：主流混叠    SFU_SHARING：辅码流

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamMode() {
        return streamMode;
    }

    public void setStreamMode(String streamMode) {
        this.streamMode = streamMode;
    }
}
