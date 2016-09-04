package com.doc.xpi.af.modules.sequencer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParameterInputValueChecker {

	private static final List<String> VALUES_TRUE = new ArrayList<String>(
			Arrays.asList(new String[] { "1", "true", "yes" }));
	private static final List<String> VALUES_FALSE = new ArrayList<String>(
			Arrays.asList(new String[] { "0", "false", "no" }));

	private ParameterInputValueChecker() {
	}

	public static boolean isNullOrEmpty(String value) {
		return (value == null || value.trim().isEmpty()) ? true : false;
	}

	public static Boolean isTrueOrFalse(String value) {
		if (!isNullOrEmpty(value)) {
			if (VALUES_TRUE
					.contains(String.valueOf(value).trim().toLowerCase())) {
				return Boolean.TRUE;
			} else if (VALUES_FALSE.contains(String.valueOf(value).trim()
					.toLowerCase())) {
				return Boolean.FALSE;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static String getLeadingCharacter(String value) {
		return (value.trim().length() == 1) ? String.valueOf(value.trim()
				.charAt(0)) : null;
	}

}
