package com.rikardlegge.mediacaster;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 *
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
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
	private static String ip;

	public void sendData(final byte contentId, InputStream newInputStream, final boolean shutdown) {

		bufferSize = Settings.bufferSize;
		port = Settings.port;
		ip = Settings.ip;

		inputStream = newInputStream;
		new Thread(new Runnable() {
			public void run() {
				try {
					initiateSend();
					sendDescription(contentId);
					sendData();
					endSend();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (shutdown)
					IntentReceiver.requestExit();
			}
		}).start();
	}

	public void sendCommand(final byte contentId, final boolean shutdown) {

		bufferSize = Settings.bufferSize;
		port = Settings.port;
		ip = Settings.ip;

		new Thread(new Runnable() {
			public void run() {
				try {
					initiateSend();
					sendDescription(contentId);
					endSend();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (shutdown)
					IntentReceiver.requestExit();
			}
		}).start();
	}

	private void initiateSend() throws UnknownHostException, IOException {
		socket = new Socket(ip, port);
		socket.setSoTimeout(1000);
		socketOutputStream = socket.getOutputStream();
	}

	private void sendDescription(byte id) throws IOException {
		socketOutputStream.write(id);
	}

	private void sendData() throws IOException {
		byte[] buffer = new byte[bufferSize];
		int read;

		while ((read = inputStream.read(buffer)) != -1) {
			socketOutputStream.write(buffer, 0, read);
		}
	}

	private void endSend() throws IOException {
		if (inputStream != null)
			inputStream.close();
		if (socketOutputStream != null)
			socketOutputStream.close();
		if (socket != null)
			socket.close();
	}
}
