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
 * 添加好友
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
		initTopBarForLeft("查找好友");

		et_find_name = (EditText) findViewById(R.id.et_find_name);
		btn_search = (Button) findViewById(R.id.btn_search);

		initXListView();
	}

	String searchName = "";
	private void initXListView() {
		mListView = (XListView) findViewById(R.id.list_search);
		// 不允许加载更多
		mListView.setPullLoadEnable(false);
		// 不允许下拉刷新
		mListView.setPullRefreshEnable(false);
		// 设置监听
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
					showToast("请输入用户名");
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
				showLog("查询附近的人总数失败"+arg1);
				refreshLoad();
			}

			@Override
			public void onSuccess(int arg0) {
				if (arg0 > users.size()) {
					curPage++;
					queryMoreSearchList(curPage);
				} else {
					showToast("数据加载完成");
					mListView.setPullLoadEnable(false);
					refreshLoad();
				}
			}
		});
	}

	/**
	 * 查询更多
	 */
	protected void queryMoreSearchList(int page) {
		userManager.queryUserByPage(true, page, searchName,
				new FindListener<BmobChatUser>() {

					@Override
					public void onError(int arg0, String arg1) {
						showLog("搜索更多用户出错:" + arg1);
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
	 * 如果是正在加载状态则停止加载，因为已经加载成功或者加载失败
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
			progress.setMessage("正在搜索...");
			progress.setCanceledOnTouchOutside(true);
			progress.show();
		}
		userManager.queryUserByPage(isUpdate, 0, searchName,
				new FindListener<BmobChatUser>() {

					@Override
					public void onError(int arg0, String arg1) {
						showLog("查询错误:" + arg1);
						if (users != null) {
							users.clear();
						}
						showToast("用户不存在");
						// 设置不能加载更多
						mListView.setPullLoadEnable(false);
						refreshPull();
						// 这样能保证每次查询都是从头开始
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
								// 如果查询的个数少于10个，就设置不可以加载更多
								mListView.setPullLoadEnable(false);
								showToast("用户搜索完成!");
							} else {
								mListView.setPullLoadEnable(true);
							}
						} else {
							BmobLog.i("查询成功:无返回值");
							if (users != null) {
								users.clear();
							}
							showToast("用户不存在");
						}
						if (!isUpdate) {
							progress.dismiss();
						} else {
							refreshPull();
						}
						// 这样能保证每次查询都是从头开始
						curPage = 0;
					}
				});
	}

	protected void refreshPull() {
		// 如果是正在刷新的状态停止刷新
		if (mListView.getPullRefreshing()) {
			mListView.stopRefresh();
		}
	}
}
