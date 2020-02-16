package cn.closeli.rtc.model.info;

//群组
public class GroupModel {
    private long groupId;                       //群组id
    private String groupName;                   //群组名称
    private boolean isAllChooseGroup = false;       //群组是否全选
    private boolean isExpandGroup = false;          //群组是否展开

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isAllChooseGroup() {
        return isAllChooseGroup;
    }

    public void setAllChooseGroup(boolean allChooseGroup) {
        isAllChooseGroup = allChooseGroup;
    }

    public boolean isExpandGroup() {
        return isExpandGroup;
    }

    public void setExpandGroup(boolean expandGroup) {
        isExpandGroup = expandGroup;
    }
}
