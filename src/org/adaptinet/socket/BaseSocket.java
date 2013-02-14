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
import java.io.OutputStream;
import java.net.Socket;

import org.adaptinet.http.Parser;
import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiverutils.CachedThread;


abstract public class BaseSocket implements Runnable {

	protected String address = null;

	protected boolean bSecure = false;

	protected String type = null;

	protected boolean cont = false;

	protected SocketState state = null;

	protected boolean isAlive = false;

	protected int bufsize = 4096;

	protected CachedThread thread = null;

	protected boolean idle = false;

	protected SocketOutputBuffer bufout = null;

	protected BaseSocketServer baseSocketServer = null;

	protected Parser Parser;

	protected int identifier = -1;

	protected ITransceiver transceiver = null;

	protected byte buffer[] = null;

	protected boolean running = false;

	protected InputStream input = null;

	protected boolean interrupted = false;

	static protected int timeout = 0;

	static {
		timeout = ITransceiver.getTransceiver().getConnectionTimeOut();
	}

	public BaseSocket(String type, ITransceiver transceiver,
			BaseSocketServer baseSocketServer, CachedThread thread) {

		this.type = type;
		this.thread = thread;
		this.baseSocketServer = baseSocketServer;
		state = new SocketState(this);
		initialize(transceiver, state.getId());
		isAlive = true;
	}

	abstract protected void terminate();

	abstract public int sendContinue() throws IOException;

	abstract public OutputStream getOutputStream();

	private void initialize(ITransceiver s, int id) {
		transceiver = s;
		identifier = id;
	}

	@SuppressWarnings("unused")
	private void initialize(ITransceiver s, String address) {
		transceiver = s;
		this.address = address;
	}

	public synchronized void bind() throws Exception {
	}

	public synchronized void bind(Socket s) {
	}

	final public boolean wakeup() {
		return thread.wakeup(this);
	}

	final public void join() {

		if (thread != null) {
			while (true) {
				try {
					thread.join();
				} catch (InterruptedException ex) {
					System.out.println(ex);
				}
			}
		}
	}

	final public SocketState getState() {
		return state;
	}

	final protected Thread getThread() {
		return thread;
	}

	final protected synchronized void unbind() {
		interruptConnection(true);
	}

	final protected synchronized void kill(boolean now) {
		isAlive = false;
		interruptConnection(now);
	}

	protected synchronized void interruptConnection(boolean now) {

		if (running == true) {
			interrupted = true;
			if (now == true) {
				terminate();
			}
		}
	}

	public final int getIdentifier() {
		return identifier;
	}

	public final synchronized boolean isRunning() {
		return running;
	}

	public final boolean isInterrupted() {
		return interrupted;
	}

	public final ITransceiver getServer() {
		return transceiver;
	}

	public InputStream getInputStream() {
		return input;
	}

}
