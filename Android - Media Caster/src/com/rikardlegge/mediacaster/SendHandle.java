package com.rikardlegge.mediacaster;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendHandle {
	private static Socket socket;
	private static OutputStream socketOutputStream;
	private static InputStream inputStream;

	private static int bufferSize;
	private static int port;
	private static String key;
	private static String ip;

	// Sends all types of data which have a inputstream.
	/**
	 * 
	 * @param contentId
	 *            : The id of the contenttype
	 *            View intentreciver for valid values. These can be changed
	 * @param newInputStream
	 *            : An inputstream to read for the sent data
	 * @param shutdown
	 *            : A boolean to shutdown or not when the send is complete
	 */
	public void sendData(final byte contentId, InputStream newInputStream, final boolean shutdown) {

		// Sets these values to the current values defined in settings
		bufferSize = Settings.bufferSize;
		port = Settings.port;
		ip = Settings.ip;
		key = Settings.key;
		if (key != null) ;

		inputStream = newInputStream; // Sets the inputstream for later sending
										// the data
		new Thread(new Runnable() {
			public void run() {
				try {
					initiateSend();
					testConnection(); // Currently an empty function, needs to
										// be written.
					sendDescription(contentId);
					sendData();
					endSend();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (shutdown) IntentReceiver.requestExit();
			}
		}).start();
	}

	/**
	 * 
	 * @param contentId
	 *            : The id of the contenttype
	 *            View intentreciver for valid values. These can be changed
	 * @param shutdown
	 *            : A boolean to shutdown or not when the send is complete
	 */
	public void sendCommand(final byte contentId, final boolean shutdown) {

		bufferSize = Settings.bufferSize;
		port = Settings.port;
		ip = Settings.ip;

		new Thread(new Runnable() {
			public void run() {
				try {
					initiateSend();
					testConnection(); // Currently an empty function, needs to
										// be written.
					sendDescription(contentId);
					endSend();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (shutdown) IntentReceiver.requestExit();
			}
		}).start();
	}

	private void testConnection() {
		// TODO: Needs some sort of test to see if the server is available and active
	}

	// First part of the send command. Creates a socket, sets the timeout and
	// caches the outputStream.
	private void initiateSend() throws UnknownHostException, IOException {
		socket = new Socket(ip, port);
		socket.setSoTimeout(1000);
		socketOutputStream = socket.getOutputStream();
	}

	// Send the content id
	private void sendDescription(byte id) throws IOException {
		socketOutputStream.write(id);
	}

	// Sends the data via the inputstream.
	private void sendData() throws IOException {
		byte[] buffer = new byte[bufferSize];
		int read;

		while ((read = inputStream.read(buffer)) != -1) {
			socketOutputStream.write(buffer, 0, read);
		}
	}

	// Closes all the ports and streams
	private void endSend() throws IOException {
		if (inputStream != null) inputStream.close();
		if (socketOutputStream != null) socketOutputStream.close();
		if (socket != null) socket.close();
	}
}
