package com.wind.gaohui.bmobchat.adapter.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.im.util.BmobLog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

/**
 * @author gaohui
 * 
 * @param <E>
 */
@SuppressLint("UseSparseArrays")
public abstract class BaseListAdapter<E> extends BaseAdapter {

	public List<E> list;

	public Context mContext;

	public LayoutInflater mLayoutInflater;

	public List<E> getList() {
		return list;
	}

	public void setList(List<E> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public void add(E e) {
		this.list.add(e);
		notifyDataSetChanged();
	}

	public void addAll(List<E> list) {
		this.list.addAll(list);
		notifyDataSetChanged();
	}

	public void remove(int position) {
		this.list.remove(position);
		notifyDataSetChanged();
	}

	public BaseListAdapter(Context context, List<E> list) {
		super();
		this.list = list;
		this.mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = bindView(position, convertView, parent);

		// 绑定内部监听
		addInternalClickListener(convertView, position, list.get(position));
		return convertView;
	}

	public Map<Integer, OnInternalClickListener> canClickItem;

	// adapter中的内部监听
	private void addInternalClickListener(final View convertView,
			final int position, final Object valuesMap) {
		if (canClickItem != null) {
			for (Integer key : canClickItem.keySet()) {
				View inView = convertView.findViewById(key);
				final OnInternalClickListener inViewListener = canClickItem
						.get(key);
				if (inView != null && inViewListener != null) {
					inView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							inViewListener.OnClickListener(convertView, v,
									position, valuesMap);
						}
					});
				}
			}
		}
	}

	Toast mToast;

	public void showToast(final String text) {
		if (!TextUtils.isEmpty(text)) {
			((Activity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mToast == null) {
						mToast = Toast.makeText(mContext, text,
								Toast.LENGTH_SHORT);
					} else {
						mToast.setText(text);
					}
					mToast.show();
				}
			});
		}
	}
	
	public void showLog(String msg) {
		BmobLog.i(msg);
	}

	public void setOnInViewListener(Integer key,
			OnInternalClickListener onClickListener) {
		if (canClickItem == null) {
			canClickItem = new HashMap<Integer, OnInternalClickListener>();
		}
		canClickItem.put(key, onClickListener);
	}

	public interface OnInternalClickListener {
		public void OnClickListener(View parentView, View view,
				Integer position, Object values);
	}

	public abstract View bindView(int position, View convertView,
			ViewGroup parent);

}
