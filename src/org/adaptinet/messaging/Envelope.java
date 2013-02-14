/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.messaging;

import java.io.Serializable;

/*******************************************************************************
 * Envelope for API execution. Envelope contains the Header and Body information
 * of the transaction request.
 ******************************************************************************/

public final class Envelope implements Serializable {

	private Header header = null;
	private Body body = null;

	private static final long serialVersionUID = -1086912759259326288L;

	public Envelope() {
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public final String getPlugin() {
		return  getHeader().getMessage().getAddress().getPlugin();
	}
	
	public final int getHopCount() {
		return  getHeader().getMessage().getHopCount();
	}

	public final Integer getUID() {
		return  new Integer(getHeader().getMessage().computeUID());
	}

	public Object getContent(int idx) {
		Object ret = null;
		if (getBody() != null) {
			Object content[] = getBody().getcontentArray();
			if (content != null && content.length > idx) {
				ret = content[idx];
			}
		}
		return ret;
	}

	public boolean isMethod(String method) {
		return getHeader().getMessage().getAddress().getMethod()
				.equalsIgnoreCase(method);
	}

	public boolean isSync() {
		return getHeader().getMessage().getAddress().isSync();
	}

	public Address getReplyTo() {
		return header.getMessage().getReplyTo();
	}
}
