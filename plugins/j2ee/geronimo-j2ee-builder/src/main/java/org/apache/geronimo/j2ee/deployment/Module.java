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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeployableJarFile;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xbean.finder.ClassFinder;

/**
 * @version $Rev$ $Date$
 */
public abstract class Module<T, U> {
    private final boolean standAlone;

    private final AbstractName moduleName;
    private final String name;
    private final Environment environment;
    private final URI moduleURI;
    private final Deployable deployable;
    private final String targetPath;
    private final URI targetPathURI;
    private final U vendorDD;
    private final String namespace;

    private EARContext earContext;
    private EARContext rootEarContext;
    private T specDD;
    private String originalSpecDD;
    private AnnotatedApp annotatedApp;
    private ClassFinder classFinder;

    protected final Map sharedContext = new HashMap();

    protected Module(boolean standAlone, 
                     AbstractName moduleName, 
                     String name, 
                     Environment environment, 
                     JarFile moduleFile, 
                     String targetPath, 
                     T specDD, 
                     U vendorDD, 
                     String originalSpecDD, 
                     String namespace, 
                     AnnotatedApp annotatedApp) {
        this(standAlone, moduleName, name, environment, new DeployableJarFile(moduleFile), 
             targetPath, specDD, vendorDD, originalSpecDD, namespace, annotatedApp);
    }

    protected Module(boolean standAlone, 
                     AbstractName moduleName,
                     String name, 
                     Environment environment, 
                     Deployable deployable, 
                     String targetPath, 
                     T specDD, 
                     U vendorDD, 
                     String originalSpecDD, 
                     String namespace, 
                     AnnotatedApp annotatedApp) {
        assert targetPath != null: "targetPath is null";
        assert moduleName != null: "moduleName is null";

        this.standAlone = standAlone;
        this.moduleName = moduleName;
        this.environment = environment;
        this.deployable = deployable;
        this.targetPath = targetPath;
        this.specDD = specDD;
        this.vendorDD = vendorDD;
        this.originalSpecDD = originalSpecDD;
        this.namespace = namespace;
        
        if (standAlone) {
            this.name = (name == null) ? environment.getConfigId().toString() : name;
            this.moduleURI = URI.create("");
        } else {
            this.name = (name == null) ? targetPath : name;
            this.moduleURI = URI.create(targetPath);
        }

        targetPathURI = URI.create(targetPath + "/");
        this.annotatedApp = annotatedApp;
    }

    public abstract ConfigurationModuleType getType();

    public String getName() {
        return name;
    }

    public boolean isStandAlone() {
        return standAlone;
    }

    public AbstractName getModuleName() {
        return moduleName;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public URI getModuleURI() {
        return moduleURI;
    }

    public JarFile getModuleFile() {
        if (deployable instanceof DeployableJarFile) {
            return ((DeployableJarFile) deployable).getJarFile();
        } else {
            throw new RuntimeException("getModuleFile() is not supported on Bundle-based deployment");
        }
    }

    public Deployable getDeployable() {
        return deployable;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public URI getTargetPathURI() {
        return targetPathURI;
    }

    public URI resolve(String path) {
        return targetPathURI.resolve(path);
    }

    public URI resolve(URI path) {
        return targetPathURI.resolve(path);
    }

    public T getSpecDD() {
        return specDD;
    }

    public U getVendorDD() {
        return vendorDD;
    }

    public String getOriginalSpecDD() {
        return originalSpecDD;
    }

    public String getNamespace() {
        return namespace;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Module) {
            Module module = (Module) obj;
            return name.equals(module.name);
        }
        return false;
    }

    public void close() {
        deployable.close();
    }

    public EARContext getEarContext() {
        return earContext;
    }

    public void setEarContext(EARContext earContext) {
        this.earContext = earContext;
    }

    public EARContext getRootEarContext() {
        return rootEarContext;
    }

    public void setRootEarContext(EARContext rootEarContext) {
        this.rootEarContext = rootEarContext;
    }

    public Map getSharedContext() {
        return sharedContext;
    }

    public void setSpecDD(T specDD) {
        this.specDD = specDD;
    }

    public void setOriginalSpecDD(String originalSpecDD) {
        this.originalSpecDD = originalSpecDD;
    }

    public AnnotatedApp getAnnotatedApp() {
        return annotatedApp;
    }

    public void setAnnotatedApp(AnnotatedApp annotatedApp) {
        this.annotatedApp = annotatedApp;
    }

    public ClassFinder getClassFinder() {
        return classFinder;
    }

    public void setClassFinder(ClassFinder classFinder) {
        this.classFinder = classFinder;
    }

    public Artifact[] getConfigId() {
        if (earContext == null) {
            throw new NullPointerException("No ear context set");
        }
        if (rootEarContext == null || rootEarContext == earContext || rootEarContext.getConfigID().equals(earContext.getConfigID())) {
            return new Artifact[] {earContext.getConfigID()};
        }
        return new Artifact[] {rootEarContext.getConfigID(), earContext.getConfigID()};
    }

    /**
     * Given a path in the ear module, return something that will resolve to that location against the eventual configuration
     * base uri.  Currently for all modules except wars that is the original path.  If we create separate configurations for
     * ejb or rar modules, those Module subclasses will need to reimplement this method.
     *
     * Example:  if a war is myweb.war, and you pass in myweb.war/WEB-INF/lib/foo.jar, you get WEB-INF/lib/foo.jar
     * if you pass in myFoo.jar, you get ../myFoo.jar
     *
     * @param path a path in the ear config, relative to the ear root.
     * @return a path to the same location, but relative to the configuration this module represents' base uri.
     */
    public String getRelativePath(String path) {
        return path;
    }

    public void addAsChildConfiguration() throws DeploymentException {
        if (rootEarContext != null && rootEarContext != earContext) {
            ConfigurationData moduleConfigurationData = earContext.getConfigurationData();
            rootEarContext.addChildConfiguration(getTargetPath(), moduleConfigurationData);
        }
    }

}
