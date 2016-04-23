package com.wind.gaohui.bmobchat.ui;

import cn.bmob.im.BmobUserManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * 除登录注册和欢迎页面外继承的基类--用于检测用户在其他设备是否登录了同一账号
 * 
 * @author gaohui
 * 
 */
public class ActivityBase extends BaseActivity {

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		// 自动登录状态下是否在其他设备登录
		checkLogin();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO 锁屏状态下检查--》对于锁屏状态和onResume方法的理解
		checkLogin();
	}

	/**
	 * 检查登录
	 */
	private void checkLogin() {
		BmobUserManager userManager = BmobUserManager.getInstance(this);
		if (userManager.getCurrentUser() == null) {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		}
	}

	/**
	 * 隐藏软键盘
	 */
	public void hideInputSoftView() {
		InputMethodManager manager = (InputMethodManager) this
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null) {
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}
}
