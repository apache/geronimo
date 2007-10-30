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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.thoughtworks.xstream.io.xml.DomWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.geronimo.kernel.util.XmlUtil;

/**
 * @version $Rev$ $Date$
 */
public class DomConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return Document.class.isAssignableFrom(clazz) || Element.class.isAssignableFrom(clazz);
    }

    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        DomReader reader;
        if (object instanceof Document) {
            Document doc = (Document) object;
            reader = new DomReader(doc);
        } else {
            Element element = (Element) object;
            reader = new DomReader(element);
        }

        copy(reader, writer);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        DocumentBuilderFactory documentBuilderFactory = XmlUtil.newDocumentBuilderFactory();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ConversionException("Cannot instantiate " + Document.class.getName(), e);
        }
        Document document = documentBuilder.newDocument();
        DomWriter writer = new DomWriter(document);

        copy(reader, writer);

        if (Document.class.isAssignableFrom(unmarshallingContext.getRequiredType())) {
            return document;
        } else {
            return document.getDocumentElement();
        }
    }

    public static void copy(HierarchicalStreamReader reader, HierarchicalStreamWriter writer) {
        writer.startNode(reader.getNodeName());

        // write the attributes
        int attributeCount = reader.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attributeName = reader.getAttributeName(i);
            String attributeValue = reader.getAttribute(i);
            writer.addAttribute(attributeName, attributeValue);
        }

        // write the child nodes recursively
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            copy(reader, writer);
            reader.moveUp();
        }

        // write the context if any
        String value = reader.getValue();
        if (value != null && value.trim().length() > 0) {
            writer.setValue(value);
        }

        writer.endNode();
    }
}
