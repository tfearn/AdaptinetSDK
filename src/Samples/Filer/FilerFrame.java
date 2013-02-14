/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Filer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.adaptinet.pluginagent.Plugin;

import java.util.*;
import java.text.DateFormat;


public class FilerFrame extends JFrame {

	private static final long serialVersionUID = 7615144095725953912L;

	Plugin plugin = null;

	JPanel contentPane;

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel jPanel1 = new JPanel();

	JLabel jLabel1 = new JLabel();

	JTextField jTextFileName = new JTextField();

	JButton jButtonGetFile = new JButton();

	FlowLayout flowLayout1 = new FlowLayout();

	JPanel jPanel2 = new JPanel();

	JScrollPane jScrollPane = new JScrollPane();

	GridLayout gridLayout1 = new GridLayout();

	ResultsTableModel resultsModel = new ResultsTableModel();

	JPanel jPanel3 = new JPanel();

	JTextField jTextStatus = new JTextField();

	JTable jTableResults = new JTable(resultsModel);

	GridLayout gridLayout2 = new GridLayout();

	/** Construct the frame */
	public FilerFrame() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Component initialization */
	private void jbInit() throws Exception {
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(567, 311));
		this.setTitle("Adaptinet Filer");
		jLabel1.setText("File Name:");
		jTextFileName.setPreferredSize(new Dimension(150, 21));
		jButtonGetFile.setText("Get File");
		jButtonGetFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonGetFile_actionPerformed(e);
			}
		});
		jPanel1.setLayout(flowLayout1);
		jPanel2.setLayout(gridLayout1);
		jTextStatus.setBackground(Color.lightGray);
		jTextStatus.setPreferredSize(new Dimension(200, 21));
		jTextStatus.setEditable(false);
		jPanel3.setLayout(gridLayout2);
		jTableResults.setMaximumSize(new Dimension(32767, 32767));
		jTableResults.setShowVerticalLines(false);
		contentPane.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jLabel1, null);
		jPanel1.add(jTextFileName, null);
		jPanel1.add(jButtonGetFile, null);
		contentPane.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jScrollPane, null);
		jScrollPane.getViewport().add(jTableResults, null);
		contentPane.add(jPanel3, BorderLayout.SOUTH);
		jPanel3.add(jTextStatus, null);
		jTableResults.getColumnModel().getColumn(0).setPreferredWidth(80);
		jTableResults.getColumnModel().getColumn(1).setPreferredWidth(125);
		jTableResults.getColumnModel().getColumn(2).setPreferredWidth(75);
		jTableResults.getColumnModel().getColumn(3).setPreferredWidth(85);
		jTableResults.getColumnModel().getColumn(4).setPreferredWidth(100);
	}

	/** Overridden so we can exit when window is closed */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			// Shutdown the plugin cleanly
			plugin.shutdown();
		}
	}

	void jButtonGetFile_actionPerformed(ActionEvent e) {
		String fileName = jTextFileName.getText();
		if (fileName.length() == 0) {
			JOptionPane.showMessageDialog(null, "File name required!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Clear the results table
		resultsModel.clearAll();

		// Do the processing in the Filer object
		((Filer) plugin).getFileButton(fileName);
	}

	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	public String getStatus() {
		return jTextStatus.getText();
	}

	public void setStatus(String status) {
		jTextStatus.setText(status);
	}

	public void setTableStatus(int row, String status) {
		resultsModel.setValueAt(status, row, 4);
	}

	public void enableGetFileButton(boolean enable) {
		jButtonGetFile.setEnabled(enable);
	}

	// Called by the Filer class to display all of the found file
	// responses from the query
	public void displayResponses(java.util.List<FindResult> foundFiles) {
		Iterator<FindResult> it = foundFiles.iterator();
		int i = 0;
		while (it.hasNext()) {
			FindResult findResult = it.next();

			DateFormat dateFormatter = DateFormat.getDateTimeInstance(
					DateFormat.SHORT, DateFormat.SHORT);

			resultsModel.addRow(findResult.getFileName(), findResult
					.getAddress().getHost()
					+ ":" + findResult.getAddress().getPort(), findResult
					.getLength().toString(), dateFormatter.format(new Date(
					findResult.getLastModified().longValue())), "File found");

			i++;
		}

		setStatus(new Integer(i).toString() + "file(s) found.");
	}
}