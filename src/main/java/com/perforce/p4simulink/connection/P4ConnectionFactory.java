package com.perforce.p4simulink.connection;

import java.util.Properties;

import com.perforce.p4java.PropertyDefs;
import com.perforce.p4java.impl.mapbased.rpc.RpcPropertyDefs;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.ServerFactory;

public class P4ConnectionFactory {

	public static IOptionsServer getConnection(P4Uri uri) throws Exception {
		IOptionsServer server = null;
		server = getRawConnection(uri);

		// TODO: Support P4TRUST, using auto accept!

		// Test for SSL connections
		if (uri.isSsl()) {
			String serverTrust = server.getTrust();
			server.addTrust(serverTrust);
		}

		// Open connection
		server.connect();

		// Set Username
		String user = uri.get(P4ConfigType.P4USER);
		server.setUserName(user);

		// Setup unicode
		if (server.getServerInfo().isUnicodeEnabled()) {
			String charset = uri.get(P4ConfigType.P4CHARSET);
			server.setCharsetName(charset);
		}

		return server;
	}

	public static String validate(P4Uri uri) {
		try {
			IOptionsServer server = getConnection(uri);
			server.disconnect();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "OK";
	}

	private static IOptionsServer getRawConnection(P4Uri uri) throws Exception {
		Properties props = System.getProperties();

		// Identify ourselves in server log files.
		P4Identifier id = new P4Identifier();
		props.put(PropertyDefs.PROG_NAME_KEY, id.getProduct());
		props.put(PropertyDefs.PROG_VERSION_KEY, id.getVersion());

		// Allow p4 admin commands.
		props.put(RpcPropertyDefs.RPC_RELAX_CMD_NAME_CHECKS_NICK, "true");

		// disable timeout for slow servers / large db lock times
		props.put(RpcPropertyDefs.RPC_SOCKET_SO_TIMEOUT_NICK, "0");

		// Get a server connection
		String serverUri = uri.getUri(P4UriType.P4JAVA);
		IOptionsServer server;
		server = ServerFactory.getOptionsServer(serverUri, props);
		return server;
	}
}
