package com.rikardlegge.mediacaster;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 *
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
	static int port;
	static int bufferSize;
	static String ip;
	static SharedPreferences prefs;

	static Context context;

	Settings(SharedPreferences newPrefs, Context newContext) {
		prefs = newPrefs;
		context = newContext;
		readSettings();
	}

	static void readSettings() {
		port = prefs.getInt("port", 5000);
		bufferSize = prefs.getInt("bufferSize", 65536);
		ip = prefs.getString("ip", "192.168.0.109");
	}

	static void writeSettings() {
		prefs.edit().putInt("port", port);
		prefs.edit().putInt("bufferSize", bufferSize);
		prefs.edit().putString("pi", ip);
		prefs.edit().commit();
	}
}
