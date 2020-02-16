package cn.closeli.rtc.model;

import java.util.List;

/**
 * 参会者有序列表
 */
public class LayoutInfoBean {
    private int mode;                   //mode枚举值如下 0：默认布局； 4：四分屏； 6：六分屏； 9：九分屏； 12：十二分屏。
    private List<PartLinkedListBean> linkedCoordinates;                  //参会者有序列表

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public List<PartLinkedListBean> getLinkedCoordinates() {
        return linkedCoordinates;
    }

    public void setLinkedCoordinates(List<PartLinkedListBean> linkedCoordinates) {
        this.linkedCoordinates = linkedCoordinates;
    }

}
