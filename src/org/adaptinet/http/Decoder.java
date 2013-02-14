/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class Decoder {
	static public String decode(byte[] input) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(input);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int c = -1;
		while ((c = in.read()) > 0) {
			if (c == '+') {
				out.write(' ');
			} else if (c == '%') {
				int c1 = Character.digit((char) in.read(), 16);
				int c2 = Character.digit((char) in.read(), 16);
				out.write((char) (c1 * 16 + c2));
			} else {
				out.write(c);
			}
		}
		out.flush();
		return out.toString();
	}
}
