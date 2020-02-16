package cn.closeli.rtc.model.ws;

public class WsError<T> {

    /**
     * code : 40007
     * message : Invalid parameter format
     * sessionId : 6if78ift1m5s1quil4t0dktjka
     * data : {}
     */

    private int code;
    private String message;
    private String sessionId;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
