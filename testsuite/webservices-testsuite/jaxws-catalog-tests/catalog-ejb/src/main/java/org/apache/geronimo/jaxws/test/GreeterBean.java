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
package org.apache.geronimo.jaxws.test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName = "GreeterService",
            portName = "GreeterPort",
            name = "Greeter", 
            targetNamespace = "http://apache.org/greeter_control",
	        wsdlLocation = "META-INF/wsdl/greeter_service.wsdl")
@Stateless(name="GreeterBean")
public class GreeterBean { 

    @Resource
    private WebServiceContext context;

    public String greetMe(String me) {
        return "Hello " + me;
    }
            
    public String sayHi() {
        return "Hi!";
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
