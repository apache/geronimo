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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EjbModule extends EJBModule<OpenejbGeronimoEjbJarType> {
    private EjbJarInfo ejbJarInfo;
    private EjbDeploymentBuilder ejbDeploymentBuilder;
    private final org.apache.openejb.config.EjbModule ejbModule;
    private final ConfigurationFactory.Chain preAutoConfigDeployer;
    private final boolean shareJndi;

    public EjbModule(org.apache.openejb.config.EjbModule ejbModule,
                     boolean standAlone,
                     AbstractName moduleName,
                     String name,
                     Environment environment,
                     JarFile moduleFile,
                     String targetPath,
                     String originalSpecDD,
                     EjbJar specDD,
                     OpenejbGeronimoEjbJarType vendorDD,
                     Map<JndiKey, Map<String, Object>> jndiContext,
                     Module parentModule,
                     boolean shareJndi) {
        super(standAlone, moduleName, name, environment, moduleFile,
              targetPath, specDD, vendorDD, originalSpecDD, jndiContext, parentModule);
        this.ejbModule = ejbModule;
        this.ejbModule.setStandaloneModule(standAlone);

        preAutoConfigDeployer = new ConfigurationFactory.Chain();
        this.shareJndi = shareJndi;
    }

    @Override
    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.EJB;
    }

    public org.apache.openejb.config.EjbModule getEjbModule() {
        return ejbModule;
    }

    public EjbJarInfo getEjbJarInfo() {
        return ejbJarInfo;
    }

    public void setEjbJarInfo(EjbJarInfo ejbInfo) {
        this.ejbJarInfo = ejbInfo;
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
        if (ejbModule.getClientModule() != null) {
            ejbModule.getClientModule().setClassLoader(classLoader);
        }
    }

    public ConfigurationFactory.Chain getPreAutoConfigDeployer() {
        return preAutoConfigDeployer;
    }

    public Map<JndiKey, Map<String, Object>> getEjbJndiContext() {
        if (shareJndi) {
            return getJndiContext();
        } else {
            return Module.share(Module.MODULE, getJndiContext());
        }
    }

    @Override
    public void close() {
        if (ejbModule.getClassLoader() instanceof TempClassLoader) {
            ClassLoaderUtil.destroyClassLoader(ejbModule.getClassLoader().getParent());
            ClassLoaderUtil.destroyClassLoader(ejbModule.getClassLoader());
        }
        super.close();
    }

    Module newEJb(ClassFinder finder, EnterpriseBean bean) throws DeploymentException {
        Ejb ejb = new Ejb(isStandAlone(), getModuleName(), getName(), getEnvironment(), getModuleFile(), getTargetPath(),
                bean, getVendorDD(), getOriginalSpecDD(), getNamespace(),
                getEjbJndiContext(), this);
        ejb.setEarContext(getEarContext());
        ejb.setRootEarContext(getRootEarContext());
        ejb.setClassFinder(finder);
        return ejb;
    }

   static class Ejb extends Module<EnterpriseBean, XmlObject> {
        protected Ejb(boolean standAlone, AbstractName moduleName, String name, Environment environment, JarFile moduleFile, String targetPath, EnterpriseBean specDD, XmlObject vendorDD, String originalSpecDD, String namespace, Map<JndiKey, Map<String, Object>> jndiContext, Module<?, ?> parentModule) {
            super(standAlone, moduleName, name, environment, moduleFile, targetPath, specDD, vendorDD, originalSpecDD, namespace, jndiContext, parentModule);
        }

        @Override
        public ConfigurationModuleType getType() {
            return ConfigurationModuleType.EJB;
        }
    }
}
