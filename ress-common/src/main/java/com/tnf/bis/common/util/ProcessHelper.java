package com.tnf.bis.common.util;

import static java.lang.Thread.currentThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.google.common.base.Joiner;

public class ProcessHelper {

	private static final TimeUnit TIMEOUT_UNIT = TimeUnit.HOURS;

	public static int process(String[] args, String groupDir, Logger logger, long timeoutHours) {
		OutputRedirector outputRedirector = new OutputRedirector();
		int errorLevel = Integer.MIN_VALUE;
		try {
			
			String copyPasteArgs = ""; // copy-paste ready cmd line
			try {
				if (args != null && args.length > 0) {
					String[] deco = new String[args.length];
					deco = new String[args.length];
					for (int i = 0; i < args.length; i++) {
						String arg = args[i];
						if (arg != null && (arg.indexOf(' ') >= 0 || arg.indexOf(';') > 0)) {
							deco[i] = "\"" + arg + "\"";
						} else {
							deco[i] = arg;
						}
						if (deco[i] == null) {
							deco[i] = "";
						}
					}
					copyPasteArgs = Joiner.on(" ").join(deco);
				}
			} catch (Exception e) {
				logger.warn("Copy-paste args {}", e);
			}

			Path path = Paths.get(System.getProperty(//
					"user.dir"), "log", groupDir, currentThread().getName()//
			);
			if (Files.notExists(path)) {
				Files.createDirectories(path);
			}
			File workDirectory = path.toFile();

			ProcessBuilder processBuilder = new ProcessBuilder() //
					.command(args) //
					.directory(workDirectory).redirectErrorStream(true);

			Process process = processBuilder.start();

			outputRedirector.setProcess(process);
			outputRedirector.setName("OutputRedirector-" + Thread.currentThread().getName());
			outputRedirector.setDaemon(true);
			outputRedirector.start();

			int pid = getPid(process, logger);

			logger.info( //
					"OS process (pid:{}) running: {}", //
					pid, copyPasteArgs//
			);
			try {
				boolean ok = true;
				if (timeoutHours > 0) {
					ok = process.waitFor(timeoutHours, TIMEOUT_UNIT);
				} else {
					process.waitFor();
				}
				if (ok) {
					errorLevel = process.exitValue();
					if (errorLevel == 0) {
						logger.info( //
								"OS process (pid:{}) finished successfuly: {}", //
								pid, copyPasteArgs//
						);
					} else {
						logger.error(//
								"OS process (pid:{}) finished with errorlevel: {} {}", //
								pid, errorLevel, copyPasteArgs//
						);
					}
				} else {
					logger.error(//
							"OS process (pid:{}) finished by timeout: {}", //
							pid, copyPasteArgs//
					);
					kill(pid, logger);
				}
			} catch (InterruptedException ex) {
				kill(pid, logger);
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}
		outputRedirector.stop = true;
		return errorLevel;
	}

	private static int getPid(Process process, Logger logger) {
		try {
			Field f = process.getClass().getDeclaredField("pid");
			f.setAccessible(true);
			return (Integer) f.get(process);
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return 0;
	}

	private static void kill(int pid, Logger logger) {
		if (pid > 0) {
			logger.info("kill -9 {}", pid);
			try {
				new ProcessBuilder("kill", "-9", Integer.toString(pid)).start();
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
	}

	private static class OutputRedirector extends Thread {
		private Process process;
		public boolean stop = false;

		public void run() {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				while (reader.readLine() != null) {
					if (stop) {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void setProcess(Process process) {
			this.process = process;
		}
	}
}
