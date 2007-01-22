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
package org.apache.geronimo.openejb.deployment;

import java.util.Map;
import java.util.jar.JarFile;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.openejb.alt.config.ejb.OpenejbJar;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EjbModule extends EJBModule {
    private String originalSpecDD;
    private XmlObject specDD;
    private ClassLoader classLoader;
    private final EjbJar ejbJar;
    private final OpenejbJar openejbJar;
    private EjbJarInfo ejbJarInfo;
    private EjbDeploymentBuilder ejbDeploymentBuilder;

    public EjbModule(boolean standAlone, AbstractName moduleName, Environment environment, JarFile moduleFile, String targetPath, EjbJar ejbJar, OpenejbJar openejbJar, OpenejbGeronimoEjbJarType geronimoOpenejb, String ejbJarXml, Map sharedContext) {
        super(standAlone, moduleName, environment, moduleFile, targetPath, null, geronimoOpenejb, ejbJarXml, sharedContext);
        this.ejbJar = ejbJar;
        this.openejbJar = openejbJar;

    }

    @Override
    public String getOriginalSpecDD() {
        return originalSpecDD;
    }

    public void setOriginalSpecDD(String originalSpecDD) {
        this.originalSpecDD = originalSpecDD;
    }

    @Override
    public XmlObject getSpecDD() {
        return specDD;
    }

    public void setSpecDD(XmlObject specDD) {
        this.specDD = specDD;
    }

    public org.apache.openejb.alt.config.EjbModule getEjbModule() {
        return new org.apache.openejb.alt.config.EjbModule(classLoader, getModuleFile().getName(), ejbJar,  openejbJar);
    }

    public EjbJarInfo getEjbJarInfo() {
        return ejbJarInfo;
    }

    public void setEjbJarInfo(EjbJarInfo ejbJarInfo) {
        this.ejbJarInfo = ejbJarInfo;
    }

    public EjbDeploymentBuilder getEjbBuilder() {
        return ejbDeploymentBuilder;
    }

    public void setEjbBuilder(EjbDeploymentBuilder ejbDeploymentBuilder) {
        this.ejbDeploymentBuilder = ejbDeploymentBuilder;
    }

    public EjbJar getEjbJar() {
        return ejbJar;
    }

    public OpenejbJar getOpenejbJar() {
        return openejbJar;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
