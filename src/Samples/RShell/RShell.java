/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.RShell;

import java.io.InputStream;

import org.adaptinet.messaging.Message;
import org.adaptinet.pluginagent.Plugin;


public class RShell extends Plugin {
	private Runtime rt = null;

	private RShellPanel consolePanel = null;

	public RShell() {
	}

	public void init() {

		rt = Runtime.getRuntime();
		consolePanel = new RShellPanel();
		consolePanel.setPlugin(this);
		consolePanel.init(transceiver.getHost() + ":"
				+ Integer.toString(transceiver.getPort()));
	}

	public void cleanup() {
	}

	public void pong() {

		try {
			String text = null;
			long pingtime = System.currentTimeMillis()
					- Long.parseLong(msg.getTimeStamp());
			text = "Ping from : " + msg.getAddress().getURL()
					+ " time in millis: " + Long.toString(pingtime);
			consolePanel.setCommandText(text);
		} catch (Exception e) {
		}
	}

	public void doCommand(String commandTxt) {
		try {
			int size = 0;
			boolean bContinue = true;

			Process command = rt.exec(commandTxt);
			InputStream es = command.getErrorStream();

			consolePanel.setCommandText(commandTxt);
			Message message = new Message(msg.getReplyTo());
			message.setReplyTo(msg.getAddress());
			message.setPlugin("RShell");
			message.setMethod("doResponse");

			byte byteArray[] = new byte[1024];
			while (bContinue) {
				size = es.read(byteArray);
				if (size > 0) {
					postMessage(message, new String(byteArray, 0, size, "UTF-8"));
				} else {
					try {
						command.exitValue();
						bContinue = false;
					} catch (Exception exx) {
						Thread.sleep(100);
						continue;
					}
				}
			}
		} catch (Exception e) {
		}
	}

}
