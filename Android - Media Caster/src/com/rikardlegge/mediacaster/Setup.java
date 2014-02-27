package com.rikardlegge.mediacaster;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */
import java.io.ByteArrayInputStream;

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

		// Load settings specified in client aplication
		// Needs to be called for the settingsvariables to be set.
		new Settings(this.getSharedPreferences("Settings", Context.MODE_PRIVATE), this);
		sendHandle = new SendHandle();

		// Caches some of the View content for later use. EX. set and reads of
		// data.
		final TextView tv_ip = (TextView) findViewById(R.id.EditText_IP);
		final TextView tv_port = (TextView) findViewById(R.id.EditText_Port);
		final TextView tv_key = (TextView) findViewById(R.id.EditText_Key);

		System.out.println(Settings.ip + ":" + Settings.port);

		OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {

					if (v.equals(tv_ip))
						Settings.prefs.edit().putString("ip", tv_ip.getText().toString()).commit();
					else if (v.equals(tv_port))
						Settings.prefs.edit().putInt("port", Integer.valueOf(tv_port.getText().toString())).commit();
					else if (v.equals(tv_key)) Settings.prefs.edit().putString("key", tv_key.getText().toString()).commit();

					// Saves the settings
					Settings.readSettings();

					// Hide the keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		};

		tv_ip.setText(Settings.ip + "");
		tv_port.setText(Settings.port + "");
		tv_key.setText(Settings.key + "");

		tv_ip.setOnEditorActionListener(onEditorActionListener);
		tv_port.setOnEditorActionListener(onEditorActionListener);
		tv_key.setOnEditorActionListener(onEditorActionListener);
	}

	// Connected to the views shutdownbutton / Currently hidden
	public void shutDown(View view) {
		sendHandle.sendCommand((byte) Commandid.Quit.Id(), false);
	}

	// Connected to the views shutdownbutton / Sends a clear command
	public void clearDisplay(View view) {
		sendHandle.sendCommand((byte) Commandid.Other.Id(), false);
	}

	// Connected to the views shutdownbutton / Sends a clear command
	public void PlayPause(View view) {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("pause").getBytes());
		sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}

	public void forward600s(View view) {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("seek600").getBytes());
		sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}

	public void back600s(View view) {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("seek-600").getBytes());
		sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}

	public void forward30s(View view) {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("seek30").getBytes());
		sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}

	public void back30s(View view) {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new String("seek-30").getBytes());
		sendHandle.sendData((byte) Commandid.Video_Controll.Id(), iStream, false);
	}
}
