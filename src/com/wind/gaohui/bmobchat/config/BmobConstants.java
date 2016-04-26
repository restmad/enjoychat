package com.wind.gaohui.bmobchat.config;

import android.annotation.SuppressLint;
import android.os.Environment;


@SuppressLint("SdCardPath")
public class BmobConstants {
	
	/**
	 * 存放发送图片的目录
	 */
	public static String BMOB_PICTURE_PATH = Environment.getExternalStorageDirectory()	+ "/bmobchat/image/";
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
	
	public static final int REQUESTCODE_TAKE_CAMERA = 0x000001;//拍照
	public static final int REQUESTCODE_TAKE_LOCAL = 0x000002;//本地图片
	public static final int REQUESTCODE_TAKE_LOCATION = 0x000003;//位置

}
