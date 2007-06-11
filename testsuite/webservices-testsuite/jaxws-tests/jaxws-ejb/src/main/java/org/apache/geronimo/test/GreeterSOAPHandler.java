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

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.WebServiceContext;

import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;

import javax.xml.namespace.QName;

import javax.xml.soap.*;

public class GreeterSOAPHandler implements SOAPHandler<SOAPMessageContext> {

    @Resource
    WebServiceContext context;

    @Resource(name="greeting")
    private String greeting;

    @PostConstruct
    public void init() {
        System.out.println(this + " init: " + context);
    }

    @PreDestroy
    public void destroy() {
        System.out.println(this + " destroy");
    }

    public void init(Map<String,Object> config) {
    }

    public boolean handleFault(SOAPMessageContext context) {
        System.out.println(this + " handleFault");
        return true;
    }

    public void close(MessageContext context) {
        System.out.println(this + " close");
    }

    public boolean handleMessage(SOAPMessageContext context) {
        System.out.println(this + " handleMessage: " + context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY) + " " + greeting);
        
        SOAPMessage message = context.getMessage();
        try {
            if ((Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
                // outbound

            } else {
                // inbound
                SOAPElement element = findElement(message.getSOAPBody(), "arg0");
                element.setValue("foo bar");

                // XXX: this does not work with Axis2
                //   message.getSOAPBody().getElementsByTagNameNS("*", "arg0").item(0).getFirstChild().setNodeValue("foo bar");

                message.saveChanges();
            }

            //  message.writeTo(System.out);
        } catch (Exception e) {
            throw new RuntimeException("handler failed", e);
        }

        return true;
    }

    private SOAPElement findElement(SOAPElement element, String name) {
        Iterator iter = element.getChildElements();
        while(iter.hasNext()) {
            Node child = (Node)iter.next();
            if (child instanceof SOAPElement) {
                SOAPElement childEl = (SOAPElement)child;
                if (name.equals(childEl.getElementName().getLocalName())) {
                    return childEl;
                } else {
                    return findElement(childEl, name);
                }
            }
        }
        return null;
    }

    public Set<QName> getHeaders() {
        System.out.println(this + " getHeaders");
        return new TreeSet<QName>();
    }

}
