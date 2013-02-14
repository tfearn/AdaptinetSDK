/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.adaptinetex;

public class AdaptinetException extends BaseException {

	private static final long serialVersionUID = -1400441403453456334L;

	public AdaptinetException(int hr) {
		super(hr);
	}

	public AdaptinetException(int sev, int fac, int code) {
		super(sev, fac, code);
	}

	public AdaptinetException(int sev, int fac, int code, String msg) {
		super(sev, fac, code, msg);
	}

	public AdaptinetException(int sev, int code) {
		super(sev, code);
	}

	public void logMessage(String msg) {
		try {
			if (bVerbose == true) {
				System.out.println("LogEntry:");
				System.out.println("\tseverity="
						+ AdaptinetException.getSeverityText(getSeverity()));
				System.out.println("\tfacility="
						+ AdaptinetException.getFacilityText(getFacility()));
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

}
