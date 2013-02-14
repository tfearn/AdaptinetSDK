/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.socket;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiverutils.ThreadCache;



abstract public class BaseSocketServer {

	static final public boolean debug = false;

	static final public int AVG_LIGHT = 1;

	static final public int AVG_NORMAL = 2;

	static final public int AVG_HIGH = 3;

	static final public int AVG_DEAD = 4;

	protected int idealFree = 0;

	protected int sockets = 0;

	protected int port = 0;

	protected InetAddress bindAddr = null;

	protected Thread socketThread = null;

	protected Thread maintenanceThread = null;

	protected boolean bRestarting = false;

	protected ThreadCache threadcache = null;

	protected int loadavg = AVG_LIGHT;

	protected boolean running = true;

	protected List<SocketState> freeList = null;

	protected List<SocketState> socketList = null;

	protected ITransceiver transceiver = null;

	protected String type = null;

	protected ServerSocket serverSocket = null;
	protected DatagramSocket datagramSocket = null;

	protected BaseSocketServer() {
		freeList = Collections.synchronizedList(new ArrayList<SocketState>());
		socketList = Collections.synchronizedList(new ArrayList<SocketState>());
	}

	abstract public void start(String identifier);

	abstract public int getLocalPort();

	abstract public InetAddress getInetAddress();

	abstract public void shutdown();

	abstract protected SocketState addSocket();

	public void initialize(ITransceiver theServer, int port, int sockets)
			throws Exception {

		this.port = port;
		this.sockets = sockets;
		transceiver = theServer;
		idealFree = (sockets >> 1);

		bindAddr = InetAddress.getLocalHost();
		serverSocket = createServerSocket();

		threadcache = new ThreadCache("sockets", sockets, transceiver
				.getClientThreadPriority(), 0);
		for (int i = 0; i < sockets; i++) {
			if (addSocket() == null) {
				throw new RuntimeException(this.getClass().getName()
						+ "[construstructor]" + ": unable to create sockets.");
			}
		}
	}

	protected ServerSocket createServerSocket() {
		return null;
	}

	protected DatagramSocket createDatagramSocket() {
		return null;
	}
	
	final public void join(int wait) {

		try {
			socketThread.join(wait);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setType(String baseType) {
		type = baseType;
	}

	protected void cleanup(boolean force) {

		killSockets(force);
		socketList = null;
		freeList = null;
		transceiver = null;
	}

	public List<SocketState> getClients() {
		return socketList;
	}

	protected synchronized void deleteClient(SocketState state) {
		socketList.remove(state);
	}

	protected void doMaintenance() {

		maintenanceThread = new Thread(new Runnable() {

			public void run() {

				while (running == true) {

					int oldavg = loadavg;
					int free = freeList.size();

					if (free >= idealFree) {
						loadavg = AVG_LIGHT;
					} else if (free < 5) {
						if ((loadavg = AVG_NORMAL) < oldavg) {
							transceiver.getThread().setPriority(
									Thread.MAX_PRIORITY);
						}
					} else if (free > 0) {
						if ((loadavg = AVG_HIGH) > oldavg) {
							transceiver.getThread().setPriority(
									transceiver.getClientThreadPriority() - 2);
						}
					} else {
						loadavg = AVG_DEAD;
					}

					if (Thread.interrupted() == true) {
						return;
					}

					killTimedoutSockets();

					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
	}

	synchronized private void killTimedoutSockets() {

		try {
			Iterator<SocketState> it = socketList.iterator();
			while (it.hasNext()) {
				try {
					SocketState state = it.next();
					if (state.isTimedOut()) {
						killSocket(state);
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
	}

	synchronized private void killSocket(SocketState state) {

		if (debug) {
			System.out.println("Connection in slot "
					+ new Integer(state.getId()).toString()
					+ " is being killed.");
		}
		state.kill(true);
		state.unbind();
		state.setStatus(SocketState.FREE);
		freeList.add(state);
	}

	synchronized public boolean socketFinished(BaseSocket socket) {

		if (running == true) {
			SocketState state = socket.getState();
			state.setStatus(SocketState.FREE);
			freeList.add(state);
		}
		return running;
	}

	public void run(BaseSocket socket) {
		socket.wakeup();
	}

	private synchronized void killSockets(boolean force) {

		running = false;

		if (force != true) {
			Iterator<SocketState> it = socketList.iterator();
			for (SocketState state = it.next(); it.hasNext(); state = it
					.next()) {
				state.kill(state.getStatus() == SocketState.IDLE);
			}

			try {
				Thread.sleep(10);
			} catch (Exception ex) {
				System.out.println(ex);
			}
		} else {
			Iterator<SocketState> it = socketList.iterator();
			for (SocketState state = it.next(); it.hasNext(); state = it
					.next()) {
				state.kill(true);
			}
		}
	}

	static final public BaseSocketServer createInstance(String type) {
		return createInstance(type, null);
	}

	@SuppressWarnings("unchecked")
	static final public BaseSocketServer createInstance(String type,
			String serverclass) {

		BaseSocketServer factory = null;
		Class<BaseSocketServer> c = null;
		String baseType = type;

		try {
			if (type.startsWith("PROXY")) {
				c = (Class<BaseSocketServer>)Class.forName("org.adaptinet.socket.ProxySocketServer");
				baseType = type;
			} else if (type.startsWith("CALLBACK")) {
				c = (Class<BaseSocketServer>)Class.forName("org.adaptinet.socket.CallbackSocketServer");
				baseType = type;
			} else if (type.startsWith("TLS")) {
				java.security.Security
						.addProvider((java.security.Provider) Class.forName(
								"com.sun.net.ssl.internal.ssl.Provider")
								.newInstance());
				Properties sysProps = System.getProperties();
				sysProps.put("java.protocol.plugin.pkgs",
						"com.sun.net.ssl.internal.www.protocol");
				System.setProperties(sysProps);
				c = (Class<BaseSocketServer>)Class
						.forName("org.adaptinet.socket.SecureHttpSocketServer");
				baseType = type.substring(3);
			} else if (type.startsWith("CUSTOM")) {
				c = (Class<BaseSocketServer>)Class.forName(serverclass);
				baseType = type;
			} else {
				c = (Class<BaseSocketServer>)Class.forName("org.adaptinet.socket.HttpSocketServer");
				baseType = type;
			}
			factory = (BaseSocketServer) c.newInstance();
			factory.setType(baseType);
		} catch (Exception e) {
			System.out
					.println("Error creating instance of socketfactory error msg = "
							+ e.getMessage());
		}
		return factory;
	}

}
