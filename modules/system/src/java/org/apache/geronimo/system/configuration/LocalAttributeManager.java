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
import java.util.LinkedHashMap;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    private final Map configurations = new LinkedHashMap();

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

    public Collection setAttributes(URI configurationName, Collection datas) throws InvalidConfigException {
        String configName = configurationName.toString();
        ConfigInfo configInfo = (ConfigInfo) configurations.get(configName);
        if (configInfo != null) {
            if (configInfo.isLoad()) {
                for (Iterator iterator = datas.iterator(); iterator.hasNext();) {
                    GBeanData data = (GBeanData) iterator.next();
                    boolean load = setAttributes(data, configInfo, configName);
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
     * @param configInfo
     * @param configName
     * @return true if the gbean should be loaded, false otherwise.
     * @throws InvalidConfigException
     */
    private boolean setAttributes(GBeanData data, ConfigInfo configInfo, String configName) throws InvalidConfigException {
        ObjectName gbeanName = data.getName();
        GBeanInfo gBeanInfo = data.getGBeanInfo();
        GBeanAttrsInfo attributeMap = configInfo.getGBean(gbeanName);
        if (attributeMap == null) {
            attributeMap = configInfo.getGBean(gbeanName.getKeyProperty("name"));
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


    private Object getValue(GAttributeInfo attribute, String value, String configurationName, ObjectName gbeanName) {
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

    public synchronized void setValue(String configurationName, ObjectName gbean, GAttributeInfo attribute, Object value) {
        if (readOnly) {
            return;
        }
        ConfigInfo config = (ConfigInfo) configurations.get(configurationName);
        if (config == null) {
            config = new ConfigInfo(true);
            configurations.put(configurationName, config);
        }
        GBeanAttrsInfo atts = config.getGBean(gbean);
        if (atts == null) {
            atts = config.getGBean(gbean.getKeyProperty("name"));
            if (atts == null) {
                atts = new GBeanAttrsInfo(true);
                config.addGBean(gbean, atts);
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
            Map attrMap = atts.getAttributes();
            attrMap.put(attribute.getName(), string);
            attributeChanged();
        } catch (ClassNotFoundException e) {
            //todo: use the Configuration's ClassLoader to load the attribute, if this ever becomes an issue
            log.error("Unable to store attribute type " + attribute.getType());
        }
    }

    public void load() throws IOException {
        ensureParentDirectory();
        if (!attributeFile.exists()) {
            return;
        }
        configurations.clear();
        Map results = new LinkedHashMap();
        InputSource in = new InputSource(new FileInputStream(attributeFile));
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        try {
            Document doc = dfactory.newDocumentBuilder().parse(in);
            Element root = doc.getDocumentElement();
            NodeList configs = root.getElementsByTagName("configuration");
            for (int c = 0; c < configs.getLength(); c++) {
                Element config = (Element) configs.item(c);
                String configName = config.getAttribute("name");
                String loadConfigString = config.getAttribute("load");
                boolean loadConfig = !"false".equals(loadConfigString);
                ConfigInfo configInfo = new ConfigInfo(loadConfig);
                results.put(configName, configInfo);
                NodeList gbeans = config.getElementsByTagName("gbean");
                for (int g = 0; g < gbeans.getLength(); g++) {
                    Element gbean = (Element) gbeans.item(g);
                    String gbeanName = gbean.getAttribute("name");
                    String loadGBeanString = gbean.getAttribute("load");
                    boolean loadGBean = !"false".equals(loadGBeanString);
                    GBeanAttrsInfo gbeanAttrs = new GBeanAttrsInfo(loadGBean);
                    if (gbeanName.indexOf(':') > -1) {
                        ObjectName name = ObjectName.getInstance(gbeanName);
                        configInfo.addGBean(name, gbeanAttrs);
                    } else {
                        configInfo.addGBean(gbeanName, gbeanAttrs);
                    }
                    NodeList attributes = gbean.getElementsByTagName("attribute");
                    for (int a = 0; a < attributes.getLength(); a++) {
                        Element attribute = (Element) attributes.item(a);
                        String attName = attribute.getAttribute("name");
                        String value = "";
                        NodeList text = attribute.getChildNodes();
                        for (int t = 0; t < text.getLength(); t++) {
                            Node n = text.item(t);
                            if (n.getNodeType() == Node.TEXT_NODE) {
                                value += n.getNodeValue();
                            }
                        }
                        gbeanAttrs.setAttribute(attName, value.trim());
                    }
                }
            }
            configurations.putAll(results);
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
        PrintWriter out = new PrintWriter(new FileWriter(tempFile), true);
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println();
        out.println("<attributes xmlns=\"http://geronimo.apache.org/xml/ns/attributes\">");
        for (Iterator it = configurations.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            ConfigInfo configInfo = (ConfigInfo) entry.getValue();
            boolean configLoaded = configInfo.isLoad();
            out.println("  <configuration name=\"" + entry.getKey() + (configLoaded ? "\">" : "\" load=\"false\">"));
            for (Iterator gb = configInfo.getGBeans().entrySet().iterator(); gb.hasNext();) {
                Map.Entry gbean = (Map.Entry) gb.next();
                String gbeanName = gbean.getKey() instanceof String ? (String) gbean.getKey() : ((ObjectName) gbean.getKey()).getCanonicalName();
                GBeanAttrsInfo atts = (GBeanAttrsInfo) gbean.getValue();
                boolean gbeanLoaded = atts.isLoad();
                out.println("    <gbean name=\"" + gbeanName + (gbeanLoaded ? "\">" : "\" load=\"false\">"));
                for (Iterator att = atts.getAttributes().entrySet().iterator(); att.hasNext();) {
                    Map.Entry attribute = (Map.Entry) att.next();
                    out.print("      <attribute name=\"" + attribute.getKey() + "\">");
                    out.print((String) attribute.getValue());
                    out.println("</attribute>");
                }
                out.println("    </gbean>");
            }
            out.println("  </configuration>");
        }
        out.println("</attributes>");
        out.close();
        if (backupFile.exists()) {
            if (!backupFile.delete()) {
                throw new IOException("Unable to delete old backup file in order to back up current manageable attribute working file for save");
            }
        }
        if (attributeFile.exists()) {
            if (!attributeFile.renameTo(backupFile)) {
                throw new IOException("Unable to rename " + attributeFile.getAbsolutePath() + " to " + backupFile.getAbsolutePath() + " in order to back up manageable attribute save file");
            }
        }
        if (!tempFile.renameTo(attributeFile)) {
            throw new IOException("EXTREMELY CRITICAL!  Unable to move manageable attributes working file to proper file name!  Configuration will revert to defaults unless this is manually corrected!  (could not rename " + tempFile.getAbsolutePath() + " to " + attributeFile.getAbsolutePath() + ")");
        }
    }

    //PersistentConfigurationList
    public boolean isKernelFullyStarted() {
         return kernelFullyStarted;
     }

     public void setKernelFullyStarted(boolean kernelFullyStarted) {
         this.kernelFullyStarted = kernelFullyStarted;
     }

    public List restore() throws IOException {
        List configs = new ArrayList();
        for (Iterator iterator = configurations.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            ConfigInfo configInfo = (ConfigInfo) entry.getValue();
            if (configInfo.isLoad()) {
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

    public synchronized void addConfiguration(String configName) {
        ConfigInfo configInfo = (ConfigInfo) configurations.get(configName);
        if (configInfo == null) {
            configInfo = new ConfigInfo(true);
            configurations.put(configName, configInfo);
        } else {
            configInfo.setLoad(true);
        }
    }

    public synchronized void removeConfiguration(String configName) {
        ConfigInfo configInfo = (ConfigInfo) configurations.get(configName);
        if (configInfo == null) {
            log.error("Trying to stop unknown configuration: " + configName);
        } else {
            Map gbeans = configInfo.getGBeans();
            if (gbeans.isEmpty()) {
                configurations.remove(configName);
            } else {
                configInfo.setLoad(false);
            }
        }
    }

    //GBeanLifeCycle
    public void doStart() throws Exception {
        load();
        if (!readOnly) {
            timer = new Timer();
        }
        log.info("Started LocalAttributeManager with data on " + configurations.size() + " configurations");
    }

    public void doStop() throws Exception {
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
        log.info("Stopped LocalAttributeManager with data on " + configurations.size() + " configurations");
        configurations.clear();
    }

    public void doFail() {
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                if (currentTask != null) {
                    currentTask.cancel();
                }
            }
        }
        configurations.clear();
    }

    private void ensureParentDirectory() throws IOException {
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


    /**
     * A thread that's notified on every attribute update.  5 seconds after
     * being notified, it will save the changes to a file.
     */
/*
    // todo: This code is not pleasing -- it uses lots of synchronization and still doesn't guarantee a timely shutdown.
    private class UpdateThread extends Thread {
        private boolean done = false;
        private boolean pending = false;

        public UpdateThread() {
            super("Manageable-Attribute-Saver");
            setDaemon(true);
        }

        public synchronized void setDone() {
            this.done = true;
        }

        public synchronized boolean isDone() {
            return done;
        }

        public void run() {
            while (!isDone()) {
                // Wait until at least one change has been made
                synchronized (LocalAttributeManager.this) {
                    if (!pending) {
                        try {
                            LocalAttributeManager.this.wait();
                            pending = true;
                        } catch (InterruptedException e) {
                        }
                    }
                    if (done) {
                        return;
                    }
                }

                // Pause for effect (and to catch a flurry of changes)
                // Don't synchronize this as it holds monitors while sleeping
                try {
                    sleep(SAVE_BUFFER_MS);
                } catch (InterruptedException e) {
                }

                // Save
                synchronized (LocalAttributeManager.this) {
                    if (!isDone()) {
                        try {
                            save();
                        } catch (IOException e) {
                            log.error("Error saving attributes", e);
                        }
                        pending = false;
                    }
                }
            }
        }

        public boolean isPending() {
            synchronized (LocalAttributeManager.this) {
                return pending;
            }
        }

        public void attributeChanged() {
            synchronized (LocalAttributeManager.this) {
                pending = true;
                LocalAttributeManager.this.notify();
            }
        }

        public void shutdown() {
            setDone();
            synchronized (LocalAttributeManager.this) {
                LocalAttributeManager.this.notify();
            }
        }
    }
*/

    private static class ConfigInfo {
        private boolean load;
        private final Map gbeans = new HashMap();

        public ConfigInfo(boolean load) {
            this.load = load;
        }

        public boolean isLoad() {
            return load;
        }

        public void setLoad(boolean load) {
            this.load = load;
        }

        public GBeanAttrsInfo getGBean(String gbeanName) {
            return (GBeanAttrsInfo) gbeans.get(gbeanName);
        }

        public void addGBean(String gbeanName, GBeanAttrsInfo gbean) {
            gbeans.put(gbeanName, gbean);
        }

        public Map getGBeans() {
            return gbeans;
        }

        public GBeanAttrsInfo getGBean(ObjectName gbeanName) {
            return (GBeanAttrsInfo) gbeans.get(gbeanName);
        }

        public void addGBean(ObjectName gbeanName, GBeanAttrsInfo gbean) {
            gbeans.put(gbeanName, gbean);
        }
    }

    private static class GBeanAttrsInfo {
        private boolean load;
        private final Map attributes = new HashMap();

        public GBeanAttrsInfo(boolean load) {
            this.load = load;
        }

        public boolean isLoad() {
            return load;
        }

        public void setLoad(boolean load) {
            this.load = load;
        }

        public Map getAttributes() {
            return attributes;
        }

        public String getAttribute(String attributeName) {
            return (String) attributes.get(attributeName);
        }

        public void setAttribute(String attributeName, String attributeValue) {
            attributes.put(attributeName, attributeValue);
        }
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
