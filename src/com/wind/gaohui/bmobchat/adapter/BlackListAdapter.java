package com.wind.gaohui.bmobchat.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.bean.BmobChatUser;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.wind.gaohui.bmobchat.adapter.base.BaseListAdapter;
import com.wind.gaohui.bmobchat.util.ImageLoadOptions;
import com.wind.gaohui.bombchat.R;

/**
 * Activity for ºÚÃûµ¥
 * @author gaohui
 *
 */
@SuppressLint("InflateParams")
public class BlackListAdapter extends BaseListAdapter<BmobChatUser> {

	public BlackListAdapter(Context context, List<BmobChatUser> blackList) {
		super(context, blackList);
	}

	@Override
	public View bindView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
			convertView = mLayoutInflater.inflate(R.layout.item_blacklist, null);
		
		BmobChatUser contact = getList().get(position);
		ImageView iv_firend_avatar = (ImageView) convertView.findViewById(R.id.img_friend_avatar);
		TextView tv_friend_name = (TextView) convertView.findViewById(R.id.tv_friend_name);
		String avatar = contact.getAvatar();
		if(avatar != null && !"".equals(avatar)) {
			ImageLoader.getInstance().displayImage(avatar, iv_firend_avatar, ImageLoadOptions.getOptions());
		} else {
			iv_firend_avatar.setImageResource(R.drawable.default_head);
		}
		tv_friend_name.setText(contact.getUsername());
		return convertView;
	}

}
