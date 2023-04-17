package com.domaado.market.widget;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.domaado.market.R;


public class ShowAlertDialogActivity extends FragmentActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // create the builder

        AlertDialog alert = builder.create();
	}
}
