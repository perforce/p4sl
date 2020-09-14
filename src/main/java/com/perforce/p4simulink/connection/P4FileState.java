package com.perforce.p4simulink.connection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.mathworks.cmlink.api.FileProperty;
import com.mathworks.cmlink.api.version.r16b.FileState;
import com.mathworks.cmlink.api.IntegerRevision;
import com.mathworks.cmlink.api.LocalStatus;
import com.mathworks.cmlink.api.Revision;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4simulink.P4CMException;

public class P4FileState implements FileState {

	private LocalStatus status = LocalStatus.UNKNOWN;
	private int haveRev;
	private boolean lock;
	private boolean latest = true;
	private File file;

	public P4FileState(IExtendedFileSpec spec) throws P4CMException {

		if (spec == null) {
			status = LocalStatus.NOT_UNDER_CM;
			return;
		}

		String clientFile = spec.getClientPathString();
		this.file = new File(clientFile);

		this.lock = spec.isLocked();
		this.haveRev = spec.getHaveRev();
		int headRev = spec.getHeadRev();

		// have a file, but it is out of date
		if (haveRev > 0 && haveRev < headRev) {
			latest = false;
		}
		
		// check if file is in conflict
		if(spec.isUnresolved()) {
			status = LocalStatus.CONFLICTED;
			return;
		}
		
		// check pending changes
		FileAction action = spec.getAction();
		if (action == FileAction.ADD) {
			status = LocalStatus.ADDED;
			return;
		}
		if (action == FileAction.MOVE_ADD) {
			status = LocalStatus.ADDED;
			return;
		}
		if (action == FileAction.EDIT) {
			status = LocalStatus.MODIFIED;
			return;
		}
		if (action == FileAction.DELETE) {
			status = LocalStatus.DELETED;
			return;
		}
		if (action == FileAction.MOVE_DELETE) {
			status = LocalStatus.DELETED;
			return;
		}

		// don't have a file, but it is versioned
		if (haveRev <= 0) {
			status = LocalStatus.NOT_UNDER_CM;
			return;
		}
		
		// remainder
		status = LocalStatus.UNMODIFIED;
		return;
	}

	@Override
	public LocalStatus getLocalStatus() {
		return status;
	}

	@Override
	public Revision getRevision() {
		return new IntegerRevision(haveRev);
	}

	@Override
	public boolean hasLock() {
		return lock;
	}

	@Override
	public Collection<FileProperty> getProperties() {
		return new ArrayList<>();
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public boolean isLatest() {
		return latest;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(file + ": " + status.name());
		return sb.toString();
	}
}
