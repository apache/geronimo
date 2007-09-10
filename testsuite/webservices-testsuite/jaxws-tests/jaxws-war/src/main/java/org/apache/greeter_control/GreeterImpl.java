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

package org.apache.greeter_control;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;

import javax.jws.WebService;
import javax.jws.HandlerChain;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;

/* serviceName, portName specified in webservices.xml */
@WebService(serviceName = "SOAPService",
            portName = "SoapPort",
            endpointInterface = "org.apache.greeter_control.Greeter",
            targetNamespace = "http://apache.org/greeter_control")
/* two handlers specified in webservices.xml */
@HandlerChain(file="handlers.xml")
public class GreeterImpl implements Greeter {

    private static final Logger LOG =
        Logger.getLogger(GreeterImpl.class.getName());

    @Resource
    private WebServiceContext context;

    @Resource(name="greeting")
    private String greeting;

    public WebServiceContext getContext() {
        return context;
    }

    public String greetMe(String me) {
        LOG.info("Invoking greetMe " + me);

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

        // just playing around

        // send foo=BAR header
        Map responseHeaders = 
            (Map)ctx.get(MessageContext.HTTP_RESPONSE_HEADERS);
        if(responseHeaders == null) { 
			LOG.info("Can't get MessageContext.HTTP_RESPONSE_HEADERS from context");
        } else {
			ArrayList values = new ArrayList();
			values.add("BAR");
			responseHeaders.put("foo", values);
        }

        return greeting + " " + me;
    }

    @PostConstruct
    public void init() {
        System.out.println(this + " PostConstruct");
    }

    @PreDestroy()
    public void destroy() {
        System.out.println(this + " PreDestroy");
    }

    public String sayHi() {
        LOG.info("Invoking sayHi ");
        
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

    public void greetMeOneWay(String me){
        LOG.info("Invoking greetMeOneWay " + me);
    }


    public void pingMe()
        throws PingMeFault {
        LOG.info("Invoking pingMe ");
        throw new PingMeFault("Custom Fault", null);
    }
}
