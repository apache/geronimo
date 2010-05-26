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

import java.util.Collection;
import java.util.jar.JarFile;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApplicationClient;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlObject;

/**
 * TODO there is almost certainly a problem with the serverEnvironment when deploying a stand alone app client outside an ear.
 *
 * @version $Rev$ $Date$
 */
public class AppClientModule extends Module<XmlObject, XmlObject> {
    private final Environment serverEnvironment;
    private JarFile earFile;
    private final AbstractName appClientName;
    private final String mainClassName;
    private final Collection<ConnectorModule> resourceModules;

    public AppClientModule(boolean standAlone, 
                           AbstractName moduleName, 
                           String name, 
                           AbstractName appClientName, 
                           Environment serverEnvironment, 
                           Environment clientEnvironment, 
                           JarFile moduleFile, 
                           String targetPath, 
                           XmlObject specDD, 
                           String mainClassName, 
                           XmlObject vendorDD, 
                           String originalSpecDD, 
                           Collection<ConnectorModule> resourceModules, 
                           AnnotatedApplicationClient annotatedAppClient ) {
        super(standAlone, moduleName, name, clientEnvironment, moduleFile, targetPath, 
              specDD, vendorDD, originalSpecDD, null, annotatedAppClient, null, null );
        this.serverEnvironment = serverEnvironment;
        this.appClientName = appClientName;
        this.mainClassName = mainClassName;
        this.resourceModules = resourceModules;
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

    public Collection<ConnectorModule> getResourceModules() {
        return resourceModules;
    }

    public void close() {
        if (resourceModules != null) {
            for (ConnectorModule resourceModule : resourceModules) {
                resourceModule.close();
            }
        }
        super.close();
    }

}


