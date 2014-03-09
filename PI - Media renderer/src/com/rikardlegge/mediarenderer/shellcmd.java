package com.rikardlegge.mediarenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class ShellCmd {

	private Process execute(String command) throws Exception {
		return Runtime.getRuntime().exec(command);
	}

	public Process executeCommandAsync(String command) {
		return executeCommandAsync(command, false);
	}

	public Process executeCommandAsync(String command, boolean keepStreamsOpen) {
		System.out.println("$ " + command);
		try {
			Process p = execute(command);
			if (!keepStreamsOpen) {
				p.getInputStream().close();
				p.getOutputStream().close();
				p.getErrorStream().close();
			}
			return p;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("");
		return null;
	}

	public String executeCommand(String command) {
		System.out.println("$ " + command);

		StringBuffer output = new StringBuffer();
		Process p = null;
		try {
			p = execute(command);
			p.waitFor();

			String line = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			reader.close();

			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				p.getInputStream().close();
				p.getOutputStream().close();
				p.getErrorStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(output.toString());
		return output.toString();
	}

}