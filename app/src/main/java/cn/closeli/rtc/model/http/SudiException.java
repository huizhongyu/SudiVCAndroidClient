package cn.closeli.rtc.model.http;

import java.io.IOException;

public class SudiException extends IOException {
    private int code;
    public SudiException(int code,String msg) {
        super(msg);
        this.code = code;
    }

    @Override
    public String toString() {
        return String.format("%1$d: %2$s",code,getMessage());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
