package com.rikardlegge.mediacaster;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	// Static variables for global access
	static int port;
	static int bufferSize;
	static String ip;
	static SharedPreferences prefs;

	static Context context;

	Settings(SharedPreferences newPrefs, Context newContext) {
		// basic settup. Needs to be called for the variables to be set. 
		prefs = newPrefs;
		context = newContext;
		readSettings();
	}

	static void readSettings() {
		// Returns the saved values or the defaults:
		// Port: 5000
		// Buffer size: 65536
		// Ip: 192.168.0.100
		port = prefs.getInt("port", 5000);
		bufferSize = prefs.getInt("bufferSize", 65536);
		ip = prefs.getString("ip", "192.168.0.100");
	}

	static void writeSettings() {
		// Saves the variables for later use
		prefs.edit().putInt("port", port);
		prefs.edit().putInt("bufferSize", bufferSize);
		prefs.edit().putString("pi", ip);
		prefs.edit().commit();
	}
}
