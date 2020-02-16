package cn.closeli.rtc.model.http;

public class TokenResp {
    /**
     * userId : 1234
     * token : 3BWY94IB7UpYoFOR4n20nty40NIttVRL
     */

    private long userId;
    private String account;
    private String token;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "TokenResp{" +
                "userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
