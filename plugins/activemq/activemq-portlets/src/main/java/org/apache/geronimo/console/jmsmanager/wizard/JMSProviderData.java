/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.jmsmanager.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.portlet.PortletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Loads data on JMS providers known to the console.  Reads from a properties
 * file on the class path.
 *
 * @version $Rev$ $Date$
 */
public class JMSProviderData implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(JMSProviderData.class);
    private String name;
    private final String raURI;
    private final String dependency;
    private String defaultTransaction;
    private ConfigPropertyData[] instanceConfigProperties;
    private ConnectionDefinition[] connectionDefinitions;
    private AdminObjectDefinition[] adminObjectDefinitions;

    public JMSProviderData(String name, String raURI, String dependency) {
        this.name = name;
        this.raURI = raURI;
        this.dependency = dependency;
    }

    public String getName() {
        return name;
    }

    public String getRaURI() {
        return raURI;
    }

    public String getDependency() {
        return dependency;
    }

    public String getDefaultTransaction() {
        return defaultTransaction;
    }

    public ConfigPropertyData[] getInstanceConfigProperties() {
        return instanceConfigProperties;
    }

    public ConnectionDefinition[] getConnectionDefinitions() {
        return connectionDefinitions;
    }

    public AdminObjectDefinition[] getAdminObjectDefinitions() {
        return adminObjectDefinitions;
    }

    public static class ConfigPropertyData implements Serializable {
        private final String name;
        private final String type;
        private final String defaultValue;
        private final String description;

        public ConfigPropertyData(String name, String type, String defaultValue, String description) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class ConnectionDefinition implements Serializable {
        private final String connectionFactoryInterface;
        private final ConfigPropertyData[] configProperties;

        public ConnectionDefinition(String connectionFactoryInterface, ConfigPropertyData[] configProperties) {
            this.connectionFactoryInterface = connectionFactoryInterface;
            this.configProperties = configProperties;
        }

        public String getConnectionFactoryInterface() {
            return connectionFactoryInterface;
        }

        public ConfigPropertyData[] getConfigProperties() {
            return configProperties;
        }
    }

    public static class AdminObjectDefinition implements Serializable {
        private final String adminObjectInterface;
        private final String adminObjectClass;
        private final ConfigPropertyData[] configProperties;

        public AdminObjectDefinition(String adminObjectInterface, String adminObjectClass, ConfigPropertyData[] configProperties) {
            this.adminObjectInterface = adminObjectInterface;
            this.adminObjectClass = adminObjectClass;
            this.configProperties = configProperties;
        }

        public String getAdminObjectInterface() {
            return adminObjectInterface;
        }

        public String getAdminObjectClass() {
            return adminObjectClass;
        }

        public ConfigPropertyData[] getConfigProperties() {
            return configProperties;
        }
    }


    // *************** Static methods to access the data ****************

    private static List all = null;
    public static JMSProviderData[] getAllProviders() {
        if(all == null) {
            loadProviders();
        }
        return (JMSProviderData[]) all.toArray(new JMSProviderData[all.size()]);
    }

    public static JMSProviderData getProviderByName(String name) {
        if(all == null) {
            loadProviders();
        }
        for (int i = 0; i < all.size(); i++) {
            JMSProviderData data = (JMSProviderData) all.get(i);
            if(data.getName().equals(name)) {
                return data;
            }
        }
        return null;
    }

    public static JMSProviderData getProviderData(String rar, PortletRequest request) throws IOException {
        if(all == null) {
            loadProviders();
        }
        for (int i = 0; i < all.size(); i++) {
            JMSProviderData data = (JMSProviderData) all.get(i);
            if(data.getRaURI().equals(rar)) {
                if(data.instanceConfigProperties == null) {
                    loadRARData(data, request);
                }
                return data;
            }
        }
        JMSProviderData data = new JMSProviderData(null, rar, null);
        loadRARData(data, request);
        all.add(data);
        return data;
    }

    private static void loadRARData(JMSProviderData data, PortletRequest request) throws IOException {
        File url = PortletManager.getRepositoryEntry(request, data.getRaURI());
        if(url == null) {
            throw new IOException("Unable to locate entry "+data.getRaURI()+" in repository");
        }
        ZipInputStream in = new ZipInputStream(new FileInputStream(url));
        ZipEntry entry;
        Document doc = null;
        try {
            while((entry = in.getNextEntry()) != null) {
                if(entry.getName().equals("META-INF/ra.xml")) {
                    DocumentBuilderFactory factory = XmlUtil.newDocumentBuilderFactory();
                    factory.setValidating(false);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    doc = builder.parse(in);
                    in.close();
                    in = null;
                    break;
                } else in.closeEntry();
            }
        } catch (ParserConfigurationException e) {
            log.error("Unable to read META-INF/ra.xml in RAR file '"+data.getRaURI()+"'", e);
        } catch (SAXException e) {
            log.error("Unable to read META-INF/ra.xml in RAR file '"+data.getRaURI()+"'", e);
        } finally {
            if (in != null)
                try {
                    in.close();        
                } catch (IOException ignore) {
                }
        }
        if(doc == null) {
            throw new IOException("Unable to locate META-INF/ra.xml in RAR file '"+data.getRaURI()+"'");
        }
        Element root = doc.getDocumentElement();
        if(data.getName() == null) {
            NodeList displays = getChildren(root, "display-name");
            if(displays != null && displays.getLength() > 0) {
                data.name = getText(displays.item(0));
            }
        }
        Element ra = (Element) getChildren(root, "resourceadapter").item(0);
        data.instanceConfigProperties = loadConfigs(ra);
        Element outbound = (Element) getChildren(ra, "outbound-resourceadapter").item(0);
        data.defaultTransaction = getTransactionSetting(getChildText(outbound, "transaction-support"));
        data.connectionDefinitions = loadConnections(outbound);
        data.adminObjectDefinitions = loadAdmins(ra);
    }

    private static String getTransactionSetting(String text) {
        if(text == null) {
            return null;
        }
        if(text.equals("XATransaction")) return "xa";
        if(text.equals("LocalTransaction")) return "local";
        if(text.equals("NoTransaction")) return "none";
        return null;
    }

    private static ConfigPropertyData[] loadConfigs(Element parent) {
        NodeList configs = getChildren(parent, "config-property");
        if(configs == null || configs.getLength() == 0) {
            return new ConfigPropertyData[0];
        }
        ConfigPropertyData[] results = new ConfigPropertyData[configs.getLength()];
        for (int i = 0; i < results.length; i++) {
            Element root = (Element) configs.item(i);
            results[i] = new ConfigPropertyData(getChildText(root, "config-property-name"),
                    getChildText(root, "config-property-type"), getChildText(root, "config-property-value"),
                    getChildText(root, "description"));
        }
        return results;
    }

    private static NodeList getChildren(Element parent, String child) {
        final List list = new ArrayList();
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(child)) {
                list.add(node);
            }
        }
        return new NodeList() {
            public Node item(int index) {
                return (Node) list.get(index);
            }

            public int getLength() {
                return list.size();
            }
        };
    }

    private static ConnectionDefinition[] loadConnections(Element outbound) {
        NodeList defs = getChildren(outbound, "connection-definition");
        if(defs == null || defs.getLength() == 0) {
            return new ConnectionDefinition[0];
        }
        ConnectionDefinition[] results = new ConnectionDefinition[defs.getLength()];
        for (int i = 0; i < results.length; i++) {
            Element def = (Element) defs.item(i);
            results[i] = new ConnectionDefinition(getChildText(def, "connectionfactory-interface"), loadConfigs(def));
        }
        return results;
    }

    private static AdminObjectDefinition[] loadAdmins(Element ra) {
        NodeList defs = getChildren(ra, "adminobject");
        if(defs == null || defs.getLength() == 0) {
            return new AdminObjectDefinition[0];
        }
        AdminObjectDefinition[] results = new AdminObjectDefinition[defs.getLength()];
        for (int i = 0; i < results.length; i++) {
            Element def = (Element) defs.item(i);
            results[i] = new AdminObjectDefinition(getChildText(def, "adminobject-interface"),
                    getChildText(def, "adminobject-class"), loadConfigs(def));
        }
        return results;
    }

    private static String getChildText(Element root, String name) {
        NodeList list = getChildren(root, name);
        if(list == null || list.getLength() == 0) {
            return null;
        }
        return getText(list.item(0));
    }

    private static String getText(Node node) {
        StringBuilder buf = null;
        NodeList list = node.getChildNodes();
        if(list != null) {
            for(int i=0; i<list.getLength(); i++) {
                Node current = list.item(i);
                if(current.getNodeType() == Node.TEXT_NODE) {
                    if(buf == null) {
                        buf = new StringBuilder();
                    }
                    buf.append(current.getNodeValue());
                }
            }
        }
        return buf == null ? null : buf.toString();
    }


    private static void loadProviders() {
        InputStream in = JMSProviderData.class.getResourceAsStream("/jms-resource-providers.properties");
        if(in == null) {
            log.error("Unable to locate JMS provider properties file");
            return;
        }
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            log.error("Unable to read JMS provider properties file", e);
        } finally {
            // load could fail, ensure stream is closed.
            try {
                in.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
        Set set = new HashSet();
        // Find the names of the provider entries
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            int start = key.indexOf('.');
            int end = key.indexOf('.', start+1);
            if(start < 0 || end < 0) {
                continue;
            }
            set.add(key.substring(start+1, end));
        }
        List list = new ArrayList(set.size());
        for (Iterator it = set.iterator(); it.hasNext();) {
            String key = (String) it.next();
            String name = props.getProperty("provider."+key+".name");
            String rar = props.getProperty("provider."+key+".rar");
            String dep = props.getProperty("provider."+key+".dependency");
            list.add(new JMSProviderData(name, rar, dep));
        }
        all = list;
    }
}
