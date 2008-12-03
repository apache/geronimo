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
import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;

@WebService(serviceName = "EchoService", portName = "EchoPort", name = "Echo", targetNamespace = "http://geronimo.apache.org/echo")
@MTOM(enabled = true)
@HandlerChain(file = "handlers.xml")
@Stateless(mappedName = "EchoBean")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
public class EchoBean {

    @Resource
    private WebServiceContext context;

    public String hello(String name) {
        return "Hello, " + name;
    }

    public byte[] echoBytes(
            @WebParam(name = "useMTOM", targetNamespace = "") boolean useMTOM,
            @WebParam(name = "bytes", targetNamespace = "") byte[] bytes) {
        return bytes;
    }

    public byte[] echoImage(
            @WebParam(name = "useMTOM", targetNamespace = "") boolean useMTOM,
            @WebParam(name = "imageBytes", targetNamespace = "") byte[] imageBytes) {
        return imageBytes;
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
