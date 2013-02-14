/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.pluginagent;

import org.adaptinet.adaptinetex.PluginException;
import org.adaptinet.messaging.Message;
import org.adaptinet.transceiver.ITransceiver;


abstract public class PluginRoot {

	protected ITransceiver transceiver = null;

	protected Message msg = null;

	protected PluginAgent agent = null;

	public PluginRoot() {
		transceiver = ITransceiver.getTransceiver();
	}

	public abstract void init();

	public abstract void cleanup();

	public abstract Object sendMessage(Message message, Object ... args)
	throws PluginException;
	
	public abstract void broadcastMessage(String toUri, Object... args)
			throws PluginException;

	public abstract void broadcastMessage(String toUri,
			ITransceiver replyTransceiver, Object... args)
			throws PluginException;

	public abstract void postMessage(Message message) throws PluginException;

	public abstract void postMessage(Message message, Object... args)
			throws PluginException;

	public abstract void localPostMessage(Message message, Object... args)
			throws PluginException;

	public abstract void broadcastMessage(Message message, Object... args)
			throws PluginException;

	final void setCurrentMessage(Message msg) {
		this.msg = msg;
	}

	final void setAgent(PluginAgent agent) {
		this.agent = agent;
	}

	final void setTransceiver(ITransceiver transceiver) {
		this.transceiver = transceiver;
	}

	public void unloadPlugin() {
		ITransceiver.getTransceiver().killRequest(agent.getName(), true);
	}

	public void shutdownTransceiver() {
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				try {
					transceiver.shutdown();
					System.exit(1);
				} catch (Exception e) {
				}
			}
		}).start();

	}
}
