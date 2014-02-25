package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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

	/**
	 * Displays an image on the screen usin a custom JLable (JImageLable)
	 * 
	 * @param img
	 */
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

	public static Dimension scaleToScreen_Full(float sw, float sh, float gw, float gh) {
		if (sw / sh > gw / gh) {
			return new Dimension((int) gw, (int) (gw / sw * sh));
		} else {
			return new Dimension((int) (gh / sh * sw), (int) gh);
		}
	}
	
	public static Dimension scaleToScreen_Full_Inverse(float sw, float sh, float gw, float gh) {
		if (sw / sh < gw / gh) {
			return new Dimension((int) gw, (int) (gw / sw * sh));
		} else {
			return new Dimension((int) (gh / sh * sw), (int) gh);
		}
	}

	/**
	 * Gets an image from the url specified
	 * 
	 * @param An
	 *            url to fech the image from
	 * @return The image from the url
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public BufferedImage GetImageFromURL(String str) throws MalformedURLException, IOException {
		return ImageIO.read(new URL(str)); // Very basic function for getting the image. Not very safe, but works
	}

	/**
	 * Clears the screen
	 */
	public void Clear() {
		if (itemContainer.imageIcon != null) {
			itemContainer.imageIcon.getImage().flush();
			itemContainer.imageIcon = null;
		}
	}
}
