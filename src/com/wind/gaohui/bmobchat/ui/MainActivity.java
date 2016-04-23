package com.wind.gaohui.bmobchat.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import cn.bmob.im.BmobChat;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;

import com.wind.gaohui.bmobchat.CustomApplication;
import com.wind.gaohui.bmobchat.MyMessageReceiver;
import com.wind.gaohui.bmobchat.ui.fragments.ContactFragment;
import com.wind.gaohui.bmobchat.ui.fragments.RecentFragment;
import com.wind.gaohui.bmobchat.ui.fragments.SettingFragment;
import com.wind.gaohui.bombchat.R;

public class MainActivity extends ActivityBase implements EventListener {

	public static final String TAG = "MainActivity" ;
	private List<Fragment> fragments = new ArrayList<Fragment>();

	private Button[] mTab;
	private ImageView iv_recent_tips;
	private ImageView iv_contact_tips;
	private int index;
	private int currentTabIndex;

	private RecentFragment recentFragment;

	private ContactFragment contactFragment;

	private SettingFragment settingFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ������ʱ�����񣨵�λΪ�룩-���������̨�Ƿ���δ������Ϣ���еĻ���ȡ����
		// �������ü�����ȽϺ������͵�������Ҳ����ȥ����仰-ͬʱ����onDestory���������stopPollService����
		BmobChat.getInstance(this).startPollService(20);

		// �����㲥������
		initNewMessageBroadCast();
		initTagMessageBroadCast();
		initView();

	}

	private void initView() {
		mTab = new Button[4];
		mTab[0] = (Button) findViewById(R.id.btn_message);
		mTab[1] = (Button) findViewById(R.id.btn_contract);
		mTab[2] = (Button) findViewById(R.id.btn_set);
		iv_recent_tips = (ImageView) findViewById(R.id.iv_recent_tips);
		iv_contact_tips = (ImageView) findViewById(R.id.iv_contact_tips);

		// �ѵ�һ��tab����Ϊѡ��״̬
		mTab[0].setSelected(true);

		recentFragment = new RecentFragment();
		contactFragment = new ContactFragment();
		settingFragment = new SettingFragment();
		fragments.add(recentFragment);
		fragments.add(contactFragment);
		fragments.add(settingFragment);
		// �����ʾ��һ��fragment TODO�����������add�����
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, fragments.get(0))
				.add(R.id.fragment_container, fragments.get(2))
				.hide(fragments.get(2)).show(fragments.get(0)).commit();

	}

	/**
	 * button����¼�
	 * 
	 * @param view
	 */
	public void onTabSelect(View view) {
		switch (view.getId()) {
		case R.id.btn_message:
			index = 0;
			break;
		case R.id.btn_contract:
			index = 1;
			break;
		case R.id.btn_set:
			index = 2;
			break;
		}
		if (currentTabIndex != index) {
			FragmentTransaction trx = getSupportFragmentManager()
					.beginTransaction();
			trx.hide(fragments.get(currentTabIndex));
			if (!fragments.get(index).isAdded()) {
				trx.add(R.id.fragment_container, fragments.get(index));
			}
			trx.show(fragments.get(index)).commit();
		}
		mTab[currentTabIndex].setSelected(false);
		// �ѵ�ǰtab��Ϊѡ��״̬
		mTab[index].setSelected(true);
		currentTabIndex = index;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// СԲ����ʾ
		if (BmobDB.create(this).hasUnReadMsg()) {
			iv_recent_tips.setVisibility(View.VISIBLE);
		} else {
			iv_recent_tips.setVisibility(View.GONE);
		}
		if (BmobDB.create(this).hasNewInvite()) {
			iv_contact_tips.setVisibility(View.VISIBLE);
		} else {
			iv_contact_tips.setVisibility(View.GONE);
		}
		// �������͵���Ϣ
		MyMessageReceiver.ehList.add(this);
		// ���
		MyMessageReceiver.mNewNum = 0;
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ȡ���������͵���Ϣ
		MyMessageReceiver.ehList.remove(this);
	}

	@Override
	public void onMessage(BmobMsg message) {
		refreshNewMessage(message);
	}

	/**
	 * ˢ�½���
	 * 
	 * @param message
	 */
	private void refreshNewMessage(BmobMsg message) {
		boolean isAllowVoice = CustomApplication.getInstance().getSpUtil()
				.isAllowVoice();
		if (isAllowVoice) {
			CustomApplication.getInstance().getMediaPlayer().start();
		}
		iv_recent_tips.setVisibility(View.VISIBLE);
		// �洢����
		if (message != null) {
			BmobChatManager.getInstance(MainActivity.this).saveReceiveMessage(
					true, message);
		}
		if (currentTabIndex == 0) {
			// ��ǰҳ�����Ϊ�Ựҳ�棬ˢ�´�ҳ��
			if (recentFragment != null) {
				recentFragment.refresh();
			}
		}
	}

	NewBroadcastReceiver newBroadCastReceiver;

	private void initNewMessageBroadCast() {
		// ע�������Ϣ�㲥
		newBroadCastReceiver = new NewBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(
				BmobConfig.BROADCAST_NEW_MESSAGE);
		// ���ȼ�Ҫ����ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(newBroadCastReceiver, intentFilter);
	}

	/**
	 * ����Ϣ�Ĺ㲥������
	 */
	private class NewBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// ˢ�½���
			refreshNewMessage(null);
			// ���㲥�ս��
			abortBroadcast();
		}

	}

	TagBroadcastReceiver userReceiver;

	private void initTagMessageBroadCast() {
		// ע�������Ϣ�㲥
		userReceiver = new TagBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(
				BmobConfig.BROADCAST_ADD_USER_MESSAGE);
		// ���ȼ�Ҫ����ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(userReceiver, intentFilter);
	}

	/**
	 * ��ǩ��Ϣ�㲥������
	 */
	private class TagBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			BmobInvitation message = (BmobInvitation) intent
					.getSerializableExtra("invite");
			Log.d(TAG,"BmobInvitation info:" + message.getFromname());
			refreshInvite(message);
			// �ǵðѹ㲥���ս��
			abortBroadcast();
		}
	}

	public void refreshInvite(BmobInvitation message) {
		boolean isAllow = CustomApplication.getInstance().getSpUtil()
				.isAllowVoice();
		if (isAllow) {
			CustomApplication.getInstance().getMediaPlayer().start();
		}
		iv_contact_tips.setVisibility(View.VISIBLE);
		if (currentTabIndex == 1) {
			if (contactFragment != null) {
				contactFragment.refresh();
			}
		} else {
			// ͬʱ����֪ͨ
			String tickerText = message.getFromname() + "������Ӻ���";
			boolean isAllowVibrate = CustomApplication.getInstance().getSpUtil()
					.isAllowVibrate();
			BmobNotifyManager.getInstance(this).showNotify(isAllow,
					isAllowVibrate, R.drawable.ic_launcher, tickerText,
					message.getFromname(), tickerText.toString(),
					NewFriendActivity.class);
		}
	}

	@Override
	public void onReaded(String conversionId, String msgTime) {

	}

	@Override
	public void onNetChange(boolean isNetConnected) {
		if (isNetConnected) {
			showToast(R.string.network_tips);
		}
	}

	@Override
	public void onAddUser(BmobInvitation message) {
		refreshInvite(message);
	}

	@Override
	public void onOffline() {
		showOfflineDialog(this);
	}

	private static long firstTime;

	/**
	 * ���������η��ؼ����˳�
	 */
	@Override
	public void onBackPressed() {
		if (firstTime + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
		} else {
			showToast("�ٰ�һ���˳�����");
		}
		firstTime = System.currentTimeMillis();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {
			unregisterReceiver(newBroadCastReceiver);
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(userReceiver);
		} catch (Exception e) {
		}
		// ȡ����ʱ������
		// BmobChat.getInstance(this).stopPollService();
	}
}
