package com.perforce.p4simulink.connection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.perforce.p4simulink.P4CMException;

public class P4Uri {

	private final Properties config;
	private String depotPath;

	/**
	 * Constructs a Uri object from a URI path.
	 * 
	 * @param uri
	 * @throws P4CMException
	 */
	public P4Uri(String uri) throws P4CMException {
		this.config = parse(uri);
	}

	/**
	 * Constructs a Uri object from a set of Properties.
	 * 
	 * @param config
	 * @throws P4CMException
	 */
	public P4Uri(Properties config) throws P4CMException {
		this.config = config;
	}

	/**
	 * Access P4URI parameters. Returning a String or empty String if not
	 * defined.
	 * 
	 * @param cfg
	 * @return
	 */
	public String get(P4ConfigType cfg) {
		String value = config.getProperty(cfg.name());
		if (value != null) {
			return value;
		}
		return "";
	}

	public String getUri(P4UriType type) {
		return toUri(type, config);
	}

	public Properties getConfig() {
		return config;
	}

	public String getDepotPath() {
		if (depotPath == null) {
			return "";
		}
		return depotPath;
	}

	public boolean isSsl() {
		String p4port = config.getProperty(P4ConfigType.P4PORT.name());
		if (p4port.startsWith("ssl")) {
			return true;
		}
		return false;
	}

	/**
	 * Parses a P4 URI and returns a Properties object.
	 * 
	 * @return
	 */
	private Properties parse(String uriPath) throws P4CMException {

		URI uri = null;
		try {
			uri = new URI(uriPath);
		} catch (URISyntaxException e) {
			throw new P4CMException("Invalid URI");
		}

		String protocol = uri.getScheme();
		if (protocol == null || !protocol.startsWith("p4")) {
			throw new P4CMException("Not a p4:// URI");
		}

		// Set remainder from the authority
		String authority = uri.getUserInfo();
		if (authority == null) {
			throw new P4CMException("URI is missing parameters");
		}

		Properties cfg = new Properties();

		// Set host and port or use defaults
		String host = (uri.getHost().isEmpty()) ? "perforce" : uri.getHost();
		int port = (uri.getPort() == -1) ? 1666 : uri.getPort();
		String ssl = (protocol.contains("p4s")) ? "ssl:" : "";
		String p4port = ssl + host + ":" + String.valueOf(port);
		cfg.setProperty(P4ConfigType.P4PORT.name(), p4port);

		String parts[] = authority.split(";");
		for (String part : parts) {
			String pair[] = part.split("=");
			if (pair.length == 2) {
				cfg.setProperty(pair[0], pair[1]);
			} else {
				// Set user
				String info[] = part.split(":");
				if (info.length == 2) {
					cfg.setProperty(P4ConfigType.P4USER.name(), info[0]);
					cfg.setProperty(P4ConfigType.P4PASS.name(), info[1]);
				} else {
					cfg.setProperty(P4ConfigType.P4USER.name(), part);
				}
			}
		}

		String path = uri.getPath();
		if (path != null) {
			if (path.startsWith("/files/")) {
				int p = "/files/".length();
				depotPath = path.substring(p);
				if (depotPath.endsWith("/")) {
					depotPath = depotPath.substring(0,
							depotPath.lastIndexOf("/"));
				}
			}
		}

		return cfg;
	}

	private String toUri(P4UriType type, Properties cfg) {

		StringBuffer uri = new StringBuffer();

		String p4user = get(P4ConfigType.P4USER);
		String p4pass = get(P4ConfigType.P4PASS);
		String p4client = get(P4ConfigType.P4CLIENT);
		String p4charset = get(P4ConfigType.P4CHARSET);

		StringBuffer auth = new StringBuffer();
		if (!p4client.isEmpty()) {
			auth.append(P4ConfigType.P4CLIENT.name() + "=");
			auth.append(p4client + ";");
		}
		if (!p4charset.isEmpty()) {
			auth.append(P4ConfigType.P4CHARSET.name() + "=");
			auth.append(p4charset + ";");
		}
		if (!p4user.isEmpty()) {
			if (!p4pass.isEmpty()) {
				auth.append(p4user + ":" + p4pass + "@");
			} else {
				auth.append(p4user + "@");
			}
		}

		String p4port = get(P4ConfigType.P4PORT);
		String[] parts = p4port.split(":", 2);
		if (parts.length != 2) {
			p4port = "localhost:" + p4port;
		}

		switch (type) {
		case P4:
			if (parts[0].startsWith("ssl")) {
				uri.append("p4s://");
				uri.append(auth);
				uri.append(parts[1]);
			} else {
				uri.append("p4://");
				uri.append(auth);
				uri.append(p4port);
			}
			break;

		case P4JAVA:
			if (parts[0].startsWith("ssl")) {
				uri.append("p4javassl://");
				uri.append(parts[1]);
			} else {
				uri.append("p4java://");
				uri.append(p4port);
			}
			break;
		}

		return uri.toString();
	}
}
