package com.wind.gaohui.bmobchat.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.task.BRequest;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.wind.gaohui.bmobchat.adapter.AddFriendAdapter;
import com.wind.gaohui.bmobchat.util.CollectionUtils;
import com.wind.gaohui.bmobchat.view.xlist.XListView;
import com.wind.gaohui.bmobchat.view.xlist.XListView.IXListViewListener;
import com.wind.gaohui.bombchat.R;

/**
 * ��Ӻ���
 * 
 * @author gaohui
 * 
 */
public class AddFriendActivity extends ActivityBase implements
		IXListViewListener, OnItemClickListener {

	private EditText et_find_name;
	private Button btn_search;
	private XListView mListView;

	private List<BmobChatUser> users = new ArrayList<BmobChatUser>();
	private AddFriendAdapter adapter;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_add_contact);

		initView();

	}

	private void initView() {
		initTopBarForLeft("���Һ���");

		et_find_name = (EditText) findViewById(R.id.et_find_name);
		btn_search = (Button) findViewById(R.id.btn_search);

		initXListView();
	}

	String searchName = "";
	private void initXListView() {
		mListView = (XListView) findViewById(R.id.list_search);
		// ��������ظ���
		mListView.setPullLoadEnable(false);
		// ����������ˢ��
		mListView.setPullRefreshEnable(false);
		// ���ü���
		mListView.setListViewListener(this);

		mListView.pullRefreshing();

		adapter = new AddFriendAdapter(this, users);
		mListView.setAdapter(adapter);

		mListView.setOnItemClickListener(this);
		
		btn_search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				users.clear();
				searchName = et_find_name.getText().toString();
				if (!TextUtils.isEmpty(searchName)) {
					initSearchList(false);
				} else {
					showToast("�������û���");
				}
			}
		});

	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadMore() {
		userManager.querySearchTotalCount(searchName, new CountListener() {

			@Override
			public void onFailure(int arg0, String arg1) {
				showLog("��ѯ������������ʧ��"+arg1);
				refreshLoad();
			}

			@Override
			public void onSuccess(int arg0) {
				if (arg0 > users.size()) {
					curPage++;
					queryMoreSearchList(curPage);
				} else {
					showToast("���ݼ������");
					mListView.setPullLoadEnable(false);
					refreshLoad();
				}
			}
		});
	}

	/**
	 * ��ѯ����
	 */
	protected void queryMoreSearchList(int page) {
		userManager.queryUserByPage(true, page, searchName,
				new FindListener<BmobChatUser>() {

					@Override
					public void onError(int arg0, String arg1) {
						showLog("���������û�����:" + arg1);
						mListView.setPullLoadEnable(false);
						refreshLoad();
					}

					@Override
					public void onSuccess(List<BmobChatUser> arg0) {
						if (CollectionUtils.isNotNull(arg0)) {
							adapter.addAll(arg0);
						}
						refreshLoad();
					}
				});
	}

	/**
	 * ��������ڼ���״̬��ֹͣ���أ���Ϊ�Ѿ����سɹ����߼���ʧ��
	 */
	protected void refreshLoad() {
		if (mListView.getPullLoading()) {
			mListView.stopLoadMore();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		BmobChatUser user = (BmobChatUser) adapter.getItem(position - 1);
		Intent intent = new Intent(this, SetMyInfoActivity.class);
		intent.putExtra("from", "add");
		intent.putExtra("username", user.getUsername());
		startAnimActivity(intent);
	}


	int curPage = 0;
	ProgressDialog progress;

	private void initSearchList(final boolean isUpdate) {
		if (!isUpdate) {
			progress = new ProgressDialog(AddFriendActivity.this);
			progress.setMessage("��������...");
			progress.setCanceledOnTouchOutside(true);
			progress.show();
		}
		userManager.queryUserByPage(isUpdate, 0, searchName,
				new FindListener<BmobChatUser>() {

					@Override
					public void onError(int arg0, String arg1) {
						showLog("��ѯ����:" + arg1);
						if (users != null) {
							users.clear();
						}
						showToast("�û�������");
						// ���ò��ܼ��ظ���
						mListView.setPullLoadEnable(false);
						refreshPull();
						// �����ܱ�֤ÿ�β�ѯ���Ǵ�ͷ��ʼ
						curPage = 0;
					}

					@Override
					public void onSuccess(List<BmobChatUser> arg0) {
						if (CollectionUtils.isNotNull(arg0)) {
							if (isUpdate) {
								users.clear();
							}
							adapter.addAll(arg0);
							if (arg0.size() < BRequest.QUERY_LIMIT_COUNT) {
								// �����ѯ�ĸ�������10���������ò����Լ��ظ���
								mListView.setPullLoadEnable(false);
								showToast("�û��������!");
							} else {
								mListView.setPullLoadEnable(true);
							}
						} else {
							BmobLog.i("��ѯ�ɹ�:�޷���ֵ");
							if (users != null) {
								users.clear();
							}
							showToast("�û�������");
						}
						if (!isUpdate) {
							progress.dismiss();
						} else {
							refreshPull();
						}
						// �����ܱ�֤ÿ�β�ѯ���Ǵ�ͷ��ʼ
						curPage = 0;
					}
				});
	}

	protected void refreshPull() {
		// ���������ˢ�µ�״ֹ̬ͣˢ��
		if (mListView.getPullRefreshing()) {
			mListView.stopRefresh();
		}
	}
}
