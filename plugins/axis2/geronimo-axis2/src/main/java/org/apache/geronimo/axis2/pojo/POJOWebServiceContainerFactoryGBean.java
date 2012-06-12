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

import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.geronimo.axis2.osgi.Axis2ModuleRegistry;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.PortInfo;
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
 * @version $Rev$ $Date$
 */
@GBean
public class POJOWebServiceContainerFactoryGBean implements WebServiceContainerFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(POJOWebServiceContainerFactoryGBean.class);

    private final ClassLoader classLoader;
    private final PortInfo portInfo;
    private final String endpointClassName;
    private Context context;
    private AnnotationHolder holder;
    private String contextRoot;
    private Bundle bundle;
    private Axis2ModuleRegistry axis2ModuleRegistry;
    private String webModuleName;
    private String catalogName;

    public POJOWebServiceContainerFactoryGBean(
                        @ParamAttribute(name="portInfo") PortInfo portInfo,
                        @ParamAttribute(name="endpointClassName") String endpointClassName,
                        @ParamAttribute(name="componentContext") Map componentContext,
                        @ParamReference(name="TransactionManager", namingType=NameFactory.JTA_RESOURCE) TransactionManager transactionManager,
                        @ParamAttribute(name="holder") AnnotationHolder holder,
                        @ParamAttribute(name="contextRoot") String contextRoot,
                        @ParamAttribute(name="catalogName") String catalogName,
                        @ParamReference(name="Axis2ModuleRegistry") Axis2ModuleRegistry axis2ModuleRegistry,
                        @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                        @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                        @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                        @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abName)
                     throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        if (componentContext != null) {
            componentContext.put("comp/env/WebServiceContext", new WebServiceContextReference());

            GeronimoUserTransaction userTransaction = new GeronimoUserTransaction(transactionManager);
            try {
                this.context = EnterpriseNamingContext.livenReferences(componentContext, userTransaction, kernel, classLoader, bundle, "comp/");
            } catch (NamingException e) {
                LOG.warn("Failed to create naming context", e);
            }
        }

        this.portInfo = portInfo;
        this.classLoader = classLoader;
        this.bundle = bundle;
        this.endpointClassName = endpointClassName;
        this.holder = holder;
        this.contextRoot = contextRoot;
        this.axis2ModuleRegistry = axis2ModuleRegistry;
        this.webModuleName = abName.getNameProperty(NameFactory.WEB_MODULE);
        this.catalogName = catalogName;
    }

    public WebServiceContainer getWebServiceContainer() {
        POJOWebServiceContainer container = new POJOWebServiceContainer(portInfo, endpointClassName, bundle, context, axis2ModuleRegistry, holder, contextRoot, webModuleName, catalogName);
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
}
