/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Filer;

import org.adaptinet.messaging.*;

public class FindResult extends Object {
	String fileName = null;

	long length = 0;

	long lastModified = 0;

	Address address = null;

	int packetsRequested = 0;

	public FindResult(String fileName, long length, long lastModified,
			Address address) {
		this.fileName = fileName;
		this.length = length;
		this.lastModified = lastModified;
		this.address = address;
	}

	public String getFileName() {
		return fileName;
	}

	public Long getLength() {
		return new Long(length);
	}

	public Long getLastModified() {
		return new Long(lastModified);
	}

	public Address getAddress() {
		return address;
	}

	public int getPacketsRequested() {
		return packetsRequested;
	}

	public void addPacketsRequested() {
		packetsRequested++;
	}
}