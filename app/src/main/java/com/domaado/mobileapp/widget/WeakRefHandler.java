package com.domaado.mobileapp.widget;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class WeakRefHandler extends Handler {

	private final WeakReference<IOnHandlerMessage> mHandlerActivity;
//	private WeakReference<Context> mContext;
	
	public WeakRefHandler(IOnHandlerMessage activity) {
		mHandlerActivity = new WeakReference<IOnHandlerMessage>(activity);
//		mContext = new WeakReference<Context>((Context) activity);
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		IOnHandlerMessage activity = (IOnHandlerMessage) mHandlerActivity.get();
		if ( activity == null ) return;
		activity.handleMessage(msg);
//		
//		IOnHandlerMessage context = (IOnHandlerMessage) mHandlerActivity.get();
//		context.handleMessage(msg);
	}
	
	public interface IOnHandlerMessage {
		public void handleMessage(Message msg);
	}
}
