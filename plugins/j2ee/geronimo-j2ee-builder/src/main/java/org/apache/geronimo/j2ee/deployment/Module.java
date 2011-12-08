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
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeployableJarFile;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xbean.finder.AbstractFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class Module<T, U> {
	private static final Logger log = LoggerFactory.getLogger(Module.class);
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
    private AbstractFinder classFinder;

    private final Map<AbstractName, GBeanData> gbeans = new LinkedHashMap<AbstractName, GBeanData>();
    protected final Map sharedContext = new HashMap();
    protected final LinkedHashSet<Module<?, ?>> modules;
    protected final LinkedHashSet<String> moduleLocations;
    protected final LinkedHashSet<String> classpath;

    private final Map<JndiKey, Map<String, Object>> jndiContext;
    private final Module<?, ?> parentModule;

    /*
     * The Modules should be sorted with following sequence:
     *
     * 1, ConnectorModule
     * 2, EJBModule
     * 3, WebModule
     * 4, AppClientModule
     */
    protected int priority = 5;

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
                     Map<JndiKey, Map<String, Object>> jndiContext,
                     Module<?, ?> parentModule) {
        this(standAlone, moduleName, name, environment, new DeployableJarFile(moduleFile),
                targetPath, specDD, vendorDD, originalSpecDD, namespace, jndiContext, parentModule);
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
                     Map<JndiKey, Map<String, Object>> jndiContext, Module<?, ?> parentModule) {
        assert targetPath != null : "targetPath is null";
        assert moduleName != null : "moduleName is null";

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
        this.moduleLocations = new LinkedHashSet<String>();
        this.modules = new LinkedHashSet<Module<?, ?>>();
        this.classpath = new LinkedHashSet<String>();
        if (jndiContext != null) {
            this.jndiContext = jndiContext;
        } else {
            this.jndiContext = assure(new HashMap<JndiKey, Map<String, Object>>());
        }
        this.parentModule = parentModule;
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
    	URI resultURI = null;
    	try {
    		resultURI = targetPathURI.resolve(path);
    	} catch (Exception e) {
    		log.warn("Exception=" + e + "; Cause=" + e.getCause()); 
    		if (e instanceof java.lang.IllegalArgumentException) {    			
    			try {
    			URI substr = new URI(null, path, null);
    			resultURI = targetPathURI.resolve(substr);
    			} catch (Exception ex) {
    				throw new RuntimeException("Exception=" + ex + "; Cause=" + ex.getCause());
    			}
    		}
    	}    	
        return resultURI;
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

    public void addGBean(GBeanData gbean) throws GBeanAlreadyExistsException {
        if (gbeans.containsKey(gbean.getAbstractName())) {
            throw new GBeanAlreadyExistsException(gbean.getAbstractName().toString());
        }
        gbeans.put(gbean.getAbstractName(), gbean);
    }

    public void flushGBeansToContext() throws GBeanAlreadyExistsException {
        for (GBeanData data: gbeans.values()) {
            earContext.addGBean(data);
        }
        gbeans.clear();
    }

    public int hashCode() {
        return moduleURI.hashCode();
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

    public AbstractFinder getClassFinder() {
        return classFinder;
    }

    public void setClassFinder(AbstractFinder classFinder) {
        this.classFinder = classFinder;
    }

    public Artifact[] getConfigId() {
        if (earContext == null) {
            throw new NullPointerException("No ear context set");
        }
        if (rootEarContext == null || rootEarContext == earContext || rootEarContext.getConfigID().equals(earContext.getConfigID())) {
            return new Artifact[]{earContext.getConfigID()};
        }
        return new Artifact[]{rootEarContext.getConfigID(), earContext.getConfigID()};
    }

    /**
     * Given a path in the ear module, return something that will resolve to that location against the eventual configuration
     * base uri.  Currently for all modules except wars that is the original path.  If we create separate configurations for
     * ejb or rar modules, those Module subclasses will need to reimplement this method.
     * <p/>
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
        if (rootEarContext != null && rootEarContext != earContext && !(earContext instanceof FragmentContext)) {
            ConfigurationData moduleConfigurationData = earContext.getConfigurationData();
            rootEarContext.addChildConfiguration(getTargetPath(), moduleConfigurationData);
        }
    }

    public LinkedHashSet<Module<?, ?>> getModules() {
        return modules;
    }

    public LinkedHashSet<String> getModuleLocations() {
        return moduleLocations;
    }

    public LinkedHashSet<String> getClassPath() {
        return classpath;
    }

    public void accumulateClassPath() {
        earContext.getBundleClassPath().addAll(this.classpath);
        for (Module module: modules) {
                module.accumulateClassPath();
        }
    }

    public Map<JndiKey, Map<String, Object>> getJndiContext() {
        return jndiContext;
    }

    public Map<String, Object> getJndiScope(JndiKey scope) {
        return jndiContext.get(scope);
    }

    public final static EnumSet<JndiScope> APP = EnumSet.of(JndiScope.global, JndiScope.app);
    public final static EnumSet<JndiScope> MODULE = EnumSet.of(JndiScope.global, JndiScope.app, JndiScope.module);

    public Module<?, ?> getParentModule() {
        return parentModule;
    }

    public static Map<JndiKey, Map<String, Object>> share(EnumSet<JndiScope> scopes, Map<JndiKey, Map<String, Object>> jndiContext) {
        Map<JndiKey, Map<String, Object>> newContext = new HashMap<JndiKey, Map<String, Object>>();
        if (jndiContext != null) {
            for (JndiScope scope : scopes) {
                Map<String, Object> scopedContext = jndiContext.get(scope);
                if (scopedContext != null) {
                    newContext.put(scope, scopedContext);
                }
            }
        }
        return assure(newContext);
    }

    public static Map<JndiKey, Map<String, Object>> assure(Map<JndiKey, Map<String, Object>> jndiContext) {
        for (JndiScope scope : JndiScope.values()) {
            if (jndiContext.get(scope) == null) {
                jndiContext.put(scope, new HashMap<String, Object>());
            }
        }
        return jndiContext;
    }


    public static class ModulePriorityComparator implements Comparator<Module<?,?>> {

        public int compare(Module<?,?> m1, Module<?,?> m2) {
            return m1.priority - m2.priority;
        }
    }

    public static class AppClientModuleLastComparator implements Comparator<Module<?, ?>> {

        public int compare(Module<?, ?> m1, Module<?, ?> m2) {
            boolean m1AppClientModule = m1 instanceof AppClientModule;
            boolean m2AppClientModule = m2 instanceof AppClientModule;
            if(m1AppClientModule && m2AppClientModule) {
                return 0;
            } else if(m1AppClientModule) {
                return 1;
            } else if(m2AppClientModule) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
