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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.adaptinetex.TransceiverException;
import org.adaptinet.mimehandlers.MimePop_Plugin;
import org.adaptinet.pluginagent.PluginAgent;
import org.adaptinet.pluginagent.PluginFactory;
import org.adaptinet.pluginagent.PluginState;
import org.adaptinet.registry.PluginFile;
import org.adaptinet.socket.BaseSocketServer;
import org.adaptinet.socket.PropData;


public class Transceiver extends ITransceiver {

	static public final String TRUE = "true";
	static public final String FALSE = "false";
	
	// Configuration defines
	static protected final String PROP_ROOT_DIR = "root";
	static protected final boolean REDIRECT_OUTPUT_STREAMS = false;

	protected int clientPriority = Thread.NORM_PRIORITY;
	protected boolean bUseProxy = false;
	protected BaseSocketServer socketServer = null;
	protected BaseSocketServer adminSocketServer = null;
	protected BaseSocketServer secureSocketServer = null;
	protected PluginFactory pluginFactory = null;
	protected boolean bRestarting = false;
	protected boolean bFinishing = false;
	protected int nPort = 8082;
	protected int nAdminPort = 0;
	protected int nSecurePort = 0;
	protected String host = "localhost";
	protected String classpath = null;
	protected String identifier = "Transceiver";
	protected URL url = null;
	protected int nMaxClients = 0;
	protected int maxpeers = 10;
	protected int peerlevels = 4;
	protected int nMaxConnections = 0;
	protected boolean verbose = false;
	protected boolean autoconnect = false;
	protected boolean showconsole = false;
	protected PluginFile pluginFile = null;
	protected NetworkAgent networkAgent = null;
	protected int timeout = 0;
	protected String fileName = null;
	protected String peerfilename = null;
	protected String connectType = null;
	protected String pluginfile = null;
	protected String httpRoot = null;
	protected String webRoot = null;
	protected String SMTPHost = null;
	protected String proxyAddress = null;
	protected String keyStore = null;
	protected String keyStorePass = null;
	protected String socketType = null;
	protected String socketServerClass = null;
	protected int messageCacheSize = 5000;

	protected TransceiverCommandLine transceiverCmdLine = null;
	protected Thread shutdownHookThread;
	protected MimePop_Plugin pop = null;

	public Transceiver() throws Exception {
		super();
	}

	public void initialize(String[] args) throws AdaptinetException {

		try {
			startSequence();
			loadSettings(args);
			start();
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			TransceiverException transceiverex = new TransceiverException(
					AdaptinetException.SEVERITY_FATAL,
					TransceiverException.TCV_INITFAILDED);
			transceiverex.logMessage(e);
			throw transceiverex;
		}
	}

	public void initialize(String configFile) throws AdaptinetException {

		try {
			startSequence();
			fileName = configFile;
			if (fileName != null) {
				loadConfig();
			}
			start();
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			TransceiverException transceiverex = new TransceiverException(
					AdaptinetException.SEVERITY_FATAL,
					TransceiverException.TCV_INITFAILDED);
			transceiverex.logMessage(e);
			throw transceiverex;
		}
	}

	public void initialize(Properties properties) throws AdaptinetException {

		try {
			startSequence();
			loadConfig(properties);
			start();
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			TransceiverException transceiverex = new TransceiverException(
					AdaptinetException.SEVERITY_FATAL,
					TransceiverException.TCV_INITFAILDED);
			transceiverex.logMessage(e);
			throw transceiverex;
		}
	}

	public void startSequence() throws Exception {

		int start = 0;
		try {
			InetAddress iaddr = InetAddress.getLocalHost();
			host = iaddr.toString();
			if ((start = host.indexOf('/')) > -1) {
				host = host.substring(start + 1);
			}
			identifier = iaddr.getHostName();
		} catch (Exception e) {
			try {
				identifier = "localhost";
				host = InetAddress.getByName("localhost").toString();
				if ((start = host.indexOf('/')) > -1) {
					host = host.substring(start + 1);
				}
			} catch (Exception ee) {
				throw ee;
			}
		}
	}

	public void loadSettings(String[] args) throws AdaptinetException {
		
		fileName = TransceiverCommandLine.findConfigFile(args);
		if (fileName != null) {
			loadConfig();
		}
		TransceiverCommandLine.processCommandLine(args, this);
	}

	public void setProperty(String name, String property) {
		TransceiverInfo.properties.setProperty(name, property);
	}

	public String getProperty(String name) {
		return TransceiverInfo.properties.getProperty(name);
	}

	public void start() throws AdaptinetException {

		try {
			ITransceiver.setKey();
			shutdownHookThread = new Thread() {
				public void run() {
					try {
						shutdown();
					} catch (Exception e) {
						if (TransceiverInfo.bVerbose) {
							e.printStackTrace();
						}
					}
				}
			};

			Runtime.getRuntime().addShutdownHook(shutdownHookThread);

			// Create the network agent and do all of the initialization
			networkAgent = new NetworkAgent(this, peerfilename, autoconnect,
					connectType, maxpeers, peerlevels);

			initializeSocketServer();
			intializeFactory();
			pluginFile.preload();
			pluginFile.startPlugins();
			networkAgent.start();
			pop = MimePop_Plugin.startMailReader();
			
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			TransceiverException transceiverex = new TransceiverException(
					AdaptinetException.SEVERITY_FATAL,
					TransceiverException.TCV_INITFAILDED);
			transceiverex.logMessage(e);
			throw transceiverex;
		}
	}

	protected void initializeSocketServer() throws AdaptinetException {

		try {
			socketServer = BaseSocketServer.createInstance(socketType,
					socketServerClass);
			socketServer.initialize(this, nPort, nMaxConnections);

			AdaptinetException transceiverex = new AdaptinetException(
					AdaptinetException.SEVERITY_SUCCESS,
					AdaptinetException.GEN_MESSAGE);
			transceiverex.logMessage(TransceiverInfo.VERSION + " starting.");

			socketServer.start(identifier);

			if (nAdminPort > 0) {
				adminSocketServer = BaseSocketServer.createInstance("HTTP");
				adminSocketServer.initialize(this, nAdminPort, nMaxConnections);
				adminSocketServer.start(identifier + "admin");
			}

			if (nSecurePort > 0) {
				secureSocketServer = BaseSocketServer.createInstance("TLS"
						+ socketType);
				secureSocketServer.initialize(this, nSecurePort,
						nMaxConnections);
				secureSocketServer.start(identifier + "SSL");
			}
		} catch (Exception e) {
			TransceiverException transceiverex = new TransceiverException(
					AdaptinetException.SEVERITY_FATAL,
					TransceiverException.TCV_INITFAILDED);
			transceiverex.logMessage(e);
			throw transceiverex;
		}

	}

	public void intializeFactory() throws AdaptinetException {

		try {
			if (pluginfile != null) {
				pluginFile = new PluginFile(pluginfile);
			} else {
				pluginFile = new PluginFile();
			}

			pluginFactory = new PluginFactory(classpath, verbose);
			pluginFactory.initialize(this, nMaxClients);
		} catch (Exception e) {
			TransceiverException transceiverex = new TransceiverException(
					AdaptinetException.SEVERITY_FATAL,
					TransceiverException.TCV_INITFAILDED);
			transceiverex.logMessage(e);
			throw transceiverex;
		}
	}

	public boolean killRequest(String id, boolean force) {
		return pluginFactory.killPlugin(id, force);
	}

	public boolean killRequest(short id, boolean force) {
		return false;
	}

	public Vector<PropData> requestList() throws AdaptinetException {

		Map<String, PluginState> clientList = pluginFactory.getPluginAgents();
		Vector<PropData> vec = new Vector<PropData>(32);

		if (clientList != null) {
			String[] stat_arr = { "Idle", "Busy", "Free", "Killed", "Finished" };
			Iterator<PluginState> it = clientList.values().iterator();

			try {
				while (it.hasNext()) {
					PluginState state = it.next();
					PluginAgent plugin = state.getPluginAgent();

					String pluginName = plugin.getName();
					if (pluginName == null) {
						pluginName = new String("no plugin");
					}

					PropData props = new PropData(pluginName, Integer
							.toString(state.getId()), stat_arr[state
							.getStatus()]);

					vec.add(props);
				}
			} catch (Exception e) {
				TransceiverException transceiverex = new TransceiverException(
						AdaptinetException.SEVERITY_FATAL,
						TransceiverException.TCV_INITFAILDED);
				transceiverex.logMessage(e);
				throw transceiverex;
			}
		}

		return vec;
	}

	public URL getURL() throws AdaptinetException {

		if (url == null) {
			try {
				if (nPort != 80) {
					url = new URL("http", host, nPort, "/");
				} else {
					url = new URL("http", host, "/");
				}
			} catch (Exception e) {
				TransceiverException transceiverex = new TransceiverException(
						AdaptinetException.SEVERITY_FATAL,
						TransceiverException.TCV_URLFAILDED);
				transceiverex.logMessage(e);
				throw transceiverex;
			}
		}
		return url;
	}

	public synchronized void restart() throws AdaptinetException {

		try {
			shutdown(true);
		} catch (AdaptinetException e) {
			throw e;
		}
	}

	public synchronized void shutdown() throws AdaptinetException {

		try {
			shutdown(false);
		} catch (AdaptinetException e) {
			throw e;
		}
	}

	public synchronized void shutdown(boolean restart)
			throws AdaptinetException {

		/**
		 * Let the network drop out.
		 */
		try {
			networkAgent.disconnect();
		} catch (Exception e) {
		}

		boolean bAbruptShutdown = shutdownHookThread != null
				&& shutdownHookThread.isAlive();
		try {
			if (pop != null) {
				pop.stop();
			}
		} catch (Exception e) {
		}

		if (!bAbruptShutdown) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
			} catch (Exception e) {
			}
		} else {
			TransceiverException se = new TransceiverException(
					TransceiverException.SEVERITY_WARNING,
					TransceiverException.TCV_ABRUPTSHUTDOWN);
			se.logMessage("It is recommended that the Transceiver be shut down from Adaptinet Administration pages. "
							+ "Failure to do so could result in the loss of system resources.");
		}

		bFinishing = true;
		bRestarting = restart;

		try {
			socketServer.shutdown();
			if (adminSocketServer != null) {
				adminSocketServer.shutdown();
			}
			if (secureSocketServer != null) {
				secureSocketServer.shutdown();
			}

			pluginFile.closeFile();
			networkAgent.closeFile();

			try {
				socketServer.join(10000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (!restart) {
				// If shutdown occurred abruptly, calling exit will cause a
				// deadlock
				if (!bAbruptShutdown) {
					System.exit(0);
				}
			}
		} catch (Exception e) {
			TransceiverException transceiverex = new TransceiverException(
					AdaptinetException.SEVERITY_FATAL, 999);
			e.printStackTrace();
			throw transceiverex;
		}
	}

	protected void cleanup(boolean restart) throws AdaptinetException {

		try {
			if (socketServer != null) {
				socketServer.shutdown();
			}
			socketServer = null;

			if (adminSocketServer != null) {
				adminSocketServer.shutdown();
			}
			adminSocketServer = null;

			if (secureSocketServer != null) {
				secureSocketServer.shutdown();
			}
			secureSocketServer = null;

			bRestarting = false;
			bFinishing = false;
		} catch (Exception e) {
			System.out.println(e.getMessage() + " In cleanup");
		}

		if (restart) {
			try {
				loadConfig();
				start();
			} catch (AdaptinetException e) {
				throw e;
			} catch (Exception e) {
				TransceiverException transceiverex = new TransceiverException(
						AdaptinetException.SEVERITY_FATAL, 999);
				transceiverex.logMessage(e);
				throw transceiverex;
			}
		}
	}

	public void run(Runnable runner) {
		try {
			if (runner instanceof PluginAgent) {
				pluginFactory.run((PluginAgent) runner);
			}
		} catch (ClassCastException cce) { // highly unlikely...
			TransceiverException xse = new TransceiverException(
					AdaptinetException.SEVERITY_ERROR,
					AdaptinetException.GEN_TYPEMISMATCH, cce.getMessage());
			xse.logMessage();
		}
	}

	public void saveConfig() {
		try {
			Properties serverProps = getConfig();
			serverProps.store(new java.io.FileOutputStream(fileName), fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveConfig(Properties serverProps) {
		try {
			serverProps.store(new java.io.FileOutputStream(fileName), fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void loadConfig() {

		try {
			TransceiverInfo.properties = TransceiverProperties
					.getInstance("org.adaptinet.transceiver.SimpleProperties");
			TransceiverInfo.properties.load(new java.io.FileInputStream(
					fileName));
			loadConfig(TransceiverInfo.properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void loadConfig(Properties properties) {

		try {
			String s = null;
			socketType = properties.getProperty(TransceiverConfig.TYPE,
					"Plugin");
			if (socketType.equals("CUSTOM")) {
				socketServerClass = properties
						.getProperty(TransceiverConfig.SOCKETSERVERCLASS);
			} else {
				socketServerClass = null;
			}
			nMaxConnections = Integer.parseInt(properties.getProperty(
					TransceiverConfig.MAX_CONNECTIONS, "30"));
			nPort = Integer.parseInt(properties.getProperty(
					TransceiverConfig.PORT, "0"));
			nAdminPort = Integer.parseInt(properties.getProperty(
					TransceiverConfig.ADMINPORT, "0"));
			maxpeers = Integer.parseInt(properties.getProperty(
					TransceiverConfig.MAXPEERS, "0"));
			peerlevels = Integer.parseInt(properties.getProperty(
					TransceiverConfig.LEVELS, "0"));

			nMaxClients = Integer.parseInt(properties.getProperty(
					TransceiverConfig.MAX_CLIENTS, "30"));
			nSecurePort = Integer.parseInt(properties.getProperty(
					TransceiverConfig.SECUREPORT, "1443"));

			timeout = Integer.parseInt(properties.getProperty(
					TransceiverConfig.CONNECTION_TIMEOUT, "0"));
			timeout *= 1000;

			classpath = properties.getProperty(TransceiverConfig.CLASSPATH);

			s = properties.getProperty(TransceiverConfig.VERBOSE);
			if (s != null && s.equals(TRUE)) {
				verbose = true;
			} else {
				verbose = false;
			}

			s = properties.getProperty(TransceiverConfig.AUTOCONNECT);
			if (s != null && s.equals(TRUE)) {
				autoconnect = true;
			} else {
				autoconnect = false;
			}

			s = properties.getProperty(TransceiverConfig.SHOWCONSOLE, FALSE);
			if (s != null && s.equals(TRUE)) {
				showconsole = true;
			} else {
				showconsole = false;
			}

			s = properties.getProperty(TransceiverConfig.USEPROXY, FALSE);
			if (s != null && s.equals(TRUE)) {
				bUseProxy = true;
			} else {
				bUseProxy = false;
			}

			httpRoot = properties.getProperty(TransceiverConfig.HTTP_ROOT);
			webRoot = properties.getProperty(TransceiverConfig.WEB_ROOT);
			peerfilename = properties.getProperty(TransceiverConfig.PEER_FILE);
			connectType = properties.getProperty(TransceiverConfig.CONNECTTYPE);
			pluginfile = properties.getProperty(TransceiverConfig.PLUGIN_FILE);
			webRoot = properties.getProperty(TransceiverConfig.WEB_ROOT);
			httpRoot = properties.getProperty(TransceiverConfig.HTTP_ROOT, ".");
			SMTPHost = properties.getProperty(TransceiverConfig.SMTPHOST,
					"localhost");
			host = properties.getProperty(TransceiverConfig.HOST, host);
			proxyAddress = properties.getProperty(
					TransceiverConfig.PROXYADDRESS, proxyAddress);

			nSecurePort = Integer.parseInt(properties.getProperty(
					TransceiverConfig.SECUREPORT, "0"));
			if (nSecurePort > 0) {
				keyStore = properties.getProperty(TransceiverConfig.KEY_STORE,
						"KeyStore");
				keyStorePass = properties.getProperty(
						TransceiverConfig.KEY_STORE_PASSPHRASE, "seamaster");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSMTPHost() {
		return SMTPHost;
	}

	public Properties getConfigFromFile() {
		Properties serverProps = new Properties();

		try {
			serverProps.load(new java.io.FileInputStream(fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serverProps;
	}

	public Properties getConfig() {
		TransceiverInfo.properties = TransceiverProperties
				.getInstance("org.adaptinet.transceiver.SimpleProperties");

		try {
			TransceiverInfo.properties.setProperty(TransceiverConfig.HTTP_ROOT,
					httpRoot);
			TransceiverInfo.properties.setProperty(TransceiverConfig.CLASSPATH,
					classpath);
			TransceiverInfo.properties.setProperty(TransceiverConfig.WEB_ROOT,
					webRoot);
			TransceiverInfo.properties.setProperty(TransceiverConfig.SMTPHOST,
					SMTPHost);
			TransceiverInfo.properties.setProperty(
					TransceiverConfig.PROXYADDRESS, proxyAddress);
			TransceiverInfo.properties
					.setProperty(TransceiverConfig.HOST, host);

			TransceiverInfo.properties.setProperty(
					TransceiverConfig.MAX_CLIENTS, Integer
							.toString(nMaxClients));
			TransceiverInfo.properties.setProperty(
					TransceiverConfig.MAX_CONNECTIONS, Integer
							.toString(nMaxConnections));
			TransceiverInfo.properties.setProperty(TransceiverConfig.MAXPEERS,
					Integer.toString(maxpeers));
			TransceiverInfo.properties.setProperty(TransceiverConfig.LEVELS,
					Integer.toString(peerlevels));
			TransceiverInfo.properties.setProperty(TransceiverConfig.PORT,
					Integer.toString(nPort));
			TransceiverInfo.properties.setProperty(TransceiverConfig.TYPE,
					socketType);
			TransceiverInfo.properties.setProperty(
					TransceiverConfig.SOCKETSERVERCLASS, socketServerClass);

			if (timeout > 0) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.CONNECTION_TIMEOUT, Integer
								.toString(timeout / 1000));
			}
			if (nSecurePort > 0) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.SECUREPORT, Integer
								.toString(nSecurePort));
			}
			if (nAdminPort > 0) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.ADMINPORT, Integer
								.toString(nAdminPort));
			}
			if (pluginFile != null) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.PLUGIN_FILE, pluginFile.getName());
			}
			if (networkAgent != null) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.PEER_FILE, networkAgent.getName());
			}
			if (verbose == true) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.VERBOSE, TRUE);
			} else {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.VERBOSE, FALSE);
			}
			if (autoconnect == true) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.AUTOCONNECT, TRUE);
			} else {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.AUTOCONNECT, FALSE);
			}
			if (bUseProxy == true) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.USEPROXY, TRUE);
			} else {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.USEPROXY, TRUE);
			}
			if (showconsole == true) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.SHOWCONSOLE, TRUE);
			} else {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.SHOWCONSOLE, FALSE);
			}
			if (nSecurePort > 0) {
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.KEY_STORE, keyStore);
				TransceiverInfo.properties.setProperty(
						TransceiverConfig.KEY_STORE_PASSPHRASE, keyStorePass);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return TransceiverInfo.properties;
	}

	public Object getAvailablePlugin(String name) {
		return pluginFactory.getAvailablePlugin(name);
	}

	public Object getAvailableHandler(String name) {
		return null;
	}

	public Object getAvailableBroker(String name) {
		return null;
	}

	public Object getAvailableServlet() {
		return null;
	}

	public Object getAvailableServlet(String name) {
		return null;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getLocalPort() {
		return socketServer.getLocalPort();
	}

	public int getSecureLocalPort() {
		return secureSocketServer.getLocalPort();
	}

	public int getAdminLocalPort() {
		return adminSocketServer.getLocalPort();
	}

	public InetAddress getInetAddress() {
		return socketServer.getInetAddress();
	}

	public InetAddress getAdminInetAddress() {
		return adminSocketServer.getInetAddress();
	}

	public InetAddress getSecureInetAddress() {
		return secureSocketServer.getInetAddress();
	}

	final public int getConnectionTimeOut() {
		return timeout;
	}

	final public void setConnectionTimeOut(int newValue) {
		timeout = newValue;
	}

	final public String getHost() {
		return host;
	}

	final public String getProxyAddress() {
		return proxyAddress;
	}

	final public void setHost(String host) {
		this.host = host;
	}

	final public int getPort() {
		return nPort;
	}

	final public void setPort(int nPort) {
		this.nPort = nPort;
	}

	final public String getSocketType() {
		return socketType;
	}

	final public void setSocketType(String socketType) {
		this.socketType = socketType;
	}

	final public String getSocketServerClass() {
		return socketServerClass;
	}

	final public void setSocketServerClass(String socketServerClass) {
		this.socketServerClass = socketServerClass;
	}

	final public int getAdminPort() {
		return nAdminPort;
	}

	final public void setAdminPort(int nAdminPort) {
		this.nAdminPort = nAdminPort;
	}

	final public int getSecurePort() {
		return nSecurePort;
	}

	final public int getMessageCacheSize() {
		return messageCacheSize;
	}

	final public void setMessageCacheSize(int nMessageCacheSize) {
		this.messageCacheSize = nMessageCacheSize;
	}
		
	final public void setSecurePort(int nSecurePort) {
		this.nSecurePort = nSecurePort;
	}

	public Object getRegistryFile() {
		return null;
	}

	public Object getRegistryDirectory() {
		return null;
	}

	final public boolean getVerboseFlag() {
		return verbose;
	}

	final public void setVerboseFlag(boolean verbose) {
		this.verbose = verbose;
	}

	final public boolean getAutoConnectFlag() {
		return autoconnect;
	}

	final public void setAutoConnectFlag(boolean autoconnect) {
		this.autoconnect = autoconnect;
	}

	final public Thread getThread() {
		return null; // thread;
	}

	final public int getClientThreadPriority() {
		return clientPriority;
	}

	final public String getHTTPRoot() {
		if (httpRoot != null && !httpRoot.endsWith(File.separator)) {
			httpRoot += File.separator;
		}
		return httpRoot;
	}

	final public void setHTTPRoot(String httpRoot) {
		this.httpRoot = httpRoot;
	}

	final public String getWebRoot() {
		return this.webRoot;
	}

	final public String getClasspath() {
		return classpath;
	}

	public String getServletClasspath() {
		return null;
	}

	final public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	final public int getMaxClients() {
		return nMaxClients;
	}

	final public void setMaxClients(int nMaxClients) {
		this.nMaxClients = nMaxClients;
	}

	final public int getMaxConnections() {
		return nMaxConnections;
	}

	final public void setMaxConnections(int nMaxConnections) {
		this.nMaxConnections = nMaxConnections;
	}

	public String getXSLPath() {
		return null;
	}

	public Object getFaultTolDBMgr() {
		return null;
	}

	public final boolean usesFaultTolerance() {
		return false;
	}

	public final boolean useProxy() {
		return bUseProxy;
	}

	public Object getSetting(String name) {
		if (name.equalsIgnoreCase(TransceiverConfig.SHOWCONSOLE)) {
			return new Boolean(showconsole);
		}
		return null;
	}

	public Object getService(String name) {

		if (name.equalsIgnoreCase(TransceiverConfig.NETWORKAGENT)) {
			return networkAgent;
		} else if (name.equalsIgnoreCase(TransceiverConfig.PEER_FILE)) {
			return peerfilename;
		} else if (name.equalsIgnoreCase(TransceiverConfig.PLUGIN_FILE)) {
			return pluginFile;
		} else if (name.equalsIgnoreCase(TransceiverConfig.PLUGINFACTORY)) {
			return pluginFactory;
		}
		return null;
	}

	public void setLogPath(String path) {
	}

	public String getLogPath() {
		return null;
	}

	public static void main(String args[]) {

		Transceiver transceiver = null;

		try {
			do {
				System.out.println(args[0]);
				try {
					transceiver = new Transceiver();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

				transceiver.initialize(args);
				System.out.println(TransceiverInfo.VERSION
						+ " listening on port [" + transceiver.nPort
						+ "] started...");
				if (transceiver.getAdminPort() > 0) {
					System.out.println("Administration listening on port ["
							+ transceiver.nAdminPort + "], web serving from ["
							+ transceiver.getWebRoot() + "]...");
				}
			} while (transceiver.bRestarting == true);
		} catch (Exception e) {
			try {
				if (transceiver != null) {
					transceiver.cleanup(false);
				}
			} catch (AdaptinetException ce) {
				ce.printStackTrace();
			}
			System.err.println(TransceiverInfo.VERSION + " is shutting down..."
					+ "Exception=[" + e.getMessage() + "]");
			System.exit(1);
		}
	}
}
