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
package org.apache.geronimo.cxf.pojo;

import java.net.URL;

import javax.xml.ws.WebServiceException;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.geronimo.cxf.CXFEndpoint;
import org.apache.geronimo.cxf.CXFServiceConfiguration;
import org.apache.geronimo.cxf.GeronimoJaxWsImplementorInfo;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.annotations.AnnotationException;

public class POJOEndpoint extends CXFEndpoint {
  
    public POJOEndpoint(Bus bus, URL configurationBaseUrl, Object instance) {
        super(bus, instance);
        
        String bindingURI = null;
        if (this.portInfo.getProtocolBinding() != null) {
            bindingURI = JAXWSUtils.getBindingURI(this.portInfo.getProtocolBinding());
        }
        implInfo = new GeronimoJaxWsImplementorInfo(implementor.getClass(), bindingURI);

        serviceFactory = new JaxWsServiceFactoryBean(implInfo);        
        serviceFactory.setBus(bus);
                
        String wsdlLocation = null;
        if (this.portInfo.getWsdlFile() != null) {
            wsdlLocation = this.portInfo.getWsdlFile();
        } else {
            wsdlLocation = implInfo.getWsdlLocation();
        }        
        URL wsdlURL = getWsdlURL(configurationBaseUrl, wsdlLocation);

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CXFServiceConfiguration configuration = 
            new CXFServiceConfiguration(this.portInfo, wsdlURL);
        serviceFactory.getConfigurations().add(0, configuration);

        service = serviceFactory.create();
        
        service.setInvoker(new JAXWSMethodInvoker(instance));       

        JNDIResolver jndiResolver = (JNDIResolver) bus.getExtension(JNDIResolver.class);
        this.annotationProcessor = new JAXWSAnnotationProcessor(jndiResolver, new POJOWebServiceContext());
    }
    
    protected void init() {        
        // configure and inject handlers
        try {
            initHandlers();
            injectHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }

        // inject resources into service
        try {
            injectResources(this.implementor);
        } catch (AnnotationException e) {
            throw new WebServiceException("Service resource injection failed", e);
        }
    }

    public void stop() {
        // call handler preDestroy
        destroyHandlers();

        // call service preDestroy
        this.annotationProcessor.invokePreDestroy(this.implementor);

        // shutdown server
        super.stop();
    }
}
