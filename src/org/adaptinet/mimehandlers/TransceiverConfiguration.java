/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.mimehandlers;

import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.adaptinet.transceiver.ITransceiver;


public class TransceiverConfiguration {
	static public void setConfiguration(ITransceiver transceiver,
			String strRequest) {
		StringTokenizer tokenizer = new StringTokenizer(strRequest, "&");
		int size = tokenizer.countTokens() * 2;
		String token = null;
		Properties properties = new Properties();
		for (int i = 0; i < size; i += 2) {
			if (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				int loc = token.indexOf('=');
				if (token.endsWith("Submit")
						|| token.endsWith("transceiverconfig"))
					continue;
				properties.setProperty(token.substring(0, loc), token
						.substring(loc + 1, token.length()));
			}
		}

		transceiver.saveConfig(properties);
	}

	static public String getConfiguration(ITransceiver transceiver) {
		return getConfiguration(transceiver, true);
	}

	static public String getConfiguration(ITransceiver transceiver,
			boolean bFromFile) {
		StringBuffer buffer = new StringBuffer();
		try {
			buffer.append("<HTML>");
			buffer.append("<head>");
			buffer.append("<SCRIPT SRC=\"/css.js\" LANGUAGE=\"JavaScript\"></SCRIPT><link rel=\"Stylesheet\" href=\"style.css\">");
			buffer.append("</head>");
			buffer.append("<BODY bgcolor=\"#ffffff\">");
			buffer.append("<FORM NAME=\"frmConfig\" ACTION=\"/\" METHOD=\"POST\">");
			buffer.append("<TABLE cellPadding=0 cellSpacing=0  border=0 WIDTH=\"500\"<tr><TD><IMG alt=\"\" src=\"images/empty.gif\" width=30 border=0></TD><td>");
			buffer.append("<TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"4\">");
			buffer.append("<INPUT type=\"hidden\" id=\"command\" name=\"command\" value=\"transceiverconfig\"/>");
			buffer.append("<INPUT type=\"button\" value=\"\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BACKGROUND-IMAGE: url(images/save.gif); BORDER-BOTTOM-WIDTH: 0px; WIDTH: 135px; HEIGHT: 31px; BORDER-RIGHT-WIDTH: 0px\" onClick=\"if (confirm('Click OK to confirm.') != true) return; document.frmConfig.submit();\"/>");
			// buffer.append("onmouseover=\"rollcheck('control','images/control_f2.gif',1)\"
			// onclick=\"swap('control')\"
			// onmouseout=\"rollcheck('control','images/control_f2.gif',0)\"");
			buffer.append("&nbsp;");
			buffer.append("<INPUT type=\"button\" value=\"\" style=\"BORDER-TOP-WIDTH: 0px; BORDER-LEFT-WIDTH: 0px; BACKGROUND-IMAGE: url(images/help.gif); BORDER-BOTTOM-WIDTH: 0px; WIDTH: 135px; HEIGHT: 31px; BORDER-RIGHT-WIDTH: 0px\" onClick=\"window.open('/confighelp.html');\"/>\n");
			buffer.append("<hr></hr></td></tr>");
			buffer.append("<tr><font class=\"header\">Transceiver Settings</font></td></tr>");

			Properties properties;
			if (bFromFile == true) {
				properties = transceiver.getConfigFromFile();
			} else {
				properties = transceiver.getConfig();
			}

			if (properties != null) {
				Set<Object> s = properties.keySet();
				Iterator<Object> it = s.iterator();

				while (it.hasNext()) {
					String key = (String)it.next();
					buffer.append("<TR class=\"text\"");
					buffer.append("><TD>");
					buffer.append(key);
					buffer.append("</TD><TD><input name=");
					buffer.append(key);
					buffer.append(" value=\"");
					buffer.append(properties.getProperty(key));
					buffer.append("\" size=30>");
					buffer.append("</TD></TR>");
				}
			}
			buffer.append("</TD></TR></TABLE></TABLE></FORM>");
			buffer.append(MimeHTML_HTTP.footer);
			buffer.append("</BODY></HTML>");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return buffer.toString();
	}
}
