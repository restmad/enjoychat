package com.wind.gaohui.bmobchat.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.UpdateListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.wind.gaohui.bmobchat.CustomApplication;
import com.wind.gaohui.bmobchat.adapter.base.BaseListAdapter;
import com.wind.gaohui.bmobchat.adapter.base.ViewHolder;
import com.wind.gaohui.bmobchat.util.CollectionUtils;
import com.wind.gaohui.bmobchat.util.ImageLoadOptions;
import com.wind.gaohui.bombchat.R;

/**
 * �µĺ�������
 * @author gaohui
 */
@SuppressLint("InflateParams")
public class NewFriendAdapter extends BaseListAdapter<BmobInvitation> {

	public NewFriendAdapter(Context context, List<BmobInvitation> list) {
		super(context, list);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View bindView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.item_add_friend,null);
			final BmobInvitation msg = getList().get(position);
			TextView name = ViewHolder.get(convertView, R.id.name);
			ImageView iv_avatar = ViewHolder.get(convertView, R.id.avatar);

			final Button btn_add = ViewHolder.get(convertView, R.id.btn_add);

			String avatar = msg.getAvatar();
			if (avatar != null && !avatar.equals("")) {
				ImageLoader.getInstance().displayImage(avatar, iv_avatar, ImageLoadOptions.getOptions());
			} else {
				iv_avatar.setImageResource(R.drawable.default_head);
			}
			int status = msg.getStatus();
			if(status == BmobConfig.INVITE_ADD_NO_VALIDATION || status == BmobConfig.INVITE_ADD_NO_VALI_RECEIVED) {
				//******������Ӻ�������ʱ���״̬��δ��֤�����յ��Է��ĺ������󣬵�δ����*******
				btn_add.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						BmobLog.i("���ͬ�ⰴť:"+msg.getFromid());
						agressAdd(btn_add, msg);
					}
				});
			} else if(status == BmobConfig.INVITE_ADD_AGREE){
				//******��ͬ����Ӻ���********
				btn_add.setText("��ͬ��");
				btn_add.setBackgroundDrawable(null);
				btn_add.setTextColor(mContext.getResources().getColor(R.color.base_color_text_black));
				btn_add.setEnabled(false);
			}
			name.setText(msg.getFromname());
		}
		return convertView;
	}
	/**
	 * ͬ����Ӻ���
	 * @param btn_add
	 * @param msg
	 */
	protected void agressAdd(final Button btn_add, BmobInvitation msg) {
		final ProgressDialog progress = new ProgressDialog(mContext);
		progress.setMessage("�������...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		try {
			//ͬ����Ӻ���
			BmobUserManager.getInstance(mContext).agreeAddContact(msg, new UpdateListener() {
				
				@SuppressWarnings("deprecation")
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					progress.dismiss();
					btn_add.setText("��ͬ��");
					btn_add.setBackgroundDrawable(null);
					btn_add.setTextColor(mContext.getResources().getColor(R.color.base_color_text_black));
					btn_add.setEnabled(false);
					//���浽application�з���Ƚ�
					CustomApplication.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(mContext).getContactList()));	
				}
				
				@Override
				public void onFailure(int arg0, final String arg1) {
					progress.dismiss();
					showToast("���ʧ��: " +arg1);
				}
			});
		} catch (final Exception e) {
			progress.dismiss();
			showToast("���ʧ��: " +e.getMessage());
		}
	}

}
