package com.rikardlegge.mediarenderer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/*
 * This is a debugscript which i have written to test out new functionality.
 */

public class Main_Debug {
	final static int socketBufferSize = 65536;
	static String ip;
	static int port;

	public static void main(String[] args) {
		ip = "192.168.0.109";
		port = 5000;

		String[] str = { "http://www.diseno-art.com/news_content/wp-content/uploads/2012/09/2013-Jaguar-F-Type-18.jpg",
				"http://www.digitalphotoartistry.com/rose1.jpg", "http://wallpoper.com/images/00/41/69/85/england-stonehenge_00416985.jpg", };

		int cmd = 1;
		try {
			switch (cmd) {
				case 0:
					send(COMMAND, "sh helper.sh omxplayer_pool" + "§");
				break;
				case 1:
					URL.id(Commandid.URL_Image.Id());
					send(URL, str[1] + "§");
				break;
				case 2:
					URL.id(Commandid.URL_Video.Id());
				// send(URL, str[0]);
				break;
				case 3:
					URL.id(Commandid.URL_Youtube.Id());
					send(URL, "http://www.youtube.com/watch?v=KOoKCuq6YY8" + "§");
				break;
				case 4:
					send(FILE, "D:/Media/Pictures/Wallpaper/sun__grass_and_space_by_sonylisation-d3ftrbx.jpg");
				break;
				case 5:
					send(ID, "");
				break;
				case 6:
				// System.out.println(new ShellCmd().executeCommand("cmd /c echo test"));
				break;
				case 7:
					URL.id(Commandid.GetInfo.Id());
					send(URL, "state" + "§");
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static StreamController URL = new StreamController() {
		private byte _id;

		public byte id() {
			return _id;
		}

		public void id(int id) {
			this._id = (byte) id;
		}

		public BufferedInputStream get(String uri) {
			return new BufferedInputStream(new ByteArrayInputStream(uri.getBytes()));
		}

		public void close() {}
	};

	static StreamController ID = new StreamController() {
		public byte id() {
			return (byte) Commandid.Other.Id();
		}

		public BufferedInputStream get(String uri) {
			return null;
		}

		public void close() {}

		public void id(int id) {}
	};

	static StreamController COMMAND = new StreamController() {
		public byte id() {
			return (byte) Commandid.Command.Id();
		}

		public BufferedInputStream get(String uri) throws Exception {
			return new BufferedInputStream(new ByteArrayInputStream(uri.getBytes()));
		}

		public void close() {}

		public void id(int id) {}
	};

	static StreamController FILE = new StreamController() {
		FileInputStream stream;

		public byte id() {
			return (byte) Commandid.Image.Id();
		}

		public BufferedInputStream get(String uri) throws Exception {
			stream = new FileInputStream(uri);
			return new BufferedInputStream(stream);
		}

		public void close() throws Exception {
			if (stream != null) {
				stream.close();
				stream = null;
			}
		}

		public void id(int id) {}
	};

	static void send(StreamController content, String args) throws IOException {
		Socket socket = null;
		BufferedInputStream socketInputStream = null;
		BufferedOutputStream socketOutputStream = null;

		try {
			socket = new Socket(ip, port);
			socket.setSoTimeout(1000);

			int bufferSize = socketBufferSize;

			BufferedInputStream stream = content.get(args);

			if (stream.available() < socketBufferSize) bufferSize = stream.available();

			socketOutputStream = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
			socketInputStream = new BufferedInputStream(socket.getInputStream(), socketBufferSize);

			socketOutputStream.write(content.id());
			socketOutputStream.flush();

			socketOutputStream.write("mytest¤".getBytes());
			socketOutputStream.flush();
			if (socketInputStream.read() != 1) throw new Exception("Invalid key");
			System.out.println("OK");

			byte[] buffer = new byte[bufferSize];
			int read;

			if (stream != null) while ((read = stream.read(buffer)) != -1) {
				socketOutputStream.write(buffer, 0, read);
			}
			socketOutputStream.flush();

			content.close();

			String str = "";
			int byt;
			while ((byt = socketInputStream.read()) != -1) {
				str += ((char) (byte) byt);
			}
			System.out.println(str);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null) socket.close();
			if (socketOutputStream != null) socketOutputStream.close();
			if (socketInputStream != null) socketInputStream.close();
		}
	}

	interface StreamController {
		public BufferedInputStream get(String uri) throws Exception;

		public void close() throws Exception;

		public byte id();

		public void id(int id);
	}
}
