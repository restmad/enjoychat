package com.wind.gaohui.bmobchat.config;

import android.annotation.SuppressLint;


@SuppressLint("SdCardPath")
public class BmobConstants {
	/**
	 * �ҵ�ͷ�񱣴�Ŀ¼
	 */
	public static final String MyAvatarDir = "/sdcard/bmobchat/avatar/";
	/**
	 * ע��ɹ�֮���½ҳ���˳�
	 */
	public static final String ACTION_REGISTER_SUCCESS_FINISH = "register.success.finish";
	/**
	 * �����޸�ͷ��
	 */
	public static final int REQUESTCODE_UPLOADAVATAR_CAMERA = 1;
	/**
	 * ��������޸�ͷ��
	 */
	public static final int REQUESTCODE_UPLOADAVATAR_LOCATION = 2;
	/**
	 * ϵͳ�ü�ͷ��
	 */
	public static final int REQUESTCODE_UPLOADAVATAR_CROP = 3;

}
