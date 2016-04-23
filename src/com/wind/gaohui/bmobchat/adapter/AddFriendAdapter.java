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
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.v3.listener.PushListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.wind.gaohui.bmobchat.adapter.base.BaseListAdapter;
import com.wind.gaohui.bmobchat.adapter.base.ViewHolder;
import com.wind.gaohui.bmobchat.util.ImageLoadOptions;
import com.wind.gaohui.bombchat.R;

@SuppressLint("InflateParams")
public class AddFriendAdapter extends BaseListAdapter<BmobChatUser> {

	public AddFriendAdapter(Context context, List<BmobChatUser> list) {
		super(context, list);
	}

	@Override
	public View bindView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.item_add_friend,null);
		}

		final BmobChatUser contact = getList().get(position);
		TextView name = ViewHolder.get(convertView, R.id.name);
		ImageView iv_avatar = ViewHolder.get(convertView, R.id.avatar);
		Button btn_add = ViewHolder.get(convertView, R.id.btn_add);
		String avatar = contact.getAvatar();
		if (avatar != null && !avatar.equals("")) {
			ImageLoader.getInstance().displayImage(avatar, iv_avatar,
					ImageLoadOptions.getOptions());
		} else {
			iv_avatar.setImageResource(R.drawable.default_head);
		}

		name.setText(contact.getUsername());
		btn_add.setText("添加");
		btn_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final ProgressDialog progressDialog = new ProgressDialog(mContext);
				progressDialog.setMessage("正在添加...");
				progressDialog.setCanceledOnTouchOutside(false);
				progressDialog.show();

				BmobChatManager.getInstance(mContext).sendTagMessage(
						BmobConfig.TAG_ADD_CONTACT, contact.getObjectId(),
						new PushListener() {

							@Override
							public void onSuccess() {
								progressDialog.dismiss();
								showToast("发送请求成功，等待对方验证");
							}

							@Override
							public void onFailure(int arg0, String arg1) {
								progressDialog.dismiss();
								showToast("发送请求失败，请重新添加");
								showLog("发送请求失败:"+arg1);
							}
						});
			}
		});
		return convertView;
	}

}
