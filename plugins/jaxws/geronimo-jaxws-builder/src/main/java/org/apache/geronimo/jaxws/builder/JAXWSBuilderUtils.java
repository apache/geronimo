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

import java.net.URI;
import java.net.URL;

import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

public class JAXWSBuilderUtils {

    private static boolean isURL(String name) {
        try {
            new URL(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String normalizeWsdlPath(Module module, String wsdlLocation){
        // is Absolute URL path
        if (isURL(wsdlLocation)) return wsdlLocation;
        
        Module parentModule = module.getParentModule();

        if(parentModule == null) {
            return wsdlLocation;
        }

        // EAR
        //   L WAR
        if (module.getType().equals(ConfigurationModuleType.WAR) && parentModule.getType().equals(ConfigurationModuleType.EAR))
            return module.getTargetPathURI().resolve(wsdlLocation).toString();
        
        // EAR 
        //   L WAR
        //       L EJB
        if (module.getType().equals(ConfigurationModuleType.EJB) && parentModule.getType().equals(ConfigurationModuleType.WAR)
                && parentModule.getParentModule() != null && parentModule.getParentModule().getType().equals(ConfigurationModuleType.EAR))
            return parentModule.getTargetPathURI().resolve(wsdlLocation).toString();
        
        // EAR
        //   L EJB
        if(module.getType().equals(ConfigurationModuleType.EJB) && parentModule.getType().equals(ConfigurationModuleType.EAR)) {
            return module.getModuleURI().toString() + "!/" + wsdlLocation;
        }

        return wsdlLocation;
    }
    
    public static String normalizeCatalogPath(Module module, String catalogName) {
        if(isURL(catalogName)) {
            return catalogName;
        }

        Module parentModule = module.getParentModule();

        if(parentModule == null) {
            return catalogName;
        }
        // EAR
        // L WAR
        if(module.getType().equals(ConfigurationModuleType.WAR) && parentModule.getType().equals(ConfigurationModuleType.EAR)) {
            return module.getTargetPathURI().resolve(catalogName).toString();
        }
        
        // EAR
        //   L EJB
        if(module.getType().equals(ConfigurationModuleType.EJB) && parentModule.getType().equals(ConfigurationModuleType.EAR)) {
            return module.getModuleURI().toString() + "!/" + catalogName;
        }

        return catalogName;
    }
    
    private static boolean isURL(URI name) {
        try {
            name.toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static URI normalizeWsdlPath(Module module, URI wsdlUri){
        // is Absolute URL path
        if (isURL(wsdlUri)) return wsdlUri;
        
        // EAR
        //   L WAR
        if (module.getType().equals(ConfigurationModuleType.WAR) && module.getParentModule() != null && module.getParentModule().getType().equals(ConfigurationModuleType.EAR))
            return module.getTargetPathURI().resolve(wsdlUri);
        
        // EAR 
        //   L WAR
        //       L EJB
        if (module.getType().equals(ConfigurationModuleType.EJB) && module.getParentModule() != null && module.getParentModule().getType().equals(ConfigurationModuleType.WAR)
                && module.getParentModule().getParentModule() != null && module.getParentModule().getParentModule().getType().equals(ConfigurationModuleType.EAR))
            return module.getParentModule().getTargetPathURI().resolve(wsdlUri);
        
            
        return wsdlUri;
    }
}
