package cn.closeli.rtc.model.info;

import java.util.List;

import cn.closeli.rtc.model.rtc.UserDeviceModel;

public class GroupInfoModel {
    private List<UserDeviceModel> groupInfo;

    public List<UserDeviceModel> getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(List<UserDeviceModel> groupInfo) {
        this.groupInfo = groupInfo;
    }
}
