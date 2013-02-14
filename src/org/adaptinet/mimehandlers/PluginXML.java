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
import java.util.Properties;
import java.util.StringTokenizer;

import org.adaptinet.registry.PluginEntry;
import org.adaptinet.registry.PluginFile;
import org.adaptinet.transceiver.ITransceiver;


public class PluginXML {

	private static PluginFile pluginFile = null;

	static {
		pluginFile = (PluginFile) ITransceiver.getTransceiver().getService(
				"PluginFile");
	}

	static public String pluginSave(ITransceiver transceiver, String strRequest)
			throws Exception {

		String pluginName = null;
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

			pluginName = properties.get("Name");
			PluginEntry plugin = pluginFile.findEntry(pluginName);
			boolean bInsert = false;
			if (plugin == null) {
				plugin = new PluginEntry();
				plugin.setName(pluginName);
				bInsert = true;
			}

			plugin.setDescription(properties.get("Description"));
			plugin.setType(properties.get("Type"));
			plugin.setClasspath(properties.get("Classpath"));
			plugin.setPreload(properties.get("Preload"));

			if (bInsert == true)
				pluginFile.insert(plugin);
			pluginFile.setDirty(true);
		} catch (Exception e) {
			throw e;
		}
		return pluginName;
	}

	static public String pluginDelete(ITransceiver transceiver,
			String strRequest) throws Exception {
		
		String pluginName = null;
		try {

			StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
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
			pluginName = properties.getProperty("HANDLERNAME", null);
			if (pluginName == null)
				throw new Exception("Plugin entry not found.");

			PluginEntry re = pluginFile.findEntry(pluginName);

			if (re != null) {
				pluginFile.remove(re);
			} else {
				throw new Exception(pluginName + " not found in the registry.");
			}
		} catch (Exception e) {
			throw e;
		}
		return pluginName;
	}

	static public String getEntries(ITransceiver transceiver) {

		StringBuffer buffer = new StringBuffer();
		try {

			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer
					.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"style.css\">");
			buffer.append("</head>");
			buffer
					.append("<BODY bgcolor=\"white\" link=\"#000080\" vlink=\"#000090\">");
			buffer.append("<form method=\"GET\" action=\"plugins/plugin\">");
			buffer
					.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"images/empty.gif\" width=30 border=0></TD><td>");
			buffer
					.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\" >");
			buffer.append("<input type=\"hidden\" name=\"entry\" value=\"\"/>");
			buffer
					.append("<input type=\"Submit\" value=\"Add Plugin\"/><br><br>");

			buffer.append("<tr valign=\"top\" class=\"header\">");
			buffer.append("  <th>");
			buffer.append("   Name");
			buffer.append("  </th>");
			buffer.append("  <th>");
			buffer.append("Class");
			buffer.append("  </th>");
			buffer.append("  <th>");
			buffer.append("    Description");
			buffer.append("  </th>");
			buffer.append("</tr>");

			Iterator<PluginEntry> it = pluginFile.getValues();
			int i = 0;
			while (it.hasNext()) {

				i++;
				PluginEntry entry = it.next();
				buffer.append("<TR class = \"text\"");
				if (i % 2 > 0)
					buffer.append(" bgcolor=#ffe4b5 ");
				buffer.append("><TD>");
				buffer.append("<a href=plugins/plugin?entry=");
				buffer.append(entry.getName());
				buffer.append(">");
				buffer.append(entry.getName());
				buffer.append("</a>&nbsp;");
				buffer.append("</TD><TD>");
				buffer.append(entry.getType());
				buffer.append("</TD><TD>");
				buffer.append(entry.getDescription());
				buffer.append("</TD></TR>");
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

	static public String getEntry(ITransceiver transceiver, String name) {
		StringBuffer buffer = new StringBuffer();
		try {

			PluginEntry entry = pluginFile.findEntry(name);

			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"../style.css\">");
			buffer.append("</head>");
			buffer.append("<BODY bgcolor=\"white\">");
			buffer.append("<FORM ACTION=\"/\" METHOD=\"POST\">");
			buffer.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"../images/empty.gif\" width=30 border=0></TD><td>");
			buffer.append("<INPUT type=\"hidden\" name=\"command\" value=\"pluginsave\"/>");
			buffer.append("<INPUT type=\"submit\" value=\" Save \"/>&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\"Delete\" onClick=\"if (confirm('Click OK to delete this entry.')==false) return; command.value='plugindelete';form.submit();\"/>&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\" Help \" onClick=\"window.open('pluginhelp.html');\"/>&nbsp;<h1>");
			if (entry != null)
				buffer.append(entry.getName());
			buffer.append("<h1/><table border=\"0\" cellspacing=\"0\" cellpadding=\"4\">");

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
			buffer.append("<TD><font class=\"header\">Description</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Description");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getDescription());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Class</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Type");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getType());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Classpath</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Classpath");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getClasspath());
			buffer.append("\"></TD>");
			buffer.append("</TR>");

			buffer.append("<TR>");
			buffer.append("<TD><font class=\"header\">Preload</font></TD>");
			buffer.append("<TD>");
			buffer.append("<INPUT name=Preload");
			buffer.append(" size=30 value=\"");
			if (entry != null)
				buffer.append(entry.getPreload());
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