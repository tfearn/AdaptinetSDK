/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.socket;

public class PropData {

	String name, id, state;

	public PropData(String name, String id, String state) {
		this.name = name;
		this.id = id;
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getState() {
		return state;
	}

}