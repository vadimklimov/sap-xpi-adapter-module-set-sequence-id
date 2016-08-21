package com.doc.xpi.af.modules.sequencer.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.aii.af.lib.mp.module.ModuleContext;

public class SetSequenceIdParametersHelper {

	public static final String PARAMETER_TERMINATE_IF_ERROR = "error.terminate";
	public static final String PARAMETER_ERROR_IF_MULTIPLE_VALUES = "multipleValues.error";
	public static final String PARAMETER_XPATH = "xpath";
	public static final String PARAMETER_SEQ_ID_LEADING_CHAR_DELETE = "sequenceId.deleteLeadingCharacter";
	public static final String PARAMETER_SEQ_ID_LEADING_CHAR = "sequenceId.leadingCharacter";
	public static final String PARAMETER_SEQ_ID_REPLACE_INVALID_CHARS = "sequenceId.replaceInvalidCharacters";
	public static final String PARAMETER_SEQ_ID_TRUNCATE = "sequenceId.truncate";
	public static final String PARAMETER_SEQ_ID_PREFIX = "sequenceId.prefix";
	public static final String PARAMETER_SEQ_ID_SUFFIX = "sequenceId.suffix";

	public boolean isParameterError;
	public boolean isTerminateIfError;
	public boolean isErrorIfMultipleValues;
	public boolean isDeleteLeadingChar;
	public boolean isReplaceInvalidChar;
	public boolean isTruncateStart;
	public boolean isTruncateEnd;
	public boolean isAddPrefix;
	public boolean isAddSuffix;
	public String xPath;
	public String leadingChar;
	public String prefix;
	public String suffix;

	private static List<String> parametersMandatory = new ArrayList<String>();

	// Set list of mandatory parameters
	static {
		parametersMandatory.add(PARAMETER_XPATH);
	}

	public SetSequenceIdParametersHelper() {

		// Set default values
		this.isParameterError = false;
		this.isTerminateIfError = true;
		this.isErrorIfMultipleValues = true;
		this.isTruncateStart = false;
		this.isTruncateEnd = false;
		this.isDeleteLeadingChar = false;
		this.isReplaceInvalidChar = false;
		this.isAddPrefix = false;
		this.isAddSuffix = false;
		this.xPath = null;
		this.leadingChar = "0";
		this.prefix = null;
		this.suffix = null;

	}

	public Map<String, String> getChannelParameters(ModuleContext moduleContext) {

		Map<String, String> channelParameters = new HashMap<String, String>();

		Enumeration<?> channelConfigParameters = moduleContext
				.getContextDataKeys();
		String channelConfigParameterName;
		String channelConfigParameterValue;

		while (channelConfigParameters.hasMoreElements()) {
			channelConfigParameterName = (String) channelConfigParameters
					.nextElement();
			channelConfigParameterValue = moduleContext
					.getContextData(channelConfigParameterName);
			channelParameters.put(channelConfigParameterName,
					channelConfigParameterValue);
		}

		return channelParameters;
	}

	public void checkInputParameters(Map<String, String> parameters) {

		// Get parameters' values
		for (Map.Entry<String, String> parameter : parameters.entrySet()) {

			// Parameters related to module processing termination
			if (parameter.getKey().equals(PARAMETER_TERMINATE_IF_ERROR)) {
				if (!ParameterInputValueChecker.isNullOrEmpty(parameter
						.getValue())) {
					this.isTerminateIfError = convertBoolean(
							ParameterInputValueChecker.isTrueOrFalse(parameter
									.getValue()), true);
				}

			} else if (parameter.getKey().equals(
					PARAMETER_ERROR_IF_MULTIPLE_VALUES)) {
				if (!ParameterInputValueChecker.isNullOrEmpty(parameter
						.getValue())) {
					this.isErrorIfMultipleValues = convertBoolean(
							ParameterInputValueChecker.isTrueOrFalse(parameter
									.getValue()), true);
				}

				// Parameters related to XPath expression
			} else if (parameter.getKey().equals(PARAMETER_XPATH)) {
				if (!ParameterInputValueChecker.isNullOrEmpty(parameter
						.getValue())) {
					this.xPath = parameter.getValue().trim();
				} else {
					this.isParameterError = true;
				}

				// Parameters related to leading symbols
			} else if (parameter.getKey().equals(
					PARAMETER_SEQ_ID_LEADING_CHAR_DELETE)) {
				if (!ParameterInputValueChecker.isNullOrEmpty(parameter
						.getValue())) {
					this.isDeleteLeadingChar = convertBoolean(
							ParameterInputValueChecker.isTrueOrFalse(parameter
									.getValue()), false);

					if (this.isDeleteLeadingChar) {
						if (!ParameterInputValueChecker
								.isNullOrEmpty(parameters
										.get(PARAMETER_SEQ_ID_LEADING_CHAR))) {
							this.leadingChar = ParameterInputValueChecker
									.getLeadingCharacter(parameters
											.get(PARAMETER_SEQ_ID_LEADING_CHAR));
						} else {
							this.isDeleteLeadingChar = false;
						}
					}
				}

				// Parameters related to replacement of invalid characters
			} else if (parameter.getKey().equals(
					PARAMETER_SEQ_ID_REPLACE_INVALID_CHARS)) {
				if (!ParameterInputValueChecker.isNullOrEmpty(parameter
						.getValue())) {
					this.isReplaceInvalidChar = convertBoolean(
							ParameterInputValueChecker.isTrueOrFalse(parameter
									.getValue()), false);
				}

				// Parameters related to truncation
			} else if (parameter.getKey().equals(PARAMETER_SEQ_ID_TRUNCATE)) {
				if (!ParameterInputValueChecker.isNullOrEmpty(parameter
						.getValue())) {
					if (parameter.getValue().trim().equalsIgnoreCase("start")) {
						this.isTruncateStart = true;
						this.isTruncateEnd = false;
					} else if (parameter.getValue().trim().equalsIgnoreCase(
							"end")) {
						this.isTruncateStart = false;
						this.isTruncateEnd = true;
					} else {
						this.isTruncateStart = false;
						this.isTruncateEnd = false;
					}
				} else {
					this.isTruncateStart = false;
					this.isTruncateEnd = false;
				}

				// Parameters related to affix
			} else if (parameter.getKey().equals(PARAMETER_SEQ_ID_PREFIX)) {
				if (!ParameterInputValueChecker.isNullOrEmpty(parameters
						.get(PARAMETER_SEQ_ID_PREFIX))) {
					this.isAddPrefix = true;
					this.prefix = parameters.get(PARAMETER_SEQ_ID_PREFIX)
							.trim();
				} else {
					this.isAddPrefix = false;
				}

			} else if (parameter.getKey().equals(PARAMETER_SEQ_ID_SUFFIX)) {
				if (!ParameterInputValueChecker.isNullOrEmpty(parameters
						.get(PARAMETER_SEQ_ID_SUFFIX))) {
					this.isAddSuffix = true;
					this.suffix = parameters.get(PARAMETER_SEQ_ID_SUFFIX)
							.trim();
				} else {
					this.isAddSuffix = false;
				}

			} else {
				// Reserved for future use
			}

		}

		// Check for mandatory parameters
		for (String parameterMandatory : parametersMandatory) {
			if (!parameters.containsKey(parameterMandatory)) {
				this.isParameterError = true;
				return;
			}
		}

	}

	private static boolean convertBoolean(Boolean convertedValue,
			boolean defaultValue) {
		return (convertedValue != null) ? convertedValue : defaultValue;
	}

}
