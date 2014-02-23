package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DisplayHandle extends JFrame {

	private static final long serialVersionUID = 8879271623173847863L;
	private static final int serverPort = 5000; // Socket variables
	private static final int socketBufferSize = 65536; // Socket variables
	private static final int maxDisplayTime = 60 * 60; // How long will local content be shown. Does not include videos since they are
														// displayed using external player Seconds

	private JPanel panel;
	private ReciverHandle reciverHandle;
	private ServerSocket socket;
	ShellCmd shell;

	private long animationFrame = 0;
	private long lastUpdate = 0;
	private JLabel StateLable;

	private boolean exit = false;
	private State state = State.idle;

	enum State {
		idle, downloading, displaying, error, omxplayer
	}

	public void createWindow() {
		panel = new JPanel(new GridBagLayout());
		reciverHandle = new ReciverHandle(panel);
		shell = new ShellCmd();

		// Use as a service on other systems?
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				initiateExit();
			}
		});

		// Caches the ESC key and exits the program.
		addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 27) {
					initiateExit();
				}
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}
		});

		getContentPane().add(panel);
		setUndecorated(true); // Remove the borders
		setResizable(false); // Static size
		setSize(Main.getScreenDimensions()); // Makes it fullscreen
		setVisible(true); // Displays it
		setIgnoreRepaint(true); // reduces CPU usage since we only need to repaint if something happends

		// Hide cursor
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		setCursor(blankCursor);

		// Initiate the basic UI
		panel.setBackground(Color.BLACK);
		StateLable = new JLabel(""); // Debuglable
		StateLable.setForeground(Color.DARK_GRAY); // Makes it less distinct
		panel.add(StateLable);
		panel.repaint(); // repaints to set the initial content

		// New thread to listen for connections, since this otherwise halts the program.
		Thread thread = new Thread(new Runnable() {
			public void run() {
				listenForConnection();
			}
		});
		thread.start();

		// Infinite loop to animate the UI, aswell as handles other things.
		// Delay: 2000 ms.
		while (!exit) {
			animateUI();
			animationFrame++;
			keepAwake();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	// Exits the program.
	private void initiateExit() {
		exit = true;
		System.exit(0);
	}

	// Keep the screen awake by moving the mouse on every animation frame
	private void keepAwake() {
		if (Main.robot != null) {
			Point loc = MouseInfo.getPointerInfo().getLocation();
			Main.robot.mouseMove(loc.x + 1, loc.y);
			Main.robot.mouseMove(loc.x, loc.y);
		}
	}

	// Animate / Display messages which might help see if the system is responsive.
	private void animateUI() {

		if (System.currentTimeMillis() - lastUpdate > maxDisplayTime * 1000) {
			lastUpdate = System.currentTimeMillis();
			setDrawState(State.idle);
		}

		// Sets the text depending on the state
		String txt = "Unknown state";
		switch (state) {
		case idle:
			txt = "Waiting for media";
			break;
		case downloading:
			txt = "Loading";
			break;
		case error:
			txt = "Error loading content";
			break;
		case displaying:
			return;
		case omxplayer:
			txt = "Loading video";
			break;
		default:
			break;
		}

		// Loadingish...
		for (int i = 0; i < animationFrame % 4; i++)
			txt += ".";
		StateLable.setText(txt);
		// Only repaint the lable
		StateLable.repaint();
	}

	// Sets the current STATE of the program
	private void setDrawState(State state) {
		this.state = state;
		// Clear if not displaying new image
		boolean toDisplay = (state == State.displaying) ? false : true;
		if (toDisplay)
			reciverHandle.Clear();
		animateUI();
	}

	// Reads a byteStream. Simple way of reading for example a URL from the socket
	public String readByteStream(BufferedInputStream dis) {
		String str = "";
		int byt;
		try {
			while ((byt = dis.read()) != -1) {
				str += ((char) (byte) byt);
			}
			return str;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// RUNTIME: Infinite loop to listen for a connection and hadle it.
	public void listenForConnection() {
		try {
			socket = new ServerSocket(serverPort);
			while (!exit) {
				try {

					/*
					 * BYTE VALUES ONLY
					 * 11: Image
					 * 21: ImageURL
					 * 22: VideoURL
					 * 254: Stop
					 * Other: Clear
					 */

					Socket clientSocket = socket.accept(); // Wait for the socket to get a connection

					lastUpdate = System.currentTimeMillis(); // Sets the last update which is used to dim the display if idle

					BufferedInputStream dis = new BufferedInputStream(clientSocket.getInputStream(), socketBufferSize);
					int packageContent = dis.read(); // Package content id, Se previous table for explanation

					BufferedImage img; // Cache for if recived image
					String str; // URL Cache
					System.out.println("Recived:" + packageContent);

					if (state == State.omxplayer) {
						shell.executeCommand("killall -9 /usr/bin/omxplayer.bin"); // Command to kill the current video process, omxplayer
																					// (Only tested on a raspberry pi)
					}

					switch (packageContent) {
					case 11:
						setDrawState(State.downloading); // Changes the draw state
						img = ImageIO.read(dis); // Loads the image from the sending device
						setDrawState(State.displaying); // Change state to prevent repaint
						if (img != null) {
							reciverHandle.RecivedImage(img); // Displays the recently loaded image
						} else
							setDrawState(State.error);
						break;
					case 21:
						setDrawState(State.downloading);
						str = readByteStream(dis);
						img = null;
						if (str != null) {
							img = reciverHandle.GetImageFromURL(str);
							setDrawState(State.displaying);
						}
						if (img != null)
							reciverHandle.RecivedImage(img);
						else
							setDrawState(State.error);
						break;
					case 22:
						str = readByteStream(dis); // Get URL
						if (str != null) {
							panel.repaint();
							setDrawState(State.omxplayer);
							shell.startProcess("omxplayer -o hdmi \"" + str + "\""); // Command to start omxplayer on the raspberry pi with
																						// hdmi as the sound output, using the current url
																						// as a source
						} else
							setDrawState(State.error);
						break;
					case 23:
						str = readByteStream(dis); // Get URL
						if (str != null) {
							panel.repaint();
							setDrawState(State.omxplayer);
							shell.executeCommand("omxplayer -o hdmi $(youtube-dl -s -g \"" + str + "\")"); // See case 22:. + uses the
																											// youtube-dl to fech the true
																											// video url.
						} else
							setDrawState(State.error);
						break;
					case 254:
						initiateExit(); // Exits the program
						break;
					default:
						setDrawState(State.idle); // Stops all and makes the program idle
						break;
					}
					dis.close();
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
					setDrawState(State.error);
				}
				Thread.yield();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			socket.close(); // Closes the socket on exit
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}