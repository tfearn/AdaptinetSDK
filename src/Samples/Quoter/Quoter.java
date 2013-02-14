/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Quoter;

import javax.swing.*;

import org.adaptinet.adaptinetex.*;
import org.adaptinet.messaging.*;
import org.adaptinet.pluginagent.Plugin;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;


public class Quoter extends Plugin {
	boolean packFrame = false;

	QuoterFrame frame = null;

	Properties properties = new Properties();

	String myURL = null;

	// Contains a list of symbols that I am watching
	java.util.Set<String> mySymbolSet = Collections.synchronizedSet(new HashSet<String>());

	// Contains a list of symbols that are retrieved from Yahoo
	java.util.Set<String> getQuoteSet = Collections.synchronizedSet(new HashSet<String>());

	// Contains a list of all quotes that are being pushed to us from other
	// transceivers.
	java.util.List<Provider> providersList = Collections
			.synchronizedList(new ArrayList<Provider>());

	// Contains a list of all remote transceivers that are currently subscribed
	// to us for quotes.
	java.util.List<Object> subscribersList = Collections
			.synchronizedList(new ArrayList<Object>());

	// Contains a list of the current outstanding subscribe requests & responses
	java.util.List<String> subscribeRequestList = Collections
			.synchronizedList(new ArrayList<String>());

	java.util.Map<String, Provider> subscribeResponseMap = Collections
			.synchronizedMap(new HashMap<String, Provider>());

	static class Subscriber {
		private String url = null;

		private String symbol = null;

		public Subscriber(String url, String symbol) {
			this.url = url;
			this.symbol = symbol;
		}

		public String getURL() {
			return url;
		}

		public String getSymbol() {
			return symbol;
		}
	}

	static class Provider {
		private String url = null;

		private String symbol = null;

		private int nbrCurrentSubscribers = 0;

		private long lastUpdate = 0;

		public Provider(String url, String symbol, int nbrCurrentSubscribers) {
			this.url = url;
			this.symbol = symbol;
			this.nbrCurrentSubscribers = nbrCurrentSubscribers;
			this.lastUpdate = 0;
		}

		public String getURL() {
			return url;
		}

		public String getSymbol() {
			return symbol;
		}

		public int getNbrCurrentSubscribers() {
			return nbrCurrentSubscribers;
		}

		public long getLastUpdate() {
			return lastUpdate;
		}

		public void setLastUpdate(long millis) {
			this.lastUpdate = millis;
		}
	}

	public Quoter() {
	}

	// This method is called by the transceiver for initialization
	public void init() {
		// Load the properties file
		boolean propertiesFound = true;
		try {
			// Get the properties file
			File file = findFileFromClassPath("quoter.properties");
			if (file == null) {
				propertiesFound = false;
				return;
			}
			try {
				properties.load(new FileInputStream(file));
			} catch (Exception e) {
				propertiesFound = false;
				e.printStackTrace();
			}
		} finally {
			if (!propertiesFound) {
				JOptionPane.showMessageDialog(null,
						"Error opening quoter.properties file!",
						"Properties File Error", JOptionPane.ERROR_MESSAGE);
				this.shutdown();
				return;
			}
		}

		// Load the frame
		frame = new QuoterFrame();
		if (packFrame)
			frame.pack();
		else
			frame.validate();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height)
			frameSize.height = screenSize.height;
		if (frameSize.width > screenSize.width)
			frameSize.width = screenSize.width;
		frame.setLocation((screenSize.width - frameSize.width) / 2,
				(screenSize.height - frameSize.height) / 2);
		frame.setVisible(true);

		frame.setPlugin(this);

		// Store my URL
		Address address = new Address(this.transceiver);
		myURL = address.getURL();

		frame.setTitle("Adaptinet Quoter - " + address.getHost() + ":"
				+ address.getPort());

		// Create a thread for retrieving quotes
		new Thread(new Runnable() {
			public void run() {
				getQuoteThread();
			}
		}).start();

		// Create a thread to monitor the providers
		new Thread(new Runnable() {
			public void run() {
				monitorProviderThread();
			}
		}).start();
	}

	// This method is called by the transceiver for cleanup
	public void cleanup() {
	}

	// This method is called by the transceiver when an error occurs
	// sending messages to other transceivers
	public void error(String uri, String errorMsg) {
		Address address = new Address(uri);

		// If an error occured on a quote update, the receiving transceiver
		// probably no longer exists, remove them from the subscribers list
		if (address.getMethod().equals("quoteUpdate")) {
			// Find the subscriber in the list
			Iterator<Object> it = subscribersList.iterator();
			while (it.hasNext()) {
				Subscriber subscriber = (Subscriber) it.next();
				if (subscriber.getURL().equals(address.getURL())) {
					it.remove();
				}
			}
		}
	}

	// This thread monitors the "getQuoteList" and periodically wakes up
	// to retrieve and distribute quotes.
	private void getQuoteThread() {
		Long getQuoteSleep = new Long(properties.getProperty("GetQuoteSleep",
				"10000"));

		while (true) {
			try {
				Thread.sleep(getQuoteSleep.longValue());
			} catch (InterruptedException e) {
			}

			// Wake up

			// Retrieve quotes from Yahoo
			if (getQuoteSet.size() > 0) {
				java.util.List<Quote> quoteList = new Yahoo().getQuotes(new ArrayList<String>(
						getQuoteSet));
				if (quoteList == null)
					continue;

				distributeQuotes(quoteList, true);
			}
		}
	}

	synchronized private void distributeQuotes(java.util.List<Quote> quoteList,
			boolean toMyself) {
		java.util.Set<Object> urlSet = new HashSet<Object>();

		// Build a unique list of subscriber URLS
		Iterator<Object> it = subscribersList.iterator();
		while (it.hasNext())
			urlSet.add(((Subscriber) it.next()).getURL());

		// Distribute quotes to our subscribers. Cycle through each
		// subscriber and deliver quotes
		it = urlSet.iterator();
		while (it.hasNext()) {
			String subscriberURL = (String) it.next();

			// Skip distribution to ourselves if so desired
			if (!toMyself && subscriberURL.equals(myURL))
				continue;

			// Find the quotes that the subscriber wants
			java.util.List<Quote> quotesWantedList = new ArrayList<Quote>();
			Iterator<Object> it2 = subscribersList.iterator();
			while (it2.hasNext()) {
				Subscriber subscriber = (Subscriber) it2.next();
				if (subscriber.getURL().equals(subscriberURL)) {
					// What does he want?
					String symbol = subscriber.getSymbol();

					// Find it in the quote list returned by Yahoo
					Iterator<Quote> it3 = quoteList.iterator();
					while (it3.hasNext()) {
						Quote quote = it3.next();
						if (quote.getSymbol().equals(symbol))
							quotesWantedList.add(quote);
					}
				}
			}

			// Convert the quotes wanted to arrays
			String[] symbolArray = new String[quotesWantedList.size()];
			String[] lastArray = new String[quotesWantedList.size()];
			String[] changeArray = new String[quotesWantedList.size()];
			String[] bidArray = new String[quotesWantedList.size()];
			String[] askArray = new String[quotesWantedList.size()];
			String[] volumeArray = new String[quotesWantedList.size()];
			for (int i = 0; i < quotesWantedList.size(); i++) {
				Quote quote = quotesWantedList.get(i);
				symbolArray[i] = quote.getSymbol();
				lastArray[i] = quote.getLast();
				changeArray[i] = quote.getChange();
				bidArray[i] = quote.getBid();
				askArray[i] = quote.getAsk();
				volumeArray[i] = quote.getVolume();
			}

			// Send the quotes to the subscriber
			Object[] args = new Object[7];
			args[0] = myURL;
			args[1] = symbolArray;
			args[2] = lastArray;
			args[3] = changeArray;
			args[4] = bidArray;
			args[5] = askArray;
			args[6] = volumeArray;
			Message message = new Message(subscriberURL + "/Quoter/quoteUpdate");
			try {
				if (myURL.equals(subscriberURL)) {
					quoteUpdate(myURL, symbolArray, lastArray, changeArray,
							bidArray, askArray, volumeArray);
				} else
					postMessage(message, args);
			} catch (AdaptinetException e) {
				e.printStackTrace();
			}
		}
	}

	// This thread monitors our provider list
	private void monitorProviderThread() {
		try {
			while (true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}

				// Wake up

				if (providersList.size() == 0)
					continue;

				// Walk the providers list
				java.util.List<String> symbolsLostList = new ArrayList<String>();
				Iterator<Provider> it = providersList.iterator();
				while (it.hasNext()) {
					Provider provider = it.next();

					if (provider.getLastUpdate() == 0)
						continue;

					// Has the provider timed out?
					Long timeout = new Long(properties.getProperty(
							"ProviderTimeout", "30000"));
					if (System.currentTimeMillis() - provider.getLastUpdate() > timeout
							.longValue()) {
						// Inform the provider we no longer want the symbol,
						// just
						// in case he is still running.
						String[] symbols = new String[1];
						symbols[0] = provider.getSymbol();
						Object[] args = new Object[2];
						args[0] = myURL;
						args[1] = symbols;
						Message message = new Message(
								"http://host:port/Quoter/unsubscribe",
								this.transceiver);
						postMessage(message, args);

						// Keep track of the symbol
						symbolsLostList.add(provider.getSymbol());

						// Remove him from our provider list
						it.remove();
					}
				}

				// Query for the new symbols
				if (symbolsLostList.size() > 0) {
					addSymbols(symbolsLostList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Called when new symbols need to be monitored and shown on our GUI
	synchronized public void addSymbols(java.util.List<String> symbols) {
		frame.setStatus("Querying peers for symbol(s)...");

		// Add the requested symbols to the list
		subscribeRequestList.clear();
		subscribeRequestList.addAll(symbols);
		subscribeResponseMap.clear();

		// Convert the quotes wanted to an array
		String[] symbolArray = new String[symbols.size()];
		for (int i = 0; i < symbols.size(); i++)
			symbolArray[i] = symbols.get(i);

		// Broadcast a message to find a provider or providers
		Object[] args = new Object[2];
		args[0] = myURL;
		args[1] = symbolArray;
		try {
			// Do the broadcast
			Message message = new Message(
					"http://host:port/Quoter/subscribeRequest",
					this.transceiver);
			message.setHops(properties.getProperty("SubscribeHops", "3"));
			broadcastMessage(message, args);

			// Also post it to myself
			Message message2 = new Message(myURL + "/Quoter/subscribeRequest",
					this.transceiver);
			postMessage(message2, args);
		} catch (AdaptinetException e) {
			e.printStackTrace();
		}

		// Create a thread to monitor the subscription responses
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(Integer.parseInt(properties.getProperty(
							"SubscribeWaitTimeout", "3000")));
				} catch (InterruptedException e) {
				}

				frame.setStatus("Confirming with provider(s)...");

				// Cycle through the response list and confirm with the
				// providers.
				// If a provider does not exist for a requested symbol, we will
				// become the provider.
				Iterator<String> it = subscribeRequestList.iterator();
				while (it.hasNext()) {
					String symbol = it.next();
					Provider provider = subscribeResponseMap
							.get(symbol);
					if (provider != null) {
						// Confirm with the provider
						Object[] args = new Object[2];
						args[0] = myURL;
						args[1] = provider.getSymbol();
						Message message = new Message(provider.getURL()
								+ "/Quoter/subscribeConfirm");
						try {
							// Are we local (calling ourselves)
							if (myURL.equals(provider.getURL())) {
								subscribeConfirm(myURL, provider.getSymbol());
								System.out.println(symbol
										+ " will be provided for locally.");
							} else {
								postMessage(message, args);
								System.out.println(symbol
										+ " will be provided by "
										+ provider.getURL());
							}

							providersList.add(provider);
						} catch (AdaptinetException e) {
							e.printStackTrace();
						}

						// Keep track of what we are monitoring
						mySymbolSet.add(provider.getSymbol());
					} else {
						System.out
								.println("No providers were found for symbol: "
										+ symbol);
					}
				}

				frame
						.setStatus("Confirmation complete.  Quotes are being pushed to us.");
			}
		}).start();
	}

	// Called to remove symbols that we are monitoring
	public void deleteSymbols(java.util.List<String> symbols) {
		String status = "Deleting symbol(s): ";
		Iterator<String> it = symbols.iterator();
		while (it.hasNext())
			status += (String) it.next() + " ";
		status = status.trim();
		status += "...";
		frame.setStatus(status);

		// Cycle through our list of providers and unsubscribe
		it = symbols.iterator();
		while (it.hasNext()) {
			String symbol = (String) it.next();

			// If we have subscribers other than ourself, skip the unsubscribe
			boolean subscriberFound = false;
			Iterator<Object> itSub = subscribersList.iterator();
			while (itSub.hasNext()) {
				Subscriber subscriber = (Subscriber) itSub.next();

				// If I am the subscriber, skip it...
				if (subscriber.getURL().equals(myURL))
					continue;

				if (subscriber.getSymbol().equals(symbol))
					subscriberFound = true;
			}
			if (subscriberFound)
				continue;

			// For each symbol, cycle through the providers
			Iterator<Provider> itProv = providersList.iterator();
			while (itProv.hasNext()) {
				Provider provider = itProv.next();
				if (provider.getSymbol().equals(symbol)) {
					// Unsubscribe
					Object args[] = new Object[2];
					String symbolArray[] = new String[1];
					symbolArray[0] = symbol;
					args[0] = myURL;
					args[1] = symbolArray;
					Message message = new Message(provider.getURL()
							+ "/Quoter/unsubscribe");
					try {
						postMessage(message, args);
					} catch (AdaptinetException e) {
						e.printStackTrace();
					}

					// Remove the provider from our list
					itProv.remove();
				}
			}
		}
	}

	// This method is called by a remote transceiver for symbol subscription
	public void subscribeRequest(String subscriberURL, String[] symbols) {
		String status = "Received a request for ";
		for (int i = 0; i < symbols.length; i++)
			status += symbols[i] + " ";
		status = status.trim();
		status += "...";
		frame.setStatus(status);

		try {
			// Cycle through the list of current subscribers and see if we are
			// already providing the symbols requested
			java.util.Set<String> providedSymbols = new HashSet<String>();
			java.util.Set<String> uniqueSubscribers = new HashSet<String>();
			for (int i = 0; i < symbols.length; i++) {
				Iterator<Object> it = subscribersList.iterator();
				while (it.hasNext()) {
					// Are we providing the symbol already?
					Subscriber subscriber = (Subscriber) it.next();
					if (subscriber.getSymbol().equals(symbols[i]))
						providedSymbols.add(symbols[i]);
					uniqueSubscribers.add(subscriber.getURL());
				}
			}

			// Add our providers symbols to the list (we can redistribute our
			// provider quotes as well).
			for (int i = 0; i < symbols.length; i++) {
				Iterator<Provider> it = providersList.iterator();
				while (it.hasNext()) {
					Provider provider = it.next();
					if (provider.getSymbol().equals(symbols[i]))
						providedSymbols.add(symbols[i]);
				}
			}

			// Can we respond?
			if (subscriberURL.equals(myURL) || providedSymbols.size() > 0) {
				Object[] args = new Object[3];
				args[0] = myURL;

				// If the request is from me, I must offer to provide all
				// of the requested symbols.
				if (subscriberURL.equals(myURL))
					args[1] = symbols;
				else {
					String[] symbolArray = new String[providedSymbols.size()];
					Iterator<String> it = providedSymbols.iterator();
					int i = 0;
					while (it.hasNext())
						symbolArray[i++] = it.next();
					args[1] = symbolArray;
				}

				// If the request is from me, set the current subscribers
				// parameter high. This will force the transceiver to get
				// quotes from the network rather than subscribing to Yahoo.
				if (subscriberURL.equals(myURL))
					args[2] = new Integer(65535);
				else
					args[2] = new Integer(uniqueSubscribers.size());

				Message message = new Message(subscriberURL
						+ "/Quoter/subscribeResponse");
				try {
					postMessage(message, args);
				} catch (AdaptinetException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// This method is a response to a subscription request (for a symbol)
	// from a remote transceiver.
	public void subscribeResponse(String providerURL, String[] symbols,
			int nbrCurrentSubscribers) {
		try {
			// Cycle through the symbols and check if our existing provider for
			// each symbol is suitable
			for (int i = 0; i < symbols.length; i++) {
				// Check for an existing provider for the symbol
				boolean providerSufficient = true;

				Provider provider = subscribeResponseMap
						.get(symbols[i]);
				if (provider != null) {
					// If the new potential provider is better, remove the old
					// one from the list
					if (nbrCurrentSubscribers < provider
							.getNbrCurrentSubscribers()) {
						providerSufficient = false;
						subscribeResponseMap.remove(symbols[i]);
					}
				}

				// If the current provider is not sufficient or we did not
				// find a provider, create a new one
				if (!providerSufficient || provider == null) {
					subscribeResponseMap.put(symbols[i], new Provider(
							providerURL, symbols[i], nbrCurrentSubscribers));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// This method is called by the subscriber to inform the provider (me)
	// that I have been chosen as the provider for the list of symbols
	// passed.
	public boolean subscribeConfirm(String subscriberURL, String symbol) {
		frame.setStatus("I am providing " + symbol + " to " + subscriberURL
				+ ".");

		try {
			Subscriber subscriber = new Subscriber(subscriberURL, symbol);
			subscribersList.add(subscriber);

			// Is a provider providing this symbol?
			boolean haveProvider = false;
			Iterator<Provider> it = providersList.iterator();
			while (it.hasNext()) {
				Provider provider = it.next();
				if (provider.getSymbol().equals(symbol)) {
					haveProvider = true;
					break;
				}
			}

			// If no provider, add it to our list
			if (!haveProvider)
				getQuoteSet.add(symbol);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// This method is called when a remote transciever wants to unsubscribe
	// to quotes
	public void unsubscribe(String subscriberURL, String[] symbols) {
		try {
			String status = "Received an unsubscribe for ";
			for (int i = 0; i < symbols.length; i++)
				status += symbols[i] + " ";
			status = status.trim();
			status += "...";
			frame.setStatus(status);

			for (int i = 0; i < symbols.length; i++) {
				// Cycle through the subscribers list
				Iterator<Object> it = subscribersList.iterator();
				while (it.hasNext()) {
					Subscriber subscriber = (Subscriber) it.next();

					// If the symbol and the URL match, remove the subscriber
					if (subscriber.getSymbol().equals(symbols[i])
							&& subscriber.getURL().equals(subscriberURL)) {
						it.remove();
					}
				}
			}

			cleanupQuoteRetrieval();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// This method checks the quote retreival list to ensure that there
	// are still subscribers
	private void cleanupQuoteRetrieval() {
		// Cycle through the current list
		Iterator<String> it = getQuoteSet.iterator();
		while (it.hasNext()) {
			String symbol = it.next();

			// Cycle through the subscribers
			boolean subscriberFound = false;
			Iterator<Object> itSub = subscribersList.iterator();
			while (itSub.hasNext()) {
				Subscriber subscriber = (Subscriber) itSub.next();
				if (subscriber.getSymbol().equals(symbol)) {
					subscriberFound = true;
					break;
				}
			}

			// No subscriber? Remove the symbol from the set
			if (!subscriberFound)
				it.remove();
		}
	}

	// This method is called by the quote providers
	public void quoteUpdate(String providerURL, String[] symbol, String[] last,
			String[] change, String[] bid, String[] ask, String[] volume) {
		// Display the quote
		for (int i = 0; i < symbol.length; i++) {
			// Find the provider
			Iterator<Provider> it = providersList.iterator();
			while (it.hasNext()) {
				Provider provider = it.next();
				if (provider.getURL().equals(providerURL)
						&& provider.getSymbol().equals(symbol[i])) {
					// Record the update time
					provider.setLastUpdate(System.currentTimeMillis());
					break;
				}
			}

			// Show the update
			if (mySymbolSet.contains(symbol[i])) {
				frame.updateQuote(new Quote(symbol[i], "", last[i], change[i],
						bid[i], ask[i], volume[i]), providerURL);
			}
		}

		// Distribute the quote to our subscribers

		// First we must build a quote list from the arrays passed in
		java.util.List<Quote> quoteList = new ArrayList<Quote>();
		for (int i = 0; i < symbol.length; i++) {
			Quote quote = new Quote(symbol[i], "", last[i], change[i], bid[i],
					ask[i], volume[i]);
			quoteList.add(quote);
		}

		distributeQuotes(quoteList, false);
	}

	private File findFileFromClassPath(String name) {
		String classpath = System.getProperty("java.class.path");
		File file = null;
		try {
			StringTokenizer classToken = new StringTokenizer(classpath,
					File.pathSeparator);
			while (classToken.hasMoreTokens()) {
				String filename = classToken.nextToken();
				String filenameLower = filename.toLowerCase();
				if (filenameLower.endsWith(".zip")
						|| filenameLower.endsWith(".jar")) {
					try {
						JarFile jarFile = new JarFile(filename);
						JarEntry j = jarFile.getJarEntry(name);
						if (j != null) {
							file = new File(filename);
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					if (!filename.endsWith(File.separator)) {
						filename += File.separatorChar;
					}

					File f = new File(filename + name);
					if (f.exists()) {
						file = f;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return file;
	}
}