package com.perforce.p4simulink;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 * <p>
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import com.mathworks.cmlink.api.ApplicationInteractor;
import com.mathworks.cmlink.api.ConfigurationManagementException;
import com.mathworks.cmlink.api.RepositorySupportedFeature;
import com.mathworks.cmlink.api.version.r16b.CMRepository;
import com.perforce.p4simulink.connection.P4Config;
import com.perforce.p4simulink.connection.P4Uri;
import com.perforce.p4simulink.ui.P4ConnectionPanel;
import com.perforce.p4simulink.util.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.ResourceBundle;

public class P4Repository extends P4Interactor implements CMRepository {

	protected static final Logger log = LogManager.getLogger(P4Repository.class
			.getName());
	private static final ResourceBundle resources = ResourceBundle.getBundle("Labels");

	private final Collection<RepositorySupportedFeature> supportedFeatures;

	/**
	 * Class constructor.
	 *
	 * @param applicationInteractor
	 * @throws P4CMException
	 */
	public P4Repository(ApplicationInteractor applicationInteractor)
			throws P4CMException {

		super(applicationInteractor);

		log.debug("Construct P4Repository() - {}");

		terminator = applicationInteractor.getTerminator();
		supportedFeatures = EnumSet
				.of(RepositorySupportedFeature.REPOSITORY_BROWSER);
	}

	/**
	 * Open the Perforce Connection dialog to generate a Perforce URI.
	 *
	 * This method is invoked when a user clicks on the 'Change...' button for
	 * the 'Repository Path'
	 *
	 * @param currentUri
	 *            Current URI from parent dialog.
	 * @param parentFrame
	 * @return The resulting URI String from the connection dialog.
	 */
	@Override
	public String browseForRepositoryPath(String currentUri, Frame parentFrame) {

		log.debug("browseForRepositoryPath() - {}", currentUri);

		// Open Perforce connection dialog
		URL connUrl = getClass().getResource("/icons/helix-core-48x48.png");
		ImageIcon perforceIcon = new ImageIcon(connUrl);
		P4ConnectionPanel s = new P4ConnectionPanel(currentUri);
		int option = JOptionPane.showConfirmDialog(parentFrame,
				s.createLayout(),  resources.getString("connection.label"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, perforceIcon);

		if (option != JOptionPane.CANCEL_OPTION) {
			// Use URI from connection dialog
			currentUri = s.getPath();
			try {
				log.debug("Setting uri in P4Server");
				P4Uri uri = new P4Uri(currentUri);
				super.setP4Uri(uri);
			} catch (P4CMException e) {
				//TODO Throw better message to user than this
				throw new UnsupportedOperationException(e);
			}
		}
		return currentUri;
	}

	/**
	 * Not implemented.
	 *
	 * @param folder
	 */
	@Override
	public void convertFolderToSandbox(File folder) {
		log.debug("convertFolderToSandbox() - {}", folder.getAbsolutePath());
		throw new UnsupportedOperationException();
	}

	/**
	 * Supported repository features
	 *
	 * @param feature
	 * @return true if feature is supported.
	 */
	@Override
	public boolean isFeatureSupported(RepositorySupportedFeature feature) {
		boolean supported = supportedFeatures.contains(feature);
		log.debug("isFeatureSupported() - {} {}", feature, supported);
		return supported;
	}

	/**
	 * Setup the Perforce connection and/or populate.
	 *
	 * This method is invoked when a user clicks on the 'Retrieve' button. For
	 * existing workspaces connect to Perforce and login for new workspaces
	 * connect and populate content.
	 *
	 * @param currentUri
	 *            a Perforce Uri representing the connection
	 * @param sandboxRoot
	 *            the local root of the Workspace
	 */
	@Override
	public void retrieveSandboxFromRepository(String currentUri,
	                                          File sandboxRoot) throws ConfigurationManagementException {

		log.debug("retrieveSandboxFromRepository() - {} {}", currentUri,
				sandboxRoot.getAbsolutePath());

		// Set URI for session
		P4Uri uri = new P4Uri(currentUri);
		setP4Uri(uri);

		// Create a P4CONFIG file
		new P4Config(sandboxRoot.toPath(), uri);

		// Connect and login to Perforce
		openConnection();
		login(parentFrame);

		// Fetch client
		client = fetchClient();

		// Check client exists
		if (client == null) {
			client = createClient(sandboxRoot, parentFrame);

			// Populate workspace
			populateClient(client, parentFrame);
		}
	}

	// ################################################################################

	public Collection<String> getTags(String repositoryPath)
			throws ConfigurationManagementException {
		log.debug("getTags() - {}", repositoryPath);
		Collection<String> tags = new ArrayList<>();
		try {
			File repositoryFile = new File(repositoryPath);
			P4Adapter p4 = (P4Adapter) new P4AdapterFactory()
					.getAdapterForThisSandboxDir(repositoryFile);
			tags = p4.getTags(repositoryFile);
		} catch (ConfigurationManagementException e) {
			Logging.logException(log, e, true);
		}
		return tags;
	}
}
