package com.perforce.p4simulink.ui;

import java.io.File;
import java.nio.file.Path;

import javax.swing.filechooser.FileFilter;

import com.perforce.p4simulink.connection.P4Config;

public class P4ConfigFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		//Should accept folder by default and only filter files.
		if (f.isDirectory()) return true;
		String P4CONFIG = P4Config.getName();
		Path path = f.toPath();
		Path name = path.getFileName();
		return P4CONFIG.equals(name.toString());
	}

	@Override
	public String getDescription() {
		return "P4CONFIG";
	}
}
