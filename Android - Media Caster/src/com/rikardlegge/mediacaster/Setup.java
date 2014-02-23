package com.rikardlegge.mediacaster;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 *
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Setup extends Activity {
	SendHandle sendHandle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		new Settings(this.getSharedPreferences("Settings", Context.MODE_PRIVATE), this);
		sendHandle = new SendHandle();

		final TextView tv_ip = (TextView) findViewById(R.id.EditText_IP);
		final TextView tv_port = (TextView) findViewById(R.id.EditText_Port);

		System.out.println(Settings.ip + ":" + Settings.port);

		tv_ip.setText(Settings.ip + "");
		tv_port.setText(Settings.port + "");

		tv_ip.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					Settings.prefs.edit().putString("ip", tv_ip.getText().toString()).commit();
					Settings.readSettings();

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		tv_port.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					Settings.prefs.edit().putInt("port", Integer.valueOf(tv_port.getText().toString())).commit();
					Settings.readSettings();

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

	}

	public void shutDown(View view) {
		sendHandle.sendCommand((byte) 254, false);
	}

	public void clearDisplay(View view) {
		sendHandle.sendCommand((byte) 1, false);
	}
}
