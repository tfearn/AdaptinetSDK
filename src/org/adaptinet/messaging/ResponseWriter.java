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

public class ResponseWriter extends BaseWriter {
	public ResponseWriter(OutputStream ostream) {
		super(ostream);
	}

	public void writeResponse(Object ret) throws Exception {
		// Declared up here hopefully put into register.
		try {
			writeString("<Envelope><Body>");
			convertToXML(ret);
			writeString("</Body></Envelope>");
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			if (t instanceof Exception)
				throw (Exception) t;
		}
	}
}