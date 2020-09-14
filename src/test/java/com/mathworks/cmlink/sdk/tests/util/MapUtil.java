/* Copyright 2010-2011 The MathWorks, Inc. */
package com.mathworks.cmlink.sdk.tests.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MapUtil {

	private final Map<File, Object> fMap;

	public MapUtil(Map<File, ? extends Object> mapToBeTested) {

		fMap = new HashMap<File, Object>();
		fMap.putAll(mapToBeTested);

	}

	public boolean allValuesMatch(Object valueToMatch) {

		Collection<?> values = fMap.values();
		boolean match = true;
		for (Object value : values) {
			if (value != valueToMatch) {
				match = false;
			}
		}
		return match;

	}

}
