package com.rikardlegge.mediacaster.helpers;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */
import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	// Static variables for global access
	public static int port;
	public static int bufferSize;
	public static String ip;
	public static String key;
	public static Long LastIntentTime;
	public static SharedPreferences prefs;

	public static Context context;

	public Settings(SharedPreferences newPrefs, Context newContext) {
		// basic settup. Needs to be called for the variables to be set.
		prefs = newPrefs;
		context = newContext;
		readSettings();
	}

	public static void readSettings() {
		port = prefs.getInt("port", 5000);
		bufferSize = prefs.getInt("bufferSize", 65536);
		ip = prefs.getString("ip", "192.168.0.100");
		key = prefs.getString("key", "psw");
		LastIntentTime = prefs.getLong("LastIntentTime", 0);
	}

	public static void writeSettings() {
		// Saves the variables for later use
		prefs.edit().putInt("port", port);
		prefs.edit().putInt("bufferSize", bufferSize);
		prefs.edit().putString("pi", ip);
		prefs.edit().putString("key", key);
		prefs.edit().putLong("LastIntentTime", LastIntentTime);
		prefs.edit().commit();
	}
}
