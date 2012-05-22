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

package org.apache.geronimo.jaxws.client;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.HandlerResolver;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.feature.WebServiceFeatureInfo;
import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.apache.geronimo.naming.reference.BundleAwareReference;
import org.apache.geronimo.naming.reference.SimpleReference;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JAXWSServiceReference extends SimpleReference implements BundleAwareReference {

    private final static Logger LOG = LoggerFactory.getLogger(JAXWSServiceReference.class);

    private static final Class<?>[] URL_SERVICE_NAME_CONSTRUCTOR = new Class[] { URL.class, QName.class };

    protected String serviceClassName;
    protected Bundle bundle;
    protected AbstractName moduleName;
    protected URI wsdlURI;
    protected QName serviceQName;
    protected HandlerChainsInfo handlerChainsInfo;
    protected Map<Object, EndpointInfo> seiInfoMap;
    protected String referenceClassName;

    protected Class enhancedServiceClass;
    protected Callback[] methodInterceptors;
    protected FastConstructor serviceConstructor;


    public JAXWSServiceReference(HandlerChainsInfo handlerChainsInfo, Map<Object, EndpointInfo> seiInfoMap, AbstractName name, QName serviceQName, URI wsdlURI, String referenceClassName, String serviceClassName) {
        this.handlerChainsInfo = handlerChainsInfo;
        this.seiInfoMap = seiInfoMap;
        this.moduleName = name;
        this.serviceQName = serviceQName;
        this.wsdlURI = wsdlURI;
        this.referenceClassName = referenceClassName;
        this.serviceClassName = serviceClassName;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    private Class loadClass(String name) throws NamingException {
        try {
            return bundle.loadClass(name);
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

        URL wsdlURL = null;
        try {
            wsdlURL = new URL(this.wsdlURI.toString());
        } catch (MalformedURLException e1) {
            if (wsdlURL == null) {
                wsdlURL = bundle.getResource(this.wsdlURI.toString());
                if (wsdlURL == null) {
                    try {
                        wsdlURL = BundleUtils.getEntry(bundle, wsdlURI.toString());
                    } catch (MalformedURLException e) {
                        LOG.warn("MalformedURLException when getting entry:" + wsdlURI + " from bundle " + bundle.getSymbolicName(), e);
                        wsdlURL = null;
                    }
                }
                if (wsdlURL == null) {
                    LOG.warn("Failed to obtain WSDL: " + this.wsdlURI);
                }
                return wsdlURL;
            }
        }

        return wsdlURL;
    }

    protected URL getCatalog() {
        URL catalogURL = JAXWSUtils.getOASISCatalogURL(bundle, JAXWSUtils.DEFAULT_CATALOG_WEB);
        if (catalogURL == null) {
            catalogURL = JAXWSUtils.getOASISCatalogURL(bundle, JAXWSUtils.DEFAULT_CATALOG_EJB);
        }
        return catalogURL;
    }

    private Class<?> getReferenceClass() throws NamingException {
        return (this.referenceClassName != null) ? loadClass(this.referenceClassName) : null;
    }

    public Object getContent() throws NamingException {
        Service instance = null;
        URL wsdlURL = getWsdlURL();

        Class<?> serviceClass = loadClass(this.serviceClassName);
        Class<?> referenceClass = getReferenceClass();

        if (referenceClass != null && Service.class.isAssignableFrom(referenceClass)) {
            serviceClass = referenceClass;
        }

        if (Service.class == serviceClass) {
            serviceClass = GenericService.class;
        }

        instance = createServiceProxy(serviceClass, bundle, this.serviceQName, wsdlURL);

        HandlerResolver handlerResolver = getHandlerResolver(serviceClass);
        if(handlerResolver != null) {
            instance.setHandlerResolver(handlerResolver);
        }

        if (referenceClass != null && !Service.class.isAssignableFrom(referenceClass)) {
            // do port lookup
            QName portName = JAXWSUtils.getPortType(referenceClass);
            EndpointInfo endpointInfo = seiInfoMap.get(portName);
            if (endpointInfo != null && endpointInfo.getWebServiceFeatureInfos().size() > 0) {
                List<WebServiceFeatureInfo> webServiceFeatureInfos = endpointInfo.getWebServiceFeatureInfos();
                WebServiceFeature[] webServiceFeatures = new WebServiceFeature[webServiceFeatureInfos.size()];
                int index = 0;
                for (WebServiceFeatureInfo webServiceFeatureInfo : webServiceFeatureInfos) {
                    webServiceFeatures[index++] = webServiceFeatureInfo.getWebServiceFeature();
                }
                return instance.getPort(referenceClass, webServiceFeatures);
            } else {
                return instance.getPort(referenceClass);
            }
        } else {
            // return service
            return instance;
        }
    }

    protected abstract HandlerResolver getHandlerResolver(Class serviceClass);

    protected PortMethodInterceptor getPortMethodInterceptor() {
        return new PortMethodInterceptor(this.seiInfoMap);
    }

    private Service createServiceProxy(Class superClass, Bundle bundle, QName serviceName, URL wsdlLocation) throws NamingException {
        if (this.serviceConstructor == null) {
            // create method interceptors
            Callback callback = getPortMethodInterceptor();
            this.methodInterceptors = new Callback[] {NoOp.INSTANCE, callback};

            // create service class
            Enhancer enhancer = new Enhancer();
            enhancer.setClassLoader(new BundleClassLoader(bundle));
            enhancer.setSuperclass(superClass);
            enhancer.setCallbackFilter(new PortMethodFilter());
            enhancer.setCallbackTypes(new Class[] { NoOp.class, MethodInterceptor.class });
            enhancer.setUseFactory(false);
            enhancer.setUseCache(false);
            this.enhancedServiceClass = enhancer.createClass();

            // get constructor
            this.serviceConstructor =
                FastClass.create(this.enhancedServiceClass).getConstructor(URL_SERVICE_NAME_CONSTRUCTOR);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing service with: " + wsdlLocation + " " + serviceName);
        }

        // associate the method interceptors with the generated service class on the current thread
        Enhancer.registerCallbacks(this.enhancedServiceClass, this.methodInterceptors);

        Object[] arguments = new Object[] { wsdlLocation, serviceName };

        try {
            return (Service)this.serviceConstructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            NamingException exception = new NamingException("Could not construct service proxy");
            exception.initCause(e.getTargetException());
            throw exception;
        }
    }
}
