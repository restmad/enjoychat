package com.wind.gaohui.bmobchat.config;

import android.annotation.SuppressLint;


@SuppressLint("SdCardPath")
public class BmobConstants {
	/**
	 * 我的头像保存目录
	 */
	public static final String MyAvatarDir = "/sdcard/bmobchat/avatar/";
	/**
	 * 注册成功之后登陆页面退出
	 */
	public static final String ACTION_REGISTER_SUCCESS_FINISH = "register.success.finish";
	/**
	 * 拍照修改头像
	 */
	public static final int REQUESTCODE_UPLOADAVATAR_CAMERA = 1;
	/**
	 * 本地相册修改头像
	 */
	public static final int REQUESTCODE_UPLOADAVATAR_LOCATION = 2;
	/**
	 * 系统裁剪头像
	 */
	public static final int REQUESTCODE_UPLOADAVATAR_CROP = 3;

}
