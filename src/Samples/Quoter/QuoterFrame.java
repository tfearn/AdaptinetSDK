/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Quoter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.adaptinet.pluginagent.Plugin;

import java.util.*;

import java.util.Date;
import java.text.DateFormat;


public class QuoterFrame extends JFrame {

	private static final long serialVersionUID = 2639264539255262268L;

	Plugin plugin = null;

	JPanel contentPane;

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel jPanel1 = new JPanel();

	JPanel jPanel2 = new JPanel();

	JPanel jPanel3 = new JPanel();

	JScrollPane jScrollPaneQuotes = new JScrollPane();

	GridLayout gridLayout1 = new GridLayout();

	GridLayout gridLayout2 = new GridLayout();

	BorderLayout borderLayout2 = new BorderLayout();

	JButton jButtonAdd = new JButton();

	ResultsTableModel resultsModel = new ResultsTableModel();

	JTable jTableQuotes = new JTable(resultsModel);

	JToolBar jToolBar1 = new JToolBar();

	JButton jButtonDelete = new JButton();

	JScrollPane jScrollPaneStatus = new JScrollPane();

	JTextArea jTextStatus = new JTextArea();

	public QuoterFrame() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	public void setStatus(String status) {
		String text = jTextStatus.getText();
		jTextStatus.append(text.length() == 0 ? status : "\n" + status);
		jTextStatus.setCaretPosition(jTextStatus.getDocument().getLength());
	}

	/** Component initialization */
	private void jbInit() throws Exception {
		// setIconImage(Toolkit.getDefaultToolkit().createImage(QuoterFrame.class.getResource("[Your
		// Icon]")));
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(631, 353));
		this.setTitle("Adaptinet Quoter");
		jPanel2.setLayout(gridLayout1);
		jPanel3.setLayout(gridLayout2);
		jPanel1.setLayout(borderLayout2);
		jButtonAdd.setIcon(new ImageIcon(getClass()
				.getResource("addButton.gif")));
		jButtonAdd.setToolTipText("Add new symbol(s)");
		jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonAdd_actionPerformed(e);
			}
		});
		jButtonDelete.setIcon(new ImageIcon(getClass().getResource(
				"deleteButton.gif")));
		jButtonDelete.setToolTipText("Delete symbol(s)");
		jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonDelete_actionPerformed(e);
			}
		});
		jTableQuotes.setToolTipText("");
		jTextStatus.setWrapStyleWord(true);
		jTextStatus.setLineWrap(true);
		jTextStatus.setMargin(new Insets(5, 5, 5, 5));
		jTextStatus.setBackground(Color.lightGray);
		jTextStatus.setEditable(false);
		jPanel3.setPreferredSize(new Dimension(631, 88));
		contentPane.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jToolBar1, BorderLayout.CENTER);
		jToolBar1.add(jButtonAdd, null);
		jToolBar1.add(jButtonDelete, null);
		contentPane.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jScrollPaneQuotes, null);
		jScrollPaneQuotes.getViewport().add(jTableQuotes, null);
		contentPane.add(jPanel3, BorderLayout.SOUTH);
		jPanel3.add(jScrollPaneStatus, null);
		jScrollPaneStatus.getViewport().add(jTextStatus, null);
		jTableQuotes.getColumnModel().getColumn(0).setPreferredWidth(70);
		jTableQuotes.getColumnModel().getColumn(1).setPreferredWidth(80);
		jTableQuotes.getColumnModel().getColumn(2).setPreferredWidth(80);
		jTableQuotes.getColumnModel().getColumn(3).setPreferredWidth(80);
		jTableQuotes.getColumnModel().getColumn(4).setPreferredWidth(80);
		jTableQuotes.getColumnModel().getColumn(5).setPreferredWidth(100);
		jTableQuotes.getColumnModel().getColumn(6).setPreferredWidth(90);
		jTableQuotes.getColumnModel().getColumn(7).setPreferredWidth(175);
	}

	/** Overridden so we can exit when window is closed */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			plugin.shutdown();
		}
	}

	public void updateQuote(Quote quote, String providerURL) {
		DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

		int nRow = isSymbolMonitored(quote.getSymbol());
		if (nRow >= 0) {
			resultsModel.setValueAt(quote.getLast(), nRow, 1);
			resultsModel.setValueAt(quote.getChange(), nRow, 2);
			resultsModel.setValueAt(quote.getBid(), nRow, 3);
			resultsModel.setValueAt(quote.getAsk(), nRow, 4);
			resultsModel.setValueAt(quote.getVolume(), nRow, 5);
			resultsModel.setValueAt(timeFormat.format(new Date()), nRow, 6);
			resultsModel.setValueAt(providerURL, nRow, 7);
		}
		/*
		 * else { resultsModel.addRow(quote.getSymbol(), quote.getLast(),
		 * quote.getChange(), quote.getBid(), quote.getAsk(), quote.getVolume(),
		 * timeFormat.format(new Date()), providerURL); }
		 */
	}

	void jButtonAdd_actionPerformed(ActionEvent e) {
		// Get new symbols from the user
		String symbols = JOptionPane.showInputDialog(null,
				"Enter symbols separated by spaces", "Add Symbol(s)",
				JOptionPane.INFORMATION_MESSAGE);
		if (symbols == null)
			return;

		symbols = symbols.toUpperCase();

		// Add the symbols to a list
		java.util.Set<String> symbolSet = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(symbols, " ");
		while (st.hasMoreTokens()) {
			String symbol = st.nextToken();

			// Is the symbol already in the table?
			if (isSymbolMonitored(symbol) >= 0)
				continue;

			symbolSet.add(symbol);
		}

		if (symbolSet.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"The symbol(s) entered are already being monitored!",
					"Duplicate Symbols Entered", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Add the symbols to the table
		Iterator<String> it = symbolSet.iterator();
		while (it.hasNext()) {
			String symbol = it.next();
			resultsModel.addRow(symbol, "N/A", "N/A", "N/A", "N/A", "N/A",
					"N/A", "Searching...");
		}

		// Do the rest...
		((Quoter) plugin).addSymbols(new ArrayList<String>(symbolSet));
	}

	void jButtonDelete_actionPerformed(ActionEvent e) {
		if (jTableQuotes.getSelectedRowCount() <= 0)
			return;

		// Build a list of symbols
		int selectedRows[] = new int[jTableQuotes.getSelectedRowCount()];
		selectedRows = jTableQuotes.getSelectedRows();
		java.util.List<String> symbolList = new ArrayList<String>();
		for (int i = 0; i < selectedRows.length; i++) {
			String tablesymbol = (String) resultsModel.getValueAt(
					selectedRows[i], 0);
			symbolList.add(tablesymbol);
		}

		// Remove the selected rows from the table in reverse order
		for (int i = selectedRows.length - 1; i >= 0; i--) {
			resultsModel.deleteRow(selectedRows[i]);
		}

		// Do the rest...
		((Quoter) plugin).deleteSymbols(symbolList);
	}

	private int isSymbolMonitored(String symbol) {
		symbol = symbol.toUpperCase();
		for (int i = 0; i < resultsModel.getRowCount(); i++) {
			String tablesymbol = (String) resultsModel.getValueAt(i, 0);
			if (tablesymbol.equals(symbol.toUpperCase()))
				return i;
		}

		return -1;
	}
}