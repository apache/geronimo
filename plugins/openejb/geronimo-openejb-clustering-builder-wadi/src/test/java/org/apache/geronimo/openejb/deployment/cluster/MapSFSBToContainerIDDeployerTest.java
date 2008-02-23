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

import java.util.List;

import junit.framework.TestCase;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class MapSFSBToContainerIDDeployerTest extends TestCase {

    public void testContainerIdAreSetForSFSB() throws Exception {
        AppModule appModule = new AppModule(getClass().getClassLoader(), "dummy.jar");
        List<EjbModule> ejbModules = appModule.getEjbModules();

        EjbJar ejbJar = new EjbJar();
        
        StatefulBean sfsb = new StatefulBean();
        String sfsbName = "SFSB";
        sfsb.setEjbName(sfsbName);
        ejbJar.addEnterpriseBean(sfsb);
        
        StatelessBean slsb = new StatelessBean();
        String slsbName = "SLSB";
        slsb.setEjbName(slsbName);
        ejbJar.addEnterpriseBean(slsb);
        
        EntityBean entity = new EntityBean();
        entity.setEjbName("entity");
        ejbJar.addEnterpriseBean(entity);
        
        MessageDrivenBean mdb = new MessageDrivenBean();
        mdb.setEjbName("mdb");
        ejbJar.addEnterpriseBean(mdb);
        
        EjbModule ejbModule = new EjbModule(ejbJar);

        OpenejbJar openejbJar = new OpenejbJar();
        EjbDeployment sfsbDeployment = new EjbDeployment();
        sfsbDeployment.setEjbName(sfsbName);
        openejbJar.addEjbDeployment(sfsbDeployment);
        
        ejbModule.setOpenejbJar(openejbJar);

        ejbModules.add(ejbModule);
        
        String targetContainerId = "targetContainerId";
        MapSFSBToContainerIDDeployer deployer = new MapSFSBToContainerIDDeployer(targetContainerId);
        deployer.deploy(appModule);
        
        assertEquals(targetContainerId, sfsbDeployment.getContainerId());
    }
    
}
