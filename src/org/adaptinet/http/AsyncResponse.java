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
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.lang.SecurityException;
import java.net.Socket;
import java.net.URLEncoder;

public class AsyncResponse {

	public AsyncResponse() {
	}

	public void Respond(String xmlBuffer) {
		String responseData = null;
		try {
			String postData = URLEncoder.encode(xmlBuffer.toString(), "UTF-8");
			if (postData != null) {
				Socket s = null;
				int port = 80;
				s = new Socket("localhost", port);
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				DataInputStream dis = new DataInputStream(s.getInputStream());

				dos.writeBytes("POST " + "HTTP/1.1\r\n" + "Content-length: "
						+ postData.length() + "\r\n\r\n");
				dos.writeBytes(postData);
				dos.close();

				StringBuffer readBuffer = new StringBuffer();
				byte[] bytes = new byte[128];

				while (dis.read(bytes) != -1)
					readBuffer.append(bytes);

				responseData = readBuffer.toString();
			}
		} catch (IOException ioe) {
			responseData = ioe.getMessage();
			System.err.println(responseData);
		} catch (SecurityException se) {
			responseData = se.getMessage();
			System.err.println(responseData);
		}
	}
}
