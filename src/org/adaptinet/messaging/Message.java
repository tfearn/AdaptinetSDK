/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */
package org.adaptinet.messaging;

import org.adaptinet.transceiver.ITransceiver;

/**
 * The Message class describes the actual message data transferred between
 * plugins.
 */
public final class Message {

	Address address = null;
	Address replyTo = null;
	String id = null;
	String timestamp = null;
	String hops = null;
	String key = null;
	String certificate = null;

	boolean guaranteed = false;
	
	public int computeUID() {
		StringBuilder sb = new StringBuilder(); 
		sb.append(replyTo.getHost());
		sb.append(replyTo.getPort());
		sb.append(address.getPlugin());
		sb.append(address.getMethod());
		sb.append(timestamp);
		
		return sb.toString().hashCode();
	}

	/**
	 * Constructs an empty Message object
	 */
	public Message() {
		address = new Address();
		replyTo = new Address(ITransceiver.getTransceiver(), false);
		timestamp = Long.toString(System.currentTimeMillis());
	}

	/**
	 * Constructs a Message object with the specified destination address
	 * 
	 * @param toAddress
	 *            destination address of this message
	 */
	public Message(Address toAddress) {
		this.address = new Address(toAddress);
		timestamp = Long.toString(System.currentTimeMillis());
		replyTo = new Address(ITransceiver.getTransceiver(), false);
	}

	/**
	 * Constructs a Message object that will respond to the specified
	 * transceiver and use the given URI as the destination address.
	 * 
	 * @param toUri
	 *            URI of the message destination
	 * @param replyTransceiver
	 *            transciever to reply to
	 */
	public Message(String toUri, ITransceiver replyTransceiver) {
		address = new Address(toUri);
		timestamp = Long.toString(System.currentTimeMillis());
		replyTo = new Address(replyTransceiver, false);
	}

	/**
	 * Constructs a Message object using the given URI as the destination
	 * address.
	 * 
	 * @param uri
	 *            URI of the message destination
	 */
	public Message(String uri) {
		address = new Address(uri);
		timestamp = Long.toString(System.currentTimeMillis());
		replyTo = new Address(ITransceiver.getTransceiver(), false);
	}

	/**
	 * Constucts a Message object initialized with the specifed message.
	 * 
	 * @param msg
	 *            Message to use for initialization
	 */
	public Message(Message msg) {
		if (msg.address != null)
			address = new Address(msg.address);
		else
			address = new Address();
		if (msg.replyTo != null)
			replyTo = new Address(msg.replyTo);
		else
			replyTo = new Address();
		if (msg.id != null)
			id = new String(msg.id);
		if (msg.timestamp != null)
			timestamp = new String(msg.timestamp);
		if (msg.hops != null)
			hops = new String(msg.hops);
		guaranteed = msg.guaranteed;
	}

	/**
	 * Constructs a Message object with a destination address of the given
	 * transceiver.
	 * 
	 * @param trasceiver
	 *            destination transceiver
	 */
	public Message(ITransceiver transceiver) {
		this(transceiver, false);
	}

	/**
	 * Constructs a Message object with a destination address of the given
	 * transceiver.
	 * 
	 * @param trasceiver
	 *            destination transceiver
	 * @param bSecure
	 *            true if a secure protocol will be used (https), false if a
	 *            non-secure protocol will be used (http)
	 */
	public Message(ITransceiver transceiver, boolean bSecure) {
		address = new Address(transceiver, false);
		timestamp = Long.toString(System.currentTimeMillis());
	}

	/**
	 * Retrives the destinaion address of this message
	 * 
	 * @return destination address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * Sets the destination address of this message
	 * 
	 * @param address
	 *            destination address
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	/**
	 * Sets the reply to address of this message
	 * 
	 * @param replyTo
	 *            reply to address
	 */
	public void setReplyTo(Address replyTo) {
		this.replyTo = new Address(replyTo);
	}

	/**
	 * Retrieves the reply to address for this message
	 * 
	 * @return reply to address
	 */
	public Address getReplyTo() {
		return replyTo;
	}

	/**
	 * Retrieves the ID of this message
	 * 
	 * @return message ID
	 */
	public String getID() {
		return id;
	}

	/**
	 * Sets the ID for this message
	 * 
	 * @param id
	 *            ID to set
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Retrieves the timestamp of this message
	 * 
	 * @return message timestamp
	 */
	public String getTimeStamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp of this message.
	 */
	public void setTimeStamp() {
		timestamp = Long.toString(System.currentTimeMillis());
	}

	/**
	 * Sets the timestamp of this message using the specified timestamp
	 * 
	 * @param timestamp
	 *            timestamp to set
	 */
	public void setTimeStamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Retrieves the hop count for this message as a string
	 * 
	 * @return hop count in string form
	 */
	public String getHops() {
		return hops;
	}

	/**
	 * Sets the hop count for this message
	 * 
	 * @param hops
	 *            hop count in string form
	 */
	public void setHops(String hops) {
		this.hops = hops;
	}

	/**
	 * Sets the hop count for this message
	 * 
	 * @param hops
	 *            hop count in int form
	 */
	public void setHops(int hops) {
		this.hops = Integer.toString(hops);
	}

	/**
	 * Retrieves the hop count for this message
	 * 
	 * @return hop count
	 */
	public int getHopCount() {
		if (hops == null)
			return 0;
		return Integer.parseInt(hops);
	}

	/**
	 * Retrieves the certificate of this object
	 * 
	 * @return message certificate in string form
	 */
	public String getCertificate() {
		return certificate;
	}

	/**
	 * Sets the certificate for this message
	 * 
	 * @param certificate
	 *            message certificate in string form
	 */
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	/**
	 * Retrieves the key of this object
	 * 
	 * @return message key in string form
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the key for this message
	 * 
	 * @param key
	 *            message key in string form
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Sets the key for this message
	 * 
	 * @param key
	 *            message key
	 */
	public void setKey(int key) {
		this.key = Integer.toString(key);
	}

	/**
	 * Retrieves the key of this object
	 * 
	 * @return message key
	 */
	public int getKeyType() {
		try {
			return Integer.parseInt(key);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Sets the method name for this messages
	 * 
	 * @param method
	 *            method name
	 */
	public void setMethod(String method) {
		try {
			address.setMethod(method);
		} catch (NullPointerException e) {
		}
	}

	/**
	 * Sets the plugin name for this message
	 * 
	 * @param plugin
	 *            plugin name
	 */
	public void setPlugin(String plugin) {
		try {
			address.setPlugin(plugin);
		} catch (NullPointerException e) {
		}
	}

	/**
	 * Constructs a new reply message for the specified message
	 * 
	 * @param msg
	 *            message to reply to
	 * @return new reply message
	 */
	static public Message createReply(Message msg) {

		Message message = null;

		if (msg != null) {
			if (msg.replyTo != null)
				message = new Message(msg.replyTo);
			else
				message = new Message();
			if (msg.address != null)
				message.replyTo = new Address(msg.address);
			else
				message.replyTo = new Address();

			if (msg.id != null)
				message.id = new String(msg.id);
			if (msg.timestamp != null)
				message.timestamp = new String(msg.timestamp);
			if (msg.hops != null)
				message.hops = new String(msg.hops);
			return message;
		}

		return new Message();
	}

	public Address hop() {
		Address to = address.hop();
		if (to != null) {
			replyTo.setRoute(to);
			return to;
		}
		return address;
	}

	public Address getRoute() {
		Address to = address.getRoute();
		if (to != null) {
			return to;
		}
		return address;
	}

	public boolean getGuaranteed() {
		return guaranteed;
	}

	public void setGuaranteed(boolean guaranteed) {
		this.guaranteed = guaranteed;
	}

	public String getURL() {
		String ret = null;
		if (address != null) {
			ret = address.getURL();
		}
		return ret;
	}
}
