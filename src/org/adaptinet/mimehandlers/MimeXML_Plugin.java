/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.mimehandlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.messaging.Envelope;
import org.adaptinet.messaging.MessageParser;
import org.adaptinet.messaging.ResponseWriter;
import org.adaptinet.parser.InputSource;
import org.adaptinet.parser.XMLReader;
import org.adaptinet.pluginagent.PluginAgent;
import org.adaptinet.pluginagent.PluginFactory;
import org.adaptinet.transceiver.ITransceiver;


public class MimeXML_Plugin extends MimeXML {

	static private FixedSizeSet<Integer> messageCache = 
			new FixedSizeSet<Integer>(ITransceiver.getTransceiver().getMessageCacheSize());

	private PluginAgent plugin = null;

	public MimeXML_Plugin() {
	}

	public ByteArrayOutputStream process(ITransceiver transceiver, String xml) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Envelope env = null;

		try {
			if (bVerbose == true)
				System.out.println("=============== Incoming XML Transaction ===============\n"
								+ xml);

			XMLReader parser = new XMLReader();
			parser.setContentHandler(new MessageParser());
			env = (Envelope) parser.parse(new InputSource(
					new ByteArrayInputStream(xml.getBytes())));
			
			Integer uid = env.getUID();
			if(messageCache.contains(uid)) {
				return out;
			}
			
			messageCache.add(uid);
			String name = env.getPlugin();
			plugin = (PluginAgent) transceiver.getAvailablePlugin(name);

			if (plugin != null) {
				if (name.equals(PluginFactory.ADAPTINET) == true)
					plugin.preProcess(PluginFactory.ADAPTINETCLASS);
				else if (name.equals(PluginFactory.MAINTENANCE) == true)
					plugin.preProcess(PluginFactory.MAINTENANCECLASS);
				else if (name.equals(PluginFactory.SERVICE) == true)
					plugin.preProcess(PluginFactory.SERVICECLASS);

				AdaptinetException exMessage = new AdaptinetException(
						AdaptinetException.SEVERITY_SUCCESS,
						AdaptinetException.GEN_MESSAGE);
				exMessage.logMessage("Plugin received Name: "
						+ plugin.getName());

				if (env.isSync()) {
					System.out.println("in a sync Message");
					new ResponseWriter(out).writeResponse(plugin.execute(env));
				} else {
					plugin.pushMessage(env);
					transceiver.run(plugin);
					try {
						out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><plugin>"
									+ plugin.getName()
									+ "</plugin><code>0</code><desc>request accepted</desc><timestamp>"
									+ (new java.util.Date(System
											.currentTimeMillis())
											.toString()) + "</timestamp></status>")
									.getBytes());
					} catch (IOException ioe) {
					}
				}
			} else {
				AdaptinetException exMessage = new AdaptinetException(
						AdaptinetException.SEVERITY_ERROR,
						AdaptinetException.GEN_BASE);
				exMessage.logMessage("Unable to load find available plugin: ");
			}

			AdaptinetException exMessage = null;

			/**
			 * We have initiated the transcation with the original peer now we
			 * can check to see if this is a broadcast message.
			 */
			int hops = env.getHopCount();
			if (hops != 0) {
				if (hops > 0) {
					hops--;
					env.getHeader().getMessage()
							.setHops(Integer.toString(hops));
				}
				networkAgent.broadcastMessage(env);
			}

			exMessage = new AdaptinetException(
					AdaptinetException.SEVERITY_SUCCESS,
					AdaptinetException.GEN_MESSAGE);
			exMessage.logMessage("Plugin successfully executed Name: "
					+ plugin.getName());
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Execution failed reason: " + e.getMessage());
			try {
				out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><plugin>unknown</plugin><code>1</code><desc>"
								+ e.getMessage()
								+ "</desc><timestamp>"
								+ (new java.util.Date(System
										.currentTimeMillis()).toString()) + "</timestamp></status>")
								.getBytes());
			} catch (IOException ioe) {
			}
		} finally {
			plugin = null;
		}

		return out;
	}

	public Object getObject() {
		return plugin;
	}
}