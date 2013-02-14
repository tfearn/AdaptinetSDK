/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.socket;

import java.net.Socket;

public class SocketState {
	public static final int IDLE = 0;

	public static final int BUSY = 1;

	public static final int FREE = 2;

	public static final int KILL = 3;

	public static final int FIN = 4;

	public static final int TIMEDOUT = 5;

	public static final int SIGNALED = 6;

	public static final int INTERRUPTED = 7;

	private static int nextid = 0;

	private BaseSocket socket = null;

	private int status = IDLE;

	private int id = 0;

	private long start = 0;

	private int timeout = -1;

	public SocketState(BaseSocket c) {
		socket = c;
		id = nextId();
	}

	final public void startTimer() {
		start = System.currentTimeMillis();
	}

	final public long getStartTime() {
		return start;
	}

	final public void kill(boolean now) {
		socket.kill(now);
	}

	final public void join() {
		socket.join();
	}

	final public void bind(Socket s) {
		socket.bind(s);
	}

	final public void bind() throws Exception {
		socket.bind();
	}

	final public void unbind() {
		socket.unbind();
	}

	final public BaseSocket getBaseSocket() {
		return socket;
	}

	final public int getStatus() {
		return status;
	}

	final public void setStatus(int s) {
		status = s;
	}

	final public int getTimeOut() {
		return timeout;
	}

	final public void setTimeOut(int i) {
		timeout = i;
	}

	final public int getId() {
		return id;
	}

	static final synchronized int nextId() {
		return nextid++;
	}

	public boolean isTimedOut() {
		boolean ret = false;
		if (timeout > 0) {
			if (status == TIMEDOUT) {
				ret = true;
			} else {
				long current = System.currentTimeMillis();
				if (current > start + timeout) {
					ret = true;
				}
			}
		}
		return ret;
	}

}
