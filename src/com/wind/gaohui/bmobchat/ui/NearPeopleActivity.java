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
	private double QUERY_KILOMETERS = 10;//Ĭ�ϲ�ѯ10���ﷶΧ�ڵ���

	private NearPeopleAdapter adapter;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_near_people);
		initView();
	}

	private void initView() {
		initTopBarForLeft("��������");
		mListView = (XListView) findViewById(R.id.list_near);
		mListView.setOnItemClickListener(this);
		// ���Ȳ�������ظ���
		mListView.setPullLoadEnable(false);
		// ��������
		mListView.setPullRefreshEnable(true);
		// ���ü�����
		mListView.setXListViewListener(this);
		// Ĭ������¾�ִ���������--->����ˢ��
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
			progress.setMessage("���ڲ�ѯ��������...");
			progress.setCanceledOnTouchOutside(true);
			progress.show();
		}
		if(!mApplication.getLatitude().equals("") && !mApplication.getLongtitude().equals("")) {
			double latitude = Double.parseDouble(mApplication.getLatitude());
			double longtitude = Double.parseDouble(mApplication.getLongtitude());
			//��װ�Ĳ�ѯ�������������ҳ��ʱ isUpdateΪfalse��������ˢ�µ�ʱ������Ϊtrue���С�
			//�˷���Ĭ��ÿҳ��ѯ10������,�����ѯ����10�������ڲ�ѯ֮ǰ����BRequest.QUERY_LIMIT_COUNT���磺BRequest.QUERY_LIMIT_COUNT=20
			// �˷����������Ĳ�ѯָ��10�����ڵ��Ա�ΪŮ�Ե��û��б�Ĭ�ϰ��������б�
			//����㲻���ѯ�Ա�ΪŮ���û������Խ�equalProperty��Ϊnull����equalObj��Ϊnull����
			userManager.queryKiloMetersListByPage(isUpdate,0,"location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new FindListener<User>() {
				//�˷���Ĭ�ϲ�ѯ���д�����λ����Ϣ�����Ա�ΪŮ���û��б�����㲻����������б�Ļ�������ѯ�����е�isShowFriends����Ϊfalse����
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
								showToast("���������������!");
							}else{
								mListView.setPullLoadEnable(true);
							}
						}else{
							showToast("���޸�������!");
						}
						
						if(!isUpdate){
							progress.dismiss();
						}else{
							refreshPull();
						}
					}
					
					@Override
					public void onError(int arg0, String arg1) {
						showToast("���޸�������!");
						mListView.setPullLoadEnable(false);
						if(!isUpdate){
							progress.dismiss();
						}else{
							refreshPull();
						}
					}

				});
			}else{
				showToast("���޸�������!");
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
		//���ǲ�ѯ10���ﷶΧ�ڵ��Ա�ΪŮ�û�����
		userManager.queryKiloMetersTotalCount(User.class, "location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new CountListener() {
	    //���ǲ�ѯ�����������Ա�ΪŮ�Ե��û�����
		//userManager.queryNearTotalCount(User.class, "location", longtitude, latitude, true,"sex",false,new CountListener() {
			
			@Override
			public void onSuccess(int arg0) {
				if(arg0 >nears.size()){
					curPage++;
					queryMoreNearList(curPage);
				}else{
					showToast("���ݼ������");
					mListView.setPullLoadEnable(false);
					refreshLoad();
				}
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				showLog("��ѯ������������ʧ��"+arg1);
				refreshLoad();
			}
		});
		
	
	}

	protected void queryMoreNearList(int page) {

		double latitude = Double.parseDouble(mApplication.getLatitude());
		double longtitude = Double.parseDouble(mApplication.getLongtitude());
		//��ѯ10���ﷶΧ�ڵ��Ա�ΪŮ���û��б�
		userManager.queryKiloMetersListByPage(true,page,"location", longtitude, latitude, true,QUERY_KILOMETERS,"sex",false,new FindListener<User>() {
		//��ѯȫ������λ����Ϣ���Ա�ΪŮ�Ե��û��б�
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
				showLog("��ѯ���฽�����˳���:"+arg1);
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
