package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 *
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;

public class Main {
	private static Toolkit toolkit;
	public static Robot  robot;

	public static void main(String[] args) {
		toolkit = Toolkit.getDefaultToolkit();
		try {
			robot = new Robot();
		} catch (AWTException headlessEnvironmentException) {
			robot = null;
		}

		new DisplayHandle().createWindow();
	}

	public static Dimension getScreenDimensions() {
		return new Dimension((int) toolkit.getScreenSize().getWidth(), (int) toolkit.getScreenSize().getHeight());
	}
}