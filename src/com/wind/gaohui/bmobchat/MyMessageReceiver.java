package com.wind.gaohui.bmobchat;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.config.BmobConstant;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnReceiveListener;
import cn.bmob.im.util.BmobJsonUtil;
import cn.bmob.v3.listener.FindListener;

import com.bmob.utils.BmobLog;
import com.wind.gaohui.bmobchat.ui.MainActivity;
import com.wind.gaohui.bmobchat.ui.NewFriendActivity;
import com.wind.gaohui.bmobchat.util.CollectionUtils;
import com.wind.gaohui.bmobchat.util.CommonUtils;
import com.wind.gaohui.bombchat.R;

/**
 * ������Ϣ������
 * 
 * @author gaohui
 */
public class MyMessageReceiver extends BroadcastReceiver {

	//�¼�����
	public static List<EventListener> ehList = new ArrayList<EventListener>();
	private BmobUserManager userManager;
	private BmobChatUser currentUser;

	public static int mNewNum = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		String json = intent.getStringExtra("msg");
		BmobLog.i("�յ�����Ϣ��" + json);

		userManager = BmobUserManager.getInstance(context);
		currentUser = userManager.getCurrentUser();
		boolean isNetConnected = CommonUtils.isNetworkAvailable(context);
		if (isNetConnected) { // ����������
			parseMessage(context, json);
		} else {
			for (int i = 0; i < ehList.size(); i++)
				((EventListener) ehList.get(i)).onNetChange(isNetConnected);
		}
	}

	/**
	 * ����json
	 * 
	 * @param context
	 * @param json
	 */
	private void parseMessage(final Context context, String json) {
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			String tag = BmobJsonUtil.getString(obj, BmobConstant.PUSH_KEY_TAG);
			if (tag.equals(BmobConfig.TAG_OFFLINE)) {
				// *************����֪ͨ************
				if (currentUser != null) {
					if (ehList.size() > 0) { // �м�����ʱ�򴫵���ȥ
						for (EventListener handler : ehList) {
							handler.onOffline();
						}
					} else {
						// û�м�����ʱ��ֱ������
						CustomApplication.getInstance().logout();
					}
				}
			} else {
				// ��ʾ��Ϣ����Դ�û�
				String fromId = BmobJsonUtil.getString(obj,
						BmobConstant.PUSH_KEY_TARGETID);
				// ������Ϣ���շ���ObjectId--Ŀ���ǽ�����˻���½ͬһ�豸ʱ���޷����յ��ǵ�ǰ��½�û�����Ϣ
				final String toId = BmobJsonUtil.getString(obj,
						BmobConstant.PUSH_KEY_TOID);
				// ��Ϣ��ʱ��
				String msgTime = BmobJsonUtil.getString(obj,
						BmobConstant.PUSH_READED_MSGTIME);
				if (fromId != null
						&& !BmobDB.create(context, toId).isBlackUser(fromId)) {// ����Ϣ���ͷ���Ϊ�������û�
					if (TextUtils.isEmpty(tag)) {
						// *******��Я��tag��ǩ-->�˿ɽ���İ���˵���Ϣ******
						BmobChatManager.getInstance(context).createReceiveMsg(
								json, new OnReceiveListener() {

									@Override
									public void onSuccess(BmobMsg msg) {
										if (ehList.size() > 0) { // �м�����ʱ�򣬴�����ȥ
											for (int i = 0; i < ehList.size(); i++) {
												ehList.get(i).onMessage(msg);
											}
										} else {
											boolean isAllow = CustomApplication
													.getInstance().getSpUtil()
													.isAllowPushNotify();
											if (isAllow
													&& currentUser != null
													&& currentUser
															.getObjectId()
															.equals(toId)) {
												// ��ǰ�û����ڣ����ҵ�ǰ�û���id���ڽ��շ���id
												mNewNum++;
												showMsgNotify(context, msg);
											}
										}
									}

									@Override
									public void onFailure(int code, String arg1) {
										BmobLog.i("��ȡ���յ���Ϣʧ�ܣ�" + arg1);
									}
								});
					} else {
						// **********��tag��ǩ**********
						if (tag.equals(BmobConfig.TAG_ADD_CONTACT)) {
							// *******��Ӻ�������*******
							// �����������Ϣ��ȡ���ĺ������󣬸��º�̨��δ����ʾ
							BmobInvitation message = BmobChatManager
									.getInstance(context).saveReceiveInvite(
											json, toId);
							if (currentUser != null) { // �е�½�û�
								if (toId.equals(currentUser.getObjectId())) {
									if (ehList.size() > 0) { // �м�����ʱ�򣬴�����ȥ
										for (EventListener handler : ehList) {
											handler.onAddUser(message);
										}
									} else {
										showOtherNotify(context,
												message.getFromname(), toId,
												message.getFromname()
														+ "������Ӻ���",
												NewFriendActivity.class);
									}
								}
							}
						} else if (tag.equals(BmobConfig.TAG_ADD_AGREE)) {
							String username = BmobJsonUtil.getString(obj, BmobConstant.PUSH_KEY_TARGETUSERNAME);
							//�յ��Է���ͬ������֮�󣬾͵���ӶԷ�Ϊ����--��Ĭ�����ͬ�ⷽΪ���ѣ������浽���غ������ݿ�
							BmobUserManager.getInstance(context).addContactAfterAgree(username, new FindListener<BmobChatUser>() {
								
								@Override
								public void onError(int arg0, final String arg1) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void onSuccess(List<BmobChatUser> arg0) {
									// TODO Auto-generated method stub
									//���浽�ڴ���
									CustomApplication.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(context).getContactList()));
								}
							});
							//��ʾ֪ͨ
							showOtherNotify(context, username, toId,  username+"ͬ�������Ϊ����", MainActivity.class);
							//����һ����ʱ��֤�Ự--�����ڻỰ�����γɳ�ʼ�Ự
							BmobMsg.createAndSaveRecentAfterAgree(context, json);
							
						} else if (tag.equals(BmobConfig.TAG_READED)) {
							// *********�Ѷ���ִ*********
							String conversationId = BmobJsonUtil.getString(obj,
									BmobConstant.PUSH_READED_CONVERSIONID);
							if (currentUser != null) {
								// ������Ϣ��״̬
								BmobChatManager.getInstance(context)
										.updateMsgStatus(conversationId,
												msgTime);
								if (ehList.size() > 0) { // �м�����ʱ�򣬴�����ȥ�������޸�ҳ��
									for (EventListener handler : ehList)
										handler.onReaded(conversationId,
												msgTime);
								}
							}
						}
					}
				} else {
					// ******������********
					// �ں������ڼ����е���Ϣ��Ӧ����Ϊ�Ѷ�����Ȼ��ȡ��������֮���ֿ��Բ�ѯ�ĵ�
					BmobChatManager.getInstance(context).updateMsgReaded(true,
							fromId, msgTime);
					BmobLog.i("����Ϣ���ͷ�Ϊ�������û�");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			// �����ȡ�����п�����web��̨���͸��ͻ��˵���Ϣ��Ҳ�п����ǿ������Զ��巢�͵���Ϣ����Ҫ���������н����ʹ���
			BmobLog.i("parseMessage����" + e.getMessage());
		}
	}

	/**
	 * ��ʾ������֪ͨ
	 * 
	 * @param context
	 * @param fromname
	 * @param toId
	 * @param ticker
	 * @param cls
	 */
	private void showOtherNotify(Context context, String fromname, String toId,
			String ticker, Class<?> cls) {
		boolean isAllow = CustomApplication.getInstance().getSpUtil()
				.isAllowPushNotify();
		boolean isAllowVoice = CustomApplication.getInstance().getSpUtil()
				.isAllowVoice();
		boolean isAllowVibrate = CustomApplication.getInstance().getSpUtil()
				.isAllowVibrate();
		if (isAllow && currentUser != null
				&& currentUser.getObjectId().equals(toId)) {
			// ͬʱ����֪ͨ
			BmobNotifyManager.getInstance(context).showNotify(isAllowVoice,
					isAllowVibrate, R.drawable.ic_launcher, ticker, fromname,
					ticker.toString(), NewFriendActivity.class);
		}
	}

	/**
	 * ��ʾ��������Ϣ��֪ͨ
	 * 
	 * @param context
	 * @param msg
	 */
	protected void showMsgNotify(Context context, BmobMsg msg) {
		// ����֪ͨ��
		int icon = R.drawable.ic_launcher;
		String trueMsg = "";
		if (msg.getMsgType() == BmobConfig.TYPE_TEXT
				&& msg.getContent().contains("\\ue")) {
			trueMsg = "[����]";
		} else if (msg.getMsgType() == BmobConfig.TYPE_IMAGE) {
			trueMsg = "[ͼƬ]";
		} else if (msg.getMsgType() == BmobConfig.TYPE_VOICE) {
			trueMsg = "[����]";
		} else if (msg.getMsgType() == BmobConfig.TYPE_LOCATION) {
			trueMsg = "[λ��]";
		} else {
			trueMsg = msg.getContent();
		}
		
		CharSequence tickerText = msg.getBelongUsername() + ":" + trueMsg;
		String contentTitle = msg.getBelongUsername()+ " (" + mNewNum + "������Ϣ)";
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		boolean isAllowVoice = CustomApplication.getInstance().getSpUtil().isAllowVoice();
		boolean isAllowVibrate = CustomApplication.getInstance().getSpUtil().isAllowVibrate();
		
		BmobNotifyManager.getInstance(context).showNotifyWithExtras(isAllowVoice,isAllowVibrate,icon, tickerText.toString(), contentTitle, tickerText.toString(),intent);
	}

}
