/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transmitter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class HttpConnection {

	static final private String VERSION = " HTTP/1.1\r\n";

	static final private String ACCEPT = "Accept: */*\r\n";

	static final private String USERAGENT = "User-Agent: Mozilla/4.0 (Compatile)\r\n";

	static final private String HOST = "Host: ";

	static final private String NEWLINE = "\r\n";

	private Socket socket;

	private String host;

	private int port;

	protected int responseCode = -1;

	protected String responseMessage = null;

	private String method = "GET";

	private String uri;

	private boolean connected = false;

	private BufferedOutputStream bos = null;

	private Map<String, String> headerFields = Collections
			.synchronizedMap(new HashMap<String, String>());

	private boolean useProxy = false;

	private String proxyHost = "sfl9ps02.nbam.com";

	private String proxyPort = "80";

	static final String[] methods = { "GET", "POST", "HEAD", "OPTIONS", "PUT",
			"DELETE", "TRACE" };

	static public void main(String args[]) {
		try {
			HttpConnection conn = new HttpConnection(
					"http://localhost:7001/keepalive/servlet1");
			conn.setContentType("html/text");
			conn.openConnection();
			OutputStream os = conn.getOutputStream();
			os.flush();
			conn.shutdownOutput();
			ResponseParser parser = new ResponseParser(conn.getInputStream());
			// System.out.println(parser.parse());

			String data = null;
			while ((data = parser.getSubscriptionData()) != null) {
				System.out.println(data);
			}

			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HttpConnection(String url) throws Exception {
		if (useProxy) {
			System.getProperties().put("proxySet", "true");
			System.getProperties().put("proxyHost", proxyHost);
			System.getProperties().put("proxyPort", proxyPort);
		}
		setURL(url);
	}

	public void shutdownOutput() throws IOException {
		socket.shutdownOutput();
	}

	public void openConnection() throws Exception {

		connected = true;
		socket = new Socket(host, port);
		OutputStream os = socket.getOutputStream();
		bos = new BufferedOutputStream(os);
		bos.write(method.getBytes());
		bos.write(' ');
		bos.write(uri.getBytes());
		bos.write(VERSION.getBytes());
		bos.write(USERAGENT.getBytes());
		bos.write(HOST.getBytes());
		bos.write(host.getBytes());
		bos.write(NEWLINE.getBytes());
		bos.write(ACCEPT.getBytes());

		Iterator<String> it = headerFields.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = headerFields.get(key);
			bos.write(key.getBytes());
			bos.write(':');
			bos.write(value.getBytes());
			bos.write(NEWLINE.getBytes());
		}

		bos.write(NEWLINE.getBytes());
		bos.flush();
	}

	public void setRequestMethod(String method) throws ProtocolException {
		if (connected) {
			throw new ProtocolException("Can't reset method: already connected");
		}
		this.method = method;
	}

	public String getRequestMethod() {
		return method;
	}

	public int getResponseCode() throws IOException {
		return responseCode;
	}

	public String getResponseMessage() throws IOException {
		return responseMessage;
	}

	public void setContentLength(int len) {
		setHeaderField("content-length", Integer.toString(len));
	}

	public int getContentLength() {
		return getHeaderFieldInt("content-length", -1);
	}

	public String getContentType() {
		return getHeaderField("content-type");
	}

	public void setContentType(String type) {
		setHeaderField("content-type", type);
	}

	public String getContentEncoding() {
		return getHeaderField("content-encoding");
	}

	public long getExpiration() {
		return getHeaderFieldDate("expires", 0);
	}

	public long getDate() {
		return getHeaderFieldDate("date", 0);
	}

	public long getLastModified() {
		return getHeaderFieldDate("last-modified", 0);
	}

	public Map<String, String> getHeaderFields() {
		return headerFields;
	}

	public int getHeaderFieldInt(String name, int Default) {
		String value = getHeaderField(name);
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
		}
		return Default;
	}

	public long getHeaderFieldDate(String name, long Default) {
		String dateString = getHeaderField(name);
		try {
			dateString.trim();
			if (dateString.indexOf("GMT") == -1) {
				dateString = dateString + " GMT";
			}
			return new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z").parse(
					dateString).getTime();
		} catch (Exception e) {
		}
		return Default;
	}

	public String getHeaderField(String name) {
		return headerFields.get(name);
	}

	public void setHeaderField(String name, String value) {
		headerFields.put(name, value);
	}

	public void setURL(String url) throws Exception {

		int start = 0;
		this.uri = url;

		if (url.startsWith("https://")) {
			start = 8;
			// preffix = "https";
		} else {
			// preffix = "http";
			if (url.startsWith("http://")) {
				start = 7;
			}
		}

		int next = url.indexOf(":", start);
		if (next < 0) {
			next = url.indexOf("/", start);
			if (next < 0) {
				host = url.substring(start);
			} else {
				host = url.substring(start, next);
			}
			port = 80;
		} else if (next > -1) {
			host = url.substring(start, next);
			start = next + 1;
			next = url.indexOf("/", start);
			if (next < 0) {
				port = Integer.parseInt(url.substring(start, next));
			} else {
				port = Integer.parseInt(url.substring(start, next));
			}
		}

		if (next > 0 && next + 1 < url.length()) {
			start = next + 1;
			next = url.indexOf("/", start);
			// path = new String(url.substring(start));
		}
	}

	final public OutputStream getOutputStream() throws IOException,
			SocketException {
		return socket.getOutputStream();
	}

	final public InputStream getInputStream() throws IOException,
			SocketException {
		return socket.getInputStream();
	}

	final public void setKeepAlive(boolean b) throws SocketException {
		socket.setKeepAlive(b);
	}

	final public void setReceiveBufferSize(int size) throws SocketException {
		socket.setReceiveBufferSize(size);
	}

	final public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}
}
