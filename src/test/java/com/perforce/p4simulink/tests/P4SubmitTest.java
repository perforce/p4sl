package com.perforce.p4simulink.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4simulink.util.Files;

public class P4SubmitTest {

	@Test
	public void testFailedSubmitString() throws Exception {
		String msg[] = {
				"Submitting change 4075.",
				"//depot/ml-t2/utilities/foo.m - must resolve before submitting",
				"//depot/ml-t2/utilities/foo.m - must resolve #5,#7",
				"Out of date files must be resolved or reverted.\n"+
				"Submit failed -- fix problems above then use 'p4 submit -c 4075'." };

		List<IFileSpec> submit = new ArrayList<IFileSpec>();

		for (String m : msg) {
			IFileSpec spec = new FileSpec(FileSpecOpStatus.ERROR, m);
			submit.add(spec);
		}

		int change = Files.changeFailedSubmit(submit);
		assertEquals(4075, change);
	}
}
