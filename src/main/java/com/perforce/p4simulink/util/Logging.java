package com.perforce.p4simulink.util;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import org.apache.logging.log4j.Logger;

import com.mathworks.cmlink.api.ConfigurationManagementException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4simulink.P4CMException;

public class Logging {

	public static void logException(Logger logger,
			ConfigurationManagementException e)
			throws ConfigurationManagementException {
		logException(logger, e, false);
	}

	public static void logException(Logger logger, P4JavaException e)
			throws ConfigurationManagementException {
		logException(logger, e, false);
	}

	public static void logException(Logger logger, P4CMException e)
			throws P4CMException {
		logException(logger, e, false);
	}

	public static void logException(Logger logger, P4CMException e,
			boolean rethrow) throws P4CMException {
		logger.error("Exception: " + e.getLocalizedMessage(),e);

		if (rethrow) {
			throw e;
		}
	}

	public static void logException(Logger logger,
			ConfigurationManagementException e, boolean rethrow)
			throws ConfigurationManagementException {
		logger.error("Exception: " + e.getLocalizedMessage(),e);

		if (rethrow) {
			throw e;
		}
	}

	public static void logException(Logger logger, P4JavaException e,
			boolean rethrow) throws ConfigurationManagementException {
		logger.error("Exception [P4Java]: " + e.getLocalizedMessage(), e);

		if (rethrow) {
			throw new ConfigurationManagementException(e);
		}
	}
}
