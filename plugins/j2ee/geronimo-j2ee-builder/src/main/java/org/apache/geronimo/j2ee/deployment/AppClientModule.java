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
import org.apache.openejb.jee.ApplicationClient;
import org.apache.xmlbeans.XmlObject;

/**
 * TODO there is almost certainly a problem with the serverEnvironment when deploying a stand alone app client outside an ear.
 *
 * @version $Rev$ $Date$
 */
public class AppClientModule extends Module<ApplicationClient, XmlObject> {
    private final Environment serverEnvironment;
    private JarFile earFile;
    private final AbstractName appClientName;
    private final String mainClassName;

    public AppClientModule(boolean standAlone, 
                           AbstractName moduleName, 
                           String name, 
                           AbstractName appClientName, 
                           Environment serverEnvironment, 
                           Environment clientEnvironment, 
                           JarFile moduleFile, 
                           String targetPath, 
                           ApplicationClient specDD, 
                           String mainClassName, 
                           XmlObject vendorDD, 
                           String originalSpecDD,
                           Map<JndiKey, Map<String, Object>> jndiContext,
                           Module<?, ?> parentModule) {
        super(standAlone, moduleName, name, clientEnvironment, moduleFile, targetPath, 
              specDD, vendorDD, originalSpecDD, null, jndiContext, parentModule);
        this.serverEnvironment = serverEnvironment;
        this.appClientName = appClientName;
        this.mainClassName = mainClassName;
        super.priority = 9;
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.CAR;
    }

    public Environment getServerEnvironment() {
        return serverEnvironment;
    }

    public JarFile getEarFile() {
        return earFile;
    }

    public void setEarFile(JarFile earFile) {
        this.earFile = earFile;
    }

    public AbstractName getAppClientName() {
        return appClientName;
    }

    public String getMainClassName() {
        return mainClassName;
    }

}


