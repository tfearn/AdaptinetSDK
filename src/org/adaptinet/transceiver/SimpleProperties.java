/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SimpleProperties extends TransceiverProperties {

	private static final long serialVersionUID = 6812589245811341796L;

	private String name;

	public SimpleProperties() {
		this("lastrun.properties");
	}

	public SimpleProperties(String name) {
		try {
			load(new FileInputStream(name));
			this.name = name;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public void bookMark() {
		try {
			store(new FileOutputStream(name), "Server Current Properties");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
}