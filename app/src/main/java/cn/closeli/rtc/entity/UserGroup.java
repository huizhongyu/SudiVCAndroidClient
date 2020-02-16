package cn.closeli.rtc.entity;

import java.util.HashSet;
import java.util.Set;

public class UserGroup {
	private String id;

	private String title;

	private User host;

	private Set<User> members;
	
	public UserGroup() {
	}
	
	public UserGroup(String id, String title, User host) {
		this.id = id;
		this.title = title;
		this.host = host;
		this.members = new HashSet<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public User getHost() {
		return host;
	}

	public void setHost(User host) {
		this.host = host;
	}

	public Set<User> getMembers() {
		return members;
	}

	public void setMembers(Set<User> members) {
		this.members = members;
	}

	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof UserGroup))return false;
	    UserGroup otherCourse = (UserGroup)other;
	    return (otherCourse.id.equals(this.id));
	}
}
