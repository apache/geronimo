/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.cxf.client;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.javaee.HandlerChainsType;
import org.apache.geronimo.cxf.CXFHandlerResolver;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.jaxws.HandlerChainsUtils;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.client.GenericService;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.naming.reference.SimpleReference;

public class CXFServiceReference extends SimpleReference implements
        ClassLoaderAwareReference, KernelAwareReference {

    private static final Log LOG = LogFactory.getLog(CXFServiceReference.class);

    private static final Class[] URL_SERVICE_NAME_CONSTRUCTOR = 
        new Class[] { URL.class, QName.class };
    
    private String serviceClassName;

    private ClassLoader classLoader;

    private AbstractName moduleName;

    private URI wsdlURI;

    private QName serviceQName;

    private Kernel kernel;

    private String handlerChainsXML;

    private Map<Object, EndpointInfo> seiInfoMap;

    private String referenceClassName;

    public CXFServiceReference(String serviceClassName,
                               String referenceClassName,
                               URI wsdlURI,
                               QName serviceQName,
                               AbstractName name,
                               String handlerChainsXML,
                               Map<Object, EndpointInfo> seiInfoMap) {
        this.serviceClassName = serviceClassName;
        this.referenceClassName = referenceClassName;
        this.wsdlURI = wsdlURI;
        this.serviceQName = serviceQName;
        this.moduleName = name;
        this.handlerChainsXML = handlerChainsXML;
        this.seiInfoMap = seiInfoMap;
    }
   
    private HandlerChainsType getHandlerChains() {
        try {
            return HandlerChainsUtils.toHandlerChains(this.handlerChainsXML, HandlerChainsType.class);
        } catch (JAXBException e) {          
            // this should not happen
            LOG.warn("Failed to deserialize handler chains", e);
            return null;
        }
    }
 
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    private Class loadClass(String name) throws NamingException {
        try {
            return this.classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            NamingException exception = new NamingException(
                    "Count not load class " + name);
            exception.initCause(e);
            throw exception;
        }
    }

    private URL getWsdlURL() {
        if (this.wsdlURI == null) {
            return null;
        }
        try {
            return new URL(this.wsdlURI.toString());
        } catch (MalformedURLException e1) {
            // not a URL, assume it's a local reference
            try {
                URL moduleBaseUrl = (URL) this.kernel.getAttribute(
                        this.moduleName, "configurationBaseUrl");
                return new URL(moduleBaseUrl.toString() + this.wsdlURI.toString());
            } catch (Exception e) {
                // this should not happen
                LOG.warn("Error constructing WSDL location", e);
                return null;
            }
        }
    }
    
    private Class getReferenceClass() throws NamingException {
        return (this.referenceClassName != null) ? loadClass(this.referenceClassName) : null;
    }

    public Object getContent() throws NamingException {
        Service instance = null;
        URL wsdlURL = getWsdlURL();
        Class serviceClass = loadClass(this.serviceClassName);
                
        if (Service.class.equals(serviceClass)) {
            serviceClass = GenericService.class;
        }
        
        instance = createServiceProxy(serviceClass, this.classLoader, this.serviceQName, wsdlURL);
        
        HandlerChainsType handlerChains = getHandlerChains();
        if (handlerChains != null) {
            JAXWSAnnotationProcessor annotationProcessor = 
                new JAXWSAnnotationProcessor(new JNDIResolver(), new WebServiceContextImpl());
            CXFHandlerResolver handlerResolver =
                new CXFHandlerResolver(this.classLoader, serviceClass, handlerChains, annotationProcessor);
            instance.setHandlerResolver(handlerResolver);
        }
        
        Class referenceClass = getReferenceClass();
        if (referenceClass != null && !Service.class.isAssignableFrom(referenceClass)) {
            // do port lookup
            return instance.getPort(referenceClass);
        } else {
            // return service
            return instance;
        }
    }
    
    private Service createServiceProxy(Class superClass, ClassLoader classLoader, QName serviceName, URL wsdlLocation) throws NamingException {
        Callback callback = new PortMethodInterceptor(this.seiInfoMap);
        Callback[] methodInterceptors = new Callback[]{NoOp.INSTANCE, callback};

        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(superClass);
        enhancer.setCallbackFilter(new PortMethodFilter());
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});        
        enhancer.setUseFactory(false);
        enhancer.setUseCache(false);        
        Class serviceClass = enhancer.createClass();

        Enhancer.registerCallbacks(serviceClass, methodInterceptors);
        
        FastConstructor constructor = 
            FastClass.create(serviceClass).getConstructor(URL_SERVICE_NAME_CONSTRUCTOR);
        Object[] arguments = 
            new Object[]{wsdlLocation, serviceName};
        
        LOG.debug("Initializing service with: " + wsdlLocation + " " + serviceName);
        
        try {
            return (Service)constructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            NamingException exception = new NamingException("Could not construct service proxy");
            exception.initCause(e.getTargetException());
            throw exception;
        }
    }

}
