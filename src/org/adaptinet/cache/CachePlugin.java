/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.adaptinetex.PluginException;
import org.adaptinet.messaging.Address;
import org.adaptinet.messaging.Body;
import org.adaptinet.messaging.Envelope;
import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.pluginagent.PluginBase;
import org.adaptinet.registry.PluginEntry;


public final class CachePlugin extends PluginBase implements ICacheListener {

	static final public String PUT = "put";
	static final public String REMOVE = "remove";
	static final public String SETLOCKED = "setLocked";
	static final public String GETNAMEDCACHE = "getNamedCache";
	static final public String REMOVENAMEDCACHE = "removeNamedCache";
	static final public String CACHECHECK = "cacheCheck";
	
	static private Map<String, CacheWorker> workers = Collections
			.synchronizedMap(new HashMap<String, CacheWorker>(10));
	static private HashMap<String, Cache> namedCaches = new HashMap<String, Cache>();
	static private CachePlugin cachePlugin = null;

	public CachePlugin() {
		cachePlugin = this;
	}

	static public final Cache getNamedCache(Object key) {
		return getNamedCache(key, true);
	}
	
	static public final Cache getNamedCache(Object key, boolean bNew) {

		Cache namedCache = namedCaches.get(key);
		if (namedCache == null && bNew) {
			namedCache = new Cache(cachePlugin, key.toString());
			namedCaches.put((String)key, namedCache);
		}
		return namedCache;
	}

	static public final HashMap<String, Cache> getNamedCaches() {
		return namedCaches;
	}

	public void startPlugin(PluginEntry entry) throws Exception {
	}

	public boolean preProcessMessage(Envelope env) {
		return true;
	}

	public Object process(Envelope env) throws Exception {

		try {
			if (env.isMethod(PUT)) {
				put(env);
			} else if (env.isMethod(REMOVE)) {
				remove(env);
			} else if (env.isMethod(SETLOCKED)) {
				setLocked(env);
			} else if (env.isMethod(GETNAMEDCACHE)) {
				getNamedCache(env);
			} else if (env.isMethod(CACHECHECK)) {
				cacheCheck(env);
			} else if (env.isMethod(REMOVENAMEDCACHE)) {
				removeNamedCache(env);
			}
		} catch (Exception e) {
			PluginException agentex = new PluginException(
					AdaptinetException.SEVERITY_FATAL,
					PluginException.ANT_OBJDOTRANS);
			agentex.logMessage("Method not supported by Cache Plugin. "
							+ e.getMessage());
			throw e;
		}
		return null;
	}

	public void put(Envelope env) {

		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 3)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			cache.putNoEvent((String)args[1], (String)args[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void remove(Envelope env) {

		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 2)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			cache.removeNoEvent((String)args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setLocked(Envelope env) {
		
		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 2)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			cache.setLockedNoEvent(((Boolean)args[1]).booleanValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public void getNamedCache(Envelope env) {
		
		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			if(!cache.isEmpty()) {
				Address sender = env.getReplyTo();
				Message message = new Message("http://" + sender.getHost() + ":"
						+ sender.getPort() + "/cache/" + PUT);
				for(Map.Entry entry : cache.entrySet()) {
					Messenger.postMessage(message, entry.getKey(), entry.getValue());
				}
			}
			cache.setLockedNoEvent(((Boolean)args[1]).booleanValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cacheCheck(Envelope env) {
		
		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			Cache cache = getNamedCache(args[0]);
			
			if(cache==null) {
				Address sender = env.getReplyTo();
				Message message = new Message("http://" + sender.getHost() + ":"
						+ sender.getPort() + "/cache/" + GETNAMEDCACHE);
				Messenger.postMessage(message, args[0]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void removeNamedCache(Envelope env) {
		
		Body body = env.getBody();
		try {
			Object args[] = body.getcontentArray();
			if (args.length != 1)
				throw new Exception("Out of Bounds");
			namedCaches.remove(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String process(String xml) throws Exception {

		PluginException agentex = new PluginException(
				AdaptinetException.SEVERITY_FATAL,
				PluginException.ANT_OBJDOTRANS);
		agentex.logMessage(agentex);
		throw agentex;
	}

	static public void doCacheCheck(Address address) {

		CacheWorker worker = new CacheWorker(address);
		workers.put(address.getURL(), worker);
	}

	static public Iterator<CacheWorker> iterator() {
		return workers.values().iterator();
	}

	static public void remove(String url) {
		workers.remove(url);
	}

	static public void clear() {
		workers.clear();
	}

	@Override
	public void putValue(String name, String key, String value) {
		
		CacheWorker worker = null;
		Iterator<CacheWorker> it = CachePlugin.iterator();
		while (it.hasNext()) {
			worker = it.next();
			if (!worker.getResponded()) {
				try {
					worker.put(name, key, value);
				} catch (Exception exx) {
					exx.printStackTrace();
				}
			}
		}
	}

	@Override
	public void removeValue(String name, String key) {
		
		CacheWorker worker = null;
		Iterator<CacheWorker> it = CachePlugin.iterator();
		while (it.hasNext()) {
			worker = it.next();
			if (!worker.getResponded()) {
				try {
					worker.remove(name, key);
				} catch (Exception exx) {
					exx.printStackTrace();
				}
			}
		}
	}

	@Override
	public void cacheLocked(String name, boolean locked) {
		
		CacheWorker worker = null;
		Iterator<CacheWorker> it = CachePlugin.iterator();
		while (it.hasNext()) {
			worker = it.next();
			if (!worker.getResponded()) {
				try {
					worker.setLocked(name, locked);
				} catch (Exception exx) {
					exx.printStackTrace();
				}
			}
		}
	}
}
