package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */

enum Commandid {
	Other(0, true), Image(1, true), URL_Image(11, true), URL_Video(12, true), URL_Youtube(13, true), Video_Controll(252, false), Command(
			253, false), Quit(254, true);

	private int id;
	private boolean clear;

	Commandid(int id, boolean clear) {
		this.id = id;
		this.clear = clear;
	}

	int Id() {
		return this.id;
	}
	
	boolean Clear() {
		return this.clear;
	}

	public static Commandid getById(int id) {
		for (Commandid cmd : Commandid.values()) {
			if (cmd.Id() == id) return cmd;
		}
		return Other;
	}
}