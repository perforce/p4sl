package com.perforce.p4simulink.ui;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPasswordField;

public class P4PasswordPane extends P4Pane {

	private JPasswordField password = new JPasswordField("");
	private List<Object> fields;

	public P4PasswordPane(String p4port, String p4user) {

		fields = new ArrayList<>();
		fields.add("User '" + p4user + "' on server '" + p4port + "'.");
		fields.add(" ");
		fields.add("Enter password: ");
		fields.add(password);
	}

	public Object[] getFields() {

		return fields.toArray();
	}

	public String getPassword() {
		return new String(password.getPassword());
	}
}
