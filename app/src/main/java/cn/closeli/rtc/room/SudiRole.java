package cn.closeli.rtc.room;

public enum SudiRole {
    /**
     * 会议中角色，包含以下内容
     * subscriber：订阅者
     * publisher：发布者 + 订阅者 // 加入会议
     * moderator：发布者 + 订阅者 + 主持人 //发起会议 -> 加入会议
     */
    subscriber("SUBSCRIBER"),
    publisher("PUBLISHER"),
    moderator("MODERATOR");

    private String role;

    SudiRole(String role) {
        this.role = role;
    }

    public String value() {
        return role;
    }


}
