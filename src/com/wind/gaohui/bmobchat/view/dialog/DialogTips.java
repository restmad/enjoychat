package com.wind.gaohui.bmobchat.view.dialog;

import android.content.Context;

/**
 * ��ʾ�Ի�����һ��ȷ�Ϻ�һ�����ذ�ť
 * @author gaohui
 * @date 2016��4��12��15:21:30
 */
public class DialogTips extends DialogBase {

	private boolean hasNegative;
	private boolean hasTitle;
	/**
	 * ���췽��
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

	/**����֪ͨ�ĶԻ�����ʽ
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
		super.setTitle("��ʾ");
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
			super.setNameNegativeButton("ȡ��");
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
	 * ȷ�ϴ�����ť������onSuccessListener��onClick
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
