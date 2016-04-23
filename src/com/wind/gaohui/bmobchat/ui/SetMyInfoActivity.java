package com.wind.gaohui.bmobchat.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.wind.gaohui.bmobchat.CustomApplication;
import com.wind.gaohui.bmobchat.bean.User;
import com.wind.gaohui.bmobchat.config.BmobConstants;
import com.wind.gaohui.bmobchat.util.CollectionUtils;
import com.wind.gaohui.bmobchat.util.ImageLoadOptions;
import com.wind.gaohui.bmobchat.util.PhotoUtil;
import com.wind.gaohui.bmobchat.view.dialog.DialogTips;
import com.wind.gaohui.bombchat.R;

@SuppressLint({ "InflateParams", "SimpleDateFormat", "ClickableViewAccessibility" })
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class SetMyInfoActivity extends ActivityBase implements OnClickListener {

	String from = "";
	String username = "";
	User user;
	private LinearLayout layout_all;
	private ImageView iv_set_avator;
	private ImageView iv_arraw;
	private ImageView iv_nickarraw;
	private TextView tv_set_name;
	private TextView tv_set_nick;
	private RelativeLayout layout_head;
	private RelativeLayout layout_nick;
	private RelativeLayout layout_gender;
	private RelativeLayout layout_black_tips;
	private TextView tv_set_gender;
	private Button btn_chat;
	private Button btn_black;
	private Button btn_add_friend;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		// ��Ϊ�����ֻ���������������ĵ�����ť����Ҫ�������ص�����Ȼ���ڵ����պ����������ť������setContentView֮ǰ���ò�����Ч
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= 14) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
		setContentView(R.layout.activity_set_info);

		from = getIntent().getStringExtra("from");
		username = getIntent().getStringExtra("username");
		initView();
	}

	private void initView() {
		layout_all = (LinearLayout) findViewById(R.id.layout_all);
		iv_set_avator = (ImageView) findViewById(R.id.iv_set_avator);
		iv_arraw = (ImageView) findViewById(R.id.iv_arraw);
		iv_nickarraw = (ImageView) findViewById(R.id.iv_nickarraw);
		tv_set_name = (TextView) findViewById(R.id.tv_set_name);
		tv_set_nick = (TextView) findViewById(R.id.tv_set_nick);
		layout_head = (RelativeLayout) findViewById(R.id.layout_head);
		layout_nick = (RelativeLayout) findViewById(R.id.layout_nick);
		layout_gender = (RelativeLayout) findViewById(R.id.layout_gender);
		// ��������ʾ
		layout_black_tips = (RelativeLayout) findViewById(R.id.layout_black_tips);
		tv_set_gender = (TextView) findViewById(R.id.tv_set_gender);
		btn_chat = (Button) findViewById(R.id.btn_chat);
		btn_black = (Button) findViewById(R.id.btn_black);
		btn_add_friend = (Button) findViewById(R.id.btn_add_friend);
		btn_add_friend.setEnabled(false);
		btn_chat.setEnabled(false);
		btn_black.setEnabled(false);

		if ("me".equals(from)) {
			initTopBarForLeft("��������");
			layout_head.setOnClickListener(this);
			layout_nick.setOnClickListener(this);
			layout_gender.setOnClickListener(this);
			iv_nickarraw.setVisibility(View.VISIBLE);
			iv_arraw.setVisibility(View.VISIBLE);
			btn_black.setVisibility(View.GONE);
			btn_chat.setVisibility(View.GONE);
			btn_add_friend.setVisibility(View.GONE);
		} else {
			initTopBarForLeft("��ϸ����");
			iv_nickarraw.setVisibility(View.INVISIBLE);
			iv_arraw.setVisibility(View.INVISIBLE);
			// ���ܶԷ��ǲ�����ĺ��ѣ������Է�����Ϣ�޸�
			btn_chat.setVisibility(View.VISIBLE);
			btn_chat.setOnClickListener(this);
			if (from.equals("add")) {// �Ӹ��������б���Ӻ���--��Ϊ��ȡ�������˵ķ����������Ƿ���ʾ���ѵ�����������������Ҫ�ж�������û��Ƿ����Լ��ĺ���
				if (mApplication.getContactList().containsKey(username)) {// �Ǻ���
					btn_black.setVisibility(View.VISIBLE);
					btn_black.setOnClickListener(this);
				} else {
					btn_black.setVisibility(View.GONE);
					btn_add_friend.setVisibility(View.VISIBLE);
					btn_add_friend.setOnClickListener(this);
				}
			} else {// �鿴����
				btn_black.setVisibility(View.VISIBLE);
				btn_black.setOnClickListener(this);
			}
			initOtherData(username);
		}
	}

	/**
	 * TODO ����initOtherData���������
	 * 
	 * @param username
	 */
	private void initOtherData(String username) {
		userManager.queryUser(username, new FindListener<User>() {
			@Override
			public void onError(int arg0, String arg1) {
				showLog("onError:" + arg1);
			}

			@Override
			public void onSuccess(List<User> arg0) {
				if (arg0 != null && arg0.size() > 0) {
					user = arg0.get(0);
					// ��Ȼ��ť���أ�����ֻ�ǿ�����������Ӧ�¼���������Ҫ����ť��Ϊ������ TODO
					btn_chat.setEnabled(true);
					btn_black.setEnabled(true);
					btn_add_friend.setEnabled(true);
					updateUser(user);
				} else {
					showLog("onSuccess ���޴���");
				}
			}
		});
	}

	protected void updateUser(User user) {
		// �����û�ͷ��
		refreshAvatar(user.getAvatar());
		tv_set_name.setText(user.getUsername());
		tv_set_nick.setText(user.getNick());
		tv_set_gender.setText(user.getSex() ? "��" : "Ů");
		if (from.equals("other")) {
			// ����Ƿ�Ϊ�������û�
			if (BmobDB.create(this).isBlackUser(user.getUsername())) {
				btn_black.setVisibility(View.GONE);
				layout_black_tips.setVisibility(View.VISIBLE);
			} else {
				btn_black.setVisibility(View.VISIBLE);
				layout_black_tips.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * �����û���ͷ��
	 * 
	 * @param avatar
	 *            ͷ��ͼƬ��Uri
	 */
	private void refreshAvatar(String avatar) {
		if (avatar != null && !"".equals(avatar)) {
			ImageLoader.getInstance().displayImage(avatar, iv_set_avator,
					ImageLoadOptions.getOptions());
		} else {
			iv_set_avator.setImageResource(R.drawable.default_head);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (from.equals("me")) {
			initMeData();
		}
	}

	private void initMeData() {
		User user = userManager.getCurrentUser(User.class);
		// hight TODO ���ֶα�ʾʲô��˼
		BmobLog.i("hight = " + user.getHight() + ",sex= " + user.getSex());
		initOtherData(user.getUsername());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_chat: // ��������
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra("user", user);
			startAnimActivity(intent);
			finish();
			break;
		case R.id.layout_head: // ��ʾͷ��ѡ��Ի���
			showAvatarPop();
			break;
		case R.id.layout_nick:
			startAnimActivity(UpdateInfoActivity.class);
			break;
		case R.id.layout_gender:// �Ա�
			showSexChooseDialog();
			break;
		case R.id.btn_black:// ���������
			showBlackDialog(user.getUsername());
			break;
		case R.id.btn_add_friend:// ��Ӻ���
			addFriend();
			break;
		}
	}

	/**
	 * ��Ӻ�������
	 */
	private void addFriend() {
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("�������...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();

		// ����TAG����----->��Ӻ���
		BmobChatManager.getInstance(this).sendTagMessage(
				BmobConfig.TAG_ADD_CONTACT, user.getObjectId(),
				new PushListener() {

					@Override
					public void onSuccess() {
						progress.dismiss();
						showToast("��������ɹ����ȴ��Է���֤");
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						progress.dismiss();
						showToast("��������ɹ����ȴ��Է���֤");
						showLog("��������ʧ��:" + arg1);
					}
				});
	}

	/**
	 * ��ʾ��������ʾ�Ի���
	 * 
	 * @param username
	 */
	private void showBlackDialog(final String username) {
		DialogTips dialog = new DialogTips(this, "���������",
				"������������㽫�����յ��Է�����Ϣ��ȷ��Ҫ������", "ȷ��", true, true);
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int userId) {
				// ��ӵ��������б�
				userManager.addBlack(username, new UpdateListener() {
					@Override
					public void onSuccess() {
						showToast("��������ӳɹ�!");
						btn_black.setVisibility(View.GONE);
						layout_black_tips.setVisibility(View.VISIBLE);
						// �����������ڴ��б���ĺ����б�
						CustomApplication.getInstance().setContactList(
								CollectionUtils.list2map(BmobDB.create(
										SetMyInfoActivity.this)
										.getContactList()));
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						showToast("���������ʧ��:" + arg1);
					}
				});
			}
		});
		// ��ʾȷ�϶Ի���
		dialog.show();
		dialog = null;

	}

	String sexs[] = new String[] { "��", "Ů" };

	// �����Ա�ѡ��Ի���
	private void showSexChooseDialog() {
		new AlertDialog.Builder(this)
				.setTitle("��ѡ��")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(sexs, 0,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								BmobLog.i("�������" + sexs[which]);
								updateInfo(which);
								dialog.dismiss();
							}
						}).setNegativeButton("ȡ��", null).show();
	}

	// �����Ա���Ϣ
	protected void updateInfo(int which) {
		final User u = new User();
		if (which == 0) {
			u.setSex(true);
		} else {
			u.setSex(false);
		}
		updateUserData(u, new UpdateListener() {
			@Override
			public void onSuccess() {
				showToast("�޸ĳɹ�");
				tv_set_gender.setText(u.getSex() == true ? "��" : "Ů");
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				showToast("onFailure:" + arg1);
			}
		});
	}

	/**
	 * �����û�����Ϣ
	 * @param u
	 * @param updateListener
	 */
	private void updateUserData(User u, UpdateListener updateListener) {
		User current = userManager.getCurrentUser(User.class);
		u.setObjectId(current.getObjectId());
		u.update(this, updateListener);
	}

	RelativeLayout layout_choose;
	RelativeLayout layout_photo;
	PopupWindow avatarPop;

	public String filePath = "";

	/**
	 * ��ʾͷ��ѡ��ĶԻ���
	 */
	@SuppressWarnings("deprecation")
	private void showAvatarPop() {
		View view = LayoutInflater.from(this).inflate(R.layout.pop_showavator,
				null);
		layout_choose = (RelativeLayout) view.findViewById(R.id.layout_choose);
		layout_photo = (RelativeLayout) view.findViewById(R.id.layout_photo);
		layout_photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLog("�������");
				layout_choose.setBackgroundColor(getResources().getColor(
						R.color.base_color_text_white));
				layout_photo.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.pop_bg_press));
				File dir = new File(BmobConstants.MyAvatarDir);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				// ԭͼ
				File file = new File(dir, new SimpleDateFormat("yyMMddHHmmss")
						.format(new Date()));
				filePath = file.getAbsolutePath();// ��ȡ��Ƭ�ı���·��
				Uri imageUri = Uri.fromFile(file);

				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent,
						BmobConstants.REQUESTCODE_UPLOADAVATAR_CAMERA);
			}
		});

		layout_choose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showLog("������");
				showToast("������");
				layout_photo.setBackgroundColor(getResources().getColor(
						R.color.base_color_text_white));
				layout_choose.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.pop_bg_press));
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent,
						BmobConstants.REQUESTCODE_UPLOADAVATAR_LOCATION);
			}
		});

		avatarPop = new PopupWindow(view, mScreenWidth, 600);
		avatarPop.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					avatarPop.dismiss();
					return true;
				}
				return false;
			}
		});

		avatarPop.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		avatarPop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		avatarPop.setTouchable(true);
		avatarPop.setFocusable(true);
		avatarPop.setOutsideTouchable(true);
		avatarPop.setBackgroundDrawable(new BitmapDrawable());
		// ����Ч�� �ӵײ�����
		avatarPop.setAnimationStyle(R.style.Animations_GrowFromBottom);
		avatarPop.showAtLocation(layout_all, Gravity.BOTTOM, 0, 0);
	}

	Uri uriTempFile = null;
	/**
	 * ʵ��ͼƬ�ü�
	 * @return void
	 * @throws
	 */
	private void startImageAction(Uri uri, int outputX, int outputY,
			int requestCode, boolean isCrop) {
		showToast("startImageAction: called");
		Intent intent = null;
		if (isCrop) {
			intent = new Intent("com.android.camera.action.CROP");
		} else {
			intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		}
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		
		/**
		 * modify ���ﲻʹ�����ַ�ʽ����Ϊmiuiϵͳ���ﲻ�ܷ���data
		 * ����ѡ���������ַ�ʽ���Ƚ�ͼƬ������sdcard���ٴ�sdcard��ȡ����
		 */
//		intent.putExtra("return-data", true);
		
		uriTempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
		/**
		 * modify ͨ���Ƚ�ͼƬ�洢�����Ա���miui���ܴ�ϵͳ�ü���ȡ��data
		 */
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTempFile);
		
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, requestCode);
	}
	
	Bitmap newBitmap;
	boolean isFromCamera = false;// ����������ת
	int degree = 0;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case BmobConstants.REQUESTCODE_UPLOADAVATAR_CAMERA:// �����޸�ͷ��
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					showToast("SD������");
					return;
				}
				isFromCamera = true;
				File file = new File(filePath);
				degree = PhotoUtil.readPictureDegree(file.getAbsolutePath());
				Log.i("life", "���պ�ĽǶȣ�" + degree);
				startImageAction(Uri.fromFile(file), 200, 200,
						BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP, true);
			}
			break;
		case BmobConstants.REQUESTCODE_UPLOADAVATAR_LOCATION:// �����޸�ͷ��
			showToast("onActivityResult :�����ϴ�");
			if (avatarPop != null) {
				avatarPop.dismiss();
			}
			Uri uri = null;
			if (data == null) {
				showToast("onActivityResult :data" + data);
				return;
			}
			if (resultCode == RESULT_OK) {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					showToast("SD������");
					return;
				}
				isFromCamera = false;
				uri = data.getData();
				showToast("onActivityResult : success uri:" + uri);
				startImageAction(uri, 200, 200,
						BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP, true);
			} else {
				showToast("��Ƭ��ȡʧ��");
			}

			break;
		case BmobConstants.REQUESTCODE_UPLOADAVATAR_CROP:// �ü�ͷ�񷵻�
			showToast("�ü�ͷ�񷵻�");
			// TODO sent to crop
			if (avatarPop != null) {
				avatarPop.dismiss();
			}
			/**
			 * modify ���miui�ü�ͼƬ������
			 */
//			if (data == null) {
//				showToast("data :" + data);
//				// Toast.makeText(this, "ȡ��ѡ��", Toast.LENGTH_SHORT).show();
//				return;
//			} else {
//			}
			
			try {
				saveCropAvator(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// ��ʼ���ļ�·��
			filePath = "";
			// �ϴ�ͷ��
			uploadAvatar();
			break;
		}
	}
	
	private void updateUserAvatar(final String url) {
		User  u =new User();
		u.setAvatar(url);
		updateUserData(u,new UpdateListener() {
			@Override
			public void onSuccess() {
				showToast("ͷ����³ɹ���");
				// ����ͷ��
				refreshAvatar(url);
			}
			@Override
			public void onFailure(int code, String msg) {
				showToast("ͷ�����ʧ�ܣ�" + msg);
			}
		});
	}
	
	String path;
	
	private void uploadAvatar() {
		BmobLog.i("ͷ���ַ��" + path);
		final BmobFile bmobFile = new BmobFile(new File(path));
		bmobFile.upload(this, new UploadFileListener() {

			@Override
			public void onSuccess() {
				String url = bmobFile.getFileUrl(SetMyInfoActivity.this);
				showToast("uploadAvatar: url:" + url);
				// ����BmobUser����
				updateUserAvatar(url);
			}

			@Override
			public void onProgress(Integer arg0) {

			}

			@Override
			public void onFailure(int arg0, String msg) {
				showToast("ͷ���ϴ�ʧ�ܣ�" + msg);
			}
		});
	}
	
	/**
	 * ����ü���ͷ��
	 * @param data
	 * @throws FileNotFoundException 
	 */
	@SuppressLint("SimpleDateFormat")
	private void saveCropAvator(Intent data) throws FileNotFoundException {
		/**
		 * modify ����ͼƬ��ʱ�򣬴�sdcard��ȡ��ͼƬ
		 */
		Bitmap bitmap = null;
		if(data == null) {
			bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriTempFile));
		} else {
			Bundle extras = data.getExtras();
			if (extras != null) {
				bitmap = extras.getParcelable("data");
			}
		}
		
		showToast("saveCropAvator bitmap:" + bitmap);
		if (bitmap != null) {
			bitmap = PhotoUtil.toRoundCorner(bitmap, 10);
			if (isFromCamera && degree != 0) {
				bitmap = PhotoUtil.rotaingImageView(degree, bitmap);
			}
			iv_set_avator.setImageBitmap(bitmap);
			// ����ͼƬ
			String filename = new SimpleDateFormat("yyMMddHHmmss")
					.format(new Date())+".png";
			showToast("����ͼƬ�� filename:" + filename);
			path = BmobConstants.MyAvatarDir + filename;
			PhotoUtil.saveBitmap(BmobConstants.MyAvatarDir, filename,
					bitmap, true);
			// �ϴ�ͷ��
			if (bitmap != null && bitmap.isRecycled()) {
				bitmap.recycle();
			}
		}
	}
}
