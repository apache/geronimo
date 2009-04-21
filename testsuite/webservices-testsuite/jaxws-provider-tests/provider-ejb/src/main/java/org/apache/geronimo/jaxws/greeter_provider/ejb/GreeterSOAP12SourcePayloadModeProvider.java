/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.jaxws.greeter_provider.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.geronimo.jaxws.greeter_provider.common.MessageUtils;

@BindingType(value = SOAPBinding.SOAP12HTTP_BINDING)
@ServiceMode(value = Service.Mode.PAYLOAD)
@WebServiceProvider(serviceName = "GreeterService", portName = "GreeterSOAP12SourcePayloadModePort", wsdlLocation = "wsdl/greeter_provider.wsdl", targetNamespace = "http://geronimo.apache.org/greeter_provider")
@Stateless(name = "GreeterSOAP12SourcePayloadModeProvider")
@Local(value = { EchoLocal.class, Provider.class })
@Remote(value = { EchoRemote.class })
public class GreeterSOAP12SourcePayloadModeProvider implements Provider<Source>, EchoLocal, EchoRemote {

    @Resource(type = WebServiceContext.class)
    private WebServiceContext webServiceContext;

    @PreDestroy()
    public void destroy() {
        System.out.println(this + " PreDestroy");
    }

    public String echo(String words) {
        return words;
    }

    @PostConstruct
    public void init() {
        System.out.println(this + " PostConstruct");
    }

    public Source invoke(Source source) {
        try {
            return MessageUtils.createResponseSOAPSource(SOAPConstants.SOAP_1_2_PROTOCOL, Service.Mode.PAYLOAD);
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }
}