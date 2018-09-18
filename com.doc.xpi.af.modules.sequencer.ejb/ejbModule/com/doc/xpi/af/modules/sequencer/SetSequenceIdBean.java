package com.doc.xpi.af.modules.sequencer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.doc.xpi.af.modules.sequencer.util.AuditLogHelper;
import com.doc.xpi.af.modules.sequencer.util.SetSequenceIdParametersHelper;
import com.sap.aii.af.lib.mp.module.Module;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.lib.mp.module.ModuleException;
import com.sap.engine.interfaces.messaging.api.DeliverySemantics;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.XMLPayload;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;
import com.sap.tc.logging.Location;

/**
 * Session Bean implementation class SetSequenceIdBean
 */

public class SetSequenceIdBean implements Module {

	private static final Location TRACE = Location.getLocation(SetSequenceIdBean.class.getName());

	/**
	 * Default constructor.
	 */
	public SetSequenceIdBean() {
		// Not used in current implementation
	}

	@Override
	public ModuleData process(ModuleContext moduleContext, ModuleData moduleData)
			throws ModuleException {

		Message message = (Message) moduleData.getPrincipalData();
		MessageKey messageKey = message.getMessageKey();

		AuditLogHelper audit = new AuditLogHelper(messageKey);

		SetSequenceIdParametersHelper parametersHandler = new SetSequenceIdParametersHelper();
		Map<String, String> parameters = parametersHandler.getChannelParameters(moduleContext);
		parametersHandler.checkInputParameters(parameters);

		String sequenceId = null;

		if (!parametersHandler.isParameterError) {

			XMLPayload payload = message.getDocument();
			InputStream payloadInputStream = payload.getInputStream();

			if (parametersHandler.xPath != null) {
				// Determine sequence ID
				String value = this.applyXPathExtractor(payloadInputStream,
						parametersHandler.xPath);

				if (value == null || value.trim().isEmpty()) {
					TRACE.errorT("No value was extracted for the XPath expression");
				}

				sequenceId = this.constructValue(value, parametersHandler);

				if (sequenceId != null && sequenceId.length() >= 1
						&& sequenceId.length() <= 16
						&& !sequenceId.equals(message.getSequenceId())) {

					// Overwrite sequence ID and delivery semantics (if not
					// EOIO)
					try {

						TRACE.debugT("Overwriting message sequence ID from "
								+ message.getSequenceId() + " to " + sequenceId);

						message.setSequenceId(sequenceId);

						if (message.getDeliverySemantics() != DeliverySemantics.ExactlyOnceInOrder
								&& message.getSequenceId() != null) {
							TRACE.debugT("Overwriting message delivery semantics from "
									+ message.getDeliverySemantics().toString()
									+ " to "
									+ DeliverySemantics.ExactlyOnceInOrder.toString());

							message.setDeliverySemantics(DeliverySemantics.ExactlyOnceInOrder);
						}

						moduleData.setPrincipalData(message);

					} catch (InvalidParamException e) {
						audit.addAuditLogEntry(
								AuditLogStatus.WARNING,
								SetSequenceIdBean.class.getSimpleName()
										+ ": Error setting message delivery semantics / sequence ID: "
										+ e.getMessage());

						TRACE.errorT("Error setting message delivery semantics / sequence ID: "
								+ e.getMessage());
					}

				} else {
					audit.addAuditLogEntry(
							AuditLogStatus.WARNING,
							SetSequenceIdBean.class.getSimpleName()
									+ ": Sequence ID cannot be set because of incorrect value: "
									+ sequenceId);

					TRACE.errorT("Sequence ID cannot be set because of incorrect value: "
							+ sequenceId);
				}

			}

		} else {
			if (parametersHandler.isTerminateIfError) {
				throw new ModuleException(
						"One or several required adapter module parameters are missing or incorrect");
			} else {
				audit.addAuditLogEntry(
						AuditLogStatus.WARNING,
						SetSequenceIdBean.class.getSimpleName()
								+ ": One or several required adapter module parameters are missing or incorrect, exiting module execution");

				TRACE.errorT("One or several required adapter module parameters are missing or incorrect, exiting module execution");
			}
		}

		if (message.getSequenceId() == null
				|| !message.getSequenceId().equals(sequenceId)) {
			if (parametersHandler.isTerminateIfError) {
				throw new ModuleException(
						"Failure while attempting to set sequence ID");
			} else {
				audit.addAuditLogEntry(
						AuditLogStatus.WARNING,
						SetSequenceIdBean.class.getSimpleName()
								+ ": Failure while attempting to set sequence ID");
			}
		}

		return moduleData;
	}

	private String applyXPathExtractor(InputStream inputStream,
			String xPathExpressionValue) {

		String value = null;

		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			documentFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();
			xPath.setNamespaceContext( new NamespaceContext() {
			    public String getNamespaceURI(String prefix) {
			      if(prefix.equals("ns0")) {
			    	  return "http://schemas.example.com/shemas/common";
			       }
			      return null;
			    }

				@Override
				public String getPrefix(String namespaceURI) {
					// Not sneeded in this context
					if(namespaceURI.equals("http://schemas.example.com/shemas/common") {
				    	  return "ns0";
				    }
					return null;
				}

		     	@Override
				public Iterator getPrefixes(String arg0) {
					// Not neededed in this context
					return null;
				}
			});
			XPathExpression xPathExpression = xPath.compile(xPathExpressionValue);
			value = (String) xPathExpression.evaluate(document,
					XPathConstants.STRING);

			TRACE.debugT("Value extracted for the XPath expression: " + value);
		} catch (ParserConfigurationException e) {
			TRACE.errorT("Error parsing message and applying XPath expression: "
					+ e.getMessage());
		} catch (SAXException e) {
			TRACE.errorT("Error parsing message and applying XPath expression: "
					+ e.getMessage());
		} catch (IOException e) {
			TRACE.errorT("Error parsing message and applying XPath expression: "
					+ e.getMessage());
		} catch (XPathExpressionException e) {
			TRACE.errorT("Error parsing message and applying XPath expression: "
					+ e.getMessage());
		}

		return value;

	}

	private String constructValue(String input,
			SetSequenceIdParametersHelper parametersHandler) {

		final int SEQ_ID_MAX_LENGTH = 16;

		String value = input.trim();

		// Adoption of the sequence ID value based on optional parameterization
		if (parametersHandler.isDeleteLeadingChar) {
			value = value.replaceFirst("^" + parametersHandler.leadingChar
					+ "+(?!$)", "");

			TRACE.debugT("Leading characters were deleted, new value: " + value);
		}

		if (parametersHandler.isAddPrefix) {
			value = parametersHandler.prefix + "_" + value;

			TRACE.debugT("Prefix was added, new value: " + value);
		}

		if (parametersHandler.isAddSuffix) {
			value = value + "_" + parametersHandler.suffix;

			TRACE.debugT("Suffix was added, new value: " + value);
		}

		if (parametersHandler.isReplaceInvalidChar) {
			value = value.replaceAll("[^\\w]", "_");

			TRACE.debugT("Invalid characters were replaced, new value: "
					+ value);
		}

		if (parametersHandler.isTruncateStart
				&& value.length() > SEQ_ID_MAX_LENGTH) {
			value = value.substring(value.length() - SEQ_ID_MAX_LENGTH);

			TRACE.debugT("Start was truncated, new value: " + value);
		}

		if (parametersHandler.isTruncateEnd
				&& value.length() > SEQ_ID_MAX_LENGTH) {
			value = value.substring(0, SEQ_ID_MAX_LENGTH);

			TRACE.debugT("End was truncated, new value: " + value);
		}

		if (value != null) {
			value = value.toUpperCase();
		}

		TRACE.debugT("Constructed value for sequence ID: " + value);

		return value;
	}

}