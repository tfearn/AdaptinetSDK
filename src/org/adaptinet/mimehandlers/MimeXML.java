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
import org.adaptinet.transceiver.NetworkAgent;


public abstract class MimeXML implements MimeBase {

	protected boolean bRollBackOnly = false;
	protected int contentLength = 0;
	protected final static short WAIT = 0;
	protected final static short CHECK = 1;
	protected final static short ROLLBACK = 2;
	protected final static short COMMIT = 3;
	protected final static short RETURN = 4;
	protected final static short COMPLETE = 5;
	protected boolean bVerbose = false;

	static NetworkAgent networkAgent = null;

	MimeXML() {
		bVerbose = ITransceiver.getTransceiver().getVerboseFlag();
	}

	public abstract ByteArrayOutputStream process(ITransceiver transceiver,
			String xml);

	public int getStatus() {
		return 200;
	}

	public String getContentType() {
		return null;
	}

	public int getContentLength() {
		return contentLength;
	}

	public ByteArrayOutputStream process(ITransceiver transceiver,
			Request request) {
		String xml = request.getRequest();
		return process(transceiver, xml);
	}

	public void init(String u, ITransceiver transceiver) {
	}

	public Object getObject() {
		return null;
	}

	static {
		networkAgent = (NetworkAgent) ITransceiver.getTransceiver().getService(
				"networkAgent");
	}
}
