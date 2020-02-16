package cn.closeli.rtc.model.rtc;

public class UserDeviceModel {
    private String deviceName;
    private String status;
    private String username;
    private String userId;
    private String appShowName;
    private String appShowDesc;
    private boolean isChecked;      //是否选中

    private String serialNumber;        //序列号
    private String account;             //设备没有账号登录时，不返回
    private String deviceStatus;        //设备状态
    private boolean isGroupData;        //是否是群组数据

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppShowName() {
        return appShowName;
    }

    public void setAppShowName(String appShowName) {
        this.appShowName = appShowName;
    }

    public String getAppShowDesc() {
        return appShowDesc;
    }

    public void setAppShowDesc(String appShowDesc) {
        this.appShowDesc = appShowDesc;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public boolean isGroupData() {
        return isGroupData;
    }

    public void setGroupData(boolean groupData) {
        isGroupData = groupData;
    }
}
