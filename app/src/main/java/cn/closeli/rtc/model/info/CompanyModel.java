package cn.closeli.rtc.model.info;

import java.util.List;

import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.rtc.UserDeviceModel;

//企业子部门 及 设备列表
public class CompanyModel {
    private long orgId;
    private String organizationName;
    private boolean isExpand;               //是否展开
    private boolean isChoose = false;               //是否全选
    private List<OrgList> organizationList;

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public List<OrgList> getOrganizationList() {
        return organizationList;
    }

    public void setOrganizationList(List<OrgList> organizationList) {
        this.organizationList = organizationList;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }
}
