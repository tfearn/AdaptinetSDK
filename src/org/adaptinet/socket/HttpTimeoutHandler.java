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
import java.net.URL;

public class HttpTimeoutHandler extends sun.net.www.protocol.http.Handler {
	int fiTimeoutVal;

	HttpURLConnectionTimeout fHUCT;

	public HttpTimeoutHandler(int iT) {
		fiTimeoutVal = iT;
	}

	protected java.net.URLConnection openConnection(URL u) throws IOException {
		return fHUCT = new HttpURLConnectionTimeout(u, this, fiTimeoutVal);
	}

	String GetProxy() {
		return proxy;
	}

	int GetProxyPort() {
		return proxyPort;
	}

	public void Close() throws Exception {
		fHUCT.Close();
	}

	public Socket GetSocket() {
		return fHUCT.GetSocket();
	}
}