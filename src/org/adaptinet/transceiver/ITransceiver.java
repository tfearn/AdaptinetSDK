/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiver;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.socket.PropData;


public abstract class ITransceiver {

	static private ITransceiver transceiver = null;

	static String key = null;

	public ITransceiver() throws Exception {
		if (transceiver == null) {

			synchronized (ITransceiver.class) {
				if (transceiver == null) {
					transceiver = this;
				} else {
					throw new Exception(
							"Can not instantiate second instance of ITransceiver");
				}
			}
		} else {
			throw new Exception(
					"Can not instantiate second instance of ITransceiver");
		}
	}

	static final public ITransceiver getTransceiver() {
		return transceiver;
	}

	static final public Object getNamedService(String name) {
		return transceiver.getService(name);
	}

	static public String getKey() {
		return key;
	}

	static protected void setKey() {
		try {
			key = Integer.toString((transceiver.getHost() + Integer
					.toString(transceiver.getPort())).hashCode());
		} catch (Exception e) {
		}
	}

	static public String getCertificate() {
		return null;
	}

	static public Object getPlugin(String name) {
		return transceiver.getAvailablePlugin(name);
	}

	public static File findFile(String name) {
		String classpath = System.getProperty("java.class.path");
		File file = null;
		try {
			StringTokenizer classToken = new StringTokenizer(classpath,
					File.pathSeparator);
			while (classToken.hasMoreTokens()) {
				String filename = classToken.nextToken();
				String filenameLower = filename.toLowerCase();
				if (filenameLower.endsWith(".zip")
						|| filenameLower.endsWith(".jar")) {
					try {
						JarFile jarFile = new JarFile(filename);
						JarEntry j = jarFile.getJarEntry(name);
						if (j != null) {
							file = new File(filename);
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (!filename.endsWith(File.separator)) {
						filename += File.separatorChar;
					}

					File f = new File(filename + name);
					if (f.exists()) {
						file = f;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return file;
	}

	public abstract void initialize(Properties properties)
			throws AdaptinetException;

	public abstract void initialize(String configFile)
			throws AdaptinetException;

	public abstract void initialize(String[] args) throws AdaptinetException;

	public abstract void start() throws AdaptinetException;

	public abstract URL getURL() throws AdaptinetException;

	public abstract int getLocalPort();

	public abstract String getIdentifier();

	public abstract InetAddress getInetAddress();

	public abstract void shutdown() throws AdaptinetException;

	public abstract void restart() throws AdaptinetException;

	public abstract boolean killRequest(short key, boolean bForce);

	public abstract boolean killRequest(String key, boolean bForce);

	public abstract Vector<PropData> requestList() throws AdaptinetException;

	public abstract Object getAvailablePlugin(String name);

	public abstract Object getAvailableHandler(String name);

	public abstract Object getAvailableServlet();

	public abstract Object getAvailableServlet(String name);

	public abstract void run(Runnable runner);

	public abstract void setProperty(String name, String property);

	public abstract String getProperty(String name);

	public abstract int getConnectionTimeOut();

	public abstract void setConnectionTimeOut(int newValue);

	public abstract String getHost();

	public abstract void setHost(String host);

	public abstract int getPort();

	public abstract void setPort(int nPort);

	public abstract int getAdminPort();

	public abstract void setAdminPort(int nPort);

	public abstract int getSecurePort();

	public abstract void setSecurePort(int nSecurePort);

	public abstract Object getRegistryFile();

	public abstract Object getRegistryDirectory();

	public abstract boolean getVerboseFlag();

	public abstract void setVerboseFlag(boolean verbose);

	public abstract void setAutoConnectFlag(boolean connect);

	public abstract Thread getThread();

	public abstract int getClientThreadPriority();

	public abstract String getHTTPRoot();

	public abstract void setHTTPRoot(String httpRoot);

	public abstract String getXSLPath();

	public abstract String getWebRoot();

	public abstract String getClasspath();

	public abstract void setClasspath(String classpath);

	public abstract int getMaxClients();

	public abstract void setMaxClients(int nMaxClients);

	public abstract int getMaxConnections();

	public abstract void setMaxConnections(int nMaxConnections);

	public abstract String getLogPath();

	public abstract void setLogPath(String path);

	public abstract String getSocketType();

	public abstract void setSocketType(String type);

	public abstract String getSocketServerClass();

	public abstract void setSocketServerClass(String type);

	public abstract Properties getConfig();

	public abstract Properties getConfigFromFile();

	public abstract void saveConfig();

	public abstract void saveConfig(Properties properties);

	public abstract String getServletClasspath();

	public abstract String getSMTPHost();

	public abstract Object getFaultTolDBMgr();

	public abstract boolean usesFaultTolerance();

	public abstract boolean useProxy();

	public abstract String getProxyAddress();

	public abstract Object getService(String name);

	public abstract Object getSetting(String name);

	public abstract int getMessageCacheSize();

	public abstract void setMessageCacheSize(int nMessageCacheSize);

};
