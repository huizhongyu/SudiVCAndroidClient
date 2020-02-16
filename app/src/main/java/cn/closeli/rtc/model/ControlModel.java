package cn.closeli.rtc.model;

public class ControlModel {
    private String serialNumber;                    //设备序列号
    private int operateCode;                        //上下移动1：上移2：下移 左右移动 3：左移     4：右移  镜头变倍 5：放大   6：缩小
    private long maxDuration;                       //持续时长


    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getOperateCode() {
        return operateCode;
    }

    public void setOperateCode(int operateCode) {
        this.operateCode = operateCode;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }
}
