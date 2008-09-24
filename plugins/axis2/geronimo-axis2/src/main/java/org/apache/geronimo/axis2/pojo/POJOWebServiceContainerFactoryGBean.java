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

package org.apache.geronimo.axis2.pojo;

import java.net.URL;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.annotations.AnnotationHolder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.naming.reference.SimpleReference;
import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerFactory;

/**
 * @version $Rev$ $Date$
 */
public class POJOWebServiceContainerFactoryGBean implements WebServiceContainerFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(POJOWebServiceContainerFactoryGBean.class);
    
    private final ClassLoader classLoader;
    private final org.apache.geronimo.jaxws.PortInfo portInfo;
    private final String endpointClassName;
    private URL configurationBaseUrl;
    private Context context;
    private AnnotationHolder holder;
    private String contextRoot;

    public POJOWebServiceContainerFactoryGBean(org.apache.geronimo.jaxws.PortInfo portInfo,
                                               String endpointClassName,
                                               ClassLoader classLoader,
                                               Map componentContext,
                                               Kernel kernel,
                                               TransactionManager transactionManager,
                                               URL configurationBaseUrl,
                                               AnnotationHolder holder,
                                               String contextRoot)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        
        if (componentContext != null) {
            
            // The name should match WebServiceContextAnnotationHelper.RELATIVE_JNDI_NAME
            componentContext.put("env/WebServiceContext", new WebServiceContextReference());
            
            GeronimoUserTransaction userTransaction = new GeronimoUserTransaction(transactionManager);
            try {
                this.context = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext,
                        userTransaction,
                        kernel,
                        classLoader);
            } catch (NamingException e) {
                LOG.warn("Failed to create naming context", e);
            }
        }
        
        this.portInfo = portInfo;
        this.classLoader = classLoader;
        this.endpointClassName = endpointClassName;
        this.configurationBaseUrl = configurationBaseUrl;   
        this.holder = holder;
        this.contextRoot = contextRoot;
    }

    public WebServiceContainer getWebServiceContainer() {
        POJOWebServiceContainer container = new POJOWebServiceContainer(portInfo, endpointClassName, classLoader, 
                                                                        context, configurationBaseUrl, holder, contextRoot);
        try {
            container.init();
        } catch (Exception e) {
            throw new RuntimeException("Failure initializing web service containter", e);
        }
        return container;
    }

    private static class WebServiceContextReference extends SimpleReference {
        public Object getContent() throws NamingException {
            return new POJOWebServiceContext();
        }        
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(POJOWebServiceContainerFactoryGBean.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addAttribute("portInfo", org.apache.geronimo.jaxws.PortInfo.class, true, true);
        infoBuilder.addAttribute("endpointClassName", String.class, true, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("componentContext", Map.class, true, true);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addReference("TransactionManager", TransactionManager.class, NameFactory.JTA_RESOURCE);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);
        infoBuilder.addAttribute("holder", AnnotationHolder.class, true);
        infoBuilder.addAttribute("contextRoot", String.class, true, true);

        infoBuilder.setConstructor(new String[]{
                "portInfo", 
                "endpointClassName", 
                "classLoader",
                "componentContext", 
                "kernel", 
                "TransactionManager", 
                "configurationBaseUrl", 
                "holder", 
                "contextRoot"
        });
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
