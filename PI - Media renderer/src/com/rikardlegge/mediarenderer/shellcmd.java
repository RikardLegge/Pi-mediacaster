package com.rikardlegge.mediarenderer;

/*
 * Copyright (C) Rikard Legge. All rights reserved.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

class ShellCmd {
	Process p;
	BufferedReader reader;
	BufferedWriter writer;
	Thread thread;

	ArrayList<String> stack;

	ShellCmd() {
		// See function create(String command) for case used.
	}

	public void startProcess(final String command) {
		System.out.println("$ " + command);
		try {
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
			p = builder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startProcess(final String commands[]) {

		System.out.print("$ ");
		for (String s : commands) {
			System.out.print(s+" ");
		}
		System.out.println("");
		
		try {
			ProcessBuilder builder = new ProcessBuilder(commands);
			p = builder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is a previous version whih used the getRuntime instead of the processbuilder. Might work
	 * 
	 * @deprecated
	 */
	public void startProcess_deprecated(final String command) {
		System.out.println("$ " + command);
		try {
			p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void executeCommand(String command) {
		System.out.println("$ " + command);
		try {
			// uses the getRuntime instead of projectbuilder, since i was unable
			// to get the processbuilder to execute long lines of shell code
			p = Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void executeCommandAndWaitForTermination(String command) {
		System.out.println("$ " + command);
		try {
			// uses the getRuntime instead of projectbuilder, since i was unable
			// to get the processbuilder to execute long lines of shell code
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}