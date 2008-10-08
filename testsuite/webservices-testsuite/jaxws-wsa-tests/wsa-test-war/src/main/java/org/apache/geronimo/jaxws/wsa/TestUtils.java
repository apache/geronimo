/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.jaxws.wsa;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestUtils {
    
    private static final QName IS_REFERENCE_PARAMETER =
        new QName("http://www.w3.org/2005/08/addressing", "IsReferenceParameter");
    
    public static SOAPElement findElement(SOAPElement rootElement, String name) {
        Iterator iter = rootElement.getChildElements();
        while(iter.hasNext()) {
            Node node = (Node)iter.next();
            if (node instanceof SOAPElement && node.getLocalName().equals(name)) {
                return (SOAPElement)node;
            }
        }
        return null;
    }
        
    public static SOAPMessage createMessage(String msg) throws SOAPException {
        Source src = new StreamSource(new StringReader(msg));
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent(src);
        message.saveChanges();
        return message;
    }
        
    public static Document createDocument(String xml) 
        throws ParserConfigurationException, SAXException, IOException {
        return createDocument(new InputSource(new StringReader(xml)));
    }
    
    public static Document createDocument(InputSource source) 
        throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse(source);
    }
    
    public static void testResponse(SOAPMessage message, String responseAction, String responseElement, int result, boolean mtom) 
        throws SOAPException, IOException {
        message.writeTo(System.out);
        
        SOAPHeader header = message.getSOAPHeader();
        SOAPElement action = TestUtils.findElement(header, "Action");
        Assert.assertNotNull(action);
        Assert.assertEquals(responseAction, action.getValue());
        
        SOAPBody body = message.getSOAPBody();
        SOAPElement responseElem = TestUtils.findElement(body, responseElement);
        Assert.assertNotNull(responseElem);
        SOAPElement returnElem = TestUtils.findElement(responseElem, "return");
        Assert.assertNotNull(returnElem);
        Assert.assertEquals(String.valueOf(result), returnElem.getValue());            
    }
    
    public static void testReferenceProperties(SOAPMessage message, String name, String value) 
        throws SOAPException {
        SOAPHeader header = message.getSOAPHeader();
        SOAPElement propElement = TestUtils.findElement(header, name);            
        Assert.assertNotNull(propElement);
        Assert.assertEquals(value, propElement.getValue());
        String attrValue = propElement.getAttributeValue(IS_REFERENCE_PARAMETER);
        Assert.assertTrue(attrValue, 
                          "true".equalsIgnoreCase(attrValue) || "1".equalsIgnoreCase(attrValue));
    }
    
    public static ExtensibilityElement getExtensibilityElement(List elements, QName queryElement) {
        for (int i = 0; i < elements.size(); i++) {
            ExtensibilityElement element = (ExtensibilityElement)elements.get(i);
            if (queryElement.equals(element.getElementType())) {
                return element;
            }
        }
        return null;
    }
    
}