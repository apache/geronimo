/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.configuration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.geronimo.system.plugin.model.AttributeType;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.plugin.model.ModuleType;
import org.apache.geronimo.system.plugin.model.ObjectFactory;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public class AttributesXmlUtil {
    public static final XMLInputFactory XMLINPUT_FACTORY = XMLInputFactory.newInstance();
    public static final JAXBContext ATTRIBUTES_CONTEXT;
    public static final JAXBContext ATTRIBUTE_CONTEXT;
    public static final JAXBContext MODULE_CONTEXT;
    public static final JAXBContext GBEAN_CONTEXT;

    static {
        try {
            ATTRIBUTES_CONTEXT = JAXBContext.newInstance(AttributesType.class);
            MODULE_CONTEXT = JAXBContext.newInstance(ModuleType.class);
            GBEAN_CONTEXT = JAXBContext.newInstance(GbeanType.class);
            ATTRIBUTE_CONTEXT = JAXBContext.newInstance(AttributeType.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create jaxb contexts for plugin types", e);
        }
    }


    public static void writeAttribute(AttributeType metadata, Writer out) throws XMLStreamException, JAXBException {
        Marshaller marshaller = ATTRIBUTE_CONTEXT.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        JAXBElement<AttributeType> element = new ObjectFactory().createAttribute(metadata);
        marshaller.marshal(element, out);
    }
    public static String extractAttributeValue(AttributeType attr) throws JAXBException, XMLStreamException {
        StringWriter sw = new StringWriter();
        writeAttribute(attr, sw);
        String s = sw.toString();
        int start = s.indexOf('>');
        start = s.indexOf('>', start + 1);
        int end = s.lastIndexOf('<');
        if (end < start) {
            return null;
        }
        return s.substring(start + 1, end).trim();
    }

    public static void writeAttributes(AttributesType metadata, Writer out) throws XMLStreamException, JAXBException {
        Marshaller marshaller = ATTRIBUTES_CONTEXT.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        JAXBElement<AttributesType> element = new ObjectFactory().createAttributes(metadata);
        marshaller.marshal(element, out);
    }


    public static AttributesType loadAttributes(Reader in) throws ParserConfigurationException, IOException, SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = ATTRIBUTES_CONTEXT.createUnmarshaller();
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        JAXBElement<AttributesType> element = unmarshaller.unmarshal(xmlStream, AttributesType.class);
        AttributesType pluginList = element.getValue();
        return pluginList;
    }
    public static ModuleType loadModule(Reader in) throws ParserConfigurationException, IOException, SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = MODULE_CONTEXT.createUnmarshaller();
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        JAXBElement<ModuleType> element = unmarshaller.unmarshal(xmlStream, ModuleType.class);
        ModuleType pluginList = element.getValue();
        return pluginList;
    }
    public static GbeanType loadGbean(Reader in) throws ParserConfigurationException, IOException, SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = GBEAN_CONTEXT.createUnmarshaller();
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        JAXBElement<GbeanType> element = unmarshaller.unmarshal(xmlStream, GbeanType.class);
        GbeanType pluginList = element.getValue();
        return pluginList;
    }
    public static AttributeType loadAttribute(Reader in) throws ParserConfigurationException, IOException, SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = ATTRIBUTE_CONTEXT.createUnmarshaller();
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        JAXBElement<AttributeType> element = unmarshaller.unmarshal(xmlStream, AttributeType.class);
        AttributeType attributeType = element.getValue();
        return attributeType;
    }

/*
    public static class NamespaceFilter extends XMLFilterImpl {
        private static String PLUGIN_NS = "http://geronimo.apache.org/xml/ns/plugins-1.3";
        private static String GBEAN_NS = "http://geronimo.apache.org/xml/ns/attributes-1.2";
        private static String ENVIRONMENT_NS = "http://geronimo.apache.org/xml/ns/deployment-1.2";

        private String namespace;

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            if ("plugin-artifact".equals(localName)) {
                namespace = PLUGIN_NS;
            } else if ("gbean".equals(localName)) {
                namespace = GBEAN_NS;
            } else if ("environment".equals(localName)) {
                namespace = ENVIRONMENT_NS;
            }
            super.startElement(namespace, localName, qname, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(namespace, localName, qName);
            if ("plugin-artifact".equals(localName)) {
                namespace = null;
            } else if ("gbean".equals(localName)) {
                namespace = PLUGIN_NS;
            } else if ("environment".equals(localName)) {
                namespace = GBEAN_NS;
            }
        }
    }
*/
}
