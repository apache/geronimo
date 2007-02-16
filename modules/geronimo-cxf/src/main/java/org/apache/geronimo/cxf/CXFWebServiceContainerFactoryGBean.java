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

package org.apache.geronimo.cxf;

import java.net.URL;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerFactory;
import org.apache.geronimo.jaxws.PortInfo;

/**
 * @version $Rev$ $Date$
 */
public class CXFWebServiceContainerFactoryGBean implements WebServiceContainerFactory {

    private static final Log LOG = LogFactory.getLog(CXFWebServiceContainerFactoryGBean.class);

    private final PortInfo portInfo;
    private final Bus bus;
    private final Object endpointInstance;
    private Context context;
    private URL configurationBaseUrl;

    public CXFWebServiceContainerFactoryGBean(PortInfo portInfo,
            String endpointClassName,
            ClassLoader classLoader,
            Map componentContext,
            Kernel kernel,
            TransactionManager transactionManager,
            URL configurationBaseUrl)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        
        if (componentContext != null) {
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
        this.bus = (new CXFBusFactory()).createBus();
        this.configurationBaseUrl = configurationBaseUrl;
        
        Class endpointClass = classLoader.loadClass(endpointClassName);
        endpointInstance = endpointClass.newInstance();
    }

    public WebServiceContainer getWebServiceContainer() {
        return new CXFWebServiceContainer(portInfo, endpointInstance, bus, context, configurationBaseUrl);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CXFWebServiceContainerFactoryGBean.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addAttribute("portInfo", PortInfo.class, true, true);
        infoBuilder.addAttribute("endpointClassName", String.class, true, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("componentContext", Map.class, true, true);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addReference("TransactionManager", TransactionManager.class, NameFactory.TRANSACTION_MANAGER);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);

        infoBuilder.setConstructor(new String[]{"portInfo", "endpointClassName", "classLoader",
                "componentContext", "kernel", "TransactionManager", "configurationBaseUrl"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
