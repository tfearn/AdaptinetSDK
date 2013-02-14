/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */
package org.adaptinet.messaging;

import org.adaptinet.transceiver.ITransceiver;

public final class Header {

	Message message = null;

	public Header() {
	}

	public Header(ITransceiver transceiver) {
		this(transceiver, false);
	}

	public Header(ITransceiver transceiver, boolean bSecure) {
		message = new Message(transceiver, false);
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
}
