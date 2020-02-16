package cn.closeli.rtc;

import java.util.List;

import cn.closeli.rtc.entity.User;
import cn.closeli.rtc.entity.UserGroup;

public class Helper {
    public static User getUserByUserId(String userId, List<UserGroup> userGroups) {
        for(UserGroup g : userGroups) {
            for(User u : g.getMembers()) {
                if(u.getId().equals(userId)) {
                    return u;
                }
            }
        }

        return null;
    }
}
