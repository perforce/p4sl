package com.perforce.p4simulink.ui;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import java.awt.Color;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class P4Pane {

	protected void checkNonEmpty(JFileChooser field) {
		File file = field.getSelectedFile();
		if (file == null || !file.exists()) {
			field.setBorder(new LineBorder(Color.RED));
		}
	}

	protected void checkNonEmpty(JTextField field) {
		String text = field.getText();
		if (text == null || text.isEmpty()) {
			field.setBorder(new LineBorder(Color.RED));
		}
	}
}
