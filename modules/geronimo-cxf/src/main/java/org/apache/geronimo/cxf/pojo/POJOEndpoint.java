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
import java.util.List;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.handler.PortInfoImpl;
import org.apache.cxf.jaxws.javaee.HandlerChainsType;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.geronimo.cxf.CXFEndpoint;
import org.apache.geronimo.cxf.CXFHandlerResolver;
import org.apache.geronimo.cxf.CXFServiceConfiguration;
import org.apache.geronimo.cxf.GeronimoJaxWsImplementorInfo;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.annotations.AnnotationException;
import org.apache.geronimo.jaxws.annotations.AnnotationProcessor;

public class POJOEndpoint extends CXFEndpoint {

    private AnnotationProcessor annotationProcessor;

    public POJOEndpoint(Bus bus, URL configurationBaseUrl, Object instance) {
        super(bus, instance);
        
        String bindingURI = null;
        if (this.portInfo.getProtocolBinding() != null) {
            bindingURI = JAXWSUtils.getBindingURI(this.portInfo.getProtocolBinding());
        }
        implInfo = new GeronimoJaxWsImplementorInfo(implementor.getClass(), bindingURI);

        serviceFactory = new JaxWsServiceFactoryBean(implInfo);        
        serviceFactory.setBus(bus);
                
        /*
         * TODO: The WSDL processing needs to be improved
         */
        URL wsdlURL = getWsdlURL(configurationBaseUrl, this.portInfo.getWsdlFile());

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CXFServiceConfiguration configuration = 
            new CXFServiceConfiguration(this.portInfo, wsdlURL);
        serviceFactory.getConfigurations().add(0, configuration);

        service = serviceFactory.create();

        service.put(Message.SCHEMA_VALIDATION_ENABLED, 
                    service.getEnableSchemaValidationForAllPort());

        service.setInvoker(new JAXWSMethodInvoker(instance));       

        JNDIResolver jndiResolver = (JNDIResolver) bus.getExtension(JNDIResolver.class);
        this.annotationProcessor = new JAXWSAnnotationProcessor(jndiResolver, new POJOWebServiceContext());
    }
    
    protected void init() {        
        // configure and inject handlers
        try {
            configureHandlers();
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

    private void injectResources(Object instance) throws AnnotationException {
        this.annotationProcessor.processAnnotations(instance);
        this.annotationProcessor.invokePostConstruct(instance);
    }

    /*
     * Gets the right handlers for the port/service/bindings and 
     * performs injection.
     */
    protected void configureHandlers() throws Exception {        
        HandlerChainsType handlerChains = this.portInfo.getHandlers(HandlerChainsType.class);
        CXFHandlerResolver handlerResolver =
            new CXFHandlerResolver(this.implementor.getClass().getClassLoader(), 
                                   this.implementor.getClass(),
                                   handlerChains, 
                                   this.annotationProcessor);
                      
        PortInfoImpl portInfo = new PortInfoImpl(implInfo.getBindingType(), 
                                                 serviceFactory.getEndpointName(),
                                                 service.getName());
        
        List<Handler> chain = handlerResolver.getHandlerChain(portInfo);

        getBinding().setHandlerChain(chain);
    }

    public void stop() {
        // call handlers preDestroy
        List<Handler> handlers = getBinding().getHandlerChain();
        for (Handler handler : handlers) {
            this.annotationProcessor.invokePreDestroy(handler);
        }

        // call service preDestroy
        this.annotationProcessor.invokePreDestroy(this.implementor);

        // shutdown server
        super.stop();
    }
}
