package com.perforce.p4simulink.util;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4simulink.P4CMException;

public class Files {
	private static Logger logger = LogManager.getLogger(Files.class.getName());

	public static boolean validateFileSpecs(List<IFileSpec> fileSpecs,
			String... ignore) throws Exception {
		return validateFileSpecs(fileSpecs, false, ignore);
	}

	public static boolean validateFileSpecs(List<IFileSpec> fileSpecs,
			boolean quiet, String... ignore) throws Exception {
		boolean success = true;
		boolean abort = false;
		String latestError = "";

		for (IFileSpec fileSpec : fileSpecs) {
			FileSpecOpStatus status = fileSpec.getOpStatus();
			if (status != FileSpecOpStatus.VALID) {
				String msg = fileSpec.getStatusMessage();

				// superfluous p4java message
				boolean unknownMsg = true;
				ArrayList<String> ignoreList = new ArrayList<String>();
				ignoreList.addAll(Arrays.asList(ignore));
				for (String istring : ignoreList) {
					if (msg.contains(istring)) {
						// its a known message
						unknownMsg = false;
					}
				}

				// check and report unknown message
				if (unknownMsg) {
					if (!quiet) {
						latestError = msg;
						logger.error(msg);
						if (status == FileSpecOpStatus.ERROR
								|| status == FileSpecOpStatus.CLIENT_ERROR) {
							abort = true;
						}
					}
					success = false;
				}
			}
		}

		if (!quiet && abort) {
			String msg = "P4 Error(s): " + latestError;
			throw new P4CMException(msg);
		}
		return success;
	}

	public static int changeFailedSubmit(List<IFileSpec> submit) {
		int change = 0;
		for (IFileSpec fileSpec : submit) {
			FileSpecOpStatus status = fileSpec.getOpStatus();
			if (status != FileSpecOpStatus.VALID) {
				String msg = fileSpec.getStatusMessage();
				for (String line : msg.split("\n")) {
					if (line.contains("p4 submit -c")) {
						String regex = ".*p4 submit -c (\\d+).*";
						String cngStr = line.replaceAll(regex, "$1");
						change = Integer.parseInt(cngStr);
					}
				}
			}
		}
		return change;
	}
}
