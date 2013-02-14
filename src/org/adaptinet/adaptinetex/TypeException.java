/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.adaptinetex;

public class TypeException extends org.adaptinet.adaptinetex.AdaptinetException {

	private static final long serialVersionUID = -7906310225048606491L;

	public TypeException() {
		super(SEVERITY_ERROR, FACILITY_GENERAL, GEN_TYPEMISMATCH);
	}
}
