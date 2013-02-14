/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.socket;

import java.net.InetAddress;

import org.adaptinet.transceiverutils.CachedThread;



public class ProxySocketServer extends BaseSocketServer {

	protected ProxySocketServer() {
		super();
	}

	public void start(String identifier) {

		socketThread = new Thread(new Runnable() {

			public void run() {
				ProxySocket socket = null;
				try {
					socket = getNextSocket();
					socket.requestProxy();
					while (running == true) {
						if (socket != null) {
							socket.bind();
						}
						socket = getNextSocket();
					}
					cleanup(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		if (identifier != null) {
			socketThread.setName(identifier);
		} else {
			socketThread.setName("Transceiver");
		}
		socketThread.setPriority(Thread.MAX_PRIORITY);
		socketThread.start();
		doMaintenance();
	}

	final public void shutdown() {
		running = false;
	}

	public int getLocalPort() {
		return 0;
	}

	public InetAddress getInetAddress() {
		return serverSocket.getInetAddress();
	}

	protected SocketState addSocket() {

		CachedThread t = threadcache.getThread(true);
		ProxySocket socket = new ProxySocket(type, transceiver, this, t);
		SocketState state = socket.getState();
		socketList.add(state);
		state.setStatus(SocketState.FREE);
		freeList.add(state);

		return state;
	}

	protected synchronized ProxySocket getNextSocket() {

		ProxySocket socket = null;
		try {
			SocketState state = null;
			int i = freeList.size() - 1;

			if (i > 0) {
				state = (SocketState) freeList.remove(i);
			}

			if (debug) {
				System.out.println("Connection in slot "
						+ new Integer(state.getId()).toString()
						+ " is connecting.");
			}

			if (state != null) {
				state.setStatus(SocketState.BUSY);
			}
			socket = (ProxySocket) state.getBaseSocket();
		} catch (Exception e) {
			System.err.println(e);
		}
		return socket;
	}
}
