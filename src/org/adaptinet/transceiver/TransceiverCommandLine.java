/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiver;

import java.io.PrintStream;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.adaptinetex.TransceiverException;


public class TransceiverCommandLine {
	static public String findConfigFile(String[] args) {
		if (args == null) {
			return null;
		}

		for (int ii = 0; ii < args.length; ii++) {
			if (args[ii].equalsIgnoreCase("-config")) {
				return args[++ii];
			}
		}
		return null;
	}

	static public boolean processCommandLine(String[] args,
			ITransceiver transceiver) {
		if (args == null) {
			return false;
		}

		// Re-process the command line normally for any overrides.
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-h")
					|| args[i].equalsIgnoreCase("-help")
					|| args[i].equalsIgnoreCase("-?")) {
				usage();
			} else if (args[i].equalsIgnoreCase("-httproot")) {
				transceiver.setHTTPRoot(args[++i]);
			} else if (args[i].equalsIgnoreCase("-classpath")) {
				transceiver.setClasspath(args[++i]);
			} else if (args[i].equalsIgnoreCase("-logpath")) {
				transceiver.setLogPath(args[++i]);
			} else if (args[i].equalsIgnoreCase("-type")) {
				transceiver.setSocketType(args[++i]);
			} else if (args[i].equalsIgnoreCase("-socketserverclass")) {
				transceiver.setSocketServerClass(args[++i]);
			} else if (args[i].equalsIgnoreCase("-registry")) {
				// transceiver.set(args[++i]);
			} else if (args[i].equalsIgnoreCase("-port")) {
				try {
					transceiver.setPort(new Integer(args[++i]).intValue());
				} catch (NumberFormatException ex) {
					TransceiverException serverex = new TransceiverException(
							AdaptinetException.SEVERITY_FATAL,
							TransceiverException.TCV_INVALIDPORT);
					serverex
							.logMessage("invalid port number [" + args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-adminport")) {
				try {
					transceiver.setAdminPort(new Integer(args[++i]).intValue());
				} catch (NumberFormatException ex) {
					TransceiverException serverex = new TransceiverException(
							AdaptinetException.SEVERITY_FATAL,
							TransceiverException.TCV_INVALIDPORT);
					serverex
							.logMessage("invalid port number [" + args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-secureport")) {
				try {
					transceiver
							.setSecurePort(new Integer(args[++i]).intValue());
				} catch (NumberFormatException ex) {
					TransceiverException serverex = new TransceiverException(
							AdaptinetException.SEVERITY_FATAL,
							TransceiverException.TCV_INVALIDPORT);
					serverex
							.logMessage("invalid port number [" + args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-messagecachesize")) {
				try {
					int cacheSize = new Integer(args[++i]).intValue();
					if(cacheSize>500) { 
						transceiver.setMessageCacheSize(cacheSize);
					}
					else {
						TransceiverException serverex = new TransceiverException(
								AdaptinetException.SEVERITY_FATAL,
								TransceiverException.TCV_INVALIDMESSAGECACHESIZE);
						serverex
								.logMessage("invalid message cache size to small [" + 
												args[i] + "] defaulting to 5000");
					}
				} catch (NumberFormatException ex) {
					TransceiverException serverex = new TransceiverException(
							AdaptinetException.SEVERITY_FATAL,
							TransceiverException.TCV_INVALIDMESSAGECACHESIZE);
					serverex
							.logMessage("invalid message cache size [" + args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-maxclients")
					&& (i + 1 < args.length)) {
				try {
					transceiver
							.setMaxClients(new Integer(args[++i]).intValue());
				} catch (NumberFormatException ex) {
					TransceiverException serverex = new TransceiverException(
							AdaptinetException.SEVERITY_FATAL,
							TransceiverException.TCV_INVALIDMAXCLIENTS);
					System.out.println("invalid number for the max clients ["
							+ args[i] + "]");
					serverex.logMessage("invalid number for the max clients ["
							+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-connecttimeout")
					&& (i + 1 < args.length)) {
				try {
					transceiver.setConnectionTimeOut(new Integer(args[++i])
							.intValue());
				} catch (NumberFormatException ex) {
					TransceiverException serverex = new TransceiverException(
							AdaptinetException.SEVERITY_FATAL,
							TransceiverException.TCV_INVALIDMAXCLIENTS);
					System.out
							.println("invalid number for the connection timeout ["
									+ args[i] + "]");
					serverex
							.logMessage("invalid number for the connection timeout ["
									+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-verbose")) {
				try {
					transceiver.setVerboseFlag(true);
				} catch (NumberFormatException ex) {
					TransceiverException serverex = new TransceiverException(
							AdaptinetException.SEVERITY_FATAL,
							TransceiverException.TCV_INVALIDMAXCLIENTS);
					System.out
							.println("invalid boolean format for verbose mode ["
									+ args[i] + "]");
					serverex
							.logMessage("invalid boolean format for verbose mode ["
									+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-autoconnect")) {
				try {
					transceiver.setAutoConnectFlag(true);
				} catch (NumberFormatException ex) {
					TransceiverException serverex = new TransceiverException(
							AdaptinetException.SEVERITY_FATAL,
							TransceiverException.TCV_INVALIDMAXCLIENTS);
					System.out
							.println("invalid boolean format for verbose mode ["
									+ args[i] + "]");
					serverex
							.logMessage("invalid boolean format for verbose mode ["
									+ args[i] + "]");
				}
			} else if (args[i].equalsIgnoreCase("-host")
					&& (i + 1 < args.length)) {
				transceiver.setHost(args[++i]);
			} else {
				continue;
			}
		}

		return true;
	}

	static final public void usage() {
		PrintStream o = System.out;
		o.println("usage: Transceiver [OPTIONS]");
		o.println("-socketType <type>         : Type of plugin handlers(Plugin|COMPlugin.");
		o.println("-socketServerClass <type>  : Java class for custom socket server.");
		o.println("-httpcfg <http file>       : Name including path of the http configuration.");
		o.println("-registry <registry file>  : Name including path of the registry file.");
		o.println("-connecttimeout <number>   : Timeout for connection.");
		o.println("-id <identifier>           : Name used to identify transceiver.");
		o.println("-port <number>             : Listen on the given port number.");
		o.println("-adminport <number>        : Listen on the given port number for admin requests.");
		o.println("-host <host>               : Full name of host running the transceiver.");
		o.println("-maxclients <number>       : Maximum number of clients.");
		o.println("-maxconnections <number>   : Maximum number of connections.");
		o.println("-registry <registry file>  : Name including path of registry file.");
		o.println("-verbose                   : Turn on verbose output.");
		o.println("-classpath <classpath>     : Additional CLASSPATH");
		o.println("-messagecachesize <number> : Size of the message cache to prevent echoing.");
		System.exit(1);
	}
}
