package com.perforce.p4simulink;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import java.io.File;
import java.nio.file.Path;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mathworks.cmlink.api.ApplicationInteractor;
import com.mathworks.cmlink.api.ConfigurationManagementException;
import com.mathworks.cmlink.api.version.r16b.CMAdapter;
import com.mathworks.cmlink.api.version.r16b.CMAdapterFactory;
import com.mathworks.cmlink.api.version.r16b.CMRepository;
import com.mathworks.cmlink.util.interactor.NullApplicationInteractor;
import com.perforce.p4simulink.connection.P4Config;

public class P4AdapterFactory implements CMAdapterFactory {

	private static final Logger log = LogManager
			.getLogger(P4AdapterFactory.class.getName());
	private static final ResourceBundle resources = ResourceBundle.getBundle("Labels");

	@Override
	public CMAdapter getAdapterForThisSandboxDir(File file,
			ApplicationInteractor applicationInteractor)
			throws ConfigurationManagementException {

		log.debug("Constructing new P4Adapter");

		return new P4Adapter(applicationInteractor, file);
	}

	@Override
	public String getDescription() {
		return resources.getString("productName.integration.label");
	}

	@Override
	public String getName() {
		return resources.getString("productName.label");
	}

	@Override
	public CMRepository getRepository(
			ApplicationInteractor applicationInteractor) {

		log.debug("Constructing new P4Repository");

		CMRepository repository = null;
		try {
			repository = new P4Repository(applicationInteractor);
		} catch (P4CMException e) {
			log.error("Error constructing a new P4Repository.");
		}
		return repository;
	}

	@Override
	public boolean isDirSandboxForThisAdapter(File file) {

		log.debug("isDirSandboxForThisAdapter - {}", file.getAbsolutePath());

		boolean status = true;
		Path path = file.toPath();
		try {
			new P4Config(path);
		} catch (P4CMException e) {
			status = false;
		}
		
		log.debug("... {}", status);
		return status;
	}

	// ################################################################################

	public CMAdapter getAdapterForThisSandboxDir(File file)
			throws ConfigurationManagementException {
		return getAdapterForThisSandboxDir(file,
				new NullApplicationInteractor());
	}
}
