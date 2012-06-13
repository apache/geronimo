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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

/* serviceName, portName, sei specified in webservices.xml */
@WebService(serviceName = "SOAPService",
            portName = "SoapPort",            
            targetNamespace = "http://apache.org/greeter_control")
public class GreeterImpl2 {

    @Resource
    private WebServiceContext context;

    @WebMethod(exclude=true)
    public WebServiceContext getContext() {
        return context;
    }

    @WebMethod
    public String greetMe(String me) {
        return "Hello " + me;
    }

    @WebMethod
    public String sayHi() {
        return "Hi";
    }
    
    @PostConstruct
    public void init() {
        System.out.println(this + " PostConstruct");
    }

    @PreDestroy()
    public void destroy() {
        System.out.println(this + " PreDestroy");
    }

}
