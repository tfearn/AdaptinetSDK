/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.mimehandlers;

import java.util.StringTokenizer;
import java.util.Properties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;

import org.adaptinet.http.HTTP;
import org.adaptinet.http.Request;
import org.adaptinet.transceiver.ITransceiver;


public class MimeCommand_HTTP implements MimeBase {
	public MimeCommand_HTTP() {
	}

	public void init(String u, ITransceiver s) {
	}

	public String getContentType() {
		return null;
	}

	public ByteArrayOutputStream process(ITransceiver transceiver,
			Request request) {
		
		String strCommand = null;
		String strRequest = request.getRequest();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			boolean bAdminPort = (request.getPort() == transceiver
					.getAdminPort());
			status = isAuthorized(transceiver, request.getUsername(), request
					.getPassword());
			int begin = strRequest.indexOf("=");
			int end = strRequest.indexOf("&");
			if (end == -1)
				end = strRequest.length();

			if (begin != -1)
				strCommand = strRequest.substring(begin + 1, end);
			else
				strCommand = strRequest;

			strCommand = strCommand.toLowerCase();

			status = 200;
			if (strCommand.startsWith("transceiverconfig")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					TransceiverConfiguration.setConfiguration(transceiver,
							strRequest);
					out.write(MimeHTML_HTTP.getConfiguration(transceiver)
							.getBytes());
				} catch (Exception e) {
					// status = 500;
					try {
						out.write("<H3>Error retrieving configuration settings</H3>"
										.getBytes());
					} catch (IOException ioe) {
					}
				}
			} else if (strCommand.equalsIgnoreCase("peersave")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					String peerName = PeerXML.peerSave(transceiver, strRequest);
					try {
						out.write(("<H3>Successful save of " + peerName + "</H3>")
										.getBytes());
					} catch (IOException e) {
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} catch (Exception e) {
					try {
						String msg = "<H3>Error updating peer entry: ";
						msg += e.getMessage();
						msg += "</H3>";
						out.write(msg.getBytes());
					} catch (IOException ioe) {
						status = 500;
					}
				}
			} else if (strCommand.startsWith("peerdelete")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					String peerName = PeerXML.peerDelete(transceiver,
							strRequest);
					try {
						out.write(("<H3>Successful delete of " + peerName + "</H3>")
										.getBytes());
					} catch (IOException e) {
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} catch (Exception e) {
					try {
						String msg = "<H3>Error updating peer entry: ";
						msg += e.getMessage();
						msg += "</H3>";
						out.write(msg.getBytes());
					} catch (IOException ioe) {
						status = 500;
					}
				}
			} else if (strCommand.equalsIgnoreCase("pluginsave")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					String pluginName = PluginXML.pluginSave(transceiver,
							strRequest);
					try {
						out.write(("<H3>Successful update of "
									+ pluginName + "</H3>").getBytes());
					} catch (IOException e) {
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} catch (Exception e) {
					try {
						String msg = "<H3>Error updating plugin entry: ";
						msg += e.getMessage();
						msg += "</H3>";
						out.write(msg.getBytes());
					} catch (IOException ioe) {
						status = 500;
					}
				}
			} else if (strCommand.equalsIgnoreCase("plugindelete")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				try {
					String pluginName = PluginXML.pluginDelete(transceiver,
							strRequest);
					try {
						out.write(("<H3>Successful delete of "
										+ pluginName + "</H3>").getBytes());
					} catch (IOException e) {
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} catch (Exception e) {
					try {
						String msg = "<H3>Error updating plugin entry: ";
						msg += e.getMessage();
						msg += "</H3>";
						out.write(msg.getBytes());
					} catch (IOException ioe) {
						status = 500;
					}
				}
			} else if (strCommand.startsWith("shutdown")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				serverInterface = transceiver;
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
						try {
							MimeCommand_HTTP.serverInterface.shutdown();
						} catch (Exception e) {
						}
					}
				}).start();
				try {
					out.write(("<H3>" + transceiver.getHost() + ":"
									+ transceiver.getPort() + " shutting down" + "</H3>")
									.getBytes());
				} catch (IOException e) {
					status = 500;
				}
			} else if (strCommand.startsWith("restart")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				serverInterface = transceiver;
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
						try {
							MimeCommand_HTTP.serverInterface.restart();
						} catch (Exception e) {
						}
					}
				}).start();

				try {
					out.write(("<H3>" + transceiver.getHost() + ":"
							+ transceiver.getPort() + " restarting" + "</H3>")
							.getBytes());
				} catch (IOException e) {
					status = 500;
				}
			} else if (strCommand.startsWith("killrequest")) {
				if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
					return out;

				StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
				int size = tokenizer.countTokens() * 2;
				String token = null;
				Properties properties = new Properties();
				for (int i = 0; i < size; i += 2) {
					if (tokenizer.hasMoreTokens()) {
						token = tokenizer.nextToken();
						int loc = token.indexOf('=');
						if (token.endsWith("Submit")
								|| token.endsWith("killrequest"))
							continue;
						properties.setProperty(token.substring(0, loc), token
								.substring(loc + 1, token.length()));
					}
				}
				String id = properties.getProperty("SYSTEMID");
				String name = properties.getProperty("NAME");
				String force = properties.getProperty("FORCE");
				boolean b = false;
				if (force != null) {
					b = Boolean.getBoolean(force);
				}
				short s = Short.parseShort(id);
				if (transceiver.killRequest(s, b)) {
					try {
						out.write(("<H3>" + "Request " + name
								+ " Successfully Stopped on "
								+ transceiver.getHost() + ":"
								+ transceiver.getPort() + "</H3>").getBytes());
					} catch (IOException e) {
						status = 500;
					}
				} else {
					try {
						out.write(("<H3>" + "Unable to stop Request " + name
								+ " on " + transceiver.getHost() + ":"
								+ transceiver.getPort() + "</H3>").getBytes());
					} catch (IOException e) {
						status = 500;
					}
				}
			} else {
				String token = null;
				MimeBase mimeXML = (MimeBase) Class.forName(
						"org.adaptinet.http.MimeXML").newInstance();
				StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");

				StringBuffer buffer = new StringBuffer(
						"<?xml version=\"1.0\"?>");
				buffer.append("<");
				buffer.append(strCommand);
				buffer.append(">");
				while (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					int i = token.indexOf('=');
					int l = token.length();
					if (i > 0) {
						buffer.append("<");
						buffer.append(token.substring(0, i - 1));
						buffer.append(">");
						buffer.append(token.substring(i + 1, l));
						buffer.append("</");
						buffer.append(token.substring(0, i - 1));
						buffer.append(">");
					}
				}

				buffer.append("</");
				buffer.append(strCommand);
				buffer.append(">");
				request.putRequest(buffer.toString(), "", null);
				return mimeXML.process(transceiver, request);
			}
		} catch (Exception c360e) {
			c360e.printStackTrace();
		}
		return out;
	}

	public int getStatus() {
		return status;
	}

	private int isAuthorized(ITransceiver s, String username, String password) {
		try {
			String urlBase = s.getHTTPRoot();
			if (urlBase == null || urlBase.equals("."))
				urlBase = System.getProperty("user.dir", "");
			if (!urlBase.endsWith(File.separator))
				urlBase += File.separator;

			String authfile = urlBase + ".xbpasswd";
			File file = new File(authfile);
			if (!file.isFile())
				return HTTP.OK;

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String userpass = br.readLine();
			br.close();

			StringTokenizer st = new StringTokenizer(userpass, ":");
			String user = st.nextToken();
			String pass = st.nextToken();

			MessageDigest md = MessageDigest.getInstance("MD5");
			String digest = new String(md.digest(password.getBytes()));

			if (user.equals(username) && pass.equals(digest))
				return HTTP.OK;
		} catch (IOException ioe) {
		} catch (NullPointerException npe) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HTTP.UNAUTHORIZED;
	}

	private int status = HTTP.UNAUTHORIZED;

	static private ITransceiver serverInterface = null;
}
