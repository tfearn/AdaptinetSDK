/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.loader;

import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarExtractor extends JarFile {
	
	public JarExtractor(File file) throws IOException {
		super(file);
	}

	public JarExtractor(File file, boolean verify) throws IOException {
		super(file, verify);
	}

	public JarExtractor(String name) throws IOException {
		super(name);
	}

	public JarExtractor(String name, boolean verify) throws IOException {
		super(name, verify);
	}

	/**
	 * Retrieves the named entry as a byte array.
	 * 
	 * @return Returns a byte array containing the contents of the JarEntry or
	 *         null if the item is not found.
	 */
	public byte[] getJarEntryBytes(String name) {
		byte[] bytes = null;
		JarEntry jarEntry = getJarEntry(name);
		if (jarEntry != null) {
			try {
				InputStream in = getInputStream(jarEntry);
				int len;
				byte[] data = new byte[1024];
				ByteArrayOutputStream buff = new ByteArrayOutputStream();
				while ((len = in.read(data)) != -1) {
					buff.write(data, 0, len);
				}

				// System.out.println("\nContents of JarEntry " +
				// jarEntry.getName() );
				// System.out.println("**** Begin Byte Array
				// ******************************");
				// buff.writeTo( System.out );
				// System.out.println("**** End Byte Array
				// ********************************");

				bytes = buff.toByteArray();

				in.close();
				buff.close();
			} catch (IOException e) {
			}
		}

		return bytes;
	}
}