package com.rikardlegge.mediacaster.helpers;

public enum Commandid {
	Other(0, true), Image(1, true), URL_Image(11, true), URL_Video(12, true), URL_Youtube(13, true), GetInfo(250, false), Image_Controll(
			251, false), Video_Controll(252, false), Command(253, false), Quit(254, true);

	private int id;
	private boolean clear;

	Commandid(int id, boolean clear) {
		this.id = id;
		this.clear = clear;
	}

	public int Id() {
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