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


package org.apache.geronimo.console.jmsmanager;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryWithKernel;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public class ManagementHelper {
    private final static String PLUGIN_HELPER_KEY = "org.apache.geronimo.console.activemq.ManagementHelper";
    private final Kernel kernel;

    public static ManagementHelper getManagementHelper(PortletRequest request) {
        ManagementHelper helper = (ManagementHelper) request.getPortletSession(true).getAttribute(PLUGIN_HELPER_KEY, PortletSession.APPLICATION_SCOPE);
        if (helper == null) {
            Kernel kernel = PortletManager.getKernel();
            helper = new ManagementHelper(kernel);
            request.getPortletSession().setAttribute(PLUGIN_HELPER_KEY, helper, PortletSession.APPLICATION_SCOPE);
        }
        return helper;
    }

    public ManagementHelper(Kernel kernel) {
        this.kernel = kernel;
    }

    public DeploymentManager getDeploymentManager() {
        DeploymentFactory factory = new DeploymentFactoryWithKernel(kernel);
        try {
            return factory.getDeploymentManager("deployer:geronimo:inVM", null, null);
        } catch (DeploymentManagerCreationException e) {
            //            log.error(e.getMessage(), e);
            return null;
        }
    }
}

