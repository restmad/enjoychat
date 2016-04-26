package com.wind.gaohui.bmobchat.config;

import android.annotation.SuppressLint;
import android.os.Environment;


@SuppressLint("SdCardPath")
public class BmobConstants {
	
	/**
	 * ��ŷ���ͼƬ��Ŀ¼
	 */
	public static String BMOB_PICTURE_PATH = Environment.getExternalStorageDirectory()	+ "/bmobchat/image/";
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
	
	public static final int REQUESTCODE_TAKE_CAMERA = 0x000001;//����
	public static final int REQUESTCODE_TAKE_LOCAL = 0x000002;//����ͼƬ
	public static final int REQUESTCODE_TAKE_LOCATION = 0x000003;//λ��

}
