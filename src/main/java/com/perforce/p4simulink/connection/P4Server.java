package com.perforce.p4simulink.connection;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.option.client.SyncOptions;
import com.perforce.p4java.option.server.GetChangelistsOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4simulink.P4CMException;
import com.perforce.p4simulink.ui.P4ClientPanel;
import com.perforce.p4simulink.ui.P4PasswordPane;
import com.perforce.p4simulink.ui.P4PendingPanel;
import com.perforce.p4simulink.ui.P4PopulatePanel;
import com.perforce.p4simulink.util.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class P4Server {

	private static final Logger log = LogManager.getLogger(P4Server.class);
	public static final ResourceBundle resources = ResourceBundle.getBundle("Labels");

	private P4Uri uri;
	protected IOptionsServer server;

	protected void setP4Uri(P4Uri uri) {
		this.uri = uri;
	}

	protected P4Uri getP4Uri() {
		return uri;
	}

	protected String getUser() {
		return uri.get(P4ConfigType.P4USER);
	}

	private String getPort() {
		return uri.get(P4ConfigType.P4PORT);
	}

	/**
	 * Open a P4 connection and set the server object.
	 *
	 * @throws Exception
	 */
	public void openConnection() throws P4CMException {
		try {
			server = P4ConnectionFactory.getConnection(uri);
		} catch (Exception e) {
			throw new P4CMException("Unable to get connection", e);
		}
	}

	/**
	 * Prompt the user for a password if the ticket is invalid or expired.
	 * <p>
	 * TODO: Save ticket back in P4TICKETS for other applications.
	 *
	 * @param frame
	 * @return
	 * @throws Exception
	 */
	public boolean login(Frame frame) throws P4CMException {

		// Login user
		server.setTicketsFilePath(getP4Tickets());

		// Exit OK if logged in or no password is required
		if (isLogin()) {
			return true;
		}

		try {
			if (server.getServerInfo().isSSOAuthRequired()) {

				server.login("");
				return true;
			}
		} catch (Exception e) {
			throw new P4CMException("Unable or login using SSO", e);
		}
		// Open dialog and prompt for password
		P4PasswordPane pane = new P4PasswordPane(getPort(), getUser());
		URL iconUrl = getClass().getResource("/icons/helix-core-48x48.png");
		ImageIcon perforceIcon = new ImageIcon(iconUrl);
		int option = JOptionPane.showConfirmDialog(frame, pane.getFields(),
				resources.getString("enter.password.label"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, perforceIcon);

		if (option == JOptionPane.OK_OPTION) {
			String password = pane.getPassword();
			try {
				server.login(password);
			} catch (Exception e) {
				throw new P4CMException("Unable or login", e);
			}
			if (isLogin()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Fetch the client workspace defined in the URI from the server connection.
	 *
	 * @return client workspace or null if not defined
	 * @throws P4CMException
	 */
	public IClient fetchClient() throws P4CMException {

		// Get client name
		String clientName = uri.get(P4ConfigType.P4CLIENT);
		if (clientName.isEmpty()) {
			throw new P4CMException("P4 Workspace not defined");
		}

		// Look for client, set as current and return
		IClient client = null;
		try {
			client = server.getClient(clientName);
			server.setCurrentClient(client);
		} catch (Exception e) {
			throw new P4CMException("Unable to fetch client Workspace");
		}
		return client;
	}

	/**
	 * Create a new client workspace allowing the user to choose the options
	 * from a dialog.
	 *
	 * @param sandboxRoot
	 * @param frame
	 * @return
	 * @throws P4CMException
	 */
	public IClient createClient(File sandboxRoot, Frame frame)
			throws P4CMException {

		P4ClientPanel c = new P4ClientPanel(uri, sandboxRoot);
		URL iconUrl = getClass().getResource("/icons/helix-core-48x48.png");
		ImageIcon perforceIcon = new ImageIcon(iconUrl);
		int option = JOptionPane.showConfirmDialog(frame, c.createLayout(),
				resources.getString("connection.label"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, perforceIcon);

		if (option != JOptionPane.OK_OPTION) {
			String msg = "You must select create or select an existing Workspace";
			throw new P4CMException(msg);
		}

		// create new Client from dialog
		try {
			Client implClient = c.getClient();
			server.createClient(implClient);
			return fetchClient();
		} catch (Exception e) {
			throw new P4CMException("Unable to fetch client Workspace", e);
		}
	}

	public IClient updateClient(IClient client, Frame frame)
			throws P4CMException {

		P4ClientPanel c = new P4ClientPanel(client);
		URL iconUrl = getClass().getResource("/icons/helix-core-48x48.png");
		ImageIcon perforceIcon = new ImageIcon(iconUrl);
		int option = JOptionPane.showConfirmDialog(frame, c.createLayout(),
				resources.getString("connection.label"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, perforceIcon);

		// no OK, then return original client
		if (option != JOptionPane.OK_OPTION) {
			return client;
		}

		// create new Client from dialog
		try {
			client = c.getClient();
			String msg = server.updateClient(client);
			log.debug("Update Client - {}", msg);
		} catch (Exception e) {
			throw new P4CMException("Unable to update client Workspace", e);
		}
		return client;
	}

	public int selectPendingChange(int current, Frame frame)
			throws P4CMException {
		try {
			GetChangelistsOptions cngOpts = new GetChangelistsOptions();
			String clientName = server.getCurrentClient().getName();
			cngOpts.setClientName(clientName);
			String userName = getUser();
			cngOpts.setUserName(userName);
			cngOpts.setTruncateDescriptions(true);

			cngOpts.setType(IChangelist.Type.PENDING);
			List<IChangelistSummary> pending = server.getChangelists(null,
					cngOpts);

			cngOpts.setType(IChangelist.Type.SHELVED);
			List<IChangelistSummary> shelved = server.getChangelists(null,
					cngOpts);

			pending.addAll(shelved);

			P4PendingPanel p = new P4PendingPanel(current, pending);
			URL iconUrl = getClass().getResource("/icons/helix-core-48x48.png");
			ImageIcon perforceIcon = new ImageIcon(iconUrl);
			int option = JOptionPane.showConfirmDialog(frame, p.createLayout(),
					"Pending Changelist", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, perforceIcon);

			// no OK, then return original change
			if (option != JOptionPane.OK_OPTION) {
				return current;
			}

			current = p.getChange();
		} catch (Exception e) {
			throw new P4CMException("Unable to update client Workspace", e);
		}

		return current;
	}

	public void populateClient(IClient client, Frame frame)
			throws P4CMException {

		try {
			String root = client.getRoot() + File.separator + "...";
			String path = toDepot(root);
			P4PopulatePanel p = new P4PopulatePanel(path);

			URL iconUrl = getClass().getResource("/icons/helix-core-48x48.png");
			ImageIcon perforceIcon = new ImageIcon(iconUrl);
			int option = JOptionPane.showConfirmDialog(frame, p.createLayout(),
					"Get Revision", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, perforceIcon);

			// if OK, then sync
			if (option == JOptionPane.OK_OPTION) {
				path = p.getPath() + p.getRev();
				List<IFileSpec> fileSpecs = FileSpecBuilder
						.makeFileSpecList(path);

				SyncOptions syncOpts = new SyncOptions();
				syncOpts.setForceUpdate(p.isForce());
				syncOpts.setSafetyCheck(p.isSafe());
				List<IFileSpec> synced = client.sync(fileSpecs, syncOpts);

				Files.validateFileSpecs(synced, "file(s) up-to-date.",
						" - no such file(s).", "- file(s) not on client.");
			}
		} catch (Exception e) {
			throw new P4CMException("Unable to sync revisions", e);
		}
	}

	private String toDepot(String path) throws P4CMException {
		IClient client = server.getCurrentClient();

		List<IFileSpec> fileSpecs = FileSpecBuilder.makeFileSpecList(path);
		try {
			List<IFileSpec> syntax = client.where(fileSpecs);
			IFileSpec depot = syntax.get(0);
			return depot.getDepotPathString();
		} catch (Exception e) {
			throw new P4CMException("Location not under client", e);
		}
	}

	private boolean isLogin() throws P4CMException {
		String status = "";

		try {
			status = server.getLoginStatus();
		} catch (P4JavaException e) {
			throw new P4CMException("Unable to get login status", e);
		}

		if (status.contains("not necessary")) {
			return true;
		}
		if (status.contains("ticket expires in")) {
			return true;
		}
		// If there is a broker or something else that swallows the message
		if (status.isEmpty()) {
			return true;
		}

		return false;
	}

	private String getP4Tickets() {
		String P4TICKETS = System.getenv("P4TICKETS");
		if (P4TICKETS != null) {
			return P4TICKETS;
		}

		String OS = System.getProperty("os.name").toLowerCase();
		String HOME = System.getProperty("user.home");

		String p4tickets = HOME + "/.p4tickets";
		if (OS.contains("win")) {
			p4tickets = HOME + "\\p4tickets.txt";
		}
		return p4tickets;
	}
}
