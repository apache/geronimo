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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.Assert;

import org.w3c.dom.Element;

public class CalculatorHandler implements SOAPHandler<SOAPMessageContext> {
    
    @PostConstruct
    public void init() {
        System.out.println(this + " PostConstruct");
    }

    @PreDestroy
    public void destroy() {
        System.out.println(this + " PreDestroy");
    }

    public boolean handleMessage(SOAPMessageContext ctx) {
        System.out.println(this + " handleMessage()");
        boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!outbound) {
            SOAPMessage message = ctx.getMessage();
            try {
                SOAPBody body = message.getSOAPBody();
                SOAPElement responseElem = CalculatorHandler.findElement(body, "multiply");
                if (responseElem != null) {
                    CalculatorHandler.verifyReferenceParameter(ctx);
                }
            } catch (SOAPException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        return true;
    }
       
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
    
    public static void verifyReferenceParameter(MessageContext ctx) {
        List<Element> rp = (List<Element>)ctx.get(MessageContext.REFERENCE_PARAMETERS);
        Assert.assertNotNull(rp);
        Assert.assertFalse(rp.isEmpty());
        Assert.assertEquals("BarKey3", rp.get(0).getLocalName());
        Assert.assertEquals("FooBar", rp.get(0).getFirstChild().getNodeValue());
    }
    
    public boolean handleFault(SOAPMessageContext smc) {
        System.out.println(this + " handleFault()");
        return true;
    }
    
    public void close(MessageContext messageContext) {
        System.out.println(this + " close()");
    }
    
    public Set<QName> getHeaders(){
        return null;
    }
    
}
