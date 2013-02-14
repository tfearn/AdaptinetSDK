/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */
package org.adaptinet.socket;

import org.adaptinet.transceiverutils.CachedThread;

public class CallbackSocketServer extends HttpSocketServer {

	private CallbackSocketListener listener = null;

	protected CallbackSocketServer() {
		super();
	}

	public void setSocketListener(CallbackSocketListener listener) {
		this.listener = listener;
	}

	protected SocketState addSocket() {

		CachedThread t = threadcache.getThread(true);
		CallbackSocket socket = new CallbackSocket(listener, type, transceiver,
				this, t);
		SocketState state = socket.getState();
		socketList.add(state);
		state.setStatus(SocketState.FREE);
		freeList.add(state);

		return state;
	}
}
