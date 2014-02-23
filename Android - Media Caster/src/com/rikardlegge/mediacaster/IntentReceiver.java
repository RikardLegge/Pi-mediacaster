package com.rikardlegge.mediacaster;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
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

	/*
	 * BYTE VALUES ONLY
	 * Id declaration
	 * 11: Image
	 * 21: Image url
	 * 22: Video url
	 * 23: Youtube url
	 * 254: Stops the server
	 * Other: Clears the screen
	 */

	// Code startup
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load settings specified in client aplication
		// Needs to be called for the settingsvariables to be set.
		new Settings(this.getSharedPreferences("Settings", Context.MODE_PRIVATE), this);
		sendHandle = new SendHandle();

		// Declare variables for easy use later
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		boolean exit = false;
		if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)) && type != null) {
			if (type.startsWith("image/")) { // If the content is an image
				handleSendImage(intent);

			} else if (type.startsWith("text/")) { // If the content is an url
				String str = intent.getExtras().getString(Intent.EXTRA_TEXT);
				if (str.contains("www.youtube.com/watch")) {
					str = str.split("&feature=", 1)[0];
					handleSendText(str, 23); // Send youtube link( id: 23)

				} else
					handleSendText(str, 21); // Send image link( id: 21)

			} else if (type.startsWith("video/")) {// If the content is an video
													// url
				String str = intent.getDataString();
				handleSendText(str, 22);// Send video link( id: 22)
			} else {
				exit = true; // set to will exit
			}
			// Currently not implemented
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {

		} else {
			exit = true; // set to will exit
		}
		if (exit)
			System.exit(-1);
	}

	void handleSendImage(Intent intent) {
		final Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM); // Extract
																					// the
																					// image
																					// URI
		if (imageUri != null) {
			try {
				// Get the images inputstream and send that data.
				sendHandle.sendData((byte) 11, getContentResolver().openInputStream(imageUri), true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	void handleSendText(String str, int id) {
		// Conversion from char to byte
		char[] chars = str.toCharArray();
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) {
			bytes[i] = (byte) chars[i];
		}
		// Create an inputstream of the text bytes to send.
		ByteArrayInputStream iStream = new ByteArrayInputStream(bytes);
		// Sends the data from the previosly created inputstream
		sendHandle.sendData((byte) id, iStream, true);
	}

	static void requestExit() {
		System.exit(0);
	}
}