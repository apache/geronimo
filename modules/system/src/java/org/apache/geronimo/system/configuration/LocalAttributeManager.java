/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.configuration;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Stores managed attributes in an XML file on the local filesystem.
 *
 * @version $Rev$ $Date$
 */
public class LocalAttributeManager implements ManageableAttributeStore, PersistentConfigurationList, GBeanLifecycle {
    private final static Log log = LogFactory.getLog(LocalAttributeManager.class);

    private final static String CONFIG_FILE_PROPERTY = "org.apache.geronimo.config.file";

    private final static String BACKUP_EXTENSION = ".bak";
    private final static String TEMP_EXTENSION = ".working";
    private final static int SAVE_BUFFER_MS = 5000;

    private final ServerInfo serverInfo;
    private final String configFile;
    private final boolean readOnly;

    private File attributeFile;
    private File backupFile;
    private File tempFile;
    private ServerOverride serverOverride;

    private Timer timer;
    private TimerTask currentTask;

    private boolean kernelFullyStarted;

    public LocalAttributeManager(String configFile, boolean readOnly, ServerInfo serverInfo) {
        this.configFile = System.getProperty(CONFIG_FILE_PROPERTY, configFile);
        this.readOnly = readOnly;
        this.serverInfo = serverInfo;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public synchronized Collection setAttributes(URI configurationName, Collection datas) throws InvalidConfigException {
        String configName = configurationName.toString();
        ConfigurationOverride configurationOverride = serverOverride.getConfiguration(configName);
        if (configurationOverride != null) {
            if (configurationOverride.isLoad()) {
                for (Iterator iterator = datas.iterator(); iterator.hasNext();) {
                    GBeanData data = (GBeanData) iterator.next();
                    boolean load = setAttributes(data, configurationOverride, configName);
                    if (!load) {
                        iterator.remove();
                    }
                }
            } else {
                return Collections.EMPTY_LIST;
            }
        }
        return datas;
    }

    /**
     * Set the attributes from the attribute store on a single gbean, and return whether or not to load the gbean.
     *
     * @param data
     * @param configurationOverride
     * @param configName
     * @return true if the gbean should be loaded, false otherwise.
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException
     */
    private synchronized boolean setAttributes(GBeanData data, ConfigurationOverride configurationOverride, String configName) throws InvalidConfigException {
        ObjectName gbeanName = data.getName();
        GBeanInfo gBeanInfo = data.getGBeanInfo();
        GBeanOverride attributeMap = configurationOverride.getGBean(gbeanName);
        if (attributeMap == null) {
            attributeMap = configurationOverride.getGBean(gbeanName.getKeyProperty("name"));
        }
        if (attributeMap != null) {
            if (attributeMap.isLoad()) {
                for (Iterator iterator = attributeMap.getAttributes().entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String attributeName = (String) entry.getKey();
                    GAttributeInfo attributeInfo = gBeanInfo.getAttribute(attributeName);
                    if (attributeInfo == null) {
                        throw new InvalidConfigException("No attribute: " + attributeName + " for gbean: " + data.getName());
                    }
                    String valueString = (String) entry.getValue();
                    Object value = getValue(attributeInfo, valueString, configName, gbeanName);
                    data.setAttribute(attributeName, value);
                }
                return true;
            } else {
                return false;
            }
        }
        //no attr info, load by default
        return true;
    }


    private synchronized Object getValue(GAttributeInfo attribute, String value, String configurationName, ObjectName gbeanName) {
        if (value == null) {
            return null;
        }
        try {
            PropertyEditor editor = PropertyEditors.findEditor(attribute.getType(), getClass().getClassLoader());
            if (editor == null) {
                log.debug("Unable to parse attribute of type " + attribute.getType() + "; no editor found");
                return null;
            }
            editor.setAsText(value);
            log.debug("Setting value for " + configurationName + "/" + gbeanName + "/" + attribute.getName() + " to value " + value);
            return editor.getValue();
        } catch (ClassNotFoundException e) {
            //todo: use the Configuration's ClassLoader to load the attribute, if this ever becomes an issue
            log.error("Unable to load attribute type " + attribute.getType());
            return null;
        }
    }

    public synchronized void setValue(String configurationName, ObjectName gbeanName, GAttributeInfo attribute, Object value) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride config = serverOverride.getConfiguration(configurationName, true);
        GBeanOverride gbeanOverride = config.getGBean(gbeanName);
        if (gbeanOverride == null) {
            gbeanOverride = config.getGBean(gbeanName.getKeyProperty("name"));
            if (gbeanOverride == null) {
                gbeanOverride = new GBeanOverride(gbeanName, true);
                config.addGBean(gbeanName, gbeanOverride);
            }
        }
        try {
            String string = null;
            if (value != null) {
                PropertyEditor editor = PropertyEditors.findEditor(attribute.getType(), getClass().getClassLoader());
                if (editor == null) {
                    log.error("Unable to format attribute of type " + attribute.getType() + "; no editor found");
                    return;
                }
                editor.setValue(value);
                string = editor.getAsText();
            }
            Map attrMap = gbeanOverride.getAttributes();
            attrMap.put(attribute.getName(), string);
            attributeChanged();
        } catch (ClassNotFoundException e) {
            //todo: use the Configuration's ClassLoader to load the attribute, if this ever becomes an issue
            log.error("Unable to store attribute type " + attribute.getType());
        }
    }

    public synchronized void setShouldLoad(String configurationName, ObjectName gbeanName, boolean load) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride config = serverOverride.getConfiguration(configurationName, true);

        GBeanOverride atts = config.getGBean(gbeanName);
        if (atts == null) {
            // attempt to lookup by short name
            atts = config.getGBean(gbeanName.getKeyProperty("name"));
        }

        if (atts == null) {
            atts = new GBeanOverride(gbeanName, load);
            config.addGBean(gbeanName, atts);
        } else {
            atts.setLoad(load);
        }
        attributeChanged();
    }

    public synchronized void load() throws IOException {
        ensureParentDirectory();
        if (!attributeFile.exists()) {
            return;
        }
        InputSource in = new InputSource(new FileInputStream(attributeFile));
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        try {
            Document doc = dfactory.newDocumentBuilder().parse(in);
            Element root = doc.getDocumentElement();
            serverOverride = new ServerOverride(root);
        } catch (SAXException e) {
            log.error("Unable to read saved manageable attributes", e);
        } catch (ParserConfigurationException e) {
            log.error("Unable to read saved manageable attributes", e);
        } catch (MalformedObjectNameException e) {
            log.error("Unable to read saved manageable attributes", e);
        }
    }

    public synchronized void save() throws IOException {
        if (readOnly) {
            return;
        }
        ensureParentDirectory();
        if (!tempFile.exists() && !tempFile.createNewFile()) {
            throw new IOException("Unable to create manageable attribute working file for save " + tempFile.getAbsolutePath());
        }
        if (!tempFile.canWrite()) {
            throw new IOException("Unable to write to manageable attribute working file for save " + tempFile.getAbsolutePath());
        }

        // write the new configuration to the temp file
        PrintWriter out = new PrintWriter(new FileWriter(tempFile), true);
        serverOverride.writeXml(out);
        out.close();

        // delete the current backup file
        if (backupFile.exists()) {
            if (!backupFile.delete()) {
                throw new IOException("Unable to delete old backup file in order to back up current manageable attribute working file for save");
            }
        }

        // rename the existing configuration file to the backup file
        if (attributeFile.exists()) {
            if (!attributeFile.renameTo(backupFile)) {
                throw new IOException("Unable to rename " + attributeFile.getAbsolutePath() + " to " + backupFile.getAbsolutePath() + " in order to back up manageable attribute save file");
            }
        }

        // rename the temp file the the configuration file
        if (!tempFile.renameTo(attributeFile)) {
            throw new IOException("EXTREMELY CRITICAL!  Unable to move manageable attributes working file to proper file name!  Configuration will revert to defaults unless this is manually corrected!  (could not rename " + tempFile.getAbsolutePath() + " to " + attributeFile.getAbsolutePath() + ")");
        }
    }

    //PersistentConfigurationList
    public synchronized boolean isKernelFullyStarted() {
        return kernelFullyStarted;
    }

    public synchronized void setKernelFullyStarted(boolean kernelFullyStarted) {
        this.kernelFullyStarted = kernelFullyStarted;
    }

    public synchronized List restore() throws IOException {
        List configs = new ArrayList();
        for (Iterator iterator = serverOverride.getConfigurations().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            ConfigurationOverride configurationOverride = (ConfigurationOverride) entry.getValue();
            if (configurationOverride.isLoad()) {
                String configName = (String) entry.getKey();
                try {
                    URI configID = new URI(configName);
                    configs.add(configID);
                } catch (URISyntaxException e) {
                    throw new IOException("Could not construct URI configID for " + configName);
                }
            }
        }
        return configs;
    }

    public synchronized void addConfiguration(String configurationName) {
        ConfigurationOverride config = serverOverride.getConfiguration(configurationName, true);
        config.setLoad(true);
    }

    public synchronized void removeConfiguration(String configName) {
        ConfigurationOverride configuration = serverOverride.getConfiguration(configName);
        if (configuration == null) {
            log.error("Trying to stop unknown configuration: " + configName);
        } else {
            if (configuration.getGBeans().isEmpty()) {
                serverOverride.removeConfiguration(configName);
            } else {
                configuration.setLoad(false);
            }
        }
    }

    //GBeanLifeCycle
    public synchronized void doStart() throws Exception {
        load();
        if (!readOnly) {
            timer = new Timer();
        }
        log.info("Started LocalAttributeManager with data on " + serverOverride.getConfigurations().size() + " configurations");
    }

    public synchronized void doStop() throws Exception {
        boolean doSave = false;
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                if (currentTask != null) {
                    currentTask.cancel();
                    doSave = true;
                }
            }
        }
        if (doSave) {
            save();
        }
        log.info("Stopped LocalAttributeManager with data on " + serverOverride.getConfigurations().size() + " configurations");
        serverOverride = new ServerOverride();
    }

    public synchronized void doFail() {
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                if (currentTask != null) {
                    currentTask.cancel();
                }
            }
        }
        serverOverride = new ServerOverride();
    }

    private synchronized void ensureParentDirectory() throws IOException {
        if (attributeFile == null) {
            attributeFile = serverInfo.resolve(configFile);
            tempFile = new File(attributeFile.getAbsolutePath() + TEMP_EXTENSION);
            backupFile = new File(attributeFile.getAbsolutePath() + BACKUP_EXTENSION);
        }
        File parent = attributeFile.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Unable to create directory for list:" + parent);
            }
        }
        if (!parent.canRead() || !parent.canWrite()) {
            throw new IOException("Unable to write manageable attribute files to directory " + parent.getAbsolutePath());
        }
    }


    private synchronized void attributeChanged() {
        if (currentTask != null) {
            currentTask.cancel();
        }
        currentTask = new TimerTask() {

            public void run() {
                try {
                    LocalAttributeManager.this.save();
                } catch (IOException e) {
                    log.error("Error saving attributes", e);
                }
            }
        };
        timer.schedule(currentTask, SAVE_BUFFER_MS);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(LocalAttributeManager.class, "AttributeStore");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addAttribute("configFile", String.class, true);
        infoFactory.addAttribute("readOnly", boolean.class, true);
        infoFactory.addInterface(ManageableAttributeStore.class);
        infoFactory.addInterface(PersistentConfigurationList.class);

        infoFactory.setConstructor(new String[]{"configFile", "readOnly", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
