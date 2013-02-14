/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.peer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.adaptinet.cache.CachePlugin;
import org.adaptinet.messaging.Address;
import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.pluginagent.MaintenancePlugin;

public class PeerNode {

	protected PeerEntry peerEntry = null;
	protected PeerTree tree = null;
	static int levels = 5;
	static Messenger messenger = null;
	static int min = 5;
	static int max = 10;
	static int average = 0;
	static public short RIGHT = 1;
	static public short LEFT = -1;

	public PeerNode() {
		tree = new PeerTree(this);
	}

	public PeerNode(PeerEntry peerEntry) {
		this.peerEntry = peerEntry;
		tree = new PeerTree(this);
	}

	public final PeerNode getConnectionNode(int inc, PeerNode link) {

		try {
			if(isAlive()) {
				long s = count(link);
				if (s < min || s < (average + inc))
					return this;
			}
			return tree.getConnectionNode(inc, link);
		} catch (Exception e) {
		}
		return null;
	}

	/*
	 * Insert a entry in the current tree
	 */
	public final PeerNode insert(PeerEntry entry) {

		try {
			// Insert into current tree.
			PeerNode node = tree.insert(entry);
			// Insert this node into the insert node reverse spin
			node.link(this);
			return node;
		} catch (Exception e) {
		}
		return null;
	}

	/*
	 * Insert a node in the current tree
	 */
	public final PeerNode insert(PeerNode node) {

		try {
			// Insert into current tree.
			tree.insert(node);
			// Insert this node into the inserte node reverse spin
			node.link(this);
			return node;
		} catch (Exception e) {
		}
		return null;
	}

	/*
	 * Link a node in the current tree
	 */
	public final void link(PeerNode node) {

		try {
			// Insert into current tree.
			tree.insert(node);
		} catch (Exception e) {
		}
	}

	// Find a local node
	public final PeerNode get(String tag) {
		try {
			return get(new Address(tag));
		} catch (Exception e) {
		}
		return null;
	}

	public final PeerNode get(Address tag) {
		try {
			return tree.get(tag);
		} catch (Exception e) {
		}
		return null;
	}

	// Find a node through out the known network
	public final PeerNode find(String tag) {
		return find(new Address(tag));
	}

	public final PeerNode find(Address tag) {
		if (tag.equals(peerEntry.getAddress()))
			return this;
		return tree.find(tag, this);
	}

	// Find a node through out the known network
	public final PeerNode find(String tag, PeerNode link) {
		try {
			return tree.find(tag, link);
		} catch (Exception e) {
		}
		return null;
	}

	public final PeerNode find(Address tag, PeerNode link) {
		try {
			return tree.find(tag, link);
		} catch (Exception e) {
		}
		return null;
	}

	public final boolean remove(PeerEntry entry) {
		return tree.remove(entry.getAddress(), this);
	}

	public final boolean remove(Address tag) {
		return tree.remove(tag, this);
	}

	public final boolean remove(String tag) {
		return tree.remove(tag, this);
	}

	public final boolean remove(PeerEntry entry, PeerNode link) {
		try {
			return tree.remove(entry.getAddress(), link);
		} catch (Exception e) {
		}
		return false;
	}

	public final boolean remove(Address tag, PeerNode link) {
		try {
			return tree.remove(tag, link);
		} catch (Exception e) {
		}
		return false;
	}

	public final boolean remove(String tag, PeerNode link) {

		try {
			return tree.remove(tag, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public final int getRoute(String tag, List<PeerNode> list, PeerNode link) {
		try {
			return tree.getRoute(tag, list, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public final int getRoute(Address tag, List<PeerNode> list, PeerNode link) {
		try {
			return tree.getRoute(tag, list, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public final int getPath(String tag, String sPath, PeerNode link) {
		try {
			return tree.getPath(tag, sPath, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public final int getPath(Address tag, String sPath, PeerNode link) {
		try {
			return tree.getPath(tag, sPath, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public final void getLeaves(List<PeerNode> list, PeerNode link) {
		try {
			tree.getLeaves(list, link);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void clear(PeerNode link) {
		try {
			tree.clear(link);
			tree = new PeerTree(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void computeAverage(PeerNode link) {
		try {
			tree.computeAverage(link);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void notifyInserted(String address) {

		List<PeerNode> list = new ArrayList<PeerNode>(10);
		tree.flatten(list, this);

		Object args[] = new Object[2];
		args[0] = new String(getEntry().getURL());
		args[1] = new String(address);
		for(PeerNode node : list) {
			node.sendNotifyInserted(args);
		}
	}

	
	public final void doMaintenance() {

		try {
			MaintenancePlugin.doPing(peerEntry.getAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void doCache() {

		try {
			CachePlugin.doCacheCheck(peerEntry.getAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public final void sendNotifyInserted(Object args[]) {
		postMessage("adaptinet", "insert", args);
	}

	public final void notifyRemoved(Object args[]) {
		postMessage("adaptinet", "remove", args);
	}

	public final void optimize(PeerNode link) {

		try {
			tree.optimize(link);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void balance(PeerNode link) {
		try {
			tree.balance(link);
		} catch (Exception e) {
		}
	}

	public final long count(PeerNode link) {
		try {
			return tree.count(link);
		} catch (Exception e) {
		}
		return 0;
	}

	public final void flatten(List<PeerNode> list, PeerNode link) {
		try {
			tree.flatten(list, link);
		} catch (Exception e) {
		}
	}

	public final void getAdjacent(List<PeerNode> list) {
		tree.getNodes(list);
		list.remove(this);
	}

	public final Iterator<PeerNode> values() {
		return tree.values();
	}

	final boolean isLeaf() {
		boolean ret = true;
		if (tree != null) {
			if (tree.size() > 0) {
				ret = false;
			}
		}
		return ret;
	}

	final public int size() {
		return tree.size();
	}

	final public PeerEntry getEntry() {
		return peerEntry;
	}

	final public Address getAddress() {
		return peerEntry.getAddress();
	}

	final public String getURL() {
		return peerEntry.getURL();
	}

	final public void broadcastMessage(String plugin, String request, Object args[]) {
		try {
			Message message = new Message(peerEntry.getURL());
			message.getAddress().setPlugin(plugin);
			message.getAddress().setMethod(request);
			broadcastMessage(message, args);
		} catch (Exception e2) {
		}
	}

	public void broadcastMessage(Message message, Object args[]) {
		try {
			tree.broadcastMessage(message, args);
		} catch (Exception e2) {
		}
	}

	final void postMessage(String plugin, String request, Object args[]) {
		try {
			if(isAlive()) {
				Message message = new Message(peerEntry.getAddress());
				message.getAddress().setPlugin(plugin);
				message.getAddress().setMethod(request);
				Messenger.postMessage(message, args);
			}
		} catch (Exception e) {
		}
	}

	final void postMessage(Message message, Object args[]) {
		try {
			if(isAlive()) {
				message.getAddress().setHost(peerEntry.getAddress().getHost());
				message.getAddress().setPort(peerEntry.getAddress().getPort());
				// System.out.println("Sending Message : " +
				// message.getAddress().getURI());
				Messenger.postMessage(message, args);
			}
		} catch (Exception e) {
		}
	}

	public void write(StringBuffer buffer, PeerNode link) {
		try {
			buffer.append("<Peer>");
			buffer.append("<URI>");
			buffer.append(peerEntry.getURL());
			buffer.append("</URI>");
			buffer.append("<Name>");
			buffer.append(peerEntry.getName());
			buffer.append("</Name>");
			buffer.append("<Time>");
			buffer.append(peerEntry.getTime());
			buffer.append("</Time>");
			tree.write(buffer, link);
			buffer.append("</Peer>");
		} catch (Exception e) {
		}
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buffer.append("<Peers>");
		tree.write(buffer, this);
		buffer.append("</Peers>");
		return buffer.toString();
	}

	public void toScreen() {
		System.out.println("<Peers>");
		tree.display(this);
		System.out.println("</Peers>");
	}

	public void display(PeerNode link) {
		try {
			System.out.println("<Peer>");
			System.out.println("<URI>");
			System.out.println(peerEntry.getNameURL());
			System.out.println("</URI>");
			System.out.println("<Name>");
			System.out.println(peerEntry.getName());
			System.out.println("</Name>");
			System.out.println("<Time>");
			System.out.println(peerEntry.getTime());
			System.out.println("</Time>");
			tree.display(link);
			System.out.println("</Peer>");
		} catch (Exception e) {
		}
	}

	static public void setLevels(int levels) {
		PeerNode.levels = levels;
	}

	static public void setMax(int max) {
		PeerNode.max = max;
		min = max / 2;
	}

	final public void setEntry(PeerEntry peerEntry) {
		this.peerEntry = peerEntry;
	}

	public final boolean isAlive() {
		return peerEntry.isAlive();
	}

	public void setAlive(boolean isAlive) {
		peerEntry.setAlive(isAlive);
	}

	public int getKey() {
		try {
			return peerEntry.getKey();
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean keyEquals(int value) {
		try {
			int key = peerEntry.getKey();
			if(value!=0 && key==value) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public boolean equals(Object object) {

		if (this == object)
			return true;
		else if (object == null)
			return false;

		try {
			return peerEquals((PeerNode) object);
		} catch (Exception e) {
		}
		return false;

	}

	public boolean peerEquals(PeerNode peer) {
		return peerEntry.getAddress().hashCode() == peer.getEntry()
				.getAddress().hashCode();
	}
}