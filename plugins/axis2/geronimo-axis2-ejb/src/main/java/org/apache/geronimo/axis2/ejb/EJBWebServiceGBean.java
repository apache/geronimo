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

package org.apache.geronimo.axis2.ejb;

import java.util.Collection;
import java.util.Properties;

import javax.naming.Context;

import org.apache.geronimo.axis2.osgi.Axis2ModuleRegistry;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.openejb.BeanContext;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */

@GBean(j2eeType = NameFactory.WEB_SERVICE_LINK)
public class EJBWebServiceGBean implements GBeanLifecycle {

    private SoapHandler soapHandler;
    private String location;
    private EJBWebServiceContainer container;

    public EJBWebServiceGBean(@ParamReference(name="EjbDeployment")EjbDeployment ejbDeploymentContext,
                              @ParamReference(name="Axis2ModuleRegistry")Axis2ModuleRegistry axis2ModuleRegistry,
                              @ParamAttribute(name="portInfo")PortInfo portInfo,
                              @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel,
                              @ParamSpecial(type = SpecialAttributeType.bundle)Bundle bundle,
                              @ParamSpecial(type = SpecialAttributeType.abstractName)AbstractName abName,
                              @ParamReference(name="WebServiceContainer")Collection<SoapHandler> webContainers,
                              @ParamAttribute(name="policyContextID")String policyContextID,
                              @ParamReference(name="ConfigurationFactory")ConfigurationFactory configurationFactory,
                              @ParamAttribute(name="realmName")String realmName,
                              @ParamAttribute(name="authMethod")String authMethod,
                              @ParamAttribute(name="virtualHosts")String[] virtualHosts,
                              @ParamAttribute(name="properties")Properties properties,
                              @ParamAttribute(name="catalogName")String catalogName) throws Exception {

        if (ejbDeploymentContext == null || webContainers == null || webContainers.isEmpty() || portInfo == null) {
            return;
        }

        this.soapHandler = webContainers.iterator().next();
        this.location = portInfo.getLocation();

        assert this.location != null : "null location received";

        String beanClassName = ejbDeploymentContext.getBeanClass().getName();
        ClassLoader classLoader = ejbDeploymentContext.getClassLoader();
        BeanContext deploymnetInfo = ejbDeploymentContext.getDeploymentInfo();
        Context context = deploymnetInfo.getJndiEnc();

        this.container = new EJBWebServiceContainer(portInfo, beanClassName, bundle, context, axis2ModuleRegistry, deploymnetInfo, abName.getNameProperty(NameFactory.EJB_MODULE), catalogName);
        this.container.init();

        soapHandler.addWebService(this.location,
                                  virtualHosts,
                                  this.container,
                                  policyContextID,
                                  configurationFactory,
                                  realmName,
                                  authMethod,
                                  properties,
                                  classLoader);
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
        if (this.soapHandler != null) {
            this.soapHandler.removeWebService(this.location);
        }
        if (this.container != null) {
            this.container.destroy();
        }
    }

    public void doFail() {
    }

}
