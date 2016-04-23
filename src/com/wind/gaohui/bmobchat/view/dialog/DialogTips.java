package com.wind.gaohui.bmobchat.view.dialog;

import android.content.Context;

/**
 * 提示对话框，有一个确认和一个返回按钮
 * @author gaohui
 * @date 2016年4月12日15:21:30
 */
public class DialogTips extends DialogBase {

	private boolean hasNegative;
	private boolean hasTitle;
	/**
	 * 构造方法
	 * @param context
	 * @param title
	 * @param message
	 * @param buttonText
	 * @param hasNegative
	 * @param hasTitle
	 */
	public DialogTips(Context context, String title,String message,String buttonText,boolean hasNegative,boolean hasTitle) {
		super(context);
		super.setTitle(title);
		super.setMessage(message);
		super.setNamePositiveButton(buttonText);
		this.hasNegative = hasNegative;
		this.hasTitle = hasTitle;
	}

	/**下线通知的对话框样式
	 * @param context
	 * @param title
	 * @param message
	 * @param buttonText
	 */
	public DialogTips(Context context,String message,String buttonText) {
		super(context);
		super.setMessage(message);
		super.setNamePositiveButton(buttonText);
		this.hasNegative = false;
		this.hasTitle = true;
		super.setTitle("提示");
		super.setCancel(false);
	}
	
	public DialogTips(Context context, String message,String buttonText,String negetiveText,String title,boolean isCancel) {
		super(context);
		super.setMessage(message);
		super.setNamePositiveButton(buttonText);
		this.hasNegative=false;
		super.setNameNegativeButton(negetiveText);
		this.hasTitle = true;
		super.setTitle(title);
		super.setCancel(isCancel);
	}
	
	@Override
	protected void onBuilding() {
		super.setWidth(dip2px(mainContext, 300));
		if(hasNegative){
			super.setNameNegativeButton("取消");
		}
		if(!hasTitle){
			super.setHasTitle(false);
		}
	}

	private int dip2px(Context context, int dipValue) {
		float scale=context.getResources().getDisplayMetrics().density;		
		return (int) (scale*dipValue+0.5f);		
	}
	/**
	 * 确认触发按钮，触发onSuccessListener的onClick
	 */
	@Override
	protected boolean OnClickPositiveButton() {
		if(onSuccessListener != null){
			onSuccessListener.onClick(this, 1);
		}
		return true;
	}

	@Override
	protected void OnClickNegativeButton() {
		if(onCancelListener != null){
			onCancelListener.onClick(this, 0);
		}
	}

	@Override
	protected void onDismiss() {
		
	}

}
