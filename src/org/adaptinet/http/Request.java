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
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.adaptinet.mimehandlers.MimeBase;
import org.adaptinet.transceiver.ITransceiver;

import sun.misc.BASE64Decoder;


public class Request {

	static final public short NONE = 0;
	static final public short XML_TEXT = 1;
	static final public short HTML_TEXT = 2;
	static final public short HTML_IMAGE = 3;
	static final public short COMMAND = 4;
	static final public short SERVLET = 5;
	static final public short DATA_TEXT = 6;
	static final public String POST = "POST";
	static final public String GET = "GET";

	protected Socket client = null;
	protected String username = null;
	protected String password = null;
	protected OutputStream outs = null;
	protected String method = null;
	protected short major = 1;
	protected short minor = 1;
	protected HashMap<String, String> properties;
	protected short mimeType = 0;
	protected String request = null;
	protected String url = null;
	protected ITransceiver transceiver;
	protected int lport = 0;

	public Request() {
		properties = new HashMap<String, String>(10);
	}

	public String getHost() {
		return (String) properties.get("host");
	}

	public void putProperty(String name, String value) {
		properties.put(name, value);
	}

	public String getProperty(String name) {
		return (String) properties.get(name);
	}

	public Enumeration<String> getHeaderNames() {
		Vector<String> v = new Vector<String>(properties.keySet());
		return v.elements();
	}

	public String getRequest() {
		return request;
	}

	public int getPort() {
		return lport;
	}

	public void putRequest(String request, String requestType, String type) {

		this.type = type;
		String mime = (String) properties.get("content-type");

		if (url.startsWith("/servlet/")) {
			mimeType = SERVLET;
			this.request = request;
			return;
		}

		if (request != null) {
			// only decode for non-servlets
			try {
				if (mime != null
						&& mime.startsWith("application/x-www-form-urlencoded"))
					request = URLDecoder.decode(request, "UTF-8");
			} catch (Exception e) {
			}

			if (mime == null
					|| mime.startsWith("application/x-www-form-urlencoded")
					|| mime.startsWith("text/xml")) {
				int start = request.indexOf("<?xml");
				if (start >= 0) {
					mimeType = XML_TEXT;
					this.request = request.substring(start);
				} else {
					this.request = request;
					mimeType = COMMAND;
				}
			} else {
				this.request = request;
				mimeType = DATA_TEXT;
			}
		} else if (requestType.equals("GET")) {
			this.request = url;
			mimeType = HTML_TEXT;
		} else {
			mimeType = HTML_TEXT;
		}
	}

	public Response processRequest(ITransceiver transceiver, OutputStream out) {

		int status = 200;
		Response response = null;
		boolean respond = true;
		String contentType = null;
		outs = out;
		ByteArrayOutputStream result = null;
		MimeBase mime = null;
		this.transceiver = transceiver;

		try {
			lport = client.getLocalPort();

			switch (mimeType) {

			case XML_TEXT:
				mime = (MimeBase) Class.forName(
						"org.adaptinet.mimehandlers.MimeXML_" + type)
						.newInstance();
				contentType = HTTP.contentTypeXML;
				break;

			case DATA_TEXT:
				mime = (MimeBase) Class.forName(
						"org.adaptinet.mimehandlers.MimeData_" + type)
						.newInstance();
					contentType = HTTP.contentTypeData;
				break;

			case COMMAND:
				mime = (MimeBase) Class.forName(
						"org.adaptinet.mimehandlers.MimeCommand_" + type)
						.newInstance();
				contentType = HTTP.contentTypeHTML;
				break;

			case HTML_TEXT:
				if (type == null)
					type = "Plugin";
				mime = (MimeBase) Class.forName(
						"org.adaptinet.mimehandlers.MimeHTML_" + type)
						.newInstance();
				mime.init(url, transceiver);
				contentType = mime.getContentType();
				respond = false;
				break;

			case SERVLET:
				mime = (MimeBase) Class.forName(
						"org.adaptinet.mimehandlers.MimeServlet")
						.newInstance();
				mime.init(url, transceiver);
				respond = false;
				break;
			}

			if (mime != null) {
				result = mime.process(transceiver, this);
				status = mime.getStatus();
			}

			if (!respond)
				return null;

			response = new Response(out, getHost(), contentType);
			response.setResponse(result);
			response.setStatus(status);
		} catch (Exception e) {
			return null;
		}
		return response;
	}

	public short getRequestType() {
		return mimeType;
	}

	final public void setVersionMethod(String data) throws Exception {
		try {
			StringTokenizer tokenizer = new StringTokenizer(data);
			method = tokenizer.nextToken();
			url = tokenizer.nextToken();
			String version = tokenizer.nextToken();
			tokenizer = new StringTokenizer(version.substring(5), ".");
			version = tokenizer.nextToken();
			major = Short.parseShort(version);
			version = tokenizer.nextToken();
			minor = Short.parseShort(version);
		} catch (Exception e) {
			throw new Exception("Error parsing Version/URL/Method : " + data);
		}
	}

	final public String getMethod() {
		return method;
	}

	final public short majorVersion() {
		return major;
	}

	final public short minorVersion() {
		return minor;
	}

	public OutputStream getOutStream() {
		return outs;
	}

	public ITransceiver getServer() {
		return transceiver;
	}

	public final String getUsername() {
		try {
			BASE64Decoder b64 = new BASE64Decoder();
			String auth = getProperty("authorization");
			StringTokenizer st = new StringTokenizer(auth, " ");
			st.nextToken();
			byte[] b = b64.decodeBuffer(st.nextToken());
			String basic = new String(b);
			st = new StringTokenizer(basic, ":");
			if (st.hasMoreTokens())
				username = st.nextToken();
			if (st.hasMoreTokens())
				password = st.nextToken();
		} catch (NullPointerException npe) {
		} catch (IOException ioe) {
		}
		return username;
	}

	public final String getPassword() {
		getUsername();
		return password;
	}

	public final boolean isSecure() {
		try {
			int cport = client.getLocalPort(); // not sure about this because
			// of accept(), check this
			// out...
			return (cport == transceiver.getSecurePort());
		} catch (NullPointerException npe) {
		}
		return false;
	}

	public final String getRemoteAddr() {
		if (client == null)
			return null;
		return client.getInetAddress().getHostAddress();
	}

	public final String getRemoteHost() {
		if (client == null)
			return null;
		return client.getInetAddress().getHostName();
	}

	/**
	 * Called by ClientSocket during Connection
	 */
	public void setSocket(java.net.Socket s) {
		client = s;
	}

	public String getQueryString() {
		try {
			return url.substring(url.indexOf('?') + 1);
		} catch (Exception e) {
		}
		return "";
	}

	public String getRequestURI() {
		int sep = url.indexOf('?');
		if (sep == -1)
			return url;
		return url.substring(0, sep);
	}

	/*
	 * public String getPathTranslated() { return ""; }
	 * 
	 * public String getRealPath(String name) { File file = new File(name);
	 * return file.getAbsolutePath(); }
	 */
	private String type = null;
}
