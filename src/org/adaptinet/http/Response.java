/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Response {
	public Response() {
	}

	public Response(OutputStream output, String newHost, String contentType) {
		os = output;
		host = newHost;
		this.contentType = contentType;
	}

	public void writeHeader(long contentLength) {
		try {
			os.write(HTTP.byteArrayVersion);
			os.write(Integer.toString(nStatus).getBytes());

			String strCode = "OK";
			if (nStatus < 200)
				strCode = HTTP.msg100[nStatus - 100];
			else if (nStatus < 300)
				strCode = HTTP.msg200[nStatus - 200];
			else if (nStatus < 400)
				strCode = HTTP.msg300[nStatus - 300];
			else if (nStatus < 500)
				strCode = HTTP.msg400[nStatus - 400];
			else if (nStatus < 600)
				strCode = HTTP.msg500[nStatus - 500];

			os.write(strCode.getBytes());
			os.write(HTTP.crlf.getBytes());

			os.write(HTTP.transceiver.getBytes());
			os.write(HTTP.crlf.getBytes());

			os.write(HTTP.date.getBytes());
			os.write(this.getDate().getBytes());
			os.write(HTTP.crlf.getBytes());

			os.write(HTTP.nocache.getBytes());
			os.write(HTTP.crlf.getBytes());

			if (nStatus == HTTP.UNAUTHORIZED) {
				os.write("WWW-Authenticate: Basic realm=\"Transceiver\""
						.getBytes());
				os.write(HTTP.crlf.getBytes());
				os.write(HTTP.crlf.getBytes());
				os.flush();
				return;
			}

			if (contentType == null) {
				os.write(HTTP.contentTypeXML.getBytes());
			} else {
				os.write(contentType.getBytes());
			}
			os.write(HTTP.crlf.getBytes());
			os.write(HTTP.acceptRange.getBytes());
			os.write(HTTP.crlf.getBytes());
			os.write(HTTP.lastModified.getBytes());
			os.write(this.getDate().getBytes());
			os.write(HTTP.crlf.getBytes());

			if (contentLength != 0) {
				os.write(HTTP.contentLength.getBytes());
				os.write(Long.toString(contentLength).getBytes());
				os.write(HTTP.crlf.getBytes());
			}
			os.write(HTTP.crlf.getBytes());

			if (nStatus >= 400)
				os.write(("<html><head><title>"
							+ Integer.toString(nStatus) + " " + strCode
							+ "</title></head><h1>" + strCode + "</h1></html>")
							.getBytes());

			os.flush();
		} catch (Throwable t) {
			System.out.println("Error in response");
			t.printStackTrace();
		}
	}

	public void respond() throws IOException {
		if (!bRespond)
			return;

		byte[] bytes = response.toByteArray();

		writeHeader(bytes.length);

		os.write(bytes);
		os.write(HTTP.crlf.getBytes());
		os.flush();
	}

	public void setHost(String newValue) {
		host = newValue;
	}

	public void setStatus(int newValue) {
		nStatus = newValue;
	}

	public void setResponse(ByteArrayOutputStream newValue) {
		try {
			if (newValue != null) {
				response = newValue;
			} else {
				response = new ByteArrayOutputStream();
				response.write(new String("Error in Processing Plugin")
						.getBytes());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private String getDate() {
		SimpleDateFormat format = new SimpleDateFormat(
				"EEE, dd MMM, yyyy hh:mm:ss zzz");
		return format.format(new Date());
	}

	public void setRespond(boolean b) {
		bRespond = b;
	}

	public OutputStream getOutputStream() {
		return os;
	}

	private boolean bRespond = true;

	@SuppressWarnings("unused")
	private String host = "localhost";

	private ByteArrayOutputStream response;

	private int nStatus = 200;

	private OutputStream os = null;

	private String contentType;
}
