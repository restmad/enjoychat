package com.wind.gaohui.bmobchat.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.SaveListener;

import com.wind.gaohui.bmobchat.bean.User;
import com.wind.gaohui.bmobchat.config.BmobConstants;
import com.wind.gaohui.bmobchat.util.CommonUtils;
import com.wind.gaohui.bombchat.R;

public class LoginActivity extends BaseActivity implements OnClickListener {

	public static final String TAG = "LoginActivity";
	EditText et_username, et_password;
	Button btn_login;
	TextView btn_register;

	private MyBroadcastReceiver receiver = new MyBroadcastReceiver();
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_login);

		initView();
		initListener();
		
		//注册退出广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(BmobConstants.ACTION_REGISTER_SUCCESS_FINISH);
		Log.d(TAG, "register broadcastreceiver");
		registerReceiver(receiver, filter);
	}

	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if ((intent != null)
					&& BmobConstants.ACTION_REGISTER_SUCCESS_FINISH
							.equals(intent.getAction())) {
				//注册成功后需要将登录页面finish
//				Log.d(TAG, "注册成功，当前页面接收广播退出");
				Log.d(TAG, "register success,now finish current Activity");
				finish();
			}
		}

	}

	private void initView() {
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_register = (TextView) findViewById(R.id.btn_register);
	}

	private void initListener() {
		btn_login.setOnClickListener(this);
		btn_register.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == btn_register) {
			Intent intent = new Intent(LoginActivity.this,
					RegisterActivity.class);
			startActivity(intent);
		} else {
			boolean isConnected = CommonUtils.isNetworkAvailable(this);
			if (!isConnected) {
				showToast(R.string.network_tips);
				return;
			}
			login();
		}
	}

	/**
	 * 登录方法
	 */
	private void login() {
		String name = et_username.getText().toString();
		String password = et_password.getText().toString();

		if (TextUtils.isEmpty(name)) {
			showToast(R.string.toast_error_username_null);
			return;
		}
		if (TextUtils.isEmpty(password)) {
			showToast(R.string.toast_error_password_null);
		}

		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("正在登录...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();

		User user = new User();
		user.setUsername(name);
		user.setPassword(password);
		userManager.login(user, new SaveListener() {

			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {
					public void run() {
						progress.setMessage("正在获取好友资料...");
					}
				});
				// 更新好友资料
				updateUserInfos();
				progress.dismiss();
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}

			@Override
			public void onFailure(int errorCode, String str) {
				progress.dismiss();
				BmobLog.i(str);
				showToast(str);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
//		Log.d(TAG, "解除注册的广播");
		Log.d(TAG, "unregister broadcastreceiver");
	}
}
