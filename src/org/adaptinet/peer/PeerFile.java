/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.adaptinet.peer.PeerRoot;


public class PeerFile {

	private static final String DEFAULT = "DefaultPeers.xml";
	private boolean bOpen = false;
	private File file;
	private boolean bDefault = false;
	private boolean bDirty = false;

	public boolean isOpen() {
		return bOpen;
	}

	public void setOpen(boolean bOpen) {
		this.bOpen = bOpen;
	}

	public boolean isDefault() {
		return bDefault;
	}

	public void setDefault(boolean bDefault) {
		this.bDefault = bDefault;
	}

	public boolean isDirty() {
		return bDirty;
	}

	public void setDirty(boolean bDirty) {
		this.bDirty = bDirty;
	}

	public void openFile(final String name) throws Exception {

	}	
	
	public PeerRoot open() throws Exception {
		this.bDefault = true;
		return open(DEFAULT);
	}
	
	public PeerRoot open(String name) throws Exception {

		if (name == null || name.length() == 0) {
			return open();
		}
		
		PeerRoot tmpRoot = new PeerRoot();
		FileInputStream fis = null;
		try {
			if (name.equalsIgnoreCase(DEFAULT)) {
				File defaultfile = new File(name);
				bDefault = true;
				if (!file.exists()) {
					throw new IOException(
							"error Default peer file does not exist.");
				}
				fis = new FileInputStream(defaultfile);
			} else {
				file = new File(name);

				if (!file.exists()) {
					new File(name.substring(0, name
							.lastIndexOf(File.separatorChar))).mkdirs();
					file.createNewFile();
				}
				fis = new FileInputStream(file);
				bOpen = true;
			}
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);

			tmpRoot.parsePeers(bytes);
			// Check to see if there are any peers if not go to the default
			// peer file that should have been included in the installation
			if (tmpRoot.count() < 1 && !bDefault) {
				return open();
			}
			return tmpRoot;
		} catch (IOException x) {
			x.printStackTrace();
			throw x;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	public void close() {
		bOpen = false;
	}

	public void save(String data) throws IOException {

		FileOutputStream fos = null;
		try {
			file.renameTo(new File(file.getName() + "~"));
			fos = new FileOutputStream(file);
			fos.write(data.getBytes());
			bDirty = false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fos.close();
		}
	}
}
