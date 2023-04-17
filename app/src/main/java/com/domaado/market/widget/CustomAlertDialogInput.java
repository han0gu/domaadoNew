package com.domaado.market.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.domaado.market.R;

import java.text.DecimalFormat;


public class CustomAlertDialogInput extends Dialog {
	
	final String TAG = CustomAlertDialogInput.class.getSimpleName();

    Context mContext;

    private TextView mContentView;
    private Button closeBtn, noBtn, yesBtn;
    private Spanned mContent;

    private String closeBtnText, noBtnText, yesBtnText;

    private View.OnClickListener[] listeners;

    private View.OnClickListener mCloseClickListener;	// close
    private View.OnClickListener mNoClickListener;	// no
    private View.OnClickListener mYesClickListener;	// yes

    private EditText dialogValue;

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

        setContentView(R.layout.custom_dialog_input);

        setLayout(isConfirm);

        setBodyMessageView(mContent);
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

	public CustomAlertDialogInput(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		// TODO Auto-generated constructor stub
		
		mContext = context;
	}
	
	public CustomAlertDialogInput(Context context, String mesg, View.OnClickListener... listener) {
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

    private void setBodyMessageView(Spanned content){
        if(!TextUtils.isEmpty(content))
            mContentView.setText(content);
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

        dialogValue = (EditText)findViewById(R.id.custom_dialog_value);
    }

    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private String inputValue;

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(!TextUtils.isEmpty(charSequence.toString()) && !charSequence.toString().equals(inputValue)){
                inputValue = charSequence.toString().replaceAll("[^0-9]", "");
                inputValue = decimalFormat.format(Double.parseDouble(inputValue));
                dialogValue.setText(inputValue);
                dialogValue.setSelection(inputValue.length());
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void setInputTypeDecimal() {
        dialogValue.addTextChangedListener(watcher);
        dialogValue.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

//
//	public void setCustomAlertDialog(String mesg, String[] btnTexts, View.OnClickListener... listener) {
//		setCustomAlertDialog(mContext.getResources().getString(R.string.app_name), mesg, btnTexts, listener);
//
//	}
//
//	public void setCustomAlertDialog(String title, String mesg, String[] btnTexts, View.OnClickListener... listener) {
//
//		this.mTitle = title;
//		this.mContent = mesg;
//
//		if(btnTexts != null) {
//			switch(btnTexts.length) {
//			case 1:
//				this.leftBtnText = btnTexts[0];
//				break;
//			case 2:
//				this.leftBtnText = btnTexts[0];
//				this.rightBtnText = btnTexts[1];
////				noBtn.setVisibility(View.VISIBLE);
//				break;
//			case 3:
//				this.leftBtnText = btnTexts[0];
//				this.centerBtnText = btnTexts[1];
//				this.rightBtnText = btnTexts[2];
////				neutralBtn.setVisibility(View.VISIBLE);
////				noBtn.setVisibility(View.VISIBLE);
//				break;
//			}
//		}
//
//		this.listeners = listener;
//	}
//
//    private void setBodyMessageView(Spanned content){
//        if(!TextUtils.isEmpty(content))
//            mContentView.setText(content);
//        else
//            mContentView.setVisibility(View.GONE);
//    }

//    public void setMessage(String content) {
//        if(!TextUtils.isEmpty(content)) {
//            content = content.replaceAll("\n", "<br/>");
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                this.mContent = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
//            } else {
//                this.mContent = Html.fromHtml(content);
//            }
//        } else {
//            this.mContent = new SpannableString("Empty message!");
//        }
//    }
//
//    public void setCloseButton(String btnText, View.OnClickListener listener) {
//        setPositiveButton(btnText, listener);
//    }
//
//    public void setPositiveButton(String btnText, View.OnClickListener listener) {
//        this.closeBtnText = btnText;
//        this.mCloseClickListener = listener;
//    }
//
//    public void setNoButton(String btnText, View.OnClickListener listener) {
//        setNegativeButton(btnText, listener);
//    }
//
//    public void setNegativeButton(String btnText, View.OnClickListener listener) {
//        this.noBtnText = btnText;
//        this.mNoClickListener = listener;
//    }
//
//    public void setYesButton(String btnText, View.OnClickListener listener) {
//        setNeutralButton(btnText, listener);
//    }
//
//    public void setNeutralButton(String btnText, View.OnClickListener listener) {
//        this.yesBtnText = btnText;
//        this.mYesClickListener = listener;
//    }
//
//	public void setIcon(int res) {
//		//
//	}
//
//	public void setButtonHeight(int height) {
//		buttonHeight = (int) Common.convertDpToPixel(mContext, height);
//	}
	
//	public int getButtonHeight() {
//		if(okBtn != null) {
//			return okBtn.getHeight();
//		}
//
//		return 0;
//	}
	
//	public void setTitle(String title) {
//		this.mTitle = title;
//	}
//
//	private void setTitleView(String title){
//        mTitleView.setText(title);
//    }
//
//    private void setContentView(String content){
//    	if(!TextUtils.isEmpty(content))
//    		mContentView.setText(content);
//    	else
//    		mContentView.setVisibility(View.GONE);
//    }
//
//    private void setButtonText(String leftBtn, String centerBtn, String rightBtn) {
//    	okBtn.setText(leftBtn);
//    	//neutralBtn.setText(centerBtn);
//    	cancelBtn.setText(rightBtn);
//    }
    
//    private void setClickListener(View.OnClickListener... listener) {
//    	if(listener != null) {
//			switch(listener.length) {
//			case 1:
//				okBtn.setOnClickListener(listener[0]);
//				break;
//			case 2:
//				okBtn.setOnClickListener(listener[0]);
//				cancelBtn.setOnClickListener(listener[1]);
//				cancelBtn.setVisibility(View.VISIBLE);
//				break;
//			case 3:
//				okBtn.setOnClickListener(listener[0]);
//				//neutralBtn.setOnClickListener(listener[1]);
//				cancelBtn.setOnClickListener(listener[2]);
//				//neutralBtn.setVisibility(View.VISIBLE);
//				cancelBtn.setVisibility(View.VISIBLE);
//				break;
//			}
//
//			setButtonWeight(listener.length);
//		}
//    }
    
    /*
     * Layout
     */
//    private void setLayout(){
//        mTitleView = (TextView) findViewById(R.id.custom_dialog_title);
//
//        ImageView custom_dialog_close_btn = (ImageView)findViewById(R.id.custom_dialog_close_btn);
//        custom_dialog_close_btn.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//
//				custom_dialog_value = null;
//				dismiss();
//			}
//        });
//
//        mContentView = (TextView)findViewById(R.id.custom_dialog_mesg);
//
//        custom_dialog_value = (EditText)findViewById(R.id.custom_dialog_value);
//
//        okBtn = (Button)findViewById(R.id.custom_dialog_okbtn);
//		cancelBtn = (Button)findViewById(R.id.custom_dialog_cancelbtn);
//    }


    public void setDefaultValue(String defvalue) {
    	if(dialogValue != null && !TextUtils.isEmpty(defvalue)) {
    		dialogValue.setText(defvalue);
    	}
    }
    
    public String getValue() {
    	String value = dialogValue.getText().toString();
    	value = !TextUtils.isEmpty(value) ? value.replaceAll(",", "") : value;

    	return value;
    }
    
    @Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
	}

//	private void setButtonWeight(int numb) {
//
//    	float weight = 3.0f;	// layout weight sum이 3으로 설정되어 있음!
//    	int margin = (int) Common.convertDpToPixel(mContext, 5);
//
//    	switch(numb) {
//    	case 1:
//    		weight = 3f;
//    		break;
//    	case 2:
//    		weight = 1.5f;
//    		break;
//    	case 3:
//    		weight = 1f;
//    		break;
//    	}
//
//		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) Common.convertDpToPixel(mContext, 35));
//		p.weight = weight;
//		if(buttonHeight>0) p.height = buttonHeight;
//		p.setMargins(margin, margin, margin, margin);
//		p.gravity = Gravity.CENTER_HORIZONTAL;
//
//		okBtn.setLayoutParams(p);
//		//neutralBtn.setLayoutParams(p);
//		cancelBtn.setLayoutParams(p);
//    }

}
