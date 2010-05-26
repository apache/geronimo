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

import java.util.LinkedHashSet;
import java.util.jar.JarFile;

import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.deployment.ModuleList;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision$ $Date$
 */
public class ApplicationInfo extends Module<XmlObject, XmlObject> {
    
    private ConfigurationModuleType type;

    public ApplicationInfo(ConfigurationModuleType type,
                           Environment environment,
                           AbstractName baseName,
                           String name,
                           JarFile earFile,
                           XmlObject specDD,
                           XmlObject vendorDD,
                           LinkedHashSet<Module<?,?>> modules,
                           ModuleList moduleLocations,
                           String originalSpecDD,
                           AnnotatedApp annotatedApp) {
        super(true, baseName, name, environment, earFile, "", specDD, vendorDD, originalSpecDD, null, annotatedApp, moduleLocations, modules);
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