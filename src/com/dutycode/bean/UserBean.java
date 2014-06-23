package com.dutycode.bean;

/**
 * 用户信息
 * @author michael
 *
 */
public class UserBean {
	
	private String userName ;
	private String name;
	private String userJID;
	private String email;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserJID() {
		return userJID;
	}
	public void setUserJID(String userJID) {
		this.userJID = userJID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
