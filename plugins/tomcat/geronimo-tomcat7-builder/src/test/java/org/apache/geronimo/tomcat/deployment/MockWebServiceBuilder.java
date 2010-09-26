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
package org.apache.geronimo.tomcat.deployment;

import java.util.Map;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.webservices.SerializableWebServiceContainerFactoryGBean;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class MockWebServiceBuilder implements WebServiceBuilder {
    public void findWebServices(Module module, boolean isEJB, Map correctedPortLocations, Environment environment, Map sharedContext) throws DeploymentException {
    }

    public boolean configurePOJO(GBeanData targetGBean, String servletName, Module module, String seiClassName, DeploymentContext context) throws DeploymentException {
        AbstractName webServiceContainerFactoryName = context.getNaming().createChildName(targetGBean.getAbstractName(), "webServiceContainer", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        GBeanData webServiceContainerFactoryGBean = new GBeanData(webServiceContainerFactoryName, SerializableWebServiceContainerFactoryGBean.GBEAN_INFO);
        webServiceContainerFactoryGBean.setAttribute("webServiceContainer", new MockWebServiceContainer());
        try {
            context.addGBean(webServiceContainerFactoryGBean);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add webServiceContainerFactoryGBean", e);
        }
        targetGBean.setReferencePattern("WebServiceContainerFactory", webServiceContainerFactoryName);
        return true;
    }

    public boolean configureEJB(GBeanData targetGBean, String ejbName, Module module, Map sharedContext, Bundle bundle) throws DeploymentException {
        return true;
    }
}
