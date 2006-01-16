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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.apache.geronimo.gbean.GReferenceInfo;
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
        serverOverride = new ServerOverride();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public synchronized Collection setAttributes(URI configurationName, Collection gbeanDatas, ClassLoader classLoader) throws InvalidConfigException {
        // clone the datas since we will be modifying this collection
        gbeanDatas = new ArrayList(gbeanDatas);

        String configName = configurationName.toString();
        ConfigurationOverride configuration = serverOverride.getConfiguration(configName);
        if (configuration == null) {
            return gbeanDatas;
        }
// IMO (DJ) the following lines mix the config.list and config.xml functionalities for no reason.
// They also cause GERONIMO-1455       
//        if (!configuration.isLoad()) {
//            return Collections.EMPTY_LIST;
//        }

        // index the incoming datas
        Map datasByName = new HashMap();
        for (Iterator iterator = gbeanDatas.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            datasByName.put(gbeanData.getName(), gbeanData);
        }

        // add the new GBeans
        for (Iterator iterator = configuration.getGBeans().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object name = entry.getKey();
            GBeanOverride gbean = (GBeanOverride) entry.getValue();
            if (!datasByName.containsKey(name) && gbean.getGBeanInfo() != null && gbean.isLoad()) {
                if (!(name instanceof ObjectName)) {
                    throw new InvalidConfigException("New GBeans must be specified with a full objectName:" +
                            " configuration=" + configName +
                            " gbeanName=" + name);
                }
                ObjectName objectName = (ObjectName) name;
                GBeanInfo gbeanInfo = GBeanInfo.getGBeanInfo(gbean.getGBeanInfo(), classLoader);
                GBeanData gBeanData = new GBeanData(objectName, gbeanInfo);
                gbeanDatas.add(gBeanData);
            }
        }

        // set the attributes
        for (Iterator iterator = gbeanDatas.iterator(); iterator.hasNext();) {
            GBeanData data = (GBeanData) iterator.next();
            boolean load = setAttributes(data, configuration, configName, classLoader);
            if (!load) {
                iterator.remove();
            }
        }
        return gbeanDatas;
    }

    /**
     * Set the attributes from the attribute store on a single gbean, and return whether or not to load the gbean.
     *
     * @param data
     * @param configuration
     * @param configName
     * @param classLoader
     * @return true if the gbean should be loaded, false otherwise.
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException
     */
    private synchronized boolean setAttributes(GBeanData data, ConfigurationOverride configuration, String configName, ClassLoader classLoader) throws InvalidConfigException {
        ObjectName gbeanName = data.getName();
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean(gbeanName.getKeyProperty("name"));
        }

        if (gbean == null) {
            //no attr info, load by default
            return true;
        }

        if (!gbean.isLoad()) {
            return false;
        }

        GBeanInfo gbeanInfo = data.getGBeanInfo();

        // set attributes
        for (Iterator iterator = gbean.getAttributes().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String attributeName = (String) entry.getKey();
            GAttributeInfo attributeInfo = gbeanInfo.getAttribute(attributeName);
            if (attributeInfo == null) {
                throw new InvalidConfigException("No attribute: " + attributeName + " for gbean: " + data.getName());
            }
            String valueString = (String) entry.getValue();
            Object value = getValue(attributeInfo, valueString, configName, gbeanName, classLoader);
            data.setAttribute(attributeName, value);
        }

        // set references
        for (Iterator iterator = gbean.getReferences().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();

            String referenceName = (String) entry.getKey();
            GReferenceInfo referenceInfo = gbeanInfo.getReference(referenceName);
            if (referenceInfo == null) {
                throw new InvalidConfigException("No reference: " + referenceName + " for gbean: " + data.getName());
            }

            Set referencePatterns = (Set) entry.getValue();

            data.setReferencePatterns(referenceName, referencePatterns);
        }
        return true;
    }


    private synchronized Object getValue(GAttributeInfo attribute, String value, String configurationName, ObjectName gbeanName, ClassLoader classLoader) {
        if (value == null) {
            return null;
        }

        try {
            PropertyEditor editor = PropertyEditors.findEditor(attribute.getType(), classLoader);
            if (editor == null) {
                log.debug("Unable to parse attribute of type " + attribute.getType() + "; no editor found");
                return null;
            }
            editor.setAsText(value);
            log.debug("Setting value for " + configurationName + "/" + gbeanName + "/" + attribute.getName() + " to value " + value);
            return editor.getValue();
        } catch (ClassNotFoundException e) {
            log.error("Unable to load attribute type " + attribute.getType());
            return null;
        }
    }

    public synchronized void setValue(String configurationName, ObjectName gbeanName, GAttributeInfo attribute, Object value) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean(gbeanName.getKeyProperty("name"));
            if (gbean == null) {
                gbean = new GBeanOverride(gbeanName, true);
                configuration.addGBean(gbeanName, gbean);
            }
        }

        try {
            gbean.setAttribute(attribute.getName(), value, attribute.getType());
            attributeChanged();
        } catch (InvalidAttributeException e) {
            // attribute can not be represented as a string
            log.error(e.getMessage());
            return;
        }
    }

    public synchronized void setReferencePattern(String configurationName, ObjectName gbeanName, GReferenceInfo reference, ObjectName pattern) {
        setReferencePatterns(configurationName, gbeanName, reference, Collections.singleton(pattern));
    }

    public synchronized void setReferencePatterns(String configurationName, ObjectName gbeanName, GReferenceInfo reference, Set patterns) {
        if (readOnly) {
            return;
        }

        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean(gbeanName.getKeyProperty("name"));
            if (gbean == null) {
                gbean = new GBeanOverride(gbeanName, true);
                configuration.addGBean(gbeanName, gbean);
            }
        }
        gbean.setReferencePatterns(reference.getName(), patterns);
        attributeChanged();
    }

    public synchronized void setShouldLoad(String configurationName, ObjectName gbeanName, boolean load) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);

        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            // attempt to lookup by short name
            gbean = configuration.getGBean(gbeanName.getKeyProperty("name"));
        }

        if (gbean == null) {
            gbean = new GBeanOverride(gbeanName, load);
            configuration.addGBean(gbeanName, gbean);
        } else {
            gbean.setLoad(load);
        }
        attributeChanged();
    }

    public void addGBean(String configurationName, GBeanData gbeanData) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName);
        if (configuration == null) {
            log.debug("Can not add GBean; Configuration not found " + configurationName);
            return;
        }
        try {
            GBeanOverride gbean = new GBeanOverride(gbeanData);
            configuration.addGBean(gbean);
            attributeChanged();
        } catch (InvalidAttributeException e) {
            // attribute can not be represented as a string
            log.error(e.getMessage());
            return;
        }
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
            ConfigurationOverride configuration = (ConfigurationOverride) entry.getValue();
            if (configuration.isLoad()) {
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
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);
        configuration.setLoad(true);
        attributeChanged();
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
            attributeChanged();
        }
    }

    //GBeanLifeCycle
    public synchronized void doStart() throws Exception {
        load();
        if (!readOnly) {
            timer = new Timer();
        }
        log.debug("Started LocalAttributeManager with data on " + serverOverride.getConfigurations().size() + " configurations");
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
        log.debug("Stopped LocalAttributeManager with data on " + serverOverride.getConfigurations().size() + " configurations");
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
        if (timer != null) {
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
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(LocalAttributeManager.class, "AttributeStore");
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
