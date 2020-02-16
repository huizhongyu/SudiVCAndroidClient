package cn.closeli.rtc.model;

public class Answer {
    public static final String RESPONSE_ACCEPT = "accept";
    public static final String RESPONSE_REJECT = "reject";
    public static final String RESPONSE_OFFLINE = "offline";

    private String groupId;
    private String from;
    private String to;
    private String channelId;
    private String response;

    public Answer(String groupId, String from, String to, String channelId, String response) {
        this.groupId = groupId;
        this.from = from;
        this.to = to;
        this.channelId = channelId;
        this.response = response;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
