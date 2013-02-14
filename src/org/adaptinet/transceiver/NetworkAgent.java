/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiver;

import java.util.ArrayList;
import java.util.Iterator;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.messaging.Address;
import org.adaptinet.messaging.Envelope;
import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.peer.PeerEntry;
import org.adaptinet.peer.PeerNode;
import org.adaptinet.peer.PeerRoot;
import org.adaptinet.pluginagent.PluginFactory;

public final class NetworkAgent implements IResetEvent {

	private String networkAgentName;
	private MaintenanceThread maintenance = null;
	private CacheThread cache = null;
	private PeerRoot root = null;
	private String connectType = null;
	private boolean bConnected = false;
	private boolean autoconnect = false;
	private Gateway gateway = null;
	
	@SuppressWarnings("unused")
	private static int RETRIES = 3;

	public NetworkAgent(final ITransceiver transceiver, final String name,
			final boolean autoconnect, final String connectType, final int max,
			final int levels) {

		this.autoconnect = autoconnect;
		this.setConnectType(connectType);
		this.root = new PeerRoot(transceiver);
		setConnected(false);
		PeerNode.setLevels(levels);
		PeerNode.setMax(max);
		
		gateway = new Gateway(root, name);
		maintenance = new MaintenanceThread(root, this);
		cache = new CacheThread(root);
		
		maintenance.start();
		cache.start();
	}

	public void start() {

		try {
			gateway.enter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean loadPeers(final String xml) throws Exception {

		boolean bRet = false;
		try {
			root.clear(null);
			root.parsePeers(xml.getBytes());
			root.setAlive(true);
			root.computeAverage();
			bRet=true;
		} catch (Exception x) {
			throw x;
		}
		return bRet;
	}

	public void closeFile() {

		try {
			if(gateway!=null) {
				gateway.close();
			}
			if(cache!=null) {
				cache.stop();
			}
			if(maintenance!=null) {
				maintenance.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PeerEntry findEntry(final String name) {

		try {
			PeerNode node = null;
			node = root.find(name);
			if (node != null) {
				return node.getEntry();
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Here are the steps to making a connection into the network
	 * 
	 * 1) A new peer sends a connect request to its known peers. once a peer is
	 * found that can service this request it will process the request.
	 * 
	 * 2) A connected message is sent back to the the peer with the topology of
	 * the network segment.
	 * 
	 * 3) All peers in the network are sent a insert message to tell them to
	 * insert the new peer into their copy of the topology.
	 */
	public void connect(final PeerEntry entry) {
		connect(entry, false);
	}

	public synchronized void connect(final PeerEntry entry, final boolean bAuto) {

		PeerNode node = null;
		try {
			if (autoconnect == false && bAuto == false) {
				root.insert(entry);
			} else {
				boolean firstconnect = bConnected;
				
				Message message = new Message(entry.getURL(),
						ITransceiver.getTransceiver());
				message.getAddress().setPlugin("adaptinet");

				Object[] args = new Object[2];
				args[0] = root.getAddress().getURL();

				if ((node = root.isConnected(entry)) != null && node.isAlive()) {
					message.getAddress().setMethod("isconnected");
					args[1] = node.toString();
					Messenger.postMessage(message, args);
				} else if ((node = root.doConnect(entry)) != null) {
					if(gateway!=null) {
						gateway.setHold(true);
					}
					//entry.setAlive(true);
					message.getAddress().setMethod("connected");
					args[1] = node.toString();
					Messenger.postMessage(message, args);
					
					//We may not be connected either so let set it now 
					if(!bConnected) {
						setConnected(true);
					}
					gateway.setDirty(true);
					peerUpdate();
					if(gateway!=null) {
						gateway.setHold(false);
					}
				} else {
					/**
					 * This a timing issue two nodes send a connect message too
					 * each other. One will get inserted first this will allow
					 * the one with the connected message to process.
					 */
					if (firstconnect == false) {
						setConnected(false);
					}
				}
			}
			System.out.println("Connect: " + root.toString());
		} catch (Exception e) {
			if(gateway!=null) {
				gateway.setHold(false);
			}
			e.printStackTrace();
		}
	}

	/**
	 * This is step 2 in the connection process I have asked to be connected and
	 * someone has responded back to me with the new topology.
	 */
	public synchronized void connected(final String peer, final String xml) {

		if (autoconnect == true) {
			try {

				if (bConnected == true) {
					root.refuseConnection(peer);
					return;
				}
				// Wake up the thread so we can load the peers.
				//System.out.println("Connected Topology: "+xml);
				if(loadPeers(xml)) {
					//System.out.println("Root Topology: "+root.toString());
					root.notifyInserted(peer);
					peerUpdate();
					root.maintenance();
					//root.display(root);
					root.cache();
					if (!bConnected) {
						setConnected(true);
					}
				}
				System.out.println("Connected: " + root.toString());
			} catch (Exception e) {
				e.printStackTrace();
				// Something went wrong so we should try all over again.
				setConnected(false);
				gateway.enter();
			}
		}
	}

	/**
	 * If we are here there may have been some timing issues so we are just
	 * being informed that we are already connected.
	 */
	public synchronized void isconnected(final String peer, final String xml) {

		if (autoconnect == true) {
			try {
				// If we are in here there may have been some sort of
				// problem so we just go on like we are getting connecting
				// again.
				connected(peer, xml);
			} catch (Exception e) {
				e.printStackTrace();
				// Something went wrong so we should try all over again.
				setConnected(false);
				gateway.enter();
			}
		}
	}

	/**
	 * This is step 3 in the connection process I've been called by the peer so
	 * I can update my copy of the topology.
	 */
	public synchronized void insert(final Envelope env) {

		// First see if this is an auto-connect otherwise we ignore this request
		if (autoconnect == true) {

			try {
				Object[] args = env.getBody().getcontentArray();
				//System.out.println("Insert : " + args[0]);
				//System.out.println("Insert at : " + args[1]);
				if (args.length < 2) {
					return;
				}

				synchronized (root) {
					PeerNode atNode = null;

					// First find the insertion point.
					Address connectAt = new Address((String) args[1]);
					if ((atNode = root.find(connectAt)) == null) {
						// If not found no need to continue,
						System.out.println("Cannot find : " + args[1]);
						return;
					}

					// Check to see if this peer already exists in our segment.
					// if it does may have been in the peer file so just update.
					Address entry = new Address((String) args[0]);
					PeerEntry pe = null;
					PeerNode pn = atNode.find(entry);
					if (pn == null) {
						pe = new PeerEntry(entry);
						pn = atNode.insert(pe);
					}
					else {
						pe = pn.getEntry();
					}
					
					if(!pe.isAlive()) {
						pe.setAlive(true);
						gateway.setDirty(false);
						peerUpdate();
						root.broadcastMessage(env.getHeader().getMessage(), args);
						pn.doMaintenance();
						pn.doCache();
					}
					if(!bConnected) {
						setConnected(true);
					}
				}
				System.out.println("Insert: " + root.toString());
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This is one of the steps in the repair process we need to update everyone
	 * that knew about the drop-out peer with the replacement peer.
	 */
	public synchronized void update(final Envelope env) {

		// First see if this is an auto-connect otherwise we ignore these
		// problems
		if (autoconnect == true) {

			try {
				Object[] args = env.getBody().getcontentArray();
				if (args.length < 2) {
					return;
				}

				synchronized (root) {
					PeerNode atNode = null;

					// First find the old peer.
					Address connectAt = new Address((String) args[0]);
					if ((atNode = root.find(connectAt)) == null) {
						// If not found no need to continue,
						return;
					}

					Address entry = new Address((String) args[1]);
					// In case we have this node somewhere else we need
					// to get ride of it first.
					root.remove(entry);
					atNode.setEntry(new PeerEntry(entry));
					gateway.setDirty(false);
					peerUpdate();
					root.broadcastMessage(env.getHeader().getMessage(), args);
				}
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notifies all plugins that the peers have changes.
	 */
	private void peerUpdate() {
		try {
			gateway.setDirty(true);
			((PluginFactory) ITransceiver.getNamedService("pluginfactory"))
					.postMessage("peerUpdate", null);
		} catch (NullPointerException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is called by the transceiver so that the network agent can inform
	 * all peers it is directly connected too that it is dropping out of the
	 * network.
	 */
	public void disconnect() {
		if (autoconnect) {
			try {
				((PluginFactory) ITransceiver.getNamedService("pluginfactory"))
						.cleanupPlugin();
				synchronized (root) {
					root.doDisconnect();
				}
			} catch (Exception e) {
				// For what ever reason we failed to
				// disconnect nicely lets try to inform
				// all our node that they have been disconnected
				synchronized (root) {
					root.postDisconnected();
				}
			}
		}
	}

	/**
	 * This is received from a message post to inform this peer that an adjacent
	 * peer has dropped out of the peer network and we are stepping in
	 */
	public void replace(final String address, final String xml) {
		if (autoconnect) {
			try {
				synchronized (root) {
					System.out.println("replace " + address + " peers " + xml);
					root.doReplace(address, xml);
					if (root.count() < 1) {
						reconnect();
					} else {
						peerUpdate();
					}
				}
			} catch (Exception e) {
				// For what ever reason we failed to
				// replace lets inform all our peers and try a
				disconnected();
			}
		}
	}

	/**
	 * This is received from a message post to inform this peer that an adjacent
	 * peer has dropped out of the peer network. If we received this message
	 * there was a problem and we will need to reconnect to the network to
	 * maintain integrity all of our peers should reconnect.
	 */
	public void disconnected() {

		if (autoconnect == true) {
			// Remove this peer so we don't send any more messages
			synchronized (root) {
				root.postDisconnected();
			}

			/**
			 * We have to try to rejoin the network. The way too do this is look
			 * at the back up peer file because obviously the current one has a
			 * problem.
			 */
			try {
				reconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void reconnect() throws Exception {
		try {
			if (autoconnect) {
				gateway.enter();
				peerUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void remove(final String peer) {
		try {
			synchronized (root) {
				if (root.remove(peer)) {
					// If we removed this peer we should continue to notify down
					// the line that we have done so.
					Object[] args = new Object[1];
					args[0] = new String(peer);
					root.notifyRemoved(args);
					peerUpdate();
					if (root.count() < 1) {
						reconnect();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	ArrayList<PeerNode> search() {
		ArrayList<PeerNode> list = new ArrayList<PeerNode>(100);

		ITransceiver transceiver = ITransceiver.getTransceiver();
		String baseaddr = transceiver.getHost().substring(0, 12);
		int me = Integer.parseInt(transceiver.getHost().substring(12));

		Message message = new Message();
		Address address = new Address();
		message.setAddress(address);

		try {
			for (int i = 0; i < 255; i++) {
				if (bConnected == true) {
					break;
				}
				if (i == me) {
					continue;
				}
				String uri = baseaddr + Integer.toString(i) + ":"
						+ transceiver.getPort();
				address.setURI(uri + "/Console/ping");
				try {
					System.out.println("Attempting to contact: " + uri);
					if (Messenger.testConnection(message)) {
						list.add(new PeerNode(new PeerEntry(uri)));
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}

		return list;
	}

	public String getName() {
		return networkAgentName;
	}

	public void broadcastMessage(final Envelope env) throws AdaptinetException {

		if (bConnected == false) {
			AdaptinetException adaptinetx = new AdaptinetException(
					AdaptinetException.SEVERITY_ERROR,
					AdaptinetException.GEN_MESSAGE);
			adaptinetx
					.logMessage("Can not broadcast message while not connected");
			throw adaptinetx;
		}
		root.broadcastMessage(env.getHeader().getMessage(), env.getBody()
				.getcontentArray());
	}

	public void broadcastMessage(final Message message, final Object[] args)
			throws AdaptinetException {

		if (bConnected == false) {
			AdaptinetException adaptinetx = new AdaptinetException(
					AdaptinetException.SEVERITY_ERROR,
					AdaptinetException.GEN_MESSAGE);
			adaptinetx
					.logMessage("Can not broadcast message while not connected");
			throw adaptinetx;
		}
		root.broadcastMessage(message, args);
	}

	public Iterator<PeerNode> getValues(final boolean all) {
		ArrayList<PeerNode> list = new ArrayList<PeerNode>(100);
		if (all == true) {
			root.flatten(list);
		} else {
			root.getAdjacent(list);
		}
		return list.iterator();
	}
	
	public static void setRetries(final int retries) {
		NetworkAgent.RETRIES = retries;
	}

	private void setConnected(boolean b) {
		bConnected = b;
		if(gateway!=null) {
			gateway.setConnected(bConnected);
		}
		if(cache!=null) {
			cache.setConnected(bConnected);
		}
		if(maintenance!=null) {
			maintenance.setConnected(bConnected);
		}
	}

	public void setDirty(final boolean bIsDirty) {
		gateway.setDirty(bIsDirty);
	}
	
	@Override
	public void reset() {
		try {
			reconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getConnectType() {
		return connectType;
	}

	public void setConnectType(String connectType) {
		this.connectType = connectType;
	}
}
