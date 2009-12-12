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
package org.apache.geronimo.kernel.config.xstream;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.net.URI;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.ReferencePatterns;

/**
 * @version $Rev$ $Date$
 */
public class GBeanDataConverter implements Converter {
    private final Mapper mapper;

    public GBeanDataConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    public boolean canConvert(Class clazz) {
        return GBeanData.class.isAssignableFrom(clazz);
    }

    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        GBeanData gbeanData = (GBeanData) object;

        // name
        AbstractName abstractName = gbeanData.getAbstractName();
        if (abstractName != null) {
            writer.addAttribute("name", abstractName.toString());
        }

        // gbeanInfo
        GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
        String sourceClass = gbeanInfo.getSourceClass();
        if (sourceClass != null) {
            writer.addAttribute("sourceClass", sourceClass);
        } else {
            writer.startNode("gbean-info");
            marshallingContext.convertAnother(gbeanInfo);
            writer.endNode();
        }

        // dependencies Set<ReferencePatterns>
        Set dependencies = gbeanData.getDependencies();
        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            ReferencePatterns referencePatterns = (ReferencePatterns) iterator.next();
            writer.startNode("dependency");
            marshallingContext.convertAnother(referencePatterns);
            writer.endNode();
        }

        // attributes Map<String, Object>
        Map attributes = gbeanData.getAttributes();
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String attributeName = (String) entry.getKey();
            Object attributeValue = entry.getValue();
            if (gbeanInfo.getAttribute(attributeName).isEncrypted()
                    && attributeValue != null) {
                attributeValue = EncryptionManager
                        .encrypt((String) attributeValue);
            }
            if (attributeValue != null) {
                writer.startNode("attribute");
                writer.addAttribute("name", attributeName);

                writer.startNode(mapper.serializedClass(attributeValue.getClass()));
                marshallingContext.convertAnother(attributeValue);
                writer.endNode();

                writer.endNode();
            }
        }
        // references Map<String, ReferencePatterns>
        Map references = gbeanData.getReferences();
        for (Iterator iterator = references.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String referenceName = (String) entry.getKey();
            ReferencePatterns referencePatterns = (ReferencePatterns) entry.getValue();
            writer.startNode("reference");
            writer.addAttribute("name", referenceName);
            marshallingContext.convertAnother(referencePatterns);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        // name
        String gbeanName = reader.getAttribute("name");
        AbstractName abstractName = null;
        if (gbeanName != null) {
            abstractName = new AbstractName(URI.create(gbeanName));
        }

        // gbeanInfo
        GBeanInfo gbeanInfo = null;
        String sourceClass = reader.getAttribute("sourceClass");
        if (sourceClass != null) {
            gbeanInfo = GBeanInfo.getGBeanInfo(sourceClass, classLoader);
        }

        Set dependencies = new LinkedHashSet();
        Map attributes = new LinkedHashMap();
        Map references = new LinkedHashMap();
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String nodeName = reader.getNodeName();
            if (nodeName.equals("gbean-info")) {
                if (gbeanInfo != null) {
                    throw new ConversionException("GBean info declared more than once in gbean " + abstractName);
                }
                gbeanInfo = (GBeanInfo) unmarshallingContext.convertAnother(reader, GBeanInfo.class);
            } else if (nodeName.equals("dependency")) {
                ReferencePatterns referencePatterns = (ReferencePatterns) unmarshallingContext.convertAnother(reader, ReferencePatterns.class);
                dependencies.add(referencePatterns);
            } else if (nodeName.equals("attribute")) {
                String attributeName = reader.getAttribute("name");

                reader.moveDown();
                String classAttribute = reader.getAttribute(mapper.attributeForImplementationClass());
                Class type;
                if (classAttribute == null) {
                    type = mapper.realClass(reader.getNodeName());
                } else {
                    type = mapper.realClass(classAttribute);
                }
                Object attributeValue = unmarshallingContext.convertAnother(reader, type);
                reader.moveUp();

                attributes.put(attributeName, attributeValue);
            } else if (nodeName.equals("reference")) {
                String referenceName = reader.getAttribute("name");
                ReferencePatterns referencePatterns = (ReferencePatterns) unmarshallingContext.convertAnother(reader, ReferencePatterns.class);
                references.put(referenceName, referencePatterns);
            } else {
                throw new ConversionException("Unknown nested node in GBean: " + nodeName);
            }

            reader.moveUp();
        }

        if (gbeanInfo == null) {
            throw new ConversionException("GBean info not declared in gbean " + abstractName);
        }

        GBeanData gbeanData = new GBeanData(abstractName, gbeanInfo);
        gbeanData.setDependencies(dependencies);
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String attributeName = (String) entry.getKey();
            Object attributeValue = entry.getValue();
            gbeanData.setAttribute(attributeName, attributeValue);
        }
        for (Iterator iterator = references.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String referenceName = (String) entry.getKey();
            ReferencePatterns referencePatterns = (ReferencePatterns) entry.getValue();
            gbeanData.setReferencePatterns(referenceName, referencePatterns);
        }

        return gbeanData;
    }
}
