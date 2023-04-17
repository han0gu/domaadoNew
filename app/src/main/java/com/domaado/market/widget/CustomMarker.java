package com.domaado.market.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.domaado.market.R;


/**
 * Created by jameshong on 2018. 6. 12..
 */

public class CustomMarker extends FrameLayout {

    private LinearLayout layout;
    private ImageView icon;
    private TextView title;
    private TextView markerName;

    public CustomMarker(Context context, String labelName, String title, int iconRes) {

        super(context);

        setPadding(10, 0, 10, 0);
        layout = new LinearLayout(context);
        layout.setVisibility(VISIBLE);

        setupView(context, layout, labelName, iconRes);
        setMarkerName(labelName, title);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.NO_GRAVITY;
        addView(layout, params);
    }

    public CustomMarker(Context context, String labelName, int iconRes) {

        super(context);

        setPadding(10, 0, 10, 0);
        layout = new LinearLayout(context);
        layout.setVisibility(VISIBLE);

        setupView(context, layout, labelName, iconRes);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.NO_GRAVITY;
        addView(layout, params);
    }

    protected void setupView(Context context, final ViewGroup parent, String labelName, int iconRes) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.custom_marker, parent, true);

        icon = view.findViewById(R.id.custom_marker_icon);
        title = view.findViewById(R.id.custom_marker_text);
        markerName = view.findViewById(R.id.custom_marker_name);

        setTitle(labelName);
        setIcon(iconRes);

    }

    public void setMarkerName(String name, String lebel) {
        if(markerName!=null&&title!=null) {
            title.setText(name);
            markerName.setText(lebel);

            markerName.setVisibility(View.VISIBLE);
        }
    }

    public void setTitle(String str) {
        title.setText(str);
    }

    public void setIcon(int res) {
        icon.setImageResource(res);
    }

    public void setOnClickListener(OnClickListener clicklistener) {
        if(layout!=null) layout.setOnClickListener(clicklistener);
    }
}
