package com.perforce.p4simulink.connection;

import com.perforce.p4simulink.P4CMException;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Properties;

public class P4Config {

	private static final String P4CONFIG = System.getenv("P4CONFIG");

	private final Path p4config;
	private final P4Uri p4uri;

	/**
	 * Searches for P4CONFIG and load from file.
	 *
	 * @param pwd
	 * @throws P4CMException
	 */
	public P4Config(Path pwd) throws P4CMException {
		this.p4config = findP4Config(pwd);
		Properties config = load(p4config);
		this.p4uri = new P4Uri(config);
	}

	/**
	 * Create new P4CONFIG from Properties at the specified path
	 *
	 * @param path
	 * @param uri
	 * @throws P4CMException
	 */
	public P4Config(Path path, P4Uri uri) throws P4CMException {
		this.p4uri = uri;
		this.p4config = path.resolve(getName());

		// only save if no P4CONFIG.
		if (!p4config.toFile().exists()) {
			save(p4config, uri);
		}
	}

	public Path getPath() {
		return p4config;
	}

	/**
	 * Access P4CONFIG parameters. Returning a String or empty String if not
	 * defined.
	 *
	 * @param cfg
	 * @return
	 */
	public String get(P4ConfigType cfg) {
		String value = p4uri.get(cfg);
		if (value != null) {
			return value;
		}
		return "";
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ConfigFile: " + p4config.toString() + "\n");
		Properties cfg = p4uri.getConfig();
		for (Entry<Object, Object> entry : cfg.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			sb.append("... " + key + "=" + value + "\n");
		}
		return sb.toString();
	}

	/**
	 * Returns P4Config represented as a P4 URI path.
	 *
	 * @return
	 */
	public String toUri() {
		return p4uri.getUri(P4UriType.P4);
	}

	/**
	 * Returns P4Config represented as a p4java URI path.
	 *
	 * @return
	 * @throws P4CMException
	 */
	public String toP4Java() throws P4CMException {
		return p4uri.getUri(P4UriType.P4JAVA);
	}

	/**
	 * Load the P4CONFIG file into memory
	 *
	 * @param path
	 * @return
	 * @throws P4CMException
	 */
	private Properties load(Path path) throws P4CMException {
		Properties cfg = new Properties();
		try {
			FileInputStream in = new FileInputStream(path.toFile());
			cfg.load(in);
			in.close();
		} catch (IOException e) {
			throw new P4CMException("Error loading config file: "
					+ path.toString(), e);
		}
		return cfg;
	}

	/**
	 * Save the P4CONFIG file.
	 *
	 * @param path
	 * @param uri
	 * @throws P4CMException
	 */
	private void save(Path path, P4Uri uri) throws P4CMException {
		try {
			FileWriter writer = new FileWriter(path.toFile());
			BufferedWriter out = new BufferedWriter(writer);

			StringBuffer sb = new StringBuffer();
			Properties cfg = uri.getConfig();
			for (Entry<Object, Object> entry : cfg.entrySet()) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				sb.append(key + "=" + value + "\n");
			}

			out.write(sb.toString());
			out.close();
		} catch (IOException e) {
			throw new P4CMException(e);
		}
	}

	/**
	 * Looks for a P4CONFIG given a pwd (present working directory). Returns a
	 * Path or null if no P4CONFIG is found.
	 *
	 * @param path
	 * @return
	 * @throws P4CMException
	 */
	private Path findP4Config(Path path) throws P4CMException {
		while (path != null) {
			Path configPath = path.resolve(getName());
			if (configPath != null && Files.exists(configPath)) {
				return configPath;
			} else {
				path = path.getParent();
			}
		}
		throw new P4CMException("No P4CONFIG found");
	}

	public static String getName() {
		if (P4CONFIG == null) {
			return ".p4config";
		}
		return P4CONFIG;
	}
}
