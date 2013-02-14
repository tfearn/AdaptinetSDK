/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiverutils;

public class CachedThread extends Thread {

	Runnable runner = null;

	private boolean alive = true;

	ThreadCache cache = null;

	CachedThread next = null;

	boolean terminated = false;

	boolean started = false;

	boolean firstime = true;

	boolean bRunning = false;

	CachedThread(ThreadCache threadCache, int id) {
		super(threadCache.getThreadGroup(), threadCache.getThreadGroup()
				.getName()
				+ ":" + id);
		cache = threadCache;
		setPriority(cache.getThreadPriority());
		setDaemon(true);
	}

	synchronized public boolean isTerminated() {
		boolean ret = terminated;
		terminated = true;
		return ret;
	}

	synchronized Runnable waitForRunner() {
		boolean bTimedout = false;

		while (alive == true) {
			if (runner != null) {
				Runnable timeOutRunner = runner;
				firstime = false;
				runner = null;
				return timeOutRunner;
			} else if (firstime) {
				try {
					wait();
				} catch (InterruptedException ex) {
				}
			} else if (alive = cache.isFree(this, bTimedout)) {
				try {
					int idleTimeout = cache.getIdleTimeout();
					if (idleTimeout > 0) {
						wait(idleTimeout);
					} else {
						wait();
					}
				} catch (InterruptedException ex) {
				}
			}
		}
		return null;
	}

	synchronized public boolean kill() {
		if (bRunning == true) {
			alive = false;
			bRunning = false;
			this.notifyAll();
		}
		return bRunning;
	}

	synchronized public boolean wakeup(Runnable runnable) {
		boolean ret = false;
		if (alive) {
			runner = runnable;
			if (started == false) {
				this.start();
			}
			notify();
			ret = true;
		}
		return ret;
	}

	public synchronized void start() {
		super.start();
		this.started = true;
	}

	public void run() {
		try {
			while (true) {
				Runnable timeOutRunner = waitForRunner();
				if (timeOutRunner != null) {
					bRunning = true;
					timeOutRunner.run();
					bRunning = false;
				}
				if (alive == false) {
					break;
				}
			}
		} catch (NoSuchMethodError e) {
		} finally {
			cache.isDead(this);
		}
	}
}
