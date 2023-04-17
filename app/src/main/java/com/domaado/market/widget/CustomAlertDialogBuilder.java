package com.domaado.market.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.domaado.market.Common;
import com.domaado.market.R;


@SuppressLint("InflateParams")
public class CustomAlertDialogBuilder extends AlertDialog.Builder {

	Context mContext;
	
	private TextView mTitleView;
    private TextView mContentView;
    private Button yesBtn, neutralBtn, noBtn;
    private String mTitle;
    private String mContent;
    
    private int buttonHeight;
    
    private String leftBtnText, centerBtnText, rightBtnText;
     
    private View.OnClickListener[] listeners;
    
    private View.OnClickListener mLeftClickListener;
    private View.OnClickListener mCenterClickListener;
    private View.OnClickListener mRightClickListener;
    
    private Activity act;
    private FrameLayout dialogView;
    
    private AlertDialog alert;
    
    public CustomAlertDialogBuilder(Activity act, int theme) {
    	super(act, theme);
		// TODO Auto-generated constructor stub
		
		//application = _app;
		mContext = act.getApplicationContext();

		if(dialogView == null) {
			layoutInit();
		}
	}
    
	public CustomAlertDialogBuilder(Activity act) {
		super(act);
		// TODO Auto-generated constructor stub
		
		//application = _app;
		mContext = act.getApplicationContext();
		
		if(dialogView == null) {
			layoutInit();			
		}
	}
	
	private void layoutInit() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (FrameLayout) inflater.inflate(R.layout.custom_dialog_builder, null);
		
		dialogView.setBackgroundResource(R.drawable.trans_space);
		
		setView(dialogView);
	}
	
	public void setUI() {
		setLayout();
        if(!TextUtils.isEmpty(mTitle)) setTitleView(mTitle);
        setContentView(mContent);
        setButtonText(leftBtnText, centerBtnText, rightBtnText);
        
        if(mLeftClickListener != null && mRightClickListener != null && mCenterClickListener != null) {
        	listeners = new View.OnClickListener[] { mLeftClickListener, mCenterClickListener, mRightClickListener };
        } else if(mLeftClickListener != null && mRightClickListener != null) {
        	listeners = new View.OnClickListener[] { mLeftClickListener, mRightClickListener };
        } else if(mLeftClickListener != null) {
        	listeners = new View.OnClickListener[] { mLeftClickListener };
        }
        
        setClickListener(listeners);
	}
	
	public void setListeners(View.OnClickListener... listener) {
		this.listeners = listener;
	}
	
	@Override
	public AlertDialog show() {
		// TODO Auto-generated method stub
		
		if(alert==null) return null;
		
		setUI();
		
		alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Window window = alert.getWindow();
        WindowManager.LayoutParams dialogWindowAttributes = window.getAttributes();

        // Set fixed width (280dp) and WRAP_CONTENT height
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        dialogWindowAttributes.windowAnimations = R.style.AppTheme;
        	lp.copyFrom(dialogWindowAttributes);
//        	lp.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, mContext.getResources().getDisplayMetrics());
//        	lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        	lp.gravity = Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL;
        	lp.dimAmount = 0.7f;
        window.setAttributes(lp);
        
        // Set to TYPE_SYSTEM_ALERT so that the Service can display it
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        
        alert.show();
        
        return alert;
		//return super.show();
	}

	public void dismiss() {
		if(alert != null) alert.dismiss();
	}

	public void setCustomAlertDialog(String mesg, String[] btnTexts, View.OnClickListener... listener) {
		setCustomAlertDialog(mContext.getResources().getString(R.string.app_name), mesg, btnTexts, listener);
		
	}
	
	public void setCustomAlertDialog(String title, String mesg, String[] btnTexts, View.OnClickListener... listener) {
		
		this.mTitle = title;
		this.mContent = mesg;
		
		if(btnTexts != null) {
			switch(btnTexts.length) {
			case 1:
				this.leftBtnText = btnTexts[0];
				break;
			case 2:
				this.leftBtnText = btnTexts[0];
				this.rightBtnText = btnTexts[1];
				break;
			case 3:
				this.leftBtnText = btnTexts[0];
				this.centerBtnText = btnTexts[1];
				this.rightBtnText = btnTexts[2];
				break;
			}
		}
		
		this.listeners = listener;
	}
	
	public void setMessage(String content) {
    	this.mContent = content;
    }
	
	public void setPositiveButton(String btnText, View.OnClickListener listener) {
		this.leftBtnText = btnText;
		this.mLeftClickListener = listener;
	}
	
	public void setNegativeButton(String btnText, View.OnClickListener listener) {
		this.rightBtnText = btnText;
		this.mRightClickListener = listener;
	}
	
	public void setNeutralButton(String btnText, View.OnClickListener listener) {
		this.rightBtnText = btnText;
		this.mCenterClickListener = listener;
	}
	
	public void setButtonHeight(int height) {
		buttonHeight = (int)Common.convertDpToPixel(mContext, height);
	}
	
	public int getButtonHeight() {
		if(yesBtn != null) {
			return yesBtn.getHeight();
		}
		
		return 0;
	}
	
	public void setTitle(String title) {
		this.mTitle = title;
	}
	
	private void setTitleView(String title){
        mTitleView.setText(title);
    }
     
    private void setContentView(String content){
    	if(!TextUtils.isEmpty(content))
    		mContentView.setText(content);
    	else
    		mContentView.setVisibility(View.GONE);
    }
    
    private void setButtonText(String leftBtn, String centerBtn, String rightBtn) {
    	yesBtn.setText(leftBtn);
    	neutralBtn.setText(centerBtn);
    	noBtn.setText(rightBtn);
    }
    
    private void setClickListener(View.OnClickListener... listener) {
    	if(listener != null) {
			switch(listener.length) {
			case 1:
				yesBtn.setOnClickListener(listener[0]);
				break;
			case 2:
				yesBtn.setOnClickListener(listener[0]);
				noBtn.setOnClickListener(listener[1]);
				noBtn.setVisibility(View.VISIBLE);
				break;
			case 3:
				yesBtn.setOnClickListener(listener[0]);
				neutralBtn.setOnClickListener(listener[1]);
				noBtn.setOnClickListener(listener[2]);
				neutralBtn.setVisibility(View.VISIBLE);
				noBtn.setVisibility(View.VISIBLE);
				break;
			}
			
			setButtonWeight(listener.length);
		}
    }
    
    /*
     * Layout
     */
    private void setLayout(){
        mTitleView = (TextView)dialogView.findViewById(R.id.cd_title);
        
        mContentView = (TextView)dialogView.findViewById(R.id.cd_message);
		
        yesBtn = (Button)dialogView.findViewById(R.id.cd_ok_btn);
		neutralBtn = (Button)dialogView.findViewById(R.id.cd_neutral_btn); neutralBtn.setVisibility(View.GONE);
		noBtn = (Button)dialogView.findViewById(R.id.cd_no_btn); noBtn.setVisibility(View.GONE);
    }
    
    private void setButtonWeight(int numb) {
		
    	float weight = 3.0f;	// layout weight sum이 3으로 설정되어 있음!
    	int margin = (int) Common.convertDpToPixel(mContext, 5);
    	
    	switch(numb) {
    	case 1:
    		weight = 3f;
    		break;
    	case 2:
    		weight = 1.5f;
    		break;
    	case 3:
    		weight = 1f;
    		break;
    	}
    	
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) Common.convertDpToPixel(mContext, 35));
		p.weight = weight;
		if(buttonHeight>0) p.height = buttonHeight;
		p.setMargins(margin, margin, margin, margin);
		p.gravity = Gravity.CENTER_HORIZONTAL;
		
		yesBtn.setLayoutParams(p);
		neutralBtn.setLayoutParams(p);
		noBtn.setLayoutParams(p);
    }
    
    @Override
	public AlertDialog create() {
		// TODO Auto-generated method stub
		//return super.create();
    	
		alert = super.create();
		
		return alert;
	}

    @Override
	public Builder setOnDismissListener(OnDismissListener onDismissListener) {
		// TODO Auto-generated method stub
		return super.setOnDismissListener(onDismissListener);
	}
}
