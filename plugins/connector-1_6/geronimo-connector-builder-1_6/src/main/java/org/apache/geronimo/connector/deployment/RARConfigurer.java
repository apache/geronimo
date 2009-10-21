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

package org.apache.geronimo.connector.deployment;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import org.apache.geronimo.connector.deployment.dconfigbean.ResourceAdapter_1_0DConfigRoot;
import org.apache.geronimo.connector.deployment.jsr88.Connector15DCBRoot;
import org.apache.geronimo.deployment.spi.ModuleConfigurer;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class RARConfigurer implements ModuleConfigurer {

    public DeploymentConfiguration createConfiguration(DeployableObject deployable) {
        if (ModuleType.RAR.equals(deployable.getType())) {
            String ddBeanRootVersion = deployable.getDDBeanRoot().getDDBeanRootVersion();
            if (ddBeanRootVersion != null && ddBeanRootVersion.equals("1.5")) {
                return new RARConfiguration(deployable, new Connector15DCBRoot(deployable.getDDBeanRoot()));
            }
            String[] specVersion = deployable.getDDBeanRoot().getText("connector/spec-version");
            if (specVersion.length > 0 && "1.0".equals(specVersion[0])) {
                return new RARConfiguration(deployable, new ResourceAdapter_1_0DConfigRoot(deployable.getDDBeanRoot()));
            }
            throw new IllegalArgumentException("Unknown resource adapter version: " + deployable.getDDBeanRoot().getDDBeanRootVersion());
        } else {
            return null;
        }
    }

    public ModuleType getModuleType() {
        return ModuleType.RAR;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(RARConfigurer.class, NameFactory.DEPLOYMENT_CONFIGURER);
        infoFactory.addInterface(ModuleConfigurer.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
