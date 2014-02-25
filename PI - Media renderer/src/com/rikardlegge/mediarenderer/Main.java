package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;

public class Main {
	private static Toolkit toolkit;
	// For global access
	public static Robot robot;

	public static void main(String[] args) {
		// Gets a toolkit for determening the width and height of the display
		toolkit = Toolkit.getDefaultToolkit();
		
		// Gets a robot to move the mouse arround, keeps the screen active.
		try {
			robot = new Robot();
		} catch (AWTException headlessEnvironmentException) {
			robot = null;
		}

		// Creates a new fullscreen window which displays the content
		new DisplayHandle();
	}

	public static Dimension getScreenDimensions() {
		return new Dimension((int) toolkit.getScreenSize().getWidth(), (int) toolkit.getScreenSize().getHeight());
	}
}