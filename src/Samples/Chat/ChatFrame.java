/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Chat;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.adaptinet.pluginagent.Plugin;

import java.util.*;
import java.util.Map.Entry;


public class ChatFrame extends JFrame {
	private static final long serialVersionUID = 1302027129707511054L;

	Plugin plugin = null;

	String myUsername = null;

	JPanel contentPane;

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel jPanel2 = new JPanel();

	JPanel jPanel3 = new JPanel();

	GridLayout gridLayout1 = new GridLayout();

	JScrollPane jScrollPane1 = new JScrollPane();

	JTextArea jTextConsole = new JTextArea();

	JPanel jPanel1 = new JPanel();

	JLabel jLabel1 = new JLabel();

	JLabel jLabel2 = new JLabel();

	JButton jButtonPost = new JButton();

	JLabel jLabel3 = new JLabel();

	JTextField jTextSend = new JTextField();

	JButton jButtonBroadcast = new JButton();

	JComboBox jComboUsers = new JComboBox();

	public void setPlugin(Plugin plugin, String myUsername) {
		this.plugin = plugin;
		this.myUsername = myUsername;
	}

	public void talk(String from, String text) {
		jTextConsole.append("[" + from + "] " + text + "\n");
		jTextConsole.setCaretPosition(jTextConsole.getDocument().getLength());
	}

	public void resetUserList(Map<String,String> usersMap) {
		jComboUsers.removeAllItems();

		// Add the items to the combo
		Entry<String, String> entry = null;
		Iterator<Entry<String, String>> it = usersMap.entrySet().iterator();
		while (it.hasNext()) {
			entry = it.next();
			String username = (String) entry.getKey();
			jComboUsers.addItem(username);
		}

		// If only one user in the list, make it visible
		if (usersMap.size() > 0)
			jComboUsers.setSelectedIndex(0);
	}

	/** Construct the frame */
	public ChatFrame() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Component initialization */
	private void jbInit() throws Exception {
		// setIconImage(Toolkit.getDefaultToolkit().createImage(ChatFrame.class.getResource("[Your
		// Icon]")));
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(444, 300));
		this.setTitle("Adaptinet Chat");
		jPanel2.setLayout(gridLayout1);
		jLabel1.setRequestFocusEnabled(false);
		jLabel1.setText("Talk to:");
		jTextConsole.setWrapStyleWord(true);
		jTextConsole.setLineWrap(true);
		jTextConsole.setMargin(new Insets(5, 5, 5, 5));
		jTextConsole.setRequestFocusEnabled(false);
		jTextConsole.setEditable(false);
		jLabel2.setText("Text to Send:");
		jButtonPost.setMinimumSize(new Dimension(79, 27));
		jButtonPost.setPreferredSize(new Dimension(79, 27));
		jButtonPost.setText("Post");
		jButtonPost.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonPost_actionPerformed(e);
			}
		});
		jLabel3.setText("  ");
		jTextSend.setMinimumSize(new Dimension(200, 21));
		jTextSend.setPreferredSize(new Dimension(150, 21));
		jButtonBroadcast.setText("Broadcast");
		jButtonBroadcast.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonBroadcast_actionPerformed(e);
			}
		});
		jComboUsers.setFont(new java.awt.Font("SansSerif", 0, 11));
		jComboUsers.setPreferredSize(new Dimension(175, 21));
		contentPane.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jScrollPane1, null);
		jScrollPane1.getViewport().add(jTextConsole, null);
		contentPane.add(jPanel3, BorderLayout.SOUTH);
		jPanel3.add(jLabel2, null);
		jPanel3.add(jTextSend, null);
		jPanel3.add(jLabel3, null);
		jPanel3.add(jButtonPost, null);
		jPanel3.add(jButtonBroadcast, null);
		contentPane.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jLabel1, null);
		jPanel1.add(jComboUsers, null);
	}

	/** Overridden so we can exit when window is closed */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			// Tell the world that we are logging off
			((Chat) plugin).doLogoff();

			// Shutdown the TransCeiver
			plugin.shutdown();
		}
	}

	void jButtonPost_actionPerformed(ActionEvent e) {
		String text = jTextSend.getText();
		if (text.length() == 0)
			text = "No message";
		talk(myUsername, text);

		((Chat) plugin).talkButton((String) jComboUsers.getSelectedItem(),
				text, false);

		jTextSend.setText("");
		jTextSend.requestFocus();
	}

	void jButtonBroadcast_actionPerformed(ActionEvent e) {
		String text = jTextSend.getText();
		if (text.length() == 0)
			text = "No message";
		talk(myUsername, text);

		((Chat) plugin).talkButton((String) jComboUsers.getSelectedItem(),
				text, true);

		jTextSend.setText("");
		jTextSend.requestFocus();
	}
}