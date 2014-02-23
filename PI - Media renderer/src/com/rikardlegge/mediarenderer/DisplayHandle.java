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
	private static final int serverPort = 5000;
	private static final int socketBufferSize = 65536;
	private static final int maxDisplayTime = 60 * 60;

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

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				initiateExit();
			}
		});

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
		setUndecorated(true);
		setResizable(false);
		setSize(Main.getScreenDimensions());
		setVisible(true);
		setIgnoreRepaint(true);

		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		setCursor(blankCursor);

		panel.setBackground(Color.BLACK);
		StateLable = new JLabel("");
		StateLable.setForeground(Color.DARK_GRAY);
		panel.add(StateLable);
		panel.repaint();

		Thread thread = new Thread(new Runnable() {
			public void run() {
				listenForConnection();
			}
		});
		thread.start();

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

	private void initiateExit() {
		exit = true;
		System.exit(0);
	}

	private void keepAwake() {
		if (Main.robot != null) {
			Point loc = MouseInfo.getPointerInfo().getLocation();
			Main.robot.mouseMove(loc.x + 1, loc.y);
			Main.robot.mouseMove(loc.x, loc.y);
		}
	}

	private void animateUI() {

		if (System.currentTimeMillis() - lastUpdate > maxDisplayTime * 1000) {
			lastUpdate = System.currentTimeMillis();
			setDrawState(State.idle);
		}

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
			return;
		default:
			break;
		}

		for (int i = 0; i < animationFrame % 4; i++)
			txt += ".";
		StateLable.setText(txt);
		StateLable.repaint();
	}

	private void setDrawState(State state) {
		this.state = state;
		boolean toDisplay = (state == State.displaying) ? false : true;
		if (toDisplay)
			reciverHandle.Clear();
		animateUI();
	}

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

	public void listenForConnection() {
		try {
			socket = new ServerSocket(serverPort);
			while (!exit) {
				try {

					Socket clientSocket = socket.accept();

					lastUpdate = System.currentTimeMillis();

					BufferedInputStream dis = new BufferedInputStream(clientSocket.getInputStream(), socketBufferSize);
					int packageContent = dis.read();

					/*
					 * 11: Image
					 * 21: ImageURL
					 * 22: VideoURL
					 * 254: Stop
					 * Other: Clear
					 */

					BufferedImage img;
					String str;
					System.out.println("Recived:" + packageContent);

					if (state == State.omxplayer) {
						shell.executeCommand("killall -9 /usr/bin/omxplayer.bin");
					}

					switch (packageContent) {
					case 11:
						setDrawState(State.downloading);
						img = ImageIO.read(dis);
						setDrawState(State.displaying);
						if (img != null) {
							reciverHandle.RecivedImage(img);
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
						str = readByteStream(dis);
						if (str != null) {
							panel.repaint();
							setDrawState(State.omxplayer);
							shell.startProcess("omxplayer -o hdmi \"" + str + "\"");
						} else
							setDrawState(State.error);
						break;
					case 23:
						str = readByteStream(dis);
						if (str != null) {
							panel.repaint();
							setDrawState(State.omxplayer);
							shell.executeCommand("omxplayer -o hdmi $(youtube-dl -s -g \"" + str + "\")");
						} else
							setDrawState(State.error);
						break;
					case 254:
						initiateExit();
						break;
					default:
						setDrawState(State.idle);
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
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}