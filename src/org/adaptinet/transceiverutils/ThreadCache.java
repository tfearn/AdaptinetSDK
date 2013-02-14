/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiverutils;

public class ThreadCache {

	@SuppressWarnings("unused")
	private static final boolean debug = false;

	private static final int DEFAULT_CACHESIZE = 5;

	private ThreadGroup group = null;

	private int cachesize = DEFAULT_CACHESIZE;

	private int threadcount = 0;

	private int threadid = 0;

	private int idlethreads = 0;

	private int usedthreads = 0;

	private CachedThread freelist = null;

	private int idletimeout = 0;

	private int threadpriority = 0;

	public ThreadCache(String name, int cachesize, int priority, int idleTimeout) {
		this(new ThreadGroup(name));

		setCachesize(cachesize);
		setThreadPriority(priority);
		setIdleTimeout(idleTimeout);
		initialize();
	}

	public ThreadCache(ThreadGroup parent, String name, int cachesize,
			int priority, int idleTimeout) {
		this(new ThreadGroup(parent, name));
		setCachesize(cachesize);
		setThreadPriority(priority);
		setIdleTimeout(idleTimeout);
		initialize();
	}

	private synchronized void initialize() {
		for (int i = 0; i < idlethreads; i++) {
			CachedThread t = createThread();
			t.next = freelist;
			freelist = t;
			t.start();
		}
	}

	synchronized final int getIdleTimeout() {
		return (threadcount <= idlethreads) ? -1 : idletimeout;
	}

	final synchronized boolean isFree(CachedThread t, boolean timedout) {
		if (timedout && (threadcount > idlethreads)) {
			if (t.isTerminated() == false) {
				threadcount--;
				usedthreads--;
				notifyAll();
			}
			return false;
		} else if (cachesize == 0 || threadcount <= cachesize) {
			t.next = freelist;
			freelist = t;
			usedthreads--;
			notifyAll();
			return true;
		} else {
			if (t.isTerminated() == false) {
				threadcount--;
				usedthreads--;
				notifyAll();
			}
			return false;
		}
	}

	final synchronized void isDead(CachedThread t) {
		if (t.isTerminated() == false) {
			threadcount--;
			notifyAll();
		}
	}

	private synchronized CachedThread createThread() {
		threadcount++;
		threadid++;
		return new CachedThread(this, threadid);
	}

	protected synchronized CachedThread allocateThread(boolean waitp) {
		CachedThread t = null;

		if (freelist != null) {
			t = freelist;
			freelist = freelist.next;
		} else {
			t = createThread();
		}

		return t;
	}

	public void setCachesize(int cachesize) {
		this.cachesize = cachesize;
		this.idlethreads = (cachesize >> 1);
	}

	private synchronized void setIdleTimeout(int idleTimeout) {
		this.idletimeout = idleTimeout;
	}

	public void setThreadPriority(int priority) {
		threadpriority = priority;
	}

	public int getThreadPriority() {
		return threadpriority;
	}

	public boolean getThread(Runnable runnable, boolean waitp) {
		while (true) {
			CachedThread t = allocateThread(waitp);
			if (t != null) {
				usedthreads++;
				if (t.wakeup(runnable))
					return true;
			} else {
				return false;
			}
		}
	}

	public CachedThread getThread(boolean waitp) {
		while (true) {
			CachedThread t = allocateThread(waitp);
			if (t != null) {
				usedthreads++;
				return t;
			}
		}
	}

	public ThreadGroup getThreadGroup() {
		return group;
	}

	public synchronized void waitForCompletion() {
		while (usedthreads > 0) {
			try {
				wait();
			} catch (InterruptedException ex) {
			}
		}
	}

	public ThreadCache(ThreadGroup threadGroup) {
		group = threadGroup;
	}
}
