/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.socket;

import java.io.DataOutputStream;
import java.io.InputStream;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiverutils.CachedThread;



public class CallbackSocket extends ClientSocket {

	private CallbackSocketListener listener = null;

	public CallbackSocket(CallbackSocketListener listener, String type,
			ITransceiver transceiver, BaseSocketServer baseSocketServer,
			CachedThread thread) {
		super(type, transceiver, baseSocketServer, thread);
		this.listener = listener;
	}

	synchronized protected boolean startConnection(InputStream in,
			DataOutputStream out) {

		input = in;
		output = out;

		try {
			StringBuffer b = new StringBuffer();
			while (in.available() > 0) {
				b.append(in.read());
			}

			listener.OnReceive(b.toString());
			input.close();
			input = null;

			output.flush();
			output.close();
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Start connection failed");
		}
		return interrupted;
	}
}
