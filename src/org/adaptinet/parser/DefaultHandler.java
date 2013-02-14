/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.parser;

import org.adaptinet.adaptinetex.ParserException;

public class DefaultHandler {
	public InputSource resolveEntity(String publicId, String systemId)
			throws ParserException {
		return null;
	}

	public void notationDecl(String name, String publicId, String systemId)
			throws ParserException {
	}

	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws ParserException {
	}

	public void startDocument() throws ParserException {
	}

	public void endDocument() throws ParserException {
	}

	public void startPrefixMapping(String prefix, String uri)
			throws ParserException {
	}

	public void endPrefixMapping(String prefix) throws ParserException {
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws ParserException {
	}

	public void endElement(String uri, String localName, String qName)
			throws ParserException {
	}

	public void characters(char ch[], int start, int length)
			throws ParserException {
	}

	public void ignorableWhitespace(char ch[], int start, int length)
			throws ParserException {
	}

	public void processingInstruction(String target, String data)
			throws ParserException {
	}

	public void skippedEntity(String name) throws ParserException {
	}

	public void warning(ParserException e) throws ParserException {
	}

	public void error(ParserException e) throws ParserException {
	}

	public void fatalError(ParserException e) throws ParserException {
		throw e;
	}

	public Object getObject() {
		return null;
	}
}
