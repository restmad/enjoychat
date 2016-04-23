package com.wind.gaohui.bmobchat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.bmob.im.db.BmobDB;

import com.wind.gaohui.bmobchat.adapter.BlackListAdapter;
import com.wind.gaohui.bmobchat.view.HeaderLayout;
import com.wind.gaohui.bombchat.R;

public class BlackListActivity extends ActivityBase implements
		OnItemClickListener {
	private HeaderLayout mHeaderLayout;
	private BlackListAdapter adapter;
	private ListView list_blacklist;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_blacklist);

		initView();
	}

	private void initView() {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		initTopBarForOnlyTitle("ºÚÃûµ¥");

		adapter = new BlackListAdapter(this, BmobDB.create(this).getBlackList());
		list_blacklist = (ListView) findViewById(R.id.list_blacklist);
		list_blacklist.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//TODO
	}
}
