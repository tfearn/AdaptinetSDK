/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.mimehandlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.adaptinet.peer.PeerEntry;
import org.adaptinet.peer.PeerNode;
import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiver.NetworkAgent;


public class PeerXML {

	static NetworkAgent peers = null;
	
	static {
		peers = (NetworkAgent) ITransceiver.getTransceiver().getService(
				"networkagent");
	}

	static public String peerSave(ITransceiver transceiver, String strRequest)
			throws Exception {
		String peerURI = null;
		try {

			StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
			int size = tokenizer.countTokens() * 2;
			String token = null;
			HashMap<String, String> properties = new HashMap<String, String>(5);

			for (int i = 0; i < size; i += 2) {
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					int loc = token.indexOf('=');
					properties.put(token.substring(0, loc), token.substring(
							loc + 1, token.length()));
				}
			}

			PeerEntry re = null;
			peerURI = properties.get("Address");

			boolean bInsert = false;
			if ((re = peers.findEntry(peerURI)) == null) {
				re = new PeerEntry();
				re.setAddress(peerURI);
				bInsert = true;
			}

			re.setName(properties.get("Name"));
			re.setType(properties.get("Type"));
			if (bInsert == true) {
				peers.connect(re);
			}
			peers.setDirty(true);
		} catch (Exception e) {
			throw e;
		}
		return peerURI;
	}

	static public String peerDelete(ITransceiver transceiver, String strRequest)
			throws Exception {

		String peerURI = null;
		try {
			StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
			int size = tokenizer.countTokens() * 2;
			String token = null;
			HashMap<String, String> properties = new HashMap<String, String>();

			for (int i = 0; i < size; i += 2) {

				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					int loc = token.indexOf('=');
					properties.put(token.substring(0, loc), token.substring(
							loc + 1, token.length()));
				}
			}

			peerURI = properties.get("Address");
			if (peerURI == null)
				throw new Exception("Peeristry entry not found.");

			PeerEntry re = peers.findEntry(peerURI);

			if (re != null) {
				peers.remove(peerURI);
			} else {
				throw new Exception(peerURI + " not found in the registry.");
			}
		} catch (Exception e) {
			throw e;
		}
		return peerURI;
	}

	static public String getEntries(ITransceiver transceiver) {
		StringBuffer buffer = new StringBuffer();
		try {
			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"style.css\">");
			buffer.append("</head>");
			buffer.append("<BODY bgcolor=\"white\" link=\"#000080\" vlink=\"#000090\">");
			buffer.append("<form method=\"GET\" action=\"peers/peer\">");
			buffer.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"../images/empty.gif\" width=30 border=0></TD><td>");
			buffer.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\" >");
			buffer.append("<input type=\"hidden\" name=\"entry\" value=\"\"/>");
			buffer.append("<input type=\"Submit\" value=\"Add Peer\"/><br><br>");
			buffer.append("<tr valign=\"top\" class=\"header\">");
			buffer.append("  <th>");
			buffer.append("   Name");
			buffer.append("  </th>");
			buffer.append("  <th>");
			buffer.append("Address");
			buffer.append("  </th>");
			buffer.append("</tr>");

			Iterator<PeerNode> it = peers.getValues(true);
			int i = 0;
			while (it.hasNext()) {

				i++;
				PeerEntry entry = it.next().getEntry();
				String url = entry.getAddress().getURL();
				if (url != null) {
					buffer.append("<TR");
					if (i % 2 > 0)
						buffer.append(" bgcolor=#ffe4b5 ");
					buffer.append("><TD>");
					buffer.append("<a href=peers/peer?entry=");
					buffer.append(url);
					buffer.append(">");
					if (entry.getName() != null)
						buffer.append(entry.getName());
					buffer.append("</a>&nbsp;");
					buffer.append("</TD><TD>");
					buffer.append(url);
					buffer.append("&nbsp;");
					buffer.append("</TD>");
					buffer.append("</TR>");
				}
			}

			buffer.append("</TABLE></TD></TR></TABLE>");
			buffer.append("</form>");
			buffer.append(MimeHTML_HTTP.footer);
			buffer.append("</BODY>");
			buffer.append("</HTML>");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return buffer.toString();
	}

	static public String getEntry(ITransceiver transceiver, String uri) {

		StringBuffer buffer = new StringBuffer();
		try {
			PeerEntry entry = peers.findEntry(uri);

			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"../style.css\">");
			buffer.append("</head>");
			buffer.append("<BODY bgcolor=\"white\" >");
			buffer.append("<FORM ACTION=\"/\" METHOD=\"POST\">");
			buffer.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"../images/empty.gif\" width=30 border=0></TD><td>");
			buffer.append("<INPUT type=\"hidden\" name=\"command\" value=\"peerSave\"/>");
			buffer.append("<INPUT type=\"submit\" value=\" Save \" />&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\"Delete\" onClick=\"if (confirm('Click OK to delete this entry.')==false) return; command.value='peerdelete';form.submit();\"/>&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\" Help \" onClick=\"window.open('peerhelp.html');\"/>&nbsp;<h1>");
			if (entry != null)
				buffer.append(entry.getName());
			buffer.append("<h1/><table border=\"0\" cellspacing=\"0\" cellpadding=\"4\">");
			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Address</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Address");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getAddress().getURI());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Name</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Name");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getName());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Type</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Type");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				if (entry.getType() != null)
					buffer.append(entry.getType());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("</TABLE></TD></TR></TABLE>");
			buffer.append("</form>");
			buffer.append(MimeHTML_HTTP.footer);
			buffer.append("</BODY>");
			buffer.append("</HTML>");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return buffer.toString();
	}

}