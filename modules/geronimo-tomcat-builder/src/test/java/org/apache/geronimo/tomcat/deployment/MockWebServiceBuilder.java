/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.tomcat.deployment;

import java.util.Map;
import java.util.Collections;
import java.util.jar.JarFile;

import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.webservices.SerializableWebServiceContainerFactoryGBean;

/**
 * @version $Rev:$ $Date:$
 */
public class MockWebServiceBuilder implements WebServiceBuilder {
    public Map findWebServices(JarFile moduleFile, boolean isEJB, Map correctedPortLocations, Environment environment) throws DeploymentException {
        return Collections.EMPTY_MAP;
    }

    public void configurePOJO(GBeanData targetGBean, Module module, Object portInfo, String seiClassName, DeploymentContext context) throws DeploymentException {
        AbstractName webServiceContainerFactoryName = context.getNaming().createChildName(targetGBean.getAbstractName(), "webServiceContainer", NameFactory.GERONIMO_SERVICE);
        GBeanData webServiceContainerFactoryGBean = new GBeanData(webServiceContainerFactoryName, SerializableWebServiceContainerFactoryGBean.GBEAN_INFO);
        try {
            context.addGBean(webServiceContainerFactoryGBean);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add webServiceContainerFactoryGBean", e);
        }
        targetGBean.setReferencePattern("WebServiceContainerFactory", webServiceContainerFactoryName);
    }

    public void configureEJB(GBeanData targetGBean, JarFile moduleFile, Object portInfo, ClassLoader classLoader) throws DeploymentException {
    }
}
