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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.adaptinet.messaging.Address;
import org.adaptinet.messaging.Message;


public class PeerTree {

	private Map<Address, PeerNode> nodes = (Map<Address, PeerNode>) Collections
			.synchronizedMap(new HashMap<Address, PeerNode>());

	private PeerNode parent;

	public PeerTree(PeerNode parent) {
		this.parent = parent;
	}

	public PeerNode insert(PeerEntry peerEntry) {
		PeerNode node = new PeerNode(peerEntry);
		nodes.put(peerEntry.getAddress(), node);
		return node;
	}

	public void insert(PeerNode node) {
		nodes.put(node.getEntry().getAddress(), node);
	}

	public PeerNode getConnectionNode(int inc, PeerNode link) {

		PeerNode node = null;
		Iterator<PeerNode> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			if (link.equals(node) == false)
				if (node.getConnectionNode(inc, parent) != null)
					break;
		}

		return node;
	}

	public void computeAverage(PeerNode link) {

		PeerNode node = null;
		PeerNode.average = (PeerNode.average + nodes.size()) / 2;
		Iterator<PeerNode> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			if (link.equals(node) == false)
				node.computeAverage(parent);
		}
	}

	public void balance(PeerNode link) {
		PeerNode node = null;
		if (nodes.size() > PeerNode.average) {
			// TODO: balance
		}
		Iterator<PeerNode> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			if (link.equals(node) == false)
				node.balance(parent);
		}
	}

	public void optimize(PeerNode link) {

		PeerNode node;
		Iterator<PeerNode> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			if (link.equals(node) == false)
				node.optimize(parent);
		}
	}

	public void setAlive(PeerNode link) {
		setAlive(link, true);
	}
	
	public void setAlive(PeerNode link, boolean bAlive) {

		PeerNode node;
		Iterator<PeerNode> it = nodes.values().iterator();
		while (it.hasNext()) {
			node = it.next();
			node.setAlive(bAlive);
		}
	}

	public int size() {
		return nodes.size();
	}

	public long count(PeerNode link) {
		long l = nodes.size();
		for(PeerNode node : nodes.values()) {
			if (link != null && (link.equals(node) == false))
				l += node.count(parent);
		}
		return l;
	}

	public PeerNode get(Address tag) {
		return nodes.get(tag);
	}

	public PeerNode find(String tag, PeerNode link) {
		return find(new Address(tag), link);
	}

	public PeerNode find(Address tag, PeerNode link) {
		PeerNode node = null;
		try {
			node = nodes.get(tag);
			if (node == null) {
				for(PeerNode value : nodes.values()) {
					if (link.equals(value) == false)
						if ((node = value.find(tag, parent)) != null)
							break;
				}
			}
		} catch (NullPointerException e) {
			node = null;
		}
		return node;
	}

	public boolean remove(String tag, PeerNode link) {
		return remove(new Address(tag), link);
	}

	public boolean remove(Address address, PeerNode link) {
		if (nodes.remove(address) != null)
			return true;

		for(PeerNode node : nodes.values()) {
			if (link.equals(node) == false)
				if (node.remove(address, parent) == true)
					return true;
		}
		return false;
	}

	public void write(StringBuffer buffer) {
		write(buffer, null);
	}

	public void write(StringBuffer buffer, PeerNode link) {

		PeerNode node = null;
		Iterator<PeerNode> it = nodes.values().iterator();
		if (it.hasNext()) {
			// Lets check to see if we have any nodes to write.
			int count = 0;
			if (link != null) {
				do {
					node = it.next();
					if (link.equals(node) == false) {
						count++;
						break;
					}
				} while (it.hasNext());
				// If we don't find any exist
				if (count == 0)
					return;
				it = nodes.values().iterator();
			}
			do {
				node = it.next();
				if (link == null || link.equals(node) == false)
					node.write(buffer, parent);
			} while (it.hasNext());

		}
	}

	public void display(PeerNode link) {

		PeerNode node = null;
		Iterator<PeerNode> it = nodes.values().iterator();
		if (it.hasNext()) {
			// Lets check to see if we have any nodes to write.
			int count = 0;
			if (link != null) {
				do {
					node = it.next();
					if (link.equals(node) == false) {
						count++;
						break;
					}
				} while (it.hasNext());
				// If we don't find any exit
				if (count == 0)
					return;
				it = nodes.values().iterator();
			}

			do {
				node = it.next();
				if (link == null || link.equals(node) == false)
					node.display(parent);
			} while (it.hasNext());
		}
	}

	public void clear(PeerNode link) {

		for(PeerNode child : nodes.values()) {
			if (link == null || link.equals(child) == false) {
				child.clear(parent);
			}
		}
		nodes.clear();
	}

	public void flatten(List<PeerNode> list, PeerNode link) {

		for(PeerNode child : nodes.values()) {
			if (link.equals(child) == false) {
				list.add(child);
				child.flatten(list, parent);
			}
		}
	}

	public boolean disconnected(PeerNode node) {

		if (node != null) {
			nodes.remove(node.getEntry().getAddress());
			return true;
		}
		return false;
	}

	public void getLeaves(List<PeerNode> list, PeerNode link) {
		for(PeerNode child : nodes.values()) {
			if (link.equals(child) == false) {
				if (child.isLeaf()) {
					list.add(child);
					continue;
				}
				child.getLeaves(list, parent);
			}
		}
	}

	public int getRoute(String tag, List<PeerNode> list, PeerNode link) {
		return getRoute(new Address(tag), list, link);
	}

	public int getRoute(Address tag, List<PeerNode> list, PeerNode link) {
		int nRet = 0;

		PeerNode node = nodes.get(tag);
		if (node != null) {
			list.add(node);
			nRet = 1;
		} else {
			for(PeerNode child : nodes.values()) {
				if (link.equals(child) == false) {
					if ((nRet = child.getRoute(tag, list, parent)) != 0) {
						nRet++;
						list.add(child);
						break;
					}
				}
			}
		}
		return nRet;
	}

	public int getPath(String tag, String sPath, PeerNode link) {
		return getPath(new Address(tag), sPath, link);
	}

	public int getPath(Address address, String sPath, PeerNode link) {
		int nRet = 0;
		PeerNode node = nodes.get(address);
		if (node != null) {
			sPath += address.getURI();
			nRet = 1;
		} else {
			PeerNode child = null;
			Iterator<PeerNode> it = nodes.values().iterator();
			Iterator<Address> itKeys = nodes.keySet().iterator();
			while (it.hasNext()) {
				child = it.next();
				if (link.equals(child) == false) {
					if ((nRet = child.getPath(address, sPath, parent)) != 0) {
						nRet++;
						sPath += itKeys.next();
						sPath += ";";
						break;
					}
				}
				itKeys.next();
			}
		}
		return nRet;
	}

	protected final void broadcastMessage(String plugin, String request,
			Object args[]) {
		try {
			Message message = new Message();
			message.getAddress().setPlugin(plugin);
			message.getAddress().setMethod(request);
			broadcastMessage(message, args);
		} catch (Exception e) {
		}
	}

	protected final void broadcastMessage(Message message, Object args[]) {

		try {
			List<PeerNode> list = new ArrayList<PeerNode>(10);
			flatten(list, parent);
			for(PeerNode node : list) {
				node.postMessage(message, args);
			}
		} catch (Exception e) {
		}
	}

	public Iterator<PeerNode> values() {
		return nodes.values().iterator();
	}

	public void getNodes(List<PeerNode> list) {
		list.addAll(nodes.values());
	}
}