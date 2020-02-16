package cn.closeli.rtc.entity;

public class User {
	private String id;

	private String account;

	private String nickName;

	private String role;

	public User() {}
	
	public User(String id, String account, String nickName, String role){
		this.id = id;
		this.account = account;
        this.nickName = nickName;
		this.role = role;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof User))return false;
	    User otherUser = (User)other;
	    return ((otherUser.id.equals(this.id)) && (otherUser.account.equals(this.account)));
	}
	
	@Override
	public int hashCode() {
	    return account.hashCode();
	}

}
