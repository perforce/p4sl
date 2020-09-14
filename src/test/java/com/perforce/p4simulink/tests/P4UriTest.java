package com.perforce.p4simulink.tests;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import com.perforce.p4simulink.connection.P4ConfigType;
import com.perforce.p4simulink.connection.P4Uri;
import com.perforce.p4simulink.connection.P4UriType;

public class P4UriTest {

	@Test
	public void testUriToConfig() throws Exception {
		String uri = "p4://P4CLIENT=ws;P4CHARSET=utf8;pallen@perforce.com:1666";
		P4Uri p4uri = new P4Uri(uri);

		assertEquals("perforce.com:1666", p4uri.get(P4ConfigType.P4PORT));
		assertEquals("ws", p4uri.get(P4ConfigType.P4CLIENT));
		assertEquals("utf8", p4uri.get(P4ConfigType.P4CHARSET));
		assertEquals("pallen", p4uri.get(P4ConfigType.P4USER));
	}

	@Test
	public void testUriWithPass() throws Exception {
		String uri = "p4://P4CLIENT=ws;P4CHARSET=utf8;pallen:pass@perforce.com:1666";
		P4Uri p4uri = new P4Uri(uri);

		assertEquals("perforce.com:1666", p4uri.get(P4ConfigType.P4PORT));
		assertEquals("ws", p4uri.get(P4ConfigType.P4CLIENT));
		assertEquals("pallen", p4uri.get(P4ConfigType.P4USER));
		assertEquals("pass", p4uri.get(P4ConfigType.P4PASS));
	}

	@Test
	public void testUriWithSSL() throws Exception {
		String uri = "p4s://P4CLIENT=ws;pallen@perforce.com:1666/";
		P4Uri p4uri = new P4Uri(uri);

		assertEquals("ssl:perforce.com:1666", p4uri.get(P4ConfigType.P4PORT));
		assertEquals("ws", p4uri.get(P4ConfigType.P4CLIENT));
		assertEquals("pallen", p4uri.get(P4ConfigType.P4USER));
	}

	@Test
	public void testConfigToUri() throws Exception {
		Properties cfg = new Properties();
		cfg.setProperty(P4ConfigType.P4PORT.name(), "localhost:1666");
		cfg.setProperty(P4ConfigType.P4USER.name(), "pallen");
		cfg.setProperty(P4ConfigType.P4CLIENT.name(), "ws");

		P4Uri p4uri = new P4Uri(cfg);

		String uri = "p4://P4CLIENT=ws;pallen@localhost:1666";
		assertEquals(uri, p4uri.getUri(P4UriType.P4));
	}

	@Test
	public void testConfigToUriWithSsl() throws Exception {
		Properties cfg = new Properties();
		cfg.setProperty(P4ConfigType.P4PORT.name(), "ssl:localhost:1666");
		cfg.setProperty(P4ConfigType.P4USER.name(), "pallen");
		cfg.setProperty(P4ConfigType.P4CLIENT.name(), "ws");
		cfg.setProperty(P4ConfigType.P4CHARSET.name(), "utf8");

		P4Uri p4uri = new P4Uri(cfg);

		String uri = "p4s://P4CLIENT=ws;P4CHARSET=utf8;pallen@localhost:1666";
		assertEquals(uri, p4uri.getUri(P4UriType.P4));
	}
}
