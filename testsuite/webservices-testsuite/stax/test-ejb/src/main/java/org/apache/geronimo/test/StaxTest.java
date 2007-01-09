/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.test;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamConstants;

/**
 * Performs basic tests using the Stax API. 
 * Some tests might be too delicate if tested with other parsers.
 */
public class StaxTest {

    private static String XML = "<foo attr1=\"value1\"><bar>myData</bar></foo>";
    
    public static void testParse(XMLInputFactory inFactory) throws Exception {
        StringReader reader = new StringReader(XML);
        
        XMLStreamReader stream = inFactory.createXMLStreamReader(reader);
        int depth = 0;
        while(stream.hasNext()) {
            int event = stream.next();
            System.out.println(event);
            switch (event) {
            case XMLStreamConstants.START_ELEMENT:
                if (depth == 0) {
                    if (!stream.getLocalName().equals("foo")) {
                        throw new Exception("unexpected element: " + stream.getLocalName());
                    }
                    if (1 != stream.getAttributeCount()) {
                        throw new Exception("unexpected number of attributes: " + 
                                           stream.getAttributeCount());
                    }
                    if (!("value1".equals(stream.getAttributeValue(0)))) {
                        throw new Exception("unexpected attribute value: " +
                                            stream.getAttributeValue(0));
                    }
                } else if (depth == 1) {
                    if (!stream.getLocalName().equals("bar")) {
                        throw new Exception("unexpected element: " + stream.getLocalName());
                    }
                    if (0 != stream.getAttributeCount()) {
                        throw new Exception("unexpected number of attributes: " + 
                                           stream.getAttributeCount());
                    }
                } else {
                    throw new Exception("unexpected element: " + stream.getLocalName());
                }
                depth++;
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (depth == 1) {
                    if (!stream.getLocalName().equals("foo")) {
                        throw new Exception("unexpected element: " + stream.getLocalName());
                    }
                } else if (depth == 2) {
                    if (!stream.getLocalName().equals("bar")) {
                        throw new Exception("unexpected element: " + stream.getLocalName());
                    }
                } else {
                    throw new Exception("unexpected element: " + stream.getLocalName());
                }
                depth--;
                break;
            case XMLStreamConstants.CHARACTERS:
                if (!(depth == 2 && "myData".equals(stream.getText()))) {
                    throw new Exception("unexpected character data: " + stream.getText());
                }
                break;
            }            
          }
    }
    
    public static void testStreamGenerate(XMLOutputFactory outFactory) throws Exception {
        
        StringWriter strWriter = new StringWriter();
        
        XMLStreamWriter writer = outFactory.createXMLStreamWriter(strWriter);
        writer.writeStartElement("foo");
        writer.writeAttribute("attr1", "value1");
        writer.writeStartElement("bar");
        writer.writeCharacters("myData");
        writer.writeEndElement();
        writer.writeEndElement();
        
        writer.flush();
        writer.close();
        
        String actual = strWriter.toString();
        
        System.out.println(actual);
        
        if (!XML.equals(actual)) {
            throw new Exception("Expected: " + XML + " Actual: " + actual);
        }
    }

    public static void testEventGenerate(XMLOutputFactory outFactory,
                                         XMLEventFactory eventFactory) throws Exception {
        
        StringWriter strWriter = new StringWriter();
        
        XMLEventWriter writer = outFactory.createXMLEventWriter(strWriter);
        
        
        writer.add(eventFactory.createStartElement("", null, "foo"));
        writer.add(eventFactory.createAttribute("attr1", "value1"));
        writer.add(eventFactory.createStartElement("", null, "bar"));
        writer.add(eventFactory.createCharacters("myData"));
        writer.add(eventFactory.createEndElement("", null, "bar"));
        writer.add(eventFactory.createEndElement("", null, "foo"));
        
        writer.flush();
        writer.close();
        
        String actual = strWriter.toString();
        
        System.out.println(actual);
        
        if (!XML.equals(actual)) {
            throw new Exception("Expected: " + XML + " Actual: " + actual);
        }
    }
}
