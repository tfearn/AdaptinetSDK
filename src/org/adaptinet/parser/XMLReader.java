/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.adaptinet.adaptinetex.ParserException;

public class XMLReader extends org.adaptinet.parser.BaseParser {

	protected Attributes attrs = new Attributes();

	protected HashMap<String, String> namespaces = new HashMap<String, String>();

	protected String tname = null;

	protected String turi = null;

	protected Object obj = null;

	protected DefaultHandler handler = null;

	protected DefaultHandler errorHandler = null;

	static public void main(String args[]) {
		try {
			File findFile = new File(args[1]);
			BufferedInputStream is = new BufferedInputStream(
					new FileInputStream(findFile));
			XMLReader xr = new XMLReader();
			xr.setContentHandler(new DefaultHandler());
			xr.parse(new InputSource(is));
		} catch (Exception e) {
		}
	}

	public XMLReader() {
	}

	public void setContentHandler(DefaultHandler handler) {
		this.handler = handler;
	}

	public void setErrorHandler(DefaultHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public Object parse(InputSource in) throws ParserException {

		obj = null;
		if ((is = in.getCharacterStream()) == null) {
			is = new InputStreamReader(in.getByteStream());
		}

		try {
			handler.startDocument();
			ch = is.read();
			while (ch != -1) {
				if (ch == '<') {
					skipWhiteSpace();
					ch = is.read();
					parseElement();
				} else {
					ch = is.read();
				}
			}
			handler.endDocument();
			obj = handler.getObject();
		} catch (IndexOutOfBoundsException e) {
			if (errorHandler != null) {
				errorHandler.error(new ParserException(e.getMessage()));
			} else {
				handler.error(new ParserException(e.getMessage()));
			}
		} catch (Exception e) {
			if (errorHandler != null) {
				errorHandler.error(new ParserException(e.getMessage()));
			} else {
				handler.error(new ParserException(e.getMessage()));
			}
		}
		return obj;
	}

	protected final void parseElement() throws Exception {

		String name = null;
		String qname = null;
		String uri = null;
		String auri = null;
		String tag = null;
		String raw = null;
		String value = null;

		attrs.clear();
		if (ch == '/') {
			ch = is.read();
			qname = getWord();
			nSize = 0;
			parseName(qname);
			name = tname;
			uri = turi;
			tname = null;
			turi = null;
			skipUntil('>');
			handler.endElement(uri, name, qname);
		} else {
			qname = getWord();
			nSize = 0;
			parseName(qname);
			name = tname;
			uri = turi;
			tname = null;
			turi = null;
			skipWhiteSpace();

			if (ch != -1 && ch != '>' && ch != '/') {
				do {
					append((char) ch);
					ch = is.read();
					if (ch == '=') {
						raw = getBuffer();
						nSize = 0;
						parseName(raw);
						tag = tname;
						auri = turi;
						tname = null;
						turi = null;
						skipUntil('"');
						ch = is.read();

						while (ch != -1 && ch != '>') {
							if (ch == '"') {
								ch = is.read();
								value = getBuffer();

								if (auri != null
										&& (auri.equalsIgnoreCase("xmlns") || auri
												.equalsIgnoreCase("xsi"))) {
									namespaces.put(tag, value);
								} else {
									if (auri != null) {
										auri = namespaces.get(auri);
									}
									attrs.addAttribute(auri, tag, raw, "",
											value);
								}
								nSize = 0;
								break;
							}
							append((char) ch);
							ch = is.read();
						}
						skipWhiteSpace();
					}
				} while (ch != -1 && ch != '>' && ch != '/');
			}

			if (uri != null) {
				uri = namespaces.get(uri);
			}

			if (isPI(name.getBytes())) {
				handler.processingInstruction(name, null);
			} else {
				handler.startElement(uri, name, qname, attrs);
			}

			if (ch == '/') {
				skipUntil('>');
				handler.endElement(uri, name, qname);
				return;
			}

			nSize = 0;
			if (ch == '>') {
				ch = is.read();
				while (ch != -1 && ch != '<') {
					append((char) ch);
					ch = is.read();
				}
			}

			if (nSize > 0) {
				handler.characters(buffer, 0, nSize);
			}
		}
	}

	protected final void parseName(String name) throws Exception {

		int colon = name.indexOf(':');
		switch (colon) {
		case -1:
			turi = null;
			tname = name;
			break;

		case 0:
			throw new Exception("error name can not start with a colon");

		default:
			turi = name.substring(0, colon);
			tname = name.substring(colon + 1);
			break;
		}
	}

	static public String normalize(char chars[], int start, int len)
			throws java.io.UnsupportedEncodingException {

		int j = start;
		for (int i = 0; i < len; i++, j++) {
			if (chars[i] == '&') {
				if (chars[i + 1] == 'l' && chars[i + 2] == 't'
						&& chars[i + 3] == ';') {
					chars[j] = '<';
					i += 3;
				} else if (chars[i + 1] == 'g' && chars[i + 2] == 't'
						&& chars[i + 3] == ';') {
					chars[j] = '>';
					i += 3;
				} else if (chars[i + 1] == 'a' && chars[i + 2] == 'm'
						&& chars[i + 3] == 'p' && chars[i + 4] == ';') {
					chars[j] = '&';
					i += 4;
				} else if (chars[i + 1] == 'q' && chars[i + 2] == 'u'
						&& chars[i + 3] == 'o' && chars[i + 4] == 't'
						&& chars[i + 5] == ';') {
					chars[j] = '"';
					i += 5;
				} else if ((chars[i + 1] == '#' && chars[i + 3] == ';')
						&& (chars[i + 2] == '\r' || chars[i + 2] == '\n')) {
					chars[j] = chars[i + 2];
					i += 3;
				} else {
					chars[j] = chars[i];
				}
			} else {
				chars[j] = chars[i];
			}
		}
		return new String(chars, 0, j);
	}

	static public boolean isPI(byte chars[]) {
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '?') {
				return true;
			} else if (chars[i] != '<' && chars[i] != ' ') {
				break;
			}
		}
		return false;
	}
}
