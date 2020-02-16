package cn.closeli.rtc.model.http;

public class SudiResponse<T> {

    /**
     * result : {}
     * errorCode : 0
     * errorMsg : sucess
     * traceId : 7f000001-169b8e93356-059ec345
     */

    private T result;
    private int errorCode;
    private String errorMsg;
    private String traceId;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public boolean isSuccess() {
        return errorCode == 0;
    }


}
