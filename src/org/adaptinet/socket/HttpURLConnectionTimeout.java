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
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.PrivilegedAction;

import sun.net.www.http.HttpClient;

public class HttpURLConnectionTimeout extends
		sun.net.www.protocol.http.HttpURLConnection {
	int fiTimeoutVal;

	HttpTimeoutHandler fHandler;

	HttpClientTimeout fClient;

	public HttpURLConnectionTimeout(URL u, HttpTimeoutHandler handler,
			int iTimeout) throws IOException {
		super(u, handler);
		fiTimeoutVal = iTimeout;
	}

	public HttpURLConnectionTimeout(URL u, String host, int port)
			throws IOException {
		super(u, host, port);
	}

	public void connect() throws IOException {
		if (connected) {
			return;
		}
		try {
			if ("http".equals(url.getProtocol())) {
				synchronized (url) {
					http = HttpClientTimeout.GetNew(url, fiTimeoutVal);
				}
				fClient = (HttpClientTimeout) http;
				((HttpClientTimeout) http).SetTimeout(fiTimeoutVal);
			} else {
				http = new HttpClientTimeout(url, fHandler.GetProxy(), fHandler
						.GetProxyPort());
			}
			ps = (PrintStream) http.getOutputStream();
		} catch (IOException e) {
			throw e;
		}
		connected = true;
	}

	protected HttpClient getNewClient(URL url) throws IOException {
		HttpClientTimeout client = new HttpClientTimeout(url, (String) null, -1);
		try {
			client.SetTimeout(fiTimeoutVal);
		} catch (Exception e) {
			System.out.println("Unable to set timeout value");
		}
		return (HttpClient) client;
	}

	public static InputStream openConnectionCheckRedirects(URLConnection c)
			throws IOException {
		boolean redir;
		int redirects = 0;
		InputStream in = null;

		do {
			if (c instanceof HttpURLConnectionTimeout) {
				((HttpURLConnectionTimeout) c)
						.setInstanceFollowRedirects(false);
			}

			in = c.getInputStream();
			redir = false;

			if (c instanceof HttpURLConnectionTimeout) {
				HttpURLConnectionTimeout http = (HttpURLConnectionTimeout) c;
				int stat = http.getResponseCode();
				if (stat >= 300 && stat <= 305
						&& stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
					URL base = http.getURL();
					String loc = http.getHeaderField("Location");
					URL target = null;
					if (loc != null) {
						target = new URL(base, loc);
					}
					http.disconnect();
					if (target == null
							|| !base.getProtocol().equals(target.getProtocol())
							|| base.getPort() != target.getPort()
							|| !HostsEquals(base, target) || redirects >= 5) {
						throw new SecurityException("illegal URL redirect");
					}
					redir = true;
					c = target.openConnection();
					redirects++;
				}
			}
		} while (redir);
		return in;
	}

	static boolean HostsEquals(URL u1, URL u2) {
		final String h1 = u1.getHost();
		final String h2 = u2.getHost();

		if (h1 == null) {
			return h2 == null;
		} else if (h2 == null) {
			return false;
		} else if (h1.equalsIgnoreCase(h2)) {
			return true;
		}

		final boolean result[] = { false };

		java.security.AccessController
				.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						try {
							InetAddress a1 = InetAddress.getByName(h1);
							InetAddress a2 = InetAddress.getByName(h2);
							result[0] = a1.equals(a2);
						} catch (UnknownHostException e) {
						} catch (SecurityException e) {
						}
						return null;
					}
				});

		return result[0];
	}

	void Close() throws Exception {
		fClient.Close();
	}

	Socket GetSocket() {
		return fClient.GetSocket();
	}
}