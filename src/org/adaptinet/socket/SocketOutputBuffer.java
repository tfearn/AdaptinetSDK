/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.socket;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class SocketOutputBuffer extends FilterOutputStream {

	private byte buf[] = null;

	private int count = 0;

	public SocketOutputBuffer(OutputStream outStream) {
		this(outStream, 512);
	}

	public SocketOutputBuffer(OutputStream outStream, int size) {
		super(outStream);
		buf = new byte[size];
		count = 0;
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		int avail = buf.length - count;
		if (len < avail) {
			System.arraycopy(b, off, buf, count, len);
			count += len;
			return;
		} else if ((avail > 0) && (count > 0)) {
			System.arraycopy(b, off, buf, count, avail);
			count += avail;
			flush();
			out.write(b, off + avail, len - avail);
		} else {
			flush();
			out.write(b, off, len);
		}
	}

	public void write(int b) throws IOException {
		if (count == buf.length) {
			flush();
			buf[count++] = (byte) b;
		}
	}

	public void close() throws IOException {
		try {
			flush();
			out.close();
		} catch (Exception e) {
			// System.out.println(e);
		}
		out = null;
		count = 0;
	}

	public void flush() throws IOException {
		if (count > 0) {
			out.write(buf, 0, count);
			count = 0;
		}
	}

	public void reuse(OutputStream newValue) {
		out = newValue;
		count = 0;
	}
}
