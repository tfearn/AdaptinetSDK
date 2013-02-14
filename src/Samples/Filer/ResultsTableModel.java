/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Filer;

import javax.swing.table.*;
import java.util.Vector;

public class ResultsTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -6759708250075124144L;

	final String[] columnNames = { "File Name", "Peer", "File Length",
			"File Date", "Status" };

	Vector<Vector<String>> rowData = new Vector<Vector<String>>(10);

	public ResultsTableModel() {
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return rowData.size();
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		Object ret = null;
		try {
			Vector<String> colData = rowData.elementAt(row);
			ret = colData.elementAt(col);
		} catch (Exception e) {
		}
		return ret;
	}

	public void addRow(String fileName, String peer, String length,
			String lastModified, String status) {
		Vector<String> colData = new Vector<String>(5);
		colData.addElement(fileName);
		colData.addElement(peer);
		colData.addElement(length);
		colData.addElement(lastModified);
		colData.addElement(status);
		rowData.addElement(colData);
		fireTableDataChanged();
	}

	public void setValueAt(String value, int row, int col) {
		Vector<String> colData = rowData.elementAt(row);
		colData.setElementAt(value, col);
		fireTableCellUpdated(row, col);
	}

	public void clearAll() {
		rowData = new Vector<Vector<String>>();
		fireTableDataChanged();
	}
}