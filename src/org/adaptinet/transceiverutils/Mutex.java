/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiverutils;

public class Mutex {
	Thread owner = null;

	public String toString() {
		String name;

		if (owner == null) {
			name = "null";
		} else {
			name = owner.getName();
		}

		return ("<" + super.toString() + "owner:" + name + ">");
	}

	public synchronized void lock() {
		boolean interrupted = false;

		while (owner != null) {
			try {
				wait();
			} catch (InterruptedException ie) {
				interrupted = true;
			}
		}

		owner = Thread.currentThread();

		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	public synchronized void unlock() {
		if (owner != Thread.currentThread()) {
			throw new IllegalMonitorStateException("Not owner");
		}
		owner = null;
		notify();
	}

}
