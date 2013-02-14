/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.mimehandlers;

import java.io.ByteArrayOutputStream;

import org.adaptinet.http.Request;
import org.adaptinet.transceiver.ITransceiver;


public interface MimeBase {
	public abstract ByteArrayOutputStream process(ITransceiver transceiver,
			Request request);

	public abstract int getStatus();

	public abstract void init(String u, ITransceiver s);

	public String getContentType();
}
