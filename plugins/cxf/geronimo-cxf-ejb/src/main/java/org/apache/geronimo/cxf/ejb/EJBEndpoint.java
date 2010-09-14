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
package org.apache.geronimo.cxf.ejb;

import java.net.URL;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerInInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.geronimo.cxf.CXFEndpoint;
import org.apache.geronimo.cxf.CXFServiceConfiguration;
import org.apache.geronimo.cxf.GeronimoJaxWsImplementorInfo;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.openejb.BeanContext;
import org.osgi.framework.Bundle;

public class EJBEndpoint extends CXFEndpoint {

    public EJBEndpoint(Bus bus, Class instance, Bundle bundle) {
        super(bus, instance, bundle);

        implInfo = new GeronimoJaxWsImplementorInfo(instance, this.portInfo, instance.getClassLoader());

        serviceFactory = new JaxWsServiceFactoryBean(implInfo);
        serviceFactory.setBus(bus);

        String wsdlLocation = null;
        if (this.portInfo.getWsdlFile() != null) {
            wsdlLocation = this.portInfo.getWsdlFile();
        } else {
            wsdlLocation = implInfo.getWsdlLocation();
        }
        URL wsdlURL = getWsdlURL(bundle, wsdlLocation);

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CXFServiceConfiguration configuration =
            new CXFServiceConfiguration(this.portInfo, wsdlURL);
        serviceFactory.getConfigurations().add(0, configuration);

        service = serviceFactory.create();
    }

    @Override
    protected Class getImplementorClass() {
        return (Class)this.implementor;
    }

    @Override
    protected void init() {
        // configure handlers
        try {
            initHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }

        BeanContext beanContext =
            bus.getExtension(BeanContext.class);

        service.setInvoker(new EJBMethodInvoker(this, this.bus, beanContext));

        Endpoint endpoint = getEndpoint();

        /*
         * Remove interceptors that perform handler processing since
         * handler processing must happen within the EJB container.
         */
        removeHandlerInterceptors(bus.getInInterceptors());
        removeHandlerInterceptors(endpoint.getInInterceptors());
        removeHandlerInterceptors(endpoint.getBinding().getInInterceptors());
        removeHandlerInterceptors(endpoint.getService().getInInterceptors());

        // install SAAJ interceptor
        if (endpoint.getBinding() instanceof SoapBinding &&
            !this.implInfo.isWebServiceProvider()) {
            endpoint.getService().getInInterceptors().add(new SAAJInInterceptor());
        }
    }

    private static void removeHandlerInterceptors(List<Interceptor> interceptors) {
        for (Interceptor interceptor : interceptors) {
            if (interceptor instanceof MustUnderstandInterceptor ||
                interceptor instanceof LogicalHandlerInInterceptor ||
                interceptor instanceof SOAPHandlerInterceptor) {
                interceptors.remove(interceptor);
            }
        }
    }

    @Override
    public synchronized void injectHandlers() {
        if (this.annotationProcessor != null) {
            // assume injection was already done
            return;
        }

        WebServiceContext wsContext = null;
        try {
            InitialContext ctx = new InitialContext();
            wsContext = (WebServiceContext) ctx.lookup("java:comp/WebServiceContext");
        } catch (NamingException e) {
            throw new WebServiceException("Failed to lookup WebServiceContext", e);
        }

        this.annotationProcessor = new JAXWSAnnotationProcessor(new JNDIResolver(), wsContext);
        super.injectHandlers();
    }

    @Override
    public void stop() {
        // call handler preDestroy
        destroyHandlers();

        // shutdown server
        super.stop();
    }

}
