package com.wind.gaohui.bmobchat.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 获取网络信息的工具类
 * @author gaohui
 *
 */
public class CommonUtils {

	/**
	 * 检查是否有网络
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
