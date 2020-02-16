package cn.closeli.rtc.model.http;

import java.util.List;

public class AddrResp {

    private List<String> signalAddrList;

    public List<String> getSignalAddrList() {
        return signalAddrList;
    }

    public void setSignalAddrList(List<String> signalAddrList) {
        this.signalAddrList = signalAddrList;
    }

    @Override
    public String toString() {
        return "AddrResp{" +
                "signalAddrList=" + signalAddrList +
                '}';
    }
}
