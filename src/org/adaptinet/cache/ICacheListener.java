/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.cache;

public interface ICacheListener {

	public void putValue(String name, String key, String value);
	public void removeValue(String name, String key);
	public void cacheLocked(String name, boolean locked);
}
