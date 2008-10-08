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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

@WebService(serviceName = "CalculatorService",
            portName = "CalculatorPort",
            name = "Calculator", 
            targetNamespace = "http://geronimo.apache.org/calculator")
@Addressing(enabled = true, required = true)     
@HandlerChain(file="handlers.xml")
public class CalculatorImpl {

    @Resource
    private WebServiceContext context;

    @Action(input="http://geronimo.apache.org/calculator/CalculatorPortType/getmyepr") 
    public W3CEndpointReference getEPR() {
        return (W3CEndpointReference)context.getEndpointReference();
    }

    public int add(int n1, int n2) {
        return n1 + n2;
    }
    
    public int multiply(int n1, int n2) {
        MessageContext ctx = context.getMessageContext();
        CalculatorHandler.verifyReferenceParameter(ctx);
        return n1 * n2;
    }
        
    @PostConstruct
    private void myInit() {
        System.out.println(this + " PostConstruct");
    }

    @PreDestroy()
    private void myDestroy() {
        System.out.println(this + " PreDestroy");
    }

}
