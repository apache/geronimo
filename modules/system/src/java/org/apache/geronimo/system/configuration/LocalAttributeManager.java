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

import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.beans.PropertyEditor;

/**
 * Stores managed attributes in an XML file on the local filesystem.
 *
 * @version $Rev: 106387 $ $Date: 2004-11-23 22:16:54 -0800 (Tue, 23 Nov 2004) $
 */
public class LocalAttributeManager implements ManageableAttributeStore, GBeanLifecycle {
    private final static Log log = LogFactory.getLog(LocalAttributeManager.class);
    private final static String BACKUP_EXTENSION=".bak";
    private final static String TEMP_EXTENSION=".working";
    private final static int SAVE_BUFFER_MS=5000;

    private final ServerInfo serverInfo;
    private final String configFile;
    private String objectName;

    private File attributeFile;
    private File backupFile;
    private File tempFile;
    private final Map configurations = new LinkedHashMap();
    private UpdateThread updater;

    public LocalAttributeManager(ServerInfo serverInfo, String configFile, String objectName) {
        this.serverInfo = serverInfo;
        this.configFile = configFile;
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    public Object getValue(String configurationName, ObjectName gbean, GAttributeInfo attribute) {
        log.info("Checking value for "+configurationName+"/"+gbean+"/"+attribute.getName());
        Map config = (Map) configurations.get(configurationName);
        if(config == null) {
            return null; // nothing specified for this configuration
        }
        Map atts = (Map) config.get(gbean);
        if(atts == null) {
            atts = (Map)config.get(gbean.getKeyProperty("name"));
        }
        if(atts == null) {
            return null; // nothing specified for this GBean
        }
        String value = (String) atts.get(attribute.getName());
        if(value == null) {
            return null; // nothing specified for this attribute
        }
        try {
            PropertyEditor editor = PropertyEditors.findEditor(attribute.getType(), getClass().getClassLoader());
            if (editor == null) {
                log.error("Unable to parse attribute of type "+attribute.getType()+"; no editor found");
                return null;
            }
            editor.setAsText(value);
            return editor.getValue();
        } catch (ClassNotFoundException e) {
            //todo: use the Configuration's ClassLoader to load the attribute, if this ever becomes an issue
            log.error("Unable to load attribute type "+attribute.getType());
            return null;
        }
    }

    public synchronized void setValue(String configurationName, ObjectName gbean, GAttributeInfo attribute, Object value) {
        Map config = (Map) configurations.get(configurationName);
        if(config == null) {
            config = new HashMap();
            configurations.put(configurationName, config);
        }
        Map atts = (Map) config.get(gbean);
        if(atts == null) {
            atts = (Map) config.get(gbean.getKeyProperty("name"));
            if(atts == null) {
                atts = new HashMap();
                config.put(gbean, atts);
            }
        }
        try {
            String string = null;
            if(value != null) {
                PropertyEditor editor = PropertyEditors.findEditor(attribute.getType(), getClass().getClassLoader());
                if (editor == null) {
                    log.error("Unable to format attribute of type "+attribute.getType()+"; no editor found");
                    return;
                }
                editor.setValue(value);
                string = editor.getAsText();
            }
            if(string == null) {
                atts.remove(attribute.getName());
            } else {
                atts.put(attribute.getName(), string);
            }
            updater.attributeChanged();
        } catch (ClassNotFoundException e) {
            //todo: use the Configuration's ClassLoader to load the attribute, if this ever becomes an issue
            log.error("Unable to store attribute type "+attribute.getType());
        }
    }

    public void load() throws IOException {
        ensureParentDirectory();
        if(!attributeFile.exists()) {
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
            for(int c=0; c<configs.getLength(); c++) {
                Element config = (Element)configs.item(c);
                String configName = config.getAttribute("name");
                Map configMap = new LinkedHashMap();
                results.put(configName, configMap);
                NodeList gbeans = config.getElementsByTagName("gbean");
                for(int g=0; g<gbeans.getLength(); g++) {
                    Element gbean = (Element)gbeans.item(g);
                    String gbeanName = gbean.getAttribute("name");
                    Map gbeanMap = new LinkedHashMap();
                    if(gbeanName.indexOf(':') > -1) {
                        ObjectName name = ObjectName.getInstance(gbeanName);
                        configMap.put(name, gbeanMap);
                    } else {
                        configMap.put(gbeanName, gbeanMap);
                    }
                    NodeList attributes = gbean.getElementsByTagName("attribute");
                    for(int a=0; a<attributes.getLength(); a++) {
                        Element attribute = (Element)attributes.item(a);
                        String attName = attribute.getAttribute("name");
                        String value = "";
                        NodeList text = attribute.getChildNodes();
                        for(int t=0; t<text.getLength(); t++) {
                            Node n = text.item(t);
                            if(n.getNodeType() == Node.TEXT_NODE) {
                                value += n.getNodeValue();
                            }
                        }
                        gbeanMap.put(attName, value.trim());
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
        ensureParentDirectory();
        if(!tempFile.exists() && !tempFile.createNewFile()) {
            throw new IOException("Unable to create manageable attribute working file for save "+tempFile.getAbsolutePath());
        }
        if(!tempFile.canWrite()) {
            throw new IOException("Unable to write to manageable attribute working file for save "+tempFile.getAbsolutePath());
        }
        PrintWriter out = new PrintWriter(new FileWriter(tempFile), true);
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println();
        out.println("<attributes xmlns=\"http://geronimo.apache.org/xml/ns/attributes\">");
        for (Iterator it = configurations.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            out.println("  <configuration name=\""+entry.getKey()+"\">");
            Map map = (Map) entry.getValue();
            for (Iterator gb = map.entrySet().iterator(); gb.hasNext();) {
                Map.Entry gbean = (Map.Entry) gb.next();
                String gbeanName = gbean.getKey() instanceof String ? (String)gbean.getKey() : ((ObjectName)gbean.getKey()).getCanonicalName();
                out.println("    <gbean name=\""+gbeanName+"\">");
                Map atts = (Map)gbean.getValue();
                for (Iterator att = atts.entrySet().iterator(); att.hasNext();) {
                    Map.Entry attribute = (Map.Entry) att.next();
                    out.print("      <attribute name=\""+attribute.getKey()+"\">");
                    out.print((String)attribute.getValue());
                    out.println("</attribute>");
                }
                out.println("    </gbean>");
            }
            out.println("  </configuration>");
        }
        out.println("</attributes>");
        out.close();
        if(backupFile.exists()) {
            if(!backupFile.delete()) {
                throw new IOException("Unable to delete old backup file in order to back up current manageable attribute working file for save");
            }
        }
        if(attributeFile.exists()) {
            if(!attributeFile.renameTo(backupFile)) {
                throw new IOException("Unable to rename "+attributeFile.getAbsolutePath()+" to "+backupFile.getAbsolutePath()+" in order to back up manageable attribute save file");
            }
        }
        if(!tempFile.renameTo(attributeFile)) {
            throw new IOException("EXTREMELY CRITICAL!  Unable to move manageable attributes working file to proper file name!  Configuration will revert to defaults unless this is manually corrected!  (could not rename "+tempFile.getAbsolutePath()+" to "+attributeFile.getAbsolutePath()+")");
        }
    }

    public void doStart() throws Exception {
        load();
        updater = new UpdateThread();
        updater.start();
        log.info("Started LocalAttributeManager with data on "+configurations.size()+" configurations");
    }

    public void doStop() throws Exception {
        if(updater != null) {
            updater.shutdown();
            if(updater.isPending()) {
                save();
            }
            updater = null;
        }
        log.info("Stopped LocalAttributeManager with data on "+configurations.size()+" configurations");
        configurations.clear();
    }

    public void doFail() {
        if(updater != null) {
            updater.shutdown();
            updater = null;
        }
        configurations.clear();
    }

    private void ensureParentDirectory() throws IOException {
        if(attributeFile == null) {
            attributeFile = serverInfo.resolve(configFile);
            tempFile = new File(attributeFile.getAbsolutePath()+TEMP_EXTENSION);
            backupFile = new File(attributeFile.getAbsolutePath()+BACKUP_EXTENSION);
        }
        File parent = attributeFile.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Unable to create directory for list:" + parent);
            }
        }
        if(!parent.canRead() || !parent.canWrite()) {
            throw new IOException("Unable to write manageable attribute files to directory "+parent.getAbsolutePath());
        }
    }


    /**
     * A thread that's notified on every attribute update.  5 seconds after
     * being notified, it will save the changes to a file.
     */
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
            while(!isDone()) {
                // Wait until at least one change has been made
                synchronized(LocalAttributeManager.this) {
                    if(!pending) {
                        try {
                            LocalAttributeManager.this.wait();
                            pending = true;
                        } catch (InterruptedException e) {}
                    }
                    if(done) {
                        return;
                    }
                }

                // Pause for effect (and to catch a flurry of changes)
                // Don't synchronize this as it holds monitors while sleeping
                try {
                    sleep(SAVE_BUFFER_MS);
                } catch (InterruptedException e) {}

                // Save
                synchronized (LocalAttributeManager.this) {
                    if(!isDone()) {
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(LocalAttributeManager.class, "AttributeStore");//does not use jsr-77 naming
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addAttribute("configFile", String.class, true);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addInterface(ManageableAttributeStore.class);

        infoFactory.setConstructor(new String[]{"ServerInfo","configFile","objectName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
