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
 * ��ϵ��ҳ��
 * 
 * @author gaohui
 * @data 2016��4��16��16:33:48
 */
public class ContactFragment extends FragmentBase implements
		OnItemClickListener, OnItemLongClickListener {

	private InputMethodManager inputMethodManager;
	/**
	 * ����ת����ƴ������
	 */
	private CharacterParser characterParser;
	/**
	 * ����ƴ��������ListView�е�����
	 */
	private PinyinComparator pinyinComparator;
	private ListView list_friends;
	private ImageView iv_msg_tips;
	private LinearLayout layout_new; // ������
	private LinearLayout layout_near; // ��������
	/**
	 * ����adapter
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
		initTopBarForRight("��ϵ��", R.drawable.base_action_bar_add_bg_selector,
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
				// ������������ֵΪ�գ�����Ϊԭ�����б�����Ϊ���������б�
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
	 * ����������е�ֵ���������ݲ�����listview
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
		// ����A-Z����
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
			// ����ĸ�״γ��ֵ�λ��
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
				// ���������
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
	 * ������fragment���ػ�����ʾ��ʱ�򣬶��ܹ���ȡ����������� Called when the hidden state (as
	 * returned by isHidden() of the fragment has changed
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			// �൱��Fragment��onPause TODO������������
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
	 * ˢ��
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
	 * ��ȡ�����б�
	 */
	protected void queryMyfriends() {
		// �Ƿ����µĺ�������
		if (BmobDB.create(getActivity()).hasNewInvite()) {
			iv_msg_tips.setVisibility(View.VISIBLE);
		} else {
			iv_msg_tips.setVisibility(View.GONE);
		}
		// ����������һ�α��صĺ������ݿ�ļ�飬��Ϊ�˱��غ������ݿ����Ѿ�����˶Է������ǽ���ȴû����ʾ����������
		// �����������ڴ��б���ĺ����б�
		CustomApplication.getInstance().setContactList(
				CollectionUtils.list2map(BmobDB.create(getActivity())
						.getContactList()));

		Map<String, BmobChatUser> users = CustomApplication.getInstance()
				.getContactList();
		// ��װ�µ�User
		filledData(CollectionUtils.map2list(users));
		if (userAdapter == null) {
			userAdapter = new UserFriendAdapter(getActivity(), friends);
			list_friends.setAdapter(userAdapter);
		} else {
			userAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * ΪListView�������
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
			// ����ת����ƴ��
			String username = sortModel.getUsername();
			// ��û��username
			if (username != null) {
				String pinyin = characterParser.getSelling(sortModel
						.getUsername());
				String sortString = pinyin.substring(0, 1).toUpperCase();
				// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
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
	 * ��ʾɾ�����ѵĶԻ���
	 * 
	 * @param user
	 */
	private void showDeleteDialog(final User user) {
		DialogTips dialog = new DialogTips(getActivity(), user.getUsername(),
				"ɾ����ϵ��", "ȷ��", true, true);
		// ���óɹ��¼�
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int userId) {
				deleteContact(user);
			}
		});
		// ��ʾȷ�϶Ի���
		dialog.show();
		dialog = null;
	}

	// ɾ������
	protected void deleteContact(final User user) {

		final ProgressDialog progress = new ProgressDialog(getActivity());
		progress.setMessage("����ɾ��...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		userManager.deleteContact(user.getObjectId(), new UpdateListener() {

			@Override
			public void onSuccess() {
				showToast("ɾ���ɹ�");
				// ɾ���ڴ�
				CustomApplication.getInstance().getContactList()
						.remove(user.getUsername());
				// ���½���
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
		// �Ƚ�����ѵ���ϸ����ҳ��
		Intent intent = new Intent(getActivity(), SetMyInfoActivity.class);
		intent.putExtra("from", "other");
		intent.putExtra("username", user.getUsername());
		startAnimActivity(intent);
	}
}
