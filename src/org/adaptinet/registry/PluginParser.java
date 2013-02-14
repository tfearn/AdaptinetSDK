/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.registry;

import java.util.HashMap;

import org.adaptinet.parser.Attributes;
import org.adaptinet.parser.DefaultHandler;


final public class PluginParser extends DefaultHandler {
	private static final int NONE = 0;

	private static final int PLUGINENTRY = 1;

	private static final int CLASSPATH = 2;

	private static final int NAME = 3;

	private static final int TYPE = 4;

	private static final int DESCRIPTION = 5;

	private static final int PRELOAD = 6;

	private PluginEntry pluginEntry = null;

	private HashMap<String, PluginEntry> entries = new HashMap<String, PluginEntry>();

	private int state = 0;

	public PluginParser() {
	}

	public void startElement(String uri, String tag, String qtag,
			Attributes attrs) {
		if (tag.equals("Plugin")) {
			state = PLUGINENTRY;
			pluginEntry = new PluginEntry();
		} else if (tag.equals("Classpath")) {
			state = CLASSPATH;
		} else if (tag.equals("Name")) {
			state = NAME;
		} else if (tag.equals("Type")) {
			state = TYPE;
		} else if (tag.equals("Preload")) {
			state = PRELOAD;
		} else if (tag.equals("Description")) {
			state = DESCRIPTION;
		}
	}

	public void characters(char buffer[], int start, int length) {
		switch (state) {
		case CLASSPATH:
			pluginEntry.setClasspath(new String(buffer, start, length));
			break;

		case NAME:
			pluginEntry.setName(new String(buffer, start, length));
			break;

		case DESCRIPTION:
			pluginEntry.setDescription(new String(buffer, start, length));
			break;

		case TYPE:
			pluginEntry.setType(new String(buffer, start, length));
			break;

		case PRELOAD:
			pluginEntry.setPreload(new String(buffer, start, length));
			break;

		default:
			break;
		}
		state = NONE;
	}

	public void endElement(String uri, String name, String qname) {
		if (name.equals("Plugin")) {
			entries.put(pluginEntry.getName(), pluginEntry);
		}

	}

	HashMap<String, PluginEntry> getEntries() {
		return entries;
	}

}