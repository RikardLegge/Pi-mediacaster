package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;


// Custom implementation of the JLable, for displaying the image centered and at a specific size.
@SuppressWarnings("serial")
public class JImageLabel extends JLabel {
	ImageIcon imageIcon;
	Image image;
	int x, y, w, h;

	public JImageLabel(ImageIcon icon) {
		super();
		this.imageIcon = icon;
	}

	public void setImage(ImageIcon icon) {
		this.imageIcon = icon;
	}

	// Custom for settings the exact image size
	public void setImageSize(int w, int h) {
		this.w = w;
		this.h = h;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Determens the position of the left top corner for the image to be
		// centered
		x = (int) ((getWidth() - w) / 2);
		y = (int) ((getHeight() - h) / 2);

		// Draw if image exists
		if (imageIcon != null) g.drawImage(imageIcon.getImage(), x, y, w, h, this);

	}
}