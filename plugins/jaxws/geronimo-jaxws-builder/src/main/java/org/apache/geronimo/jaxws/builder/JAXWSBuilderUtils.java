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

package org.apache.geronimo.jaxws.builder;

import java.net.URL;

import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

public class JAXWSBuilderUtils {    
    public static boolean isURL(String name) {
        try {
            new URL(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isWSDLNormalizedRequired(Module module, String wsdlLocation) {
        return (module.getType().equals(ConfigurationModuleType.WAR) || (module.getType().equals(ConfigurationModuleType.EJB) && module.getParentModule() != null && module.getParentModule().getType()
                .equals(ConfigurationModuleType.WAR)))
                && !isURL(wsdlLocation);
    }
}
