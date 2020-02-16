package cn.closeli.rtc.model;

public class Call {
    private String groupId;
    private String from;
    private String to;
    private String channelId;
    private String token;

    public Call(String groupId, String from, String to, String channelId, String token) {
        this.groupId = groupId;
        this.from = from;
        this.to = to;
        this.channelId = channelId;
        this.token = token;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

