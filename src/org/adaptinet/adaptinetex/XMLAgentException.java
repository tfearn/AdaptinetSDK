/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.adaptinetex;

public class XMLAgentException extends AdaptinetException {

	public final static int ANT_BASE = 0;

	public final static int ANT_PARSER = ANT_BASE + 1;

	public final static int ANT_INVALIDPI = ANT_BASE + 2;

	public final static int ANT_INVALIDBIZTALKROUTE = ANT_BASE + 3;

	public final static int ANT_XSLT = ANT_BASE + 4;

	public final static int ANT_FILENOTFOUND = ANT_BASE + 5;

	public final static int ANT_INVALIDMETHODNAME = ANT_BASE + 6;

	public final static int ANT_METHODNOTSUPPORTED = ANT_BASE + 7;

	public final static int ANT_CREATEINSTANCEFAILURE = ANT_BASE + 8;

	public final static int ANT_INVALIDXMLIDL = ANT_BASE + 9;

	public final static int ANT_ERRORDURINGMETHODEXECUTION = ANT_BASE + 10;

	public final static int ANT_CLASSERROR = ANT_BASE + 11;

	public final static int ANT_SAXPARSINGERROR = ANT_BASE + 12;

	public final static int ANT_OBJDOTRANS = ANT_BASE + 13;

	public final static int ANT_OBJRETURN = ANT_BASE + 14;

	public final static int ANT_TRANCEIVERTIMEOUT1 = ANT_BASE + 15;

	public final static int ANT_TRANCEIVERTIMEOUT2 = ANT_BASE + 16;

	public final static int ANT_TRANCEIVERTIMEOUT3 = ANT_BASE + 17;

	public final static int ANT_TRANCEIVERTIMEOUT4 = ANT_BASE + 18;

	public final static int ANT_EMAILFAILURE = ANT_BASE + 19;

	public final static int XBR_ASNYCRESFAILURE = ANT_BASE + 20;

	private static final long serialVersionUID = -1433550759825655958L;

	public XMLAgentException(int sev, int code) {
		super(sev, FACILITY_TRANSCEIVER, code);
	}

	public XMLAgentException(int sev, int code, String xT) {
		super(sev, FACILITY_TRANSCEIVER, code);
		setExtraText(xT);
	}

	public final String getMessageInternal(int e) {
		String errorMessage = new String("[TRANSCEIVER]");

		switch (e) {
		case ANT_PARSER:
			errorMessage += "Error parsing XML transaction.";
			break;
		case ANT_INVALIDPI:
			errorMessage += "Error parsing Proccesing Instruction.";
			break;
		case ANT_INVALIDBIZTALKROUTE:
			errorMessage += "Error parsing Proccesing BizTalk Route information";
			break;
		case ANT_XSLT:
			errorMessage += "Error during Style Sheet Transformation";
			break;
		case ANT_FILENOTFOUND:
			errorMessage += "File not found";
			break;
		case ANT_INVALIDMETHODNAME:
			errorMessage += "Method name invalid.";
			break;
		case ANT_METHODNOTSUPPORTED:
			errorMessage += "Method not supported by this object.";
			break;
		case ANT_CREATEINSTANCEFAILURE:
			errorMessage += "Unable to create instance of plugin.";
			break;
		case ANT_INVALIDXMLIDL:
			errorMessage += "Invalid XMLIDL.";
			break;
		case ANT_ERRORDURINGMETHODEXECUTION:
			errorMessage += "Error during method execution.";
			break;
		case ANT_SAXPARSINGERROR:
			errorMessage += "Exception occured during sax parsing.";
			break;
		case ANT_CLASSERROR:
			errorMessage += "Class error unable to load class";
			break;
		case ANT_OBJDOTRANS:
			errorMessage += "Error executing object plugin's doTransaction.";
			break;
		case ANT_OBJRETURN:
			errorMessage += "Processing return Object from object plugin.";
			break;
		case ANT_TRANCEIVERTIMEOUT1:
			errorMessage += "Agent timed out, rollback called. Agent name :";
			break;
		case ANT_TRANCEIVERTIMEOUT2:
			errorMessage += "Agent timed out and interrupted called. Agent name :";
			break;
		case ANT_TRANCEIVERTIMEOUT3:
			errorMessage += "Agent timed out and thread stop called. Agent name :";
			break;
		case ANT_TRANCEIVERTIMEOUT4:
			errorMessage += "Agent thread died freeing agent. Agent name :";
			break;
		case ANT_EMAILFAILURE:
			errorMessage += "Agent cannot deliver mail. ";
			break;
		case XBR_ASNYCRESFAILURE:
			errorMessage += "Asynchronous response failure";
			break;
		default:
			errorMessage += "Unknown error code from XML Agent";
			break;
		}
		return errorMessage;
	}

}
