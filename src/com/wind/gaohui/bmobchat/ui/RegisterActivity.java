package com.wind.gaohui.bmobchat.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.SaveListener;

import com.wind.gaohui.bmobchat.bean.User;
import com.wind.gaohui.bmobchat.config.BmobConstants;
import com.wind.gaohui.bmobchat.util.CommonUtils;
import com.wind.gaohui.bombchat.R;

public class RegisterActivity extends BaseActivity {

	protected static final String TAG = "RegisterActivity";
	EditText et_username, et_password, et_email;
	Button btn_register;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_register);

		initView();

	}

	private void initView() {
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		et_email = (EditText) findViewById(R.id.et_email);
		btn_register = (Button) findViewById(R.id.btn_register);

		btn_register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				register();
			}
		});
	}

	protected void register() {
		String name = et_username.getText().toString();
		String password = et_password.getText().toString();
		String pwd_again = et_email.getText().toString();

		if (TextUtils.isEmpty(name)) {
			showToast(R.string.toast_error_username_null);
			return;
		}
		if (TextUtils.isEmpty(password)) {
			showToast(R.string.toast_error_password_null);
			return;
		}
		if (!pwd_again.equals(password)) {
			showToast(R.string.toast_error_comfirm_password);
			return;
		}
		boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
		if (!isNetConnected) {
			showToast(R.string.network_tips);
			return;
		}
		
		final ProgressDialog progress = new ProgressDialog(RegisterActivity.this);
		progress.setMessage("正在注册...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		//由于每个应用的注册所需的资料都不一样，故IM sdk未提供注册方法，用户可按照Bmod SDK的注册方式进行注册。
		//注册的时候需要注意两点：1、User表中绑定设备id和type，2、设备表中绑定username字段
		final User bu = new User();
		bu.setUsername(name);
		bu.setPassword(password);
		//将user和设备id进行绑定aa
		bu.setSex(true);
		bu.setDeviceType("android");
		bu.setInstallId(BmobInstallation.getInstallationId(this));
		bu.signUp(RegisterActivity.this, new SaveListener() {

			@Override
			public void onSuccess() {
				progress.dismiss();
				showToast("注册成功");
				// 将设备与username进行绑定
				userManager.bindInstallationForRegister(bu.getUsername());
				//更新地理位置信息
				//updateUserLocation();
				//发广播通知登陆页面退出
				sendBroadcast(new Intent(BmobConstants.ACTION_REGISTER_SUCCESS_FINISH));
				Log.d(TAG, "send broadcast to info LogingActivity finish");
				// 启动主页
				Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
				
			}

			@Override
			public void onFailure(int errorCode, String mes) {
				BmobLog.i(mes);
				showToast("注册失败:" + mes);
				progress.dismiss();
			}
		});
	}
}
