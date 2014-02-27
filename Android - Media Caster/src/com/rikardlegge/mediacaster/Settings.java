package com.rikardlegge.mediacaster;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */
import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	// Static variables for global access
	static int port;
	static int bufferSize;
	static String ip;
	static String key;
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
		key = prefs.getString("key", "psw");
	}

	static void writeSettings() {
		// Saves the variables for later use
		prefs.edit().putInt("port", port);
		prefs.edit().putInt("bufferSize", bufferSize);
		prefs.edit().putString("pi", ip);
		prefs.edit().putString("key", key);
		prefs.edit().commit();
	}
}
