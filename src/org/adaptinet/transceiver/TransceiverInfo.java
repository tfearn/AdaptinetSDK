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

public class TransceiverInfo {
	static public TransceiverProperties properties;

	static public boolean bVerbose = false;

	static public boolean bAutoReload = false;

	public static final String VERSION = "Transceiver version 0.91 Beta";

	static public Properties getServerProperty() {
		return properties;
	}

	static public String getServerProperty(String name) {
		return properties.getProperty(name);
	}

}
