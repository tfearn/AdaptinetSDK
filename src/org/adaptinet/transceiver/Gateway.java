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

import org.adaptinet.messaging.Address;
import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.peer.PeerEntry;
import org.adaptinet.peer.PeerFile;
import org.adaptinet.peer.PeerNode;
import org.adaptinet.peer.PeerRoot;

public class Gateway implements Runnable {

	private static final String CONNECT = "/adaptinet/connect";
	private static final int STARTWAIT = 5000;
	private static final int CONNECTEDWAIT = 60000;

	private PeerRoot root = null;
	private String name = null;
	private PeerFile file = null;
	private Thread runner = null;
	private String peer = null;
	private boolean bIsDirty = false;
	private boolean bStopped = true;
	private boolean bConnected = false;
	private boolean bHold = false;
	private int nWaitTime = 5000;

	public Gateway(PeerRoot root, String name) {
		this.root = root;
		this.name = name;
	}

	void enter() {
		PeerRoot.initRoot();
		file = new PeerFile();
		runner = new Thread(this);
		bStopped = false;
		runner.start();
	}

	public void join() throws InterruptedException {
		runner.join();
	}

	public void join(long wait) throws InterruptedException {
		runner.join(wait);
	}

	public boolean isAlive() {
		return runner.isAlive();
	}

	public void run() {

		PeerRoot tmpRoot = null;
		PeerNode child = null;
		boolean bAddedLastKnownPeers = false;

		try {
			tmpRoot = file.open(name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			ITransceiver transceiver = ITransceiver.getTransceiver();
			Object[] args = new Object[3];
			Message message = new Message();
			Address address = new Address();
			message.setAddress(address);

			args[0] = transceiver.getHost() + ":" + transceiver.getPort();
			args[1] = transceiver.getIdentifier();
			args[2] = new Boolean(true);

			ArrayList<PeerNode> list = null;

			// Nothing to connect do a scan instead.
			if (tmpRoot.count() < 1) {
				list = search();
				if (list.size() < 1) {
					return;
				}
			} else {
				list = new ArrayList<PeerNode>(100);
				tmpRoot.flatten(list);
				tmpRoot.clear(null);
			}

			while (!bStopped && !bConnected) {
				if(bHold) {
					try {
						wait(500);
						Thread.interrupted();
					} catch (Exception e) {
						/*
						 */
					}
				}
				else {
					Iterator<PeerNode> it = list.iterator();
					synchronized (this) {
						try {
							while (it.hasNext()) {
								if(bHold) {
									break;
								}
								if (bStopped) {
									return;
								}
								child = it.next();
								if (!child.isAlive() && !root.equals(child)) {
									try {
										address.setURI(child.getEntry().getURL()
												+ CONNECT);
										Messenger.postMessage(message, args);
										wait(200);
										Thread.interrupted();
									} catch (Exception e) {
										/*
										 * If we get an exception here we where
										 * unable to connect so we continue trying
										 * other peers until one connects us.
										 */
									}
								}
							}
							if(bHold) {
								break;
							}
							if (bStopped || bConnected) {
								return;
							}
							if (bConnected) { 
								nWaitTime = CONNECTEDWAIT;		
								if(bIsDirty) {
									save();
									bIsDirty = false;
								}							
								root.computeAverage();
							}
							else {
								nWaitTime = STARTWAIT;
							}
							
							/**
							 * We where not able to connect so we will add the
							 * peers we connected to last time and try everybody again.
							 */
							if (!bConnected && !bAddedLastKnownPeers) {
								try {
									bAddedLastKnownPeers = true;
									tmpRoot = file.open(name);
									file.setDefault(true);
									ArrayList<PeerNode> templist = new ArrayList<PeerNode>(100);
									tmpRoot.flatten(list);
									list.addAll(templist);
								} catch (Exception e) {
									/* No default peer file to process. */
									continue;
								}
							}
						} catch (Exception e) {
						}
					}
	
					try {
						Thread.sleep(nWaitTime);
						Thread.interrupted();
					} catch (InterruptedException e) {
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() throws InterruptedException {

		if (!bStopped) {
			if (isAlive()) {
				synchronized (this) {
					notifyAll();
					join();
				}
			}
		}
	}

	public void close() {
		try {
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() throws Exception {

		if (bConnected) {
			String data = null;
			if (root.count() > 0) {
				synchronized (root) {
					data = root.toString();
				}

				if (name != null && name.length() > 0) {
					if (!file.isDefault()) {
						file.save(data);
					} else {
						PeerFile tmpFile = new PeerFile();
							tmpFile.open(name);
					}
				}
			}
		}
	}

	public void loadPeers(final String xml) throws Exception {

		try {
			root.clear(null);
			root.parsePeers(xml.getBytes());
			root.computeAverage();
		} catch (Exception x) {
			throw x;
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
				if (bConnected || bStopped) {
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

	public final PeerRoot getRoot() {
		return root;
	}

	public final void setRoot(PeerRoot root) {
		this.root = root;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final void setConnected(boolean bConnected) {
		this.bConnected = bConnected;
	}

	public final void setHold(boolean bHold) {
		this.bHold = bHold;
	}

	public final PeerFile getFile() {
		return file;
	}

	public final void setFile(PeerFile file) {
		this.file = file;
	}

	public final String getPeer() {
		return peer;
	}

	public final void setPeer(String peer) {
		this.peer = peer;
	}

	public void setDirty(final boolean bIsDirty) {
		this.bIsDirty = bIsDirty;
	}
}
