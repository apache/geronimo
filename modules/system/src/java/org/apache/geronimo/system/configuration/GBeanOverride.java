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

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.util.EncryptionManager;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.beans.PropertyEditor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
class GBeanOverride {
    private final Object name;
    private boolean load;
    private final Map attributes = new LinkedHashMap();
    private final Map references = new LinkedHashMap();
    private final String gbeanInfo;

    public GBeanOverride(String name, boolean load) {
        this.name = name;
        this.load = load;
        gbeanInfo = null;
    }

    public GBeanOverride(AbstractName name, boolean load) {
        this.name = name;
        this.load = load;
        gbeanInfo = null;
    }

    public GBeanOverride(GBeanData gbeanData) throws InvalidAttributeException {
        GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
        this.gbeanInfo = gbeanInfo.getSourceClass();
        if (this.gbeanInfo == null) {
            throw new IllegalArgumentException("GBeanInfo must have a source class set");
        }
        name = gbeanData.getName();
        load = true;

        // set attributes
        for (Iterator iterator = gbeanData.getAttributes().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String attributeName = (String) entry.getKey();
            GAttributeInfo attributeInfo = gbeanInfo.getAttribute(attributeName);
            if (attributeInfo == null) {
                throw new InvalidAttributeException("No attribute: " + attributeName + " for gbean: " + gbeanData.getName());
            }
            Object attributeValue = entry.getValue();
            setAttribute(attributeName, attributeValue, attributeInfo.getType());
        }

        // references can be coppied in blind
        references.putAll(gbeanData.getReferences());
    }

    public GBeanOverride(Element gbean) throws MalformedObjectNameException, InvalidGBeanException {
        String nameString = gbean.getAttribute("name");
        if (nameString.indexOf(':') > -1) {
            name = ObjectName.getInstance(nameString);
        } else {
            name = nameString;
        }

        String gbeanInfoString = gbean.getAttribute("gbeanInfo");
        if (gbeanInfoString.length() > 0) {
            gbeanInfo = gbeanInfoString;
        } else {
            gbeanInfo = null;
        }
        if (gbeanInfo != null && !(name instanceof ObjectName)) {
            throw new MalformedObjectNameException("A gbean element using the gbeanInfo attribute must be specified using a full ObjectName: name=" + nameString);
        }

        String loadString = gbean.getAttribute("load");
        load = !"false".equals(loadString);

        // attributes
        NodeList attributes = gbean.getElementsByTagName("attribute");
        for (int a = 0; a < attributes.getLength(); a++) {
            Element attribute = (Element) attributes.item(a);

            String attributeName = attribute.getAttribute("name");
            String attributeValue = (String)EncryptionManager.decrypt(getContentsAsText(attribute));
            setAttribute(attributeName, attributeValue);
        }

        // references
        NodeList references = gbean.getElementsByTagName("reference");
        for (int r = 0; r < references.getLength(); r++) {
            Element reference = (Element) references.item(r);

            String referenceName = reference.getAttribute("name");

            Set objectNamePatterns = new LinkedHashSet();
            NodeList patterns = reference.getElementsByTagName("pattern");
            for (int p = 0; p < references.getLength(); p++) {
                Element pattern = (Element) patterns.item(p);
                String groupId = getChildAsText(pattern, "groupId");
                String artifactId = getChildAsText(pattern, "artifactId");
                String version = getChildAsText(pattern, "version");
                String type = getChildAsText(pattern, "type");
                String module = getChildAsText(pattern, "module");
                String name = getChildAsText(pattern, "name");

                Artifact referenceArtifact = null;
                if (artifactId != null) {
                    referenceArtifact = new Artifact(groupId, artifactId, version, type);
                }
                Map nameMap = new HashMap();
                if (module != null) {
                    nameMap.put("module", module);
                }
                if (name != null) {
                    nameMap.put("name", name);
                }
                AbstractNameQuery abstractNameQuery = new AbstractNameQuery(referenceArtifact, nameMap, Collections.EMPTY_SET);
                objectNamePatterns.add(abstractNameQuery);
            }

            setReferencePatterns(referenceName, new ReferencePatterns(objectNamePatterns));
        }
    }

    private static String getChildAsText(Element element, String name) throws InvalidGBeanException {
        NodeList children = element.getElementsByTagName(name);
        if (children == null || children.getLength() == 0) {
            return null;
        }
        if (children.getLength() > 1) {
            throw new InvalidGBeanException("invalid name, too many parts named: " + name);
        }
        return getContentsAsText((Element) children.item(0));
    }

    private static String getContentsAsText(Element element) {
        String value = "";
        NodeList text = element.getChildNodes();
        for (int t = 0; t < text.getLength(); t++) {
            Node n = text.item(t);
            if (n.getNodeType() == Node.TEXT_NODE) {
                value += n.getNodeValue();
            }
        }
        return value.trim();
    }

    public Object getName() {
        return name;
    }

    public String getGBeanInfo() {
        return gbeanInfo;
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

    public void setAttribute(String attributeName, Object attributeValue, String attributeType) throws InvalidAttributeException {
        String stringValue = getAsText(attributeValue, attributeType);
        attributes.put(attributeName, stringValue);
    }
    public void setAttribute(String attributeName, String attributeValue) {
        attributes.put(attributeName, attributeValue);
    }

    public Map getReferences() {
        return references;
    }

    public ReferencePatterns getReferencePatterns(String name) {
        return (ReferencePatterns) references.get(name);
    }

    public void setReferencePatterns(String name, ReferencePatterns patterns) {
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
        if (gbeanInfo != null) {
            out.print(" gbeanInfo=\"" + gbeanInfo + "\"");
        }

        if (!load) {
            out.print(" load=\"false\"");
        }
        out.println(">");

        // attributes
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            if(name.toLowerCase().indexOf("password") > -1) {
                value = EncryptionManager.encrypt(value);
            }
            out.println("      <attribute name=\"" + name + "\">" +  value + "</attribute>");
        }

        // references
        for (Iterator iterator = references.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            ReferencePatterns patterns = (ReferencePatterns) entry.getValue();

            out.println("      <reference name=\"" + name + "\">");
            for (Iterator patternIterator = patterns.getPatterns().iterator(); patternIterator.hasNext();) {
                AbstractNameQuery pattern = (AbstractNameQuery) patternIterator.next();
                out.println("          <pattern>");
                List artifacts = pattern.getArtifacts();
                if (artifacts != null && !artifacts.isEmpty()) {
                    Artifact artifact = (Artifact) artifacts.get(0);
                    if (artifact.getGroupId() != null) {
                        out.println("              <groupId>" + artifact.getGroupId() + "</groupId>");
                    }
                    out.println("              <artifactId>" + artifact.getArtifactId() + "</artifactId>");
                    if (artifact.getVersion() != null) {
                        out.println("              <version>" + artifact.getVersion() + "</version>");
                    }
                    if (artifact.getType() != null) {
                        out.println("              <type>" + artifact.getType() + "</ype>");
                    }
                    Map nameMap = pattern.getName();
                    if (nameMap.get("module") != null) {
                        out.println("              <module>" + nameMap.get("module") + "</module>");
                    }
                    if (nameMap.get("name") != null) {
                        out.println("              <name>" + nameMap.get("name") + "</name>");
                    }
                }
                out.print(pattern.toString());
                out.println("</pattern>");
            }
            out.println("      </reference>");
        }

        out.println("    </gbean>");
    }

    public static String getAsText(Object value, String type) throws InvalidAttributeException {
        try {
            String attributeStringValue = null;
            if (value != null) {
                PropertyEditor editor = PropertyEditors.findEditor(type, GBeanOverride.class.getClassLoader());
                if (editor == null) {
                    throw new InvalidAttributeException("Unable to format attribute of type " + type + "; no editor found");
                }
                editor.setValue(value);
                attributeStringValue = editor.getAsText();
            }
            return attributeStringValue;
        } catch (ClassNotFoundException e) {
            //todo: use the Configuration's ClassLoader to load the attribute, if this ever becomes an issue
            throw new InvalidAttributeException("Unable to store attribute type " + type);
        }
    }
}
