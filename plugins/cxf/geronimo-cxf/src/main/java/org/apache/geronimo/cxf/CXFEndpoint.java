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
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;

import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.handler.PortInfoImpl;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.annotations.AnnotationException;
import org.apache.geronimo.jaxws.annotations.AnnotationProcessor;
import org.apache.geronimo.jaxws.handler.GeronimoHandlerResolver;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CXFEndpoint {

    private static final Logger log = LoggerFactory.getLogger(CXFEndpoint.class);

    protected Bus bus;

    protected Object implementor;

    protected Server server;

    protected Service service;

    protected JaxWsImplementorInfo implInfo;

    protected JaxWsServiceFactoryBean serviceFactory;

    protected PortInfo portInfo;

    protected AnnotationProcessor annotationProcessor;

    private String address;

    private Bundle bundle;

    public CXFEndpoint(Bus bus, Object implementor, Bundle bundle) {
        this.bus = bus;
        this.implementor = implementor;
        this.portInfo = bus.getExtension(PortInfo.class);
        this.bundle = bundle;
        this.bus.setExtension(this, CXFEndpoint.class);
    }

    protected URL getWsdlURL(Bundle bundle, String wsdlFile) {
        if (wsdlFile == null || wsdlFile.trim().length() == 0) {
            return null;
        }
        URL wsdlURL = null;
        wsdlFile = wsdlFile.trim();
        try {
            wsdlURL = new URL(wsdlFile);
        } catch (MalformedURLException e) {
            // Not a URL, try as a resource
            wsdlURL = bundle.getResource("/" + wsdlFile);
            if (wsdlURL == null) {
                 try {
                    wsdlURL = BundleUtils.getEntry(bundle, wsdlFile);
                } catch (MalformedURLException e1) {
                    log.warn("MalformedURLException when getting entry:" + wsdlFile + " from bundle " + bundle.getSymbolicName(), e);
                }
            }
        }
        return wsdlURL;
    }

    protected Class getImplementorClass() {
        return this.implementor.getClass();
    }

    protected org.apache.cxf.endpoint.Endpoint getEndpoint() {
        return (getServer()).getEndpoint();
    }

    public boolean isSOAP11() {
       return SOAPBinding.SOAP11HTTP_BINDING.equals(implInfo.getBindingType()) ||
              SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(implInfo.getBindingType());
    }

    public boolean isHTTP() {
        return HTTPBinding.HTTP_BINDING.equals(implInfo.getBindingType());
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

    public Object getImplementor() {
        return implementor;
    }

    public boolean isPublished() {
        return server != null;
    }

    public void publish(String address) {
        doPublish(address);
    }

    private static class GeronimoJaxWsServerFactoryBean extends JaxWsServerFactoryBean {
        public GeronimoJaxWsServerFactoryBean() {
            // disable CXF resource injection
            doInit = false;
        }
    }

    protected void doPublish(String baseAddress) {
        // XXX: assume port 8080 by default since we don't know the actual port at startup
        String address = (baseAddress == null) ? "http://localhost:8080" : baseAddress;

        JaxWsServerFactoryBean svrFactory = new GeronimoJaxWsServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setAddress(address + this.portInfo.getLocation());
        svrFactory.setServiceFactory(serviceFactory);
        svrFactory.setStart(false);
        svrFactory.setServiceBean(implementor);

        if (HTTPBinding.HTTP_BINDING.equals(implInfo.getBindingType())) {
            svrFactory.setTransportId("http://cxf.apache.org/bindings/xformat");
        }

        server = svrFactory.create();

        init();

        //org.apache.cxf.endpoint.Endpoint endpoint = getEndpoint();

        if (getBinding() instanceof SOAPBinding && this.portInfo.getMtomFeatureInfo() != null) {
            ((SOAPBinding) getBinding()).setMTOMEnabled(this.portInfo.getMtomFeatureInfo().isEnabled());
        }

        server.start();
    }

    protected void init() {
    }

    /*
     * Update service's address on the very first invocation. The address
     * assumed at start up might not be valid.
     */
    synchronized void updateAddress(URI request) {
        if (this.address == null) {
            String requestAddress = CXFWebServiceContainer.getBaseUri(request);
            getEndpoint().getEndpointInfo().setAddress(requestAddress);
            this.address = requestAddress;
        }
    }

    /*
     * Set appropriate handlers for the port/service/bindings.
     */
    protected void initHandlers() throws Exception {
        GeronimoHandlerResolver handlerResolver = new GeronimoHandlerResolver(bundle, getImplementorClass(), portInfo.getHandlerChainsInfo(), null);
        PortInfoImpl portInfo = new PortInfoImpl(implInfo.getBindingType(),
                                                 serviceFactory.getEndpointName(),
                                                 service.getName());
        List<Handler> chain = handlerResolver.getHandlerChain(portInfo);
        getBinding().setHandlerChain(chain);
    }

    protected void injectResources(Object instance) throws AnnotationException {
        this.annotationProcessor.processAnnotations(instance);
        this.annotationProcessor.invokePostConstruct(instance);
    }

    protected void injectHandlers() {
        List<Handler> handlers = getBinding().getHandlerChain();
        try {
            for (Handler handler : handlers) {
                injectResources(handler);
            }
        } catch (AnnotationException e) {
            throw new WebServiceException("Handler annotation failed", e);
        }
    }

    protected void destroyHandlers() {
        if (this.annotationProcessor != null) {
            // call handlers preDestroy
            List<Handler> handlers = getBinding().getHandlerChain();
            for (Handler handler : handlers) {
                this.annotationProcessor.invokePreDestroy(handler);
            }
        }
    }

    public void stop() {
        // shutdown server
        if (this.server != null) {
            this.server.stop();
        }
    }
}
