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
 * �������
 * 
 * @author gaohui
 * @date 2016��4��23��15:14:19
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
		// ע��㲥������
		initNewMessageBroadCast();
	}

	private void initNewMessageBroadCast() {

	}
	MessageChatAdapter mAdapter;
	/**
	 * ����Ϣ�㲥������
	 */
	private class NewBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String from = intent.getStringExtra("fromId");
			String msgId = intent.getStringExtra("msgId");
			String msgTime = intent.getStringExtra("msgTime");
			//�յ�����㲥��ʱ����Ϣ�Ѿ�����Ϣ�б��У���ֱ�ӻ�ȡ
			if(!TextUtils.isEmpty(from) && !TextUtils.isEmpty(msgId) && !TextUtils.isEmpty(msgTime)) {
				BmobMsg msg =BmobChatManager.getInstance(ChatActivity.this).getMessage(msgId, msgTime);
				if(!from.equals(targetId)) {
					//������ǵ�ǰ��������Ķ��󣬲�����
					return;
				}
				//��ӵ���ǰҳ��
				mAdapter.add(msg);
				//��λ
				mListView.setSelection(mAdapter.getCount()-1);
				//ȡ����ǰ�����δ����ʾ
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
			// �ǵðѹ㲥���ս��
			abortBroadcast();
		}

	}
}
