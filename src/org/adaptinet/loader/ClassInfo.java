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
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

public class ClassInfo {

	private String className;

	private File file;

	public ClassInfo(String className, File file) {
		this.className = className;
		this.file = file;
	}

	public ClassInfo(String packageName, String classSimpleName, File file) {
		this.className = packageName + "." + classSimpleName;
		this.file = file;
	}

	public byte[] getClassBytes() {
		byte[] byteRet = null;

		if (file == null) {
			return byteRet;
		}

		try {
			if (isInZip()) {
				JarExtractor zipFile = new JarExtractor(file);
				String entryName = className.replace('.', '/') + ".class";
				byteRet = zipFile.getJarEntryBytes(entryName);
				zipFile.close();
			} else {
				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(file));
				ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
				byte buf[] = new byte[1024];
				int got = 0;
				while ((got = in.read(buf)) > 0) {
					out.write(buf, 0, got);
				}
				byteRet = out.toByteArray();
				in.close();
				out.close();
			}
		} catch (Exception e) {
		}

		return byteRet;
	}

	public String getClassName() {
		return className;
	}

	public String getClassSimpleName() {
		int pos = className.lastIndexOf('.');
		return className.substring(pos + 1);
	}

	public String getPackageName() {
		int pos = className.lastIndexOf('.');
		return className.substring(0, pos);
	}

	protected File getFile() {
		return file;
	}

	private boolean isInZip() {
		String fileName = file.getName().toLowerCase();
		if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
			return true;
		} else {
			return false;
		}
	}
}