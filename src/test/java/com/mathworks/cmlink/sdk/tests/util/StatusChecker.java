package com.mathworks.cmlink.sdk.tests.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.mathworks.cmlink.api.ConfigurationManagementException;
import com.mathworks.cmlink.api.version.r16b.FileState;
import com.mathworks.cmlink.api.LocalStatus;
import com.mathworks.cmlink.api.Revision;
import com.mathworks.cmlink.api.version.r16b.CMAdapter;

public class StatusChecker {

	private final CMAdapter fAdapter;

	public StatusChecker(CMAdapter adapter) {
		fAdapter = adapter;
	}

	public CMAdapter getAdaptor() {
		return fAdapter;
	}

	public void checkStateIsAsExpected(Collection<File> files,
			Collection<LocalStatus> possibleStatus)
			throws ConfigurationManagementException {

		// Test whether the specified list of files all share the specified
		// expected status.

		CMAdapter adapter = getAdaptor();
		Map<File, FileState> statusMap = adapter.getFileState(files);

		for (File file : files) {
			LocalStatus actualStatus = statusMap.get(file).getLocalStatus();

			assertThat("file :" + file, possibleStatus, hasItem(actualStatus));
		}
	}

	public void checkStateIsAsExpected(Collection<File> files,
			LocalStatus expectedStatus) throws ConfigurationManagementException {

		files = stripDirectories(files);

		// Test whether the specified list of files all share the specified
		// expected status.

		CMAdapter adapter = getAdaptor();
		Map<File, FileState> statusMap = adapter.getFileState(files);

		for (File file : files) {
			LocalStatus actualStatus = statusMap.get(file).getLocalStatus();

			assertThat("file status" + file, expectedStatus,
					is(equalTo(actualStatus)));

		}
	}

	private static Collection<File> stripDirectories(Collection<File> files) {

		Collection<File> filesNoDirs = new ArrayList<File>();
		for (File file : files) {
			if (file.isFile() || !file.exists()) {
				filesNoDirs.add(file);
			}
		}
		return filesNoDirs;
	}

	public void checkAtLeastOneStateIsAsExpected(Collection<File> files,
			LocalStatus expectedStatus) throws ConfigurationManagementException {

		files = stripDirectories(files);

		CMAdapter adapter = getAdaptor();
		Map<File, FileState> statusMap = adapter.getFileState(files);
		for (Map.Entry<File, FileState> entry : statusMap.entrySet()) {
			LocalStatus actualStatus = entry.getValue().getLocalStatus();
			if (actualStatus == expectedStatus) {
				return;
			}
		}
		assertTrue("At least one status should have been " + expectedStatus,
				false);

	}

	public void checkedOut(Collection<File> files, boolean actualCheckedoutState)
			throws ConfigurationManagementException {

		files = stripDirectories(files);

		CMAdapter adapter = getAdaptor();
		Map<File, FileState> fileStateMap = adapter.getFileState(files);

		for (Map.Entry<File, FileState> entry : fileStateMap.entrySet()) {
			assertEquals("Value mismatch", actualCheckedoutState, entry
					.getValue().hasLock());
		}

	}

	public static void revisionsAllEqual(Collection<Revision> revisions,
			Revision expectedRevision) {

		for (Revision revision : revisions) {
			assertTrue("Expected revision " + expectedRevision
					+ " does not match revision " + revision,
					0 == revision.compareTo(expectedRevision));
		}
	}

	public static void revisionsAllLess(Collection<Revision> revisions,
			Revision benchmarkRevision) {

		for (Revision revision : revisions) {
			assertTrue("Expected revision " + revision
					+ " to be less than the bench mark revision "
					+ benchmarkRevision,
					0 > revision.compareTo(benchmarkRevision));

		}
	}

	public static void revisionsAllGreater(Collection<Revision> revisions,
			Revision benchmarkRevision) {

		for (Revision revision : revisions) {
			assertTrue("Expected revision " + revision
					+ " to be greater than the bench mark revision "
					+ benchmarkRevision,
					0 < revision.compareTo(benchmarkRevision));
		}
	}

}
