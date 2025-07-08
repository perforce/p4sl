package com.perforce.p4simulink;

/**
 * Copyright (C) 2014 Perforce Software. All rights reserved.
 *
 * Please see LICENSE.txt in top-level folder of this distribution.
 */

import com.mathworks.cmlink.api.AdapterSupportedFeature;
import com.mathworks.cmlink.api.ApplicationInteractor;
import com.mathworks.cmlink.api.ConfigurationManagementException;
import com.mathworks.cmlink.api.ConflictedRevisions;
import com.mathworks.cmlink.api.IntegerRevision;
import com.mathworks.cmlink.api.Revision;
import com.mathworks.cmlink.api.customization.CoreAction;
import com.mathworks.cmlink.api.customization.CustomizationWidgetFactory;
import com.mathworks.cmlink.api.customization.file.CustomizationFileActionFactory;
import com.mathworks.cmlink.api.version.r16b.CMAdapter;
import com.mathworks.cmlink.api.version.r16b.FileState;
import com.mathworks.cmlink.util.status.NoBaseConflictedRevision;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.ILabelSummary;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.option.changelist.SubmitOptions;
import com.perforce.p4java.option.client.AddFilesOptions;
import com.perforce.p4java.option.client.DeleteFilesOptions;
import com.perforce.p4java.option.client.EditFilesOptions;
import com.perforce.p4java.option.client.ReconcileFilesOptions;
import com.perforce.p4java.option.client.ReopenFilesOptions;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.p4java.option.client.RevertFilesOptions;
import com.perforce.p4java.option.client.SyncOptions;
import com.perforce.p4java.option.server.GetExtendedFilesOptions;
import com.perforce.p4java.option.server.GetFileContentsOptions;
import com.perforce.p4java.option.server.GetRevisionHistoryOptions;
import com.perforce.p4java.option.server.MoveFileOptions;
import com.perforce.p4java.option.server.OpenedFilesOptions;
import com.perforce.p4java.option.server.TagFilesOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4simulink.connection.P4Config;
import com.perforce.p4simulink.connection.P4FileState;
import com.perforce.p4simulink.connection.P4Uri;
import com.perforce.p4simulink.util.Files;
import com.perforce.p4simulink.util.Logging;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class P4Adapter extends P4Interactor implements CMAdapter {

	private static final Logger log = LogManager.getLogger(P4Adapter.class);

	private final Collection<AdapterSupportedFeature> supportedFeatures;

	/**
	 * Class constructor.
	 *
	 * @param applicationInteractor
	 * @throws P4CMException
	 */
	public P4Adapter(ApplicationInteractor applicationInteractor, File file)
			throws P4CMException {

		super(applicationInteractor);

		P4Config config = new P4Config(file.toPath());
		String uriString = config.toUri();
		P4Uri uri = new P4Uri(uriString);
		setP4Uri(uri);

		log.debug("Construct P4Adapter() - {}", file);

		supportedFeatures = EnumSet.of(
				AdapterSupportedFeature.FOLDERS_NOT_STORED,
				AdapterSupportedFeature.ALTERNATE_UPDATE,
				AdapterSupportedFeature.LOCK,
				AdapterSupportedFeature.GET_REVISION,
				AdapterSupportedFeature.IS_LATEST,
				AdapterSupportedFeature.MOVE,
				AdapterSupportedFeature.LATEST_REVISION,
				AdapterSupportedFeature.RESOLVE,
				AdapterSupportedFeature.GET_CONFLICT_REVISION,
				AdapterSupportedFeature.TAG);
	}

	@Override
	public boolean isFeatureSupported(
			AdapterSupportedFeature adapterSupportedFeature) {
		return supportedFeatures.contains(adapterSupportedFeature);
	}

	@Override
	public String getRepositorySpecifier(File sandboxDirectory)
			throws ConfigurationManagementException {

		log.debug("getRepositorySpecifier - {}",
				sandboxDirectory.getAbsolutePath());

		P4Config config = new P4Config(sandboxDirectory.toPath());
		String uriString = config.toUri();
		return uriString;
	}

	@Override
	public Map<File, FileState> getFileState(Collection<File> files)
			throws ConfigurationManagementException {

		log.debug("getFileState() - {}", files);

		Map<File, FileState> map = new HashMap<File, FileState>();
		for (File file : files) {
			// Skip directories
			if (file.isDirectory()) {
				continue;
			}
			try {
				// get FileSpec for file
				IFileSpec fileSpec = new FileSpec(file.getCanonicalPath());
				List<IFileSpec> fileSpecs = Arrays.asList(fileSpec);

				// check for modified files
				isModified(fileSpecs);

				// get FileState for FileSpec
				for (P4FileState state : fstat(fileSpecs)) {
					if (state.getFile() == null) {
						state.setFile(file);
					}
					map.put(file, state);
					log.debug("... {}", state);
				}
			} catch (IOException e) {
				throw new P4CMException("IOException" + e);
			}
		}
		return map;
	}

	@Override
	public Map<File, FileState> getStateForAllKnownFilesRecursively(File file)
			throws ConfigurationManagementException {

		log.debug("getStateForAllKnownFilesRecursively() - {}", file);

		Map<File, FileState> map = new HashMap<File, FileState>();

		try {
			// get FileSpec for recursive path
			String path = file.getCanonicalPath() + File.separator + "...";
			List<IFileSpec> pathSpec = FileSpecBuilder.makeFileSpecList(path);

			// get FileState for FileSpec
			for (P4FileState state : fstat(pathSpec)) {
				map.put(state.getFile(), state);
				log.debug("... {}", state);
			}
		} catch (IOException e) {
			throw new P4CMException("IOException" + e);
		}
		return map;
	}

	/**
	 * Run a 'p4 fstat' on an individual file returning a file state object.
	 *
	 * @param fileSpecs
	 * @return P4FileState
	 * @throws P4CMException
	 */
	private List<P4FileState> fstat(List<IFileSpec> fileSpecs)
			throws P4CMException {

		List<P4FileState> list = new ArrayList<P4FileState>();
		try {
			// run 'p4 fstat' on FileSpec
			GetExtendedFilesOptions fstatOpts = new GetExtendedFilesOptions();
			List<IExtendedFileSpec> stats = server.getExtendedFiles(fileSpecs,
					fstatOpts);

			// generate FileState and add to list
			for (IExtendedFileSpec exSpec : stats) {
				FileSpecOpStatus status = exSpec.getOpStatus();
				if (status != FileSpecOpStatus.VALID) {
					exSpec = null;
				}
				P4FileState fstat = new P4FileState(exSpec);
				list.add(fstat);
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			throw new P4CMException("fstat", e);
		}
		return list;
	}

	/**
	 * Modified files are resolved to edit in the current pending change.
	 *
	 * @param fileSpecs
	 * @throws ConfigurationManagementException
	 */
	private void isModified(List<IFileSpec> fileSpecs)
			throws ConfigurationManagementException {
		try {

			// Find modified files
			ReconcileFilesOptions statusOpts = new ReconcileFilesOptions();
			statusOpts.setChangelistId(getChangeID());
			statusOpts.setOutsideEdit(true);
			statusOpts.setNoUpdate(true);
			statusOpts.setLocalSyntax(true);
			List<IFileSpec> status = client.reconcileFiles(fileSpecs, statusOpts);
			Files.validateFileSpecs(status, "no file(s) to reconcile");

			for (IFileSpec spec : status) {
				if (spec.getOpStatus().equals(FileSpecOpStatus.VALID) && !isOpened(spec)) {
					// If not opened, open it for edit
					edit(Arrays.asList(new File(spec.getLocalPathString())));
				}
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	private boolean isOpened(IFileSpec spec) throws P4JavaException {
		OpenedFilesOptions openedOpts = new OpenedFilesOptions();
		List<IFileSpec> opened = client.openedFiles(Arrays.asList(spec), openedOpts);
		for(IFileSpec open: opened) {
			if (open.getOpStatus().equals(FileSpecOpStatus.INFO)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * [p4 sync file#head]
	 *
	 * @param files
	 *            files for which we want the latest version
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void getLatest(Collection<File> files)
			throws ConfigurationManagementException {

		log.debug("getLatest() - {}", files);

		List<IFileSpec> specs = toSpec(files);
		try {
			// basic sync
			SyncOptions syncOpts = new SyncOptions();
			List<IFileSpec> synced = client.sync(specs, syncOpts);

			// ignore cases where we've already got the latest, and/or there are
			// no files to check out
			Files.validateFileSpecs(synced, "file(s) up-to-date.",
					" - no such file(s).", "- file(s) not on client.");
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	/**
	 * Determines whether the user's workspace contains the latest version of
	 * files from the depot.
	 *
	 * @param files
	 *            list of files to check
	 * @return Mapping of file to boolean where true means the workspace has the
	 *         latest version, false if not
	 * @throws ConfigurationManagementException
	 */
	@Override
	public Map<File, Boolean> isLatest(Collection<File> files)
			throws ConfigurationManagementException {

		log.debug("isLatest() - {}", files);

		// TODO: determine if this is truly necessary - with large numbers of
		// files, I was getting exceptions
		if (files.size() > 20) {
			files = Collections.singletonList(new File(client.getRoot()));
		}
		Map<File, Boolean> isLatest = new HashMap<>();

		// no work if no files were asked for
		if (files.size() == 0) {
			return isLatest;
		}

		// get local file state, marking any modified/missing or out of date
		// files as not latest
		Map<File, FileState> states = getFileState(files);
		for (File file : states.keySet()) {
			P4FileState state = (P4FileState) states.get(file);
			isLatest.put(file, state.isLatest());
		}

		return isLatest;
	}

	/**
	 * [p4 sync file#rev]
	 *
	 * @param fileRevisionMap
	 *            List of files and revisions to sync.
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void getRevision(Map<File, Revision> fileRevisionMap)
			throws ConfigurationManagementException {

		log.debug("getRevision() - {}", fileRevisionMap);

		try {
			for (Map.Entry<File, Revision> entry : fileRevisionMap.entrySet()) {
				File file = entry.getKey();
				Revision rev = entry.getValue();

				String strRev = rev.getStringRepresentation();
				int revNumber = Integer.parseInt(strRev);

				String path = file.getAbsolutePath() + "#" + revNumber;
				List<IFileSpec> spec = FileSpecBuilder.makeFileSpecList(path);

				SyncOptions options = new SyncOptions();
				List<IFileSpec> synced = client.sync(spec, options);

				Files.validateFileSpecs(synced, "file(s) up-to-date.");
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	/**
	 * [p4 sync ...#head]
	 *
	 * @param file
	 *            Sync all files under the specified path.
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void update(File file) throws ConfigurationManagementException {

		log.debug("update() - {}", file);

		try {
			String path = file.getCanonicalPath() + File.separator + "...";
			List<IFileSpec> pathSpec = FileSpecBuilder.makeFileSpecList(path);

			SyncOptions options = new SyncOptions();
			List<IFileSpec> synced = client.sync(pathSpec, options);

			Files.validateFileSpecs(synced, "file(s) up-to-date.");
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	/**
	 * [p4 add]
	 *
	 * @param files
	 *            list of files to add to the depot
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void add(Collection<File> files)
			throws ConfigurationManagementException {

		log.debug("add() - {}", files);

		List<IFileSpec> specs = toSpec(files);
		try {
			if (!specs.isEmpty()) {
				AddFilesOptions opts = new AddFilesOptions();
				opts.setChangelistId(getChangeID());
				List<IFileSpec> added = client.addFiles(specs, opts);

				Files.validateFileSpecs(added, "empty, assuming text",
						"can't add existing file");
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	/**
	 * [p4 sync -f] and [p4 edit]
	 *
	 * @param files
	 *            Files to check out of source control (sync and open for edit).
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void checkout(Collection<File> files)
			throws ConfigurationManagementException {
		log.debug("checkout()");
		log.debug(files);
		try {
			// sync the latest files down
			getLatest(files);

			// mark everything for edit
			edit(files);
		} catch (ConfigurationManagementException e) {
			Logging.logException(log, e, true);
		}
	}

	/**
	 * Convenience method that opens files in P4 for edit.
	 *
	 * @param files
	 *            Files to edit
	 * @throws ConfigurationManagementException
	 */
	public void edit(Collection<File> files)
			throws ConfigurationManagementException {

		log.debug("edit() - {}", files);

		List<IFileSpec> specs = toSpec(files);
		try {
			if (!specs.isEmpty()) {
				EditFilesOptions editOpts = new EditFilesOptions();
				editOpts.setChangelistId(getChangeID());
				List<IFileSpec> edited = client.editFiles(specs, editOpts);

				Files.validateFileSpecs(edited, "empty, assuming text",
						" - currently opened for edit",
						" - file(s) not on client.");

				// move files open in another changelist
				// TODO don't think this is needed, change swap should update
				// status and then this should be tested for all operations!
				ReopenFilesOptions openOpts = new ReopenFilesOptions();
				openOpts.setChangelistId(getChangeID());
				client.reopenFiles(specs, openOpts);
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	/**
	 * [p4 revert]
	 *
	 * @param files
	 *            List of files to revert.
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void uncheckout(Collection<File> files)
			throws ConfigurationManagementException {
		log.debug("uncheckout()");
		log.debug(files);
		List<IFileSpec> specs = toSpec(files);
		try {
			RevertFilesOptions options = new RevertFilesOptions();
			List<IFileSpec> reverted = client.revertFiles(specs, options);
			Files.validateFileSpecs(reverted,
					"file(s) not opened on this client");
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	/**
	 * uncheckout() then [p4 delete -k]
	 *
	 * @param files
	 *            List of files to remove from source control.
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void remove(Collection<File> files)
			throws ConfigurationManagementException {

		log.debug("remove() - {}", files);

		List<IFileSpec> specs = toSpec(files);
		try {
			// reopen and revert file
			uncheckout(files);

			// delete, but leave content?
			DeleteFilesOptions delOpts = new DeleteFilesOptions();
			delOpts.setChangelistId(getChangeID());
			delOpts.setBypassClientDelete(true);
			List<IFileSpec> deleted = client.deleteFiles(specs, delOpts);
			Files.validateFileSpecs(deleted, "files(s) not on client",
					"clobber writable");

			// Leaving deleted files writable
			for (File file: files) {
				file.setWritable(true);
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	/**
	 * Moves file to file2 within the depot. This is used in renaming operations
	 * within Simulink.
	 *
	 * @param src
	 *            Source file
	 * @param dst
	 *            Destination file
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void moveFile(File src, File dst)
			throws ConfigurationManagementException {

		log.debug("moveFile() {} {}", src, dst);

		if (src.isDirectory()) {
			moveFiles(src, dst);
			return;
		}

		IFileSpec srcSpec = new FileSpec(src.getAbsolutePath());
		IFileSpec dstSpec = new FileSpec(dst.getAbsolutePath());
		try {
			// ensure file is opened for add or edit
			// TODO check if open and revert or error
			FileAction srcAct = srcSpec.getAction();
			if (srcAct != FileAction.ADD && srcAct != FileAction.EDIT) {

				List<IFileSpec> files = Arrays.asList(srcSpec);
				EditFilesOptions editOpts = new EditFilesOptions();
				editOpts.setChangelistId(getChangeID());
				List<IFileSpec> edited = client.editFiles(files, editOpts);
				Files.validateFileSpecs(edited);
			}

			MoveFileOptions moveOpts = new MoveFileOptions();
			moveOpts.setChangelistId(getChangeID());
			List<IFileSpec> moved = server.moveFile(srcSpec, dstSpec, moveOpts);
			Files.validateFileSpecs(moved);
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (Exception e) {
			if (e instanceof P4JavaException) {
				Logging.logException(log, new P4JavaException(e), true);
			}
		}
	}

	private void moveFiles(File src, File dst)
			throws ConfigurationManagementException {

		src.renameTo(dst);

		IFileSpec srcSpec = new FileSpec(src.getAbsolutePath() + "/...");
		IFileSpec dstSpec = new FileSpec(dst.getAbsolutePath() + "/...");

		try {
			// revert files before rec

			List<IFileSpec> recSpecs = new ArrayList<>();
			recSpecs.add(srcSpec);
			recSpecs.add(dstSpec);

			RevertFilesOptions revertOpts = new RevertFilesOptions();
			client.revertFiles(recSpecs,revertOpts);

			ReconcileFilesOptions statusOpts = new ReconcileFilesOptions();
			statusOpts.setChangelistId(getChangeID());

			List<IFileSpec> status = client.reconcileFiles(recSpecs, statusOpts);
			Files.validateFileSpecs(status, "no file(s) to reconcile");

		} catch (Exception e) {
			if (e instanceof P4JavaException) {
				Logging.logException(log, new P4JavaException(e), true);
			}
		}
	}

	/**
	 * According to the SVN example, this function simply resolves conflicts by
	 * accepting the working file. Since this seems to be what Simulink expects,
	 * this is what I implemented.
	 *
	 * @param file
	 *            File on which we are resolving the conflict.
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void resolveConflict(File file)
			throws ConfigurationManagementException {

		log.debug("resolveConflict() - {}", file);

		List<IFileSpec> spec = toSpec(file);
		// accept the working file
		try {
			ResolveFilesAutoOptions resolveOpts = new ResolveFilesAutoOptions();
			resolveOpts.setAcceptYours(true);
			List<IFileSpec> files = client.resolveFilesAuto(spec, resolveOpts);

			Files.validateFileSpecs(files);
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		}
	}

	/**
	 * [p4 submit]
	 *
	 * @param files
	 *            Files to submit to P4
	 * @param description
	 *            Description for this commit.
	 * @throws com.mathworks.cmlink.api.ConfigurationManagementException
	 */
	@Override
	public void checkin(Collection<File> files, String description)
			throws ConfigurationManagementException {

		log.debug("checkin() {} {}", description, files);

		// Sync files to latest to schedule resolve
		getLatest(files);

		List<IFileSpec> specs = toSpec(files);

		try {
			IChangelist change = null;
			if (getChangeID() > 0) {
				// use existing pending change
				change = server.getChangelist(getChangeID());
				change.setDescription(description);
			} else {
				// create new change for default
				change = new Changelist();
				change.setDescription(description);
				change = client.createChangelist(change);

				// move files from default change
				ReopenFilesOptions reopenOpts = new ReopenFilesOptions();
				reopenOpts.setChangelistId(change.getId());
				client.reopenFiles(specs, reopenOpts);
			}

			// submit change
			SubmitOptions submitOpts = new SubmitOptions();
			List<IFileSpec> submit = change.submit(submitOpts);

			// Set current change to default or pending change for failed submit
			int c = Files.changeFailedSubmit(submit);
			setChangeID(c);

			Files.validateFileSpecs(submit);
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		} catch (Exception e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
			e.printStackTrace();
		}
	}

	/**
	 * Check in a single file with the given description.
	 *
	 * @param file
	 *            File to submit to P4.
	 * @param description
	 *            Description for this commit.
	 * @throws ConfigurationManagementException
	 */
	@Override
	public void checkin(File file, String description)
			throws ConfigurationManagementException {
		log.debug("checkin() - Single File: {} {}", description,
				file.toString());
		try {
			checkin(Collections.singletonList(file), description);
		} catch (ConfigurationManagementException e) {
			Logging.logException(log, e, true);
		}
	}

	@Override
	public boolean canCommitEmpty() throws ConfigurationManagementException {
		return true;
	}

	/**
	 * Forbidden filenames
	 *
	 * http://www.perforce.com/perforce/r14.1/manuals/cmdref/filespecs.html
	 *
	 * @return List of strings representing illegal filename formats.
	 */
	@Override
	public Collection<String> getForbiddenFileNames() {
		Collection<String> names = new HashSet<>();
		// P4-specific forbidden patterns
		names.add(".*@.*");
		names.add(".*#.*");
		names.add(".*\\*.*");
		names.add(".*\\.\\.\\..*");
		names.add(".*%.*");
		names.add(".*\\/.*");
		return names;
	}

	/**
	 * p4 print a file at a specified location to allow MatLab to diff the
	 * content.
	 *
	 * @param fileRevisionMap
	 *            A map of files in the workspace and revisions
	 * @param fileFileMap
	 *            A map of workspace files to diff and their diff location
	 */
	@Override
	public void export(Map<File, Revision> fileRevisionMap,
			Map<File, File> fileFileMap)
			throws ConfigurationManagementException {

		log.debug("export() {} {}", fileRevisionMap.toString(),
				fileFileMap.toString());

		try {
			for (Map.Entry<File, Revision> entry : fileRevisionMap.entrySet()) {
				File srcFile = entry.getKey();
				Revision rev = entry.getValue();

				String strRev = rev.getStringRepresentation();
				int revNumber = Integer.parseInt(strRev);

				String path = srcFile.getAbsolutePath() + "#" + revNumber;
				List<IFileSpec> pathSpec = FileSpecBuilder
						.makeFileSpecList(path);

				// Look up destination file
				File dstFile = fileFileMap.get(srcFile);

				// use p4 'print' to get the specified revision
				GetFileContentsOptions printOpts = new GetFileContentsOptions();
				printOpts.setNoHeaderLine(true);

				// stream the contents to the new file location
				InputStream stream = server
						.getFileContents(pathSpec, printOpts);
				FileOutputStream fos = new FileOutputStream(dstFile);
				IOUtils.copy(stream, fos);
				stream.close();
				fos.close();
			}
		} catch (IOException e) {
			Logging.logException(log, new ConfigurationManagementException(e),
					true);
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		}
	}

	@Override
	public Collection<Revision> listRevisions(File file)
			throws ConfigurationManagementException {

		log.debug("listRevisions()");
		log.debug(file);

		Collection<Revision> revisions = new ArrayList<>();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			List<IFileSpec> spec = toSpec(file);

			GetRevisionHistoryOptions logOpts = new GetRevisionHistoryOptions();
			logOpts.setIncludeInherited(false); // (-i)
			logOpts.setOmitNonContributaryIntegrations(true); // (-s)
			logOpts.setContentHistory(false); // (-h)

			Map<IFileSpec, List<IFileRevisionData>> history;
			history = server.getRevisionHistory(spec, logOpts);

			for (Entry<IFileSpec, List<IFileRevisionData>> entry : history
					.entrySet()) {
				IFileSpec dfile = entry.getKey();
				List<IFileRevisionData> data = entry.getValue();

				// If no history found, p4 returns with error and data is null
				if (data != null) {
					for (IFileRevisionData d : data) {

						FileAction action = d.getAction();
						if (action.equals(FileAction.MOVE_DELETE)
								|| action.equals(FileAction.DELETE)) {
							continue;
						}

						Map<String, String> revInfo = new HashMap<>();
						revInfo.put("Action", action.toString());
						revInfo.put("Change", "" + d.getChangelistId());
						revInfo.put("File", "" + dfile.getDepotPathString());
						revInfo.put("Log", d.getDescription());
						revInfo.put("Submitted", format.format(d.getDate()));
						revInfo.put("User", d.getUserName());

						Revision r = new IntegerRevision(d.getRevision(), revInfo);
						revisions.add(r);
					}

				}
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		}
		return revisions;
	}

	// ################################################################################
	// TODO all functions below unchecked
	// ################################################################################

	@Override
	public ConflictedRevisions getRevisionCausingConflict(File file)
			throws ConfigurationManagementException {
		log.debug("getRevisionCausingConflict()");
		try {
			FileState fileState = getFileState(Collections.singletonList(file))
					.get(file);
			NoBaseConflictedRevision baseRev = new NoBaseConflictedRevision(fileState.getRevision());
			return baseRev;
		} catch (ConfigurationManagementException e) {
			Logging.logException(log, e, true);
		}
		return null;
	}

	//TODO @Override
	public void addTag(Collection<File> files, String tagName, String comment)
			throws ConfigurationManagementException {

		log.debug("addTag() {} {} {}", tagName, comment, files.toString());

		try {
			List<IFileSpec> specs = toSpec(files);
			if (server != null && !specs.isEmpty()) {
				server.tagFiles(specs, tagName, new TagFilesOptions());
				ArrayList<File> f = new ArrayList<>();
				f.addAll(files);
				log.debug("TAGS: " + getTags(f.get(0)));
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		}
	}

	//TODO @Override
	public void addTagRecursively(File directory, String tagName, String comment)
			throws ConfigurationManagementException {

		log.debug("addTagRecursively() {} {}", tagName, comment);

		try {
			addTag(Collections.singletonList(directory), tagName, comment);
		} catch (ConfigurationManagementException e) {
			Logging.logException(log, e, true);
		}
	}

	//TODO @Override
	public Collection<String> getTags(File file)
			throws ConfigurationManagementException {

		log.debug("getTags()");
		log.debug(file);

		Collection<String> tags = new ArrayList<>();
		try {
			List<ILabelSummary> labels = server.getLabels(null, 1, null,
					toSpec(file));
			for (ILabelSummary label : labels) {
				tags.add(label.getName());
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		}
		return tags;
	}

	//TODO @Override
	public void removeTag(Collection<File> files, String tagName, String comment)
			throws ConfigurationManagementException {
		log.debug("removeTag() {} {}", tagName, comment);
		try {
			List<IFileSpec> specs = toSpec(files);
			TagFilesOptions options = new TagFilesOptions();
			options.setDelete(true);
			server.tagFiles(specs, tagName, options);
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		}
	}

	//TODO @Override
/*	public void removeTag(String tagName, String comment, File file)
			throws ConfigurationManagementException {
		log.debug("removeTag() {} {}", tagName, comment);
		try {
			List<IFileSpec> specs = toSpec(file);
			TagFilesOptions options = new TagFilesOptions();
			options.setDelete(true);
			server.tagFiles(specs, tagName, options);
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		}
	}*/

	@Override
	public Map<File, Boolean> isStored(Collection<File> files)
			throws ConfigurationManagementException {
		log.debug("isStored()");
		log.debug(files);
		Map<File, Boolean> stored = new HashMap<>();

		try {
			List<IFileSpec> specs = toSpec(files);
			List<IExtendedFileSpec> extended = server.getExtendedFiles(specs,
					new GetExtendedFilesOptions());
			for (IExtendedFileSpec spec : extended) {
				if (spec.getClientPathString() == null) {
					continue;
				}
				stored.put(new File(spec.getClientPathString()),
						spec.isMapped());
			}
		} catch (AccessException ae) {
			try {
				connect();
			} catch (ConfigurationManagementException ce) {
				log.error("Unable to renew connection.", ce);
			}
		} catch (P4JavaException e) {
			Logging.logException(log, e, true);
		}
		return stored;
	}

	@Override
	public void buildCustomFileActions(CustomizationFileActionFactory customizationFileActionFactory) {
		//TODO Not implementing this one since the one below is already overridden from P4Interactor.
		//super.buildCustomActions(customizationFileActionFactory);
	}

	@Override
	public void buildCustomActions(CustomizationWidgetFactory widgetFactory) {

		log.debug("buildCustomActions()");

		// include buttons defined in the interactor
		super.buildCustomActions(widgetFactory);

		// Create widget to edit the active Client Workspace
		URL syncUrl = getClass().getResource("/icons/sync_toolbar_icon.png");
		ImageIcon syncIcon = new ImageIcon(syncUrl);
		widgetFactory.createActionWidget("Get Revisions", syncIcon,
				new CoreAction() {
					@Override
					public void execute()
							throws ConfigurationManagementException {
						populateClient(client, parentFrame);
					}

					@Override
					public String getDescription() {
						return "Get Revisions";
					}

					@Override
					public boolean canCancel() {
						return true;
					}
				});

		// Create widget to edit the active Client Workspace
		URL clientUrl = getClass().getResource("/icons/clients_icon.png");
		ImageIcon clientIcon = new ImageIcon(clientUrl);
		widgetFactory.createActionWidget("Workspace", clientIcon,
				new CoreAction() {
					@Override
					public void execute()
							throws ConfigurationManagementException {
						client = updateClient(fetchClient(), parentFrame);
					}

					@Override
					public String getDescription() {
						return "Edit Workspace";
					}

					@Override
					public boolean canCancel() {
						return true;
					}
				});

		// Create widget to select the active Pending Changelist
		URL pendingUrl = getClass().getResource("/icons/pending_icon.png");
		ImageIcon pendingIcon = new ImageIcon(pendingUrl);
		widgetFactory.createActionWidget("Pending", pendingIcon,
				new CoreAction() {
					@Override
					public void execute()
							throws ConfigurationManagementException {
						int c = selectPendingChange(getChangeID(), parentFrame);
						setChangeID(c);
					}

					@Override
					public String getDescription() {
						return "Current Changelist";
					}

					@Override
					public boolean canCancel() {
						return true;
					}
				});

		// line separation after P4 widgets
		widgetFactory.createLineBreak();
	}

	/**
	 * Access for test cases
	 *
	 * @return IOptionsServer
	 */
	public IOptionsServer getServer() {
		return server;
	}
}
