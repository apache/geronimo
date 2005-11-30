/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.management.MalformedObjectNameException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
class ServerOverride {
    private final Map configurations = new LinkedHashMap();

    public ServerOverride() {
    }

    public ServerOverride(Element element) throws MalformedObjectNameException {
        NodeList configs = element.getElementsByTagName("configuration");
        for (int i = 0; i < configs.getLength(); i++) {
            Element configurationElement = (Element) configs.item(i);
            ConfigurationOverride configuration = new ConfigurationOverride(configurationElement);
            addConfiguration(configuration);
        }
    }

    public ConfigurationOverride getConfiguration(String configurationName) {
        return getConfiguration(configurationName, false);
    }

    public ConfigurationOverride getConfiguration(String configurationName, boolean create) {
        ConfigurationOverride configuration = (ConfigurationOverride) configurations.get(configurationName);
        if (create && configuration == null) {
            configuration = new ConfigurationOverride(configurationName, true);
            configurations.put(configurationName, configuration);
        }
        return configuration;
    }

    public void addConfiguration(ConfigurationOverride configuration) {
        configurations.put(configuration.getName(), configuration);
    }

    public void removeConfiguration(String configurationName) {
        configurations.remove(configurationName);
    }

    public Map getConfigurations() {
        return configurations;
    }

    public void writeXml(PrintWriter out) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println();
        out.println("<attributes xmlns=\"http://geronimo.apache.org/xml/ns/attributes\">");
        for (Iterator it = configurations.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            ConfigurationOverride configurationOverride = (ConfigurationOverride) entry.getValue();
            configurationOverride.writeXml(out);
        }
        out.println("</attributes>");
        out.close();
    }
}
