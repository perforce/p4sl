package com.perforce.p4simulink.utils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import com.mathworks.cmlink.api.version.r16b.CMAdapter;
import com.mathworks.cmlink.util.interactor.NullApplicationInteractor;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4simulink.P4Adapter;
import com.perforce.p4simulink.P4AdapterFactory;
import com.perforce.p4simulink.connection.P4Config;
import com.perforce.p4simulink.connection.P4Uri;

public class P4SimulinkTest {

	private static final Logger log = LogManager.getLogger(P4SimulinkTest.class);
	
	protected final static String P4ROOT = "tmp-p4root";
	protected final static String P4PORT = "localhost:1999";
	protected final static P4TestServer p4d;
	protected final static String P4BASE;
	
	protected P4Adapter adapter;
	protected File root;

	static {
		String pwd = System.getProperty("user.dir");
		String P4BIN = pwd + "/p4-bin/";
		p4d = new P4TestServer(P4BIN, P4ROOT, P4PORT);
		P4BASE = pwd + "/ws/";
	}

	@Before
	public void setup() throws Exception {
		log.info("Setting up p4d");

		File ckp = new File("src/test/resources/checkpoint.gz");
		assertTrue(ckp.exists());

		File depot = new File("src/test/resources/depot.tar.gz");
		assertTrue(depot.exists());

		p4d.clean();

		File root = new File(P4ROOT);
		assertTrue(root.exists());

		p4d.restore(ckp);
		p4d.upgrade();
		p4d.extract(depot);

		p4d.start();

		env("admin", "Password", "test.ws");
	}

	private void env(String user, String pass, String client) throws Exception {
		root = new File(P4BASE + client);
		FileUtils.deleteDirectory(root);
		root.mkdirs();

		String uri = "p4://P4CLIENT=" + client + ";" + user + "@" + P4PORT;
		P4Uri p4uri = new P4Uri(uri);

		P4Config config = new P4Config(root.toPath(), p4uri);
		Path path = config.getPath();
		assertTrue(Files.exists(path));

		P4AdapterFactory factory = new P4AdapterFactory();
		boolean ok = factory.isDirSandboxForThisAdapter(root);
		assertTrue(ok);

		NullApplicationInteractor inter = new NullApplicationInteractor();
		CMAdapter cmAdapter = factory.getAdapterForThisSandboxDir(root, inter);

		// connect to Perforce and login
		adapter = (P4Adapter) cmAdapter;
		adapter.openConnection();
		IOptionsServer server = adapter.getServer();
		server.login(pass);
		adapter.connect();

		// set up workspace
		IClient iclient = server.getClient(client);
		iclient.setRoot(root.getAbsolutePath());
		server.updateClient(iclient);
	}

	@After
	public void teardown() throws Exception {
		p4d.stop();
		p4d.clean();
		FileUtils.deleteDirectory(root);
	}
}
