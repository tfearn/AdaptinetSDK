/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.pluginagent;

import org.adaptinet.messaging.Address;

public class Worker {

	protected Address address = null;
	protected long starttime = 0;
	protected long endtime = 0;
	protected boolean bresponded = false;

	public Worker(Address address) {
		this.address = address;
		endtime = 0;
		starttime = 0;
	}

	public Address getAddress() {
		return address;
	}

	public boolean getResponded() {
		return bresponded;
	}

	public void setResponded(boolean bresponded) {
		this.bresponded = bresponded;
	}

	public void setEndTime(long endtime) {
		this.endtime = endtime;
	}
	
	public long getEndTime() {
		// Check to see if an end time is available
		if (endtime == 0) {
			return 0;
		}
		return (starttime - endtime);
	}

	public long getStartTime() {
		return starttime;
	}

	public int hashCode() {
		return address.hashCode();
	}

	public boolean equals(Object o) {
		
		if (this == o) {
			return true;
		} else if (o == null || getClass() != o.getClass()) {
			return false;
		}
		return hashCode() == ((Worker) o).hashCode();
	}
}
