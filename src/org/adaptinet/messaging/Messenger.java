/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.messaging;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.adaptinetex.PluginException;
import org.adaptinet.parser.InputSource;
import org.adaptinet.parser.XMLReader;
import org.adaptinet.pluginagent.PluginAgent;
import org.adaptinet.socket.HttpTimeoutHandler;
import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiver.NetworkAgent;
import org.adaptinet.transceiver.TransceiverConfig;

import sun.net.smtp.SmtpClient;


public final class Messenger {

	static private boolean autoconnect = false;
	private OutputStream os = null;
	private InputStream is = null;
	private HttpURLConnection connection = null;
	// private URLConnection connection = null;
	private Object proxyArgs[] = null;
	private Message proxyMessage = null;
	public static final int MAXHOPS = 4;
	private static NetworkAgent networkAgent = null;
	private static boolean useProxy = false;

	static {
		try {
			networkAgent = (NetworkAgent) ITransceiver.getTransceiver()
					.getService("networkagent");
			useProxy = ITransceiver.getTransceiver().useProxy();
		} catch (Exception e) {
		}
	}

	public Messenger() {
		if (useProxy == true) {
			proxyArgs = new Object[2];
			Address proxyAddress = new Address(ITransceiver.getTransceiver()
					.getHost());
			proxyAddress.setPlugin("ProxyServer");
			proxyAddress.setMethod("sendRequest");
			proxyMessage = new Message(ITransceiver.getTransceiver());
			proxyMessage.setAddress(proxyAddress);
		}
	}

	static public void postMessage(Message message) {

		class PostMessage implements Runnable {

			Message msg = null;
			PostMessage(Message msg) {
				this.msg = new Message(msg);
			}

			final public void run() {
				try {
					Messenger messenger = new Messenger();
					messenger.doPostMessage(msg);
				} catch (Exception e) {
					if (msg.getGuaranteed() == true) {
						sendMail(msg);
					}
					postError(msg, e.getMessage());
				}
			}
		}
		new Thread(new PostMessage(message)).start();
	}

	static public void postMessage(Message message, Object ... arguments) {

		class PostMessage implements Runnable {

			Message msg = null;
			Object args[] = null;

			PostMessage(Message msg, Object args[]) {
				this.msg = new Message(msg);
				if (args != null) {
					this.args = new Object[args.length];
					System.arraycopy(args, 0, this.args, 0, args.length);
				}
			}

			final public void run() {
				try {
					Messenger messenger = new Messenger();
					messenger.doPostMessage(msg, this.args);
				} catch (Exception e) {
					if (msg.getGuaranteed() == true) {
						sendMail(msg, args);
					}
					postError(msg, e.getMessage());
				}
			}
		}
		new Thread(new PostMessage(message, arguments)).start();
	}

	static public boolean testConnection(Message msg) throws Exception {
		msg.getAddress().setSync();
		return new Messenger().doTestConnection(msg);
	}

	static public Object sendMessage(Message msg, Object ... args)
			throws Exception {
		
		msg.getAddress().setSync();
		String ret = new Messenger().doPostMessage(msg, args);
		XMLReader parser = new XMLReader();
		parser.setContentHandler(new MessageParser());
		Envelope env = (Envelope) parser.parse(new InputSource(
				new ByteArrayInputStream(ret.getBytes())));
		return env.getContent(0);
	}

	static public void localPostMessage(Message message, Object ... arguments) {

		class LocalPostMessage implements Runnable {

			LocalPostMessage(Message msg, Object args[]) {
				this.msg = msg;
				this.args = args;
			}

			final public void run() {

				try {
					PluginAgent plugin = null;
					try {
						ITransceiver transceiver = ITransceiver
								.getTransceiver();
						String name = msg.getAddress().getPlugin();
						plugin = (PluginAgent) transceiver
								.getAvailablePlugin(name);

						if (plugin != null) {
							Header header = new Header();
							header.setMessage(msg);
							Envelope env = new Envelope();
							env.setHeader(header);
							Body body = new Body();
							body.setcontentArray(args);
							env.setBody(body);
							plugin.pushMessage(env);
							transceiver.run(plugin);
						} else {
							AdaptinetException exMessage = new AdaptinetException(
									AdaptinetException.SEVERITY_ERROR,
									AdaptinetException.GEN_BASE);
							exMessage.logMessage("Unable to load plugin : "
									+ name);
						}

					} catch (Exception e) {
						AdaptinetException exMessage = new AdaptinetException(
								AdaptinetException.GEN_MESSAGE,
								AdaptinetException.SEVERITY_SUCCESS);
						exMessage
								.logMessage("Unable to perform localPostMessage to plugin: "
										+ plugin.getName());
					}
				} catch (Exception e) {
				}
			}
			Message msg = null;
			Object args[] = null;
		}
		new Thread(new LocalPostMessage(message, arguments)).start();
	}

	static public void localPostMessage(PluginAgent plugin, String method,
			Object[] arguments) {

		class LocalPostMessage implements Runnable {

			static final String local = "localhost:8082";

			LocalPostMessage(PluginAgent plugin, String method, Object ... args) {
				this.plugin = plugin;
				this.method = method;
				this.args = args;
			}

			final public void run() {
				try {
					// plugin.preProcess("org.adaptinet.pluginagent.ServicePlugin");
					// Set to local host default port even though it is not used
					Message msg = new Message(local);
					msg.getAddress().setMethod(method);
					msg.getAddress().setPlugin(plugin.getName());
					Header header = new Header();
					header.setMessage(msg);
					Envelope env = new Envelope();
					env.setHeader(header);
					Body body = new Body();
					if (args != null)
						body.setcontentArray(args);
					env.setBody(body);
					plugin.pushMessage(env);
					ITransceiver.getTransceiver().run(plugin);
				} catch (Exception e) {
					AdaptinetException exMessage = new AdaptinetException(
							AdaptinetException.GEN_MESSAGE,
							AdaptinetException.SEVERITY_SUCCESS);
					exMessage
							.logMessage("Unable to perform localPostMessage to plugin: "
									+ plugin.getName());
				}
			}

			PluginAgent plugin = null;
			String method = null;
			Object args[] = null;
		}
		new Thread(new LocalPostMessage(plugin, method, arguments)).start();
	}

	static public void broadcastMessage(Message message, Object ... arguments) {

		broadcastMessage(message, -1, arguments);
	}

	static public void broadcastMessage(Message message, int hops, Object ... arguments) {

		class BroadcastMessage implements Runnable {

			BroadcastMessage(Message msg, Object args[]) {
				this.msg = new Message(msg);
				if (args != null) {
					this.args = new Object[args.length];
					System.arraycopy(args, 0, this.args, 0, args.length);
				}
				/*
				 * If this transceiver is not in autoconnect mode we need to
				 * limit the number of hops a message can be broadcasted.
				 */
				if (autoconnect == false) {
					int hops = this.msg.getHopCount();
					if (hops < 0 || hops > MAXHOPS) {
						this.msg.setHops(MAXHOPS);
					}
				}
			}

			final public void run() {
				try {
					networkAgent.broadcastMessage(msg, this.args);
				} catch (Exception e) {
					postError(msg, e.getMessage());
				}
			}
			Message msg = null;
			Object args[] = null;
		}
		message.setHops(hops);
		new Thread(new BroadcastMessage(message, arguments)).start();
	}

	static public void postError(Message msg, String error) {

		try {
			Object args[] = new Object[2];
			args[0] = msg.getAddress().getURI();
			args[1] = error;
			Message message = new Message();
			message.setAddress(msg.getReplyTo());
			String name = msg.getAddress().getPlugin();
			if (name != null) {
				message.getAddress().setPlugin(name);
				message.getAddress().setMethod("error");
				localPostMessage(message, args);
			}
		} catch (Exception e) {
		}
	}

	private String doPostMessage(Message msg, Object ... args) throws Exception {

		String ret = null;
		try {
			if (useProxy == false) {
				Address receiver = msg.hop();
				String url = receiver.getURL();
				String pst = receiver.getPostfix();
				if (pst != null) {
					url += "/" + pst;
				}
				openConnection(url, false);
				new PostWriter(os).write(msg, args);
				ret = completeConnection();
			} else {
				proxyArgs[0] = msg;
				proxyArgs[1] = args;
				String url = msg.getAddress().getURL();
				openConnection(url, false);
				new PostWriter(os).write(proxyMessage, proxyArgs);
				ret = completeConnection();
			}
		} finally {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
		}
		return ret;
	}

	private boolean doTestConnection(Message msg) throws Exception {
		boolean connected = false;
		try {
			Address receiver = msg.hop();
			String address = receiver.getURL();
			String pst = receiver.getPostfix();
			if (pst != null) {
				address += "/" + pst;
			}
			HttpTimeoutHandler xHTH = new HttpTimeoutHandler(10);
			URL url = new URL((URL) null, address, xHTH);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			connection.disconnect();
			connected = true;
		} catch (InterruptedIOException e) {
			connected = false;
		}
		return connected;
	}

	private void openConnection(String address, boolean bSecure)
			throws PluginException {

		try {
			URL url = new URL(address);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Content-Type", " " + "text/xml");
			connection.setRequestProperty("Connection", "Keep-Alive");
			// connection.setRequestProperty("Connection", " close");
			connection.setDoOutput(true);
			connection.setAllowUserInteraction(true);
			os = connection.getOutputStream();
		} catch (Exception e) {
			PluginException agentex = new PluginException(
					AdaptinetException.SEVERITY_FATAL,
					PluginException.ANT_UNKNOWNHOST);
			throw agentex;
		}
	}

	private String completeConnection() throws Exception {

		try {
			connection.connect();
			os.flush();
			os.close();

			is = connection.getInputStream();
			StringBuffer stringbuffer = new StringBuffer();

			// read the response
			int b = -1;
			while (true) {
				b = is.read();
				if (b == -1)
					break;
				stringbuffer.append((char) b);
			}

			is.close();
			os.close();
			is = null;
			os = null;

			return stringbuffer.toString();
		} catch (Exception e) {
			PluginException agentex = new PluginException(
					AdaptinetException.SEVERITY_FATAL,
					PluginException.ANT_OBJDOTRANS);
			throw agentex;
		}
	}

	static private void sendMail(Message msg, Object ... args) {

		try {
			SmtpClient smtp = new SmtpClient(ITransceiver.getTransceiver()
					.getSMTPHost());
			smtp.from(msg.getReplyTo().getEmail());
			smtp.to(msg.getAddress().getEmail());
			PrintStream ps = smtp.startMessage();

			ps.println(msg.getReplyTo().getEmail());
			ps.println(msg.getAddress().getEmail());
			ps.println("Subject: transaction");
			ps.println("");

			new PostWriter(ps).write(msg, args);
			smtp.closeServer();
			ps.close();
			/*
			 * String mailHost = agentFactory.getServer().getSMTPHost(); if
			 * (mailHost == null || mailHost.length()==0) throw new
			 * Exception("Mail Host is not defined in transceiver settings.");
			 * 
			 * Properties properties = new Properties();
			 * properties.put("mail.smtp.host", mailHost); String from =
			 * handlerMap.getProperty(org.adaptinet.registry.RegEntry.MAILFROM);
			 * if (from == null || from.length() == 0) throw new Exception("A
			 * \"From\" address is not registered for handlerMap " + this.name);
			 * properties.put("mail.from", from); javax.mail.Session session =
			 * Session.getInstance(properties, null); javax.mail.Message message
			 * = new MimeMessage(session); javax.mail.InternetAddress address =
			 * new InternetAddress(respondto.substring(7));
			 * message.setRecipient(Message.RecipientType.TO, address);
			 * message.setFrom(new InternetAddress(from));
			 * message.setSubject("XML-Agent response from handlerMap: " +
			 * this.name); message.setContent(results, "text/plain"); Transport
			 * transport = session.getTransport(address); transport.connect();
			 * transport.send(message);
			 */
		} catch (Exception e) {
			PluginException be = new PluginException(
					PluginException.SEVERITY_ERROR,
					PluginException.ANT_POSTFAILURE);
			be.logMessage(e);
			// e.printStackTrace();
		}
	}

	static {
		try {

			String s = ITransceiver.getTransceiver().getProperty(
					TransceiverConfig.AUTOCONNECT);
			if (s != null && s.equalsIgnoreCase("true"))
				autoconnect = true;
			else
				autoconnect = false;
		} catch (Exception e) {
		}
	}
}
