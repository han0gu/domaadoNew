package com.domaado.market.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.domaado.market.R;

import java.util.ArrayList;

/**
 * Created by hongeuichan on 2017. 8. 3..
 */

public class CustomSpinnerAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> data;
    LayoutInflater inflater;
    ArrayList<String> itemValue;
    int textColor;


    public CustomSpinnerAdapter(Context context, ArrayList<String> data, ArrayList<String> itemValue) {
        this.context = context;
        this.data = data;
        this.itemValue = itemValue;
        this.textColor = context.getResources().getColor(R.color.Black);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public CustomSpinnerAdapter(Context context, ArrayList<String> data, ArrayList<String> itemValue, int textColor) {
        this.context = context;
        this.data = data;
        this.itemValue = itemValue;
        this.textColor = textColor;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(data!=null) return data.size();
        else return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            convertView = inflater.inflate(R.layout.custom_spinner_normal, parent, false);
        }

        if(data!=null){
            //데이터세팅
            String text = data.get(position);
            ((TextView)convertView.findViewById(R.id.spinnerText)).setText(text);
            ((TextView)convertView.findViewById(R.id.spinnerText)).setTextColor(textColor);
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = inflater.inflate(R.layout.custom_spinner_dropdown, parent, false);
        }

        //데이터세팅
        String text = data.get(position);
        ((TextView)convertView.findViewById(R.id.spinnerText)).setText(text);
        ((TextView)convertView.findViewById(R.id.spinnerText)).setTextColor(textColor);

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getItemValue(int position) {
        return this.itemValue.get(position).toString();
    }

}
