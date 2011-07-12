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


package org.apache.geronimo.openejb;

import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.EjbResolver;
import org.apache.openejb.loader.SystemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:$ $Date:$
 */
@GBean(j2eeType = NameFactory.EJB_MODULE + "Starter")
public class EjbModuleStarter implements GBeanLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(EjbModuleStarter.class);

    private final EjbModuleImpl ejbModule;
    private final OpenEjbSystem openEjbSystem;

    public EjbModuleStarter(@ParamReference(name = "EjbModule", namingType = NameFactory.EJB_MODULE)EjbModuleImpl ejbModule,
                            @ParamReference(name = "OpenEjbSystem")OpenEjbSystem openEjbSystem) {
        this.ejbModule = ejbModule;
        this.openEjbSystem = openEjbSystem;
    }

    @Override
    public void doStart() throws Exception {
        List<BeanContext> allDeployments = ejbModule.getAppContext().getDeployments();
        //start code from openejb assembler

                // deploy
                for (BeanContext deployment : allDeployments) {
                    try {
                        Container container = deployment.getContainer();
                        container.deploy(deployment);
                        logger.info("createApplication.createdEjb" + deployment.getDeploymentID() + deployment.getEjbName() + container.getContainerID());
                        if (logger.isDebugEnabled()) {
                            for (Map.Entry<Object, Object> entry : deployment.getProperties().entrySet()) {
                                logger.info("createApplication.createdEjb.property" + deployment.getEjbName() + entry.getKey() + entry.getValue());
                            }
                        }
                    } catch (OpenEJBException e) {
                        logger.warn("Apparent double start of ejb?? ", e);
                    } catch (Throwable t) {
                        throw new OpenEJBException("Error deploying '"+deployment.getEjbName()+"'.  Exception: "+t.getClass()+": "+t.getMessage(), t);
                    }
                }

                // start
                for (BeanContext deployment : allDeployments) {
                    try {
                        Container container = deployment.getContainer();
                        container.start(deployment);
                        logger.info("createApplication.startedEjb" + deployment.getDeploymentID() + deployment.getEjbName() + container.getContainerID());
                    } catch (Throwable t) {
                        throw new OpenEJBException("Error starting '"+deployment.getEjbName()+"'.  Exception: "+t.getClass()+": "+t.getMessage(), t);
                    }
                }
        EjbResolver globalEjbResolver = SystemInstance.get().getComponent(EjbResolver.class);
        globalEjbResolver.addAll(ejbModule.getAppInfo().ejbJars);

        for (String deploymentId: ejbModule.getEjbDeploymentMap().keySet()) {
            BeanContext beanContext = openEjbSystem.getDeploymentInfo(deploymentId);
            GeronimoThreadContextListener.get().getEjbDeployment((BeanContext) beanContext);
        }
    }

    @Override
    public void doStop() throws Exception {
    }

    @Override
    public void doFail() {
    }
}
