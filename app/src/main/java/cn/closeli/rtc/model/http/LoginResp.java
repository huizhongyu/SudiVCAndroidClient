package cn.closeli.rtc.model.http;

public class LoginResp {
    /**
     * userId : id
     * account : 15168256662@suditech.cn
     * nickName : nickName
     */

    private long userId;
    private String account;
    private String nickName;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return "LoginResp{" +
                "userId='" + userId + '\'' +
                ", account='" + account + '\'' +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}
