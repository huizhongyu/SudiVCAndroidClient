package cn.closeli.rtc.model.info;

import java.util.List;

import cn.closeli.rtc.model.rtc.UserDeviceModel;

/**
 * 部门设备model
 */
public class PartDeviceModel {
    private List<UserDeviceModel> deviceList;

    public List<UserDeviceModel> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<UserDeviceModel> deviceList) {
        this.deviceList = deviceList;
    }
}
