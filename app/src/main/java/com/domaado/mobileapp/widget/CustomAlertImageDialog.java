package com.domaado.mobileapp.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.domaado.mobileapp.R;


/**
 * 이미지를 포함한 커스텀 알럿 다이얼로그!
 */
public class CustomAlertImageDialog extends Dialog {
	Context mContext;

	private ImageView mImageIconView;
	private TextView mTitleTextView;
	private TextView mMessageTextView;
    private Button closeBtn, noBtn, yesBtn;

	private int mImageIcon;
	private String mTitle;
    private Spanned mMessage;

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

        setContentView(R.layout.custom_dialog_icon_message);

        setLayout(isConfirm);

		setImageIcon(mImageIcon);
		setTitleText(mTitle);
		setMessageText(mMessage);

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

	public CustomAlertImageDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		// TODO Auto-generated constructor stub

		mContext = context;
	}

	public void setImage(int resId) {
		this.mImageIcon = resId;
	}

	public void setTitle(String content) {
		this.mTitle = content;
	}
	
	public void setMessage(String content) {
		if(!TextUtils.isEmpty(content)) content = content.replaceAll("\n","<br/>");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			this.mMessage = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
		} else {
			this.mMessage = Html.fromHtml(content);
		}
    }

	public void setMessage(Spanned content) {
		this.mMessage = content;
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

	private void setImageIcon(int iconRes) {
		if(iconRes>0) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				mImageIconView.setImageDrawable(mContext.getResources().getDrawable(iconRes, null));
			} else {
				mImageIconView.setImageDrawable(mContext.getResources().getDrawable(iconRes));
			}

			mImageIconView.setVisibility(View.VISIBLE);
		} else {
			mImageIconView.setVisibility(View.GONE);
		}
	}

	private void setTitleText(String content){
		if(!TextUtils.isEmpty(content))
			mTitleTextView.setText(content);
		else
			mTitleTextView.setVisibility(View.GONE);
	}

    private void setMessageText(Spanned content){
    	if(!TextUtils.isEmpty(content.toString()))
    		mMessageTextView.setText(content);
    	else
    		mMessageTextView.setVisibility(View.GONE);
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

		mImageIconView = (ImageView)findViewById(R.id.icon_message_icon);

        mTitleTextView = (TextView)findViewById(R.id.icon_message_title);
		mMessageTextView = (TextView)findViewById(R.id.icon_message_text);
        
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
