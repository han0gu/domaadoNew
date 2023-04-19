package com.domaado.mobileapp.widget;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.domaado.mobileapp.R;


/**
 * 
 * @author idman99
 * 참고사이트 : http://developer.android.com/guide/topics/ui/notifiers/toasts.html
 */
public class CustomToast extends Toast {

	Context mContext;
	int fontSize = 12;
	
	public CustomToast(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
		mContext = context;
	}
	
	public void showToast(String body) {
		showToast(mContext, body, Toast.LENGTH_SHORT);
	}
	
	public void showToast(String body, int duration) {
		showToast(mContext, body, duration);
	}
	
	public void showToast(String body, int duration, int size) {
		fontSize = size;
		showToast(mContext, body, duration);
	}

	public void showToast(Context ctx, String body, int duration){
		LayoutInflater inflater;
		View v;
		final Toast toast = this;

		inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.custom_toast, null);
		
		TextView text = (TextView) v.findViewById(R.id.CustomToastText);
		text.setText(body);
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
 
		show(this,v,duration);
		
		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				toast.cancel();
			}
			
		});
    }
 
    private void show(Toast toast, View v, int duration){
//        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
    	toast.setDuration(duration);
    	toast.setView(v);
    	toast.show();
    }
}
