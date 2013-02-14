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
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathLoader extends ClassLoader {
	
	public static boolean bAutoReload = true;

	private String classpath = "";

	private Hashtable<String, ClassInfo> classInfoMap = new Hashtable<String, ClassInfo>();

	protected static final boolean DEBUG = true;

	public ClasspathLoader(String classpath) {
		this.classpath = (classpath != null) ? classpath : "";
	}

	public ClasspathLoader(String classpath, ClassLoader parent) {
		super(parent);
		this.classpath = (classpath != null) ? classpath : "";
	}

	/**
	 * Retrieves the local classpath being used by this class loader
	 * 
	 * @return local classpath used by this loader
	 */
	public String getLocalClasspath() {
		return classpath;
	}

	/**
	 * Appends the string argument to the local classpath. Method insures that
	 * the new path entries are unique with respect to the current local
	 * classpath.
	 * 
	 * @param cp
	 *            classpath string to append
	 * @return String representing the entries that were appended (duplicates
	 *         removed).
	 */
	public synchronized String appendLocalClasspath(String cp) {
		if (cp == null) {
			return "";
		}
		StringBuffer buff = new StringBuffer();
		StringTokenizer tok = new StringTokenizer(cp, File.pathSeparator);
		String path;
		while (tok.hasMoreTokens()) {
			path = tok.nextToken();
			// Make sure the new entry is unique with respect to current
			// classpath
			if (classpath.indexOf(path) == -1) {
				if (buff.length() > 0) {
					buff.append(File.pathSeparatorChar);
				}
				buff.append(path);
			}
		}
		String newEntries = buff.toString();
		if (classpath.length() > 0) {
			classpath += File.pathSeparatorChar + newEntries;
		} else {
			classpath = newEntries;
		}
		return newEntries;
	}

	@SuppressWarnings("unchecked")
	protected Class findClass(String name) throws ClassNotFoundException {
		Class c = findLocalClass(name);
		if (c == null) {
			throw new ClassNotFoundException(name);
		}
		return c;
	}

	/*
	 * This commented method contains the actual logic that this loader should
	 * exhibit. Unfortunately there are issues wherein casting fails when a
	 * class can be loaded from both the parent loader and this loader. (ie: If
	 * the same path entry exists in both the system classpath and this class
	 * loader's local classpath then an exception will occur when a cast is
	 * attempted.) To resolve this issue it was necessary to let the parent
	 * attempt to load the class before any attempt is made by this loader. The
	 * new logic is present in findClass(...) above. If sometime in the future
	 * this issue is resolved, remove the findClass(...) method and restore this
	 * one.
	 */
	/*
	 * protected synchronized Class loadClass(String name, boolean resolve)
	 * throws ClassNotFoundException { //First, check if the class has already
	 * been loaded Class c = findLoadedClass(name); if( c == null ) { // Next,
	 * try to load the class from our local classpath c = findLocalClass(name);
	 * if( c == null ) { // Finally, delegate to the superclass c =
	 * super.loadClass(name, false); } } if (resolve) { resolveClass(c); }
	 * return c; }
	 */

	protected ClassInfo findClassInfo(String className) {
		return classInfoMap.get(className);
	}

	protected boolean removeClassInfo(String className) {
		return false;
	}

	/**
	 * Method should be overridden by subclasses that wish to specialize a
	 * <code>ClassInfo</code> instance
	 */
	protected ClassInfo createClassInfo(String className) {
		ClassInfo entry = null;
		File file = findClassFile(className);
		if (file != null) {
			entry = new ClassInfo(className, file);
		}
		return entry;
	}

	protected File findClassFile(String className) {
		File file = null;
		try {
			String zipEntryName = className.replace('.', '/') + ".class"; // Zip
			// files
			// always
			// use
			// '/'
			String classFileName = className.replace('.', File.separatorChar)
					+ ".class";
			StringTokenizer classToken = new StringTokenizer(classpath,
					File.pathSeparator);
			while (classToken.hasMoreTokens()) {
				String filename = classToken.nextToken();
				String filenameLower = filename.toLowerCase();
				if (filenameLower.endsWith(".zip")
						|| filenameLower.endsWith(".jar")) {
					try {
						JarFile jarFile = new JarFile(filename);
						JarEntry j = jarFile.getJarEntry(zipEntryName);
						if (j != null) {
							file = new File(filename);
							break;
						}
					} catch (IOException e) {
					}
				} else {
					if (!filename.endsWith(File.separator)) {
						filename += File.separatorChar;
					}

					File f = new File(filename + classFileName);
					if (f.exists()) {
						file = f;
						break;
					}
				}
			}
		} catch (Exception e) {
		}

		/*
		 * if( file != null ) { trace("** Class " + className + " found in file " +
		 * file.toString() ); }
		 */
		return file;
	}

	private Class<?> findLocalClass(String name) {
		Class<?> cl = null;
		ClassInfo entry = createClassInfo(name);
		if (entry != null) {
			cl = loadFromInfo(entry);
			if (cl != null) {
				classInfoMap.put(entry.getClassName(), entry);
				// trace("** Class " + entry.getClassName() + " loaded from
				// local classpath ");
			}
		}
		return cl;
	}

	private Class<?> loadFromInfo(ClassInfo entry) {
		Class<?> cl = null;
		byte[] bytes = entry.getClassBytes();
		if (bytes != null) {
			try {
				cl = this.defineClass(entry.getClassName(), bytes, 0,
						bytes.length);
			} catch (Throwable th) {
				trace("** Failed to defineClass:" + entry.getClassName()
						+ "  Reason:" + th.getMessage());
			}
		}
		return cl;
	}

	private void trace(String msg) {
		if (DEBUG) {
			System.out.println(msg);
		}
	}

	public static void main(String[] args) {
		try {
			// ofx200.repository.ofx200.OFX ofx;

			/*
			 * String classpath = "c:\\Adaptinet\\stage\\samples.jar;";// +
			 * System.getProperty("java.class.path");
			 * 
			 * ClasspathLoader loader = new ClasspathLoader(classpath); Class cl =
			 * loader.loadClass("chat.Chat"); ClassInfo info =
			 * loader.findClassInfo(cl.getName());
			 * 
			 * Object obj = cl.newInstance(); int x = 0;
			 */

			ClasspathLoader loader = new ClasspathLoader(null);
			loader
					.appendLocalClasspath("D:\\KaanBaan\\Projects\\OFXRepository\\classes;one\\two;three\\four");
			@SuppressWarnings("unused")
			ClassLoader thisLoader = loader.getClass().getClassLoader();
			@SuppressWarnings("unused")
			ClassLoader anotherLoader = ClassLoader.getSystemClassLoader();

			// ClassLoader loader = ClassLoader.getSystemClassLoader();
			Class<?> cl = loader.loadClass("ofx200.repository.ofx200.OFX");
			// Thread.sleep(15000);
			// cl= null;
			// cl = loader.loadClass("repository.KBCom.CPU");
			// cl = loader.loadClass("java.util.BitSet");
			// cl = loader.loadClass("java.util.BitSet");
			// cl = loader.loadClass("repository.KBCom.Computer");
			@SuppressWarnings("unused")
			Object obj = cl.newInstance();

			// ofx = (ofx200.repository.ofx200.OFX)obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}