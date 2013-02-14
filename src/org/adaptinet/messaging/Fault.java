/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */
package org.adaptinet.messaging;

import java.util.Vector;
import java.util.Enumeration;

public class Fault {

	private Vector<String> _faultcode = new Vector<String>();

	private Vector<String> _faultString = new Vector<String>();

	private Vector<String> _faultactor = new Vector<String>();

	private Vector<String> _detail = new Vector<String>();

	private String _contentData;
	
	public int getfaultcodeSize() {
		return _faultcode.size();
	}

	public Enumeration<String> getfaultcodeElements() {
		return _faultcode.elements();
	}

	public String getfaultcode(int i) {
		return _faultcode.elementAt(i);
	}

	public void setfaultcode(String newValue) {
		_faultcode.addElement(newValue);
	}

	public int getfaultStringSize() {
		return _faultString.size();
	}

	public Enumeration<String> getfaultStringElements() {
		return _faultString.elements();
	}

	public String getfaultString(int i) {
		return _faultString.elementAt(i);
	}

	public void setfaultString(String newValue) {
		_faultString.addElement(newValue);
	}

	public int getfaultactorSize() {
		return _faultactor.size();
	}

	public Enumeration<String> getfaultactorElements() {
		return _faultactor.elements();
	}

	public String getfaultactor(int i) {
		return _faultactor.elementAt(i);
	}

	public void setfaultactor(String newValue) {
		_faultactor.addElement(newValue);
	}

	public int getdetailSize() {
		return _detail.size();
	}

	public Enumeration<String> getdetailElements() {
		return _detail.elements();
	}

	public String getdetail(int i) {
		return _detail.elementAt(i);
	}

	public void setdetail(String newValue) {
		_detail.addElement(newValue);
	}

	public String getContentData() {
		return _contentData;
	}

	public void setContentData(String newValue) {
		_contentData = newValue;
	}
}
