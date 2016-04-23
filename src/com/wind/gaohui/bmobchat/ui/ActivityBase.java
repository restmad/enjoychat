package com.wind.gaohui.bmobchat.ui;

import cn.bmob.im.BmobUserManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * ����¼ע��ͻ�ӭҳ����̳еĻ���--���ڼ���û��������豸�Ƿ��¼��ͬһ�˺�
 * 
 * @author gaohui
 * 
 */
public class ActivityBase extends BaseActivity {

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		// �Զ���¼״̬���Ƿ��������豸��¼
		checkLogin();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO ����״̬�¼��--����������״̬��onResume���������
		checkLogin();
	}

	/**
	 * ����¼
	 */
	private void checkLogin() {
		BmobUserManager userManager = BmobUserManager.getInstance(this);
		if (userManager.getCurrentUser() == null) {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		}
	}

	/**
	 * ���������
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
