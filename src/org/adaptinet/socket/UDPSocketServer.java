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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.adaptinetex.TransceiverException;
import org.adaptinet.transceiverutils.CachedThread;

public class UDPSocketServer extends BaseSocketServer {


	protected UDPSocketServer() {
		super();
	}

	protected DatagramSocket createDatagramSocket() {

		try {
			if (bindAddr != null) {
				return new DatagramSocket(port);
			} else {
				return new DatagramSocket(port, bindAddr);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public void start(String identifier) {

		socketThread = new Thread(new Runnable() {

			public void run() {
				while (running == true) {
					Socket ns = null;

					try {
						ns = serverSocket.accept();
						if (running == false)
							break;
						ns.setTcpNoDelay(true);
						handleConnection(ns);
					} catch (IOException ioe) {
						try {
							ns.close();
						} catch (Exception e) {
							new TransceiverException(
									AdaptinetException.SEVERITY_FATAL, 999)
									.logMessage(e);
						}
					}
				}

				try {
					serverSocket.close();
					serverSocket = null;
				} catch (IOException ioe) {
				}

				try {
					cleanup(true);
				} catch (Exception e) {
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

	protected synchronized void handleConnection(Socket socket) {

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
				state.bind(socket);

				if (debug) {
					System.out.println("Connection in slot "
							+ new Integer(state.getId()).toString()
							+ " is connected.");
				}
			} else {
				try {
					socket.close();
				} catch (IOException ex) {
					System.out.println(ex);
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	final public void shutdown() {

		try {
			running = false;
			Socket unlock = null;
			unlock = new Socket(bindAddr, port);
			unlock.close();
		} catch (Exception e) {
		}
	}

	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	public InetAddress getInetAddress() {
		return serverSocket.getInetAddress();
	}

	protected SocketState addSocket() {

		CachedThread t = threadcache.getThread(true);
		ClientSocket socket = new ClientSocket(type, transceiver, this, t);
		SocketState state = socket.getState();
		socketList.add(state);

		state.setStatus(SocketState.FREE);
		freeList.add(state);

		return state;
	}
}
