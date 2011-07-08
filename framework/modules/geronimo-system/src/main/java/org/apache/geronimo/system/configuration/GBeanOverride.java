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
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.apache.geronimo.system.plugin.model.AttributeType;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.plugin.model.ReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class GBeanOverride implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(GBeanOverride.class);

    public static final String ATTRIBUTE_NAMESPACE = "http://geronimo.apache.org/xml/ns/attributes-1.2";
    private final Object name;
    private String comment;
    private boolean load;
    //Note that encrypted attributes are stored encrypted in this map.
    private final Map<String, String> attributes = new LinkedHashMap<String, String>();
    private final Map<String, String> propertyEditors = new HashMap<String, String>();
    private final Map<String, ReferencePatterns> references = new LinkedHashMap<String, ReferencePatterns>();
    private final Set<String> clearAttributes = new LinkedHashSet<String>();
    private final Set<String> nullAttributes = new LinkedHashSet<String>();
    private final Set<String> clearReferences = new LinkedHashSet<String>();
    private final String gbeanInfo;
    private final JexlExpressionParser expressionParser;

    public GBeanOverride(String name, boolean load, JexlExpressionParser expressionParser) {
        this.name = name;
        this.load = load;
        gbeanInfo = null;
        this.expressionParser = expressionParser;
    }

    public GBeanOverride(AbstractName name, boolean load, JexlExpressionParser expressionParser) {
        this.name = name;
        this.load = load;
        gbeanInfo = null;
        this.expressionParser = expressionParser;
    }

    public GBeanOverride(GBeanOverride original, String oldArtifact, String newArtifact) {
        Object name = original.name;
        if (name instanceof String) {
            name = replace((String) name, oldArtifact, newArtifact);
        } else if (name instanceof AbstractName) {
            String value = name.toString();
            value = replace(value, oldArtifact, newArtifact);
            name = new AbstractName(URI.create(value));
        }
        this.name = name;
        this.load = original.load;
        this.comment = original.getComment();
        this.attributes.putAll(original.attributes);
        this.propertyEditors.putAll(original.propertyEditors);
        this.references.putAll(original.references);
        this.clearAttributes.addAll(original.clearAttributes);
        this.nullAttributes.addAll(original.nullAttributes);
        this.clearReferences.addAll(original.clearReferences);
        this.gbeanInfo = original.gbeanInfo;
        this.expressionParser = original.expressionParser;
    }

    private static String replace(String original, String oldArtifact, String newArtifact) {
        int pos = original.indexOf(oldArtifact);
        if (pos == -1) {
            return original;
        }
        int last = -1;
        StringBuilder buf = new StringBuilder();
        while (pos > -1) {
            buf.append(original.substring(last + 1, pos));
            buf.append(newArtifact);
            last = pos + oldArtifact.length() - 1;
            pos = original.indexOf(oldArtifact, last);
        }
        buf.append(original.substring(last + 1));
        return buf.toString();
    }

    public GBeanOverride(GBeanData gbeanData, JexlExpressionParser expressionParser, Bundle bundle) throws InvalidAttributeException {
        GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
        this.gbeanInfo = gbeanInfo.getSourceClass();
        if (this.gbeanInfo == null) {
            throw new IllegalArgumentException("GBeanInfo must have a source class set");
        }
        name = gbeanData.getAbstractName();
        load = true;

        // set attributes
        for (Map.Entry<String, Object> entry : gbeanData.getAttributes().entrySet()) {
            String attributeName = entry.getKey();
            GAttributeInfo attributeInfo = gbeanInfo.getAttribute(attributeName);
            if (attributeInfo == null) {
                throw new InvalidAttributeException("No attribute: " + attributeName + " for gbean: " + gbeanData.getAbstractName());
            }
            // TODO: shouldn't we only save manageable attributes here?
            Object attributeValue = entry.getValue();
            setAttribute(attributeInfo, attributeValue, bundle);
        }

        // references can be coppied in blind
        references.putAll(gbeanData.getReferences());
        this.expressionParser = expressionParser;
    }

    public GBeanOverride(GbeanType gbean, JexlExpressionParser expressionParser) throws InvalidGBeanException {
        String nameString = gbean.getName();
        if (nameString.indexOf('?') > -1) {
            name = new AbstractName(URI.create(nameString));
        } else {
            name = nameString;
        }

        String gbeanInfoString = gbean.getGbeanInfo();
        if (gbeanInfoString != null && gbeanInfoString.length() > 0) {
            gbeanInfo = gbeanInfoString;
        } else {
            gbeanInfo = null;
        }
        if (gbeanInfo != null && !(name instanceof AbstractName)) {
            throw new InvalidGBeanException("A gbean element using the gbeanInfo attribute must be specified using a full AbstractName: name=" + nameString);
        }

        load = gbean.isLoad();
        comment = gbean.getComment();

        // attributes
        for (Object o : gbean.getAttributeOrReference()) {
            if (o instanceof AttributeType) {
                AttributeType attr = (AttributeType) o;

                String propertyEditor = attr.getPropertyEditor();
                if (null != propertyEditor) {
                    propertyEditors.put(attr.getName(), propertyEditor);
                }
                
                if (attr.isNull()) {
                    setNullAttribute(attr.getName());
                } else {
                    String value;
                    try {
                        value = AttributesXmlUtil.extractAttributeValue(attr);
                    } catch (JAXBException e) {
                        throw new InvalidGBeanException("Could not extract attribute value from gbean override", e);
                    } catch (XMLStreamException e) {
                        throw new InvalidGBeanException("Could not extract attribute value from gbean override", e);
                    }
                    if (value == null || value.length() == 0) {
                        setClearAttribute(attr.getName());
                    } else {
                        setAttribute(attr.getName(), value);
                    }
                }
            } else if (o instanceof ReferenceType) {
                ReferenceType ref = (ReferenceType) o;
                if (ref.getPattern().isEmpty()) {
                    setClearReference(ref.getName());
                } else {
                    Set<AbstractNameQuery> patternSet = new HashSet<AbstractNameQuery>();
                    for (ReferenceType.Pattern pattern : ref.getPattern()) {
                        String groupId = pattern.getGroupId();
                        String artifactId = pattern.getArtifactId();
                        String version = pattern.getVersion();
                        String type = pattern.getType();
                        String module = pattern.getModule();
                        String name = pattern.getName();

                        Artifact referenceArtifact = null;
                        if (artifactId != null) {
                            referenceArtifact = new Artifact(groupId, artifactId, version, type);
                        }
                        Map<String, String> nameMap = new HashMap<String, String>();
                        if (module != null) {
                            nameMap.put("module", module);
                        }
                        if (name != null) {
                            nameMap.put("name", name);
                        }
                        AbstractNameQuery abstractNameQuery = new AbstractNameQuery(referenceArtifact, nameMap, Collections.EMPTY_SET);
                        patternSet.add(abstractNameQuery);
                    }
                    ReferencePatterns patterns = new ReferencePatterns(patternSet);
                    setReferencePatterns(ref.getName(), patterns);
                }
            }
        }
        this.expressionParser = expressionParser;
    }

    public Object getName() {
        return name;
    }

    public String getGBeanInfo() {
        return gbeanInfo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isLoad() {
        return load;
    }

    public void setLoad(boolean load) {
        this.load = load;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public Set<String> getClearAttributes() {
        return clearAttributes;
    }

    public Set<String> getNullAttributes() {
        return nullAttributes;
    }

    public boolean isNullAttribute(String attributeName) {
        return nullAttributes.contains(attributeName);
    }

    public boolean isClearAttribute(String attributeName) {
        return clearAttributes.contains(attributeName);
    }

    public Set<String> getClearReferences() {
        return clearReferences;
    }

    public boolean isClearReference(String referenceName) {
        return clearReferences.contains(referenceName);
    }

    public void setClearAttribute(String attributeName) {
        clearAttributes.add(attributeName);
        // remove attribute from other maps
        nullAttributes.remove(attributeName);
        attributes.remove(attributeName);
    }

    public void setNullAttribute(String attributeName) {
        nullAttributes.add(attributeName);
        // remove attribute from other maps
        clearAttributes.remove(attributeName);
        attributes.remove(attributeName);
    }

    public void setClearReference(String referenceName) {
        clearReferences.add(referenceName);
        references.remove(referenceName);
    }

    public void setAttribute(GAttributeInfo attrInfo, Object attributeValue,
            Bundle bundle) throws InvalidAttributeException {
        String stringValue = getAsText(attrInfo.getName(), attributeValue, attrInfo.getType(), bundle);
        stringValue = (String) attrInfo.getEncryptedSetting().encrypt(stringValue);
        setAttribute(attrInfo.getName(), stringValue);
    }

    /**
     * This method should be discouraged for usage outside in future, as it does
     * not pass in encryption meta-information about the attribute being set.
     * 
     * Use setAttribute(GAttributeInfo attrInfo, Object attributeValue,
     * ClassLoader classLoader) instead.
     * 
     */
    private void setAttribute(String attributeName, String attributeValue) {
        if (attributeValue == null || attributeValue.length() == 0) {
            setClearAttribute(attributeName);
        } else {
            attributes.put(attributeName, attributeValue);
            // remove attribute from other maps
            clearAttributes.remove(attributeName);
            nullAttributes.remove(attributeName);
        }
    }
    
    public Map<String, ReferencePatterns> getReferences() {
        return references;
    }

    public ReferencePatterns getReferencePatterns(String name) {
        return references.get(name);
    }

    public void setReferencePatterns(String name, ReferencePatterns patterns) {
        references.put(name, patterns);
        clearReferences.remove(name);
    }

    public boolean applyOverrides(GBeanData data, Artifact configName, AbstractName gbeanName, Bundle bundle) throws InvalidConfigException {
        if (!isLoad()) {
            return false;
        }

        GBeanInfo gbeanInfo = data.getGBeanInfo();

        // set attributes
        for (Map.Entry<String, String> entry : getAttributes().entrySet()) {
            String attributeName = entry.getKey();
            GAttributeInfo attributeInfo = gbeanInfo.getAttribute(attributeName);
            if (attributeInfo == null) {
                throw new InvalidConfigException("No attribute: " + attributeName + " for gbean: " + data.getAbstractName());
            }
            String valueString = entry.getValue();
            Object value = getValue(attributeInfo, valueString, configName, gbeanName, bundle);
            data.setAttribute(attributeName, value);

            //encrypt any encryptable attributes set to plaintext by users
            if (valueString != null) {
                if (valueString.equals(attributeInfo.getEncryptedSetting().decrypt(valueString))) {
                    String encrypted = (String) attributeInfo.getEncryptedSetting().encrypt(valueString);
                    if (!encrypted.equals(valueString)) {
                        entry.setValue(encrypted);
                    }
                }
            }
        }

        //Clear attributes
        for (String attribute : getClearAttributes()) {
            data.clearAttribute(attribute);
        }

        //Null attributes
        for (String attribute : getNullAttributes()) {
            data.setAttribute(attribute, null);
        }

        // set references
        for (Map.Entry<String, ReferencePatterns> entry : getReferences().entrySet()) {
            String referenceName = entry.getKey();
            GReferenceInfo referenceInfo = gbeanInfo.getReference(referenceName);
            if (referenceInfo == null) {
                throw new InvalidConfigException("No reference: " + referenceName + " for gbean: " + data.getAbstractName());
            }

            ReferencePatterns referencePatterns = entry.getValue();

            data.setReferencePatterns(referenceName, referencePatterns);
        }

        //Clear references
        for (String reference : getClearReferences()) {
            data.clearReference(reference);
        }

        return true;
    }

    private synchronized Object getValue(GAttributeInfo attribute, String value, Artifact configurationName, AbstractName gbeanName, Bundle bundle) {
        if (value == null) {
            return null;
        }
        value = (String) attribute.getEncryptedSetting().decrypt(value);
        value = substituteVariables(attribute.getName(), value);
        PropertyEditor editor = loadPropertyEditor(attribute, bundle);
        editor.setAsText(value);
        log.debug("Setting value for " + configurationName + "/" + gbeanName + "/" + attribute.getName() + " to value " + value);
        return editor.getValue();
    }

    protected PropertyEditor loadPropertyEditor(GAttributeInfo attribute, Bundle bundle) {
        String propertyEditor = propertyEditors.get(attribute.getName());
        if (null == propertyEditor) {
            PropertyEditor editor;
            try {
                editor = PropertyEditors.findEditor(attribute.getType(), bundle);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Unable to load property editor for attribute type: " + attribute.getType());
            }            
            if (editor == null) {
                throw new IllegalStateException("Unable to parse attribute of type " + attribute.getType() + "; no editor found");
            }
            return editor;
        } else {
            try {
                Class propertyEditorClass = bundle.loadClass(propertyEditor);
                return (PropertyEditor) propertyEditorClass.newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot load property editor [" + propertyEditor + "]", ex);
            }
        }
    }

    public String substituteVariables(String attributeName, String input) {
        if (expressionParser != null) {
            return expressionParser.parse(input);
        }
        return input;
    }

    /**
     * Creates a new child of the supplied parent with the data for this
     * GBeanOverride, adds it to the parent, and then returns the new
     * child element.
     *
     * @return newly created element for this override
     */
    public GbeanType writeXml() {
        GbeanType gbean = new GbeanType();
        String gbeanName;
        if (name instanceof String) {
            gbeanName = (String) name;
        } else {
            gbeanName = name.toString();
        }
        gbean.setName(gbeanName);
        if (gbeanInfo != null) {
            gbean.setGbeanInfo(gbeanInfo);
        }
        if (!load) {
            gbean.setLoad(false);
        }
        if (comment != null) {
            gbean.setComment(comment);
        }

        // attributes
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                nullAttributes.add(name);
                clearAttributes.remove(name);
            } else {                
                nullAttributes.remove(name);
                clearAttributes.remove(name);
/**
 * if there was a value such as jdbc url with &amp; then when that value was oulled
 * from the config.xml the &amp; would have been replaced/converted to '&', we need to check
 * and change it back because an & would create a parse exception.
 */
                value = "<attribute xmlns='" + ATTRIBUTE_NAMESPACE + "'>" + value.replaceAll("&(?!amp;)", "&amp;") + "</attribute>";
                Reader reader = new StringReader(value);
                try {
                    AttributeType attribute = AttributesXmlUtil.loadAttribute(reader);
                    attribute.setName(name);
                    String editorClass = propertyEditors.get(name);
                    if (null != editorClass) {
                        attribute.setPropertyEditor(editorClass);
                    }
                    gbean.getAttributeOrReference().add(attribute);
                } catch (Exception e) {
                    log.error("Could not serialize attribute " + name + " in gbean " + gbeanName + ", value: " + value, e);
                }
            }
        }

        // cleared attributes
        for (String name : clearAttributes) {
            AttributeType attribute = new AttributeType();
            attribute.setName(name);
            gbean.getAttributeOrReference().add(attribute);
        }

        // Null attributes
        for (String name : nullAttributes) {
            AttributeType attribute = new AttributeType();
            attribute.setName(name);
            attribute.setNull(true);
            gbean.getAttributeOrReference().add(attribute);
        }

        // references
        for (Map.Entry<String, ReferencePatterns> entry : references.entrySet()) {
            String name = entry.getKey();
            ReferencePatterns patterns = entry.getValue();
            ReferenceType reference = new ReferenceType();
            reference.setName(name);

            Set<AbstractNameQuery> patternSet;
            if (patterns.isResolved()) {
                patternSet = Collections.singleton(new AbstractNameQuery(patterns.getAbstractName()));
            } else {
                patternSet = patterns.getPatterns();
            }

            for (AbstractNameQuery pattern : patternSet) {
                ReferenceType.Pattern patternType = new ReferenceType.Pattern();
                Artifact artifact = pattern.getArtifact();

                if (artifact != null) {
                    if (artifact.getGroupId() != null) {
                        patternType.setGroupId(artifact.getGroupId());
                    }
                    if (artifact.getArtifactId() != null) {
                        patternType.setArtifactId(artifact.getArtifactId());
                    }
                    if (artifact.getVersion() != null) {
                        patternType.setVersion(artifact.getVersion().toString());
                    }
                    if (artifact.getType() != null) {
                        patternType.setType(artifact.getType());
                    }
                }

                Map nameMap = pattern.getName();
                if (nameMap.get("module") != null) {
                    patternType.setModule((String) nameMap.get("module"));
                }

                if (nameMap.get("name") != null) {
                    patternType.setName((String) nameMap.get("name"));
                }
                reference.getPattern().add(patternType);
            }
            gbean.getAttributeOrReference().add(reference);
        }

        // cleared references
        for (String name : clearReferences) {
            ReferenceType reference = new ReferenceType();
            reference.setName(name);
            gbean.getAttributeOrReference().add(reference);
        }

        return gbean;
    }

    protected String getAsText(String attributeName, Object value, String type, Bundle bundle) throws InvalidAttributeException {
        try {
            if (null == value || value instanceof String) {
                return (String) value;
            }
            
            Class typeClass = ClassLoading.loadClass(type, bundle);
            PropertyEditor editor = PropertyEditors.findEditor(value.getClass());
            if (null == editor) {
                editor = PropertyEditors.findEditor(typeClass);
                if (null == editor) {
                    throw new InvalidAttributeException("Unable to format attribute of type " + type + "; no editor found");
                }
            }
            
            if (!type.equals(value.getClass().getName())
                    && !typeClass.isPrimitive()
                    && !Collection.class.isAssignableFrom(typeClass)) {
                propertyEditors.put(attributeName, editor.getClass().getName());
            }

            editor.setValue(value);
            return editor.getAsText();
        } catch (ClassNotFoundException e) {
            //todo: use the Configuration's ClassLoader to load the attribute, if this ever becomes an issue
            throw (InvalidAttributeException) new InvalidAttributeException("Unable to store attribute type " + type).initCause(e);
        }
    }
}
