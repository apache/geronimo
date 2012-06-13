/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2.client;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.HandlerResolver;

import org.apache.axis2.jaxws.catalog.impl.OASISCatalogManager;
import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.client.JAXWSServiceReference;
import org.apache.geronimo.jaxws.client.PortMethodInterceptor;
import org.apache.geronimo.jaxws.handler.GeronimoHandlerResolver;
import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.apache.xml.resolver.Catalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class Axis2ServiceReference extends JAXWSServiceReference {

    private static final Logger LOG = LoggerFactory.getLogger(Axis2ServiceReference.class);

    public Axis2ServiceReference(String serviceClassName,
                                 String referenceClassName,
                                 URI wsdlURI,
                                 QName serviceQName,
                                 AbstractName name,
                                 HandlerChainsInfo handlerChainsInfo,
                                 Map<Object, EndpointInfo> seiInfoMap) {
        super(handlerChainsInfo, seiInfoMap, name, serviceQName, wsdlURI, referenceClassName, serviceClassName);
    }

    @Override
    public Object getContent() throws NamingException {
        DescriptionBuilderComposite composite = null;

        URL catalogURL = getCatalog();
        if (catalogURL != null) {
            composite = new DescriptionBuilderComposite();
            OASISCatalogManager catalogManager = new OASISCatalogManager();
            Catalog catalog = catalogManager.getCatalog();
            try {
                catalog.parseCatalog(catalogURL);
            } catch (IOException e) {
                NamingException toThrow = new NamingException("Fail to parse catalog file " + catalogURL.toString());
                toThrow.initCause(e);
                throw toThrow;
            }            
            composite.setCatalogManager(catalogManager);
        }

        ServiceDelegate.setServiceMetadata(composite);
        try {
            return super.getContent();
        } finally {
            ServiceDelegate.setServiceMetadata(null);
        }
    }

    protected HandlerResolver getHandlerResolver(Class serviceClass) {
        JAXWSAnnotationProcessor annotationProcessor =
                new JAXWSAnnotationProcessor(new JNDIResolver(), new WebServiceContextImpl());
        GeronimoHandlerResolver handlerResolver =
                new GeronimoHandlerResolver(bundle, serviceClass, handlerChainsInfo, annotationProcessor);
        return handlerResolver;
    }

    protected PortMethodInterceptor getPortMethodInterceptor() {
        return new Axis2PortMethodInterceptor(this.seiInfoMap);
    }

}
