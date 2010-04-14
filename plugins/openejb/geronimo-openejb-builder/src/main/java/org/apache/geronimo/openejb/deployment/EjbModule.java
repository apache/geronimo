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

import java.util.jar.JarFile;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EjbModule extends EJBModule {
    private String originalSpecDD;
    private XmlObject specDD;
    private EjbJarInfo ejbJarInfo;
    private EjbDeploymentBuilder ejbDeploymentBuilder;
    private OpenejbGeronimoEjbJarType vendorDD;
    private final org.apache.openejb.config.EjbModule ejbModule;
    private final ConfigurationFactory.Chain preAutoConfigDeployer;

    public EjbModule(org.apache.openejb.config.EjbModule ejbModule, 
                     boolean standAlone, 
                     AbstractName moduleName, 
                     String name, 
                     Environment environment, 
                     JarFile moduleFile, 
                     String targetPath, 
                     String ejbJarXml, 
                     AnnotatedApp annoatedApp) {
        super(standAlone, moduleName, name, environment, moduleFile, 
              targetPath, null, null, ejbJarXml, annoatedApp);
        this.ejbModule = ejbModule;
        
        preAutoConfigDeployer = new ConfigurationFactory.Chain();
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

    @Override
    public OpenejbGeronimoEjbJarType getVendorDD() {
        return vendorDD;
    }

    public void setVendorDD(OpenejbGeronimoEjbJarType vendorDD) {
        this.vendorDD = vendorDD;
    }

    public org.apache.openejb.config.EjbModule getEjbModule() {
        return ejbModule;
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
        return ejbModule.getEjbJar();
    }

    public OpenejbJar getOpenejbJar() {
        return ejbModule.getOpenejbJar();
    }

    public ClassLoader getClassLoader() {
        return ejbModule.getClassLoader();
    }

    public void setClassLoader(ClassLoader classLoader) {
        ejbModule.setClassLoader(classLoader);
    }

    public ConfigurationFactory.Chain getPreAutoConfigDeployer() {
        return preAutoConfigDeployer;
    }
}
