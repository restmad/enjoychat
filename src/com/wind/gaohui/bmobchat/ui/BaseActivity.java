package com.wind.gaohui.bmobchat.ui;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.v3.listener.FindListener;

import com.wind.gaohui.bmobchat.CustomApplication;
import com.wind.gaohui.bmobchat.util.CollectionUtils;
import com.wind.gaohui.bmobchat.view.HeaderLayout;
import com.wind.gaohui.bmobchat.view.HeaderLayout.HeaderStyle;
import com.wind.gaohui.bmobchat.view.HeaderLayout.onLeftImageButtonClickListener;
import com.wind.gaohui.bmobchat.view.HeaderLayout.onRightImageButtonClickListener;
import com.wind.gaohui.bmobchat.view.dialog.DialogTips;
import com.wind.gaohui.bombchat.R;

public class BaseActivity extends FragmentActivity {

	BmobUserManager userManager; // �û�������
	BmobChatManager manager;    //���������
	protected int mScreenWidth;
	protected int mScreenHeight;

	public CustomApplication mApplication;
	/**
	 * ���õ�Header����
	 */
	public HeaderLayout mHeaderLayout;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		userManager = BmobUserManager.getInstance(this);
		manager = BmobChatManager.getInstance(this);
		DisplayMetrics metric = new DisplayMetrics();
		// ����ǰ���ڵ���Ϣ�ŵ�DisplayMetrics��
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenWidth = metric.widthPixels;
		mScreenHeight = metric.heightPixels;

		mApplication = CustomApplication.getInstance();
	}

	Toast mToast;

	public void showToast(final String text) {
		if (!TextUtils.isEmpty(text)) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mToast == null) {
						mToast = Toast.makeText(getApplicationContext(), text,
								Toast.LENGTH_LONG);
					} else {
						mToast.setText(text);
					}
					mToast.show();
				}
			});
		}
	}

	public void showToast(final int resId) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mToast == null) {
					mToast = Toast.makeText(
							BaseActivity.this.getApplicationContext(), resId,
							Toast.LENGTH_LONG);
				} else {
					mToast.setText(resId);
				}
				mToast.show();
			}
		});
	}

	/**
	 * ���ڵ�½�����Զ���½����µ��û����ϼ��������ϵļ�����
	 */
	public void updateUserInfos() {
		// ���µ���λ����Ϣ
		// updateUserLocation();
		// ��ѯ���û��ĺ����б�(��������б���ȥ���������û���Ŷ),Ŀǰ֧�ֵĲ�ѯ���Ѹ���Ϊ100�������޸����ڵ����������ǰ����BmobConfig.LIMIT_CONTACTS���ɡ�
		// ����Ĭ�ϲ�ȡ���ǵ�½�ɹ�֮�󼴽������б�洢�����ݿ��У������µ���ǰ�ڴ���,
		userManager.queryCurrentContactList(new FindListener<BmobChatUser>() {

			@Override
			public void onError(int arg0, String arg1) {
				if (arg0 == BmobConfig.CODE_COMMON_NONE) {
					showLog(arg1);
				} else {
					showLog("��ѯ�����б�ʧ�ܣ�" + arg1);
				}
			}

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// ���浽application�з���Ƚ�
				CustomApplication.getInstance().setContactList(
						CollectionUtils.list2map(arg0));
			}
		});
	}

	/**
	 * ֻ�б���ʱ��ͷ������
	 * 
	 * @param titleName
	 */
	public void initTopBarForOnlyTitle(String titleName) {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		mHeaderLayout.init(HeaderStyle.DEFAULT_TITLE);
		mHeaderLayout.setDefaultTitle(titleName);
	}

	/**
	 * ��ʼ��������-�����Ұ�ť
	 * 
	 * @return void
	 */
	public void initTopBarForBoth(String titleName, int rightDrawableId,
			onRightImageButtonClickListener listener) {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
		mHeaderLayout.setTitleAndLeftImageButton(titleName,
				R.drawable.base_action_bar_back_bg_selector,
				new OnLeftButtonClickListener());
		mHeaderLayout.setTitleAndRightImageButton(titleName, rightDrawableId,
				listener);
	}

	/**
	 * ֻ����߰�ť
	 */
	public void initTopBarForLeft(String titleName) {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		mHeaderLayout.init(HeaderStyle.TITLE_LIFT_IMAGEBUTTON);
		mHeaderLayout.setTitleAndLeftImageButton(titleName,
				R.drawable.base_action_bar_back_bg_selector,
				new OnLeftButtonClickListener());
	}

	/**
	 * ��߅���o�����֘��} initTopBarForRight
	 */
	public void initTopBarForRight(String titleName, int rightDrawableId,
			onRightImageButtonClickListener listener) {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		mHeaderLayout.init(HeaderStyle.TITLE_RIGHT_IMAGEBUTTON);
		mHeaderLayout.setTitleAndRightImageButton(titleName, rightDrawableId,
				listener);
	}

	// ��߰�ť�ĵ���¼�
	public class OnLeftButtonClickListener implements
			onLeftImageButtonClickListener {

		@Override
		public void onClick() {
			finish();
		}
	}
	
	/** ��ʾ���ߵĶԻ���
	  */
	public void showOfflineDialog(final Context context) {
		DialogTips dialog = new DialogTips(this,"�����˺����������豸�ϵ�¼!", "���µ�¼");
		// ���óɹ��¼�
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int userId) {
				CustomApplication.getInstance().logout();
				startActivity(new Intent(context, LoginActivity.class));
				finish();
				dialogInterface.dismiss();
			}
		});
		// ��ʾȷ�϶Ի���
		dialog.show();
		dialog = null;
	}

	/**
	 * ��Log
	 */
	public void showLog(String msg) {
		Log.i("life", msg);
	}

	public void startAnimActivity(Class<?> cla) {
		this.startActivity(new Intent(this, cla));
	}

	public void startAnimActivity(Intent intent) {
		this.startActivity(intent);
	}
}
