/**
 *	Copyright (C), 2012 Adaptinet.org (Todd Fearn, Anthony Graffeo)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *  
 */

package org.adaptinet.registry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.adaptinet.adaptinetex.AdaptinetException;
import org.adaptinet.parser.InputSource;
import org.adaptinet.parser.XMLReader;
import org.adaptinet.pluginagent.PluginAgent;
import org.adaptinet.pluginagent.PluginFactory;
import org.adaptinet.transceiver.ITransceiver;
import org.adaptinet.transceiver.TransceiverConfig;


public final class PluginFile {

	private boolean bIsDirty = false;
	private boolean bOpen = false;
	private String pluginFileName;
	private File file;
	private Thread saveThread;
	private StringBuffer buffer = null;
	private Map<String, PluginEntry> plugins = null;
	
	public PluginFile() {
	}

	public PluginFile(String name) {
		try {
			openFile(name);
		} catch (Exception e) {
		}
	}

	public void openFile(String name) throws Exception {
		FileInputStream fis = null;
		pluginFileName = name;
		try {
			file = new File(pluginFileName);
			if (!file.exists()) {
				new File(pluginFileName.substring(0, pluginFileName
						.lastIndexOf(File.separatorChar))).mkdirs();
				file.createNewFile();
			}
			bOpen = true;

			fis = new FileInputStream(file);
			byte bytes[] = new byte[fis.available()];
			fis.read(bytes);

			if (bytes.length > 0) {
				XMLReader parser = new XMLReader();
				PluginParser pluginsParser = new PluginParser();
				parser.setContentHandler(pluginsParser);
				parser.parse(new InputSource(new ByteArrayInputStream(bytes)));
				plugins = Collections.synchronizedMap(pluginsParser.getEntries());
			} else {
				plugins = Collections.synchronizedMap(new HashMap<String, PluginEntry>());
			}

			startUpdate();
		} catch (Exception x) {
			x.printStackTrace();
			throw x;
		} finally {
			if (fis != null)
				fis.close();
		}
	}

    public void preload() throws Exception {

        ITransceiver transceiver = ITransceiver.getTransceiver();
        PluginEntry entry = null;
        PluginAgent plugin = null;
        try {
            plugin=(PluginAgent)transceiver.getAvailablePlugin(PluginFactory.ADAPTINET);
            entry = new PluginEntry();
            entry.setName(PluginFactory.ADAPTINET);
            plugin.preProcess(PluginFactory.ADAPTINETCLASS);
            plugin.startPlugin(entry);

            plugin=(PluginAgent)transceiver.getAvailablePlugin(PluginFactory.MAINTENANCE);
            entry = new PluginEntry();
            entry.setName(PluginFactory.MAINTENANCE);
            plugin.preProcess(PluginFactory.MAINTENANCECLASS);
            plugin.startPlugin(entry);
            
            plugin=(PluginAgent)transceiver.getAvailablePlugin(PluginFactory.CACHE);
            entry = new PluginEntry();
            entry.setName(PluginFactory.CACHE);
            plugin.preProcess(PluginFactory.CACHECLASS);
            plugin.startPlugin(entry);
            
            Boolean b = (Boolean)transceiver.getSetting(TransceiverConfig.SHOWCONSOLE);
            if(b.booleanValue()==true) {
                plugin=(PluginAgent)transceiver.getAvailablePlugin(PluginFactory.CONSOLE);
                entry = new PluginEntry();
                entry.setName(PluginFactory.CONSOLE);
                entry.setType(PluginFactory.CONSOLECLASS);
                plugin.preProcess(PluginFactory.SERVICECLASS);
                plugin.startPlugin(entry);
            }
        }
        catch(Exception e) {
            throw e;
        }

        new Thread(new Runnable() {

            public void run() {
                ITransceiver transceiver = ITransceiver.getTransceiver();
                PluginEntry entry = null;
                PluginAgent plugin = null;
                try {
                    Iterator<PluginEntry> it = plugins.values().iterator();
                    while(it.hasNext()) {
                        try {
                            entry = it.next();
                            if(entry.isPreload()) {
                                plugin=(PluginAgent)transceiver.getAvailablePlugin(entry.getName());
                                plugin.preProcess(PluginFactory.SERVICECLASS);
                                plugin.startPlugin(entry);
                            }
                        }
                        catch(Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }

  

public void startPlugins() throws Exception {

		new Thread(new Runnable() {

			public void run() {
				ITransceiver transceiver = ITransceiver.getTransceiver();
				PluginEntry entry = null;
				PluginAgent plugin = null;
				try {
					Iterator<PluginEntry> it = plugins.values().iterator();
					while (it.hasNext()) {
						try {
							entry = it.next();
							if (entry.isPreload()) {
								plugin = (PluginAgent) transceiver.getAvailablePlugin(entry.getName());
								//plugin.preProcess("org.adaptinet.pluginagent.ServicePlugin");
								plugin.startPlugin(entry);
							}
						} catch (Exception e) {
							System.out.println(e.getMessage());
						}
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}).start();
	}

	public void startUpdate() {
		try {
			saveThread = new Thread() {
				public void run() {
					try {
						while (bOpen) {
							try {
								Thread.sleep(60000);
							} catch (InterruptedException ex) {
							}
							if (bIsDirty == true) {
								save();
							}
						}
					} catch (IOException ioe) {
						AdaptinetException plugx = new AdaptinetException(
								AdaptinetException.SEVERITY_ERROR,
								AdaptinetException.GEN_MESSAGE);
						plugx.logMessage(ioe);
					} catch (Exception e) {
					}
				}
			};
			saveThread.start();
		} catch (Exception e) {
		}
	}

	public void save() throws IOException {
		buffer = new StringBuffer(1024);
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buffer.append("<Plugins>");

		Iterator<PluginEntry> it = plugins.values().iterator();
		while (it.hasNext())
			it.next().write(buffer);
		bIsDirty = false;

		buffer.append("</Plugins>");

		file.renameTo(new File(pluginFileName + "~"));
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(buffer.toString().getBytes());
		fos.close();
	}

	public void closeFile() {
		try {
			bOpen = false;
			saveThread.interrupt();
			saveThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PluginEntry findEntry(String name) {
		return plugins.get(name);
	}

	public void insert(PluginEntry entry) {
		insert(entry.getName(), entry);
	}

	public void insert(String tag, PluginEntry entry) {
		try {
			plugins.put(tag, entry);
			bIsDirty = true;
		} catch (NullPointerException npe) {
		} catch (Exception e) {
		}
	}

	public void remove(PluginEntry entry) {
		try {
			plugins.remove(entry);
			bIsDirty = true;
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String getName() {
		return pluginFileName;
	}

	public Iterator<PluginEntry> getValues() {
		return plugins.values().iterator();
	}

	public void setDirty(boolean bIsDirty) {
		this.bIsDirty = bIsDirty;
	}

}
