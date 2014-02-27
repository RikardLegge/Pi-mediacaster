package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/*
 * This is a debugscript which i have written to test out new functionality.
 * The names should be quite clear and it's not fully documented but there is a
 * examplefunction.
 */

public class Main_Debug {
	final static int socketBufferSize = 65536;

	static void example(String serverIp, int serverPort) {
		try {
			Socket socket = new Socket(serverIp, serverPort); // Opens a socked connection for sending the data
			socket.setSoTimeout(1000); // Don't realy know if it's needed, but I keep it since it might be good if no server is found.
			OutputStream socketOutputStream = socket.getOutputStream(); // The socket outputstream to send data

			String[] str = {}; // A string with urls to send.
			socketOutputStream.write((byte) Commandid.Other.Id()); // The contenttype (23: Youtubelink)
			socketOutputStream.write((new String(str[2])).getBytes()); // Sends the string as bytes.

			// IMPORTANT: Closes the streams
			socketOutputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void sendFile(String serverIp, int serverPort) {
		try {
			Socket socket = new Socket(serverIp, serverPort);
			socket.setSoTimeout(1000);

			FileInputStream fileInputStream = new FileInputStream(
					"D:/Media/Pictures/Wallpaper/sun__grass_and_space_by_sonylisation-d3ftrbx.jpg");
			OutputStream socketOutputStream = socket.getOutputStream();

			byte[] buffer = new byte[socketBufferSize];
			int read;

			socketOutputStream.write((byte) Commandid.Image.Id());

			while ((read = fileInputStream.read(buffer)) != -1) {
				socketOutputStream.write(buffer, 0, read);
			}

			socketOutputStream.close();
			fileInputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void sendUrl(String serverIp, int serverPort) {
		try {
			Socket socket = new Socket(serverIp, serverPort);
			socket.setSoTimeout(1000);
			OutputStream dos = socket.getOutputStream();

			String[] str = { "http://www.diseno-art.com/news_content/wp-content/uploads/2012/09/2013-Jaguar-F-Type-18.jpg",
					"http://www.digitalphotoartistry.com/rose1.jpg",
					"http://wallpoper.com/images/00/41/69/85/england-stonehenge_00416985.jpg", "http://www.youtube.com/watch?v=KOoKCuq6YY8" };

			// dos.write((new String("TestKey001")).getBytes());
			dos.write((byte) Commandid.URL_Youtube.Id());
			dos.write((new String(str[3])).getBytes());

			// dis.close();
			dos.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void sendCommand(String serverIp, int serverPort, String cmd) {
		try {
			Socket socket = new Socket(serverIp, serverPort);
			socket.setSoTimeout(1000);
			OutputStream socketOutputStream = socket.getOutputStream();
			socketOutputStream.write((byte) Commandid.Video_Controll.Id());
			socketOutputStream.write((new String(cmd)).getBytes());

			socketOutputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void clear(String serverIp, int serverPort) {
		try {
			Socket socket = new Socket(serverIp, serverPort);
			socket.setSoTimeout(1000);
			OutputStream socketOutputStream = socket.getOutputStream();
			socketOutputStream.write((byte) Commandid.Other.Id());

			socketOutputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String url = "192.168.0.109";
		int port = 5000;

		int cmd = 2;
		switch (cmd) {
			case 0:
				clear(url, port);
			break;
			case 1:
				sendFile(url, port);
			break;
			case 2:
				sendUrl(url, port);
			break;
			case 3:
				sendCommand(url, port, "info");
			break;
		}
	}
}
