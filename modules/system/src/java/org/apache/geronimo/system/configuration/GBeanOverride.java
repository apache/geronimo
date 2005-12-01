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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.geronimo.gbean.GBeanData;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
class GBeanOverride {
    private final Object name;
    private boolean load;
    private final Map attributes = new LinkedHashMap();
    private final Map references = new LinkedHashMap();
    private final String gbeanInfoSource;

    public GBeanOverride(String name, boolean load) {
        this.name = name;
        this.load = load;
        gbeanInfoSource = null;
    }

    public GBeanOverride(ObjectName name, boolean load) {
        this.name = name;
        this.load = load;
        gbeanInfoSource = null;
    }

    public GBeanOverride(GBeanData gbeanData) {
        gbeanInfoSource = gbeanData.getGBeanInfo().getSourceClass();
        if (gbeanInfoSource == null) {
            throw new IllegalArgumentException("GBeanInfo must have a source class set");
        }
        name = gbeanData.getName();
        attributes.putAll(gbeanData.getAttributes());
        references.putAll(gbeanData.getReferences());
    }

    public GBeanOverride(Element gbean) throws MalformedObjectNameException {
        String nameString = gbean.getAttribute("name");
        if (nameString.indexOf(':') > -1) {
            name = ObjectName.getInstance(nameString);
        } else {
            name = nameString;
        }

        gbeanInfoSource = gbean.getAttribute("gbeanInfo");

        String loadString = gbean.getAttribute("load");
        load = !"false".equals(loadString);

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
            setAttribute(attName, value.trim());
        }
    }

    public Object getName() {
        return name;
    }

    public String getGbeanInfoSource() {
        return gbeanInfoSource;
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

    public Map getReferences() {
        return references;
    }

    public Set getReferencePatterns(String name) {
        return (Set) references.get(name);
    }

    public void setReferencePattern(String name, ObjectName pattern) {
        setReferencePatterns(name, Collections.singleton(pattern));
    }

    public void setReferencePatterns(String name, Set patterns) {
        references.put(name, patterns);
    }

    public void writeXml(PrintWriter out) {
        String gbeanName;
        if (name instanceof String) {
            gbeanName = (String) name;
        } else {
            gbeanName = ((ObjectName) name).getCanonicalName();
        }

        out.print("    <gbean name=\"" + gbeanName + "\"");
        if (gbeanInfoSource != null) {
            out.print(" gbeanInfoSource=\"" + gbeanInfoSource + "\"");
        }
        
        if (!load) {
            out.print(" load=\"false\"");
        }
        out.println(">");

        // Attribute values
        for (Iterator att = attributes.entrySet().iterator(); att.hasNext();) {
            Map.Entry attribute = (Map.Entry) att.next();
            out.print("      <attribute name=\"" + attribute.getKey() + "\">");
            out.print((String) attribute.getValue());
            out.println("</attribute>");
        }

        out.println("    </gbean>");
    }
}
