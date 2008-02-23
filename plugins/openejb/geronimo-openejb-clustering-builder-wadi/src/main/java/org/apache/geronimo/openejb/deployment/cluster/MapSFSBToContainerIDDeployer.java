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

package org.apache.geronimo.openejb.deployment.cluster;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DynamicDeployer;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class MapSFSBToContainerIDDeployer implements DynamicDeployer {

    private final String containerId;

    public MapSFSBToContainerIDDeployer(String containerId) {
        if (null == containerId) {
            throw new IllegalArgumentException("containerId is required");
        }
        this.containerId = containerId;
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (org.apache.openejb.config.EjbModule ejbModule : appModule.getEjbModules()) {
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            EjbJar ejbJar = ejbModule.getEjbJar();
            for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    switch (sessionBean.getSessionType()) {
                        case STATEFUL:
                            String ejbName = sessionBean.getEjbName();
                            EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(ejbName);
                            if (null == ejbDeployment) {
                                throw new OpenEJBException("No ejbDeployment for ejbName [" + ejbName + "]");
                            }
                            ejbDeployment.setContainerId(containerId);
                    }
                }
            }
        }
        return appModule;
    }
}