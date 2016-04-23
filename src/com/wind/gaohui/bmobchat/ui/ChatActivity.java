package com.wind.gaohui.bmobchat.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.db.BmobDB;

import com.wind.gaohui.bmobchat.adapter.MessageChatAdapter;
import com.wind.gaohui.bmobchat.view.xlist.XListView;
import com.wind.gaohui.bombchat.R;

/**
 * 聊天界面
 * 
 * @author gaohui
 * @date 2016年4月23日15:14:19
 */
public class ChatActivity extends ActivityBase {

	private static int MsgPagerNum;
	private BmobChatUser targetUser;
	private String targetId;
	
	XListView mListView;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_chat);

		manager = BmobChatManager.getInstance(this);

		MsgPagerNum = 0;

		targetUser = (BmobChatUser) getIntent().getSerializableExtra("user");
		targetId = targetUser.getObjectId();
		// 注册广播接收器
		initNewMessageBroadCast();
	}

	private void initNewMessageBroadCast() {

	}
	MessageChatAdapter mAdapter;
	/**
	 * 新消息广播接收者
	 */
	private class NewBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String from = intent.getStringExtra("fromId");
			String msgId = intent.getStringExtra("msgId");
			String msgTime = intent.getStringExtra("msgTime");
			//收到这个广播的时候，消息已经在消息列表中，可直接获取
			if(!TextUtils.isEmpty(from) && !TextUtils.isEmpty(msgId) && !TextUtils.isEmpty(msgTime)) {
				BmobMsg msg =BmobChatManager.getInstance(ChatActivity.this).getMessage(msgId, msgTime);
				if(!from.equals(targetId)) {
					//如果不是当前正在聊天的对象，不处理
					return;
				}
				//添加到当前页面
				mAdapter.add(msg);
				//定位
				mListView.setSelection(mAdapter.getCount()-1);
				//取消当前对象的未读显示
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
			// 记得把广播给终结掉
			abortBroadcast();
		}

	}
}
