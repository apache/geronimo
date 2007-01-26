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
package org.apache.geronimo.cxf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Provider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.xml.XMLBindingInfoFactoryBean;
import org.apache.cxf.binding.xml.XMLConstants;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.ProviderChainObserver;
import org.apache.cxf.jaxws.ProviderInvoker;
import org.apache.cxf.jaxws.binding.soap.JaxWsSoapBindingInfoFactoryBean;
import org.apache.cxf.jaxws.handler.AnnotationHandlerChainBuilder;
import org.apache.cxf.jaxws.javaee.HandlerChainType;
import org.apache.cxf.jaxws.javaee.HandlerChainsType;
import org.apache.cxf.jaxws.support.AbstractJaxWsServiceFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.jaxws.support.ProviderServiceFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractBindingInfoFactoryBean;
import org.apache.geronimo.cxf.annotations.AnnotationException;
import org.apache.geronimo.cxf.annotations.AnnotationProcessor;

/*
 * This class somewhat replicates CXF Endpoint functionality but it is necessary
 * to do custom annotation handling for the service and handlers.
 */
public class CXFEndpoint extends Endpoint {

    private Bus bus;

    private Object implementor;

    private Server server;

    private Service service;

    private JaxWsImplementorInfo implInfo;

    private AbstractJaxWsServiceFactoryBean serviceFactory;

    private String bindingURI;

    private PortInfo portInfo;

    private AnnotationProcessor annotationProcessor;

    public CXFEndpoint(Bus bus,
                       URL configurationBaseUrl,
                       Object instance,
                       String bindingURI) {
        this.bus = bus;
        this.implementor = instance;
        this.bindingURI = bindingURI;

        this.portInfo = (PortInfo) bus.getExtension(PortInfo.class);

        implInfo = new JaxWsImplementorInfo(implementor.getClass());

        if (implInfo.isWebServiceProvider()) {
            serviceFactory = new ProviderServiceFactoryBean(implInfo);
        } else {
            serviceFactory = new JaxWsServiceFactoryBean(implInfo);
        }
        serviceFactory.setBus(bus);

        /*
         * TODO: The WSDL processing needs to be improved
         */
        URL wsdlURL = getWsdlURL(configurationBaseUrl, this.portInfo.getWsdlFile());

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CXFServiceConfiguration configuration = new CXFServiceConfiguration(
                this.portInfo, wsdlURL);
        serviceFactory.getConfigurations().add(0, configuration);

        service = serviceFactory.create();

        service.put(Message.SCHEMA_VALIDATION_ENABLED, service
                .getEnableSchemaValidationForAllPort());

        if (implInfo.isWebServiceProvider()) {
            service.setInvoker(new ProviderInvoker((Provider<?>) instance));
        } else {
            service.setInvoker(new JAXWSMethodInvoker(instance));
        }

        JNDIResolver jndiResolver = (JNDIResolver) bus
                .getExtension(JNDIResolver.class);
        this.annotationProcessor = new CXFAnnotationProcessor(jndiResolver);
    }

    private URL getWsdlURL(URL configurationBaseUrl, String wsdlFile) {
        URL wsdlURL = null;
        if (wsdlFile != null) {

            try {
                wsdlURL = new URL(wsdlFile);
            } catch (MalformedURLException e) {
                // Not a URL, try as a resource
                wsdlURL = this.implementor.getClass().getResource(
                        "/" + wsdlFile);

                if (wsdlURL == null && configurationBaseUrl != null) {
                    // Cannot get it as a resource, try with
                    // configurationBaseUrl
                    try {
                        wsdlURL = new URL(configurationBaseUrl.toString()
                                + wsdlFile);
                    } catch (MalformedURLException ee) {
                        // ignore
                    }
                }
            }
        }
        return wsdlURL;
    }

    org.apache.cxf.endpoint.Endpoint getEndpoint() {
        return ((ServerImpl) getServer()).getEndpoint();
    }

    public ServerImpl getServer() {
        return (ServerImpl) server;
    }

    public Binding getBinding() {
        return ((JaxWsEndpointImpl) getEndpoint()).getJaxwsBinding();
    }

    public void setExecutor(Executor executor) {
        service.setExecutor(executor);
    }

    public Executor getExecutor() {
        return service.getExecutor();
    }

    @Override
    public Object getImplementor() {
        return implementor;
    }

    @Override
    public List<Source> getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPublished() {
        return server != null;
    }

    @Override
    public void publish(Object arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void publish(String address) {
        doPublish(address);
    }

    public void setMetadata(List<Source> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setProperties(Map<String, Object> arg0) {
        // TODO Auto-generated method stub
    }

    protected void doPublish(String address) {
        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setAddress(address);
        svrFactory.setServiceFactory(serviceFactory);
        svrFactory.setStart(false);

        // FIXME: setServiceClass() or setSerivceBean() ?
        svrFactory.setServiceClass(implementor.getClass());
        // svrFactory.setServiceBean(implementor);

        // TODO: Replace with discovery mechanism!!
        AbstractBindingInfoFactoryBean bindingFactory = null;
        if (XMLConstants.NS_XML_FORMAT.equals(bindingURI)
                || HTTPBinding.HTTP_BINDING.equals(bindingURI)) {
            bindingFactory = new XMLBindingInfoFactoryBean();
        } else {
            // Just assume soap otherwise...
            bindingFactory = new JaxWsSoapBindingInfoFactoryBean();
        }

        svrFactory.setBindingFactory(bindingFactory);

        server = svrFactory.create();
        
        init();

        if (implInfo.isWebServiceProvider()) {
            getServer().setMessageObserver(
                    new ProviderChainObserver(getEndpoint(), bus, implInfo));
        }

        org.apache.cxf.endpoint.Endpoint endpoint = getEndpoint();

        if (endpoint.getEnableSchemaValidation()) {
            endpoint.put(Message.SCHEMA_VALIDATION_ENABLED, endpoint
                    .getEnableSchemaValidation());
        }
        server.start();
    }

    protected void init() {
        // configure handlers
        try {
            configureHandlers();
        } catch (Exception e) {
            throw new RuntimeException("Error configuring handlers", e);
        }

        // inject resources into service
        try {
            injectResources(this.implementor);
        } catch (AnnotationException e) {
            // TODO: better way to deal with it
            throw new RuntimeException("Service resource injection failed", e);
        }

        // inject resources into handlers
        List<Handler> handlers = getBinding().getHandlerChain();
        for (Handler handler : handlers) {
            try {
                injectResources(handler);
            } catch (AnnotationException e) {
                // TODO: better way to deal with it
                throw new RuntimeException("Handler resource injection failed",
                        e);
            }
        }
    }

    private void injectResources(Object instance) throws AnnotationException {
        this.annotationProcessor.processAnnotations(instance);
        this.annotationProcessor.invokePostConstruct(instance);
    }

    protected void configureHandlers() throws Exception {
        AnnotationHandlerChainBuilder builder = (new AnnotationHandlerChainBuilder() {
            public ClassLoader getHandlerClassLoader() {
                return implementor.getClass().getClassLoader();
            }
        });

        // we'll do our own resource injection
        builder.setHandlerInitEnabled(false);

        List<Handler> chain = null;

        // handlers in DD overwrite the handlers in annotation
        HandlerChainsType handlerChains = this.portInfo.getHandlers();
        if (handlerChains != null && handlerChains.getHandlerChain() != null
                && handlerChains.getHandlerChain().size() > 0) {
            chain = new ArrayList<Handler>();
            for (HandlerChainType chainType : handlerChains.getHandlerChain()) {
                // TODO: check if the handler chain should be added to this
                // service
                chain.addAll(builder
                        .buildHandlerChainFromConfiguration(chainType));
            }
            chain = builder.sortHandlers(chain);
        } else {
            chain = builder.buildHandlerChainFromClass(implementor.getClass());
        }

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
        if (this.server != null) {
            this.server.stop();
        }
    }
}
