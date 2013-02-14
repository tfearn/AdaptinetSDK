/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Chat;

import javax.swing.*;

import org.adaptinet.adaptinetex.*;
import org.adaptinet.cache.Cache;
import org.adaptinet.messaging.*;
import org.adaptinet.pluginagent.Plugin;

import java.awt.*;
import java.text.DateFormat;
import java.util.*;


public class Chat extends Plugin {
	
	private boolean packFrame = false;
	private ChatFrame frame = null;
	private String myUsername = null;
	private boolean bConnected = false;

	java.util.Map<String, String> usersMap = Collections
			.synchronizedMap(new HashMap<String, String>());

	/** 
	 * Construct the application 
	*/
	public Chat() {
	}

	/**
	 *  This method is called by the transceiver for initialization
	 */
	public void init() {
		/**
		 *  Give the transceiver some time to connect to the network. If the
		 *  connection takes longer than 3 seconds we are probably the 
		 *  only one in the network.
		 */

		// ITransceiver.waitForConnect(3000);

		/**
		 *  Prompt for a username
		 */
		if (myUsername == null) {
			myUsername = JOptionPane.showInputDialog(null,
					"Enter your user name", "Adaptinet Chat Sample",
					JOptionPane.QUESTION_MESSAGE);
			if (myUsername == null) {
				this.shutdown();
				return;
			}
		}

		// Create the frame window
		frame = new ChatFrame();
		if (packFrame)
			frame.pack();
		else
			frame.validate();

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height)
			frameSize.height = screenSize.height;
		if (frameSize.width > screenSize.width)
			frameSize.width = screenSize.width;
		frame.setLocation((screenSize.width - frameSize.width) / 2,
				(screenSize.height - frameSize.height) / 2);
		frame.setVisible(true);

		frame.setPlugin(this, myUsername);
		frame.setTitle("Chat (" + myUsername + ") " + Address.getMyHost() + ":"
				+ Address.getMyPort());

		// Add myself to the users list
		usersMap.put(myUsername, Address.getMyURL());
		frame.resetUserList(usersMap);
		if (bConnected) {
			bConnected = false;
			peerUpdate();
			bConnected = true;
		}
	}

	/**
	 * This method is called by the transceiver for cleanup
	 */
	public void cleanup() {
		doLogoff();
	}

	/**
	 * This method is called when peers are added or removed from the network
	 */
	public void peerUpdate() {

		if (!bConnected) {
			/**
			 *  Tell the world that I have joined the Chat network. 
			 *  Obviously you would want to change this if you are 
			 *  expecting a large network of users.
			 */
			if (myUsername!=null && !myUsername.isEmpty()) {
				frame.resetUserList(usersMap);
				try {
					broadcastMessage("/Chat/joinRequest", myUsername, Address.getMyURL());
				} catch (AdaptinetException e) {
					e.printStackTrace();
				}
			}
			bConnected = true;
		}
	}

	/**
	 *  This method is called by the frame when the talk button is pressed
	 * @param name
	 * @param text
	 * @param broadcast
	 */
	public void talkButton(String name, String text, boolean broadcast) {
		try {
			/**
			 * Find the sendTo address
			 */
			String sendTo = usersMap.get(name);

			try {
				/**
				 * Go ahead and send the message
				 */
				if (broadcast)
					broadcastMessage(sendTo + "/Chat/chat", myUsername, text);
				else
					postMessage(sendTo + "/Chat/chat", myUsername, text);
				
				/**
				 *  Test cache
				 */
				Cache chatCache = Cache.getNamedCache("chat");
				String current = DateFormat.getDateTimeInstance(
			            DateFormat.LONG, DateFormat.LONG).format(new Date());
				chatCache.put(myUsername+":"+current, text);
			} catch (AdaptinetException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called by the frame when the user closes the frame
	 */
	public void doLogoff() {
		try {
			broadcastMessage("/Chat/logoff", myUsername, Address.getMyURL());
		} catch (AdaptinetException e) {
			e.printStackTrace();
		}
	}

	/**
	 *  This method is called by a remote transceiver to notify you
	 *  that they would like to join the Chat network
	 * @param name
	 * @param address
	 */
	public void joinRequest(String name, String address) {
		try {
			/**
			 *  Do we already have a member with the same name but a 
			 *  different address?
			 */
			String mapAddress = usersMap.get(name);
			if (mapAddress != null) {
				if (mapAddress.equals(address)) {
					/**
					 * If the name is the same, then we already have this
					 * member in the list.
					 */
					return;
				} else {
					/**
					 * Attempt to kick them off the chat network
					 */
					try {
						postMessage(address + "/Chat/denyJoin");
					} catch (AdaptinetException e) {
						e.printStackTrace();
					}
				}
				return;
			}

			/**
			 * Add the new member to the map
			 */
			usersMap.put(name, address);
			if (frame != null) {
				frame.resetUserList(usersMap);

				frame.talk("System", name + " has joined the chat network.");
			}
			/**
			 *  Respond back letting the caller know who I am...
			 */
			try {
				postMessage(address + "/Chat/currentMember", myUsername, Address.getMyURL());
			} catch (AdaptinetException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  This method is called by a remote transceiver to notify you that 
	 *  they are logging off.
	 * @param name
	 * @param address
	 */
	public void logoff(String name, String address) {
		try {
			/**
			 *  Make sure they are in our list
			 */
			String mapAddress = usersMap.get(name);
			if (mapAddress != null && mapAddress.equals(address)) {
				usersMap.remove(name);
				frame.resetUserList(usersMap);

				frame.talk("System", name + " has left the chat network.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  This method is called if you are denied membership to the chat network
	 *  because of a non-unique username.
	 */
	public void denyJoin() {
		JOptionPane
				.showMessageDialog(
						null,
						"You are being denied access to the network because of a non-unique username!",
						"Chat Network Access Denied", JOptionPane.ERROR_MESSAGE);

		/**
		 *  Shutdown the transceiver
		 */
		this.shutdown();
	}

	/**
	 *  This method is called by a remote transceiver notifying you 
	 *  that they are already a member of the network. This gives 
	 *  you the ability to update your member list.
	 * @param name
	 * @param address
	 */
	public void currentMember(String name, String address) {
		try {
			/**
			 *  Add the existing member to the map
			 */
			usersMap.put(name, address);
			frame.resetUserList(usersMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  This method is called by a remote transceiver to "chat" with you
	 * @param name
	 * @param text
	 */
	public void chat(String name, String text) {
		String address = usersMap.get(name);
		if (address == null)
			return;
		frame.talk(name, text);
	}
}