/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.xmlutils;

import org.adaptinet.xmltools.xmlutils.IXMLInputSerializer;
import org.adaptinet.xmltools.xmlutils.IXMLOutputSerializer;

/**
 * <p>
 * Title: XMLSerializerFactory
 * </p>
 * <p>
 * Description: The Serializer Factory has methods to create the proper
 * serializer for your based on the version of the JRE it is running in.
 * </p>
 * <p>
 * Company: Adaptinet
 * </p>
 * 
 * @author amg
 * @version 1.0
 */
public class XMLSerializerFactory {
	private static boolean bJava2 = false;

	/**
	 * A private constructor is defined to prevent instantiation of a factory
	 * object.
	 */
	private XMLSerializerFactory() {
	}

	/**
	 * This static method will create a proper input serializer for you and
	 * return the interface to you.
	 */
	public static IXMLInputSerializer getInputSerializer() throws Exception {
		Class<?> x = null;
		if (bJava2) {
			x = Class.forName("org.adaptinet.xmlutils.XMLInputSerializer");
		} else {
			x = Class.forName("org.adaptinet.jvm11x.xmlutils.XMLInputSerializer");
		}
		IXMLInputSerializer in = (IXMLInputSerializer) x.newInstance();
		return in;
	}

	/**
	 * This static method will create a proper input serializer for you and
	 * return the interface to you.
	 */
	public static IXMLOutputSerializer getOutputSerializer() throws Exception {
		Class<?> x = null;
		if (bJava2) {
			x = Class.forName("org.adaptinet.xmlutils.XMLOutputSerializer");
		} else {
			x = Class
					.forName("org.adaptinet.jvm11x.xmlutils.XMLOutputSerializer");
		}
		IXMLOutputSerializer out = (IXMLOutputSerializer) x.newInstance();
		return out;
	}

	public static IXMLOutputSerializer getOutputSerializerVerbose()
			throws Exception {
		Class<?> x = null;
		if (bJava2) {
			x = Class
					.forName("org.adaptinet.xmlutils.XMLOutputSerializerVerbose");
		} else {
			x = Class
					.forName("org.adaptinet.jvm11x.xmlutils.XMLOutputSerializerVerbose");
		}
		IXMLOutputSerializer out = (IXMLOutputSerializer) x.newInstance();
		return out;
	}

	static {
		try {
			bJava2 = true;
			Class.forName("java.lang.reflect.AccessibleObject");
		} catch (ClassNotFoundException cnfe) {
			bJava2 = false;
		}
	}
}