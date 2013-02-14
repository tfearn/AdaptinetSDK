/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.plugins;

import java.util.LinkedList;

import org.adaptinet.socket.BaseSocketServer;
import org.adaptinet.socket.CallbackSocketListener;
import org.adaptinet.socket.CallbackSocketServer;
import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiverutils.Semaphore;



public class ProxyClient implements CallbackSocketListener {

	LinkedList<Object> messages = new LinkedList<Object>();

	CallbackSocketServer server = null;

	Semaphore sem = new Semaphore();

	static int staticport = 7070;

	public ProxyClient() {
		server = (CallbackSocketServer) BaseSocketServer
				.createInstance("CALLBACK");
		server.setSocketListener(this);
	}

	public void OnReceive(Object obj) {
		try {
			synchronized (messages) {
				messages.add(obj);
			}
			sem.semPost();
		} catch (Exception e) {
		}
	}

	public Object removeFirst() {

		sem.semWait();

		synchronized (messages) {
			return messages.removeFirst();
		}
	}

	public Object requestProxy() {

		while (true) {
			try {
				requestProxy(staticport);
			} catch (Exception e) {
				staticport++;
				if (staticport > 8000)
					return new Integer(0);
				continue;
			}
			break;
		}
		Integer ret = new Integer(staticport);
		staticport++;
		return ret;
	}

	public void requestProxy(int port) throws Exception {

		server.initialize(ITransceiver.getTransceiver(), staticport, 30);
	}
}