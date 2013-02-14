/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.cache;

import java.util.Map.Entry;

import org.adaptinet.messaging.Address;
import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.pluginagent.PluginFactory;
import org.adaptinet.pluginagent.Worker;
import org.adaptinet.transceiver.ITransceiver;


public class CacheWorker extends Worker {

	static final private String PUT = "/cache/put";
	static final private String REMOVE = "/cache/remove";
	static final private String SETLOCKED = "/cache/setLocked";

	public CacheWorker(Address address) {
		super(address);
	}

	public void put(String name, String key, String value) {
		Message message = null;

		try {
			message = new Message(address.getURL() + PUT);
			message.getReplyTo().setPlugin(PluginFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, key, value);
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void remove(String name, String key) {
		Message message = null;

		try {
			message = new Message(address.getURL() + REMOVE);
			message.getReplyTo().setPlugin(PluginFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, key);
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void setLocked(String name, boolean b) {
		Message message = null;

		try {
			message = new Message(address.getURL() + SETLOCKED);
			message.getReplyTo().setPlugin(PluginFactory.CACHE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.postMessage(message, name, new Boolean(b));
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}

	public void doCacheCheck() {
		Message message = null;

		try {
			ITransceiver transceiver = ITransceiver.getTransceiver();
			for (Entry<String, Cache> entry : CachePlugin.getNamedCaches().entrySet()) {
				message = new Message(address.getURL()
						+ "/CachePlugin/cacheCheck");
				message.getReplyTo().setPlugin("CachePlugin");
				bresponded = false;
				starttime = System.currentTimeMillis();
				Messenger.postMessage(message, entry.getKey());
			}
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}
	
}
