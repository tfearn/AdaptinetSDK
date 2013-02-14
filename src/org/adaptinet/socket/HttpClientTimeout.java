/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */
package org.adaptinet.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

import sun.net.www.http.HttpClient;


public class HttpClientTimeout extends HttpClient {
	public HttpClientTimeout(URL url, String proxy, int proxyPort)
			throws IOException {
		super(url, proxy, proxyPort);
	}

	public HttpClientTimeout(URL url) throws IOException {
		super(url, (String) null, -1);
	}

	public HttpClientTimeout(URL url, int timeout) throws IOException {
		super(url, (String) null, -1);
	}

	public void SetTimeout(int i) throws SocketException {
		serverSocket.setSoTimeout(i);
	}

	public static HttpClientTimeout GetNew(URL url) throws IOException {
		HttpClientTimeout ret = (HttpClientTimeout) kac.get(url, null);
		if (ret == null) {
			ret = new HttpClientTimeout(url);
		} else {
			ret.url = url;
		}
		return ret;
	}

	public static HttpClientTimeout GetNew(URL url, int timeout)
			throws IOException {
		HttpClientTimeout ret = (HttpClientTimeout) kac.get(url,null);
		if (ret == null) {
			ret = new HttpClientTimeout(url, timeout);
		} else {
			ret.url = url;
		}
		return ret;
	}

	public void Close() throws IOException {
		serverSocket.close();
	}

	public Socket GetSocket() {
		return serverSocket;
	}
}
