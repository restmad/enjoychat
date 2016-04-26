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
 * 推送消息监听器
 * 
 * @author gaohui
 */
public class MyMessageReceiver extends BroadcastReceiver {

	//事件监听
	public static List<EventListener> ehList = new ArrayList<EventListener>();
	private BmobUserManager userManager;
	private BmobChatUser currentUser;

	public static int mNewNum = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		String json = intent.getStringExtra("msg");
		BmobLog.i("收到的消息：" + json);

		userManager = BmobUserManager.getInstance(context);
		currentUser = userManager.getCurrentUser();
		boolean isNetConnected = CommonUtils.isNetworkAvailable(context);
		if (isNetConnected) { // 有网络连接
			parseMessage(context, json);
		} else {
			for (int i = 0; i < ehList.size(); i++)
				((EventListener) ehList.get(i)).onNetChange(isNetConnected);
		}
	}

	/**
	 * 解析json
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
				// *************下线通知************
				if (currentUser != null) {
					if (ehList.size() > 0) { // 有监听的时候传递下去
						for (EventListener handler : ehList) {
							handler.onOffline();
						}
					} else {
						// 没有监听的时候直接下线
						CustomApplication.getInstance().logout();
					}
				}
			} else {
				// 表示消息的来源用户
				String fromId = BmobJsonUtil.getString(obj,
						BmobConstant.PUSH_KEY_TARGETID);
				// 增加消息接收方的ObjectId--目的是解决多账户登陆同一设备时，无法接收到非当前登陆用户的消息
				final String toId = BmobJsonUtil.getString(obj,
						BmobConstant.PUSH_KEY_TOID);
				// 消息的时间
				String msgTime = BmobJsonUtil.getString(obj,
						BmobConstant.PUSH_READED_MSGTIME);
				if (fromId != null
						&& !BmobDB.create(context, toId).isBlackUser(fromId)) {// 该消息发送方不为黑名单用户
					if (TextUtils.isEmpty(tag)) {
						// *******不携带tag标签-->此可接收陌生人的消息******
						BmobChatManager.getInstance(context).createReceiveMsg(
								json, new OnReceiveListener() {

									@Override
									public void onSuccess(BmobMsg msg) {
										if (ehList.size() > 0) { // 有监听的时候，传递下去
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
												// 当前用户存在，而且当前用户的id等于接收方的id
												mNewNum++;
												showMsgNotify(context, msg);
											}
										}
									}

									@Override
									public void onFailure(int code, String arg1) {
										BmobLog.i("获取接收的消息失败：" + arg1);
									}
								});
					} else {
						// **********带tag标签**********
						if (tag.equals(BmobConfig.TAG_ADD_CONTACT)) {
							// *******添加好友请求*******
							// 保存从推送消息中取到的好友请求，更新后台的未读标示
							BmobInvitation message = BmobChatManager
									.getInstance(context).saveReceiveInvite(
											json, toId);
							if (currentUser != null) { // 有登陆用户
								if (toId.equals(currentUser.getObjectId())) {
									if (ehList.size() > 0) { // 有监听的时候，传递下去
										for (EventListener handler : ehList) {
											handler.onAddUser(message);
										}
									} else {
										showOtherNotify(context,
												message.getFromname(), toId,
												message.getFromname()
														+ "请求添加好友",
												NewFriendActivity.class);
									}
								}
							}
						} else if (tag.equals(BmobConfig.TAG_ADD_AGREE)) {
							String username = BmobJsonUtil.getString(obj, BmobConstant.PUSH_KEY_TARGETUSERNAME);
							//收到对方的同意请求之后，就得添加对方为好友--已默认添加同意方为好友，并保存到本地好友数据库
							BmobUserManager.getInstance(context).addContactAfterAgree(username, new FindListener<BmobChatUser>() {
								
								@Override
								public void onError(int arg0, final String arg1) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void onSuccess(List<BmobChatUser> arg0) {
									// TODO Auto-generated method stub
									//保存到内存中
									CustomApplication.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(context).getContactList()));
								}
							});
							//显示通知
							showOtherNotify(context, username, toId,  username+"同意添加您为好友", MainActivity.class);
							//创建一个临时验证会话--用于在会话界面形成初始会话
							BmobMsg.createAndSaveRecentAfterAgree(context, json);
							
						} else if (tag.equals(BmobConfig.TAG_READED)) {
							// *********已读回执*********
							String conversationId = BmobJsonUtil.getString(obj,
									BmobConstant.PUSH_READED_CONVERSIONID);
							if (currentUser != null) {
								// 更新消息的状态
								BmobChatManager.getInstance(context)
										.updateMsgStatus(conversationId,
												msgTime);
								if (ehList.size() > 0) { // 有监听的时候，传递下去，便于修改页面
									for (EventListener handler : ehList)
										handler.onReaded(conversationId,
												msgTime);
								}
							}
						}
					}
				} else {
					// ******黑名单********
					// 在黑名单期间所有的消息都应该置为已读，不然等取消黑名单之后又可以查询的到
					BmobChatManager.getInstance(context).updateMsgReaded(true,
							fromId, msgTime);
					BmobLog.i("该消息发送方为黑名单用户");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			// 这里截取到的有可能是web后台推送给客户端的消息，也有可能是开发者自定义发送的消息，需要开发者自行解析和处理
			BmobLog.i("parseMessage错误：" + e.getMessage());
		}
	}

	/**
	 * 显示其他的通知
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
			// 同时提醒通知
			BmobNotifyManager.getInstance(context).showNotify(isAllowVoice,
					isAllowVibrate, R.drawable.ic_launcher, ticker, fromname,
					ticker.toString(), NewFriendActivity.class);
		}
	}

	/**
	 * 显示与聊天消息的通知
	 * 
	 * @param context
	 * @param msg
	 */
	protected void showMsgNotify(Context context, BmobMsg msg) {
		// 更新通知栏
		int icon = R.drawable.ic_launcher;
		String trueMsg = "";
		if (msg.getMsgType() == BmobConfig.TYPE_TEXT
				&& msg.getContent().contains("\\ue")) {
			trueMsg = "[表情]";
		} else if (msg.getMsgType() == BmobConfig.TYPE_IMAGE) {
			trueMsg = "[图片]";
		} else if (msg.getMsgType() == BmobConfig.TYPE_VOICE) {
			trueMsg = "[语音]";
		} else if (msg.getMsgType() == BmobConfig.TYPE_LOCATION) {
			trueMsg = "[位置]";
		} else {
			trueMsg = msg.getContent();
		}
		
		CharSequence tickerText = msg.getBelongUsername() + ":" + trueMsg;
		String contentTitle = msg.getBelongUsername()+ " (" + mNewNum + "条新消息)";
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		boolean isAllowVoice = CustomApplication.getInstance().getSpUtil().isAllowVoice();
		boolean isAllowVibrate = CustomApplication.getInstance().getSpUtil().isAllowVibrate();
		
		BmobNotifyManager.getInstance(context).showNotifyWithExtras(isAllowVoice,isAllowVibrate,icon, tickerText.toString(), contentTitle, tickerText.toString(),intent);
	}

}
