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
import android.widget.LinearLayout;
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

    public interface BottomSetupDialogListener {
        void onClose();
        void onSelectCamera();
        void onSelectGallery();
    }

    private BottomSelectData bottomSelectData;
    private BottomSetupDialogListener bottomSetupDialogListener;

    public BottomSelectDialog(@NonNull Context context, BottomSelectData bottomSelectData) {
        super(context);
        this.context = context;
        this.bottomSelectData = bottomSelectData;
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

        ImageView bottom_select_close = findViewById(R.id.bottom_select_close);

        LinearLayout bottom_select_camera = findViewById(R.id.bottom_select_camera);
        LinearLayout bottom_select_gallery = findViewById(R.id.bottom_select_gallery);

        ImageView bottom_select_camera_icon = findViewById(R.id.bottom_select_camera_icon);
        TextView bottom_select_camera_ti = findViewById(R.id.bottom_select_camera_ti);

        ImageView bottom_select_gallery_icon = findViewById(R.id.bottom_select_gallery_icon);
        TextView bottom_select_gallery_ti = findViewById(R.id.bottom_select_gallery_ti);

        if(context!=null && bottomSelectData!=null) {
            bottom_select_camera_icon.setImageResource(bottomSelectData.cameraIcon);
            bottom_select_camera_ti.setText(context.getResources().getString(bottomSelectData.cameraTi));

            bottom_select_gallery_icon.setImageResource(bottomSelectData.galleryIcon);
            bottom_select_gallery_ti.setText(context.getResources().getString(bottomSelectData.galleryTi));
        }

        bottom_select_close.setOnClickListener(v -> {
            if(bottomSetupDialogListener!=null) {
                bottomSetupDialogListener.onClose();
                dismiss();
            }
        });

        bottom_select_camera.setOnClickListener(v -> {
            if(bottomSetupDialogListener!=null) {
                bottomSetupDialogListener.onSelectCamera();
                dismiss();
            }
        });

        bottom_select_gallery.setOnClickListener(v -> {
            if(bottomSetupDialogListener!=null) {
                bottomSetupDialogListener.onSelectGallery();
                dismiss();
            }
        });

        findViewById(android.R.id.content).setOnClickListener(v -> {
            dismiss();
        });

        showAnimation(findViewById(R.id.bottom_select_container));
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

        hideAnimation(findViewById(R.id.bottom_select_container), new Handler(msg -> {
            super.dismiss();
            return true;
        }));

    }

}
