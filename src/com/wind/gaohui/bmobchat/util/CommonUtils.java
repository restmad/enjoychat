package com.wind.gaohui.bmobchat.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * ��ȡ������Ϣ�Ĺ�����
 * @author gaohui
 *
 */
public class CommonUtils {

	/**
	 * ����Ƿ�������
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		NetworkInfo info = getNetWorkInfo(context);
		if (info != null) {
			return info.isAvailable();
		}
		return false;

	}

	/**
	 * @param context
	 * @return
	 */
	private static NetworkInfo getNetWorkInfo(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}
}
