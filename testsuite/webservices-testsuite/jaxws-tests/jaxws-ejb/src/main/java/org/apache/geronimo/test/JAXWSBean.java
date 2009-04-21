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

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.HandlerChain;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

@WebService
@Stateless(name="JAXWSBean")
@Remote(JAXWSGreeter.class)
@HandlerChain(file="handlers.xml")
@SOAPBinding(style=SOAPBinding.Style.RPC, 
             use=SOAPBinding.Use.LITERAL,
             parameterStyle=SOAPBinding.ParameterStyle.WRAPPED
)
public class JAXWSBean implements JAXWSGreeter { 

    private static final Logger LOG =
        Logger.getLogger(JAXWSBean.class.getName());

    @Resource
    private WebServiceContext context;

    public String greetMe(String me) {
        LOG.info("WebServiceContext: " + context);
        LOG.info("Principal: " + context.getUserPrincipal());
        LOG.info("Context: " + context.getMessageContext());

        MessageContext ctx = context.getMessageContext();
        Iterator iter = ctx.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            System.out.println("Key: "+entry.getKey());
            System.out.println("Value: " +entry.getValue());
        }

        System.out.println("i'm a ejb ws: " + me);

        if (!"foo bar".equals(me)) {
            throw new RuntimeException("Wrong parameter");
        }
        return "Hello " + me;
    }
    
    public String greetMeEjb(String me) {
        return "Hello EJB " + me;
    }
    
    public void greetMeFault(String me) {
        System.out.println("generate SOAP fault");
        SOAPFault fault = null;
        try {
            fault = SOAPFactory.newInstance().createFault();
            fault.setFaultCode(new QName("http://foo", "MyFaultCode"));
            fault.setFaultString("my error");
            fault.setFaultActor("my actor");
        } catch (SOAPException ex) {
            throw new RuntimeException(ex);
        }

        throw new SOAPFaultException(fault);
    }
    
}
