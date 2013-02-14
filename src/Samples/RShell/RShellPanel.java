/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.RShell;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class RShellPanel extends JPanel {

	private static final long serialVersionUID = 2929482097689626805L;

	private JFrame frame = null;

	public RShellPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertPeer(String address) {
		peerList.addItem(address);
	}

	public void init(String title) {

		frame = new JFrame(title);
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLocation(50, 50);
		frame.setSize(400, 150);

		try {

			java.net.URL loc = getClass().getResource("RShellPanel.gif");
			ImageIcon logo = new ImageIcon(loc);
			frame.setIconImage(logo.getImage());
		} catch (Exception e) {
		}

		if (peerList.getItemCount() > 0) {
			peerList.setSelectedIndex(0);
		}
		frame.setVisible(true);
	}

	private void jbInit() throws Exception {

		jLabel1.setText("Send To");

		toPanel.setLayout(new FlowLayout());
		jPanel1.setLayout(borderLayout1);
		jLabel2.setText("       ");

		peerList.setEditable(true);
		peerList.setAlignmentX(Component.LEFT_ALIGNMENT);
		/*
		 * peerList.addItemListener(new java.awt.event.ItemListener() { public
		 * void itemStateChanged(ItemEvent e) { boxMethod_itemStateChanged(e); }
		 * });
		 */
		Command.setText("Execute");
		Command.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_Command(e);
			}
		});
		Command.setToolTipText("Execute command on selected peer");

		jTextField1.setPreferredSize(new Dimension(120, 21));
		jTextField1.setText("jTextField1");
		jEditorPane1.setText("jEditorPane1");
		toPanel.add(jLabel1, null);
		toPanel.add(peerList, null);
		toPanel.add(jLabel2, null);
		toPanel.add(jTextField1, null);
		toPanel.add(Command, null);

		this.setLayout(new BorderLayout());
		this.add(toPanel, BorderLayout.NORTH);
		this.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jEditorPane1, BorderLayout.CENTER);

	}

	void do_Exit(ActionEvent e) {
		plugin.shutdown();
	}

	void do_Command(ActionEvent e) {
		plugin.doCommand((String) peerList.getSelectedItem());
	}

	void setPlugin(RShell plugin) {
		this.plugin = plugin;
	}

	public void setCommandText(String text) {
		jEditorPane1.setText(text);
	}

	RShell plugin = null;

	JButton Command = new JButton();

	JPanel toPanel = new JPanel();

	JLabel jLabel1 = new JLabel();

	JPanel jPanel1 = new JPanel();

	BorderLayout borderLayout1 = new BorderLayout();

	JLabel jLabel2 = new JLabel();

	JComboBox peerList = new JComboBox();

	JTextField jTextField1 = new JTextField();

	JEditorPane jEditorPane1 = new JEditorPane();
}