package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

// Custom implementation of the JLable, for displaying the image centered and at a specific size.
@SuppressWarnings("serial")
public class JImageLabel extends JLabel {
	ImageIcon imageIcon;
	int x = 0, y = 0, w = 0, h = 0, r = 0;

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

	public void setImagePosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setImageRotation(int r) {
		this.r = r;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (imageIcon == null) return;

		// Determens the position of the left top corner for the image to be centered
		int nx = (int) ((getWidth() - w) / 2) + x;
		int ny = (int) ((getHeight() - h) / 2) + y;

		Graphics2D g2d = (Graphics2D) g;
		AffineTransform trans = new AffineTransform();

		trans.translate(nx + x, ny + y);
		trans.scale((double) w / (double) imageIcon.getIconWidth(), (double) h / (double) imageIcon.getIconHeight());

		trans.rotate(Math.toRadians(r), imageIcon.getIconWidth() / 2, imageIcon.getIconHeight() / 2);

		g2d.drawImage(imageIcon.getImage(), trans, this);

	}
}