package com.wind.gaohui.bmobchat.bean;

import cn.bmob.v3.BmobObject;

public class Blog extends BmobObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 博客短文
	 */
	private String brief;
	
	/**
	 * 博客的作者
	 */
	private User user;

	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	
}
