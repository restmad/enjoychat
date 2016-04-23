package com.wind.gaohui.bmobchat.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.db.BmobDB;

import com.wind.gaohui.bmobchat.adapter.NewFriendAdapter;
import com.wind.gaohui.bmobchat.view.dialog.DialogTips;
import com.wind.gaohui.bombchat.R;

public class NewFriendActivity extends ActivityBase implements
		OnItemLongClickListener {

	private String from = "";
	private ListView listview;
	private NewFriendAdapter adapter;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_new_friend);
		from = getIntent().getStringExtra("from");
		initView();
	}

	private void initView() {
		initTopBarForLeft("������");
		listview = (ListView) findViewById(R.id.list_newfriend);
		listview.setOnItemLongClickListener(this);
		adapter = new NewFriendAdapter(this, BmobDB.create(this)
				.queryBmobInviteList());
		listview.setAdapter(adapter);
		if (from == null) {
			// ������֪ͨ������λ�����һ��
			listview.setSelection(adapter.getCount());
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		BmobInvitation invite = (BmobInvitation) adapter.getItem(position);
		showDeleteDialog(position, invite);
		return true;
	}

	private void showDeleteDialog(final int position,
			final BmobInvitation invite) {
		DialogTips dialog = new DialogTips(this, invite.getFromname(),
				"ɾ����������", "ȷ��", true, true);
		// ���óɹ��¼�
		dialog.SetOnSuccessListener(new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int userId) {
				deleteInvite(position, invite);
			}
		});
		// ��ʾȷ�϶Ի���
		dialog.show();
		dialog = null;
	}

	/**
	 * ɾ����������
	 * 
	 * @param position
	 * @param invite
	 */
	protected void deleteInvite(int position, BmobInvitation invite) {
		adapter.remove(position);
		BmobDB.create(this).deleteInviteMsg(invite.getFromid(),
				Long.toString(invite.getTime()));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (from == null) {
			startAnimActivity(MainActivity.class);
		}
	}
}
