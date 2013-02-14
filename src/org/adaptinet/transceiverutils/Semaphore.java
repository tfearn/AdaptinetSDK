/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiverutils;

public class Semaphore {
	int count = 0;

	public Semaphore() {
		count = 0;
	}

	public Semaphore(int i) {
		count = i;
	}

	public synchronized void init(int i) {
		count = i;
	}

	public synchronized void semWait() {
		boolean interrupted = false;
		while (count == 0) {
			try {
				wait();
			} catch (InterruptedException ie) {
				interrupted = true;
			}
		}

		count--;
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	public synchronized void semPost() {
		count++;
		notify();
	}
}
