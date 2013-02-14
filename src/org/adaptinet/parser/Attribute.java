/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.parser;

public class Attribute {
	public static final String LNULLATTRIBUTE = "";

	public static final String LABSTRACT = "abstract";

	public static final String LATTRIBUTEFORMDEFAULT = "attributeformdefault";

	public static final String LBASE = "base";

	public static final String LBLOCK = "block";

	public static final String LBLOCKDEFAULT = "blockdefault";

	public static final String LELEMENTFORMDEFAULT = "elementformdefault";

	public static final String LFINAL = "fina";

	public static final String LFINALDEFAULT = "finaldefault";

	public static final String LFIXED = "fixed";

	public static final String LFORM = "form";

	public static final String LITEMTYPE = "itemtype";

	public static final String LMEMBERTYPES = "membertypes";

	public static final String LMAXOCCURS = "maxoccurs";

	public static final String LMINOCCURS = "minoccurs";

	public static final String LMIXED = "mixed";

	public static final String LNAME = "name";

	public static final String LNAMESPACE = "namespace";

	public static final String LNONAMESPACESCHEMALOCATION = "nonamespaceschemalocation";

	public static final String LXSINULL = "xsi:nul";

	public static final String LNULLABLE = "nullable";

	public static final String LPROCESSCONTENTS = "processcontents";

	public static final String LREF = "ref";

	public static final String LSCHEMALOCATION = "schemalocation,";

	public static final String LXSISCHEMALOCATION = "xsi:schemalocation";

	public static final String LSUBSTITUTIONGROUP = "substitutiongroup";

	public static final String LTARGETNAMESPACE = "targetnamespace";

	public static final String LTYPE = "type";

	public static final String LXSITYPE = "xsi:type";

	public static final String LUSE = "use";

	public static final String LVALUE = "value";

	public static final String LXPATH = "xpath";

	public static final short UNKNOWN = 1;

	public static final short ABSTRACT = 2;

	public static final short ATTRIBUTEFORMDEFAULT = 3;

	public static final short BASE = 4;

	public static final short BLOCK = 5;

	public static final short BLOCKDEFAULT = 6;

	public static final short ELEMENTFORMDEFAULT = 7;

	public static final short FINAL = 8;

	public static final short FINALDEFAULT = 9;

	public static final short FIXED = 10;

	public static final short FORM = 11;

	public static final short ITEMTYPE = 12;

	public static final short MEMBERTYPES = 13;

	public static final short MAXOCCURS = 14;

	public static final short MINOCCURS = 15;

	public static final short MIXED = 16;

	public static final short NAME = 17;

	public static final short NAMESPACE = 18;

	public static final short NONAMESPACESCHEMALOCATION = 19;

	public static final short XSINULL = 20;

	public static final short NULLABLE = 21;

	public static final short PROCESSCONTENTS = 22;

	public static final short REF = 23;

	public static final short SCHEMALOCATION = 24;

	public static final short XSISCHEMALOCATION = 25;

	public static final short SUBSTITUTIONGROUP = 26;

	public static final short TARGETNAMESPACE = 27;

	public static final short TYPE = 28;

	public static final short XSITYPE = 29;

	public static final short USE = 30;

	public static final short VALUE = 31;

	public static final short XPATH = 32;

	public Attribute(String type, String value) {
		this.value = value;
		this.type = convertType(type);
	}

	static Short convertType(String t) {
		short retType = UNKNOWN;

		if (t == LNAME)
			retType = NAME;
		else if (t == LREF)
			retType = REF;
		else if (t == LABSTRACT)
			retType = ABSTRACT;
		else if (t == LATTRIBUTEFORMDEFAULT)
			retType = ATTRIBUTEFORMDEFAULT;
		else if (t == LBASE)
			retType = BASE;
		else if (t == LBLOCK)
			retType = BLOCK;
		else if (t == LBLOCKDEFAULT)
			retType = BLOCKDEFAULT;
		else if (t == LELEMENTFORMDEFAULT)
			retType = ELEMENTFORMDEFAULT;
		else if (t == LFINAL)
			retType = FINAL;
		else if (t == LFINALDEFAULT)
			retType = FINALDEFAULT;
		else if (t == LFIXED)
			retType = FIXED;
		else if (t == LFORM)
			retType = FORM;
		else if (t == LITEMTYPE)
			retType = ITEMTYPE;
		else if (t == LMEMBERTYPES)
			retType = MEMBERTYPES;
		else if (t == LMAXOCCURS)
			retType = MAXOCCURS;
		else if (t == LMINOCCURS)
			retType = MINOCCURS;
		else if (t == LMIXED)
			retType = MIXED;
		else if (t == LNAMESPACE)
			retType = NAMESPACE;
		else if (t == LNONAMESPACESCHEMALOCATION)
			retType = NONAMESPACESCHEMALOCATION;
		else if (t == LXSINULL)
			retType = XSINULL;
		else if (t == LNULLABLE)
			retType = NULLABLE;
		else if (t == LPROCESSCONTENTS)
			retType = PROCESSCONTENTS;
		else if (t == LSCHEMALOCATION)
			retType = SCHEMALOCATION;
		else if (t == LXSISCHEMALOCATION)
			retType = XSISCHEMALOCATION;
		else if (t == LSUBSTITUTIONGROUP)
			retType = SUBSTITUTIONGROUP;
		else if (t == LTARGETNAMESPACE)
			retType = TARGETNAMESPACE;
		else if (t == LTYPE)
			retType = TYPE;
		else if (t == LXSITYPE)
			retType = XSITYPE;
		else if (t == LUSE)
			retType = USE;
		else if (t == LVALUE)
			retType = VALUE;
		else if (t == LXPATH)
			retType = XPATH;

		return new Short(retType);
	}

	static String convertType(Short t) {
		return convertType(t.shortValue());
	}

	static String convertType(short t) {
		switch (t) {
		case NAME:
			return LNAME;
		case REF:
			return LREF;
		case ABSTRACT:
			return LABSTRACT;
		case ATTRIBUTEFORMDEFAULT:
			return LATTRIBUTEFORMDEFAULT;
		case BASE:
			return LBASE;
		case BLOCK:
			return LBLOCK;
		case BLOCKDEFAULT:
			return LBLOCKDEFAULT;
		case ELEMENTFORMDEFAULT:
			return LELEMENTFORMDEFAULT;
		case FINAL:
			return LFINAL;
		case FINALDEFAULT:
			return LFINALDEFAULT;
		case FIXED:
			return LFIXED;
		case FORM:
			return LFORM;
		case ITEMTYPE:
			return LITEMTYPE;
		case MEMBERTYPES:
			return LMEMBERTYPES;
		case MAXOCCURS:
			return LMAXOCCURS;
		case MINOCCURS:
			return LMINOCCURS;
		case MIXED:
			return LMIXED;
		case NAMESPACE:
			return LNAMESPACE;
		case NONAMESPACESCHEMALOCATION:
			return LNONAMESPACESCHEMALOCATION;
		case XSINULL:
			return LXSINULL;
		case NULLABLE:
			return LNULLABLE;
		case PROCESSCONTENTS:
			return LPROCESSCONTENTS;
		case SCHEMALOCATION:
			return LSCHEMALOCATION;
		case XSISCHEMALOCATION:
			return LXSISCHEMALOCATION;
		case SUBSTITUTIONGROUP:
			return LSUBSTITUTIONGROUP;
		case TARGETNAMESPACE:
			return LTARGETNAMESPACE;
		case TYPE:
			return LTYPE;
		case XSITYPE:
			return LXSITYPE;
		case USE:
			return LUSE;
		case VALUE:
			return LVALUE;
		case XPATH:
			return LXPATH;
		default:
			return LNULLATTRIBUTE;
		}
	}

	public String getValue() {
		return value;
	}

	public Short getType() {
		return type;
	}

	private Short type;

	private String value;
}
