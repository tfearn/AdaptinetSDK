/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.transmitter;

import java.applet.Applet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.adaptinet.xmltools.xmlutils.IXMLInputSerializer;
import org.adaptinet.xmltools.xmlutils.IXMLOutputSerializer;
import org.adaptinet.xmltools.xmlutils.XMLSerializerFactory;

public class XMLTransmitter extends Applet implements Runnable {
	private static final long serialVersionUID = -9196649406826095026L;

	static final public String JSESSIONID = "jsessionid";

	static final public String SETCOOKIE = "Set-Cookie";

	private boolean isStandalone = true;

	private String host = "localhost";

	private int port = 8082;

	private int timeout = 120;

	private String method = "POST";

	private String file = "/";

	private String result = null;

	private String request;

	private String pi;

	private String address;

	private String xmlIn = null;

	private boolean bDebug = true;

	private Exception lastError = null;

	private String contentType = "text/xml";

	private int responseCode = 200;

	private String responseMessage = null;

	private static String jsessionid = null;

	private boolean useProxy = false;

	private String proxyHost = "";

	private String proxyPort = "80";

	static {
		try {
			Class.forName("java.lang.reflect.AccessibleObject");
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Java2 Disabled");
		}
	}

	// Construct the applet
	public XMLTransmitter() {
		if (useProxy) {
			System.getProperties().put("proxySet", "true");
			System.getProperties().put("proxyHost", proxyHost);
			System.getProperties().put("proxyPort", proxyPort);
		}
	}

	// Initialize the applet
	public void init() {
		lastError = null;
		try {
			isStandalone = false;
			URL docbase = this.getDocumentBase();
			host = this.getParameter("host", docbase.getHost());
			port = Integer.parseInt(this.getParameter("port", "8082"));
			method = this.getParameter("method", "POST");
			file = this.getParameter("file", "/");
			timeout = Integer.parseInt(this.getParameter("timeout", "1200"));
			setSize(0, 0);
		} catch (Exception e) {
			lastError = e;
			e.printStackTrace();
		}
	}

	// Get a parameter value
	public String getParameter(String key, String def) {
		return isStandalone ? System.getProperty(key, def)
				: (getParameter(key) != null ? getParameter(key) : def);
	}

	// Get parameter info
	public String[][] getParameterInfo() {
		String[][] pinfo = { { "host", "String", "Servername or IP Address" },
				{ "port", "int", "Port of Server for Request" },
				{ "method", "String", "Method type (GET or POST)" },
				{ "file", "String", "File to GET or script to POST" },
				{ "timeout", "int", "Timeout for transaction (in SECONDS)" } };
		return pinfo;
	}

	// Start the applet
	public void start() {
	}

	// Stop the applet
	public void stop() {
	}

	// Destroy the applet
	public void destroy() {
	}

	// Get Applet information
	public String getAppletInfo() {
		return "XML Request Client applet";
	}

	public String doTransaction(String xmlIn) {

		synchronized (this) {
			lastError = null;
			this.xmlIn = xmlIn;
			Thread t = new Thread(this);
			t.start();

			try {
				t.join(timeout * 1000);
			} catch (InterruptedException e) {
				if (bDebug) {
					e.printStackTrace();
				}
				result = null;
			}
		}
		return result;
	}

	public Object doTransaction(Object o) {
		return doTransaction(o, null);
	}

	public Object doTransaction(Object o, Properties piAttributes) {
		lastError = null;
		Object retObj = null;

		synchronized (this) {

			if (piAttributes != null) {
				pi = formatPiAttribs(piAttributes);
			}

			try {
				IXMLOutputSerializer out = null;

				out = XMLSerializerFactory.getOutputSerializer();
				this.xmlIn = out.get(o, true, pi);

				Thread t = new Thread(this);
				t.start();

				try {
					t.join(timeout * 1000);
				} catch (InterruptedException e) {
					if (bDebug) {
						e.printStackTrace();
					}
					result = null;
					retObj = null;
				}

				if (result != null) {
					// Get the working package name from the in object
					String className = o.getClass().getName();
					String strpackage = className.substring(0, className
							.lastIndexOf('.'));

					IXMLInputSerializer in = XMLSerializerFactory.getInputSerializer();
					in.setPackage(strpackage);

					retObj = in.get(result);
				}
			} catch (Exception e) {
				lastError = e;
				retObj = null;
			}
		}
		return retObj;
	}

	public String getPI() {
		String ret = new String("");

		if (pi != null && pi.length() > 0) {
			ret += "<?Transceiver" + pi + " ?>";
		}

		return ret;
	}

	private String formatPiAttribs(Properties props) {
		String piAttribs = "";
		Enumeration<?> enumer = props.propertyNames();
		while (enumer.hasMoreElements()) {
			String propName = (String) enumer.nextElement();
			piAttribs += propName + "=\"" + props.getProperty(propName) + "\" ";
		}

		return piAttribs;
	}

	public void run() {

		// if (xmlIn.startsWith("<?xml")==false) {
		// if(xmlIn.indexOf("<?Transceiver")<0&&xmlIn.indexOf("<?xmlagent")<0) {
		// request = "<?xml version='1.0'?>"+getPI()+xmlIn;
		// }
		// }
		// else {
		request = xmlIn;
		// }

		// System.out.println(request);
		result = null;
		OutputStream os = null;
		InputStream is = null;

		try {
			if (address == null) {
				if (!file.startsWith("/")) {
					file = "/" + file;
				}
				address = "http://" + host + ":" + Integer.toString(port)
						+ file;
			}

			if (isStandalone && address.startsWith("https://")) {

				try {
					java.security.Security
							.addProvider((java.security.Provider) Class
									.forName(
											"com.sun.net.ssl.internal.ssl.Provider")
									.newInstance());
					Properties sysProps = System.getProperties();
					sysProps.put("java.protocol.plugin.pkgs",
							"com.sun.net.ssl.internal.www.protocol");
					System.setProperties(sysProps);
				} catch (Exception e) {
					lastError = e;
					e.printStackTrace();
					return;
				}
			}

			// Assume an error.
			responseCode = 601;
			URL url = new URL(address);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			// String password = "username:password";
			// String encodedPassword = base64Encode( password );
			// connection.setRequestProperty( "Proxy-Authorization",
			// encodedPassword );

			if (method.equals("POST")) {

				connection.setDoOutput(true);
				connection
						.setRequestProperty("Content-type", " " + contentType);
				if (jsessionid != null) {
					connection.setRequestProperty(JSESSIONID, jsessionid);
				}
				connection.setRequestProperty("Connection", " close");
				os = connection.getOutputStream();

				// String encoded = URLEncoder.encode(request);
				int len = request.length();
				byte[] data = request.getBytes();
				for (int i = 0; i < len; i++) {
					os.write(data[i]);
				}
				os.flush();
			}

			String line = connection.getHeaderField(0);
			responseCode = Integer.parseInt(line.substring(9, 12));

			if (responseCode > 200) {

				// Here is some code to display the headers in the response
				// good for debuging
				/*
				 * for(int i=0;line!=null;i++) { key =
				 * connection.getHeaderFieldKey(i); line =
				 * connection.getHeaderField(i); if(line==null) break;
				 * System.out.println(key+" : " +line); }
				 */
				if (responseCode < 601) {
					responseMessage = line.substring(13);
					result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><url>"
							+ address
							+ "</url><code>"
							+ Integer.toString(responseCode)
							+ "</code><desc>"
							+ responseMessage
							+ "</desc><timestamp>"
							+ (new java.util.Date(System.currentTimeMillis())
									.toString()) + "</timestamp></status>";
				} else {
					responseMessage = connection
							.getHeaderField("BOA-Error-Message");
					result = responseMessage;
				}
			} else {
				if (jsessionid == null) {
					jsessionid = connection.getHeaderField(SETCOOKIE);
					if (jsessionid != null) {
						jsessionid = jsessionid.substring(11, jsessionid
								.indexOf(';'));
					}
				}

				is = connection.getInputStream();
				StringBuffer stringbuffer = new StringBuffer();

				int b = -1;
				while (true) {
					b = is.read();
					if (b == -1) {
						break;
					}
					stringbuffer.append((char) b);
				}

				result = stringbuffer.toString();
			}
		} catch (Exception e) {
			lastError = e;
			if (bDebug) {
				e.printStackTrace();
			}
		} finally {
			this.address = null;
			try {
				if (os != null) {
					os.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException ioe) {
			}
		}
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUrl(String address) {
		this.address = address;
	}

	public boolean getUseProxy() {
		return useProxy;
	}

	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public final Exception getLastError() {
		return lastError;
	}

	public final boolean hasError() {
		return lastError != null;
	}

	public void setDebug(boolean b) {
		bDebug = b;
	}

}
