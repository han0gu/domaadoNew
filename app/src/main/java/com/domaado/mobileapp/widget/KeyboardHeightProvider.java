package com.domaado.mobileapp.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * Created by kbins(James Hong) on 2019,December,06
 */
public class KeyboardHeightProvider extends PopupWindow {
    public KeyboardHeightProvider(Context context, WindowManager windowManager, View parentView, KeyboardHeightListener listener) {
        super(context);

        LinearLayout popupView = new LinearLayout(context);
        popupView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        popupView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);

            Rect rect = new Rect();
            popupView.getWindowVisibleDisplayFrame(rect);

            int keyboardHeight = metrics.heightPixels - (rect.bottom - rect.top);
            int resourceID = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceID > 0) {
                keyboardHeight -= context.getResources().getDimensionPixelSize(resourceID);
            }
            if (keyboardHeight < 100) {
                keyboardHeight = 0;
            }
            boolean isLandscape = metrics.widthPixels > metrics.heightPixels;
            boolean keyboardOpen = keyboardHeight > 0;
            if (listener != null) {
                listener.onKeyboardHeightChanged(keyboardHeight, keyboardOpen, isLandscape);
            }
        });

        setContentView(popupView);

        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setWidth(0);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(new ColorDrawable(0));

        try {
            parentView.post(() -> showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0));
        } catch(Exception e) {
            e.printStackTrace();
        }

//        if (Build.VERSION.SDK_INT > 23) {
//            parentView.post(() -> showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0));
//        } else {
//            parentView.getHandler().post(() -> showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0));
////            new Handler(Looper.getMainLooper()).post(new Runnable() {
////                @Override
////                public void run() {
////                    showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
////                }
////            });
//        }
    }

    public interface KeyboardHeightListener {
        void onKeyboardHeightChanged(int keyboardHeight, boolean keyboardOpen, boolean isLandscape);
    }
}
