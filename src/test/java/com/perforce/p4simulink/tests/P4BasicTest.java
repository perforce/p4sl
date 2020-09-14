package com.perforce.p4simulink.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mathworks.cmlink.api.ConflictedRevisions;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.mathworks.cmlink.api.ConfigurationManagementException;
import com.mathworks.cmlink.api.version.r16b.FileState;
import com.mathworks.cmlink.api.IntegerRevision;
import com.mathworks.cmlink.api.LocalStatus;
import com.mathworks.cmlink.api.Revision;
import com.perforce.p4simulink.utils.P4SimulinkTest;

public class P4BasicTest extends P4SimulinkTest {

	@Test
	public void testCheckP4D() throws Exception {
		int ver = p4d.getVersion();
		assertTrue(ver >= 20121);
	}

	@Test
	public void testSync() throws Exception {
		// Sync files at workspace root
		Collection<File> files = new ArrayList<>();
		files.add(root);
		adapter.getLatest(files);

		File syncFile = new File(root, "Main/file-10.txt");
		assertTrue(syncFile.exists());
	}

	@Test
	public void testAdd() throws Exception {
		// add 'add_file.txt' to workspace Main
		File file = new File(root, "add_file.txt");
		FileWriter write = new FileWriter(file);
		write.append("Content");
		write.close();

		// Open file for ADD
		Collection<File> files = new ArrayList<>();
		files.add(file);
		adapter.add(files);

		// Check state
		Map<File, FileState> status = adapter.getFileState(files);
		FileState state = status.get(file);
		assertEquals(LocalStatus.ADDED, state.getLocalStatus());
	}

	@Test
	public void testEdit() throws Exception {
		// edit existing file in Main/file-22.txt
		File file = new File(root, "Main/file-22.txt");

		// Open file for EDIT
		Collection<File> files = new ArrayList<>();
		files.add(file);
		adapter.checkout(files);

		// Modify file
		FileWriter write = new FileWriter(file);
		write.append("Content EDIT");
		write.close();

		// Check state
		Map<File, FileState> status = adapter.getFileState(files);
		FileState state = status.get(file);
		assertEquals(LocalStatus.MODIFIED, state.getLocalStatus());
	}

	@Test
	public void testDelete() throws Exception {
		// delete existing file in Main/file-87.txt
		File file = new File(root, "Main/file-87.txt");

		// Sync then Delete file
		Collection<File> files = new ArrayList<>();
		files.add(file);
		adapter.getLatest(files);
		adapter.remove(files);

		assertTrue(file.exists());

		// Check state
		Map<File, FileState> status = adapter.getFileState(files);
		FileState state = status.get(file);
		assertEquals(state.getLocalStatus(), LocalStatus.DELETED);
	}

	@Test
	public void testRename() throws Exception {
		// rename existing file in Main/file-21.txt to Main/file-21r.txt
		File src = new File(root, "Main/file-21.txt");
		File dst = new File(root, "Main/file-21r.txt");

		// Open file for EDIT
		adapter.checkout(Arrays.asList(src));

		// Rename file
		adapter.moveFile(src, dst);

		// Check state
		Map<File, FileState> status = adapter.getFileState(Arrays.asList(src));
		FileState state = status.get(src);
		assertEquals(LocalStatus.DELETED, state.getLocalStatus());

		status = adapter.getFileState(Arrays.asList(dst));
		state = status.get(dst);
		assertEquals(LocalStatus.ADDED, state.getLocalStatus());
	}

	@Test
	public void testEditSubmit() throws Exception {
		// edit existing file in Main/file-12.txt
		File file = new File(root, "Main/file-12.txt");

		// Open file for EDIT
		adapter.checkout(Arrays.asList(file));

		// Submit file
		adapter.checkin(file, "empty edit");

		Map<File, Boolean> map = adapter.isStored(Arrays.asList(file));
		assertTrue(map.get(file));
	}

	@Test
	public void testHistory() throws Exception {
		// get history for Main/file-1.txt
		File file = new File(root, "Main/file-1.txt");

		// sync file
		adapter.update(root);

		// get history
		Collection<Revision> revs = adapter.listRevisions(file);
		assertFalse(revs.isEmpty());

		Iterator<Revision> itr = revs.iterator();
		itr.next();
		Revision rev = itr.next();
		Map<String, String> map = rev.getRevisionInfo();
		assertEquals("11", map.get("Change"));

		Map<File, Revision> revMap = new HashMap<File, Revision>();
		revMap.put(file, rev);
		adapter.getRevision(revMap);

		Map<File, Boolean> isMap = adapter.isLatest(Arrays.asList(file));
		assertFalse(isMap.get(file));
	}

	@Test
	public void testExport() throws Exception {
		// print Main/file-2.txt
		File file = new File(root, "Main/file-2.txt");
		File export = new File(root, "export_file-2.txt");

		Revision rev = new IntegerRevision(1);
		Map<File, Revision> fileRevisionMap = new HashMap<>();
		fileRevisionMap.put(file, rev);

		Map<File, File> fileFileMap = new HashMap<>();
		fileFileMap.put(file, export);

		adapter.export(fileRevisionMap, fileFileMap);
		assertTrue(export.exists());

		String test = "filename: file-2.txt";
		String content = FileUtils.readFileToString(export, "utf-8");
		assertTrue(content.contains(test));
	}

	@Test
	public void testConflict() throws Exception {
		// print Main/file-3.txt
		File file = new File(root, "Main/file-3.txt");

		Revision rev = new IntegerRevision(1);
		Map<File, Revision> fileRevisionMap = new HashMap<>();
		fileRevisionMap.put(file, rev);

		adapter.getRevision(fileRevisionMap);
		adapter.edit(Arrays.asList(file));

		try {
			adapter.checkin(file, "conflict");
		} catch (ConfigurationManagementException e) {
			// check switch to pending change
			int pending = adapter.getChangeID();
			assertTrue(pending > 0);
		}

		// Check conflicted status
		Map<File, FileState> status = adapter.getFileState(Arrays.asList(file));
		FileState state = status.get(file);
		assertEquals(state.getLocalStatus(), LocalStatus.CONFLICTED);

		// Check revision
		ConflictedRevisions revMap = adapter.getRevisionCausingConflict(file);
		assertEquals(new IntegerRevision(4), revMap.getTheirsRevision());
		
		adapter.resolveConflict(file);
		
		// Check resolved status
		status = adapter.getFileState(Arrays.asList(file));
		state = status.get(file);
		assertEquals(state.getLocalStatus(), LocalStatus.MODIFIED);
		
		// Check switch back to default changelist
		adapter.checkin(file, "resolved");
		int pending = adapter.getChangeID();
		assertTrue(pending == 0);
	}
}
