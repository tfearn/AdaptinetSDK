/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.pluginagent;

import org.adaptinet.messaging.Address;
import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.transceiver.ITransceiver;


public final class MaintenanceWorker extends Worker{

	public MaintenanceWorker(Address address) {
		super(address);
	}

	public void doPing() {
		Message message = null;

		try {
			message = new Message(address.getURL() + "/" + 
					PluginFactory.MAINTENANCE + "/ping",
					ITransceiver.getTransceiver());
			message.getReplyTo().setPlugin(PluginFactory.MAINTENANCE);
			bresponded = false;
			starttime = System.currentTimeMillis();
			Messenger.sendMessage(message);
		} catch (Exception e) {
			starttime = 0;
			endtime = 0;
		}
	}
}
