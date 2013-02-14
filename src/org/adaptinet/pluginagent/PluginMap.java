/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.pluginagent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.adaptinetex.PluginException;
import org.adaptinet.loader.ClasspathLoader;
import org.adaptinet.messaging.Envelope;
import org.adaptinet.messaging.Message;
import org.adaptinet.transceiver.ITransceiver;


public class PluginMap {
	
	protected Plugin pluginObj;

	private Hashtable<String, Method> methods = new Hashtable<String, Method>();

	public PluginMap() {
	}

	@SuppressWarnings("unchecked")
	public void createInstance(String strName, ClasspathLoader loader)
			throws AdaptinetException {

		Class<Plugin> pluginClass = null;
		try {

			if (loader == null)
				pluginClass = (Class<Plugin>)Class.forName(strName);
			else {
				pluginClass = (Class<Plugin>)loader.loadClass(strName);
			}
			if (pluginClass == null) {
				AdaptinetException pluginex = new AdaptinetException(
						AdaptinetException.SEVERITY_FATAL,
						AdaptinetException.GEN_CLASSNOTFOUND);
				pluginex.logMessage("Class not found error class name is "
						+ strName);
				throw pluginex;
			}

			pluginObj = (Plugin) pluginClass.newInstance();
			if (pluginObj == null) {
				AdaptinetException pluginex = new AdaptinetException(
						AdaptinetException.SEVERITY_FATAL,
						AdaptinetException.GEN_CLASSNOTFOUND);
				pluginex.logMessage("Unable to create instance of class "
						+ strName);
				throw pluginex;
			}

			pluginObj.setTransceiver(ITransceiver.getTransceiver());
			Method[] methodarray = pluginClass.getMethods();
			int length = methodarray.length;
			for (int i = 0; i < length; i++) {
				methods.put(methodarray[i].getName(), methodarray[i]);
			}
		} catch (AdaptinetException e) {
			throw e;
		} catch (Exception e) {
			PluginException pluginex = new PluginException(
					AdaptinetException.SEVERITY_FATAL,
					PluginException.ANT_CREATEINSTANCEFAILURE);
			pluginex.logMessage("Class not found error class name is "
					+ strName + " reason " + e.getMessage());
			throw pluginex;
		}
	}

	public void setCurrentMessage(Message msg) {
		pluginObj.setCurrentMessage(msg);
	}

	public boolean preProcessMessage(Envelope env) {
		return pluginObj.preProcessMessage(env);
	}

	public void setAgent(PluginAgent agent) {
		pluginObj.setAgent(agent);
	}

	public Object executeMethod(String name, Object args[], boolean bLogError)
			throws AdaptinetException {

		Object ret = null;

		try {
			if (name != null && name.length() > 0) {
				Method m = methods.get(name);
				if (m == null) {
					PluginException pluginex = new PluginException(
							AdaptinetException.SEVERITY_FATAL,
							PluginException.ANT_METHODNOTSUPPORTED);
					if (bLogError == true) {
						pluginex.logMessage("Illegal method name value is "
								+ name);
					}
					throw pluginex;
				}
				ret = m.invoke(pluginObj, args);
			}
		} catch (AdaptinetException e) {
			throw e;
		} catch (InvocationTargetException e) {
			PluginException pluginex = new PluginException(
					AdaptinetException.SEVERITY_FATAL, 999);
			if (bLogError == true) {
				pluginex.logMessage("Error executing method " + name
						+ " InvocationTargetException thrown: " + e);
			}
			Throwable ex = e.getTargetException();
			if (bLogError == true) {
				pluginex.logMessage("Error executing method " + name
						+ " Target exception thrown: " + ex.getMessage());
			}
			ex.printStackTrace();
			throw pluginex;
		} catch (Exception e) {
			PluginException pluginex = new PluginException(
					AdaptinetException.SEVERITY_FATAL, 999);
			if (bLogError == true) {
				pluginex.logMessage("Error executing method " + name
						+ " exception thrown: " + e);
			}
			throw pluginex;
		}
		return ret;
	}

	public void peerUpdate() {
		pluginObj.peerUpdate();
	}
}
