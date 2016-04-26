package com.wind.gaohui.bmobchat.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import cn.bmob.im.task.BRequest;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.wind.gaohui.bmobchat.adapter.NearPeopleAdapter;
import com.wind.gaohui.bmobchat.bean.User;
import com.wind.gaohui.bmobchat.util.CollectionUtils;
import com.wind.gaohui.bmobchat.view.xlist.XListView;
import com.wind.gaohui.bmobchat.view.xlist.XListView.IXListViewListener;
import com.wind.gaohui.bombchat.R;

public class NearPeopleActivity extends ActivityBase implements
		IXListViewListener, OnItemClickListener {

	private XListView mListView;

	List<User> nears = new ArrayList<User>();
	private double QUERY_KILOMETERS = 10;//默认查询10公里范围内的人

	private NearPeopleAdapter adapter;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_near_people);
		initView();
	}

	private void initView() {
		initTopBarForLeft("附近的人");
		mListView = (XListView) findViewById(R.id.list_near);
		mListView.setOnItemClickListener(this);
		// 首先不允许加载更多
		mListView.setPullLoadEnable(false);
		// 允许下拉
		mListView.setPullRefreshEnable(true);
		// 设置监听器
		mListView.setXListViewListener(this);
		// 默认情况下就执行这个动作--->下拉刷新
		mListView.pullRefreshing();

		adapter = new NearPeopleAdapter(this, nears);
		mListView.setAdapter(adapter);

		initNearByList(false);
	}

	int curPage = 0;
	ProgressDialog progress;

	private void initNearByList(final boolean isUpdate) {
		if (!isUpdate) {
			progress = new ProgressDialog(NearPeopleActivity.this);
			progress.setMessage("正在查询附近的人...");
			progress.setCanceledOnTouchOutside(true);
			progress.show();
		}
		if(!mApplication.getLatitude().equals("") && !mApplication.getLongtitude().equals("")) {
			double latitude = Double.parseDouble(mApplication.getLatitude());
			double longtitude = Double.parseDouble(mApplication.getLongtitude());
			//封装的查询方法，当进入此页面时 isUpdate为false，当下拉刷新的时候设置为true就行。
			//此方法默认每页查询10条数据,若想查询多于10条，可在查询之前设置BRequest.QUERY_LIMIT_COUNT，如：BRequest.QUERY_LIMIT_COUNT=20
			// 此方法是新增的查询指定10公里内的性别为女性的用户列表，默认包含好友列表
			//如果你不想查询性别为女的用户，可以将equalProperty设为null或者equalObj设为null即可
			userManager.queryKiloMetersListByPage(isUpdate,0,"location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new FindListener<User>() {
				//此方法默认查询所有带地理位置信息的且性别为女的用户列表，如果你不想包含好友列表的话，将查询条件中的isShowFriends设置为false就行
				//userManager.queryNearByListByPage(isUpdate,0,"location", longtitude, latitude, true,"sex",false,new FindListener<User>() {
					@Override
					public void onSuccess(List<User> arg0) {
						if (CollectionUtils.isNotNull(arg0)) {
							if(isUpdate){
								nears.clear();
							}
							adapter.addAll(arg0);
							if(arg0.size()<BRequest.QUERY_LIMIT_COUNT){
								mListView.setPullLoadEnable(false);
								showToast("附近的人搜索完成!");
							}else{
								mListView.setPullLoadEnable(true);
							}
						}else{
							showToast("暂无附近的人!");
						}
						
						if(!isUpdate){
							progress.dismiss();
						}else{
							refreshPull();
						}
					}
					
					@Override
					public void onError(int arg0, String arg1) {
						showToast("暂无附近的人!");
						mListView.setPullLoadEnable(false);
						if(!isUpdate){
							progress.dismiss();
						}else{
							refreshPull();
						}
					}

				});
			}else{
				showToast("暂无附近的人!");
				progress.dismiss();
				refreshPull();
			}
	}

	protected void refreshPull() {
		if (mListView.getPullRefreshing()) {
			mListView.stopRefresh();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	@Override
	public void onRefresh() {
		initNearByList(true);
	}

	@Override
	public void onLoadMore() {
		double latitude = Double.parseDouble(mApplication.getLatitude());
		double longtitude = Double.parseDouble(mApplication.getLongtitude());
		//这是查询10公里范围内的性别为女用户总数
		userManager.queryKiloMetersTotalCount(User.class, "location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new CountListener() {
	    //这是查询附近的人且性别为女性的用户总数
		//userManager.queryNearTotalCount(User.class, "location", longtitude, latitude, true,"sex",false,new CountListener() {
			
			@Override
			public void onSuccess(int arg0) {
				if(arg0 >nears.size()){
					curPage++;
					queryMoreNearList(curPage);
				}else{
					showToast("数据加载完成");
					mListView.setPullLoadEnable(false);
					refreshLoad();
				}
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				showLog("查询附近的人总数失败"+arg1);
				refreshLoad();
			}
		});
		
	
	}

	protected void queryMoreNearList(int page) {

		double latitude = Double.parseDouble(mApplication.getLatitude());
		double longtitude = Double.parseDouble(mApplication.getLongtitude());
		//查询10公里范围内的性别为女的用户列表
		userManager.queryKiloMetersListByPage(true,page,"location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new FindListener<User>() {
		//查询全部地理位置信息且性别为女性的用户列表
		//userManager.queryNearByListByPage(true,page, "location", longtitude, latitude, true,"sex",false,new FindListener<User>() {

			@Override
			public void onSuccess(List<User> arg0) {
				if (CollectionUtils.isNotNull(arg0)) {
					adapter.addAll(arg0);
				}
				refreshLoad();
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				showLog("查询更多附近的人出错:"+arg1);
				mListView.setPullLoadEnable(false);
				refreshLoad();
			}

		});
	
	}

	protected void refreshLoad() {
		if (mListView.getPullLoading()) {
			mListView.stopLoadMore();
		}
	}
}
