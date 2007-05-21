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
package org.apache.geronimo.system.configuration;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.util.EncryptionManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @version $Rev$ $Date$
 */
public class GBeanOverride implements Serializable {
    private final Object name;
    private boolean load;
    private final Map attributes = new LinkedHashMap();
    private final Map references = new LinkedHashMap();
    private final ArrayList clearAttributes = new ArrayList();
    private final ArrayList nullAttributes = new ArrayList();
    private final ArrayList clearReferences = new ArrayList();
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

    public GBeanOverride(GBeanOverride original, String oldArtifact, String newArtifact) {
        Object name = original.name;
        if(name instanceof String) {
            name = replace((String)name, oldArtifact, newArtifact);
        } else if(name instanceof AbstractName) {
            String value = name.toString();
            value = replace(value, oldArtifact, newArtifact);
            name = new AbstractName(URI.create(value));
        }
        this.name = name;
        this.load = original.load;
        this.attributes.putAll(original.attributes);
        this.references.putAll(original.references);
        this.clearAttributes.addAll(original.clearAttributes);
        this.nullAttributes.addAll(original.nullAttributes);
        this.clearReferences.addAll(original.clearReferences);
        this.gbeanInfo = original.gbeanInfo;
    }

    private static String replace(String original, String oldArtifact, String newArtifact) {
        int pos = original.indexOf(oldArtifact);
        if(pos == -1) {
            return original;
        }
        int last = -1;
        StringBuffer buf = new StringBuffer();
        while(pos > -1) {
            buf.append(original.substring(last+1, pos));
            buf.append(newArtifact);
            last = pos+oldArtifact.length()-1;
            pos = original.indexOf(oldArtifact, last);
        }
        buf.append(original.substring(last+1));
        return buf.toString();
    }

    public GBeanOverride(GBeanData gbeanData) throws InvalidAttributeException {
        GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
        this.gbeanInfo = gbeanInfo.getSourceClass();
        if (this.gbeanInfo == null) {
            throw new IllegalArgumentException("GBeanInfo must have a source class set");
        }
        name = gbeanData.getAbstractName();
        load = true;

        // set attributes
        for (Iterator iterator = gbeanData.getAttributes().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String attributeName = (String) entry.getKey();
            GAttributeInfo attributeInfo = gbeanInfo.getAttribute(attributeName);
            if (attributeInfo == null) {
                throw new InvalidAttributeException("No attribute: " + attributeName + " for gbean: " + gbeanData.getAbstractName());
            }
            Object attributeValue = entry.getValue();
            setAttribute(attributeName, attributeValue, attributeInfo.getType());
        }

        // references can be coppied in blind
        references.putAll(gbeanData.getReferences());
    }

    public GBeanOverride(Element gbean) throws InvalidGBeanException {
        String nameString = gbean.getAttribute("name");
        if (nameString.indexOf('?') > -1) {
            name = new AbstractName(URI.create(nameString));
        } else {
            name = nameString;
        }

        String gbeanInfoString = gbean.getAttribute("gbeanInfo");
        if (gbeanInfoString.length() > 0) {
            gbeanInfo = gbeanInfoString;
        } else {
            gbeanInfo = null;
        }
        if (gbeanInfo != null && !(name instanceof AbstractName)) {
            throw new InvalidGBeanException("A gbean element using the gbeanInfo attribute must be specified using a full AbstractName: name=" + nameString);
        }

        String loadString = gbean.getAttribute("load");
        load = !"false".equals(loadString);

        // attributes
        NodeList attributes = gbean.getElementsByTagName("attribute");
        for (int a = 0; a < attributes.getLength(); a++) {
            Element attribute = (Element) attributes.item(a);

            String attributeName = attribute.getAttribute("name");

            // Check to see if there is a value attribute
            if (attribute.hasAttribute("value")) {
                setAttribute(attributeName, (String) EncryptionManager.decrypt(attribute.getAttribute("value")));
                continue;
            }

            // Check to see if there is a null attribute
            if (attribute.hasAttribute("null")) {
                String nullString = attribute.getAttribute("null");
                if (nullString.equals("true")) {
                    setNullAttribute(attributeName);
                    continue;
                }
            }

            String rawAttribute = getContentsAsText(attribute);
            // If there are no contents, then it's to be cleared
            if (rawAttribute.length() == 0) {
                setClearAttribute(attributeName);
                continue;
            }
            String attributeValue = (String) EncryptionManager.decrypt(rawAttribute);

            setAttribute(attributeName, attributeValue);
        }

        // references
        NodeList references = gbean.getElementsByTagName("reference");
        for (int r = 0; r < references.getLength(); r++) {
            Element reference = (Element) references.item(r);

            String referenceName = reference.getAttribute("name");

            Set objectNamePatterns = new LinkedHashSet();
            NodeList patterns = reference.getElementsByTagName("pattern");

            // If there is no pattern, then its an empty set, so its a
            // cleared value
            if (patterns.getLength() == 0) {
                setClearReference(referenceName);
                continue;
            }

            for (int p = 0; p < patterns.getLength(); p++) {
                Element pattern = (Element) patterns.item(p);
                if (pattern == null)
                    continue;

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

    private static String getContentsAsText(Element element) throws InvalidGBeanException {
        String value = "";
        NodeList text = element.getChildNodes();
        for (int t = 0; t < text.getLength(); t++) {
            Node n = text.item(t);
            if (n.getNodeType() == Node.TEXT_NODE) {
                value += n.getNodeValue();
            } else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                OutputFormat of = new OutputFormat(Method.XML, null, false);
                of.setOmitXMLDeclaration(true);
                XMLSerializer serializer = new XMLSerializer(pw, of);
                try {
                    serializer.prepare();
                    serializer.serializeNode(n);
                    value += sw.toString();
                } catch (IOException ioe) {
                    throw new InvalidGBeanException("Error serializing GBean element", ioe);
                }
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

    public ArrayList getClearAttributes() {
        return clearAttributes;
    }

    public ArrayList getNullAttributes() {
        return nullAttributes;
    }

    public boolean getNullAttribute(String attributeName) {
        return nullAttributes.contains(attributeName);
    }

    public boolean getClearAttribute(String attributeName) {
        return clearAttributes.contains(attributeName);
    }

    public ArrayList getClearReferences() {
        return clearReferences;
    }

    public boolean getClearReference(String referenceName) {
        return clearReferences.contains(referenceName);
    }

    public void setClearAttribute(String attributeName) {
        if (!clearAttributes.contains(attributeName))
            clearAttributes.add(attributeName);
    }

    public void setNullAttribute(String attributeName) {
        if (!nullAttributes.contains(attributeName))
            nullAttributes.add(attributeName);
    }

    public void setClearReference(String referenceName) {
        if (!clearReferences.contains(referenceName))
            clearReferences.add(referenceName);
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

    /**
     * Creates a new child of the supplied parent with the data for this
     * GBeanOverride, adds it to the parent, and then returns the new
     * child element.
     */
    public Element writeXml(Document doc, Element parent) {
        String gbeanName;
        if (name instanceof String) {
            gbeanName = (String) name;
        } else {
            gbeanName = name.toString();
        }

        Element gbean = doc.createElement("gbean");
        parent.appendChild(gbean);
        gbean.setAttribute("name", gbeanName);
        if (gbeanInfo != null) {
            gbean.setAttribute("gbeanInfo", gbeanInfo);
        }
        if (!load) {
            gbean.setAttribute("load", "false");
        }

        // attributes
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (value == null) {
                setNullAttribute(name);
            }
            else {
                if (getNullAttribute(name)) {
                    nullAttributes.remove(name);
                }
                if (name.toLowerCase().indexOf("password") > -1) {
                    value = EncryptionManager.encrypt(value);
                }
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", name);
                gbean.appendChild(attribute);
                if (value.length() == 0) {
                    attribute.setAttribute("value", "");
                }
                else {
                    try {
                        //
                        // NOTE: Construct a new document to handle mixed content attribute values
                        //       then add nodes which are children of the first node.  This allows
                        //       value to be XML or text.
                        //
                        
                        DocumentBuilderFactory factory = XmlUtil.newDocumentBuilderFactory();
                        DocumentBuilder builder = factory.newDocumentBuilder();

                        /**
                         * if there was a value such as jdbc url with &amp; then when that value was oulled
                         * from the config.xml the &amp; would have been replaced/converted to '&', we need to check
                         * and change it back because an & would create a parse exception.
                         */
                        value = value.replaceAll("&(?!amp;)", "&amp;");

                        // Wrap value in an element to be sure we can handle xml or text values
                        String xml = "<fragment>" + value + "</fragment>";
                        InputSource input = new InputSource(new StringReader(xml));
                        Document fragment = builder.parse(input);

                        Node root = fragment.getFirstChild();
                        NodeList children = root.getChildNodes();
                        for (int i=0; i<children.getLength(); i++) {
                            Node child = children.item(i);

                            // Import the child (and its children) into the new document
                            child = doc.importNode(child, true);
                            attribute.appendChild(child);
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Failed to write attribute value fragment: " + e.getMessage(), e);
                    }
                }
            }
        }

        // cleared attributes
        for (Iterator iterator = clearAttributes.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            Element attribute = doc.createElement("attribute");
            gbean.appendChild(attribute);
            attribute.setAttribute("name", name);
        }

        // Null attributes
        for (Iterator iterator = nullAttributes.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            Element attribute = doc.createElement("attribute");
            gbean.appendChild(attribute);
            attribute.setAttribute("name", name);
            attribute.setAttribute("null", "true");
        }

        // references
        for (Iterator iterator = references.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            ReferencePatterns patterns = (ReferencePatterns) entry.getValue();

            Element reference = doc.createElement("reference");
            reference.setAttribute("name", name);
            gbean.appendChild(reference);

            Set patternSet;
            if (patterns.isResolved()) {
                patternSet = Collections.singleton(new AbstractNameQuery(patterns.getAbstractName()));
            } else {
                patternSet = patterns.getPatterns();
            }

            for (Iterator patternIterator = patternSet.iterator(); patternIterator.hasNext();) {
                AbstractNameQuery pattern = (AbstractNameQuery) patternIterator.next();
                Element pat = doc.createElement("pattern");
                reference.appendChild(pat);
                Artifact artifact = pattern.getArtifact();

                if (artifact != null) {
                    if (artifact.getGroupId() != null) {
                        Element group = doc.createElement("groupId");
                        group.appendChild(doc.createTextNode(artifact.getGroupId()));
                        pat.appendChild(group);
                    }
                    if (artifact.getArtifactId() != null) {
                        Element art = doc.createElement("artifactId");
                        art.appendChild(doc.createTextNode(artifact.getArtifactId()));
                        pat.appendChild(art);
                    }
                    if (artifact.getVersion() != null) {
                        Element version = doc.createElement("version");
                        version.appendChild(doc.createTextNode(artifact.getVersion().toString()));
                        pat.appendChild(version);
                    }
                    if (artifact.getType() != null) {
                        Element type = doc.createElement("type");
                        type.appendChild(doc.createTextNode(artifact.getType()));
                        pat.appendChild(type);
                    }
                }

                Map nameMap = pattern.getName();
                if (nameMap.get("module") != null) {
                    Element module = doc.createElement("module");
                    module.appendChild(doc.createTextNode(nameMap.get("module").toString()));
                    pat.appendChild(module);
                }

                if (nameMap.get("name") != null) {
                    Element patName = doc.createElement("name");
                    patName.appendChild(doc.createTextNode(nameMap.get("name").toString()));
                    pat.appendChild(patName);
                }
            }
        }

        // cleared references
        for (Iterator iterator = clearReferences.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            Element reference = doc.createElement("reference");
            reference.setAttribute("name", name);
            gbean.appendChild(reference);
        }

        return gbean;
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
