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
package org.apache.geronimo.j2ee.deployment;

import java.util.Map;
import java.util.jar.JarFile;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.openejb.jee.Application;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision$ $Date$
 */
public class ApplicationInfo extends Module<Application, XmlObject> {
    
    private ConfigurationModuleType type;

    public ApplicationInfo(ConfigurationModuleType type,
                           Environment environment,
                           AbstractName baseName,
                           String name,
                           JarFile earFile,
                           Application specDD,
                           XmlObject vendorDD,
                           String originalSpecDD) {
        this(type, environment, baseName, name, earFile, specDD, vendorDD, originalSpecDD, null);        
    }
    
    public ApplicationInfo(ConfigurationModuleType type,
                           Environment environment,
                           AbstractName baseName,
                           String name,
                           JarFile earFile,
                           Application specDD,
                           XmlObject vendorDD,
                           String originalSpecDD,
                           Map<JndiKey, Map<String, Object>> jndiContext) {
        super(true, baseName, name, environment, earFile, "", specDD, vendorDD, originalSpecDD, null, jndiContext, null);
        assert type != null;
        assert environment != null;
        assert modules != null;
        assert moduleLocations != null;

        this.type = type;
    }

    public ConfigurationModuleType getType() {
        return type;
    }

}