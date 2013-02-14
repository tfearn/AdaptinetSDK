/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package Samples.Filer;

import javax.swing.*;

import org.adaptinet.adaptinetex.*;
import org.adaptinet.messaging.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import sun.misc.*;


public class Filer extends org.adaptinet.pluginagent.Plugin {
	public static final String MAX_ACKNOWLEDGEMENTS = "10";

	public static final String MAX_SEARCH_SECONDS = "3";

	public static final String DEFAULT_PACKET_SIZE = "102400";

	boolean packFrame = false;

	FilerFrame frame = null;

	java.util.List<FindResult> foundList = null;

	int acks = 0;

	boolean killSearch = false;

	Properties properties = null;

	long totalPacketsRecvd = 0;

	/** Construct the application */
	public Filer() {
	}

	// This method is called by the transceiver for initialization
	public void init() {
		// Get the properties file
		File file = findFileFromClassPath("filer.properties");
		if (file == null)
			return;
		properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}

		frame = new FilerFrame();
		// Validate frames that have preset sizes
		// Pack frames that have useful preferred size info, e.g. from their
		// layout
		if (packFrame) {
			frame.pack();
		} else {
			frame.validate();
		}
		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2,
				(screenSize.height - frameSize.height) / 2);
		frame.setVisible(true);
		frame.setPlugin(this);

		// Set the title of the frame to include my hostname and port
		Address address = new Address(this.transceiver);
		frame.setTitle("Adaptinet Filer - " + address.getHost() + ":"
				+ address.getPort());

		// Create a synchronized list of peers that acknowledged that they
		// have the file
		foundList = Collections.synchronizedList(new ArrayList<FindResult>());
	}

	// This method is called by the transceiver for cleanup
	public void cleanup() {
	}

	// This method is called from the Frame when the Get File button
	// is pressed
	public void getFileButton(String fileName) {
		try {
			frame.enableGetFileButton(false);
			frame.setStatus("Searching for " + fileName + "...");

			Object[] args = new Object[1];
			args[0] = fileName;
			Message message = new Message(
					"http://localhost:8083/Filer/queryFile", this.transceiver);
			// Message message = new
			// Message("http://localhost:8083/Filer/queryFile",
			// this.transceiver);

			// We're going to broadcast our message to our child peers and
			// their child peers. That should be enough.
			message.setHops("3");

			// Reset the acknowledgements and the thread kill variables
			acks = 0;
			killSearch = false;
			foundList.clear();
			totalPacketsRecvd = 0;

			try {
				// this.postMessage(message, args);
				this.broadcastMessage(message, args);
			} catch (AdaptinetException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),
						"Exception", JOptionPane.ERROR_MESSAGE);
			}

			// Create a thread to monitor feedback from the search
			new Thread(new Runnable() {
				public void run() {
					if (monitorSearch()) {
						requestFilePackets();
						monitorPackets();
						assembleFile();
					} else
						frame.setStatus("File not found.");

					frame.enableGetFileButton(true);
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean monitorSearch() {
		String status = frame.getStatus();

		int searchTimeout = Integer.parseInt(properties.getProperty(
				"SearchTimeout", MAX_SEARCH_SECONDS));

		// Loop while waiting for responses
		for (int i = 0; i < (searchTimeout * 10); i++) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (killSearch)
				break;

			status += ".";
			frame.setStatus(status);
		}

		return (foundList.size() == 0 ? false : true);
	}

	private void requestFilePackets() {
		try {
			frame.setStatus("Requesting the file from the peer list...");

			// Send the responses to the frame for display
			frame.displayResponses(foundList);

			long fileSize = 0;
			long startPos = 0;
			long packetSize = 0;
			Iterator<FindResult> it = foundList.iterator();
			while (it.hasNext()) {
				FindResult findResult = it.next();
				if (fileSize == 0) {
					fileSize = findResult.getLength().longValue();

					long defaultPacketSize = Long.parseLong(properties
							.getProperty("DefaultPacketSize",
									DEFAULT_PACKET_SIZE));

					// Set the chunk transfer size
					if (fileSize <= defaultPacketSize)
						packetSize = fileSize;
					else
						packetSize = defaultPacketSize;
				}

				Object[] args = new Object[3];
				args[0] = findResult.getFileName();
				args[1] = new Long(startPos);
				args[2] = new Long(
						(startPos + packetSize >= fileSize) ? fileSize
								- startPos : packetSize);
				startPos += packetSize;
				Message message = new Message("http://"
						+ findResult.getAddress().getHost() + ":"
						+ findResult.getAddress().getPort()
						+ "/Filer/getFilePacket", this.transceiver);

				try {
					this.postMessage(message, args);
				} catch (AdaptinetException e) {
					e.printStackTrace();
				}

				// If we're at the end of the list, reset to the beginning
				if (!it.hasNext())
					it = foundList.iterator();

				// Are we done?
				if (startPos >= fileSize)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void monitorPackets() {
		try {
			frame.setStatus("Waiting for packets...");

			Iterator<FindResult> it = foundList.iterator();
			FindResult findResult = it.next();

			// This should have a timeout eventually
			while (totalPacketsRecvd < findResult.getLength().longValue()) {
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void assembleFile() {
		try {
			// Assemble the path name
			Iterator<FindResult> it = foundList.iterator();
			if (!it.hasNext())
				return;
			FindResult findResult = it.next();
			String fileName = findResult.getFileName();

			frame.setStatus("Writing file: " + fileName + "...");

			String path = properties.getProperty("DestDir");
			if (path == null)
				return;
			if (!path.endsWith(File.separator))
				path += File.separatorChar;

			// Create the destination file
			File outFile = new File(path + fileName);
			outFile.createNewFile();

			long defaultPacketSize = Long.parseLong(properties.getProperty(
					"DefaultPacketSize", DEFAULT_PACKET_SIZE));

			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(outFile));
			byte[] bytes = new byte[(int) defaultPacketSize];
			for (long i = 0; i < findResult.getLength().longValue(); i += defaultPacketSize) {
				// Read the "in" file chunk
				File inFile = new File(path + fileName + "."
						+ new Long(i).toString());
				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(inFile));
				int read = in.read(bytes, 0, (int) inFile.length());
				in.close();

				// Delete it
				inFile.delete();

				// Write it to the output stream
				out.write(bytes, 0, read);
				out.flush();
			}
			out.close();

			frame.setStatus("File transfer complete: " + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fileFound(String fileName, Long length, Long lastModified) {
		try {
			// Ignore zero file length responses
			if (length.longValue() == 0)
				return;

			// Add the information to an internal list
			foundList.add(new FindResult(fileName, length.longValue(),
					lastModified.longValue(), msg.getReplyTo()));

			int maxAcks = Integer.parseInt(properties.getProperty(
					"MaxAcknowledgments", MAX_ACKNOWLEDGEMENTS));
			acks++;
			if (acks >= maxAcks)
				killSearch = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void queryFile(String fileName) {
		try {
			Address sender = msg.getReplyTo();

			// Get the share directory from the properties
			String path = properties.getProperty("ShareDir");
			if (path == null)
				return;

			// Look for the file
			File findFile = new File(path + File.separator + fileName);
			if (!findFile.exists())
				return;

			// Send a reply (yes I have the file) to the originator
			Object[] args = new Object[3];
			args[0] = fileName;
			args[1] = new Long(findFile.length());
			args[2] = new Long(findFile.lastModified());
			Message message = new Message("http://" + sender.getHost() + ":"
					+ sender.getPort() + "/Filer/fileFound", this.transceiver);

			this.postMessage(message, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getFilePacket(String fileName, Long startPos, Long size) {
		try {
			// Get the share directory from the properties
			String path = properties.getProperty("ShareDir");
			if (path == null)
				return;

			// Create the in stream
			FileInputStream in = new FileInputStream(new File(path
					+ File.separator + fileName));

			byte[] bytes = new byte[size.intValue()];
			try {
				in.skip(startPos.longValue());
				in.read(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
			in.close();

			BASE64Encoder encoder = new BASE64Encoder();

			Object[] args = new Object[4];
			args[0] = fileName;
			args[1] = startPos;
			args[2] = size;
			args[3] = encoder.encode(bytes);

			Address sender = msg.getReplyTo();
			Message message = new Message("http://" + sender.getHost() + ":"
					+ sender.getPort() + "/Filer/packetSent", this.transceiver);

			this.postMessage(message, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void packetSent(String fileName, Long startPos, Long size,
			String data) {
		try {
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] bytes = decoder.decodeBuffer(data);
			frame.setStatus("Receiving packet for file:" + fileName
					+ ". Size: " + size.toString() + " bytes. Start position:"
					+ startPos.toString() + "...");

			// Get the destination directory from the properties
			String path = properties.getProperty("DestDir");
			if (path == null)
				return;
			if (!path.endsWith(File.separator))
				path += File.separatorChar;

			String name = path + fileName + "." + startPos.toString();
			File file = new File(name);
			file.createNewFile();
			// file.deleteOnExit();

			FileOutputStream os = new FileOutputStream(file);
			os.write(bytes);
			os.flush();
			os.close();

			totalPacketsRecvd += size.longValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected File findFileFromClassPath(String name) {
		String classpath = System.getProperty("java.class.path");
		File file = null;
		try {
			StringTokenizer classToken = new StringTokenizer(classpath,
					File.pathSeparator);
			while (classToken.hasMoreTokens()) {
				String filename = classToken.nextToken();
				String filenameLower = filename.toLowerCase();
				if (filenameLower.endsWith(".zip")
						|| filenameLower.endsWith(".jar")) {
					try {
						JarFile jarFile = new JarFile(filename);
						JarEntry j = jarFile.getJarEntry(name);
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

					File f = new File(filename + name);
					if (f.exists()) {
						file = f;
						break;
					}
				}
			}
		} catch (Exception e) {
		}

		return file;
	}
}