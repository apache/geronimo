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

package org.apache.geronimo.activemq;

import java.util.Map;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.system.configuration.LocalAttributeManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigSubstitutionsHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConfigSubstitutionsHelper.class);

    public static Map<String, Object> getSubstitutions(BundleContext bundleContext,
                                                      Map<String, Object> defaultSubstitutions) {
        Kernel kernel = KernelRegistry.getSingleKernel();
        if (kernel != null) {
            try {
                LocalAttributeManager attributeManager = kernel.getGBean(LocalAttributeManager.class);
                if (attributeManager != null) {
                    return attributeManager.getConfigSubstitutionsVariables();
                }
            } catch (Exception e) {
                // ignore - fall through
                LOG.debug("Error getting config substitutions", e);
            }
        }
        return defaultSubstitutions;
    }

}
