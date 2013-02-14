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


public class MimeImage implements MimeBase {
	@SuppressWarnings("unused")
	private String url = null;

	private String contentType = null;

	public MimeImage(String url) {
		this.url = url;
	}

	public void init(String u, ITransceiver s) {
	}

	public String getContentType() {
		return contentType;
	}

	public ByteArrayOutputStream process(ITransceiver transceiver,
			Request request) {
		// TODO: Implement this org.adaptinet.http.MimeBase method
		return null;
	}

	public int getStatus() {
		return 200;
	}
}
