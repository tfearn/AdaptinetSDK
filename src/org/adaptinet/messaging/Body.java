/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.messaging;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class Body {

	private List<Object> _content = null;

	public Body() {
	}

	final public Object get(int i) {
		try {
			return _content.get(i);
		} catch (Exception e) {
		}
		return null;
	}

	final public void add(Object newValue) {
		try {
			_content.add(newValue);
		} catch (Exception e) {
		}
	}

	final public int getcontentSize() {
		try {
			return _content.size();
		} catch (Exception e) {
		}
		return 0;
	}

	final public Object[] getcontentArray() {
		try {
			return _content.toArray();
		} catch (Exception e) {
		}
		return null;
	}

	final public void setcontentArray(Object[] objs) {
		try {
			_content = new ArrayList<Object>(objs.length);
			for (int i = 0; i < objs.length; i++)
				_content.add(objs[i]);
		} catch (Exception e) {
		}
	}

	final public Iterator<Object> getcontentElements() {
		try {
			return _content.iterator();
		} catch (Exception e) {
		}
		return null;
	}

	final public void setcontent(List<Object> newValue) {
		_content = newValue;
	}

	public List<Object> getcontent() {
		return _content;
	}
}
