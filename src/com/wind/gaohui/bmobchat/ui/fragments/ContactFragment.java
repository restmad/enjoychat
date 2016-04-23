package com.wind.gaohui.bmobchat.ui.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.UpdateListener;

import com.wind.gaohui.bmobchat.CustomApplication;
import com.wind.gaohui.bmobchat.adapter.UserFriendAdapter;
import com.wind.gaohui.bmobchat.bean.User;
import com.wind.gaohui.bmobchat.ui.AddFriendActivity;
import com.wind.gaohui.bmobchat.ui.FragmentBase;
import com.wind.gaohui.bmobchat.ui.NearPeopleActivity;
import com.wind.gaohui.bmobchat.ui.NewFriendActivity;
import com.wind.gaohui.bmobchat.ui.SetMyInfoActivity;
import com.wind.gaohui.bmobchat.util.CharacterParser;
import com.wind.gaohui.bmobchat.util.CollectionUtils;
import com.wind.gaohui.bmobchat.util.PinyinComparator;
import com.wind.gaohui.bmobchat.view.ClearEditText;
import com.wind.gaohui.bmobchat.view.HeaderLayout.onRightImageButtonClickListener;
import com.wind.gaohui.bmobchat.view.MyLetterView;
import com.wind.gaohui.bmobchat.view.MyLetterView.OnTouchingLetterChangedListener;
import com.wind.gaohui.bmobchat.view.dialog.DialogTips;
import com.wind.gaohui.bombchat.R;

/**
 * 联系人页面
 * 
 * @author gaohui
 * @data 2016年4月16日16:33:48
 */
public class ContactFragment extends FragmentBase implements
		OnItemClickListener, OnItemLongClickListener {

	private InputMethodManager inputMethodManager;
	/**
	 * 汉字转化成拼音的类
	 */
	private CharacterParser characterParser;
	/**
	 * 根据拼音来排列ListView中的数据
	 */
	private PinyinComparator pinyinComparator;
	private ListView list_friends;
	private ImageView iv_msg_tips;
	private LinearLayout layout_new; // 新朋友
	private LinearLayout layout_near; // 附近的人
	/**
	 * 好友adapter
	 */
	private UserFriendAdapter userAdapter;
	private List<User> friends = new ArrayList<User>();
	private MyLetterView right_letter;
	private TextView dialog;
	private ClearEditText mClearEditText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contacts, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		init();
	}

	private void init() {
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		initTopBarForRight("联系人", R.drawable.base_action_bar_add_bg_selector,
				new onRightImageButtonClickListener() {
					@Override
					public void onClick() {
						startAnimActivity(AddFriendActivity.class);
					}
				});
		initListView();
		initRightLetterView();
		initEditText();
	}

	private void initEditText() {
		mClearEditText = (ClearEditText) findViewById(R.id.et_msg_search);
		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
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

	/**
	 * 根据输入框中的值来过滤数据并更新listview
	 * 
	 * @param string
	 */
	protected void filterData(String fiterStr) {
		List<User> filterDataList = new ArrayList<User>();
		if (TextUtils.isEmpty(fiterStr)) {
			filterDataList = friends;
		} else {
			filterDataList.clear();
			for (User sortModel : friends) {
				String name = sortModel.getUsername();
				if (name != null) {
					if (name.indexOf(fiterStr.toString()) != -1
							|| characterParser.getSelling(name).startsWith(
									fiterStr.toString())) {
						filterDataList.add(sortModel);
					}
				}
			}
		}
		// 根据A-Z排序
		Collections.sort(filterDataList, pinyinComparator);
		userAdapter.updateListView(filterDataList);
	};

	private void initRightLetterView() {
		right_letter = (MyLetterView) findViewById(R.id.right_letter);
		dialog = (TextView) findViewById(R.id.dialog);
		right_letter.setTextDialog(dialog);
		right_letter
				.setOnTouchingLetterChangedListener(new LetterListViewListener());
	}

	private class LetterListViewListener implements
			OnTouchingLetterChangedListener {
		@Override
		public void onTouchingLetterChanged(String s) {
			// 该字母首次出现的位置
			int position = userAdapter.getPositionForSection(s.charAt(0));
			if (position != -1) {
				list_friends.setSelection(position);
			}
		}
	}

	@SuppressLint("InflateParams")
	private void initListView() {
		list_friends = (ListView) findViewById(R.id.list_friends);
		RelativeLayout headView = (RelativeLayout) mInflater.inflate(
				R.layout.include_new_friend, null);
		iv_msg_tips = (ImageView) headView.findViewById(R.id.iv_msg_tips);
		layout_new = (LinearLayout) headView.findViewById(R.id.layout_new);
		layout_near = (LinearLayout) headView.findViewById(R.id.layout_near);

		layout_new.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						NewFriendActivity.class);
				intent.putExtra("from", "contact");
				startAnimActivity(intent);
			}
		});

		layout_near.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						NearPeopleActivity.class);
				startAnimActivity(intent);
			}
		});

		list_friends.addHeaderView(headView);
		userAdapter = new UserFriendAdapter(getActivity(), friends);
		list_friends.setAdapter(userAdapter);

		list_friends.setOnItemClickListener(this);
		list_friends.setOnItemLongClickListener(this);

		list_friends.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 隐藏软键盘
				if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getActivity().getCurrentFocus() != null) {
						inputMethodManager.hideSoftInputFromWindow(
								getActivity().getWindow().getCurrentFocus()
										.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
				return false;
			}
		});
	}

	private boolean hidden;

	/**
	 * 这样在fragment隐藏或者显示的时候，都能够获取到共享的数据 Called when the hidden state (as
	 * returned by isHidden() of the fragment has changed
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			// 相当于Fragment中onPause TODO对于这里的理解
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			refresh();
		}
	}
	/**
	 * 刷新
	 */
	public void refresh() {
		try {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					queryMyfriends();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取好友列表
	 */
	protected void queryMyfriends() {
		// 是否有新的好友请求
		if (BmobDB.create(getActivity()).hasNewInvite()) {
			iv_msg_tips.setVisibility(View.VISIBLE);
		} else {
			iv_msg_tips.setVisibility(View.GONE);
		}
		// 在这里再做一次本地的好友数据库的检查，是为了本地好友数据库中已经添加了对方，但是界面却没有显示出来的问题
		// 重新设置下内存中保存的好友列表
		CustomApplication.getInstance().setContactList(
				CollectionUtils.list2map(BmobDB.create(getActivity())
						.getContactList()));

		Map<String, BmobChatUser> users = CustomApplication.getInstance()
				.getContactList();
		// 组装新的User
		filledData(CollectionUtils.map2list(users));
		if (userAdapter == null) {
			userAdapter = new UserFriendAdapter(getActivity(), friends);
			list_friends.setAdapter(userAdapter);
		} else {
			userAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param datass
	 */
	@SuppressLint("DefaultLocale")
	private void filledData(List<BmobChatUser> datas) {
		friends.clear();
		int total = datas.size();
		for (int i = 0; i < total; i++) {
			BmobChatUser user = datas.get(i);
			User sortModel = new User();
			sortModel.setAvatar(user.getAvatar());
			sortModel.setNick(user.getNick());
			sortModel.setUsername(user.getUsername());
			sortModel.setObjectId(user.getObjectId());
			sortModel.setContacts(user.getContacts());
			// 汉字转换成拼音
			String username = sortModel.getUsername();
			// 若没有username
			if (username != null) {
				String pinyin = characterParser.getSelling(sortModel
						.getUsername());
				String sortString = pinyin.substring(0, 1).toUpperCase();
				// 正则表达式，判断首字母是否是英文字母
				if (sortString.matches("[A-Z]")) {
					sortModel.setSortLetters(sortString.toUpperCase());
				} else {
					sortModel.setSortLetters("#");
				}
			} else {
				sortModel.setSortLetters("#");
			}
			friends.add(sortModel);
		}
		Collections.sort(friends, pinyinComparator);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		User user = (User) userAdapter.getItem(position - 1);
		showDeleteDialog(user);
		return true;
	}

	/**
	 * 显示删除好友的对话框
	 * 
	 * @param user
	 */
	private void showDeleteDialog(final User user) {
		DialogTips dialog = new DialogTips(getActivity(), user.getUsername(),
				"删除联系人", "确定", true, true);
		// 设置成功事件
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int userId) {
				deleteContact(user);
			}
		});
		// 显示确认对话框
		dialog.show();
		dialog = null;
	}

	// 删除好友
	protected void deleteContact(final User user) {

		final ProgressDialog progress = new ProgressDialog(getActivity());
		progress.setMessage("正在删除...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		userManager.deleteContact(user.getObjectId(), new UpdateListener() {

			@Override
			public void onSuccess() {
				showToast("删除成功");
				// 删除内存
				CustomApplication.getInstance().getContactList()
						.remove(user.getUsername());
				// 更新界面
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						progress.dismiss();
						userAdapter.remove(user);
					}
				});
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				progress.dismiss();
			}
		});

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		User user = (User) userAdapter.getItem(position - 1);
		// 先进入好友的详细资料页面
		Intent intent = new Intent(getActivity(), SetMyInfoActivity.class);
		intent.putExtra("from", "other");
		intent.putExtra("username", user.getUsername());
		startAnimActivity(intent);
	}
}
