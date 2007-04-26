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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.support.AbstractJaxWsServiceFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.Service;
import org.apache.geronimo.jaxws.PortInfo;

public abstract class CXFEndpoint extends Endpoint {

    protected Bus bus;

    protected Object implementor;

    protected Server server;

    protected Service service;

    protected JaxWsImplementorInfo implInfo;

    protected AbstractJaxWsServiceFactoryBean serviceFactory;

    protected PortInfo portInfo;

    public CXFEndpoint(Bus bus, Object implementor) {
        this.bus = bus;
        this.implementor = implementor;
        this.portInfo = (PortInfo) bus.getExtension(PortInfo.class);       
    }
  
    protected URL getWsdlURL(URL configurationBaseUrl, String wsdlFile) {
        URL wsdlURL = null;
        if (wsdlFile != null && wsdlFile.trim().length() > 0) {
            wsdlFile = wsdlFile.trim();
            try {
                wsdlURL = new URL(wsdlFile);
            } catch (MalformedURLException e) {
                // Not a URL, try as a resource
                wsdlURL = getImplementorClass().getResource("/" + wsdlFile);

                if (wsdlURL == null && configurationBaseUrl != null) {
                    // Cannot get it as a resource, try with
                    // configurationBaseUrl
                    try {
                        wsdlURL = new URL(configurationBaseUrl, wsdlFile);
                    } catch (MalformedURLException ee) {
                        // ignore
                    }
                }
            }
        }
        return wsdlURL;
    }
    
    protected Class getImplementorClass() {
        return this.implementor.getClass();
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
        svrFactory.setServiceBean(implementor);
              
        if (HTTPBinding.HTTP_BINDING.equals(implInfo.getBindingType())) {
            svrFactory.setTransportId("http://cxf.apache.org/bindings/xformat");
        }
        
        server = svrFactory.create();
        
        init();

        org.apache.cxf.endpoint.Endpoint endpoint = getEndpoint();

        if (getBinding() instanceof SOAPBinding) {
            ((SOAPBinding)getBinding()).setMTOMEnabled(this.portInfo.isMTOMEnabled());
        }
        
        /**
        if (endpoint.getEnableSchemaValidation()) {
            endpoint.ge
            endpoint.put(Message.SCHEMA_VALIDATION_ENABLED, 
                         endpoint.getEnableSchemaValidation());
        }
        **/
        server.start();
    }

    protected void init() { 
    }
          
    public void stop() {
        // shutdown server
        if (this.server != null) {
            this.server.stop();
        }
    }
}
