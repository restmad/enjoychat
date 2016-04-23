package com.wind.gaohui.bmobchat.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.EditText;

public class EmotionsEditText extends EditText {
	public EmotionsEditText(Context context) {
		super(context);
	}

	public EmotionsEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EmotionsEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		if (!TextUtils.isEmpty(text)) {
			super.setText(replace(text.toString()), type);
		} else {
			super.setText(text, type);
		}
	}

	private CharSequence replace(String string) {
		try {
			SpannableString spannableString = new SpannableString(string);
			int start = 0;
			Pattern pattern = buildPattern();
			Matcher matcher = pattern.matcher(string);
			while (matcher.find()) {
				String faceText = matcher.group();
				String key = faceText.substring(1);
				BitmapFactory.Options options = new BitmapFactory.Options();
				Bitmap bitmap = BitmapFactory.decodeResource(
						getContext().getResources(),
						getContext().getResources().getIdentifier(key, "drawable",
								getContext().getPackageName()), options);
				ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
				int startIndex = string.indexOf(faceText, start);
				int endIndex = startIndex + faceText.length();
				if(startIndex >= 0) {
					spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			return spannableString;
		} catch (Exception e) {
			return string;
		}
	}

	private Pattern buildPattern() {
		return Pattern.compile("\\\\ue[a-z0-9]{3}", Pattern.CASE_INSENSITIVE);
	}

}
