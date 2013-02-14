/**
 *	Copyright (C), 1999-2012 Adaptinet Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Quoter;

import java.util.*;
import java.net.*;
import java.io.*;

public class Yahoo {
	public Yahoo() {
	}

	public List<Quote> getQuotes(List<String> symbols) {
		try {
			return this.parseData(getRawDataFromYahoo(getURL(symbols)));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns an URL for an enumeration of symbols to retrieve from Yahoo!
	 */
	private URL getURL(List<String> symbols) throws MalformedURLException {
		StringBuffer sb = new StringBuffer(128);
		sb.append("http://quote.yahoo.com/d/quotes.csv?f=snl1c1bav&s=");
		boolean bFirstTime = true;

		Iterator<String> it = symbols.iterator();
		while (it.hasNext()) {
			String symbol = it.next();
			if (!bFirstTime)
				sb.append("+");
			sb.append(symbol);
			bFirstTime = false;
		}
		return new URL(sb.toString());
	}

	/**
	 * Actually retrieves the quotes as a Vector of Strings (comma separated
	 * values)
	 */
	private Vector<String> getRawDataFromYahoo(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		BufferedReader br = new BufferedReader(new InputStreamReader(connection
				.getInputStream()));
		Vector<String> v = new Vector<String>();
		String line = "";
		while (line != null) {
			line = br.readLine();
			if (line != null)
				v.addElement(line);
		}
		br.close();

		return v;
	}

	/**
	 * Parse the returned Vector of Strings into a list of Quote objects
	 */
	private List<Quote> parseData(Vector<String> data) {
		List<Quote> quotes = new ArrayList<Quote>();

		Enumeration<String> e = data.elements();
		while (e.hasMoreElements()) {
			StringTokenizer st = new StringTokenizer(e.nextElement(), ",");

			String ticker = new String((String) st.nextElement());
			ticker = ticker.substring(1, ticker.length() - 1); // remove the
																// quotes
			ticker = ticker.trim(); // remove whitespace
			String fullname = (String) st.nextElement();
			fullname = fullname.substring(1, fullname.length() - 1); // remove
																		// the
																		// quotes
			fullname = fullname.trim(); // remove whitespace
			String last = (String) st.nextElement();
			String change = (String) st.nextElement();
			String bid = (String) st.nextElement();
			String ask = (String) st.nextElement();
			String volume = (String) st.nextElement();

			Quote quote = new Quote(ticker, fullname, last, change, bid, ask,
					volume);
			quotes.add(quote);
		}

		return quotes;
	}
}