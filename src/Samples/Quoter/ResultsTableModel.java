/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Quoter;

import javax.swing.table.*;
import java.util.Vector;

public class ResultsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 5065490678815614801L;

	final String[] columnNames = { "Symbol", "Last", "Change", "Bid", "Ask",
			"Volume", "Updated", "Provider" };

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

	public void addRow(String symbol, String last, String change, String bid,
			String ask, String volume, String updated, String provider) {
		Vector<String> colData = new Vector<String>(8);
		colData.addElement(symbol);
		colData.addElement(last);
		colData.addElement(change);
		colData.addElement(bid);
		colData.addElement(ask);
		colData.addElement(volume);
		colData.addElement(updated);
		colData.addElement(provider);
		rowData.addElement(colData);
		fireTableDataChanged();
	}

	public void deleteRow(int row) {
		rowData.remove(row);
		fireTableRowsDeleted(row, row);
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