/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.adaptinetex;

public class PluginException extends AdaptinetException {

	public final static int ANT_BASE = 0;

	public final static int ANT_PARSER = ANT_BASE + 1;

	public final static int ANT_FILENOTFOUND = ANT_BASE + 2;

	public final static int ANT_INVALIDMETHODNAME = ANT_BASE + 3;

	public final static int ANT_METHODNOTSUPPORTED = ANT_BASE + 4;

	public final static int ANT_CREATEINSTANCEFAILURE = ANT_BASE + 5;

	public final static int ANT_ERRORDURINGMETHODEXECUTION = ANT_BASE + 6;

	public final static int ANT_SAXPARSINGERROR = ANT_BASE + 7;

	public final static int ANT_OBJDOTRANS = ANT_BASE + 8;

	public final static int ANT_CLASSERROR = ANT_BASE + 9;

	public final static int ANT_OBJRETURN = ANT_BASE + 10;

	public final static int ANT_UNKNOWNHOST = ANT_BASE + 11;

	public final static int ANT_TRANCEIVERTIMEOUT1 = ANT_BASE + 12;

	public final static int ANT_TRANCEIVERTIMEOUT2 = ANT_BASE + 13;

	public final static int ANT_TRANCEIVERTIMEOUT3 = ANT_BASE + 14;

	public final static int ANT_TRANCEIVERTIMEOUT4 = ANT_BASE + 15;

	public final static int ANT_PLUGINERROR = ANT_BASE + 16;

	public final static int ANT_LOCALPOSTFAILURE = ANT_BASE + 17;

	public final static int ANT_POSTFAILURE = ANT_BASE + 18;

	public final static int ANT_BROADCASTFAILURE = ANT_BASE + 19;

	private static final long serialVersionUID = 2247282476681244913L;

	public PluginException(int sev, int code) {
		super(sev, FACILITY_TRANSCEIVER, code);
	}

	public PluginException(int sev, int code, String xT) {
		super(sev, FACILITY_TRANSCEIVER, code);
		setExtraText(xT);
	}

	public final String getMessageInternal(int e) {
		String errorMessage = new String("[TRANSCEIVER]");

		switch (e) {
		case ANT_PARSER:
			errorMessage += "Error parsing Request.";
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
			errorMessage += "Error posting message.";
			break;
		case ANT_OBJRETURN:
			errorMessage += "Processing return Object from object plugin.";
			break;
		case ANT_UNKNOWNHOST:
			errorMessage += "Unable to contect to Peer. ";
			break;
		case ANT_TRANCEIVERTIMEOUT1:
			errorMessage += "Plugin timed out, rollback called. Plugin name :";
			break;
		case ANT_TRANCEIVERTIMEOUT2:
			errorMessage += "Plugin timed out and interrupted called. Plugin name :";
			break;
		case ANT_TRANCEIVERTIMEOUT3:
			errorMessage += "Plugin timed out and thread stop called. Plugin name :";
			break;
		case ANT_TRANCEIVERTIMEOUT4:
			errorMessage += "Plugin thread died freeing Plugin. Plugin name :";
			break;
		case ANT_PLUGINERROR:
			errorMessage += "Plugin error unknown plugin";
			break;
		case ANT_LOCALPOSTFAILURE:
			errorMessage += "Error during local post message";
			break;
		case ANT_POSTFAILURE:
			errorMessage += "Error during post message";
			break;
		case ANT_BROADCASTFAILURE:
			errorMessage += "Error during broadcast message";
			break;
		default:
			errorMessage += "Unknown error code from XML Plugin";
			break;
		}
		return errorMessage;
	}

}
