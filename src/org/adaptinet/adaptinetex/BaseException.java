/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.adaptinetex;

import java.util.Date;
import java.util.GregorianCalendar;


/**
 * The layout of the errorCode integer is as follows:
 * 
 * 3 3 2 2 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5
 * 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
 * +---+-----+---------------------+-------------------------------+ |Sev|M B Z|
 * Facility | Code |
 * +---+-----+---------------------+-------------------------------+
 */

public class BaseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3646402026989790806L;

	// Severity values
	public final static int SEVERITY_SUCCESS = 0;

	public final static int SEVERITY_WARNING = 1;

	public final static int SEVERITY_ERROR = 2;

	public final static int SEVERITY_FATAL = 3;

	public final static int ALL_SEVERITY = 4;

	public final static int MIN_SEVERITY = SEVERITY_SUCCESS;

	public final static int MAX_SEVERITY = ALL_SEVERITY;

	// Facility values
	public final static int FACILITY_GENERAL = 0;

	public final static int FACILITY_SERVER = 1;

	public final static int FACILITY_TRANCEIVER = 2;

	public final static int FACILITY_PARSER = 3;

	public final static int FACILITY_HTTP = 4;

	public final static int FACILITY_REPOSITORY = 5;

	public final static int FACILITY_LOGGER = 6;

	public final static int FACILITY_SECURITY = 7;

	public final static int FACILITY_TRANSCEIVER = 8;

	public final static int FACILITY_COM = 9;

	public final static int FACILITY_LICENSE = 10;

	public final static int FACILITY_INVOCATION = 11;

	public final static int FACILITY_FAULTTOL = 12;

	public final static int FACILITY_LOGIN = 13;

	public final static int ALL_FACILITYS = 14;

	public final static int MIN_FACILITY = FACILITY_GENERAL;

	public final static int MAX_FACILITY = ALL_FACILITYS;

	// Shifts and masks for picking things out of the error integer
	public final static int SEVERITY_SHIFT = 30;

	public final static int FACILITY_SHIFT = 16;

	public final static int SEVERITY_MASK = 0x3;

	public final static int FACILITY_MASK = 0x7ff;

	public final static int CODE_MASK = 0xffff;

	public final static int GEN_BASE = 0;

	public final static int GEN_NOTIMPLEMENTED = GEN_BASE + 1;

	public final static int GEN_TYPEMISMATCH = GEN_BASE + 2;

	public final static int GEN_CLASSNOTFOUND = GEN_BASE + 3;

	public final static int GEN_MESSAGE = GEN_BASE + 4;

	static public boolean bVerbose = false;

	protected int errorCode;

	protected String hostName = "localhost";

	protected int port = 5050;

	protected Date exceptionDate;

	String extraText = null;

	public BaseException(int hr) {
		errorCode = hr;
		exceptionDate = new Date();
	}

	public BaseException(int sev, int fac, int code) {
		errorCode = MakeErrorCode(sev, fac, code);
		exceptionDate = new Date();
	}

	public BaseException(int sev, int fac, int code, String msg) {
		errorCode = MakeErrorCode(sev, fac, code);
		exceptionDate = new Date();
		extraText = msg;
	}

	public BaseException(int sev, int code) {
		errorCode = MakeErrorCode(sev, FACILITY_GENERAL, code);
		exceptionDate = new Date();
	}

	public static String getSeverityText(int severity) {
		switch (severity) {
		case SEVERITY_SUCCESS:
			return "[SUCCESS/INFO]";

		case SEVERITY_FATAL:
			return "[FATAL]";

		case SEVERITY_ERROR:
			return "[ERROR]";

		case SEVERITY_WARNING:
			return "[WARNING]";

		default:
			return "[UNKNOWN SEVERITY]";
		}
	}

	public static String getFacilityText(int facility) {
		switch (facility) {
		case FACILITY_GENERAL:
			return "[GEN]";
		case FACILITY_SERVER:
			return "[SRV]";
		case FACILITY_TRANCEIVER:
			return "[TCV]";
		case FACILITY_PARSER:
			return "[PAR]";
		case FACILITY_HTTP:
			return "[HTP]";
		case FACILITY_REPOSITORY:
			return "[REP]";
		case FACILITY_LOGGER:
			return "[LOG]";
		case FACILITY_SECURITY:
			return "[SEC]";
		case FACILITY_TRANSCEIVER:
			return "[REQ]";
		case FACILITY_COM:
			return "[COM]";
		case FACILITY_LICENSE:
			return "[LIC]";
		case FACILITY_INVOCATION:
			return "[INV]";
		case FACILITY_FAULTTOL:
			return "[TOL]";
		default:
			return "[APPLICATION DEFINED FACILITY]";
		}
	}

	public void logMessage(Exception e) {
		logMessage(e.toString());
	}

	public void logMessage() {
		logMessage(new String(""));
	}

	public void logMessage(String msg) {
		try {
			if (bVerbose == true) {
				System.out.println("LogEntry:");
				System.out.println("\tseverity="
						+ BaseException.getSeverityText(getSeverity()));
				System.out.println("\tfacility="
						+ BaseException.getFacilityText(getFacility()));
				System.out.println("\terrorCode=" + getCode());
				System.out.println("\terrorMessage=" + getMessage());
				System.out.println("\textraText=" + msg);
				System.out.println("\tentryTime=" + exceptionDate.toString());
				System.out.println("");
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public String getMessage() {
		if (extraText != null) {
			return getMessageInternal(getCode()) + "[" + getExtraText() + "]";
		} else {
			return getMessageInternal(getCode());
		}
	}

	public String getPrimaryMessage() {
		return getMessageInternal(getCode());
	}

	public final int getErrorCode() {
		return errorCode;
	}

	static public final int MakeErrorCode(int sev, int fac, int code) {
		return (((sev & SEVERITY_MASK) << SEVERITY_SHIFT)
				| ((fac & FACILITY_MASK) << FACILITY_SHIFT) | (code & CODE_MASK));
	}

	public final int getCode() {
		return errorCode & CODE_MASK;
	}

	public final int getFacility() {
		return (errorCode >> FACILITY_SHIFT) & FACILITY_MASK;
	}

	public final int getSeverity() {
		return (errorCode >> SEVERITY_SHIFT) & SEVERITY_MASK;
	}

	public final boolean isFatal() {
		return (getSeverity() == SEVERITY_FATAL);
	}

	public final boolean isError() {
		return (getSeverity() == SEVERITY_ERROR);
	}

	public final boolean isWarning() {
		return (getSeverity() == SEVERITY_WARNING);
	}

	public final boolean isSuccess() {
		return (getSeverity() == SEVERITY_SUCCESS);
	}

	public final String getDate() {
		// temporary code until I figure out how to use DateFormat
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(exceptionDate);
		return gc.toString();
	}

	public void setHostName(String strHostName) {
		hostName = strHostName;
	}

	public void setPort(int nPort) {
		port = nPort;
	}

	public String getHostName() {
		return hostName;
	}

	public int getPort() {
		return port;
	}

	protected BaseException(String msg) {
		extraText = msg;
	}

	protected BaseException() {
	}

	public final void setExtraText(String xT) {
		extraText = xT;
	}

	public final String getExtraText() {
		if (extraText != null) {
			return extraText;
		} else {
			return "(No extra text associated with this exception)";
		}
	}

	public String getMessageInternal(int e) {
		String errorMessage = new String("[GEN]");

		switch (e) {
		case GEN_NOTIMPLEMENTED:
			errorMessage += "Function Not Implemented";
			break;
		case GEN_CLASSNOTFOUND:
			errorMessage += "Class not found";
			break;
		case GEN_MESSAGE:
			errorMessage += "Message :";
			break;
		default:
			errorMessage += "Runtime Error ";
			break;

		}
		return errorMessage;
	}

	static public String facilityAsString(int facility) {
		String strFacility = new String("UnSuported Facility");

		switch (facility) {
		case BaseException.FACILITY_TRANCEIVER:
			strFacility = "FACILITY_TRANCEIVER";
			break;
		case BaseException.FACILITY_GENERAL:
			strFacility = "FACILITY_GENERAL";
			break;
		case BaseException.FACILITY_HTTP:
			strFacility = "FACILITY_HTTP";
			break;
		case BaseException.FACILITY_LOGGER:
			strFacility = "FACILITY_LOGGER";
			break;
		case BaseException.FACILITY_MASK:
			strFacility = "FACILITY_MASK";
			break;
		case BaseException.FACILITY_PARSER:
			strFacility = "FACILITY_PARSER";
			break;
		case BaseException.FACILITY_REPOSITORY:
			strFacility = "REPOSITORY";
			break;
		case BaseException.FACILITY_SECURITY:
			strFacility = "FACILITY_SECURITY";
			break;
		case BaseException.FACILITY_SERVER:
			strFacility = "FACILITY_SERVER";
			break;
		case BaseException.FACILITY_SHIFT:
			strFacility = "FACILITY_SHIFT";
			break;
		case BaseException.FACILITY_INVOCATION:
			strFacility = "FACILITY_INVOCATION";
			break;
		case BaseException.FACILITY_FAULTTOL:
			strFacility = "FACILITY_FAULTTOL";
			break;
		default:
			strFacility = "Unknown FACILITY";
			break;
		}

		return strFacility;
	}

	static public String severityAsString(int severity) {
		String strSeverity = new String();

		switch (severity) {
		case BaseException.SEVERITY_SUCCESS:
			strSeverity = "SUCCESS";
			break;
		case BaseException.SEVERITY_WARNING:
			strSeverity = "WARNING";
			break;
		case BaseException.FACILITY_HTTP:
			strSeverity = "ERROR";
			break;
		case BaseException.FACILITY_LOGGER:
			strSeverity = "FATAL";
			break;
		default:
			strSeverity = "Unknown SEVERITY";
			break;
		}
		return strSeverity;
	}

}
