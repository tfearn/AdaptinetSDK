/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.pluginagent;

import org.adaptinet.loader.ClasspathLoader;
import org.adaptinet.transceiverutils.CachedThread;


public class PluginState {

	static final int IDLE = 0;

	static final int BUSY = 1;

	static final int FREE = 2;

	static final int KILL = 3;

	static final int FIN = 4;

	static final int TIMEDOUT = 5;

	static final int SIGNALED = 6;

	static final int INTERRUPTED = 7;

	private static int nextid = 0;

	private PluginAgent plugin = null;

	private int status = IDLE;

	private int id = 0;

	private long start = 0;

	private int timeout = -1;

	private ClasspathLoader loader = new ClasspathLoader(null);

	public PluginState(PluginAgent requestPlugin) {
		plugin = requestPlugin;
		id = nextId();
	}

	final public String getName() {
		return plugin.getName();
	}

	final public void startTimer() {
		start = System.currentTimeMillis();
	}

	final public long getStartTime() {
		return start;
	}

	final public boolean kill(boolean now) {
		boolean killed = false;

		plugin.cleanupPlugin();
		if (now == true) {
			killed = plugin.kill();
		} else {
			plugin.interrupt();
			join();
		}
		return killed;
	}

	final public void join() {
		plugin.join();
	}

	final public PluginAgent getPluginAgent() {
		return plugin;
	}

	final public void reset(CachedThread t) {
		plugin.reset(t);
	}

	final public void reset() {
		plugin.reset();
	}

	final public int getStatus() {
		return status;
	}

	final public void setStatus(int s) {
		status = s;
	}

	final public int getId() {
		return id;
	}

	final public int getTimeOut() {
		return timeout;
	}

	final public void setTimeOut(int i) {
		timeout = i;
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

	public void appendClasspath(String classpath) {
		loader.appendLocalClasspath(classpath);
	}

	public ClasspathLoader getLoader() {
		return loader;
	}
}
