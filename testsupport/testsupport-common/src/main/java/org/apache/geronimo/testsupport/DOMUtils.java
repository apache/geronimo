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

package org.apache.geronimo.testsupport;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * @version $Rev: 514087 $ $Date: 2007-03-03 01:13:40 -0500 (Sat, 03 Mar 2007) $
 */
public class DOMUtils {

    public static Document load(String xml) throws Exception {
        DocumentBuilder builder = getDocumentBuilder();         
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        return document;
    }

    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        return builder;
    }
    
    private static void trimEmptyTextNodes(Node node) {
        Element element = null;
        if (node instanceof Document) {
            element = ((Document)node).getDocumentElement();
        } else if (node instanceof Element) {
            element = (Element)node;
        } else {
            return;
        }
        
        List<Node> nodesToRemove = new ArrayList<Node>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                trimEmptyTextNodes(child);
            } else if (child instanceof Text) {
                Text t = (Text)child;
                if (t.getData().trim().length() == 0) {
                    nodesToRemove.add(child);
                }
            }
        }
        
        for (Node n : nodesToRemove) {
            element.removeChild(n);
        }
    }
    
    public static void compareNodes(Node expected, Node actual, boolean trimEmptyTextNodes) throws Exception {
        if (trimEmptyTextNodes) {
            trimEmptyTextNodes(expected);
            trimEmptyTextNodes(actual);
        }
        compareNodes(expected, actual);
    }
    
    public static void compareNodes(Node expected, Node actual) throws Exception {
        if (expected.getNodeType() != actual.getNodeType()) {
            throw new Exception("Different types of nodes: " + expected + " " + actual);
        }
        if (expected instanceof Document) {
            Document expectedDoc = (Document)expected;
            Document actualDoc = (Document)actual;
            compareNodes(expectedDoc.getDocumentElement(), actualDoc.getDocumentElement());
        } else if (expected instanceof Element) {
            Element expectedElement = (Element)expected;
            Element actualElement = (Element)actual;
            
            // compare element names
            if (!expectedElement.getLocalName().equals(actualElement.getLocalName())) {
                throw new Exception("Element names do not match: " + expectedElement.getLocalName() + " " + actualElement.getLocalName());
            }   
            // compare element ns
            String expectedNS = expectedElement.getNamespaceURI();
            String actualNS = actualElement.getNamespaceURI();
            if ((expectedNS == null && actualNS != null) || (expectedNS != null && !expectedNS.equals(actualNS))) {
                throw new Exception("Element namespaces names do not match: " + expectedNS + " " + actualNS);
            }
            
            String elementName = "{" + expectedElement.getNamespaceURI() + "}" + actualElement.getLocalName();
            
            // compare attributes
            NamedNodeMap expectedAttrs = expectedElement.getAttributes();
            NamedNodeMap actualAttrs = actualElement.getAttributes();
            if (countNonNamespaceAttribures(expectedAttrs) != countNonNamespaceAttribures(actualAttrs)) {
                throw new Exception(elementName + ": Number of attributes do not match up: " + countNonNamespaceAttribures(expectedAttrs) + " " + countNonNamespaceAttribures(actualAttrs));
            }
            for (int i = 0; i < expectedAttrs.getLength(); i++) {
                Attr expectedAttr = (Attr)expectedAttrs.item(i);
                if (expectedAttr.getName().startsWith("xmlns")) {
                    continue;
                }
                Attr actualAttr = null;
                if (expectedAttr.getNamespaceURI() == null) {
                    actualAttr = (Attr)actualAttrs.getNamedItem(expectedAttr.getName());
                } else {
                    actualAttr = (Attr)actualAttrs.getNamedItemNS(expectedAttr.getNamespaceURI(), expectedAttr.getLocalName());
                }
                if (actualAttr == null) {
                    throw new Exception(elementName + ": No attribute found:" + expectedAttr);
                }
                if (!expectedAttr.getValue().equals(actualAttr.getValue())) {
                    throw new Exception(elementName + ": Attribute values do not match: " + expectedAttr.getValue() + " " + actualAttr.getValue());
                }
            }
            
            // compare children
            NodeList expectedChildren = expectedElement.getChildNodes();
            NodeList actualChildren = actualElement.getChildNodes();
            if (expectedChildren.getLength() != actualChildren.getLength()) {
                throw new Exception(elementName + ": Number of children do not match up: " + expectedChildren.getLength() + " " + actualChildren.getLength());
            }
            for (int i = 0; i < expectedChildren.getLength(); i++) {
                Node expectedChild = expectedChildren.item(i);
                Node actualChild = actualChildren.item(i);
                compareNodes(expectedChild, actualChild);
            }
        } else if (expected instanceof Text) {
            String expectedData = ((Text)expected).getData().trim();
            String actualData = ((Text)actual).getData().trim();
            
            if (!expectedData.equals(actualData)) {
                throw new Exception("Text does not match: " + expectedData + " " + actualData);
            }
        }
    }

    private static int countNonNamespaceAttribures(NamedNodeMap attrs) {
        int n = 0;
        for (int i = 0; i< attrs.getLength(); i++ ) {
            Attr attr = (Attr) attrs.item(i);
            if (!attr.getName().startsWith("xmlns")) {
                n++;
            }
        }
        return n;
    }

}
