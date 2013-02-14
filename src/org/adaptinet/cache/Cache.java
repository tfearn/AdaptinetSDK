/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Cache extends HashMap<String, String> {

	static private final long serialVersionUID = -2323837462860727180L;
	private boolean locked = false;
	private String name = null;
	private ICacheListener listener = null;
	
	static public final Cache getNamedCache(String key) {
		return CachePlugin.getNamedCache(key);
	}
	
	public Cache(ICacheListener listener, String name, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		this.name = name;
		this.listener = listener;
	}

	public Cache(ICacheListener listener, String name, int initialCapacity) {
		super(initialCapacity);
		this.name = name;
		this.listener = listener;
	}

	public Cache(ICacheListener listener, String name) {
		super();
		this.name = name;
		this.listener = listener;
	}

	public Cache(ICacheListener listener, String name, Map<? extends String, ? extends String> m) {
		super(m);
		this.name = name;
		this.listener = listener;
	}

	public String put(String key, String value) {
		String ret = super.put(key, value);
		listener.putValue(name, key, value);
		return ret;
	}

	public String putNoEvent(String key, String value) {
		String ret = super.put(key, value);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void putAll(Map<? extends String, ? extends String> m) {
		super.putAll(m);
		Iterator<Map.Entry<? extends String, ? extends String>> it = (Iterator<Map.Entry<? extends String, ? extends String>>) m.entrySet();
		while(it.hasNext()) {
			Map.Entry<? extends String, ? extends String> entry = it.next();
			listener.putValue(name, entry.getKey(), entry.getValue());
		}
	}

	public String remove(String key) {
		String ret = super.remove(key);
		listener.removeValue(name, key);
		return ret;
	}

	public String removeNoEvent(String key) {
		String ret = super.remove(key);
		return ret;
	}

	public final void setLocked(boolean locked) {
		this.locked = locked;
		listener.cacheLocked(name, locked);
	}

	public final void setLockedNoEvent(boolean locked) {
		this.locked = locked;
	}

	public final boolean isLocked() {
		return locked;
	}

	public final String getName() {
		return name;
	}
}
