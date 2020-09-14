package com.perforce.p4simulink;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import com.mathworks.cmlink.api.ConfigurationManagementException;

public class P4CMException extends ConfigurationManagementException {

	private static final long serialVersionUID = 1L;

	public P4CMException(String s) {
		super(s);
	}

	public P4CMException(String s, Exception e) {
		super(s, e);
	}

	public P4CMException(Exception e) {
		super(e);
	}

	@Override
	public String toString() {
		return "p4simulink: " + getMessage();
	}
}
