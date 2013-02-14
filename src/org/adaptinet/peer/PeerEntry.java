/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.peer;

import org.adaptinet.messaging.Address;
import org.adaptinet.transceiver.ITransceiver;


final public class PeerEntry {

	private Address address = null;
	private String name = null;
	private String type = null;
	private long time = 0;
	private boolean isAlive = false;

	public PeerEntry() {
		reset();
		address = new Address();
	}

	public PeerEntry(String url) {
		reset();
		address = new Address(url);
		time = System.currentTimeMillis();
	}

	public PeerEntry(Address address) {
		reset();
		this.address = new Address(address);
		time = System.currentTimeMillis();
	}

	public PeerEntry(PeerEntry pe) {
		reset();
		address = new Address(pe.address);
		name = pe.name;
		type = pe.type;
		time = pe.time;
	}

	public PeerEntry(ITransceiver transceiver, boolean bSecure) {
		try {
			StringBuffer buffer = new StringBuffer();
			if (bSecure == false)
				buffer.append("http");
			else
				buffer.append("https");
			buffer.append("://");
			buffer.append(transceiver.getHost());
			buffer.append(":");
			buffer.append(Integer.toString(transceiver.getPort()));
			this.address = new Address(buffer.toString());
			time = System.currentTimeMillis();
		} catch (Exception e) {
		}
	}

	private void reset() {
		address = null;
		name = null;
		type = null;
		time = 0;
	}

	public void setAddress(String address) {
		this.address = new Address(address);
	}

	public Address getAddress() {
		return address;
	}

	public int getKey() {

		try {
			return address.hashCode();
		} catch (Exception e) {
			return 0;
		}
	}

	public void setURL(String url) {
		address.setURL(url);
	}

	public String getURL() {
		return address.getURL();
	}

	public String getNameURL() {
		return address.getNameURL();
	}

	public void setName(String newValue) {
		name = newValue;
	}

	public String getName() {
		return name;
	}

	public void setEmail(String newValue) {
		address.setEmail(newValue);
	}

	public String getEmail() {
		return address.getEmail();
	}

	public void setType(String newValue) {
		type = newValue;
	}

	public String getType() {
		return type;
	}

	public void setTime(String newValue) {
		if (newValue == null || newValue.length() == 0)
			return;
		setTime(Long.parseLong(newValue));
	}

	public void setTime(long newValue) {
		time = newValue;
	}

	public long getTime() {
		return time;
	}

	public final boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive=isAlive;
	}

	public String getTimeAsString() {
		try {
			return Long.toString(time);
		} catch (NumberFormatException nfe) {
			return "-1";
		}
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		else if (o == null || getClass() != o.getClass())
			return false;

		PeerEntry p = (PeerEntry) o;
		return (address.equals(p.address) && name.equals(p.name) && type
				.equals(p.type));
	}
}
