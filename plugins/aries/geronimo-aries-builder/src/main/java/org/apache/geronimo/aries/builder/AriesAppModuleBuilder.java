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
package org.apache.geronimo.aries.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.ApplicationMetadataManager;
import org.apache.aries.application.Content;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.osgi.BundleUtils;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class AriesAppModuleBuilder implements ModuleBuilder, GBeanLifecycle {
    
    private static final Logger LOG = LoggerFactory.getLogger(AriesAppModuleBuilder.class);
    
    private Kernel kernel;
    private Bundle bundle;

    public AriesAppModuleBuilder(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                                 @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle) {
        this.kernel = kernel;
        this.bundle = bundle;
    }
    
    public void doStart() throws Exception {
    }

    public void doStop() {
    }

    public void doFail() {
        doStop();
    }
   
    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "aries-application", null, null, null, naming, idBuilder);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, earName, naming, idBuilder);
    }
    
    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        
        JarEntry appManifest = moduleFile.getJarEntry("META-INF/APPLICATION.MF");
        
        if (appManifest == null) {
            return null;
        }
        
        ApplicationMetadataManager service = getApplicationMetadataManager();
        if (service == null) {
            return null;
        }
        
        ApplicationMetadata appMetadata = null;
        try {
            InputStream in = moduleFile.getInputStream(appManifest);
            appMetadata = service.parseApplication(in);
        } catch (IOException e) {
            throw new DeploymentException("Failed to parse application metadata", e);
        }
        
        if (appMetadata == null) {
            return null;
        }
        
        LOG.debug("Found Aries Application: {}", appMetadata.getApplicationName());
        
        Environment env = new Environment();
        env.setConfigId(new Artifact("aries", appMetadata.getApplicationSymbolicName(), appMetadata.getApplicationVersion().toString(), "jar"));
                
        AbstractName moduleName = naming.createRootName(env.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
        
        AriesAppModule module = new AriesAppModule(moduleName, env, moduleFile, targetPath, appMetadata);
                                               
        return module;
    }

    private ApplicationMetadataManager getApplicationMetadataManager() {
        ServiceReference ref = 
            bundle.getBundleContext().getServiceReference(ApplicationMetadataManager.class.getName());
        if (ref != null) {
            return (ApplicationMetadataManager) bundle.getBundleContext().getService(ref);
        } else {
            return null;
        }
    }
    
    private String getSymbolicName(Manifest mf) {
        String name = null;
        if (mf != null) {
            name = (String) mf.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
        }
        return name;
    }
    
    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repositories) throws DeploymentException {
        AriesAppModule ariesModule = (AriesAppModule) module;        
        JarFile moduleFile = module.getModuleFile();
        
        /*
         * XXX: This is totally not right but for now allows us to install 
         * simple Aries applications into Geronimo. 
         */
        HashMap<String, String> mapping = new HashMap<String, String>();
        Enumeration<JarEntry> entries = moduleFile.entries();
        while(entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".jar")) {
                try {
                    InputStream in = moduleFile.getInputStream(entry);
                    JarInputStream jarInput = new JarInputStream(in);
                    String name = getSymbolicName(jarInput.getManifest());
                    if (name != null) {
                        mapping.put(name, entry.getName());
                    }
                } catch (IOException e) {
                    LOG.warn("Error getting jar entry {}", entry.getName(), e);
                }
            }
        }

        List<Bundle> installedBundles = new ArrayList<Bundle>();        
        try {
            for (Content content : ariesModule.getApplicationMetadata().getApplicationContents()) {
                String entryName = mapping.get(content.getContentName());
                if (entryName == null) {
                    LOG.warn("Unknown bundle name in application context {}", content.getContentName());
                    continue;
                }
                ZipEntry entry = moduleFile.getEntry(entryName);
                if (entry == null) {
                    // this should not happen
                    throw new DeploymentException("Jar entry not found " + entryName);
                }

                InputStream in = moduleFile.getInputStream(entry);
                Bundle appBundle = bundle.getBundleContext().installBundle(content.getContentName(), in);
                installedBundles.add(appBundle);
            }
            
            for (Bundle installedBundle : installedBundles) {
                if (BundleUtils.canStart(installedBundle)) {
                    installedBundle.start();
                }
            }
        } catch (Exception e) {
            throw new DeploymentException("Failed to install application", e);
        }
        
        module.setEarContext(earContext);
        module.setRootEarContext(earContext);
    }

    public void initContext(EARContext earContext, Module clientModule, Bundle bundle) throws DeploymentException {       
    }

    public void addGBeans(EARContext earContext, Module module, Bundle earBundle, Collection repositories) throws DeploymentException {
    }

    public String getSchemaNamespace() {
        return null;
    }

    public int getPriority() {
        return 50;
    }
    
    public boolean supportsModule(Class moduleType) {
        return AriesAppModule.class.isAssignableFrom(moduleType);
    }
}
