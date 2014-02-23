package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) the Pi-mediacaster contributors. All rights reserved.
 *
 * This file is part of Pi-mediacaster, distributed under the GNU GPL v2 with
 * a Linking Exception. For full terms see the included COPYING file.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;

class ShellCmd {
	Process p;
	Runtime r;
	BufferedReader reader;
	BufferedWriter writer;
	Thread thread;

	ArrayList<String> stack;

	ShellCmd() {
		r = Runtime.getRuntime();
	}

	public void startProcess(final String command) {
		create(command);
	}

	private void create(String command) {
		/*try {
			System.out.println("$: " + command);
			p = r.exec(command);
			
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
*/
		try {
			System.out.println("$: " + command);

			ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
			p = builder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void executeCommand(String command) {
		try {
			System.out.println("$ " + command);
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}