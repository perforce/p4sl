package com.perforce.p4simulink.utils;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P4TestServer {

	private static final Logger log = LogManager.getLogger(P4TestServer.class);

	private final String p4d;
	private final String p4port;
	private final File p4root;

	private DefaultExecutor executor;
	private final int P4D_TIMEOUT = 120; // timeout in seconds

	public P4TestServer(String p4bin, String root, String p4port) {
		String p4d = p4bin;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			p4d += "bin.ntx64/p4d.exe";
		}
		if (os.contains("mac")) {
			p4d += "bin.darwin90x86_64/p4d";
		}
		if (os.contains("nix") || os.contains("nux")) {
			p4d += "bin.linux26x86_64/p4d";
		}

		this.p4d = p4d;
		this.p4root = new File(root);
		this.p4port = p4port;
	}

	public void start() throws Exception {
		CommandLine cmdLine = new CommandLine(p4d);
		cmdLine.addArgument("-vserver=3");
		cmdLine.addArgument("-C1");
		cmdLine.addArgument("-r");
		cmdLine.addArgument(formatPath(p4root.getAbsolutePath()));
		cmdLine.addArgument("-p");
		cmdLine.addArgument(p4port);
		cmdLine.addArgument("-Llog");

		log.info("Starting P4D:");
		// ensure p4d processes get cleaned up after at most 30 seconds
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		executor = new DefaultExecutor();
		executor.setWatchdog(new ExecuteWatchdog(P4D_TIMEOUT * 1000));
		executor.execute(cmdLine, resultHandler);

		Thread.sleep(100);
	}

	public void stop() throws Exception {
		ExecuteWatchdog watchdog = executor.getWatchdog();
		if (watchdog != null) {
			watchdog.destroyProcess();
			executor = null;
			Thread.sleep(100);
		} else {
			log.warn("Unable to stop P4D.");
		}
	}

	public void upgrade() throws Exception {
		exec(new String[] { "-xu" });
	}

	public void restore(File ckp) throws Exception {
		exec(new String[] { "-jr", "-z", formatPath(ckp.getAbsolutePath()) });
	}

	public void extract(File archive) throws Exception {
		TarArchiveInputStream tarIn = null;
		tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(
				new BufferedInputStream(new FileInputStream(archive))));

		TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
		log.info("Extracting tarball: ");
		while (tarEntry != null) {
			File node = new File(p4root, tarEntry.getName());
			if (tarEntry.isDirectory()) {
				node.mkdirs();
			} else {
				node.createNewFile();
				byte[] buf = new byte[1024];
				BufferedOutputStream bout = new BufferedOutputStream(
						new FileOutputStream(node));

				int len = 0;
				while ((len = tarIn.read(buf)) != -1) {
					bout.write(buf, 0, len);
				}

				bout.close();
				buf = null;
			}
			tarEntry = tarIn.getNextTarEntry();
		}
		tarIn.close();
	}

	public void clean() throws IOException {
		if (p4root.exists()) {
			FileUtils.cleanDirectory(p4root);
		} else {
			p4root.mkdir();
		}
	}

	public int getVersion() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		CommandLine cmdLine = new CommandLine(p4d);
		cmdLine.addArgument("-V");
		DefaultExecutor executor = new DefaultExecutor();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		executor.setStreamHandler(streamHandler);
		executor.execute(cmdLine);

		int version = 0;
		for (String line : outputStream.toString().split("\\n")) {
			if (line.startsWith("Rev. P4D")) {
				Pattern p = Pattern.compile("\\d{4}\\.\\d{1}");
				Matcher m = p.matcher(line);
				while (m.find()) {
					String found = m.group();
					found = found.replace(".", ""); // strip "."
					version = Integer.parseInt(found);
				}
			}
		}
		return version;
	}

	private int exec(String[] args) throws Exception {
		CommandLine cmdLine = new CommandLine(p4d);
		cmdLine.addArgument("-C1");
		cmdLine.addArgument("-r");
		cmdLine.addArgument(formatPath(p4root.getAbsolutePath()));
		for (String arg : args) {
			cmdLine.addArgument(arg);
		}

		DefaultExecutor executor = new DefaultExecutor();
		return executor.execute(cmdLine);
	}

	private String formatPath(String path) {
		final String Q = "\"";
		path = Q + path + Q;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			path = path.replace('\\', '/');
		}
		return path;
	}

}
