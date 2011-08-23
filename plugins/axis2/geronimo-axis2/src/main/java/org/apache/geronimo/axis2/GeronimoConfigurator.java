/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.axis2;

import java.io.InputStream;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.util.Loader;

public class GeronimoConfigurator extends DeploymentEngine implements AxisConfigurator {

    private String resourceName;

    public GeronimoConfigurator(String resourceName) throws AxisFault {
        this.resourceName = resourceName;
    }

    public synchronized AxisConfiguration getAxisConfiguration() throws AxisFault {
        InputStream configStream = Loader.getResourceAsStream(this.resourceName);
        if (configStream == null) {
            configStream = GeronimoConfigurator.class.getClassLoader().getResourceAsStream(this.resourceName);
            if (configStream == null) {
                throw new AxisFault("Unable to find configuration: " + this.resourceName);
            }
        }
        this.axisConfig = populateAxisConfiguration(configStream);

        loadFromClassPath();

        this.axisConfig.setConfigurator(this);
        return this.axisConfig;
    }

    public void engageGlobalModules() throws AxisFault {
        engageModules();
    }

    @Override
    public void loadServices() {
    }
}
