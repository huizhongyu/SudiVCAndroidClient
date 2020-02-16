package cn.closeli.rtc.model.ws;

import java.util.List;

import cn.closeli.rtc.model.PartLinkedListBean;
/**
 * 窗口布局变更通知，点名发言交换窗口位置后发送该通知
 */
public class ConferenceLayoutChangedResp {
    private int mode;
    private List<PartLinkedListBean> partLinkedList;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public List<PartLinkedListBean> getPartLinkedList() {
        return partLinkedList;
    }

    public void setPartLinkedList(List<PartLinkedListBean> partLinkedList) {
        this.partLinkedList = partLinkedList;
    }
}
