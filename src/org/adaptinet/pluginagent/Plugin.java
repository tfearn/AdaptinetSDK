/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.pluginagent;

import java.util.Iterator;

import org.adaptinet.adaptinetex.PluginException;
import org.adaptinet.messaging.Envelope;
import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.peer.PeerNode;
import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiver.NetworkAgent;


/**
 * The Plugin class is an abstract class from which all client plugins must
 * derive.
 */
public abstract class Plugin extends PluginRoot {

	/**
	 * Gives a plugin a chance to do one time initialization. Method is called
	 * when the plugin is first loaded.
	 */
	public abstract void init();

	/**
	 * Gives a plugin a chance to cleanup any outstanding resources this plugin
	 * may be holding. Method is called just before it is unloaded.
	 */
	public abstract void cleanup();

	/**
	 * This method will unload this plugin.
	 */
	public void unload() {
		unloadPlugin();
	}

	/**
	 * This method will shutdown the Transceiver.
	 */
	public void shutdown() {
		shutdownTransceiver();
	}

	/**
	 * This method allows the another entity to post an error message to the
	 * plugin. The source of the message could be a local failure or from a
	 * remote request.
	 * 
	 * @param uri
	 *            This is the uri of the message when the fault was caused.
	 * @param errorMsg
	 *            This is the message indicating the particular fault.
	 */
	public void error(String uri, String errorMsg) {
		// Default do nothing up to the plugin writer to override
		// System.out.println("error called to " +uri+" error message "
		// +errorMsg);
	}

	/**
	 * This method is for notification of plugins that the status of the peers
	 * has changed
	 */
	public void peerUpdate() {
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 * @return Return value
	 */
	public final Object sendMessage(Message message, Object ... args)
			throws PluginException {

		try {
			return Messenger.sendMessage(message, args);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_POSTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Immediately sends the specified message with the given arguments to the
	 * local transceiver
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void localPostMessage(Message message, Object ... args)
			throws PluginException {

		try {
			Messenger.localPostMessage(message, args);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_LOCALPOSTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void postMessage(Message message)
			throws PluginException {

		try {
			Messenger.postMessage(message);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_POSTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void postMessage(String toUri)
			throws PluginException {

		Message message = new Message(toUri, this.transceiver);
		try {
			Messenger.postMessage(message);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_POSTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param toUri
	 * @param args
	 * @throws PluginException
	 */
	public final void postMessage(String toUri, Object ... args)
			throws PluginException {

		Message message = new Message(toUri, this.transceiver);
		try {
			Messenger.postMessage(message, args);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_POSTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param toUri
	 * @param replyTransceiver
	 * @param args
	 * @throws PluginException
	 */
	public final void postMessage(String toUri, ITransceiver replyTransceiver, Object ... args)
			throws PluginException {

		Message message = new Message(toUri, replyTransceiver);

		try {
			Messenger.postMessage(message, args);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_POSTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Initiates the delivery of the specified message with the given arguments
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void postMessage(Message message, Object ... args)
			throws PluginException {

		try {
			Messenger.postMessage(message, args);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_POSTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Initiates the broadcast of the specified message to each peer of this node
	 * @param toUri
	 * @param args
	 * @throws PluginException
	 */
	public final void broadcastMessage(String toUri, Object ... args)
			throws PluginException {

		Message message = new Message(toUri, this.transceiver);

		try {
			Messenger.broadcastMessage(message, args);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_BROADCASTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Initiates the broadcast of the specified message to each peer of this node
	 * @param toUri
	 * @param replyTransceiver
	 * @param args
	 * @throws PluginException
	 */
	public final void broadcastMessage(String toUri, ITransceiver replyTransceiver, Object ... args)
			throws PluginException {

		Message message = new Message(toUri, replyTransceiver);

		try {

			Messenger.broadcastMessage(message, args);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_BROADCASTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Initiates the broadcast of the specified message to each peer of this
	 * node
	 * 
	 * @param message
	 *            message to deliver
	 * @param args
	 *            message arguments
	 */
	public final void broadcastMessage(Message message, Object ... args)
			throws PluginException {

		try {
			Messenger.broadcastMessage(message, args);
		} catch (Exception e) {
			throw new PluginException(PluginException.SEVERITY_ERROR,
					PluginException.ANT_BROADCASTFAILURE, message.getAddress()
							.getURI());
		}
	}

	/**
	 * Gives a plugin an opportunity to do any message preprocessing that may be
	 * required.
	 * 
	 * @param env
	 *            Envelope of message
	 * @return true if preprocessing was successful and the message is ready to
	 *         be delivered. false if preprocessing failed and message delivery
	 *         should not be attempted.
	 */
	public boolean preProcessMessage(Envelope env) {
		return true;
	}

	/**
	 * Checks this plugin's message queue for available messages.
	 * 
	 * @param bRemove
	 *            Indicates whether the message should be removed from the
	 *            queue.
	 * @return Envelope of available message or null if no message is available
	 */
	public final Envelope peekMessage(boolean bRemove) {
		return agent.peekMessage(bRemove);
	}

	/**
	 * This retrieves the list of peers being maintained by the peer topology.
	 * 
	 * @param bAll
	 *            Indicates whether all the peers should be returned, or only
	 *            the peers that are directly connected
	 * @return Iterator of the peers requested null if there are no peers
	 *         connected
	 */
	public final Iterator<PeerNode> getPeers(boolean all) {

		try {
			return ((NetworkAgent) transceiver.getService("networkagent"))
					.getValues(all);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
