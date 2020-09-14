/* Copyright 2010-2011 The MathWorks, Inc. */
package com.mathworks.cmlink.sdk.tests.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class FileCreation {

	private FileCreation() {
		// Non-instantiable.
	}

	private final static String[] EXTENSIONS = { ".txt", ".mdl", ".m", ".mat" };
	private static int sExtensionIndex = 0;

	public static File createTempFileContainingText(File rootDirectory)
			throws IOException {

		if (!rootDirectory.exists()) {
			createDir(rootDirectory);
		}

		// Create a file and check it is not stored:
		File fileTemp = File.createTempFile("myfile", getExtension()); // in
																		// tempdir
		String filename = rootDirectory.getAbsolutePath() + File.separator
				+ fileTemp.getName();

		File file = new File(filename);
		createFileContainingText(file);

		return file;
	}

	private static String getExtension() {
		sExtensionIndex++;
		if (sExtensionIndex >= EXTENSIONS.length) {
			sExtensionIndex = 0;
		}
		return EXTENSIONS[sExtensionIndex];
	}

	public static void createFilesContainingText(Collection<File> files)
			throws IOException {

		for (File file : files) {
			createFileContainingText(file);
		}
	}

	public static void createFileContainingText(File file) throws IOException {
		File parentDir = file.getParentFile();
		if (!parentDir.exists()) {
			createDir(parentDir);
		}

		if (!file.createNewFile() && file.exists()) {
			// file already exists, so we're done
			return;
		}

		FileWriter w = new FileWriter(file);
		try {
			w.write("Created file: " + file + "\nfor use by TAdapter.");
		} catch (IOException e) {
			System.out.println("ISSUES WRITING TO FILE: "
					+ file.getAbsolutePath() + " : " + e.getMessage());
		} finally {
			w.close();
		}
	}

	public static void modifyFiles(Collection<File> files) throws IOException {
		for (File file : files) {
			if (file.isDirectory()) {
				continue;
			}
			FileWriter writer = new FileWriter(file);
			try {
				writer.write("MODIFIED for test " + Math.random());
			} finally {
				writer.close();
			}

		}

	}

	private static void createDir(File dir) throws IOException {
		if (!dir.mkdirs()) {
			throw new IOException("Could not create directory " + dir);
		}

	}

	public static File changeRoot(File file, File oldRoot, File newRoot) {

		String relativePath = getRelativePath(oldRoot, file);
		return new File(newRoot, relativePath);
	}

	public static String getRelativePath(File fileRoot, File fileChild) {

		return getRelativePath(fileRoot.getAbsolutePath(),
				fileChild.getAbsolutePath());

	}

	public static String getRelativePath(String fileRootPath,
			String fileChildPath) {
		int rootLength = fileRootPath.length();
		String relativePath;
		if (fileChildPath.length() == rootLength) {
			relativePath = "";
		} else {
			relativePath = fileChildPath.substring(rootLength + 1);
		}
		return relativePath;
	}

}
