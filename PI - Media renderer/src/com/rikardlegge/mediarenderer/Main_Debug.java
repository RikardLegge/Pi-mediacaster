package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
			Socket socket = new Socket(serverIp, serverPort); // Opens a socked
																// connection
																// for sending
																// the data
			socket.setSoTimeout(1000); // Don't realy know if it's needed, but I
										// keep it since it might be good if no
										// server is found.
			OutputStream socketOutputStream = socket.getOutputStream(); // The
																		// socket
																		// outputstream
																		// to
																		// send
																		// data

			String[] str = {}; // A string with urls to send.
			socketOutputStream.write((byte) 23); // The contenttype (23:
													// Youtubelink)
			socketOutputStream.write((new String(str[2])).getBytes()); // Sends
																		// the
																		// string
																		// as
																		// bytes.

			socketOutputStream.close(); // IMPORTANT: Closes the streams
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

			socketOutputStream.write((byte) 11);

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
			OutputStream socketOutputStream = socket.getOutputStream();

			String[] str = {
					"http://www.diseno-art.com/news_content/wp-content/uploads/2012/09/2013-Jaguar-F-Type-18.jpg",
					"http://www.digitalphotoartistry.com/rose1.jpg",
					"http://www.youtube.com/watch?v=9N6CKccJzFg",
					"http://media-b175.firedrive.com/stream/16/12/beb16e41a4a3c1b987bd0e98b9ae58c2.flv?h=yhlUofM2QDDqawu8tlcZ-A&e=1393106680&f=beb16e41a4a3c1b987bd0e98b9ae58c2.flv" };
			socketOutputStream.write((byte) 23);
			socketOutputStream.write((new String(str[2])).getBytes());

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
			socketOutputStream.write((byte) 25);

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
		}
	}

	public static void executeCommand(String command) {

		ProcessBuilder builder = new ProcessBuilder("ipconfig");// new
																// ProcessBuilder("killall","-9",
																// "omxplayer");
		Process p;
		try {
			System.out.println("$ " + command);
			p = builder.start();
			p.waitFor();
			System.out.println("waited");
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			System.out.println("read");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
