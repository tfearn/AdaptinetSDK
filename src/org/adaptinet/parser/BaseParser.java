/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class BaseParser {

	protected Reader is = null;

	protected int ch = -1;

	protected char buffer[] = new char[128];

	protected int nSize = 0;

	public BaseParser() {
	}

	public BaseParser(InputSource is) {
		setInputStream(is);
	}

	public void setInputStream(InputSource is) {
		if ((this.is = is.getCharacterStream()) == null)
			this.is = new InputStreamReader(is.getByteStream());
	}

	public final Reader getReader() {
		return is;
	}

	public void setReader(Reader is) {
		this.is = is;
	}

	protected final String getWord() throws IOException {

		nSize = 0;
		String ret = null;

		while (ch != ' ' && ch != '/' && ch != '>' && ch >= 32) {
			append((char) ch);
			ch = is.read();
		}

		if (nSize > 0) {
			ret = getBuffer();
		}

		return ret;
	}

	protected final String getValue(int endChar, boolean inclusive)
			throws IOException {

		String ret = null;
		nSize = 0;

		if (inclusive == false) {
			ch = is.read();
		}

		while (ch != endChar && ch != -1) {
			append((char) ch);
			ch = is.read();
		}

		if (inclusive == true) {
			append((char) ch);
		}

		if (nSize > 0) {
			ret = getBuffer();
		}

		ch = is.read();

		return ret;
	}

	protected final String getToken(int endChar) throws IOException {

		String ret = null;
		nSize = 0;

		ch = is.read();

		while (ch != endChar && ch != -1) {
			append((char) ch);
			ch = is.read();
		}

		if (nSize > 0) {
			ret = getBuffer();
		}

		return ret;
	}

	protected final void skipUntil(int i) throws IOException {

		while ((ch != -1) && (ch != i)) {
			ch = is.read();
		}
	}

	protected final void skipUntilWhiteSpace() throws IOException {

		while ((ch != -1) && (ch != ' ') && (ch != '\t')) {
			ch = is.read();
		}
	}

	protected final void skipWhiteSpace() throws IOException {

		while ((ch == ' ') || (ch == '\t') || (ch == '\r') || (ch == '\n')) {
			ch = is.read();
		}
	}

	protected final String getBuffer() {
		String ret = null;
		try {
			ret = new String(buffer, 0, nSize);
		} catch (Exception e) {
			System.err.println(e);
		}
		return ret;
	}

	protected final String getBuffer(char b[]) {
		String ret = null;
		try {
			ret = new String(b, 0, b.length);
			// System.out.println(ret);
		} catch (Exception e) {
			System.err.println(e);
		}
		return ret;
	}

	protected final void append(int c) {

		if (nSize + 1 >= buffer.length) {
			char newBuffer[] = new char[buffer.length * 2];
			System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
			buffer = newBuffer;
		}
		buffer[nSize++] = (char) c;
	}

	protected void setInputStream(InputStream is) {
		this.is = new InputStreamReader(is);
	}

}
