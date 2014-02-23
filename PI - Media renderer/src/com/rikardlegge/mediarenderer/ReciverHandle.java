package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ReciverHandle {
	private JPanel panel;
	private Dimension screen;
	private JImageLabel itemContainer;

	ReciverHandle(JPanel panel) {
		this.panel = panel;

		itemContainer = new JImageLabel(null);
		itemContainer.setBackground(Color.WHITE);
		panel.add(itemContainer);
	}

	public void RecivedImage(BufferedImage img) {

		Clear(); // Clears and flushes the previous image is any
		screen = Main.getScreenDimensions();

		// Determends the size to make the image fullscreen at the original
		// aspect ratio. Can be changed to get other effects
		int w = 0, h = 0;
		if (img.getWidth() / img.getHeight() > screen.getWidth() / screen.getHeight()) {
			w = (int) screen.getWidth();
			h = (int) (screen.getWidth() / img.getWidth() * img.getHeight());
		} else {
			w = (int) (screen.getHeight() / img.getHeight() * img.getWidth());
			h = (int) screen.getHeight();
		}

		// Adds an image to the itemcontainer
		itemContainer.setImage(new ImageIcon(img));
		// Set itemcontainer size
		itemContainer.setSize((int) screen.getWidth(), (int) screen.getHeight());
		// Set image size. Custom implementation
		itemContainer.setImageSize(w, h);

		// Repaints the panel / Render the image
		panel.repaint();

		img.flush(); // Get back system resources
	}

	public BufferedImage GetImageFromURL(String str) {
		try {
			return ImageIO.read(new URL(str)); // Very basic function for
												// getting the image. Not very
												// safe but works
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void Clear() {
		if (itemContainer.imageIcon != null) {
			itemContainer.imageIcon.getImage().flush();
			itemContainer.imageIcon = null;
		}
	}

	// Custom implementation of the JLable, for displaying the image centered
	// and at a specific size.
	class JImageLabel extends JLabel {
		private static final long serialVersionUID = -530032801718700014L;
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
			if (imageIcon != null)
				g.drawImage(imageIcon.getImage(), x, y, w, h, this);

		}
	}
}
