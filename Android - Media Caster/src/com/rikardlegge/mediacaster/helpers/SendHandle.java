package com.rikardlegge.mediacaster.helpers;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.rikardlegge.mediacaster.IntentReceiver;

public class SendHandle {

	public void sendData(final byte contentId, InputStream newInputStream, final boolean shutdown) {
		sendData(contentId, newInputStream, shutdown, null);
	}

	public void sendData(final byte contentId, final InputStream newInputStream, final boolean shutdown, final SocketCallback callback) {
		new Thread(new Runnable() {
			public void run() {
				new socketting().SendOtherData(contentId, newInputStream, shutdown, callback);
			}
		}).start();
	}

	public void sendCommand(final byte contentId, final boolean shutdown) {
		new Thread(new Runnable() {
			public void run() {
				new socketting().sendCommand(contentId, shutdown);
			}
		}).start();
	}

	class socketting {

		private Socket socket;
		private OutputStream socketOutputStream;
		private InputStream socketInputStream;
		private InputStream inputStream;

		private int bufferSize;
		private int port;
		private String key;
		private String ip;

		socketting() {
			// Sets these values to the current values defined in settings
			bufferSize = Settings.bufferSize;
			port = Settings.port;
			ip = Settings.ip;
			key = Settings.key;
			if (key != null) ;
		}

		public void SendOtherData(byte contentId, InputStream newInputStream, boolean shutdown, SocketCallback callback) {

			inputStream = newInputStream;
			try {
				initiateSend();
				if (!testConnection()) throw new Exception();
				sendDescription(contentId);
				if (sendAuthentication()) {
					sendData();
					if (callback != null) {
						String response = getData();
						callback.response(response);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				endSend();
			}
			if (shutdown) IntentReceiver.requestExit();
		}

		public void sendCommand(byte contentId, boolean shutdown) {
			try {
				initiateSend();
				if (!testConnection()) throw new Exception();
				sendDescription(contentId);
				sendAuthentication();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				endSend();
			}
			if (shutdown) IntentReceiver.requestExit();
		}

		private boolean testConnection() {
			return true;
		}

		// First part of the send command. Creates a socket, sets the timeout and
		// caches the outputStream.
		private void initiateSend() throws UnknownHostException, IOException {
			socket = new Socket(ip, port);
			socket.setSoTimeout(1000);
			socketOutputStream = socket.getOutputStream();
			socketInputStream = new BufferedInputStream(socket.getInputStream(), bufferSize);

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

		private boolean sendAuthentication() throws Exception {
			socketOutputStream.write((Settings.key + "¤").getBytes());
			socketOutputStream.flush();
			if (socketInputStream.read() != 1) return false;
			return true;
		}

		private String getData() throws IOException {
			String str = "";
			int byt;
			while ((byt = socketInputStream.read()) != -1 && byt != ((byte) '¤' & 0xFF)) {
				str += ((char) (byte) byt);
			}
			return str;
		}

		// Closes all the ports and streams
		private void endSend() {
			try {
				if (inputStream != null) inputStream.close();
				if (socketInputStream != null) socketInputStream.close();
				if (socketOutputStream != null) socketOutputStream.close();
				if (socket != null) socket.close();
			} catch (Exception e) {}
		}
	}
}
