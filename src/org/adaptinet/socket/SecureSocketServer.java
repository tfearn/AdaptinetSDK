/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.socket;

import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiver.TransceiverConfig;



public class SecureSocketServer extends HttpSocketServer {

	protected ServerSocket createServerSocket() {

		try {
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

			SSLContext ctx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JKS");

			String keyFile = ITransceiver.getTransceiver().getConfigFromFile()
					.getProperty(TransceiverConfig.KEY_STORE,
							"XMLAgentKeyStore");
			if (keyFile == null)
				throw new Exception(
						"KeyStore for SSL is not in the configuration of XML-Agent.");

			File fileKeyStore = new File(keyFile);
			if (!fileKeyStore.exists())
				throw new Exception("KeyStore "
						+ fileKeyStore.getAbsolutePath() + " does not exist.");

			char[] passphrase = (ITransceiver.getTransceiver()
					.getConfigFromFile().getProperty(
					TransceiverConfig.KEY_STORE_PASSPHRASE, "seamaster"))
					.toCharArray();
			ks.load(new FileInputStream(fileKeyStore), passphrase);
			kmf.init(ks, passphrase);

			System.out.print("Starting SSL Listener...");
			ctx.init(kmf.getKeyManagers(), null, null);
			System.out.println("done.");

			SSLServerSocketFactory ssf = ctx.getServerSocketFactory();
			ssf.createServerSocket(port, Math.max(128, sockets));
		} catch (Exception e) {
		}
		return null;
	}
}
