/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.mimehandlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.adaptinet.http.HTTP;
import org.adaptinet.http.Request;
import org.adaptinet.http.Response;
import org.adaptinet.socket.PropData;
import org.adaptinet.transceiver.ITransceiver;


public class MimeHTML_HTTP implements MimeBase {
	private String url = null;

	private String urlBase = null;

	private String webBase = null;

	private String mimeType = null;

	private String pathName = null;

	private ITransceiver transceiver = null;

	private int status = 200;

	private boolean bAdminPort = false;

	static private String PLUGIN_ENTRY = "plugins/plugin?entry=";

	static private String PEER_ENTRY = "peers/peer?entry=";

	static private int PLUGIN_ENTRYLEN = 0;

	static private int PEER_ENTRYLEN = 0;

	static final String footer = "<br><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" "
			+ "width=\"100%\" ><tr><td width=\"127\" class=\"footerorange\">"
			+ "<IMG height=\"1\" src=\"/images/space.gif\" width=\"127\"></td><td width=\"400\" "
			+ "class=\"footerorange\"><IMG height=\"1\" src=\"/images/space.gif\" width=\"400\"></td>"
			+ "<td width=\"100%\" class=\"footerorange\"><IMG height=\"1\" src=\"/images/space.gif\" "
			+ "width=\"1\"></td></tr><tr><td width=\"127\" class=\"footerorange\">"
			+ "<IMG height=27 src=\"/images/space.gif\" width=127></td><td align=\"left\" "
			+ "class=\"footerorange\"  nowrap>25 B Vreeland Rd. Suite 103, Florham Park, NJ"
			+ " 07932 &nbsp;&nbsp;&nbsp; 973-451-9600 &nbsp; fx 973-439-1745</td>"
			+ "<td width=\"100%\" class=\"footerorange\"><IMG height=27 "
			+ "src=\"/images/space.gif\" width=1></td></tr><tr><td width=\"127\">"
			+ "<IMG height=1 src=\"/images/space.gif\" width=127></td><td align=\"left\" "
			+ "class=\"footer\" nowrap>all materials © Adaptinet Inc. All rights reserved.</td>"
			+ "<td align=\"right\" width=\"100%\" class=\"footer\" nowrap>"
			+ "<A class=\"footerlink\" href=\"#\">contact Adaptinet</A> &nbsp;|&nbsp;"
			+ " <A class=\"footerlink\" href=\"#\">support</A>  &nbsp;</td></tr></table><br><br><br><br>";

	static {
		PLUGIN_ENTRYLEN = PLUGIN_ENTRY.length();
		PEER_ENTRYLEN = PEER_ENTRY.length();
	}

	public MimeHTML_HTTP() {
	}

	public void init(String u, ITransceiver s) {
		transceiver = s;
		urlBase = s.getHTTPRoot();
		webBase = s.getWebRoot();
		if (urlBase == null || urlBase.equals(".")) {
			urlBase = System.getProperty("user.dir", "");
		}
		if (!urlBase.endsWith(File.separator))
			urlBase += File.separator;

		if (webBase == null || webBase.equals(".")) {
			webBase = System.getProperty("user.dir", "");
		}
		if (!webBase.endsWith(File.separator))
			webBase += File.separator;

		url = u;
		parseUrl();
	}

	private void parseUrl() {
		try {
			pathName = URLDecoder.decode(url, "UTF-8");
		} catch (Exception e) {
			pathName = url;
		}

		if (pathName.endsWith("/"))
			pathName += "index.html";

		if (pathName.startsWith("/"))
			pathName = pathName.substring(1);

		int dot = pathName.indexOf(".");
		if (dot > 0) {
			String ext = pathName.substring(dot + 1);
			setMimeForExt(ext);
		} else
			mimeType = HTTP.contentTypeHTML;
	}

	public String getContentType() {
		return mimeType;
	}

	private void setMimeForExt(String ext) {
		if (ext.equalsIgnoreCase("jpg"))
			mimeType = HTTP.contentTypeJPEG;
		else if (ext.equalsIgnoreCase("htm") || ext.equalsIgnoreCase("html"))
			mimeType = HTTP.contentTypeHTML;
		else if (ext.equalsIgnoreCase("gif"))
			mimeType = HTTP.contentTypeGIF;
		else if (ext.equalsIgnoreCase("xsl"))
			mimeType = HTTP.contentTypeXML;
		else
			mimeType = HTTP.contentTypeHTML;
	}

	public ByteArrayOutputStream process(ITransceiver transceiver,
			Request request) {
		// note: this process has gotten pretty big, really fast
		// need to revisit the exception handling here, there is a better way
		File file = null;
		String monitor = null;
		long length = 0;

		try {
			bAdminPort = (request.getPort() == transceiver.getAdminPort());
			status = isAuthorized(request.getUsername(), request.getPassword());

			if (!bAdminPort) {
				// non-admin port are regular html requests...
				String base = webBase;
				if (bAdminPort)
					base = urlBase;

				file = new File(base + pathName);
				if (file.isFile() == false || !file.canRead())
					throw new FileNotFoundException();
				else
					length = file.length();
			} else {
				if (pathName.equalsIgnoreCase("configuration.html")) {
					if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
						throw new IllegalAccessException();

					monitor = getConfiguration(transceiver);
					System.out.println(monitor);
					if (monitor != null)
						length = monitor.length();
					else
						status = HTTP.NOT_FOUND;
				} else if (pathName.equalsIgnoreCase("monitor.html")) {
					if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
						throw new IllegalAccessException();

					try {
						monitor = getMonitor();
						length = monitor.length();
					} catch (Exception e) {
						monitor = e.getMessage();
						status = HTTP.INTERNAL_SERVER_ERROR;
					} catch (Throwable t) {
						monitor = t.getMessage();
						status = HTTP.INTERNAL_SERVER_ERROR;
					}
				} else if (pathName.equalsIgnoreCase("plugins.html")) {
					if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
						throw new IllegalAccessException();

					monitor = getPlugins(transceiver);
					if (monitor != null)
						length = monitor.length();
					else
						status = HTTP.INTERNAL_SERVER_ERROR;
				} else if (pathName.equalsIgnoreCase("peers.html")) {
					if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
						throw new IllegalAccessException();

					monitor = getPeers(transceiver);
					if (monitor != null)
						length = monitor.length();
					else
						status = HTTP.INTERNAL_SERVER_ERROR;
				} else if (pathName.startsWith(PLUGIN_ENTRY)) {
					if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
						throw new IllegalAccessException();

					if (pathName.length() > PLUGIN_ENTRYLEN)
						monitor = getPluginEntry(transceiver, pathName
								.substring(PLUGIN_ENTRYLEN));
					else
						monitor = getPluginEntry(transceiver, "");

					if (monitor != null)
						length = monitor.length();
					else
						status = HTTP.NOT_FOUND;
				} else if (pathName.startsWith(PEER_ENTRY)) {
					if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
						throw new IllegalAccessException();

					if (pathName.length() > PEER_ENTRYLEN)
						monitor = getPeerEntry(transceiver, pathName
								.substring(PEER_ENTRYLEN));
					else
						monitor = getPeerEntry(transceiver, "");

					if (monitor != null)
						length = monitor.length();
					else
						status = HTTP.NOT_FOUND;
				} else if (pathName.startsWith("setadmin?")) {
					if (status == HTTP.UNAUTHORIZED || bAdminPort == false)
						throw new IllegalAccessException();

					monitor = setAdmin(pathName.substring(9), request);
					length = monitor.length();
				} else // files
				{
					String base = webBase;
					if (bAdminPort)
						base = urlBase;

					file = new File(base + pathName);
					if (file.isFile() == false || !file.canRead())
						throw new FileNotFoundException();
					else
						length = file.length();

				}
			}
		} catch (IOException e) {
			status = HTTP.NOT_FOUND;
		} catch (IllegalAccessException iae) {
			status = HTTP.UNAUTHORIZED;
		} catch (Exception e) {
			status = HTTP.INTERNAL_SERVER_ERROR;
		}
		try {
			Response resp = new Response(request.getOutStream(), transceiver
					.getHost(), mimeType);
			resp.setResponse(new ByteArrayOutputStream());
			resp.setStatus(status);
			resp.writeHeader(length);

			if (status != HTTP.OK)
				return null;

			BufferedOutputStream out = new BufferedOutputStream(request
					.getOutStream());

			if (file != null) {
				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(file));

				byte[] bytes = new byte[255];
				while (true) {
					int read = in.read(bytes);
					if (read < 0)
						break;
					out.write(bytes, 0, read);
				}
				in.close();
			} else if (monitor != null) {
				out.write(monitor.getBytes());
			}

			out.flush();
		} catch (Exception e) {
		}
		return null;
	}

	public static String getPlugins(ITransceiver transceiver) {
		try {
			return PluginXML.getEntries(transceiver);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getPeers(ITransceiver transceiver) {
		try {
			return PeerXML.getEntries(transceiver);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getConfiguration(ITransceiver transceiver) {
		try {
			return TransceiverConfiguration.getConfiguration(transceiver);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getPluginEntry(ITransceiver transceiver, String entry) {
		try {
			return PluginXML.getEntry(transceiver, entry);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getPeerEntry(ITransceiver transceiver, String peer) {
		try {
			return PeerXML.getEntry(transceiver, peer);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getLicenseKey(ITransceiver transceiver) {
		try {
			// return
			// simpleTransform.transform(licensingXML.getLicense(transceiver),
			// transceiver.getHTTPRoot() + "/licensing.xsl");
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private String setAdmin(String params, Request request) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(params, "&");
			int size = tokenizer.countTokens() * 2;
			String token = null;
			Properties properties = new Properties();
			for (int i = 0; i < size; i += 2) {
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					int loc = token.indexOf('=');
					properties.setProperty(token.substring(0, loc), token
							.substring(loc + 1, token.length()));
				}
			}

			String userid = properties.getProperty("userid");
			String current = properties.getProperty("current");
			String password = properties.getProperty("password");
			String confirm = properties.getProperty("password2");

			// check current password here
			if (isAuthorized(request.getUsername(), current) == HTTP.UNAUTHORIZED)
				return "<H2>The current password is incorrect for user: "
						+ request.getUsername() + "</H2>";

			if (!password.equals(confirm))
				return "<H2>The password does not match the confirm password</H2>";

			if (password.equals(""))
				return "<H2>The password cannot be empty.</H2>";

			MessageDigest md = MessageDigest.getInstance("MD5");
			String digest = new String(md.digest(password.getBytes()));
			String userpass = userid + ":" + digest;
			String authfile = urlBase + ".xbpasswd";
			FileOutputStream fs = new FileOutputStream(authfile);
			fs.write(userpass.getBytes());
			fs.close();
			return "<H2>Change password success for " + userid + "</H2>";
		} catch (Exception e) {
		}
		return "<H2>Change password failure.</H2>";
	}

	public int getStatus() {
		return status;
	}

	private int isAuthorized(String username, String password) {
		try {
			if (!bAdminPort)
				return HTTP.OK;

			String authfile = urlBase + ".xbpasswd";
			File file = new File(authfile);
			if (!file.isFile())
				return HTTP.OK;

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String userpass = br.readLine();
			br.close();

			StringTokenizer st = new StringTokenizer(userpass, ":");
			String user = st.hasMoreTokens() ? st.nextToken() : "";
			String pass = st.hasMoreTokens() ? st.nextToken() : "";

			MessageDigest md = MessageDigest.getInstance("MD5");
			String digest = new String(md.digest(password != null ? password
					.getBytes() : "".getBytes()));

			if (user.equals(username) && pass.equals(digest))
				return HTTP.OK;
		} catch (IOException ioe) {
		} catch (NullPointerException npe) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HTTP.UNAUTHORIZED;
	}

	String getMonitor() {

		StringBuffer buffer = new StringBuffer();
		try {
			Vector<PropData> vec = transceiver.requestList();
			buffer.append("<html><head><title>Status</title>");
			buffer.append("<link rel=\"Stylesheet\" href=\"/style.css\">");
			buffer.append("<script language=\"JavaScript\" src=\"/css.js\"></script></head>");
			buffer.append("<body BGCOLOR=\"#FFFFFF\">");
			buffer.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"images/empty.gif\" width=30 border=0></TD><td>");
			buffer.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\">");
			buffer.append("<tr valign=\"top\" class=\"header\">");
			buffer.append("<td>Plugin Name</td>");
			buffer.append("<td>System ID</td>");
			buffer.append("<td>Status</td>");
			buffer.append("<td>Control</td></tr>");

			Enumeration<PropData> e = vec.elements();
			while (e.hasMoreElements()) {
				PropData data = e.nextElement();
				buffer.append("<tr class=\"text\">");
				buffer.append("<td><strong>");
				buffer.append(data.getName());
				buffer.append("</strong></td>");
				buffer.append("<td>");
				buffer.append(data.getId());
				buffer.append("</td>");
				buffer.append("<td>");
				buffer.append(data.getState());
				buffer.append("</td>");
				buffer.append("<td>");
				buffer.append("<FORM Method=\"POST\" Action=\"\">");
				buffer
						.append("<INPUT type=\"hidden\" name=\"command\" value=\"killrequest\"></INPUT>");
				buffer
						.append("<INPUT TYPE=\"Submit\" value=\" Kill \"></INPUT>");
				buffer
						.append("<INPUT type=\"checkbox\" name=\"Force\" value=\"true\">Force</INPUT>");
				buffer.append("<INPUT type=\"hidden\" ");
				buffer.append("name=\"SYSTEMID\" ");
				buffer.append("value=\">");
				buffer.append(data.getId());
				buffer.append("\"></INPUT>");
				buffer.append("<INPUT type=\"hidden\" ");
				buffer.append("name=\"Name\" ");
				buffer.append("value=\">");
				buffer.append(data.getName());
				buffer.append("\"></INPUT>");
				buffer.append("</td>");
			}
			buffer.append("</FORM>");
			buffer.append("</table></td></tr></table>");
			buffer.append(footer);
			buffer.append("</body>");
			buffer.append("</html>");
		} catch (Exception e) {
			return null;
		}

		return buffer.toString();
	}

}
