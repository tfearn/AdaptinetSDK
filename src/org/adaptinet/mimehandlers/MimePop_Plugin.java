/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.mimehandlers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Properties;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.messaging.Envelope;
import org.adaptinet.messaging.MessageParser;
import org.adaptinet.parser.InputSource;
import org.adaptinet.parser.XMLReader;
import org.adaptinet.pluginagent.PluginAgent;
import org.adaptinet.pluginagent.PluginFactory;
import org.adaptinet.transceiver.ITransceiver;


public class MimePop_Plugin {

	static private String server = null;

	private String username = null;

	private String password = null;

	private int polltime = 600000;

	private int port = 110;

	private boolean delete = false;

	private boolean securelogin = false;

	private Socket sock;

	private ITransceiver transceiver = ITransceiver.getTransceiver();

	private Thread mailThread = null;

	private boolean bContinue = true;

	private PluginAgent plugin = null;

	private boolean debug = true;

	private BufferedReader in;

	private PrintStream out;

	public static MimePop_Plugin startMailReader() {

		MimePop_Plugin pop = null;
		try {
			Properties properties = new Properties();
			File file = ITransceiver.findFile("mail.properties");

			if (properties.getProperty("activate", "false").equals("true")) {
				properties.load(new FileInputStream(file));
				pop = new MimePop_Plugin();
				pop.setUsername(properties.getProperty("Username", "scott"));
				pop.setPassword(properties.getProperty("password", "tiger"));
				pop.setServer(properties.getProperty("mailserver",
						MimePop_Plugin.server));
				pop.setPolltime(Integer.parseInt(properties.getProperty(
						"polltime", "60000")));
				pop.setPort(Integer.parseInt(properties.getProperty("port",
						"110")));
				pop.setDelete(Boolean.getBoolean(properties.getProperty(
						"delete", "true")));
				pop.setSecurelogin(Boolean.getBoolean(properties.getProperty(
						"securelogin", "false")));
				pop.mailReader();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return pop;
	}

	public MimePop_Plugin() {
		MimePop_Plugin.server = "mail.adaptinet.net";
	}

	public MimePop_Plugin(String server, int port) {
		MimePop_Plugin.server = server;
		this.port = port;
	}

	public void stop() {
		bContinue = false;
	}

	public void mailReader() {

		try {
			if (mailThread == null) {
				mailThread = new Thread() {
					public void run() {
						try {
							while (bContinue) {
								// Read the mail see what we get
								readMail();

								synchronized (mailThread) {
									try {
										mailThread.wait(polltime);
									} catch (InterruptedException ex) {
										break;
									}
								}
							}
						} catch (Exception e) {
							AdaptinetException ae = new AdaptinetException(
									AdaptinetException.SEVERITY_ERROR,
									AdaptinetException.GEN_MESSAGE);
							ae.logMessage(e);
							e.printStackTrace();
						}
					}
				};
			}
			if (mailThread.isAlive() == false)
				mailThread.start();
		} catch (Exception e) {
		}
	}

	public void readMail() {

		try {
			open();
			if (login() == false)
				return;

			String data = read();
			write("STAT");
			data = read();

			if (data.startsWith("+OK") == false) {
				printDebug("Error:" + data);
				sock.close();
				return;
			}

			int i = data.lastIndexOf(' ');
			String numberMessages = data.substring(4, i);
			printDebug("You have " + numberMessages
					+ " message(s) in your mailbox");

			int n = Integer.parseInt(numberMessages);
			for (int msg = 1; msg <= n; msg++) {
				printDebug("Retreaving message " + msg);
				write("RETR " + msg);
				data = read();
				if (!data.startsWith("+OK")) {
					printDebug("Error: " + data);
					sock.close();
					return;
				}

				i = data.lastIndexOf(' ');

				StringBuffer buffer = new StringBuffer();
				data = in.readLine();

				while (data.compareTo(".") != 0) {

					printDebug(data);
					buffer.append(data);
					data = in.readLine();
				}

				if (delete) {
					write("DELE " + msg);
					read();
				}

				if (buffer.length() > 0)
					process(buffer.toString());
			}

			write("QUIT");
			read();
			close();
		} catch (IOException e) {
			System.err.println("IOException : " + e);
		} catch (Exception e) {
			System.err.println("IOException : " + e);
		}
	}

	private void open() throws Exception {
		try {
			sock = new Socket(server, port);
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
		} catch (Exception e) {
			printDebug("Error connecting to server - " + server + " Error "
					+ e.getMessage());
			throw e;
		}
	}

	private boolean login() {

		String data = null;
		try {
			if (securelogin) {
				/*
				 * String timeStamp = data.substring(4,data.length());
				 * printDebug(timeStamp); md5 mdc = new md5(timeStamp +
				 * password); printDebug(timeStamp + password); mdc.calc();
				 * write("APOP " + username + " " + mdc); read(); if
				 * (data.startsWith("+OK")) { return true; }
				 */
			} else {
				write("USER " + username);
				if (read().startsWith("+OK")) {
					out.println("PASS " + password);
					data = read();
					if (data.startsWith("+OK")) {
						return true;
					}
				}
			}
			printDebug("Invalid username/password, disconnecting from mail server");
			sock.close();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void close() {
		try {
			sock.close();
		} catch (Exception e) {
		}
	}

	private void process(String xml) {

		Envelope env = null;

		try {
			XMLReader parser = new XMLReader();
			parser.setContentHandler(new MessageParser());
			env = (Envelope) parser.parse(new InputSource(
					new ByteArrayInputStream(xml.getBytes())));
			String name = env.getHeader().getMessage().getAddress().getPlugin();
			plugin = (PluginAgent) transceiver.getAvailablePlugin(name);

			if (plugin != null) {
				if (name.equals(PluginFactory.ADAPTINET) == true)
					plugin.preProcess(PluginFactory.ADAPTINETCLASS);
				else if (name.equals(PluginFactory.MAINTENANCE) == true)
					plugin.preProcess(PluginFactory.MAINTENANCECLASS);
				else if (name.equals(PluginFactory.SERVICE) == true)
					plugin.preProcess(PluginFactory.SERVICECLASS);

				AdaptinetException exMessage = new AdaptinetException(
						AdaptinetException.SEVERITY_SUCCESS,
						AdaptinetException.GEN_MESSAGE);
				exMessage.logMessage("Plugin received Name: "
						+ plugin.getName());

				// String type = env.getHeader().getMessage().getAddress()
				// .getType();
				plugin.pushMessage(env);
				transceiver.run(plugin);
			} else {
				AdaptinetException exMessage = new AdaptinetException(
						AdaptinetException.SEVERITY_ERROR,
						AdaptinetException.GEN_BASE);
				exMessage.logMessage("Unable to load find available plugin: ");
			}

			AdaptinetException exMessage = null;
			exMessage = new AdaptinetException(
					AdaptinetException.SEVERITY_SUCCESS,
					AdaptinetException.GEN_MESSAGE);
			exMessage.logMessage("Plugin successfully executed Name: "
					+ plugin.getName());
		} catch (Exception e) {
			AdaptinetException exMessage = new AdaptinetException(
					AdaptinetException.GEN_MESSAGE,
					AdaptinetException.SEVERITY_SUCCESS);
			exMessage.logMessage("Execution failed reason: " + e.getMessage());
			try {
				out
						.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><plugin>unknown</plugin><code>1</code><desc>"
								+ e.getMessage()
								+ "</desc><timestamp>"
								+ (new java.util.Date(System
										.currentTimeMillis()).toString()) + "</timestamp></status>")
								.getBytes());
			} catch (IOException ioe) {
			}
		} finally {
			plugin = null;
		}
	}

	private void write(String s) throws IOException {
		printDebug("write: " + s);
		out.println(s);
	}

	private String read() throws IOException {
		String s = in.readLine();
		printDebug("read: " + s);
		return s;
	}

	private void printDebug(String s) {
		if (debug) {
			if (s != null) {
				System.out.println(s);
			}
		}
	}

	public Object getObject() {
		return plugin;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setServer(String server) {
		MimePop_Plugin.server = server;
	}

	public void setPolltime(int polltime) {
		this.polltime = polltime;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public void setSecurelogin(boolean securelogin) {
		this.securelogin = securelogin;
	}
}
