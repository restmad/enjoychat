package com.wind.gaohui.bmobchat.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobRecordManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnRecordChangeListener;
import cn.bmob.im.inteface.UploadListener;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.PushListener;

import com.wind.gaohui.bmobchat.MyMessageReceiver;
import com.wind.gaohui.bmobchat.adapter.EmoViewPagerAdapter;
import com.wind.gaohui.bmobchat.adapter.EmoteAdapter;
import com.wind.gaohui.bmobchat.adapter.MessageChatAdapter;
import com.wind.gaohui.bmobchat.adapter.NewRecordPlayClickListener;
import com.wind.gaohui.bmobchat.bean.FaceText;
import com.wind.gaohui.bmobchat.config.BmobConstants;
import com.wind.gaohui.bmobchat.util.CommonUtils;
import com.wind.gaohui.bmobchat.util.FaceTextUtils;
import com.wind.gaohui.bmobchat.view.EmotionsEditText;
import com.wind.gaohui.bmobchat.view.HeaderLayout;
import com.wind.gaohui.bmobchat.view.dialog.DialogTips;
import com.wind.gaohui.bmobchat.view.xlist.XListView;
import com.wind.gaohui.bmobchat.view.xlist.XListView.IXListViewListener;
import com.wind.gaohui.bombchat.R;

/**
 * �������
 * 
 * @author gaohui
 * @date 2016��4��23��15:14:19
 */
@SuppressLint({ "ClickableViewAccessibility", "InflateParams" })
public class ChatActivity extends ActivityBase implements OnClickListener, IXListViewListener,EventListener {

	private static int MsgPagerNum;
	private BmobChatUser targetUser;
	private String targetId;
	
	XListView mListView;
	
	MessageChatAdapter mAdapter;
	private NewBroadcastReceiver receiver;
	private BmobRecordManager recordManager;
	private Button btn_chat_add;
	private Button btn_chat_emo;
	private Button btn_chat_keyboard;
	private Button btn_chat_voice;
	private Button btn_chat_send;
	private LinearLayout layout_more;
	private LinearLayout layout_add;
	private LinearLayout layout_emo;
	private TextView tv_picture;
	private TextView tv_camera;
	private TextView tv_location;
	private ViewPager pager_emo;
	
	private static final int NEW_MESSAGE = 0x001;  //�յ�����Ϣ

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
		initView();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == NEW_MESSAGE) {
				BmobMsg message = (BmobMsg) msg.obj;
				String uid = message.getBelongId();
				BmobMsg m = BmobChatManager.getInstance(ChatActivity.this).getMessage(message.getConversationId(),message.getMsgTime());
				if (!uid.equals(targetId))// ������ǵ�ǰ��������������Ϣ��������
					return;
				mAdapter.add(m);
				//��λ
				mListView.setSelection(mAdapter.getCount()-1);
				//ȡ����ǰ�����δ����ʾ
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
		}
		
	};

	private void initView() {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		mListView = (XListView) findViewById(R.id.mListView);
		initTopBarForLeft("��" + targetUser.getUsername() + "�Ի�");
		initBoottomView();
		initXListView();
		initVoiceView();
	}

	/**
	 * ��ʼ����������
	 */
	private void initVoiceView() {
		layout_record = (RelativeLayout) findViewById(R.id.layout_record);
		tv_voice_tips = (TextView) findViewById(R.id.tv_voice_tips);
		iv_record = (ImageView) findViewById(R.id.iv_record);
		btn_speak.setOnTouchListener(new VoiceTouchListen());
		initVoiceAnimRes();
		initRecordManager();
	}
	/**
	 * ��ʼ������������
	 */
	private void initRecordManager() {
		//������ع�����
		recordManager = BmobRecordManager.getInstance(this);
		recordManager.setOnRecordChangeListener(new OnRecordChangeListener() {
			
			@Override
			public void onVolumnChanged(int value) {
				iv_record.setImageDrawable(drawable_Anims[value]);
			}
			
			@Override
			public void onTimeChanged(int recordTime, String localPath) {
				BmobLog.i("voice","��¼������:" + recordTime);
				if(recordTime >= BmobRecordManager.MAX_RECORD_TIME) {
					//��Ҫ���ð�ť
					btn_speak.setPressed(false);
					btn_speak.setClickable(false);
					//ȡ��¼����
					layout_record.setVisibility(View.INVISIBLE);
					//����������Ϣ
					sendVoiceMessage(localPath,recordTime);
					//��Ϊ�˷�ֹ����¼��ʱ��󣬻�෢һ��������ȥ�����
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							btn_speak.setClickable(true);
						}
					}, 1000);
				}
			}
		});
	}

	/**
	 * ��ʼ������������Դ
	 */
	private void initVoiceAnimRes() {
		drawable_Anims = new Drawable[]{
				getResources().getDrawable(R.drawable.chat_icon_voice2),
				getResources().getDrawable(R.drawable.chat_icon_voice3),
				getResources().getDrawable(R.drawable.chat_icon_voice4),
				getResources().getDrawable(R.drawable.chat_icon_voice5),
				getResources().getDrawable(R.drawable.chat_icon_voice6)
		};
	}

	/**
	 * ����˵�� 
	 */
	class VoiceTouchListen implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!CommonUtils.checkSdCard()) {
					showToast("����������Ҫsdcard֧�֣�");
					return false;
				}
				try {
					v.setPressed(true);
					layout_record.setVisibility(View.VISIBLE);
					tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
					// ��ʼ¼��
					recordManager.startRecording(targetId);
				} catch (Exception e) {
				}
				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					tv_voice_tips
							.setText(getString(R.string.voice_cancel_tips));
					tv_voice_tips.setTextColor(Color.RED);
				} else {
					tv_voice_tips.setText(getString(R.string.voice_up_tips));
					tv_voice_tips.setTextColor(Color.WHITE);
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				layout_record.setVisibility(View.INVISIBLE);
				try {
					if (event.getY() < 0) {// ����¼��
						recordManager.cancelRecording();
						BmobLog.i("voice", "������������");
					} else {
						int recordTime = recordManager.stopRecording();
						if (recordTime > 1) {
							// ���������ļ�
							BmobLog.i("voice", "��������");
							sendVoiceMessage(
									recordManager.getRecordFilePath(targetId),
									recordTime);
						} else {// ¼��ʱ����̣�����ʾ¼�����̵���ʾ
							layout_record.setVisibility(View.GONE);
							showShortToast().show();
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				return true;
			default:
				return false;
			}
		}
	}
	
	Toast toast;
	/**
	 * ��ʾ¼��ʱ����̵�toast
	 */
	public Toast showShortToast() {
		if(toast == null) {
			toast = new Toast(this);
		}
		View view = LayoutInflater.from(this).inflate(R.layout.include_chat_voice_short, null);
		toast.setView(view);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(50);
		return toast;
	}

	/**
	 * ����������Ϣ
	 * @param recordFilePath
	 * @param recordTime
	 */
	public void sendVoiceMessage(String localPath, int length) {
		manager.sendVoiceMessage(targetUser, localPath, length, new UploadListener() {
			
			@Override
			public void onSuccess() {
				mAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onStart(BmobMsg msg) {
				refreshMessage(msg);
			}
			
			@Override
			public void onFailure(int error, String arg1) {
				showLog("�ϴ�����ʧ�ܣ�arg1:" + arg1);
				mAdapter.notifyDataSetChanged();
			}
		});
	}
	/**
	 * ˢ�½���
	 * @param msg
	 */
	protected void refreshMessage(BmobMsg msg) {
		mAdapter.add(msg);
		mListView.setSelection(mAdapter.getCount()-1);
		edit_user_comment.setText("");
	}

	@SuppressLint("ClickableViewAccessibility")
	private void initXListView() {
		//��������ظ���
		mListView.setPullLoadEnable(false);
		//��������
		mListView.setPullRefreshEnable(true);
		//���ü�����
		mListView.setXListViewListener(this);
		
		mListView.pullRefreshing();
		mListView.setDividerHeight(0);
		//��������
		initOrRefresh();
		
		mListView.setSelection(mAdapter.getCount()-1);
		mListView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//���������
				hideInputSoftView();
				layout_more.setVisibility(View.GONE);
				layout_add.setVisibility(View.GONE);
				btn_chat_voice.setVisibility(View.VISIBLE);
				btn_chat_keyboard.setVisibility(View.GONE);
				btn_chat_send.setVisibility(View.GONE);
				return false;
			}
		});
		
		//�ط���ť�ĵ���¼�
		mAdapter.setOnInViewListener(R.id.iv_fail_resend, new MessageChatAdapter.OnInternalClickListener(){

			@Override
			public void OnClickListener(View parentView, View view,
					Integer position, Object values) {
				// �ط���Ϣ
				showResendDialog(parentView, view, values);
			}});
	}

	/**
	 * �ط���Ϣ�Ի���
	 * @param parentView
	 * @param view
	 * @param values
	 */
	protected void showResendDialog(final View parentView, View view, final Object values) {
		DialogTips dialog = new DialogTips(this, "ȷ���ط�����Ϣ", "ȷ��", "ȡ��", "��ʾ",
				true);
		// ���óɹ��¼�
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int userId) {
				if (((BmobMsg) values).getMsgType() == BmobConfig.TYPE_IMAGE
						|| ((BmobMsg) values).getMsgType() == BmobConfig.TYPE_VOICE) {// ͼƬ���������͵Ĳ���
					resendFileMsg(parentView, values);
				} else {
					resendTextMsg(parentView, values);
				}
				dialogInterface.dismiss();
			}
		});
		// ��ʾȷ�϶Ի���
		dialog.show();
		dialog = null;
	}
	/**
	 * �ط��ı���Ϣ
	 * @param parentView
	 * @param values
	 */
	protected void resendTextMsg(final View parentView, final Object values) {

		BmobChatManager.getInstance(ChatActivity.this).resendTextMessage(
				targetUser, (BmobMsg) values, new PushListener() {

					@Override
					public void onSuccess() {
						showLog("���ͳɹ�");
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_SUCCESS);
						parentView.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentView.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.INVISIBLE);
						parentView.findViewById(R.id.tv_send_status)
								.setVisibility(View.VISIBLE);
						((TextView) parentView.findViewById(R.id.tv_send_status))
								.setText("�ѷ���");
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						showLog("����ʧ��:" + arg1);
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_FAIL);
						parentView.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentView.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.VISIBLE);
						parentView.findViewById(R.id.tv_send_status)
								.setVisibility(View.INVISIBLE);
					}
				});
		mAdapter.notifyDataSetChanged();
	
	}
	/**
	 * �ط�ͼƬ��Ϣ
	 * @param parentView
	 * @param values
	 */
	protected void resendFileMsg(final View parentView, final Object values) {

		BmobChatManager.getInstance(ChatActivity.this).resendFileMessage(
				targetUser, (BmobMsg) values, new UploadListener() {

					@Override
					public void onStart(BmobMsg msg) {
					}

					@Override
					public void onSuccess() {
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_SUCCESS);
						parentView.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentView.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.INVISIBLE);
						if (((BmobMsg) values).getMsgType() == BmobConfig.TYPE_VOICE) {
							parentView.findViewById(R.id.tv_send_status)
									.setVisibility(View.GONE);
							parentView.findViewById(R.id.tv_voice_length)
									.setVisibility(View.VISIBLE);
						} else {
							parentView.findViewById(R.id.tv_send_status)
									.setVisibility(View.VISIBLE);
							((TextView) parentView
									.findViewById(R.id.tv_send_status))
									.setText("�ѷ���");
						}
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_FAIL);
						parentView.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentView.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.VISIBLE);
						parentView.findViewById(R.id.tv_send_status)
								.setVisibility(View.INVISIBLE);
					}
				});
		mAdapter.notifyDataSetChanged();
	
	}

	/**
	 * ����ˢ��
	 */
	private void initOrRefresh() {
		if(mAdapter != null) {
			if(MyMessageReceiver.mNewNum != 0) { //���ڸ����������������ʱ������Ϣ����ʱ�ٻص�����ҳ���ʱ����Ҫ��ʾ��������Ϣ
				//�п��������ڼ䣬����N����Ϣ,�����Ҫ������ʾ�ڽ�����
				int news = MyMessageReceiver.mNewNum;
				int size = initMsgData().size();
				for(int i = (news-1);i>= 0;i--) {
					mAdapter.add(initMsgData().get(size-(i+1)));  //������һ����Ϣ��������ʾ
				}
				mListView.setSelection(mAdapter.getCount()-1);
			} else {
				mAdapter.notifyDataSetChanged();
			}
		} else {
			mAdapter = new MessageChatAdapter(ChatActivity.this, initMsgData());
			mListView.setAdapter(mAdapter);
		}
	}
	
	/**
	 * ������Ϣ��ʷ�������ݿ��ж���
	 */
	private List<BmobMsg> initMsgData() {
		List<BmobMsg> list = BmobDB.create(this).queryMessages(targetId,MsgPagerNum);
		return list;
	}

	private void initBoottomView() {
		//�����
		btn_chat_add = (Button) findViewById(R.id.btn_chat_add);
		btn_chat_emo = (Button) findViewById(R.id.btn_chat_emo);
		btn_chat_add.setOnClickListener(this);
		btn_chat_emo.setOnClickListener(this);
		//���ұ�
		btn_chat_keyboard = (Button) findViewById(R.id.btn_chat_keyboard);
		btn_chat_voice = (Button) findViewById(R.id.btn_chat_voice);
		btn_chat_voice.setOnClickListener(this);
		btn_chat_keyboard.setOnClickListener(this);
		btn_chat_send = (Button) findViewById(R.id.btn_chat_send);
		btn_chat_send.setOnClickListener(this);
		layout_more = (LinearLayout) findViewById(R.id.layout_more);
		layout_add = (LinearLayout) findViewById(R.id.layout_add);
		layout_emo = (LinearLayout) findViewById(R.id.layout_emo);
		
		initAddView();
		initEmoView();
		
		btn_speak = (Button) findViewById(R.id.btn_speak);
		edit_user_comment = (EmotionsEditText) findViewById(R.id.edit_user_comment);
		edit_user_comment.setOnClickListener(this);
		
		edit_user_comment.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(s)) {
					btn_chat_send.setVisibility(View.VISIBLE);
					btn_chat_keyboard.setVisibility(View.GONE);
					btn_chat_voice.setVisibility(View.GONE);
				} else {
					if (btn_chat_voice.getVisibility() != View.VISIBLE) {
						btn_chat_voice.setVisibility(View.VISIBLE);
						btn_chat_send.setVisibility(View.GONE);
						btn_chat_keyboard.setVisibility(View.GONE);
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
	}
	
	List<FaceText> emos;
	private Button btn_speak;
	private EmotionsEditText edit_user_comment;
	private RelativeLayout layout_record;
	private TextView tv_voice_tips;
	private ImageView iv_record;
	private Drawable[] drawable_Anims;  //��Ͳ����
	/**
	 * ��ʼ�����鲼��
	 */
	private void initEmoView() {
		pager_emo = (ViewPager) findViewById(R.id.pager_emo);
		emos = FaceTextUtils.faceTexts;
		List<View> views = new ArrayList<View>();
		for(int i = 0;i < 2;i++) {
			views.add(getGridView(i));
		}
		pager_emo.setAdapter(new EmoViewPagerAdapter(views));
	}

	private View getGridView(int i) {
		View view = View.inflate(this, R.layout.include_emo_gridview, null);
//		LayoutInflater.from(ChatActivity.this).inflate(R.layout.include_emo_gridview, null);
		GridView gridview = (GridView)view.findViewById(R.id.gridview);
		List<FaceText> list = new ArrayList<FaceText>();
		if(i == 0) {
			list.addAll(emos.subList(0, 21));
		} else if(i == 1) {
			list.addAll(emos.subList(21, emos.size()));
		}
		final EmoteAdapter gridAdapter = new EmoteAdapter(ChatActivity.this, list);
		gridview.setAdapter(gridAdapter);
		
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				FaceText name = (FaceText) gridAdapter.getItem(position);
				String key = name.text.toString();
				try {
					if(edit_user_comment != null && !TextUtils.isEmpty(key)) {
						int start = edit_user_comment.getSelectionStart();
						CharSequence content = edit_user_comment.getText().insert(start, key);
						edit_user_comment.setText(content);
						//��λ���λ��
						CharSequence info = edit_user_comment.getText();
						if(info instanceof Spannable) {
							Spannable spanText = (Spannable) info;
							Selection.setSelection(spanText, start+key.length());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return view;
	}

	private void initAddView() {
		tv_picture = (TextView) findViewById(R.id.tv_picture);
		tv_camera = (TextView) findViewById(R.id.tv_camera);
		tv_location = (TextView) findViewById(R.id.tv_location);
		tv_picture.setOnClickListener(this);
		tv_location.setOnClickListener(this);
		tv_camera.setOnClickListener(this);
	}

	private void initNewMessageBroadCast() {
		//ע�������Ϣ�㲥
		receiver = new NewBroadcastReceiver(); 
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
		//���ù㲥�����ȼ�����MainActivity,���������Ϣ����ʱ�����������chatҳ�棬ֱ����ʾ��Ϣ����������ʾ��Ϣδ��
		intentFilter.setPriority(5);
		registerReceiver(receiver, intentFilter);
	}
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
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.edit_user_comment: //����ı������
			mListView.setSelection(mListView.getCount() - 1);
			if (layout_more.getVisibility() == View.VISIBLE) {
				layout_add.setVisibility(View.GONE);
				layout_emo.setVisibility(View.GONE);
				layout_more.setVisibility(View.GONE);
			}
			break;
		case R.id.btn_chat_emo: //���Ц��ͼ��
			if (layout_more.getVisibility() == View.GONE) {
				showEditState(true);
			} else {
				if (layout_add.getVisibility() == View.VISIBLE) {
					layout_add.setVisibility(View.GONE);
					layout_emo.setVisibility(View.VISIBLE);
				} else {
					layout_more.setVisibility(View.GONE);
				}
			}
			break;
		case R.id.btn_chat_add:// ��Ӱ�ť-��ʾͼƬ�����ա�λ��
			if (layout_more.getVisibility() == View.GONE) {
				layout_more.setVisibility(View.VISIBLE);
				layout_add.setVisibility(View.VISIBLE);
				layout_emo.setVisibility(View.GONE);
				hideInputSoftView();
			} else {
				if (layout_emo.getVisibility() == View.VISIBLE) {
					layout_emo.setVisibility(View.GONE);
					layout_add.setVisibility(View.VISIBLE);
				} else {
					layout_more.setVisibility(View.GONE);
				}
			}

			break;
		case R.id.btn_chat_voice:// ������ť
			edit_user_comment.setVisibility(View.GONE);
			layout_more.setVisibility(View.GONE);
			btn_chat_voice.setVisibility(View.GONE);
			btn_chat_keyboard.setVisibility(View.VISIBLE);
			btn_speak.setVisibility(View.VISIBLE);
			hideInputSoftView();
			break;
		case R.id.btn_chat_keyboard:// ���̰�ť������͵������̲����ص�������ť
			showEditState(false);
			break;
		case R.id.btn_chat_send:// �����ı�
			final String msg = edit_user_comment.getText().toString();
			if (msg.equals("")) {
				showToast("�����뷢����Ϣ!");
				return;
			}
			boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
			if (!isNetConnected) {
				showToast(R.string.network_tips);
				// return;
			}
			// ��װBmobMessage����
			BmobMsg message = BmobMsg.createTextSendMsg(this, targetId, msg);
			message.setExtra("Bmob");
			// Ĭ�Ϸ�����ɣ������ݱ��浽������Ϣ�������Ự����
			manager.sendTextMessage(targetUser, message);
			// ˢ�½���
			refreshMessage(message);

			break;
		case R.id.tv_camera:// ����
			selectImageFromCamera();
			break;
		case R.id.tv_picture:// ͼƬ
			selectImageFromLocal();
			break;
		case R.id.tv_location:// λ��
			selectLocationFromMap();
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BmobConstants.REQUESTCODE_TAKE_CAMERA:// ��ȡ��ֵ��ʱ����ϴ�path·���µ�ͼƬ��������
				showLog("����ͼƬ�ĵ�ַ��" + localCameraPath);
				sendImageMessage(localCameraPath);
				break;
			case BmobConstants.REQUESTCODE_TAKE_LOCAL:
				if (data != null) {
					Uri selectedImage = data.getData();
					if (selectedImage != null) {
						Cursor cursor = getContentResolver().query(
								selectedImage, null, null, null, null);
						cursor.moveToFirst();
						int columnIndex = cursor.getColumnIndex("_data");
						String localSelectPath = cursor.getString(columnIndex);
						cursor.close();
						if (localSelectPath == null
								|| localSelectPath.equals("null")) {
							showToast("�Ҳ�������Ҫ��ͼƬ");
							return;
						}
						sendImageMessage(localSelectPath);
					}
				}
				break;
			case BmobConstants.REQUESTCODE_TAKE_LOCATION:// ����λ��
				double latitude = data.getDoubleExtra("x", 0);// ά��
				double longtitude = data.getDoubleExtra("y", 0);// ����
				String address = data.getStringExtra("address");
				if (address != null && !address.equals("")) {
					sendLocationMessage(address, latitude, longtitude);
				} else {
					showToast("�޷���ȡ������λ����Ϣ!");
				}

				break;
			}
		}
	}
	private void sendLocationMessage(String address, double latitude,double longtitude) {
		if (layout_more.getVisibility() == View.VISIBLE) {
			layout_more.setVisibility(View.GONE);
			layout_add.setVisibility(View.GONE);
			layout_emo.setVisibility(View.GONE);
		}
		// ��װBmobMessage����
		BmobMsg message = BmobMsg.createLocationSendMsg(this, targetId,
				address, latitude, longtitude);
		// Ĭ�Ϸ�����ɣ������ݱ��浽������Ϣ�������Ự����
		manager.sendTextMessage(targetUser, message);
		// ˢ�½���
		refreshMessage(message);
	}

	/**
	 * Ĭ�����ϴ�����ͼƬ��֮�����ʾ����
	 * @param local
	 */
	private void sendImageMessage(String local) {
		if (layout_more.getVisibility() == View.VISIBLE) {
			layout_more.setVisibility(View.GONE);
			layout_add.setVisibility(View.GONE);
			layout_emo.setVisibility(View.GONE);
		}
		manager.sendImageMessage(targetUser, local, new UploadListener() {

			@Override
			public void onStart(BmobMsg msg) {
				showLog("��ʼ�ϴ�onStart��" + msg.getContent() + ",״̬��"
						+ msg.getStatus());
				refreshMessage(msg);
			}

			@Override
			public void onSuccess() {
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(int error, String arg1) {
				showLog("�ϴ�ʧ�� -->arg1��" + arg1);
				mAdapter.notifyDataSetChanged();
			}
		});
	
	}

	/**
	 * ������ͼ
	 */
	private void selectLocationFromMap() {	
		Intent intent = new Intent(this, LocationActivity.class);
		intent.putExtra("type", "select");
		startActivityForResult(intent, BmobConstants.REQUESTCODE_TAKE_LOCATION);
	}

	private void selectImageFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
		} else {
			intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, BmobConstants.REQUESTCODE_TAKE_LOCAL);
	}

	private String localCameraPath = "";// ���պ�õ���ͼƬ��ַ

	/**
	 * ���������������
	 */
	private void selectImageFromCamera() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File dir = new File(BmobConstants.BMOB_PICTURE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, String.valueOf(System.currentTimeMillis())
				+ ".jpg");
		localCameraPath = file.getPath();
		Uri imageUri = Uri.fromFile(file);
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(openCameraIntent,
				BmobConstants.REQUESTCODE_TAKE_CAMERA);
	
	}

	/**
	 * �����Ƿ���Ц������ʾ�ı����״̬
	 * @param isEmo �����������ֺͱ���
	 */
	private void showEditState(boolean isEmo) {

		edit_user_comment.setVisibility(View.VISIBLE);
		btn_chat_keyboard.setVisibility(View.GONE);
		btn_chat_voice.setVisibility(View.VISIBLE);
		btn_speak.setVisibility(View.GONE);
		edit_user_comment.requestFocus();
		if (isEmo) {
			layout_more.setVisibility(View.VISIBLE);
			layout_more.setVisibility(View.VISIBLE);
			layout_emo.setVisibility(View.VISIBLE);
			layout_add.setVisibility(View.GONE);
			hideInputSoftView();
		} else {
			layout_more.setVisibility(View.GONE);
			showSoftInputView();
		}
	
	}
	
	// ��ʾ�����
	public void showSoftInputView() {
		if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.showSoftInput(edit_user_comment, 0);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//����Ϣ�������ˢ�½���
		initOrRefresh();
		//�������͵���Ϣ
		MyMessageReceiver.ehList.add(this);
		//�п��������ڼ䣬������������֪ͨ������ʱ����Ҫ���֪ͨ�����δ����Ϣ��
		BmobNotifyManager.getInstance(ChatActivity.this).cancelNotify();
		BmobDB.create(this).resetUnread(targetId);
		//�����Ϣδ����-���Ҫ��ˢ��֮��
		MyMessageReceiver.mNewNum=0;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MyMessageReceiver.ehList.remove(this);// �������͵���Ϣ
		// ֹͣ¼��
		if (recordManager.isRecording()) {
			recordManager.cancelRecording();
			layout_record.setVisibility(View.GONE);
		}
		// ֹͣ����¼��
		if (NewRecordPlayClickListener.isPlaying
				&& NewRecordPlayClickListener.currentPlayListener != null) {
			NewRecordPlayClickListener.currentPlayListener.stopPlayRecord();
		}
	}
	
	@Override
	public void onRefresh() {
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				MsgPagerNum ++;
				int total = BmobDB.create(ChatActivity.this).queryChatTotalCount(targetId);
				BmobLog.i("��¼������" + total);
				//��ǰ�ļ�¼��
				int current = mAdapter.getCount();
				if(total<=current) {
					showToast("�����¼��������Ŷ");
				} else {
					List<BmobMsg> msgList = initMsgData();
					mAdapter.setList(msgList);
					mListView.setSelection(mAdapter.getCount()-current-1);
				}
				mListView.stopRefresh();
			}
		}, 1000);
	}

	@Override
	public void onLoadMore() {
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (layout_more.getVisibility() == 0) {
				layout_more.setVisibility(View.GONE);
				return false;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		hideInputSoftView();
		try {
			unregisterReceiver(receiver);
		} catch (Exception e) {
		}
	}

	//���յ���Ϣ
	@Override
	public void onMessage(BmobMsg message) {
		Message handlerMessage = handler.obtainMessage(NEW_MESSAGE);
		handlerMessage.obj = message;
		handler.sendMessage(handlerMessage);
	}

	@Override
	public void onReaded(String conversionId, String msgTime) {
		// �˴�Ӧ�ù��˵����Ǻ͵�ǰ�û�������Ļ�ִ��Ϣ�����ˢ��
		if(conversionId.split("&")[1].equals(targetId)) {
			//�޸Ľ�����ָ����Ϣ���Ķ�״̬
			for(BmobMsg msg : mAdapter.getList()) {
				if(msg.getConversationId().equals(conversionId) && msg.getMsgTime().equals(msgTime)) {
					msg.setStatus(BmobConfig.STATUS_SEND_RECEIVERED);
				}
				mAdapter.notifyDataSetChanged();
			}
		}
		
	}

	@Override
	public void onNetChange(boolean isNetConnected) {
		if(!isNetConnected) {
			showToast(R.string.network_tips);
		}
	}

	@Override
	public void onAddUser(BmobInvitation message) {
		
	}

	@Override
	public void onOffline() {
		showOfflineDialog(this);
	}
}
