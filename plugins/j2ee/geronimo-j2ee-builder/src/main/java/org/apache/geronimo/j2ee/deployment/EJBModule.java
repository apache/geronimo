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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EJBModule extends Module<XmlObject, XmlObject> {
    private AbstractName moduleCmpEngineName;

    public EJBModule(boolean standAlone, AbstractName moduleName, Environment environment, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD, AnnotatedApp annotatedApp) {
        super(standAlone, moduleName, environment, moduleFile, targetPath, specDD, vendorDD, originalSpecDD, null, annotatedApp);
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.EJB;
    }

//    public void addClass(URI location, String fqcn, byte[] bytes, DeploymentContext context) throws IOException, URISyntaxException {
//        context.addClass(location, fqcn, bytes);
//    }

    public AbstractName getModuleCmpEngineName() {
        return moduleCmpEngineName;
    }

    public void setModuleCmpEngineName(AbstractName moduleCmpEngineName) {
        this.moduleCmpEngineName = moduleCmpEngineName;
    }

}

