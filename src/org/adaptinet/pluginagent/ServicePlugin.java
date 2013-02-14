/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.pluginagent;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.adaptinetex.PluginException;
import org.adaptinet.messaging.Envelope;
import org.adaptinet.registry.PluginEntry;
import org.adaptinet.registry.PluginFile;
import org.adaptinet.transceiver.ITransceiver;


class ServicePlugin extends PluginBase {

	protected PluginMap pluginMap = null;

	final public Object process(Envelope env) throws Exception {
		
		Object ret = null;
		try {
			if (pluginMap == null) {
				startPlugin(((PluginFile) ITransceiver.getTransceiver()
						.getService("pluginfile")).findEntry(env.getHeader()
						.getMessage().getAddress().getPlugin()));
			}
			pluginMap.setCurrentMessage(env.getHeader().getMessage());
			Object args[] = env.getBody().getcontentArray();
			String method = env.getHeader().getMessage().getAddress()
					.getMethod();
			ret = pluginMap.executeMethod(method, args, true);
		} catch (Exception e) {
			PluginException agentex = new PluginException(
					AdaptinetException.SEVERITY_FATAL,
					PluginException.ANT_OBJDOTRANS, e.getMessage());
			throw agentex;
		}
		return ret;
	}

	public final boolean preProcessMessage(Envelope env) {
		boolean bRet = false;
		try {
			if (pluginMap == null)
				bRet = true;
			else
				bRet = pluginMap.preProcessMessage(env);
		} catch (Exception ex) {
		}
		return bRet;
	}

	final public String process(String xml) throws Exception {
		PluginException agentex = new PluginException(
				AdaptinetException.SEVERITY_FATAL,
				PluginException.ANT_OBJDOTRANS);
		agentex.logMessage(agentex);
		throw agentex;
	}

	public final void startPlugin(PluginEntry entry) throws Exception {
		try {
			if (pluginMap == null) {
				createPluginMap();
				pluginMap.createInstance(entry.getType(), loader);
				pluginMap.executeMethod("init", null, true);
			}
		} catch (Exception e) {
			PluginException agentex = new PluginException(
					AdaptinetException.SEVERITY_FATAL,
					PluginException.ANT_OBJDOTRANS);
			agentex
					.logMessage("End document Exception error performing do transaction. "
							+ e.getMessage());
			throw e;
		}
	}

	public final void cleanupPlugin() {
		try {
			if (pluginMap != null) {
				pluginMap.executeMethod("cleanup", null, true);
			}
		} catch (Exception e) {
			PluginException agentex = new PluginException(
					AdaptinetException.SEVERITY_FATAL,
					PluginException.ANT_OBJDOTRANS);
			agentex
					.logMessage("End document Exception error performing do transaction. "
							+ e.getMessage());
		}
	}

	void createPluginMap() {
		pluginMap = new PluginMap();
	}
}
