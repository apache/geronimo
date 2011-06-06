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

package org.apache.geronimo.cxf.pojo;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.geronimo.cxf.CXFCatalogUtils;
import org.apache.geronimo.cxf.CXFWebServiceContainer;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.ServerJNDIResolver;
import org.apache.geronimo.jaxws.annotations.AnnotationHolder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.naming.reference.SimpleReference;
import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerFactory;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev: 508298 $ $Date: 2007-02-15 22:25:05 -0500 (Thu, 15 Feb 2007) $
 */
public class POJOWebServiceContainerFactoryGBean implements WebServiceContainerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(POJOWebServiceContainerFactoryGBean.class);

    private final Bus bus;
    private final Class servletClass;
    private final Bundle bundle;

    public POJOWebServiceContainerFactoryGBean(PortInfo portInfo,
                                               String endpointClassName,
                                               ClassLoader classLoader,
                                               Map componentContext,
                                               Kernel kernel,
                                               Bundle bundle,
                                               TransactionManager transactionManager,
                                               AnnotationHolder holder,
                                               String contextRoot)
            throws ClassNotFoundException,
                   IllegalAccessException,
                   InstantiationException {

        Context context = null;

        if (componentContext != null) {

            // The name should match WebServiceContextAnnotationHelper.RELATIVE_JNDI_NAME
            componentContext.put("env/WebServiceContext", new WebServiceContextReference());

            GeronimoUserTransaction userTransaction = new GeronimoUserTransaction(transactionManager);
            try {
                Context localContext = EnterpriseNamingContext.livenReferences(componentContext, userTransaction, kernel, classLoader, bundle, "comp/");
                Set<Context> contexts = new LinkedHashSet<Context>(3);
                contexts.add(localContext);
                context = EnterpriseNamingContext.createEnterpriseNamingContext(contexts);
            } catch (NamingException e) {
                LOG.warn("Failed to create naming context", e);
            }
        }

        this.bus = CXFWebServiceContainer.getBus();
        this.bundle = bundle;

        this.servletClass = classLoader.loadClass(endpointClassName);

        this.bus.setExtension(new ServerJNDIResolver(context), JNDIResolver.class);
        this.bus.setExtension(portInfo, PortInfo.class);
        this.bus.setExtension(context, Context.class);
        this.bus.setExtension(holder, AnnotationHolder.class);

        URL catalog = JAXWSUtils.getOASISCatalogURL(bundle, JAXWSUtils.DEFAULT_CATALOG_WEB);
        if (catalog != null) {
            CXFCatalogUtils.loadOASISCatalog(this.bus, catalog);
        }
    }

    public WebServiceContainer getWebServiceContainer() {
        return new POJOWebServiceContainer(bus, servletClass, bundle);
    }

    private static class WebServiceContextReference extends SimpleReference {
        public Object getContent() throws NamingException {
            return new WebServiceContextImpl();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(POJOWebServiceContainerFactoryGBean.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addAttribute("portInfo", PortInfo.class, true, true);
        infoBuilder.addAttribute("endpointClassName", String.class, true, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("componentContext", Map.class, true, true);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addAttribute("bundle", Bundle.class, false);
        infoBuilder.addReference("TransactionManager", TransactionManager.class, NameFactory.JTA_RESOURCE);
        infoBuilder.addAttribute("holder", AnnotationHolder.class, true);
        infoBuilder.addAttribute("contextRoot", String.class, true, true);

        infoBuilder.setConstructor(new String[]{
                "portInfo",
                "endpointClassName",
                "classLoader",
                "componentContext",
                "kernel",
                "bundle",
                "TransactionManager",
                "holder",
                "contextRoot"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
