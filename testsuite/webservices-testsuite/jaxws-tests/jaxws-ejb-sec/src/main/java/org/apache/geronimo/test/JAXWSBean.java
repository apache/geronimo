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

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

@WebService(wsdlLocation="wsdl/greeter.wsdl")
@Stateless
@Remote(JAXWSGreeter.class)
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

        System.out.println("i'm a ejb ws: " + me);

        return "Hello " + me;
    }
    
    public String greetMeEjb(String me) {
        return "Hello EJB " + me;
    }
    
}
