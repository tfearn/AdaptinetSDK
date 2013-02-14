/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transmitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.parser.BaseParser;
import org.adaptinet.parser.InputSource;



public class ResponseParser extends BaseParser {

	static final private String BOUNDARY = "boundary";

	private byte boundary[];

	private Properties headers = new Properties();

	private String httpVersion = null;

	private int responseCode = 200;

	private String responseMessage = null;

	private String contentType = null;

	private int check = 0;

	private int len = 0;

	private boolean bFirstTime = true;

	private boolean bStopSubscription = false;

	public ResponseParser(InputStream is) {
		super(new InputSource(is));
	}

	public String parse() {
		String result = null;
		try {
			parseHeading();
			result = parseResponse();
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Start connection failed reason: "
					+ e.getMessage());
		}
		return result;
	}

	public String getSubscriptionData() {
		String result = null;
		if (bStopSubscription == true)
			return null;
		try {
			if (bFirstTime == true) {
				bFirstTime = false;
				parseHeading();
				// Eat the first boundry
				// should be a blank-line.
				parseResponse();
			}
			// we need to eat the blank-line
			while (true) {
				ch = is.read();
				if (ch == '\r') {
					if ((ch = is.read()) == '\n')
						break;
				} else if (ch == '\n') {
					break;
				}
			}
			// we should get a content-type header to start.
			parseHeaders();
			result = parseResponse();
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Start connection failed reason: "
					+ e.getMessage());
		}
		return result;
	}

	public void parseHeading() throws IOException, AdaptinetException {

		String length = null;

		try {
			parseResponseLine();
			parseHeaders();

			if (responseCode == 200) {
				if (parseContentType(headers.getProperty("content-type")) == false) {
					length = headers.getProperty("content-length");
					if (length != null)
						len = new Integer(length).intValue();
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
	}

	public String parseResponse() throws IOException, AdaptinetException {

		String response = null;

		try {
			if (len > 0) {
				char[] data = new char[len];
				int ch = 0;

				for (int i = 0; i < len; i++) {
					ch = is.read();
					data[i] = (char) ch;
				}

				response = getBuffer(data);
				while (is.read() > 0)
					;
			} else if (boundary.length > 0) {
				response = getTillboundary();
			}
		} catch (IndexOutOfBoundsException e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("The data required size: "
					+ Integer.toString(len) + " excceded the amount available"
					+ e.getMessage());
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Start connection failed reason: "
					+ e.getMessage());
		}
		return response;
	}

	private void parseResponseLine() throws IOException {
		// Prime the pump
		while (true) {

			ch = is.read();
			if (ch == '\r') {
				if ((ch = is.read()) == '\n')
					break;
			} else if (ch == '\n') {
				break;
			}
			append((char) ch);
		}

		String line = getBuffer();
		httpVersion = line.substring(0, 8);
		responseCode = Integer.parseInt(line.substring(9, 12));
		responseMessage = line.substring(13);

	}

	final protected void parseHeaders() throws IOException {

		ch = is.read();
		while (true) {
			if (ch == '\r') {
				if ((ch = is.read()) == '\n') {
					ch = is.read();
					return;
				}
			} else if (ch == '\n') {
				ch = is.read();
				return;
			}

			String name = getHeaderName();
			skipWhiteSpace();
			parseHeader();
			name = name.toLowerCase();
			headers.put(name, getBuffer());
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

	final private boolean parseContentType(String type) {
		boolean bret = false;
		if (type != null && type.startsWith("multipart/x-mixed-replace")) {
			StringTokenizer tokenizer = new StringTokenizer(type, ";");
			String token = null;
			if (tokenizer.hasMoreTokens())
				contentType = tokenizer.nextToken();

			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				if (token.toLowerCase().startsWith(BOUNDARY)) {
					boundary = ("--" + token.substring(token.indexOf("=") + 1))
							.getBytes();
					bret = true;
					break;
				}
			}
		}
		return bret;
	}

	protected final String getTillboundary() throws IOException {

		nSize = 0;
		check = 0;
		String ret = null;

		while (ch != -1 && checkboundary() == false) {
			append((char) ch);
			ch = is.read();
		}

		if (nSize > 0) {
			nSize -= boundary.length;
			ret = getBuffer();
		}

		// we need to eat the blank-line and check for the very end.
		while (true) {
			ch = is.read();
			if (ch == '-') {
				if ((ch = is.read()) == '-') {
					bStopSubscription = true;
					break;
				}
			} else if (ch != '\r' && ch != '\n') {
				break;
			}
		}

		return ret;
	}

	protected final boolean checkboundary() {
		if (ch == boundary[check]) {
			check++;
			if (check == boundary.length) {
				return true;
			}
		}
		return false;
	}

	final public String getVersion() {
		return httpVersion;
	}

	final public int getresponseCode() {
		return responseCode;
	}

	final public String getresponseMessage() {
		return responseMessage;
	}

	final public String getContentType() {
		return contentType;
	}

	final public Properties getResponseHeaders() {
		return headers;
	}

	final public boolean isSubscription() {
		return boundary.length > 0;
	}
}
