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

package org.apache.geronimo.kernel.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.apache.xbean.osgi.bundle.util.DelegatingBundle;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * A Configuration represents a collection of runnable services that can be
 * loaded into a Geronimo Kernel and brought online. The primary components in
 * a Configuration are a codebase, represented by a collection of URLs that
 * is used to locate classes, and a collection of GBean instances that define
 * its state.
 * <p/>
 * The persistent attributes of the Configuration are:
 * <ul>
 * <li>its unique configId used to identify this specific config</li>
 * <li>the configId of a parent Configuration on which this one is dependent</li>
 * <li>a List<URI> of code locations (which may be absolute or relative to a baseURL)</li>
 * <li>a byte[] holding the state of the GBeans instances in Serialized form</li>
 * </ul>
 * When a configuration is started, it converts the URIs into a set of absolute
 * URLs by resolving them against the specified baseURL (this would typically
 * be the root of the CAR file which contains the configuration) and then
 * constructs a ClassLoader for that codebase. That ClassLoader is then used
 * to de-serialize the persisted GBeans, ensuring the GBeans can be recycled
 * as necessary. Once the GBeans have been restored, they are brought online
 * by registering them with the MBeanServer.
 * <p/>
 * A dependency on the Configuration is created for every GBean it loads. As a
 * result, a startRecursive() operation on the configuration will result in
 * a startRecursive() for all the GBeans it contains. Similarly, if the
 * Configuration is stopped then all of its GBeans will be stopped as well.
 *
 * @version $Rev:385718 $ $Date$
 */

@GBean
public class Configuration implements GBeanLifecycle, ConfigurationParent {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    /**
     * Converts an Artifact to an AbstractName for a configuration.  Does not
     * validate that this is a reasonable or resolved Artifact, or that it
     * corresponds to an actual Configuration.
     * @param configId id for configuration
     * @return abstract name constructed from supplied id
     * @throws InvalidConfigException if the ObjectName could not be constructed
     */
    public static AbstractName getConfigurationAbstractName(Artifact configId) throws InvalidConfigException {
        return new AbstractName(configId, Collections.singletonMap("configurationName", configId.toString()), getConfigurationObjectName(configId));
    }

    public static boolean isConfigurationObjectName(ObjectName name) {
        return name.getDomain().equals("geronimo.config") && name.getKeyPropertyList().size() == 1 && name.getKeyProperty("name") != null;
    }

    public static Artifact getConfigurationID(ObjectName objectName) {
        if (isConfigurationObjectName(objectName)) {
            String name = ObjectName.unquote(objectName.getKeyProperty("name"));
            return Artifact.create(name);
        } else {
            throw new IllegalArgumentException("ObjectName " + objectName + " is not a Configuration name");
        }
    }

    private static ObjectName getConfigurationObjectName(Artifact configId) throws InvalidConfigException {
        try {
            return new ObjectName("geronimo.config:name=" + ObjectName.quote(configId.toString()));
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException("Could not construct object name for configuration", e);
        }
    }

    /**
     * The registered abstractName for this configuraion.
     */
    private final AbstractName abstractName;

    /**
     * Used to resolve dependecies and paths
     */
    private final ConfigurationResolver configurationResolver;

    /**
     * Contains ids of class and service dependencies for parent configurations
     */
    private final DependencyNode dependencyNode;

    /**
     * All service parents depth first
     */
    private final List<Configuration> allServiceParents;

    /**
     * The GBeanData objects by ObjectName
     */
    private final Map<AbstractName, GBeanData> gbeans = new LinkedHashMap<AbstractName, GBeanData>();

    /**
     * Naming system used when generating a name for a new gbean
     */
    private final Naming naming;

    /**
     * Environment, classpath, gbeans and other data for this configuration.
     */
    private final ConfigurationData configurationData;

    /**
     * The nested configurations of this configuration.
     */
    private final List<Configuration> children = new ArrayList<Configuration>();

    /**
     * The enclosing parent of this configuration (e.g. if this is for a war file, an enclosing ear file's configuration);
     */
    private Configuration parent = null;

    /**
     * Manageable Attribute Store containing overrides to this configuration.
     */
    private ManageableAttributeStore attributeStore = null;
    
    private Bundle bundle;

    /**
     * Creates a configuration.
     *
//     * @param classLoaderHolder Classloaders for this configuration
     * @param configurationData the module type, environment and classpath of the configuration
     * @param dependencyNode Class and Service parent ids
     * @param allServiceParents ordered list of transitive closure of service parents for gbean searches
     * @param attributeStore Customization info for gbeans
     * @param configurationResolver (there should be a better way) Where this configuration is actually located in file system
     * @throws InvalidConfigException if this configuration turns out to have a problem.
     */
    public Configuration(
            @ParamAttribute(name = "configurationData") ConfigurationData configurationData,
            @ParamAttribute(name = "dependencyNode") DependencyNode dependencyNode,
            @ParamAttribute(name = "allServiceParents") List<Configuration> allServiceParents,
            @ParamAttribute(name = "attributeStore") ManageableAttributeStore attributeStore,
            @ParamAttribute(name = "configurationResolver") ConfigurationResolver configurationResolver,
            @ParamAttribute(name = "configurationManager") ConfigurationManager configurationManager) throws InvalidConfigException {
        if (configurationData == null) {
            throw new NullPointerException("configurationData is null");
        }

        this.configurationData = configurationData;
        this.naming = configurationData.getNaming();
        this.attributeStore = attributeStore;
        this.dependencyNode = dependencyNode;
        this.allServiceParents = allServiceParents;
        this.configurationResolver = configurationResolver;
        this.abstractName = getConfigurationAbstractName(dependencyNode.getId());
        this.bundle = configurationData.getBundleContext().getBundle();
        
        if (configurationData.isUseEnvironment() && configurationManager != null) {
            try {
                Collection<Bundle> bundles = getBundles(configurationData, configurationResolver, configurationManager);            
                this.bundle = new DelegatingBundle(bundles);
            } catch (Exception e) {
                log.debug("Failed to identify bundle parents for " + configurationData.getId(), e);
            }
        }
        
        try {
            // Deserialize the GBeans in the configurationData
            Collection<GBeanData> gbeans = configurationData.getGBeans(bundle);
            if (attributeStore != null) {
                gbeans = attributeStore.applyOverrides(dependencyNode.getId(), gbeans, bundle);
            }
            for (GBeanData gbeanData : gbeans) {
                this.gbeans.put(gbeanData.getAbstractName(), gbeanData);
            }

        } catch (RuntimeException e) {
            shutdown();
            throw e;
        } catch (Error e) {
            shutdown();
            throw e;
        } catch (InvalidConfigException e) {
            shutdown();
            throw e;
        }
    }

    private Collection<Bundle> getBundles(ConfigurationData configurationData,
                                          ConfigurationResolver configurationResolver,
                                          ConfigurationManager configurationManager) 
        throws MissingDependencyException, InvalidConfigException {
        
        Set<Bundle> bundles = new LinkedHashSet<Bundle>();
        bundles.add(configurationData.getBundleContext().getBundle());
      
        getAllBundles(configurationData, configurationManager, new HashSet<Artifact>(), bundles);
                
        return bundles;
    }

    /*
     * Gets all dependent non-configuration bundles.
     */
    private void getAllBundles(ConfigurationData configurationData, ConfigurationManager configurationManager, Set<Artifact> artifacts, Collection<Bundle> bundles) throws MissingDependencyException, InvalidConfigException {
        LinkedHashSet<Artifact> parents = configurationManager.resolveParentIds(configurationData);
        for (Artifact parent : parents) {
            if (artifacts.contains(parent)) {
                continue;
            }
            artifacts.add(parent);
            boolean isConfiguration = configurationManager.isConfiguration(parent);
            if (isConfiguration) {
                Configuration configuration = configurationManager.getConfiguration(parent);
                getAllBundles(configuration.getConfigurationData(), configurationManager, artifacts, bundles);
            } else {
                String location = getBundleLocation(configurationResolver, parent);
                Bundle bundle = getBundleByLocation(configurationData.getBundleContext(), location);
                if (bundle != null) {
                    bundles.add(bundle);
                }
            }
        }
    }
    
    private static String getBundleLocation(ConfigurationResolver configurationResolver, Artifact configurationId) {
        if (System.getProperty("geronimo.build.car") == null) {
            return "mvn:" + configurationId.getGroupId() + "/" + configurationId.getArtifactId() + "/" + configurationId.getVersion() + ("jar".equals(configurationId.getType())?  "": "/" + configurationId.getType());
        }
        if (configurationResolver == null) {
            throw new NullPointerException("ConfigurationResolver is null");
        }
        try {
            File file = configurationResolver.resolve(configurationId);
            return "reference:file://" + file.getAbsolutePath();
        } catch (MissingDependencyException e) {
            return null;
        }
    }
    
    private static Bundle getBundleByLocation(BundleContext bundleContext, String location) {
        for (Bundle bundle: bundleContext.getBundles()) {
            if (location.equals(bundle.getLocation())) {
               return bundle;
            }
        }
        return null;
    }
    
    /**
     * Add a contained configuration, such as for a war inside an ear
     * @param child contained configuration
     */
    void addChild(Configuration child) {
        children.add(child);
        child.parent = this;
    }

    /**
     * Return the unique Id
     *
     * @return the unique Id
     */
    public Artifact getId() {
        return dependencyNode.getId();
    }

    /**
     * Gets the unique name of this configuration within the kernel.
     *
     * @return the unique name of this configuration
     */
    public String getObjectName() {
        try {
            return getConfigurationObjectName(getId()).getCanonicalName();
        } catch (InvalidConfigException e) {
            throw new AssertionError(e);
        }
    }

    public AbstractName getAbstractName() {
        return abstractName;
    }

//    public ClassLoaderHolder getClassLoaderHolder() {
//        return classLoaderHolder;
//    }

    /**
     * Gets the parent configurations used for class loading.
     * @return the parents of this configuration used for class loading
     */
//    public List<Configuration> getClassParents() {
//        return classParents;
//    }

    /**
     * Gets the parent configurations used for service resolution.
     *
     * @return the parents of this configuration used for service resolution
     */
//    public List<Configuration> getServiceParents() {
//        return serviceParents;
//    }
    public DependencyNode getDependencyNode() {
        return dependencyNode;
    }

    /**
     * Gets the artifact dependencies of this configuration.
     * @return the artifact dependencies of this configuration
     */
//    public LinkedHashSet<Artifact> getDependencies() {
//        return dependencies;
//    }

    /**
     * Gets the declaration of the environment in which this configuration runs.
     *
     * @return the environment of this configuration
     */
    public Environment getEnvironment() {
        return configurationData.getEnvironment();
    }

    /**
     * This is used by the configuration manager to restart an existing configuation.
     * Do not modify the configuration data.
     *
     * @return the configuration data for this configuration; do not modify
     */
    ConfigurationData getConfigurationData() {
        return configurationData;
    }

    public File getConfigurationDir() {
        return configurationData.getConfigurationDir();
    }

    /**
     * Provide a way to locate where this configuration is for web apps and persistence units
     * @return the ConfigurationResolver for this configuration
     */
    public ConfigurationResolver getConfigurationResolver() {
        return configurationResolver;
    }

    /**
     * Gets the type of the configuration (WAR, RAR et cetera)
     *
     * @return Type of the configuration.
     */
    public ConfigurationModuleType getModuleType() {
        return configurationData.getModuleType();
    }

    /**
     * Gets the time at which this configuration was created (or deployed).
     *
     * @return the time at which this configuration was created (or deployed)
     */
    public long getCreated() {
        return configurationData.getCreated();
    }

    /**
     * Gets the class loader for this configuration.
     *
     * @return the bundle for this configuration
     */
    public Bundle getBundle() {
        return bundle;
    }

    public BundleContext getBundleContext() {
        return bundle.getBundleContext();
    }

    /**
     * Gets the nested configurations of this configuration.  That is, the
     * configurations within this one as a WAR can be within an EAR; not
     * including wholly separate configurations that just depend on this
     * one as a parent.
     *
     * @return the nested configuration of this configuration
     */
    public List<Configuration> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Gets the configurations owned by this configuration.  This is only used for cascade-uninstall.
     *
     * @return the configurations owned by this configuration
     */
    public Set<Artifact> getOwnedConfigurations() {
        return configurationData.getOwnedConfigurations();
    }

    /**
     * Gets an unmodifiable collection of the GBeanDatas for the GBeans in this configuration.
     *
     * @return the GBeans in this configuration
     */
    public Map<AbstractName, GBeanData> getGBeans() {
        return Collections.unmodifiableMap(gbeans);
    }

    /**
     * Determines of this configuration constains the specified GBean.
     *
     * @param gbean the name of the GBean
     * @return true if this configuration contains the specified GBean; false otherwise
     */
    public synchronized boolean containsGBean(AbstractName gbean) {
        return gbeans.containsKey(gbean);
    }

    /**
     * Gets the enclosing configuration of this one (e.g. the EAR for a WAR),
     * or null if it has none.
     *
     * @return enclosing configuration, if any
     */
    public Configuration getEnclosingConfiguration() {
        return parent;
    }

    /**
     * Gets the manageable attribute store for this configuration.
     * This is used in the configuration manager to apply overrides
     *
     * @return customization source for gbeans
     */
    ManageableAttributeStore getManageableAttributeStore() {
        return attributeStore;
    }

    public synchronized AbstractName addGBean(String name, GBeanData gbean) throws GBeanAlreadyExistsException {
        AbstractName abstractName = gbean.getAbstractName();
        if (abstractName != null) {
            throw new IllegalArgumentException("gbean already has an abstract name: " + abstractName);
        }

        String j2eeType = gbean.getGBeanInfo().getJ2eeType();
        if (j2eeType == null) j2eeType = "GBean";
        abstractName = naming.createRootName(getId(), name, j2eeType);
        gbean.setAbstractName(abstractName);

        if (gbeans.containsKey(abstractName)) {
            throw new GBeanAlreadyExistsException(gbean.getAbstractName().toString());
        }
        gbeans.put(abstractName, gbean);
        return abstractName;
    }

    public synchronized void addGBean(GBeanData gbean) throws GBeanAlreadyExistsException {
        if (gbeans.containsKey(gbean.getAbstractName())) {
            throw new GBeanAlreadyExistsException(gbean.getAbstractName().toString());
        }
        gbeans.put(gbean.getAbstractName(), gbean);
    }

    public synchronized void removeGBean(AbstractName name) throws GBeanNotFoundException {
        if (!gbeans.containsKey(name)) {
            throw new GBeanNotFoundException(name);
        }
        gbeans.remove(name);
    }

    public AbstractName findGBean(AbstractNameQuery pattern) throws GBeanNotFoundException {
        if (pattern == null) throw new NullPointerException("pattern is null");
        return findGBean(Collections.singleton(pattern));
    }

    public GBeanData findGBeanData(AbstractNameQuery pattern) throws GBeanNotFoundException {
        if (pattern == null) throw new NullPointerException("pattern is null");
        return findGBeanData(Collections.singleton(pattern));
    }

    public AbstractName findGBean(ReferencePatterns referencePatterns) throws GBeanNotFoundException {
        if (referencePatterns == null) throw new NullPointerException("referencePatterns is null");
        if (referencePatterns.isResolved()) {
            return referencePatterns.getAbstractName();
        }

        // check the local config
        Set<AbstractNameQuery> patterns = referencePatterns.getPatterns();
        return findGBean(patterns);
    }

    public AbstractName findGBean(Set<AbstractNameQuery> patterns) throws GBeanNotFoundException {
        if (patterns == null) throw new NullPointerException("patterns is null");
        return findGBeanData(patterns).getAbstractName();
    }

    public GBeanData findGBeanData(Set<AbstractNameQuery> patterns) throws GBeanNotFoundException {
        if (patterns == null) throw new NullPointerException("patterns is null");
        Set<GBeanData> result = findGBeanDatas(this, patterns);
        if (result.size() > 1) {
            throw new GBeanNotFoundException("More than one match to referencePatterns in local configuration", patterns, mapToNames(result));
        } else if (result.size() == 1) {
            return result.iterator().next();
        }

        // search all parents
        for (Configuration configuration : allServiceParents) {
            result.addAll(findGBeanDatas(configuration, patterns));

        }
        // if we already found a match we have an ambiguous query
        if (result.size() > 1) {
            List<AbstractName> names = new ArrayList<AbstractName>(result.size());
            for (GBeanData gBeanData : result) {
                names.add(gBeanData.getAbstractName());
            }
            throw new GBeanNotFoundException("More than one match to referencePatterns in parent configurations: " + names.toString(), patterns, mapToNames(result));
        }

        if (result.isEmpty()) {
            throw new GBeanNotFoundException("No matches for referencePatterns", patterns, null);
        }

        return result.iterator().next();
    }

    private Set<AbstractName> mapToNames(Set<GBeanData> datas) {
        Set<AbstractName> names = new HashSet<AbstractName>(datas.size());
        for (GBeanData gBeanData : datas) {
            names.add(gBeanData.getAbstractName());
        }
        return names;
    }

    public LinkedHashSet<AbstractName> findGBeans(AbstractNameQuery pattern) {
        if (pattern == null) throw new NullPointerException("pattern is null");
        return findGBeans(Collections.singleton(pattern));
    }

    public LinkedHashSet<AbstractName> findGBeans(ReferencePatterns referencePatterns) {
        if (referencePatterns == null) throw new NullPointerException("referencePatterns is null");
        if (referencePatterns.getAbstractName() != null) {
            // this pattern is already resolved
            LinkedHashSet<AbstractName> result = new LinkedHashSet<AbstractName>();
            result.add(referencePatterns.getAbstractName());
            return result;
        }

        // check the local config
        Set<AbstractNameQuery> patterns = referencePatterns.getPatterns();
        return findGBeans(patterns);
    }

    public LinkedHashSet<AbstractName> findGBeans(Set<AbstractNameQuery> patterns) {
        if (patterns == null) throw new NullPointerException("patterns is null");
        LinkedHashSet<GBeanData> datas = findGBeanDatas(patterns);
        LinkedHashSet<AbstractName> result = new LinkedHashSet<AbstractName>(datas.size());
        for (GBeanData gBeanData : datas) {
            result.add(gBeanData.getAbstractName());
        }

        return result;
    }

    public LinkedHashSet<GBeanData> findGBeanDatas(Set<AbstractNameQuery> patterns) {
        if (patterns == null) throw new NullPointerException("patterns is null");
        LinkedHashSet<GBeanData> datas = findGBeanDatas(this, patterns);

        // search all parents
        for (Configuration configuration : allServiceParents) {
            Set<GBeanData> match = findGBeanDatas(configuration, patterns);
            datas.addAll(match);
        }
        return datas;
    }

    /**
     * Find the gbeanDatas matching the patterns in this configuration only, ignoring parents.
     *
     * @param configuration configuration to look in
     * @param patterns      patterns to look for
     * @return set of gbeandatas matching one of the patterns from this configuration only, not including parents.
     */
    public LinkedHashSet<GBeanData> findGBeanDatas(Configuration configuration, Set<AbstractNameQuery> patterns) {
        LinkedHashSet<GBeanData> result = new LinkedHashSet<GBeanData>();

        Set<Map.Entry<AbstractName, GBeanData>> gbeanNames = configuration.getGBeans().entrySet();
        for (AbstractNameQuery abstractNameQuery : patterns) {
            Artifact queryArtifact = abstractNameQuery.getArtifact();

            // Does this query apply to this configuration
            if (queryArtifact == null || queryArtifact.matches(configuration.getId())) {

                // Search the GBeans
                for (Map.Entry<AbstractName, GBeanData> entry : gbeanNames) {
                    AbstractName abstractName = entry.getKey();
                    GBeanData gbeanData = entry.getValue();
                    if (abstractNameQuery.matches(abstractName, gbeanData.getGBeanInfo().getInterfaces())) {
                        result.add(gbeanData);
                    }
                }
            }
        }
        return result;
    }

    public void doStart() throws Exception {
        log.debug("Started configuration {}", getId());
    }

    public synchronized void doStop() throws Exception {
        log.debug("Stopping configuration {}", getId());
        shutdown();

    }

    public void doFail() {
        log.debug("Failed configuration {}", getId());
        shutdown();
    }

    private void shutdown() {
        for (Configuration configuration : children) {
            configuration.shutdown();
        }

        // clear references to GBeanDatas
        gbeans.clear();

//        classLoaderHolder.destroy();
    }

}
