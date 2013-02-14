/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.adaptinet.messaging.Message;
import org.adaptinet.messaging.Messenger;
import org.adaptinet.pluginagent.Plugin;


public class ProxyServer extends Plugin {

    private Map<String, ProxyClient> clientMap = Collections.synchronizedMap(new HashMap<String, ProxyClient>());
    private boolean bDebug = true;

	public ProxyServer() {
	}

    public void peerUpdate() {
    }

    public void init() {
    }

    public void cleanup() {
    }

    public void error(String uri, String msg) {
        System.out.println(uri+": "+msg);
    }

	public void requestProxy(String host) {

        try {
            ProxyClient client = new ProxyClient();
            client.requestProxy();
            clientMap.put(host,client);
        }
        catch(Exception e) {
        }
    }

	public void requestProxyForPort(String host, int port) {

        try {
            ProxyClient client = new ProxyClient();
            client.requestProxy(port);
            clientMap.put(host,client);
        }
        catch(Exception e) {
        }
    }

    public Object getRequest(String host) {
        try {
            ProxyClient client = clientMap.get(host);
            return client.removeFirst();
        }
        catch(Exception e) {
        }
        return null;
    }

    public void fowardMessage(Message msg, Object args[]) {
        try {
            Messenger.postMessage(msg,args);
        }
        catch(Exception e) {
        }
    }

    public Object fowardRequest(String address, String method, String request) {

        String result=null;
        URLConnection connection = null;
        int responseCode=0;

        try {
            if(address.startsWith("https://")) {
                try {
                    java.security.Security.addProvider((java.security.Provider)Class.forName("com.sun.net.ssl.internal.ssl.Provider").newInstance());
                    Properties sysProps = System.getProperties();
                    sysProps.put("java.protocol.plugin.pkgs", "com.sun.net.ssl.internal.www.protocol");
                    System.setProperties(sysProps);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            URL url = new URL(address);
            connection = url.openConnection();

            if (method.equals("POST")) {
                connection.setDoOutput(true);
                connection.setAllowUserInteraction(true);

                OutputStreamWriter out = new OutputStreamWriter(
                                                new BufferedOutputStream(connection.getOutputStream()),
                                                "8859_1");
                out.write(request);
                connection.connect();
                out.flush();
                out.close();
            }
            else {
                connection.setDoOutput(false);
                connection.connect();
            }

            InputStreamReader reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream()));
            StringBuffer stringbuffer = new StringBuffer();

            int b = -1;
            while((b=reader.read())!=-1) {
                stringbuffer.append((char)b);
            }

            result = stringbuffer.toString();
            reader.close();
        }
        catch (Exception e) {

            result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><url>" +
                address + "</url><code>" + Integer.toString(responseCode) +
                "</code><desc>" + e.getMessage() + "</desc><timestamp>" +
                (new java.util.Date(System.currentTimeMillis()).toString()) +
                "</timestamp></status>";

            if (bDebug) {
                e.printStackTrace();
                System.out.println(result);
            }
        }
        return result;
    }

    public Object fowardRequestRaw(String address, String request) {

        String result=null;
        URLConnection connection = null;
        int responseCode=0;

        try {
            if(address.startsWith("https://")) {
                try {
                    java.security.Security.addProvider((java.security.Provider)Class.forName("com.sun.net.ssl.internal.ssl.Provider").newInstance());
                    Properties sysProps = System.getProperties();
                    sysProps.put("java.protocol.plugin.pkgs", "com.sun.net.ssl.internal.www.protocol");
                    System.setProperties(sysProps);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            responseCode=601;
            URL url = new URL(address);
            connection = url.openConnection();

            OutputStreamWriter out = new OutputStreamWriter(
                                            new BufferedOutputStream(connection.getOutputStream()),
                                            "8859_1");
            out.write(request);
            connection.connect();
            out.flush();
            out.close();

            InputStreamReader reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream()));
            StringBuffer stringbuffer = new StringBuffer();

            int b = -1;
            while((b=reader.read())!=-1) {
                stringbuffer.append((char)b);
            }

            result = stringbuffer.toString();
            reader.close();
        }
        catch (Exception e) {

            result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><status><url>" +
                address + "</url><code>" + Integer.toString(responseCode) +
                "</code><desc>" + e.getMessage() + "</desc><timestamp>" +
                (new java.util.Date(System.currentTimeMillis()).toString()) +
                "</timestamp></status>";

            if (bDebug) {
                e.printStackTrace();
                System.out.println(result);
            }
        }
        return result;
    }
}