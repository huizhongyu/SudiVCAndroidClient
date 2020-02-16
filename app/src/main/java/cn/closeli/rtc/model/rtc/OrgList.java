package cn.closeli.rtc.model.rtc;

import java.util.ArrayList;

public class OrgList {
    private String organizationName;
    private long orgId;
    private long parentId;
    private boolean isExpand;                   //是否展开
    private boolean isLastData = false;                 //是否是最底层数据
    private boolean isAllChoose = false;                //是否全选
    private ArrayList<OrgList> organizationList;
    private ArrayList<UserDeviceModel> deviceList;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public ArrayList<OrgList> getOrganizationList() {
        return organizationList;
    }

    public void setOrganizationList(ArrayList<OrgList> organizationList) {
        this.organizationList = organizationList;
    }

    public ArrayList<UserDeviceModel> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(ArrayList<UserDeviceModel> deviceList) {
        this.deviceList = deviceList;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public boolean isLastData() {
        return isLastData;
    }

    public void setLastData(boolean lastData) {
        isLastData = lastData;
    }

    public boolean isAllChoose() {
        return isAllChoose;
    }

    public void setAllChoose(boolean allChoose) {
        isAllChoose = allChoose;
    }
}
