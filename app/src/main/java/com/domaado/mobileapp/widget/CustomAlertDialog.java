package com.domaado.mobileapp.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.domaado.mobileapp.R;


/**
 * 커스텀 알럿 다이럴로그
 */
public class CustomAlertDialog extends Dialog {
	Context mContext;

	private TextView mTitle;
	private TextView mContentView;
    private Button closeBtn, noBtn, yesBtn;
    private Spanned mContent;

    private String mTitleText;
    private String closeBtnText, noBtnText, yesBtnText;
     
    private View.OnClickListener[] listeners;
    
    private View.OnClickListener mCloseClickListener;	// close
    private View.OnClickListener mNoClickListener;	// no
    private View.OnClickListener mYesClickListener;	// yes
    
    private LinearLayout popup_button_set_yn;
    private LinearLayout popup_button_set_ok;
    
    private boolean isConfirm = false;
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);
         
        setContentView(R.layout.custom_dialog_message);

		setLayout(isConfirm);

		setBodyMessageView();
        setButtonText();
        
        if(mCloseClickListener != null && mYesClickListener != null && mNoClickListener != null) {
        	listeners = new View.OnClickListener[] { mCloseClickListener, mNoClickListener, mYesClickListener };
        } else if(mNoClickListener != null && mYesClickListener != null) {
        	listeners = new View.OnClickListener[] { mNoClickListener, mYesClickListener };
        } else if(mCloseClickListener != null) {
        	listeners = new View.OnClickListener[] { mCloseClickListener };
        }
        
        setClickListener(listeners);
	}

	public CustomAlertDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		// TODO Auto-generated constructor stub
		
		mContext = context;
	}
	
	public CustomAlertDialog(Context context, String mesg, View.OnClickListener... listener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		// TODO Auto-generated constructor stub
		
		mContext = context;
		
		this.listeners = listener;
	}

	public void setMessage(String content) {
		if(!TextUtils.isEmpty(content)) {
			content = content.replaceAll("\n", "<br/>");

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				this.mContent = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
			} else {
				this.mContent = Html.fromHtml(content);
			}
		} else {
			this.mContent = new SpannableString("Empty message!");
		}
    }

    public void setMTitle(String title) {
		this.mTitleText = title;
	}

	public void setMessage(Spanned content) {
		this.mContent = content;
	}
	
	public void setCloseButton(String btnText, View.OnClickListener listener) {
		setPositiveButton(btnText, listener);
	}
	
	public void setPositiveButton(String btnText, View.OnClickListener listener) {
		this.closeBtnText = btnText;
		this.mCloseClickListener = listener;
	}
	
	public void setNoButton(String btnText, View.OnClickListener listener) {
		setNegativeButton(btnText, listener);
	}
	
	public void setNegativeButton(String btnText, View.OnClickListener listener) {
		this.noBtnText = btnText;
		this.mNoClickListener = listener;
	}
	
	public void setYesButton(String btnText, View.OnClickListener listener) {
		setNeutralButton(btnText, listener);
	}
	
	public void setNeutralButton(String btnText, View.OnClickListener listener) {
		this.yesBtnText = btnText;
		this.mYesClickListener = listener;
	}
	
	public void setIcon(int res) {
		//
	}
	
	public int getButtonHeight() {
		if(yesBtn != null) {
			return yesBtn.getHeight();
		}
		
		return 0;
	}

    private void setBodyMessageView(){

		if(TextUtils.isEmpty(mTitleText)) mTitleText = mContext.getResources().getString(R.string.app_title);

		mTitle.setText(mTitleText);

    	if(!TextUtils.isEmpty(mContent))
    		mContentView.setText(mContent);
    	else
    		mContentView.setVisibility(View.GONE);
    }
    
    private void setButtonText() {

		closeBtn.setText(closeBtnText);
		noBtn.setText(noBtnText);
		yesBtn.setText(yesBtnText);
    }
    
    private void setClickListener(View.OnClickListener... listener) {
    	if(listener != null) {
			switch(listener.length) {
			case 1:
				closeBtn.setOnClickListener(listener[0]);
				popup_button_set_yn.setVisibility(View.GONE);
	        	popup_button_set_ok.setVisibility(View.VISIBLE);
				break;
			case 2:
				noBtn.setOnClickListener(listener[0]);
				yesBtn.setOnClickListener(listener[1]);
				popup_button_set_yn.setVisibility(View.VISIBLE);
	        	popup_button_set_ok.setVisibility(View.GONE);
				break;
			case 3:
				closeBtn.setOnClickListener(listener[0]);
				noBtn.setOnClickListener(listener[1]);
				yesBtn.setOnClickListener(listener[2]);
				popup_button_set_yn.setVisibility(View.VISIBLE);
	        	popup_button_set_ok.setVisibility(View.VISIBLE);
				break;
			}

		}
    }
    
    /*
     * Layout
     */
    private void setLayout(boolean isYN) {

    	mTitle = (TextView)findViewById(R.id.cd_title);
        mContentView = (TextView)findViewById(R.id.cd_message);
        
        popup_button_set_yn = (LinearLayout)findViewById(R.id.popup_button_set_yn);
        popup_button_set_ok = (LinearLayout)findViewById(R.id.popup_button_set_ok);

        yesBtn = (Button)findViewById(R.id.cd_ok_btn);
		noBtn = (Button)findViewById(R.id.cd_no_btn);
		closeBtn = (Button)findViewById(R.id.cd_close_btn);
		
        if(isYN) {
        	popup_button_set_yn.setVisibility(View.VISIBLE);
        	popup_button_set_ok.setVisibility(View.GONE);
        } else {
        	popup_button_set_yn.setVisibility(View.GONE);
        	popup_button_set_ok.setVisibility(View.VISIBLE);
			popup_button_set_ok.invalidate();
        }
    }

}
