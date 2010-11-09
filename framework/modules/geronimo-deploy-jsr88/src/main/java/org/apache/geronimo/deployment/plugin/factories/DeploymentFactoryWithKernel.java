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

package org.apache.geronimo.deployment.plugin.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.apache.geronimo.deployment.spi.ModuleConfigurer;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 * @version $Rev: 503905 $ $Date: 2007-02-06 09:20:49 +1100 (Tue, 06 Feb 2007) $
 */
@GBean(j2eeType = "DeploymentFactory")
public class DeploymentFactoryWithKernel extends BaseDeploymentFactory {

    private final Kernel kernel;

    public DeploymentFactoryWithKernel(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel) {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        }
        this.kernel = kernel;
        DeploymentFactoryManager.getInstance().registerDeploymentFactory(this);
    }

    protected Collection<ModuleConfigurer> getModuleConfigurers() throws DeploymentManagerCreationException {
        Collection<ModuleConfigurer> moduleConfigurers = new ArrayList<ModuleConfigurer>();
        Set<AbstractName> configurerNames = kernel.listGBeans(new AbstractNameQuery(ModuleConfigurer.class.getName()));
        for (AbstractName configurerName : configurerNames) {
            try {
                ModuleConfigurer configurer = (ModuleConfigurer) kernel.getGBean(configurerName);
                moduleConfigurers.add(configurer);
            } catch (GBeanNotFoundException e) {
                throw (AssertionError) new AssertionError("No gbean found for name returned in query : " + configurerName).initCause(e);
            }
        }
        return moduleConfigurers;
    }

}
