package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class DisplayHandle extends JFrame {

	private static final int serverPort = 5000; // Socket variables
	private static final int socketBufferSize = 65536; // Socket variables
	private static final int maxDisplayTime = 60 * 60; // How long will local content be shown. Does not include videos
														// since they are
														// displayed using external player Seconds

	private JPanel panel;
	private ReciverHandle reciverHandle;
	ShellCmd shell;

	private long animationFrame;
	private long lastUpdate;
	private JLabel StateLable;

	private boolean exit;
	private State state;
	private String stateMessage;

	// The states of the program
	enum State {
		idle, downloading, displaying, error, video
	}

	// Creation
	DisplayHandle() {
		super();

		panel = new JPanel(new BorderLayout());// JPanel(new GridBagLayout());
		reciverHandle = new ReciverHandle(panel);
		shell = new ShellCmd();

		animationFrame = 0;
		lastUpdate = 0;
		exit = false;
		state = State.idle;

		initiateShell();
		setupUI();
		hideCursor();

		// New thread to listen for connections, since this otherwise halts the program.
		Thread thread = new Thread(new Runnable() {
			public void run() {
				listenForConnection();
				shell.executeCommand("sh helper.sh rmfifo");
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

	private void initiateShell() {
		shell.executeCommand("sh helper.sh mkfifo");
	}

	// Sets up the Interface
	private void setupUI() {
		// Use as a service on other systems?
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				initiateExit();
			}
		});

		// Waits for the ESC key and exits the program.
		addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 27) {
					initiateExit();
				}
			}

			public void keyTyped(KeyEvent e) {}

			public void keyPressed(KeyEvent e) {}
		});

		// Sets up the window
		getContentPane().add(panel);
		setUndecorated(true); // Remove the borders
		setResizable(false); // Static size
		setSize(Main.getScreenDimensions()); // Makes it fullscreen
		setVisible(true); // Displays it
		setIgnoreRepaint(true); // reduces CPU usage since we only need to repaint if something happends

		// Initiate the basic UI
		panel.setBackground(Color.BLACK);
		StateLable = new JLabel(""); // Debuglable
		StateLable.setForeground(Color.GRAY); // Makes it less distinct
		StateLable.setVerticalAlignment(SwingConstants.BOTTOM);
		StateLable.setHorizontalAlignment(SwingConstants.LEFT);
		StateLable.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)));

		panel.add(StateLable);
		panel.repaint(); // repaints to set the initial content
	}

	// Keep the screen awake by moving the mouse on every animation frame
	private void keepAwake() {
		if (Main.robot != null) {
			try {
				Point loc = MouseInfo.getPointerInfo().getLocation();
				Main.robot.mouseMove(loc.x + 1, loc.y);
				Main.robot.mouseMove(loc.x, loc.y);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	// Makes the cursor invisible
	private void hideCursor() {
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		setCursor(blankCursor);
	}

	// Request exit for the program
	private void initiateExit() {
		exit = true;
		System.exit(0);
	}

	// Animate / Display messages which might help see if the system is responsive
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
				txt = "";
			break;
			case video:
				txt = "Loading video";
			break;
			default:
			break;
		}
		if (stateMessage != null) txt += ": " + stateMessage;

		// Loadingish...
		if (state != State.error) for (int i = 0; i < animationFrame % 4; i++)
			txt += ".";
		// Only repaint the lable
		StateLable.setText(txt);
		StateLable.repaint();
	}

	// Sets the current STATE of the program
	private void setDrawState(State state, String stateMessage) {
		this.state = state;
		this.stateMessage = stateMessage;
		if (state != State.displaying) reciverHandle.Clear();
		animateUI();
	}

	private void setDrawState(State state) {
		setDrawState(state, null);
	}

	private void setDrawStateMessage(String stateMessage) {
		this.stateMessage = stateMessage;
		animateUI();
	}

	// Reads a byteStream. Simple way of reading for example a URL from the socket
	public String readByteStream(BufferedInputStream dis) throws Exception {
		String str = "";
		int byt;
		while ((byt = dis.read()) != -1) {
			str += ((char) (byte) byt);
		}
		return str;
	}

	private interface ReciverPreHandle {
		void read(BufferedInputStream dis) throws Exception;
	}

	ReciverPreHandle RPH_Other = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			setDrawState(State.idle);
		}
	};

	ReciverPreHandle RPH_Image = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			BufferedImage img = ImageIO.read(dis); // Loads the image from the sending device

			if (img == null) throw new Exception("Unable to fech image");

			setDrawState(State.displaying); // Change state to prevent repaint
			reciverHandle.RecivedImage(img); // Displays the recently loaded image
		}
	};

	ReciverPreHandle RPH_URL_Image = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			BufferedImage img = null;
			setDrawStateMessage(str);

			if (str != null) img = reciverHandle.GetImageFromURL(str);
			if (img == null) throw new Exception("No image found at the specified URL");

			setDrawState(State.displaying);
			reciverHandle.RecivedImage(img);
		}
	};

	ReciverPreHandle RPH_URL_Video = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			if (str == null) throw new Exception("Unable to recive the video URL");

			panel.repaint();
			setDrawState(State.video, str);

			// Command to start omxplayer on the raspberry pi with hdmi as the sound output, using the
			// current url as a source
			// shell.startProcess("omxplayer -o hdmi \"" + str + "\"");
			shell.executeCommand("sh helper.sh omxplayer " + str);
		}
	};

	ReciverPreHandle RPH_URL_Youtube = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			if (str == null) throw new Exception("Unable to recive the video URL");

			panel.repaint();
			setDrawState(State.video, str);

			// Command to start omxplayer on the raspberry pi with hdmi as the sound output, using the
			// current url as a source
			// + uses the youtube-dl to fech the true video url.
			// shell.executeCommand("sh ./youtube.sh " + str);
			shell.executeCommand("sh helper.sh omxplayer_youtube " + str);
			// shell.startProcess(new String[]{"omxplayer","-o", "hdmi", "$(youtube-dl -s -g "+str+")"});
		}
	};

	ReciverPreHandle RPH_Video_Controll = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			if (str == null) throw new Exception("Unable to recive the video URL");

			switch (str) {
				case "pause":
					shell.executeCommand("sh helper.sh omxplayer_pause");
				break;
				case "seek-30":
					shell.executeCommand("sh helper.sh omxplayer_seek-30");
				break;
				case "seek30":
					shell.executeCommand("sh helper.sh omxplayer_seek30");
				break;
				case "seek-600":
					shell.executeCommand("sh helper.sh omxplayer_seek-600");
				break;
				case "seek600":
					shell.executeCommand("sh helper.sh omxplayer_seek600");
				break;
				case "quit":
					shell.executeCommand("sh helper.sh omxplayer_quit");
				break;
				case "forcequit":
					shell.executeCommand("killall -9 /usr/bin/omxplayer.bin");
					shell.executeCommand("killall -9 youtube-dl");
				break;
			}
		}
	};

	ReciverPreHandle RPH_Command = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			if (str == null) throw new Exception("Unable to recive command");

			panel.repaint();
			setDrawState(State.idle, str);
			shell.executeCommand(str); // Disabled for security reasons
		}
	};

	ReciverPreHandle RPH_Quit = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			initiateExit();
		}
	};

	// RUNTIME: Infinite loop to listen for a connection and hadle it.
	public void listenForConnection() {
		ServerSocket socket;
		try {
			socket = new ServerSocket(serverPort);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		ReciverPreHandle[] handles = new ReciverPreHandle[255];
		handles[Commandid.Image.Id()] = RPH_Image;
		handles[Commandid.URL_Image.Id()] = RPH_URL_Image;
		handles[Commandid.URL_Video.Id()] = RPH_URL_Video;
		handles[Commandid.URL_Youtube.Id()] = RPH_URL_Youtube;
		handles[Commandid.Video_Controll.Id()] = RPH_Video_Controll;
		handles[Commandid.Other.Id()] = RPH_Other;
		handles[Commandid.Command.Id()] = RPH_Command;
		handles[Commandid.Quit.Id()] = RPH_Quit;

		while (!exit) {
			Socket clientSocket = null;
			BufferedInputStream dis = null;
			try {
				clientSocket = socket.accept(); // Wait for the socket to get a connection
				dis = new BufferedInputStream(clientSocket.getInputStream(), socketBufferSize);
				lastUpdate = System.currentTimeMillis();

				int packageId = dis.read();
				System.out.println("Recived: " + Commandid.getById(packageId));

				if (handles[packageId] == null) packageId = Commandid.Other.Id();

				if (Commandid.getById(packageId).Clear()) {
					if (state == State.video) {
						// Command to kill the current video process, omxplayer (Only tested on a raspberry pi)
						shell.executeCommand("killall -9 youtube-dl");
						Thread.sleep(100);
						shell.executeCommandAndWaitForTermination("killall -9 /usr/bin/omxplayer.bin");
						Thread.sleep(5000); // Make sure to not kill a new instance of the player.
					}
					setDrawState(State.downloading);
				}
				handles[packageId].read(dis);
			} catch (Exception e) {
				e.printStackTrace();
				setDrawState(State.error, e.getMessage());
			}

			try {
				if (dis != null) dis.close();
				if (clientSocket != null) clientSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.yield();
		}

		try {
			socket.close(); // Closes the socket on exit
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @deprecated
	 */
	public void listenForConnection_deprecated() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(serverPort);
			while (!exit) {
				try {

					/*
					 * BYTE VALUES ONLY
					 * 11: Image
					 * 21: ImageURL
					 * 22: VideoURL
					 * 23: YoutubeURL
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

					if (state == State.video) {
						shell.executeCommandAndWaitForTermination("killall -9 /usr/bin/omxplayer.bin"); // Command to kill the current video
																										// process, omxplayer
						// (Only tested on a raspberry pi)
					}

					switch (packageContent) {
						case 11:
							setDrawState(State.downloading); // Changes the draw state
							img = ImageIO.read(dis); // Loads the image from the sending device
							setDrawState(State.displaying); // Change state to prevent repaint
							if (img != null) {
								reciverHandle.RecivedImage(img); // Displays the recently loaded image
							} else setDrawState(State.error);
						break;
						case 21:
							setDrawState(State.downloading);
							str = readByteStream(dis);
							img = null;
							if (str != null) {
								img = reciverHandle.GetImageFromURL(str);
								setDrawState(State.displaying);
							}
							if (img != null) reciverHandle.RecivedImage(img);
							else setDrawState(State.error);
						break;
						case 22:
							str = readByteStream(dis); // Get URL
							if (str != null) {
								panel.repaint();
								setDrawState(State.video);
								shell.startProcess("omxplayer -o hdmi \"" + str + "\""); // Command to start omxplayer on the raspberry
																							// pi
																							// with
																							// hdmi as the sound output, using the
																							// current
																							// url
																							// as a source
							} else setDrawState(State.error);
						break;
						case 23:
							str = readByteStream(dis); // Get URL
							if (str != null) {
								panel.repaint();
								setDrawState(State.video);
								shell.executeCommand("omxplayer -o hdmi $(youtube-dl -s -g \"" + str + "\")"); // See case 22:. + uses the
																												// youtube-dl to fech the
																												// true
																												// video url.
							} else setDrawState(State.error);
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
				} catch (Exception e) {
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