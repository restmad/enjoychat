package com.wind.gaohui.bmobchat.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.wind.gaohui.bmobchat.bean.User;
import com.wind.gaohui.bmobchat.util.ImageLoadOptions;
import com.wind.gaohui.bombchat.R;

public class UserFriendAdapter extends BaseAdapter {
	List<User> datas;
	Context ct;

	public UserFriendAdapter(Context ct, List<User> datas) {
		this.datas = datas;
		this.ct = ct;
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(ct).inflate(
					R.layout.item_user_friend, null);

			viewHolder = new ViewHolder();
			viewHolder = new ViewHolder();
			viewHolder.alpha = (TextView) convertView.findViewById(R.id.alpha);
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.tv_friend_name);
			viewHolder.avatar = (ImageView) convertView
					.findViewById(R.id.img_friend_avatar);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		User friend = datas.get(position);
		final String name = friend.getUsername();
		final String avatar = friend.getAvatar();

		if (!TextUtils.isEmpty(avatar)) {
			ImageLoader.getInstance().displayImage(avatar, viewHolder.avatar, ImageLoadOptions.getOptions());
		} else {
			viewHolder.avatar.setImageDrawable(ct.getResources().getDrawable(R.drawable.head));
		}
		viewHolder.name.setText(name);

		// ����position��ȡ���������ĸ��Char asciiֵ
		int section = getSectionForPosition(position);
		// �����ǰλ�õ��ڸ÷�������ĸ��Char��λ�� ������Ϊ�ǵ�һ�γ���
		if (position == getPositionForSection(section)) {
			viewHolder.alpha.setVisibility(View.VISIBLE);
			viewHolder.alpha.setText(friend.getSortLetters());
		} else {
			viewHolder.alpha.setVisibility(View.GONE);
		}

		return convertView;
	}
	/**
	 * ����ListView�ĵ�ǰλ�û�ȡ���������ĸ��Char asciiֵ
	 * @param position
	 * @return
	 */
	private int getSectionForPosition(int position) {
		return datas.get(position).getSortLetters().charAt(0);
	}

	static class ViewHolder {
		TextView alpha;
		ImageView avatar;
		TextView name;
	}

	/**
	 * ���ݷ��������ĸ��Char asciiֵ��ȡ���һ�γ��ָ�����ĸ��λ��
	 * 
	 * @param section
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = datas.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (section == firstChar) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * ��ListView�ķ����ı�ʱ���ø÷������и���
	 * 
	 * @param filterDataList
	 */
	public void updateListView(List<User> list) {
		this.datas = list;
		notifyDataSetChanged();
	}

	public void remove(User user) {
		this.datas.remove(user);
		notifyDataSetChanged();
	}

}
