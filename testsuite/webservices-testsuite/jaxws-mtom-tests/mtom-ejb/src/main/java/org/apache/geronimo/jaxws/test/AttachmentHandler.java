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

package org.apache.geronimo.jaxws.test;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.Assert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachmentHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentHandler.class);

    public void close(MessageContext messageContext) {
        // TODO Auto-generated method stub
    }

    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        boolean outbound = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        SOAPMessage soapMessage = soapMessageContext.getMessage();
        try {
            if (outbound) {
                //MTOM is always enabled
                Element xopInclude = findElementNode(soapMessage.getSOAPBody(), "include");
                //include element should be find in the soap message
                Assert.assertNotNull(xopInclude);
                //Attachment size should be 1
                int attachments = soapMessage.countAttachments();
                if (attachments == 0) {
                    LOG.warn("Expected 1 attachment but got 0");
                } else if (attachments == 1) {
                    // that's what we expect
                } else {
                    Assert.fail("Unexpected number of attachments: " + attachments);
                }
            } else {
                Element useMTOMElement = findElementNode(soapMessage.getSOAPBody(), "useMTOM");
                if (useMTOMElement != null) {
                    String sUseMTOM = getSubTextChildValue(useMTOMElement);
                    //If the useMTOM is true, the attachment size should be 1, or the attachment size should be 0
                    if (Boolean.parseBoolean(sUseMTOM))
                        Assert.assertEquals(soapMessage.countAttachments(), 1);
                    else
                        Assert.assertEquals(soapMessage.countAttachments(), 0);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private String getSubTextChildValue(Element element) {
        NodeList childrenList = element.getChildNodes();
        if (childrenList.getLength() == 0)
            return null;
        for (int i = 0; i < childrenList.getLength(); i++) {
            Node node = childrenList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE)
                return ((Text) node).getNodeValue();
        }
        return null;
    }

    private Element findElementNode(Element rootElement, String elementName) {
        if (rootElement.getLocalName().equalsIgnoreCase(elementName))
            return rootElement;
        NodeList childrenList = rootElement.getChildNodes();
        if (childrenList.getLength() > 0) {
            for (int i = 0; i < childrenList.getLength(); i++) {
                Node node = childrenList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element findElement = findElementNode((Element) node, elementName);
                    if (findElement != null)
                        return findElement;
                }
            }
        }
        return null;
    }

    public Set<QName> getHeaders() {
        // TODO Auto-generated method stub
        return null;
    }
}
