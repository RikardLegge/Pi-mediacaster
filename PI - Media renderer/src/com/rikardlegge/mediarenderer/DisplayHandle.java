package com.rikardlegge.mediarenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
	private static final int maxDisplayTime = 60 * 60;
	private static final int breakChar = (byte) '¤' & 0xFF;

	private JPanel panel;
	private ReciverHandle reciverHandle;
	ShellCmd shell;

	private long animationFrame;
	private long lastUpdate;
	private JLabel StateLable;

	private boolean exit;
	private State state;
	private String stateMessage;
	private String validationKey;

	// The states of the program
	enum State {
		idle, downloading, image, error, video;
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
		validationKey = shell.executeCommand("sh helper.sh validationkey").trim();
		validationKey = (validationKey != null) ? validationKey : "_default_";

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
		shell.executeCommand("sh helper.sh rmfifo").length();
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

	private void closeVideo() throws InterruptedException {
		shell.executeCommand("killall -9 youtube-dl");
		Thread.sleep(100);
		shell.executeCommand("killall -9 /usr/bin/omxplayer.bin");
		Thread.sleep(100);
	}

	// Animate / Display messages which might help see if the system is responsive
	private void animateUI() {

		if (System.currentTimeMillis() - lastUpdate > maxDisplayTime * 1000 && state != State.video) {
			lastUpdate = System.currentTimeMillis();
			reciverHandle.Clear();
			setDrawState(State.idle);
		}

		// Sets the text depending on the state
		String txt;
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
			case image:
				txt = "";
			break;
			case video:
				txt = "Loading video";
			break;
			default:
				txt = "Unknown state";
			break;
		}

		if (stateMessage != null) txt += ": " + stateMessage;

		// Loadingish...
		if (state != State.error) for (int i = 0; i < animationFrame % 4; i++)
			txt += ".";

		// Hide if video is running
		if (state == State.video) if (shell.executeCommand("sh helper.sh omxplayer_pool").length() > 2) {
			txt = "";
		} else {
			if (System.currentTimeMillis() - lastUpdate > 60 * 1000) {
				try {
					closeVideo();
				} catch (Exception e) {}

				state = State.idle;
				setDrawStateMessage("");
				txt = "Closing video...";
			}
		}

		// Only repaint the lable
		StateLable.setText(txt);
		StateLable.repaint();
	}

	// Sets the current STATE of the program
	private void setDrawState(State state, String stateMessage) {
		this.state = state;
		this.stateMessage = stateMessage;
		if (state != State.image) reciverHandle.Clear();
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
		while ((byt = dis.read()) != breakChar && byt != -1) {
			str += ((char) (byte) byt);
		}
		return str;
	}

	private interface ReciverPreHandle {
		void read(BufferedInputStream dis) throws Exception;

		void write(BufferedOutputStream dos) throws Exception;
	}

	ReciverPreHandle RPH_Other = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			setDrawState(State.idle);
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_Image = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			BufferedImage img = ImageIO.read(dis); // Loads the image from the sending device

			if (img == null) throw new Exception("Unable to fech image");

			setDrawState(State.image); // Change state to prevent repaint
			reciverHandle.RecivedImage(img); // Displays the recently loaded image
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_URL_Image = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			str = str.substring(0, str.length() - 1);
			BufferedImage img = null;
			setDrawStateMessage(str);

			if (str != null) img = reciverHandle.GetImageFromURL(str);
			if (img == null) throw new Exception("No image found at the specified URL");

			setDrawState(State.image);
			reciverHandle.RecivedImage(img);
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_URL_Video = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			str = str.substring(0, str.length() - 1);
			if (str == null) throw new Exception("Unable to recive the video URL");

			panel.repaint();
			setDrawState(State.video, str);
			shell.executeCommandAsync("sh helper.sh omxplayer " + str);
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_URL_Youtube = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			str = str.substring(0, str.length() - 1);
			if (str == null) throw new Exception("Unable to recive the video URL");

			panel.repaint();
			setDrawState(State.video, str);

			shell.executeCommandAsync("sh helper.sh omxplayer_youtube " + str);
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_Video_Controll = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			if (str == null) throw new Exception("No controll was sent");

			switch (str) {
				case "pause":
					shell.executeCommandAsync("sh helper.sh omxplayer_pause");
				break;
				case "seek-30":
					shell.executeCommandAsync("sh helper.sh omxplayer_seek-30");
				break;
				case "seek30":
					shell.executeCommandAsync("sh helper.sh omxplayer_seek30");
				break;
				case "seek-600":
					shell.executeCommandAsync("sh helper.sh omxplayer_seek-600");
				break;
				case "seek600":
					shell.executeCommandAsync("sh helper.sh omxplayer_seek600");
				break;
				case "_quit":
					shell.executeCommandAsync("sh helper.sh omxplayer_quit");
				break;
				case "quit": // forcequit
					closeVideo();
				break;
			}
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_Image_Controll = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			str = str.substring(0, str.length() - 1);
			int[] decoded = new int[5];
			String[] split = new String[6];
			if (str == null) throw new Exception("No controll was sent");

			split = str.split(":");
			try {
				decoded[0] = Integer.parseInt(split[1]);
				decoded[1] = Integer.parseInt(split[2]);
				decoded[2] = Integer.parseInt(split[3]);
				decoded[3] = Integer.parseInt(split[4]);
				decoded[4] = Integer.parseInt(split[5]);
			} catch (Exception e) {}

			switch (split[0]) {
				case "position":
					reciverHandle.itemContainer.setImagePosition(decoded[0], decoded[1]);
				break;
				case "size":
					reciverHandle.itemContainer.setImageSize(decoded[0], decoded[1]);
				break;
				case "rotation":
					reciverHandle.itemContainer.setImageRotation(decoded[0]);
				break;
				case "full":
					reciverHandle.itemContainer.setImagePosition(decoded[0], decoded[1]);
					reciverHandle.itemContainer.setImageSize(decoded[2], decoded[3]);
					reciverHandle.itemContainer.setImageRotation(decoded[4]);
				break;
			}
			reciverHandle.itemContainer.repaint();
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_Command = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			if (str == null) throw new Exception("Unable to recive command");

			panel.repaint();
			setDrawState(State.idle, str);
			// shell.executeCommand(str); // Disabled for security reasons
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_Quit = new ReciverPreHandle() {
		public void read(BufferedInputStream dis) throws Exception {
			initiateExit();
		}

		public void write(BufferedOutputStream dos) throws Exception {}
	};

	ReciverPreHandle RPH_GETINFO = new ReciverPreHandle() {

		String message;

		public void read(BufferedInputStream dis) throws Exception {
			String str = readByteStream(dis); // Get URL
			if (str == null) throw new Exception("No querry was sent");
			str = str.substring(0, str.length() - 1);
			Dimension screenDimensions;
			switch (str) {
				case "screensize":
					screenDimensions = Main.getScreenDimensions();
					message = screenDimensions.width + ":" + screenDimensions.height;
				break;
				case "imagecontent":
					if (reciverHandle.itemContainer != null && reciverHandle.itemContainer.imageIcon != null) {
						message = "";
						message += reciverHandle.itemContainer.x + ":" + reciverHandle.itemContainer.y + ":";
						message += reciverHandle.itemContainer.imageIcon.getIconWidth() + ":"
								+ reciverHandle.itemContainer.imageIcon.getIconHeight() + ":";
						message += reciverHandle.itemContainer.r;
					}
				break;
				case "state":
					message = state.name() + "";
				break;
			}
		}

		public void write(BufferedOutputStream dos) throws Exception {
			boolean error = false;
			try {
				if (message == null) message = "-1";
				message += "¤";
				dos.write(message.getBytes());
				dos.flush();

			} catch (IOException e) {
				error = true;
			}
			message = null;
			if (dos != null) dos.close();
			if (error) throw new Exception("Unable to reply to request");
		}
	};

	// RUNTIME: Infinite loop to listen for a connection and hadle it.
	public void listenForConnection() {
		ServerSocket server;
		try {
			server = new ServerSocket(serverPort);
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
		handles[Commandid.Image_Controll.Id()] = RPH_Image_Controll;
		handles[Commandid.GetInfo.Id()] = RPH_GETINFO;
		handles[Commandid.Other.Id()] = RPH_Other;
		handles[Commandid.Command.Id()] = RPH_Command;
		handles[Commandid.Quit.Id()] = RPH_Quit;

		while (!exit) {
			Socket socket = null;
			BufferedInputStream dis = null;
			BufferedOutputStream dos = null;
			try {
				socket = server.accept(); // Wait for the socket to get a connection
				dis = new BufferedInputStream(socket.getInputStream(), socketBufferSize);
				dos = new BufferedOutputStream(socket.getOutputStream(), socketBufferSize);

				int packageId = dis.read();
				System.out.println("Recived: " + Commandid.getById(packageId));

				String key = readByteStream(dis);
				key = key.substring(0, key.length() - 1);

				int sucessvalue = (key.equals(validationKey)) ? 1 : 0;

				dos.write((byte) sucessvalue);
				dos.flush();
				if (sucessvalue == 0) {
					System.out.println("\"" + key + "\" / \"" + validationKey + "\"");
					throw new Exception("Invalid key");
				}

				lastUpdate = System.currentTimeMillis();

				if (handles[packageId] == null) packageId = Commandid.Other.Id();

				if (Commandid.getById(packageId).Clear()) {

					if (state == State.video)
						closeVideo();
					else if (state == State.image) reciverHandle.Clear();
					setDrawState(State.downloading);
				}

				handles[packageId].read(dis);
				handles[packageId].write(dos);
			} catch (Exception e) {
				e.printStackTrace();
				setDrawState(State.error, e.getMessage());
			}

			try {
				if (dis != null) dis.close();
				if (socket != null) socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.yield();
		}

		try {
			server.close(); // Closes the socket on exit
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}