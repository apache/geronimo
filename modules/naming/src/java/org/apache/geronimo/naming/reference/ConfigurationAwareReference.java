/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.naming.reference;

import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;

import java.util.Set;

/**
 * @version $Rev:$ $Date:$
 */
public abstract class ConfigurationAwareReference extends SimpleAwareReference {

    private final Artifact configId;
    protected final AbstractNameQuery abstractNameQuery;

    protected ConfigurationAwareReference(Artifact configId, AbstractNameQuery abstractNameQuery) {
        this.configId = configId;
        this.abstractNameQuery = abstractNameQuery;
    }

    public Configuration getConfiguration() {
        Kernel kernel = getKernel();
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        return configurationManager.getConfiguration(configId);
    }

    public AbstractName resolveTargetName() throws GBeanNotFoundException {
        Configuration configuration = getConfiguration();
        try {
            return configuration.findGBean(abstractNameQuery);
        } catch (GBeanNotFoundException e) {
            Set results = getKernel().listGBeans(abstractNameQuery);
            if (results.size() == 1) {
                return (AbstractName) results.iterator().next();
            }
            throw new GBeanNotFoundException("Name query " + abstractNameQuery + " not satisfied in kernel, matches: " + results, e);
        }
    }

}
