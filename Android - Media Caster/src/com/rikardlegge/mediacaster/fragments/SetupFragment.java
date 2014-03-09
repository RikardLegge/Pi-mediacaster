package com.rikardlegge.mediacaster.fragments;

import com.rikardlegge.mediacaster.MainActivity;
import com.rikardlegge.mediacaster.R;
import com.rikardlegge.mediacaster.helpers.Commandid;
import com.rikardlegge.mediacaster.helpers.Settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SetupFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_setup, container, false);

		// Caches some of the View content for later use. EX. set and reads of
		// data.
		final TextView tv_ip = (TextView) root.findViewById(R.id.EditText_IP);
		final TextView tv_port = (TextView) root.findViewById(R.id.EditText_Port);
		final TextView tv_key = (TextView) root.findViewById(R.id.EditText_Key);
		final Button clearDisplay = (Button) root.findViewById(R.id.Button_ClearDisplay);

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
					InputMethodManager imm = (InputMethodManager) Settings.context.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		};

		OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				clearDisplay();
			}
		};

		clearDisplay.setOnClickListener(clickListener);

		tv_ip.setText(Settings.ip + "");
		tv_port.setText(Settings.port + "");
		tv_key.setText(Settings.key + "");

		tv_ip.setOnEditorActionListener(onEditorActionListener);
		tv_port.setOnEditorActionListener(onEditorActionListener);
		tv_key.setOnEditorActionListener(onEditorActionListener);

		return root;
	}

	// Connected to the views shutdownbutton / Currently hidden
	public void shutDown() {
		MainActivity.sendHandle.sendCommand((byte) Commandid.Quit.Id(), false);
	}

	// Connected to the views shutdownbutton / Sends a clear command
	public void clearDisplay() {
		MainActivity.sendHandle.sendCommand((byte) Commandid.Other.Id(), false);
		MainActivity.GetState();
	}
}
