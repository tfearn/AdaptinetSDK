/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.peer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.adaptinet.messaging.Address;
import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.parser.InputSource;
import org.adaptinet.parser.XMLReader;
import org.adaptinet.transceiver.ITransceiver;


final public class PeerRoot extends PeerNode {

	
	public PeerRoot(ITransceiver transceiver) {
		super();
		this.peerEntry = new PeerEntry(transceiver, false);
		this.peerEntry.setName(transceiver.getHost());
	}

	public PeerRoot(PeerEntry peerEntry) {
		super(peerEntry);
	}

	public PeerRoot() {
		super();
		this.peerEntry = new PeerEntry();
	}

	static public void initRoot() {
		messenger = new Messenger();
	}

	public PeerNode isConnected(PeerEntry entry) throws Exception {

		if (entry.getURL().equals(getURL())) {
			return null;
		}
		// Check to see if this peer already exists in our segment.
		return tree.find(entry.getURL(), this);
	}

	/**
	 * When finding a node to insert a lot has to be determined.
	 */
	public PeerNode doConnect(PeerEntry entry) throws Exception {

		/**
		 * Now we need to find the peer that is willing to accept the request
		 */
		try {
			/**
			 * First try to find a node that is within the average
			 * and increment up until max;
			 */
			PeerNode node = getConnectionNode();

			// Check to see if we are the insertion node.
			if (node == this) {
				//System.out.println("Root is the insertion node");
				return insert(entry);
			}

			/**
			 * If no one volunteers to take this peer we will have to go outside
			 * our segment. Two help balance lets find a leave with the lowest
			 * count.
			 */
			if (node == null) {
				List<PeerNode> list = new ArrayList<PeerNode>();
				tree.getLeaves(list, this);
				Iterator<PeerNode> it = list.iterator();
				int lowest = Integer.MAX_VALUE;
				PeerNode temp = null;
				while (it.hasNext()) {
					temp = it.next();
					if (lowest > temp.size()) {
						lowest = temp.size();
						node = temp;
					}
				}
				// The node with the lowest count should fall through
			}

			// Most likely candidate for accepting the request.
			if (node != null) {
				Object args[] = new Object[3];
				args[0] = new String(entry.getURL());
				args[1] = new String(entry.getName());
				args[2] = new Boolean(true);
				node.postMessage("adaptinet", "connect", args);
			}
		} catch (Exception e) {
			// Need to do something better but this will due for debugging
			e.printStackTrace();
			throw e;
		}
		return null;
	}

	public final PeerNode getConnectionNode() {

		try {
			int inc = 0;
			PeerNode node = null;
			while (inc + average <= max) {
				/**
				 * Before we go crazy maybe I'm the insertion node Its a simple
				 * Check so lets do it first;
				 */
				if (inc + average <= count()) {
					return this;
				}
				/**
				 * This must be full so We'll have to check to see who can do
				 * the connection.
				 */
				if ((node = tree.getConnectionNode(inc, this)) != null)
					return node;
				inc++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public final void maintenance() {

		try {
			if(isAlive()) {
				List<PeerNode> list = new ArrayList<PeerNode>(10);
				tree.flatten(list, this);
				for(PeerNode node : list) {
					node.doMaintenance();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void cache() {

		try {
			if(isAlive()) {
				List<PeerNode> list = new ArrayList<PeerNode>(10);
				tree.flatten(list, this);
				for(PeerNode node : list) {
					node.doCache();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refuseConnection(String peer) {

		// Check to see if this peer is in our segment anyway.
		if (tree.find(peer, this) != null)
			return;

		Message message = new Message(peer);
		message.getAddress().setPlugin("adaptinet");
		message.getAddress().setMethod("remove");
		Object args[] = new Object[1];
		args[0] = new String(peerEntry.getAddress().getURL());
		Messenger.postMessage(message, args);
	}

	/**
	 * This is called through the NetworkAgent and is initiated by the
	 * transceiver form shutdown to disconnect from the network
	 */
	public final void doDisconnect() throws Exception {
		doDisconnect(this);
	}

	public final void doDisconnect(PeerNode node) throws Exception {

		try {
			// Find our replacement (first node in the tree)
			Iterator<PeerNode> it = node.values();

			if (it.hasNext()) {
				PeerNode peer = it.next();
				// Get the message ready.
				Object args[] = null;
				args = new Object[1];
				args[0] = new String(node.getEntry().getURL());
				notifyRemoved(args);
				node.remove(peer.getAddress());
			}
		} catch (Exception e) {
			// TODO: add some reporting change exception type
			throw e;
		}
	}

	/**
	 * This is called through the NetworkAgent to inform this peer that an
	 * adjacent peer has dropped out of the peer network and we are stepping in
	 */
	final public void doReplace(String address, String xml) throws Exception {

		try {
			Address replaceAt = new Address(address);
			remove(replaceAt);
			Iterator<PeerNode> it = tree.values();

			// First we need to find our replacement
			while (it.hasNext()) {
				PeerNode node = (PeerNode) it.next();
				// Make sure we are not sending it back to who
				// we are replacing.
				// if(replaceAt.hashCode()==node.getKey())
				// continue;

				Message message = new Message(node.getEntry().getURL());
				message.getAddress().setPlugin("adaptinet");
				message.getAddress().setMethod("replace");
				message.setKey(peerEntry.getKey());

				remove(node.getAddress());
				// Prepare the parameters
				Object args[] = new Object[2];
				args[0] = new String(peerEntry.getURL());
				args[1] = toString();

				// Post message to our sub
				node.postMessage(message, args);
				break;
			}
			//
			// Update our new structure
			parsePeers(xml.getBytes());

			Message message = new Message();
			message.getAddress().setPlugin("adaptinet");
			message.getAddress().setMethod("update");
			message.setKey(peerEntry.getKey());

			// Prepare the parameters
			Object args[] = new Object[2];
			args[0] = address;
			args[1] = new String(peerEntry.getURL());
			broadcastMessage(message, args);

		} catch (Exception e) {
			// TODO: add some reporting change exception type
			throw e;
		}
	}

	public final void postDisconnected() {
		tree.broadcastMessage("adaptinet", "disconnected", null);
	}

	/**
	 * This will be called by an adjacent peer when it is dropping out of the
	 * network or being optimized out.
	 */
	public void parsePeers(byte bytes[]) throws Exception {

		try {
			if (bytes.length > 0) {
				XMLReader parser = new XMLReader();
				parser.setContentHandler(new PeerParser(this));
				parser.parse(new InputSource(new ByteArrayInputStream(bytes)));
			}
		} catch (Exception x) {
			throw x;
		}
	}

	public final long count() {
		return count(null);
	}

	public final int getRoute(String tag, List<PeerNode> list) {
		return tree.getRoute(tag, list, this);
	}

	public final int getRoute(Address tag, List<PeerNode> list) {
		return tree.getRoute(tag, list, this);
	}

	public final int getPath(String tag, String sPath) {
		return tree.getPath(tag, sPath, this);
	}

	public final int getPath(Address tag, String sPath) {
		return tree.getPath(tag, sPath, this);
	}

	public final void getLeaves(List<PeerNode> list) {
		tree.getLeaves(list, this);
	}

	public final void setAlive(boolean bAlive) {
		tree.setAlive(this, true);
	}

	public final void computeAverage() {
		PeerNode.average = (int) count();
		if (PeerNode.average > 0) {
			PeerNode.average /= 2;
		}
		tree.computeAverage(this);
	}

	public final void optimize() {
		tree.optimize(this);
	}

	public void balance() {
		tree.balance(this);
	}

	public final void flatten(List<PeerNode> list) {
		tree.flatten(list, this);
	}

}
