/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transceiverutils;

import java.util.Vector;

class Event {
	
	long m_time;

	Object m_data;

	EventHandler m_handler;

	Event(long time, EventHandler handler, Object data) {
		m_time = time;
		m_handler = handler;
		m_data = data;
	}
}

public class EventManager extends Thread {

	private Vector<Event> queue = new Vector<Event>();

	private boolean done = false;

	public EventManager() {
		setName("Event Manager");
	}

	public Object registerTimer(long ms, EventHandler handler, Object data) {
		long time = ms + System.currentTimeMillis();
		Event event = new Event(time, handler, data);
		return registerTimer(event);
	}

	public synchronized void stopEventManager() {
		done = true;
		notify();
	}

	public synchronized Object registerTimer(Event newEvent) {
		int lo = 0;
		int hi = queue.size();
		long newTime = newEvent.m_time;
		Event e;
		long midTime;

		if (done) {
			return null;
		}

		if (hi == 0) {
			queue.addElement(newEvent);
		} else {
			while (hi - lo > 0) {
				int mid = (hi + lo) >> 1;
				e = (Event) queue.elementAt(mid);
				midTime = e.m_time;

				if (midTime < newTime) {
					lo = mid + 1;
				} else if (midTime > newTime) {
					hi = mid;
				} else {
					lo = mid;
					break;
				}
			}

			if (lo < hi && ((Event) queue.elementAt(lo)).m_time > newTime) {
				lo += 1;
			}
			queue.insertElementAt(newEvent, lo);
		}
		notify();

		return newEvent;
	}

	public synchronized Object recallTimer(Object timer) {
		int lo = 0;
		int hi = queue.size();
		int limit = hi;
		long destTime = ((Event) timer).m_time;
		Event e;
		long midTime;

		if (hi == 0) {
			return null;
		}

		while (hi - lo > 0) {
			int mid = (hi + lo) >> 1;
			e = (Event) queue.elementAt(mid);
			midTime = e.m_time;

			if (midTime < destTime) {
				lo = mid + 1;
			} else if (midTime > destTime) {
				hi = mid;
			} else {
				lo = mid;
				for (int i = mid - 1; i >= 0; i--) {
					e = (Event) queue.elementAt(i);
					if (e.m_time == midTime) {
						lo = i;
					} else {
						break;
					}
				}
				break;
			}
		}

		while (lo < limit) {
			e = (Event) queue.elementAt(lo);
			if (e.m_time == destTime) {
				if (e == timer) {
					queue.removeElementAt(lo);
					break;
				} else {
					lo += 1;
				}
			} else {
				return null;
			}
		}

		if (lo == 0) {
			notify();
		}
		return timer;
	}

	synchronized Event getNextEvent() {
		while (true) {
			while (queue.size() == 0) {
				if (done == true) {
					return null;
				}

				try {
					wait();
				} catch (InterruptedException e) {
				}
			}

			Event e = (Event) queue.elementAt(0);
			long now = System.currentTimeMillis();
			long dt;

			dt = e.m_time - now;

			if (dt <= 0) {
				queue.removeElementAt(0);
				return e;
			}

			try {
				wait(dt);
			} catch (InterruptedException ex) {
			}
		}
	}

	public void run() {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);
		while (true) {
			Event event = getNextEvent();

			if (done) {
				break;
			}

			try {
				event.m_handler.handleTimerEvent(event.m_data, event.m_time);
			} catch (Exception e) {
				System.err.println(e);
			} catch (Error e) {
				System.err.println(e);
			}
		}
	}
};
