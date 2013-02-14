/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Quoter;

import java.util.Date;

public class Quote {
	private String symbol = null;

	private String name = null;

	private String last = null;

	private String change = null;

	private String bid = null;

	private String ask = null;

	private String volume = null;

	private Date updated = null;

	public Quote() {
	}

	public Quote(String symbol, String name, String last, String change,
			String bid, String ask, String volume) {
		this.symbol = symbol;
		this.name = name;
		this.last = last;
		this.change = change;
		this.bid = bid;
		this.ask = ask;
		this.volume = volume;
		this.updated = new Date();
	}

	public String getSymbol() {
		return symbol;
	}

	public String getName() {
		return name;
	}

	public String getLast() {
		return last;
	}

	public String getChange() {
		return change;
	}

	public String getBid() {
		return bid;
	}

	public String getAsk() {
		return ask;
	}

	public String getVolume() {
		return volume;
	}

	public Date getUpdated() {
		return updated;
	}
}