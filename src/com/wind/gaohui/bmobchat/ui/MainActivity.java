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

		// 开启定时检测服务（单位为秒）-在这里检测后台是否还有未读的消息，有的话就取出来
		// 如果你觉得检测服务比较耗流量和电量，你也可以去掉这句话-同时还有onDestory方法里面的stopPollService方法
		BmobChat.getInstance(this).startPollService(20);

		// 开启广播接收器
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

		// 把第一个tab设置为选中状态
		mTab[0].setSelected(true);

		recentFragment = new RecentFragment();
		contactFragment = new ContactFragment();
		settingFragment = new SettingFragment();
		fragments.add(recentFragment);
		fragments.add(contactFragment);
		fragments.add(settingFragment);
		// 添加显示第一个fragment TODO这里对于两次add的理解
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, fragments.get(0))
				.add(R.id.fragment_container, fragments.get(2))
				.hide(fragments.get(2)).show(fragments.get(0)).commit();

	}

	/**
	 * button点击事件
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
		// 把当前tab设为选中状态
		mTab[index].setSelected(true);
		currentTabIndex = index;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 小圆点提示
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
		// 监听推送的消息
		MyMessageReceiver.ehList.add(this);
		// 清空
		MyMessageReceiver.mNewNum = 0;
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 取消监听推送的消息
		MyMessageReceiver.ehList.remove(this);
	}

	@Override
	public void onMessage(BmobMsg message) {
		refreshNewMessage(message);
	}

	/**
	 * 刷新界面
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
		// 存储起来
		if (message != null) {
			BmobChatManager.getInstance(MainActivity.this).saveReceiveMessage(
					true, message);
		}
		if (currentTabIndex == 0) {
			// 当前页面如果为会话页面，刷新此页面
			if (recentFragment != null) {
				recentFragment.refresh();
			}
		}
	}

	NewBroadcastReceiver newBroadCastReceiver;

	private void initNewMessageBroadCast() {
		// 注册接收消息广播
		newBroadCastReceiver = new NewBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(
				BmobConfig.BROADCAST_NEW_MESSAGE);
		// 优先级要低于ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(newBroadCastReceiver, intentFilter);
	}

	/**
	 * 新消息的广播接收者
	 */
	private class NewBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 刷新界面
			refreshNewMessage(null);
			// 将广播终结掉
			abortBroadcast();
		}

	}

	TagBroadcastReceiver userReceiver;

	private void initTagMessageBroadCast() {
		// 注册接收消息广播
		userReceiver = new TagBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(
				BmobConfig.BROADCAST_ADD_USER_MESSAGE);
		// 优先级要低于ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(userReceiver, intentFilter);
	}

	/**
	 * 标签消息广播接收者
	 */
	private class TagBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			BmobInvitation message = (BmobInvitation) intent
					.getSerializableExtra("invite");
			Log.d(TAG,"BmobInvitation info:" + message.getFromname());
			refreshInvite(message);
			// 记得把广播给终结掉
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
			// 同时提醒通知
			String tickerText = message.getFromname() + "请求添加好友";
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
	 * 连续按两次返回键就退出
	 */
	@Override
	public void onBackPressed() {
		if (firstTime + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
		} else {
			showToast("再按一次退出程序");
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
		// 取消定时检测服务
		// BmobChat.getInstance(this).stopPollService();
	}
}
