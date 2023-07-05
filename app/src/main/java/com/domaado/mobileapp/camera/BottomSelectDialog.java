package com.domaado.mobileapp.camera;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.R;
import com.domaado.mobileapp.widget.DialogCommon;

/**
 * Created by kbins(James Hong) on 2022,1월,18
 */
public class BottomSelectDialog extends Dialog  {

    private String TAG = BottomSelectDialog.class.getSimpleName();
    private Context context;

    public static class BottomSetupData {
        int title;
        int icon;
        int text;
        int confirm_btn;
        int cancel_btn;

        public int getTitle() {
            return title;
        }

        public void setTitle(int title) {
            this.title = title;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public int getText() {
            return text;
        }

        public void setText(int text) {
            this.text = text;
        }

        public int getConfirm_btn() {
            return confirm_btn;
        }

        public void setConfirm_btn(int confirm_btn) {
            this.confirm_btn = confirm_btn;
        }

        public int getCancel_btn() {
            return cancel_btn;
        }

        public void setCancel_btn(int cancel_btn) {
            this.cancel_btn = cancel_btn;
        }
    }

    public interface BottomSetupDialogListener {
        void onClose();
        void onConfirm();
    }

    private BottomSetupData bottomSetupData;
    private BottomSetupDialogListener bottomSetupDialogListener;

    public BottomSelectDialog(@NonNull Context context, BottomSetupData bottomSetupData) {
        super(context);
        this.context = context;
        this.bottomSetupData = bottomSetupData;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        InsetDrawable inset = new InsetDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT), 0);

        getWindow().setBackgroundDrawable(inset);
        getWindow().setGravity(Gravity.TOP);

        DialogCommon.setMargins(this, 0, 0, 0, 0 );

        setContentView(R.layout.bottom_setup_dialog);

        setUI();

    }

    private void setUI() {

        TextView bottom_setup_message_ti = findViewById(R.id.bottom_setup_message_ti);
        ImageView bottom_setup_message_icon = findViewById(R.id.bottom_setup_message_icon);
        TextView bottom_setup_message = findViewById(R.id.bottom_setup_message);
        Button bottom_setup_confirm_btn = findViewById(R.id.bottom_setup_confirm_btn);
        Button bottom_setup_close_btn = findViewById(R.id.bottom_setup_close_btn);

        if(context!=null && bottomSetupData!=null) {
            bottom_setup_message_ti.setText(context.getResources().getString(bottomSetupData.title));
            bottom_setup_message_icon.setImageResource(bottomSetupData.icon);
            bottom_setup_message.setText(context.getResources().getString(bottomSetupData.text));

            bottom_setup_confirm_btn.setText(context.getResources().getString(bottomSetupData.confirm_btn));
            bottom_setup_close_btn.setText(context.getResources().getString(bottomSetupData.cancel_btn));
        }

        bottom_setup_close_btn.setOnClickListener(v -> {
            if(bottomSetupDialogListener!=null) {
                bottomSetupDialogListener.onClose();
                dismiss();
            }
        });

        bottom_setup_confirm_btn.setOnClickListener(v -> {
            if(bottomSetupDialogListener!=null) {
                bottomSetupDialogListener.onConfirm();
                dismiss();
            }
        });

        findViewById(android.R.id.content).setOnClickListener(v -> {
            dismiss();
        });

        showAnimation(findViewById(R.id.bottom_setup_container));
    }

    public void build(BottomSetupDialogListener listener) {
        this.bottomSetupDialogListener = listener;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                create();
            }

            show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void showAnimation(@NonNull View targetView) {

        float height = (float) targetView.getHeight() + Common.convertDpToPixel(context, 40);

        targetView.clearAnimation();

        TranslateAnimation stranslation = new TranslateAnimation(0f, 0f, height, 0f);
        stranslation.setDuration(300);
        stranslation.setFillAfter(true);
        stranslation.setInterpolator(new AccelerateDecelerateInterpolator());
        stranslation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                animationDrop(main_search_bar);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        targetView.startAnimation(stranslation);
    }

    private void hideAnimation(@NonNull View targetView, Handler handler) {

        float height = (float) targetView.getHeight() + Common.convertDpToPixel(context, 40);

        targetView.clearAnimation();

        TranslateAnimation stranslation = new TranslateAnimation(0f, 0f, 0f, height);
        stranslation.setDuration(300);
        stranslation.setFillAfter(true);
        stranslation.setInterpolator(new AccelerateDecelerateInterpolator());
        stranslation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(handler!=null) handler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        targetView.startAnimation(stranslation);
    }

    @Override
    public void dismiss() {

        if(bottomSetupDialogListener!=null) bottomSetupDialogListener.onClose();

        hideAnimation(findViewById(R.id.bottom_setup_container), new Handler(msg -> {
            super.dismiss();
            return true;
        }));

    }

}
