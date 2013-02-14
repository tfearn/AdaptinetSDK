/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.http;

import java.io.IOException;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.parser.BaseParser;
import org.adaptinet.parser.InputSource;



public class Parser extends BaseParser {

	private String type = null;

	public Parser(InputSource is, String type) {
		super(is);
		this.type = type;
	}

	public Request parse() throws IOException, AdaptinetException {
		Request request = null;
		String length = null;

		try {
			request = new Request();
			request.setVersionMethod(getValue('\n', false));
			parseHeaders(request);

			String method = request.getMethod();
			if (method != null) {
				if (method.equals(Request.POST)) {
					length = request.getProperty("content-length");
					if (length != null) {
						Integer num = new Integer(length);
						int nLength = num.intValue();
						if (nLength > 0) {
							byte[] data = new byte[nLength];
							int ch = 0;

							for (int i = 0; i < nLength; i++) {
								ch = is.read();
								data[i] = (byte) ch;
							}
							request.putRequest(new String(data), Request.POST,
									type);
							// while(is.read()>0);
						}
					} else {
						AdaptinetException exMessage = new AdaptinetException(
								AdaptinetException.GEN_MESSAGE,
								AdaptinetException.SEVERITY_SUCCESS);
						exMessage
								.logMessage("Content-Length: appears to be zero on a POST "
										+ length);
						throw exMessage;
					}
				} else if (method.equals(Request.GET)) {
					request.putRequest(null, Request.GET, type);
				}
			}
		} catch (IndexOutOfBoundsException e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("The data required size: " + length
					+ " excceded the amount available" + e.getMessage());
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Start connection failed reason: "
					+ e.getMessage());
		}

		return request;
	}

	final protected void parseHeaders(Request request) throws IOException {

		while (true) {
			if (ch == '\r') {
				if ((ch = is.read()) == '\n')
					return;
			} else if (ch == '\n') {
				return;
			}

			String name = getHeaderName();
			skipWhiteSpace();
			parseHeader();
			name = name.toLowerCase();
			request.putProperty(name, getBuffer());
		}
	}

	protected String getHeaderName() throws IOException {

		nSize = 0;
		while ((ch >= 32) && (ch != ':')) {
			append((char) ch);
			ch = is.read();
		}

		String ret = null;
		if (ch == ':') {
			ch = is.read();
			if (nSize > 0) {
				ret = getBuffer();
			}
		}

		return ret;
	}

	protected void parseHeader() throws IOException {
		boolean bContinue = true;
		nSize = 0;

		skipWhiteSpace();

		while (bContinue) {
			switch (ch) {
			case -1:
				bContinue = false;
				break;

			case '\r':
				if ((ch = is.read()) != '\n') {
					append('\r');
					continue;
				}

				// fall-thru
			case '\n':
				switch (ch = is.read()) {
				case ' ':
				case '\t':
					skipWhiteSpace();
					append(ch);
					break;
				default:
					bContinue = false;
					break;
				}
				break;

			default:
				append((char) ch);
				ch = is.read();
				break;
			}
		}
	}

	protected final void getVersion(String version) {
	}

}
