/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.messaging;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


import org.adaptinet.adaptinetex.ParserException;
import org.adaptinet.parser.Attributes;
import org.adaptinet.parser.DefaultHandler;
import org.adaptinet.parser.XMLReader;
import org.adaptinet.xmltools.xmlutils.IXMLInputSerializer;
import org.adaptinet.xmltools.xmlutils.XMLSerializerFactory;


public class MessageParser extends DefaultHandler {

	protected static final int NONE = 0;
	protected static final int ENVELOPE = 1;
	protected static final int HEADER = 2;
	protected static final int MESSAGE = 3;
	protected static final int ID = 6;
	protected static final int ADDRESS = 7;
	protected static final int METHOD = 9;
	protected static final int PREFIX = 10;
	protected static final int BODY = 11;
	protected static final int VALUE = 12;
	protected static final int STRING = 13;
	protected static final int BOOLEAN = 14;
	protected static final int FLOAT = 15;
	protected static final int DOUBLE = 16;
	protected static final int DECIMAL = 17;
	protected static final int TIMEDURATION = 18;
	protected static final int RECURRINGDURATION = 19;
	protected static final int BINARY = 20;
	protected static final int URIREFERENCE = 21;
	protected static final int IDTYPE = 22;
	protected static final int IDREF = 23;
	protected static final int ENTITY = 24;
	protected static final int NOTATION = 25;
	protected static final int QNAME = 26;
	protected static final int LANGUAGE = 28;
	protected static final int ENTITIES = 29;
	protected static final int NMTOKEN = 30;
	protected static final int NMTOKENS = 31;
	protected static final int NAMETYPE = 32;
	protected static final int NCNAME = 33;
	protected static final int INTEGER = 34;
	protected static final int NONPOSITIVEINTEGER = 35;
	protected static final int NEGATIVEINTEGER = 36;
	protected static final int LONG = 37;
	protected static final int INT = 38;
	protected static final int SHORT = 39;
	protected static final int BYTE = 40;
	protected static final int NONNEGATIVEINTEGER = 41;
	protected static final int UNSIGNEDLONG = 42;
	protected static final int UNSIGNEDINT = 43;
	protected static final int UNSIGNEDSHORT = 44;
	protected static final int UNSIGNEDBYTE = 45;
	protected static final int POSITIVEINTEGER = 46;
	protected static final int TIMEINSTANT = 47;
	protected static final int TIME = 48;
	protected static final int TIMEPERIOD = 49;
	protected static final int DATE = 50;
	protected static final int MONTH = 51;
	protected static final int YEAR = 52;
	protected static final int CENTURY = 53;
	protected static final int RECURRINGDATE = 54;
	protected static final int RECURRINGDAY = 55;
	protected static final int ARRAY = 56;
	protected static final int PARAMETERS = 57;
	protected static final int IDREFS = 58;
	protected static final int STRUCT = 59;
	protected static final int NAME = 60;
	protected static final int HOST = 61;
	protected static final int PORT = 62;
	protected static final int TIMESTAMP = 63;
	protected static final int PLUGIN = 64;
	protected static final int BYTEARRAY = 65;
	protected static final int CHARARRAY = 66;
	protected static final int HOPS = 67;
	protected static final int TYPE = 68;
	protected static final int CERTIFICATE = 69;
	protected static final int KEY = 70;
	protected static final int EMAIL = 71;
	protected static final int POSTFIX = 72;
	private List<Object> list = null;
	private int type = 0;
	private int subtype = 0;
	private Envelope env = null;
	private Header header = null;
	private Body body = null;
	private Message message = null;
	private Address address = null;
	private Stack<List<Object>> stack = new Stack<List<Object>>();
	private Stack<String> arrayTypes = new Stack<String>();
	private Stack<Address> routes = new Stack<Address>();
	private int state = 0;
	private transient Object object = null;

	final public void startElement(String uri, String tag, String qtag,
			Attributes attrs) throws ParserException {

		try {
			switch (state) {
			case NONE:
				if (tag.equalsIgnoreCase("Envelope")) {
					state = ENVELOPE;
					env = new Envelope();
					object = env;
				}
				break;

			case HEADER:
				if (tag.equalsIgnoreCase("Message")) {
					state = MESSAGE;
					message = new Message();
				}
				break;

			case MESSAGE:
				if (tag.equalsIgnoreCase("To")) {
					state = ADDRESS;
					address = new Address();
				} else if (tag.equalsIgnoreCase("ReplyTo")) {
					state = ADDRESS;
					address = new Address();
				} else if (tag.equalsIgnoreCase("Route")) {
					state = ADDRESS;
					routes.push(address);
					address = new Address();
				} else if (tag.equalsIgnoreCase("ID")) {
					type = ID;
				} else if (tag.equalsIgnoreCase("Hops")) {
					type = HOPS;
				} else if (tag.equalsIgnoreCase("Key")) {
					type = KEY;
				} else if (tag.equalsIgnoreCase("Certificate")) {
					type = CERTIFICATE;
				} else if (tag.equalsIgnoreCase("Timestamp")) {
					type = TIMESTAMP;
				}
				break;

			case ADDRESS:
				if (tag.equalsIgnoreCase("Prefix")) {
					type = PREFIX;
				}
				if (tag.equalsIgnoreCase("Postfix")) {
					type = POSTFIX;
				} else if (tag.equalsIgnoreCase("Host")) {
					type = HOST;
				} else if (tag.equalsIgnoreCase("Port")) {
					type = PORT;
				} else if (tag.equalsIgnoreCase("Plugin")) {
					type = PLUGIN;
				} else if (tag.equalsIgnoreCase("Method")) {
					type = METHOD;
				} else if (tag.equalsIgnoreCase("Email")) {
					type = EMAIL;
				} else if (tag.equalsIgnoreCase("Type")) {
					type = TYPE;
				}
				break;

			case ENVELOPE:
				if (tag.equalsIgnoreCase("Header")) {
					state = HEADER;
					header = new Header();
				} else if (tag.equalsIgnoreCase("Body") == true) {
					state = BODY;
					body = new Body();
					list = new ArrayList<Object>();
				}
				break;

			case BODY: {
				setType(tag);
				if (type == ARRAY) {
					int index = attrs.getIndex(null, "type");
					if (index > -1) {
						String arraytype = attrs.getValue(index);
						if (arraytype.equalsIgnoreCase("byte")) {
							type = BYTEARRAY;
							return;
						} else if (arraytype.equalsIgnoreCase("char")) {
							type = CHARARRAY;
							return;
						}
						arrayTypes.push(convertType(arraytype));
					}
					stack.push(list);
					list = new ArrayList<Object>();
				} else {
					object = null;
				}
				break;
			}

			default:
				break;
			}
		} catch (Exception e) {
			throw new ParserException(e.getMessage());
		}
	}

	final public void characters(char buffer[], int start, int length)
			throws ParserException {

		try {
			switch (type) {
			case NONE:
				break;

			case ID:
				message.setID(new String(buffer, start, length));
				break;

			case HOPS:
				message.setHops(new String(buffer, start, length));
				break;

			case KEY:
				message.setKey(new String(buffer, start, length));
				break;

			case CERTIFICATE:
				message.setCertificate(new String(buffer, start, length));
				break;

			case TIMESTAMP:
				message.setTimeStamp(new String(buffer, start, length));
				break;

			case PREFIX:
				address.setPrefix(new String(buffer, start, length));
				break;

			case POSTFIX:
				address.setPostfix(new String(buffer, start, length));
				break;

			case HOST:
				address.setHost(new String(buffer, start, length));
				break;

			case PORT:
				address.setPort(new String(buffer, start, length));
				break;

			case PLUGIN:
				address.setPlugin(new String(buffer, start, length));
				break;

			case METHOD:
				address.setMethod(new String(buffer, start, length));
				break;

			case TYPE:
				address.setType(new String(buffer, start, length));
				break;

			case EMAIL:
				address.setEmail(new String(buffer, start, length));
				break;

			default:
				createType(buffer, start, length);
				break;
			}
			subtype = type;
			type = NONE;
		} catch (Exception e) {
			throw new ParserException(e.getMessage());
		}
	}

	final public void endElement(String uri, String name, String qname)
			throws ParserException {

		try {
			switch (state) {
			case HEADER:
				if (name.equalsIgnoreCase("Header")) {
					env.setHeader(header);
					state = ENVELOPE;
				}
				break;

			case BODY:
				if (name.equalsIgnoreCase("Array")) {
					if (subtype != BYTEARRAY && subtype != CHARARRAY) {
						Object array = null;
						try {
							String t = arrayTypes.pop();
							Class<?> c = Class.forName(t);
							int size = list.size();
							array = Array.newInstance(c, size);
							for (int i = 0; i < size; i++) {
								Array.set(array, i, list.get(i));
							}
						} catch (Exception e) {
						}
						list = stack.pop();
						if (array != null)
							list.add(array);
					}
				} else if (name.equalsIgnoreCase("Body")) {
					state = ENVELOPE;
					try {
						setContents(list);
					} catch (Exception e) {
						env = null;
						return;
					}
					env.setBody(body);
				} else if (object == null) {
					// Either there where no characters or something happened
					// In createType so we should place a null on the stack
					// As a place holder.
					list.add(object);
				}
				break;

			case MESSAGE:
				if (name.equalsIgnoreCase("Message")) {
					header.setMessage(message);
					state = HEADER;
				}
				break;

			case ADDRESS:
				if (name.equalsIgnoreCase("ReplyTo")) {
					message.setReplyTo(address);
					state = MESSAGE;
				} else if (name.equalsIgnoreCase("To")) {
					message.setAddress(address);
					state = MESSAGE;
				} else if (name.equalsIgnoreCase("Route")) {
					routes.peek().setRoute(address);
					address = routes.pop();
				}
				break;

			default:
				break;
			}
		} catch (Exception e) {
			throw new ParserException(e.getMessage());
		}
	}

	final protected void createType(char buffer[], int start, int length) {

		try {
			object = null;
			String data = XMLReader.normalize(buffer, start, length);
			switch (type) {

			case ARRAY:
				// This should not happen
				return;
			case CHARARRAY:
				object = data.toCharArray();
				break;
			case BYTEARRAY:
				object = data.getBytes();
				break;
			case STRING:
				object = new String(data);
				break;
			case BOOLEAN:
				if (data.equalsIgnoreCase("true"))
					object = new Boolean(true);
				else
					object = new Boolean(false);
				break;
			case FLOAT:
				object = new Float(Float.parseFloat(data));
				break;
			case DOUBLE:
				object = new Double(Double.parseDouble(data));
				break;
			case DECIMAL:
				object = new Double(Double.parseDouble(data));
				break;
			case INTEGER:
				object = new Integer(Integer.parseInt(data));
				break;
			case NONPOSITIVEINTEGER:
				object = new Integer(Integer.parseInt(data));
				break;
			case NEGATIVEINTEGER:
				object = new Integer(Integer.parseInt(data));
				break;
			case LONG:
				object = new Long(Long.parseLong(data));
				break;
			case INT:
				object = new Integer(Integer.parseInt(data));
				break;
			case SHORT:
				object = new Short(Short.parseShort(data));
				break;
			case BYTE:
				object = new Byte(Byte.parseByte(data));
				break;
			case NONNEGATIVEINTEGER:
				object = new Integer(Integer.parseInt(data));
				break;
			case UNSIGNEDLONG:
				object = new Long(Long.parseLong(data));
				break;
			case UNSIGNEDINT:
				object = new Integer(Integer.parseInt(data));
				break;
			case UNSIGNEDSHORT:
				object = new Short(Short.parseShort(data));
				break;
			case UNSIGNEDBYTE:
				object = new Byte(Byte.parseByte(data));
				break;
			case POSITIVEINTEGER:
				object = new Integer(Integer.parseInt(data));
				break;
			case TIMEDURATION:
				object = new Integer(Integer.parseInt(data));
				break;
			case RECURRINGDURATION:
				object = new Integer(Integer.parseInt(data));
				break;
			case BINARY:
				object = new Integer(Integer.parseInt(data));
				break;
			case DATE:
				object = new Integer(Integer.parseInt(data));
				break;
			case MONTH:
				object = new Integer(Integer.parseInt(data));
				break;
			case YEAR:
				object = new Integer(Integer.parseInt(data));
				break;
			case URIREFERENCE:
			case IDTYPE:
			case IDREF:
			case ENTITY:
			case NOTATION:
			case QNAME:
			case LANGUAGE:
			case IDREFS:
			case ENTITIES:
			case NMTOKEN:
			case NMTOKENS:
			case NAMETYPE:
			case NCNAME:
			case TIMEINSTANT:
			case TIME:
			case TIMEPERIOD:
			case CENTURY:
			case RECURRINGDATE:
			case RECURRINGDAY:
				object = data;
				break;
			case STRUCT: {
				try {
					IXMLInputSerializer serializer = XMLSerializerFactory
							.getInputSerializer();
					object = serializer.get(data);
				} catch (Exception e) {
					object = null;
				}
			}
				break;
			default:
				break;
			}
			if (object != null)
				list.add(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final protected void setType(String name) throws Exception {
		type = NONE;
		if (name.equalsIgnoreCase("Array"))
			type = ARRAY;
		else if (name.equalsIgnoreCase("string"))
			type = STRING;
		else if (name.equalsIgnoreCase("boolean"))
			type = BOOLEAN;
		else if (name.equalsIgnoreCase("float"))
			type = FLOAT;
		else if (name.equalsIgnoreCase("double"))
			type = DOUBLE;
		else if (name.equalsIgnoreCase("decimal"))
			type = DECIMAL;
		else if (name.equalsIgnoreCase("integer"))
			type = INTEGER;
		else if (name.equalsIgnoreCase("nonPositiveInteger"))
			type = NONPOSITIVEINTEGER;
		else if (name.equalsIgnoreCase("negativeInteger"))
			type = NEGATIVEINTEGER;
		else if (name.equalsIgnoreCase("long"))
			type = LONG;
		else if (name.equalsIgnoreCase("int"))
			type = INT;
		else if (name.equalsIgnoreCase("short"))
			type = SHORT;
		else if (name.equalsIgnoreCase("byte"))
			type = BYTE;
		else if (name.equalsIgnoreCase("nonNegativeInteger"))
			type = NONNEGATIVEINTEGER;
		else if (name.equalsIgnoreCase("unsignedLong"))
			type = UNSIGNEDLONG;
		else if (name.equalsIgnoreCase("unsignedInt"))
			type = UNSIGNEDINT;
		else if (name.equalsIgnoreCase("unsignedShort"))
			type = UNSIGNEDSHORT;
		else if (name.equalsIgnoreCase("unsignedByte"))
			type = UNSIGNEDBYTE;
		else if (name.equalsIgnoreCase("positiveInteger"))
			type = POSITIVEINTEGER;
		else if (name.equalsIgnoreCase("timeDuration"))
			type = TIMEDURATION;
		else if (name.equalsIgnoreCase("recurringDuration"))
			type = RECURRINGDURATION;
		else if (name.equalsIgnoreCase("binary"))
			type = BINARY;
		else if (name.equalsIgnoreCase("uriReference"))
			type = URIREFERENCE;
		else if (name.equalsIgnoreCase("ID"))
			type = IDTYPE;
		else if (name.equalsIgnoreCase("IDREF"))
			type = IDREF;
		else if (name.equalsIgnoreCase("ENTITY"))
			type = ENTITY;
		else if (name.equalsIgnoreCase("NOTATION"))
			type = NOTATION;
		else if (name.equalsIgnoreCase("QName"))
			type = QNAME;
		else if (name.equalsIgnoreCase("language"))
			type = LANGUAGE;
		else if (name.equalsIgnoreCase("IDREFS"))
			type = IDREFS;
		else if (name.equalsIgnoreCase("ENTITIES"))
			type = ENTITIES;
		else if (name.equalsIgnoreCase("NMTOKEN"))
			type = NMTOKEN;
		else if (name.equalsIgnoreCase("NMTOKENS"))
			type = NMTOKENS;
		else if (name.equalsIgnoreCase("Name"))
			type = NAMETYPE;
		else if (name.equalsIgnoreCase("NCName"))
			type = NCNAME;
		else if (name.equalsIgnoreCase("timeInstant"))
			type = TIMEINSTANT;
		else if (name.equalsIgnoreCase("time"))
			type = TIME;
		else if (name.equalsIgnoreCase("timePeriod"))
			type = TIMEPERIOD;
		else if (name.equalsIgnoreCase("date"))
			type = DATE;
		else if (name.equalsIgnoreCase("month"))
			type = MONTH;
		else if (name.equalsIgnoreCase("year"))
			type = YEAR;
		else if (name.equalsIgnoreCase("century"))
			type = CENTURY;
		else if (name.equalsIgnoreCase("recurringDate"))
			type = RECURRINGDATE;
		else if (name.equalsIgnoreCase("recurringDay"))
			type = RECURRINGDAY;
		else if (name.equalsIgnoreCase("Struct"))
			type = STRUCT;
		else
			throw new Exception("Invalid Format");
	}

	static String convertType(String name) {
		if (name.equalsIgnoreCase("string"))
			return "java.lang.String";
		if (name.equalsIgnoreCase("boolean"))
			return "java.lang.Boolean";
		if (name.equalsIgnoreCase("float"))
			return "java.lang.Float";
		if (name.equalsIgnoreCase("double"))
			return "java.lang.Double";
		if (name.equalsIgnoreCase("integer"))
			return "java.lang.Integer";
		if (name.equalsIgnoreCase("long"))
			return "java.lang.Long";
		if (name.equalsIgnoreCase("short"))
			return "java.lang.Short";
		if (name.equalsIgnoreCase("byte"))
			return "java.lang.Byte";
		if (name.equalsIgnoreCase("char"))
			return "java.lang.Char";

		return name;
	}

	protected void setContents(List<Object> contents) throws Exception {
		body.setcontent(contents);
	}

	final protected Body getBody() {
		return body;
	}

	final protected List<Object> getList() {
		return list;
	}

	final protected int getType() {
		return type;
	}

	final protected void add(Object o) {
		list.add(o);
	}

	final public Object getObject() {
		return env;
	}

}