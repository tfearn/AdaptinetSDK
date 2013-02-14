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
import org.adaptinet.messaging.Envelope;
import org.adaptinet.registry.PluginEntry;


public abstract class PluginBase {
	
	protected String name = null;

	protected ClasspathLoader loader = null;

	@SuppressWarnings("unused")
	private PluginAgent agent = null;

	public PluginBase() {
	}

	public void init(ClasspathLoader loader, PluginAgent agent) {
		this.agent = agent;
		this.loader = loader;
	}

	public void startPlugin(PluginEntry entry) throws Exception {
		throw new Exception("MethodNotSupported");
	}

	public void cleanupPlugin() {
	}

	public abstract boolean preProcessMessage(Envelope env);

	public abstract Object process(Envelope env) throws Exception;
}
