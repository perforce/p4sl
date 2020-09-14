package com.perforce.p4simulink;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mathworks.cmlink.api.ApplicationInteractor;
import com.mathworks.cmlink.api.ConfigurationManagementException;
import com.mathworks.cmlink.api.InteractorSupportedFeature;
import com.mathworks.cmlink.api.Terminator;
import com.mathworks.cmlink.api.customization.CustomizationWidgetFactory;
import com.mathworks.cmlink.api.version.r16b.CMInteractor;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.ServerStatus;
import com.perforce.p4simulink.connection.P4Identifier;
import com.perforce.p4simulink.connection.P4Server;

public class P4Interactor extends P4Server implements CMInteractor {

	protected static final Logger log = LogManager.getLogger(P4Interactor.class
			.getName());

	protected final Frame parentFrame;

	protected IClient client;

	private int changeID;

	public P4Interactor(ApplicationInteractor applicationInteractor)
			throws P4CMException {

		log.debug("Construct P4Interactor() - {}");

		parentFrame = applicationInteractor.getParentFrame();
		terminator = applicationInteractor.getTerminator();
	}

	@Override
	public void disconnect() throws ConfigurationManagementException {
		try {
			if (isReady()) {
				server.disconnect();
			}
		} catch (Exception e) {
			log.error(e);
			throw new ConfigurationManagementException(e);
		}
	}

	/*
	Simple connect method without login. No need to login too early.
	 */
	@Override
	public void connect() throws ConfigurationManagementException {
		try {
			if (getP4Uri() != null) {
				openConnection();

				// login to server
				login(parentFrame);

				// Fetch client
				client = fetchClient();
			}
		} catch (Exception e) {
			log.error(e);
			throw new ConfigurationManagementException(e);
		}
	}

	@Override
	public String getShortSystemName() {
		return P4Server.resources.getString("productName.label");
	}

	@Override
	public String getSystemName() {
		P4Identifier identifier = new P4Identifier();
		String product = identifier.getProduct();
		String version = identifier.getVersion();
		return product + " (" + version + ")";
	}

	@Override
	public boolean isFeatureSupported(
			InteractorSupportedFeature interactorSupportedFeature) {
		return supportedFeatures.contains(interactorSupportedFeature);
	}

	@Override
	public boolean isReady() {
		return server != null && server.isConnected()
				&& server.getStatus() == ServerStatus.READY;
	}

	protected void setChangeID(int change) {
		changeID = change;
	}

	public int getChangeID() {
		// exit early if default change is selected
		if (changeID == 0) {
			return changeID;
		}

		// check change can be used
		try {
			IChangelist change = server.getChangelist(changeID);

			// check if pending
			ChangelistStatus status = change.getStatus();
			if (status != ChangelistStatus.PENDING) {
				changeID = 0;
				return changeID;
			}

			// check ownership
			String owner = change.getUsername();
			if (!getUser().equalsIgnoreCase(owner)) {
				changeID = 0;
				return changeID;
			}

			// check client workspace
			String currentID = client.getName();
			String clientID = change.getClientId();
			if (!currentID.equals(clientID)) {
				changeID = 0;
				return changeID;
			}
		} catch (Exception e) {
			changeID = 0;
			return changeID;
		}
		return changeID;
	}

	// ################################################################################

	// list of supported features
	private static final Collection<InteractorSupportedFeature> supportedFeatures = EnumSet
			.of(InteractorSupportedFeature.CONNECTION);

	protected Terminator terminator;
	protected ApplicationInteractor applicationInteractor;

	public void setTerminator(Terminator terminator) {
		this.terminator = terminator;
	}

	@Override
	public void buildCustomActions(CustomizationWidgetFactory widgetFactory) {

		log.debug("buildCustomActions()");

	}

	/**
	 * Converts a collection of files into a list of IFileSpec objects. Any
	 * directories contained in the input list are expanded into a list of files
	 * contained within them (if any).
	 *
	 * @param files
	 *            Collection of files for which we want to get file specs. Note
	 *            that directories are expanded into the list of files contain
	 *            in them (if any).
	 * @return List of IFileSpec objects for the input list.
	 */
	public List<IFileSpec> toSpec(Collection<File> files) {
		List<IFileSpec> specs = new ArrayList<>();
		try {
			HashSet<String> filePaths = new HashSet<>();
			for (File file : files) {
				// adding a directory means we should add the files under it
				if (file.isDirectory()) {
					filePaths.add(file.getCanonicalPath() + File.separator
							+ "...");
				} else {

					String filename = file.getCanonicalPath();
					if (filename.contains("#") || filename.contains("@")
							|| filename.contains("%")) {
						filename = filename.replace("%", "%25");
						filename = filename.replace("@", "%40").replace("#",
								"%23");
					}

					// regular file
					filePaths.add(filename);
				}
			}

			if (!filePaths.isEmpty()) {
				specs = FileSpecBuilder.makeFileSpecList(filePaths
						.toArray(new String[filePaths.size()]));
			}
		} catch (IOException e) {
			log.error("IO Exception building list of file specs: "
					+ e.getLocalizedMessage());
		}
		return specs;
	}

	public List<IFileSpec> toSpec(File file) {
		return toSpec(Collections.singletonList(file));
	}
}
