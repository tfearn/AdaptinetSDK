/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiver;

import java.util.Properties;

public abstract class TransceiverProperties extends Properties {

	private static final long serialVersionUID = 821655720798197367L;

	static public TransceiverProperties getInstance(String name) {
		TransceiverProperties o = null;
		try {
			Class<?> c = Class.forName(name);
			o = (TransceiverProperties)c.newInstance();

		} catch (Exception e) {
			o = null;
		}
		return o;
	}
}
