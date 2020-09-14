package com.perforce.p4simulink.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.perforce.p4simulink.connection.P4Config;
import com.perforce.p4simulink.connection.P4ConfigType;
import com.perforce.p4simulink.connection.P4Uri;

public class P4ConfigTest {

	@Test
	public void testFindConfig() throws Exception {
		File location = new File("src/test/resources/config");
		P4Config config = new P4Config(location.toPath());

		String uri = "p4://P4CLIENT=guest_sl;guest@public.perforce.com:1666";
		assertEquals(uri, config.toUri());
		
		String java = "p4java://public.perforce.com:1666";
		assertEquals(java, config.toP4Java());

		String user = config.get(P4ConfigType.P4USER);
		assertEquals("guest", user);
		
		String charset = config.get(P4ConfigType.P4CHARSET);
		assertEquals("", charset);
		
		String info = config.toString();
		assertTrue(info.contains("... P4CLIENT=guest_sl"));
	}

	@Test
	public void testCreateConfig() throws Exception {
		String uri = "p4://P4CLIENT=ws;P4CHARSET=utf8;pallen:pass@perforce.com:1666";
		P4Uri p4uri = new P4Uri(uri);

		File location = new File("src/test/resources/test_config");
		location.mkdirs();

		P4Config config = new P4Config(location.toPath(), p4uri);
		Path path = config.getPath();
		assertTrue(Files.exists(path));

		String test = "P4PORT=perforce.com:1666";
		String content = FileUtils.readFileToString(path.toFile(), "utf-8");
		assertTrue(content.contains(test));

		Files.delete(path);
	}

}
