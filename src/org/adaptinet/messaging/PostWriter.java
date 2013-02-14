/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.messaging;

import java.io.OutputStream;

import org.adaptinet.transceiver.ITransceiver;


public class PostWriter extends BaseWriter {

	public PostWriter() {
	}

	public PostWriter(OutputStream ostream) {
		super(ostream);
	}

	public void write(Message msg, Object[] args) throws Exception {
		// System.out.println();
		// System.out.println("*************Outgoing XML*********************");

		// Declared up here hopefully put into register.
		String temp = null;
		try {
			Address addr = msg.getAddress();
			writeString("<Envelope><Header><Message>");
			if (addr != null) {
				writeString("<To>");
				writeAddress(addr);
				writeString("</To>");
			}

			temp = msg.getID();
			if (temp != null) {
				writeString("<id>");
				writeString(temp);
				writeString("</id>");
			}

			// Check to see if a certificate is available
			if (ITransceiver.getCertificate() != null) {
				writeString("<Certificate>");
				writeString(ITransceiver.getCertificate());
				writeString("</Certificate>");
			}

			// Check to see if a certificate is available
			if (ITransceiver.getCertificate() != null) {
				writeString("<Certificate>");
				writeString(ITransceiver.getCertificate());
				writeString("</Certificate>");
			}

			writeString("<Key>");
			writeString(ITransceiver.getKey());
			writeString("</Key>");
			writeString("<Timestamp>");
			writeString(msg.getTimeStamp());
			writeString("</Timestamp>");
			temp = msg.getHops();
			if (temp != null) {
				writeString("<Hops>");
				writeString(temp);
				writeString("</Hops>");
			}
			Address replyTo = msg.getReplyTo();
			if (replyTo != null) {
				writeString("<ReplyTo>");
				writeAddress(replyTo);
				writeString("</ReplyTo>");
			}
			writeString("</Message>");
			writeString("</Header><Body>");
			if (args != null) {
				for (int i = 0; i < args.length; i++)
					convertToXML(args[i]);
			}
			writeString("</Body></Envelope>");
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			if (t instanceof Exception)
				throw (Exception) t;
		}
		// System.out.println();
	}

	public void writeAddress(Address address) throws Exception {
		String temp = null;
		writeString("<Address><Prefix>");
		writeString(address.getPrefix());
		writeString("</Prefix><Host>");
		writeString(address.getHost());
		writeString("</Host><Port>");
		writeString(address.getPort());
		writeString("</Port>");
		temp = address.getPostfix();
		if (temp != null) {
			writeString("<Postfix>");
			writeString(temp);
			writeString("</Postfix>");
		}
		temp = address.getPlugin();
		if (temp != null) {
			writeString("<Plugin>");
			writeString(temp);
			writeString("</Plugin>");
		}
		temp = address.getMethod();
		if (temp != null) {
			writeString("<Method>");
			writeString(temp);
			writeString("</Method>");
		}
		temp = address.getType();
		if (temp != null) {
			writeString("<Type>");
			writeString(temp);
			writeString("</Type>");
		}
		temp = address.getEmail();
		if (temp != null) {
			writeString("<Email>");
			writeString(temp);
			writeString("</Email>");
		}
		Address route = address.getRoute();
		if (route != null) {
			writeString("<Route>");
			writeAddress(route);
			writeString("</Route>");
		}
		writeString("</Address>");
	}
}