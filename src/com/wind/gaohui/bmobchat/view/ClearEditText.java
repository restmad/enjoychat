package com.wind.gaohui.bmobchat.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.wind.gaohui.bombchat.R;

/**
 * ��ɾ����ť��EditText
 * @author gaohui
 * @date 2016��4��16��15:23:11
 */
@SuppressLint("ClickableViewAccessibility")
public class ClearEditText extends EditText implements OnFocusChangeListener, TextWatcher {
	
	//ɾ����ť������
	private Drawable mClearDrawable;
	public ClearEditText(Context context) {
		this(context,null);
	}

	public ClearEditText(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		//���EditText�ұߵ�DrawableRight�����û�о���Ĭ�ϵ� 
		mClearDrawable = getCompoundDrawables()[2];
		if(mClearDrawable == null) {
			mClearDrawable = getResources().getDrawable(R.drawable.search_clear);
		}
		mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
		setClearIconVisible(false);
		setOnFocusChangeListener(this);
		addTextChangedListener(this);
	}
	
	/**
	 * �������ͼ�����ʾ�����أ�����setCompoundDrawablesΪEditText������ȥ
	 * @param visible
	 */
	private void setClearIconVisible(boolean visible) {
		Drawable right = visible?mClearDrawable:null;
		setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
	}

	/**
	 * �����ڿؼ��ڲ����޷�Ϊ�����õ���¼��ļ���
	 * ���������ס���µ�λ����ģ�����¼�
	 * �������λ����EditText�Ŀ�� - ͼ�굽�ؼ��ұߵļ�� - ͼ��Ŀ�� �� EditText�Ŀ�� - ͼ�굽�ؼ��ұߵļ�� ֮��ʱ�����ǵ���˸�ͼ��
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(getCompoundDrawables()[2] != null ) {
			if(event.getAction() == MotionEvent.ACTION_UP) {
				boolean touchable = event.getX()>(getWidth()-getPaddingRight()-mClearDrawable.getIntrinsicWidth()) &&
						event.getX() < (getWidth()-getPaddingRight());
				if(touchable) {
					setText("");
				}
			}
		}
		
		return super.onTouchEvent(event);
	}
	
	/**
	 * ��ClearEditText�Ľ��㷢���仯ʱ������������ַ������������������ť����ʾ������
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus) {  //�����ý���
			setClearIconVisible(getText().length()>0);
		} else {
			setClearIconVisible(false);
		}
	}

	/**
	 * ��������е����ݷ����仯�ǻص�
	 */
	@Override
	public void onTextChanged(CharSequence text, int start,
			int lengthBefore, int lengthAfter) {
		setClearIconVisible(text.length()>0);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		
	}


}
