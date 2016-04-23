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
 * 带删除按钮的EditText
 * @author gaohui
 * @date 2016年4月16日15:23:11
 */
@SuppressLint("ClickableViewAccessibility")
public class ClearEditText extends EditText implements OnFocusChangeListener, TextWatcher {
	
	//删除按钮的引用
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
		//获得EditText右边的DrawableRight，如果没有就用默认的 
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
	 * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
	 * @param visible
	 */
	private void setClearIconVisible(boolean visible) {
		Drawable right = visible?mClearDrawable:null;
		setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
	}

	/**
	 * 由于在控件内部，无法为其设置点击事件的监听
	 * 所以这里记住按下的位置来模拟点击事件
	 * 当点击的位置在EditText的宽度 - 图标到控件右边的间距 - 图标的宽度 和 EditText的宽度 - 图标到控件右边的间距 之间时，算是点击了该图标
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
	 * 当ClearEditText的焦点发生变化时，根据里面的字符串长度来控制清除按钮的显示与隐藏
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus) {  //如果获得焦点
			setClearIconVisible(getText().length()>0);
		} else {
			setClearIconVisible(false);
		}
	}

	/**
	 * 当输入框中的内容发生变化是回调
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
