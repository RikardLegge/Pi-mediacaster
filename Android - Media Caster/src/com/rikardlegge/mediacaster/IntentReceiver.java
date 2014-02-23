package com.rikardlegge.mediacaster;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 *
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class IntentReceiver extends Activity {
	final static int socketBufferSize = 65536;
	SendHandle sendHandle;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_setup);
		new Settings(this.getSharedPreferences("Settings", Context.MODE_PRIVATE), this);
		sendHandle = new SendHandle();

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		boolean exit = false;
		if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)) && type != null) {
			if (type.startsWith("image/")) {
				handleSendImage(intent); // Handle single image being sent
			} else if (type.startsWith("text/")) {
				String str = intent.getExtras().getString(Intent.EXTRA_TEXT);
				if (str.contains("www.youtube.com/watch")){
					str = str.split("&feature=", 1)[0];
					handleSendText(str, 23);
				}else
					handleSendText(str, 21);
			} else if (type.startsWith("video/")) {
				String str = intent.getDataString();
				handleSendText(str, 22);
			} else {
				exit = true;
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				// handleSendMultipleImages(intent);
			} else {
				exit = true;
			}
		} else {
			exit = true;
		}
		if (exit)
			System.exit(-1);
	}

	void handleSendImage(Intent intent) {
		final Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			try {
				sendHandle.sendData((byte) 11, getContentResolver().openInputStream(imageUri), true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	void handleSendText(String str, int id) {
		char[] chars = str.toCharArray();
		byte[] bytes = new byte[chars.length];

		for (int i = 0; i < chars.length; i++) {
			bytes[i] = (byte) chars[i];
		}

		ByteArrayInputStream iStream = new ByteArrayInputStream(bytes);
		sendHandle.sendData((byte) id, iStream, true);
	}

	static void requestExit() {
		System.exit(0);
	}
}