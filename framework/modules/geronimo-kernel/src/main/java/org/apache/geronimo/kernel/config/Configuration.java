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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.classloader.JarFileClassLoader;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;

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
public class Configuration implements GBeanLifecycle, ConfigurationParent
{
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    /**
     * Converts an Artifact to an AbstractName for a configuration.  Does not
     * validate that this is a reasonable or resolved Artifact, or that it
     * corresponds to an actual Configuration.
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
     * The artifact id for this configuration.
     */
    private final Artifact id;

    /**
     * The registered abstractName for this configuraion.
     */
    private final AbstractName abstractName;

    /**
     * Defines the environment requred for this configuration.
     */
    private final Environment environment;

    /**
     * Used to resolve dependecies and paths
     */
    private final ConfigurationResolver configurationResolver;

    /**
     * Parent configurations used for class loader.
     */
    private final List<Configuration> classParents = new ArrayList<Configuration>();

    /**
     * Parent configuations used for service resolution.
     */
    private final List<Configuration> serviceParents = new ArrayList<Configuration>();

    /**
     * All service parents depth first
     */
    private final List<Configuration> allServiceParents = new ArrayList<Configuration>();

    /**
     * Artifacts added to the class loader (non-configuation artifacts).
     */
    private final LinkedHashSet<Artifact> dependencies = new LinkedHashSet<Artifact>();

    /**
     * The GBeanData objects by ObjectName
     */
    private final Map<AbstractName, GBeanData> gbeans = new LinkedHashMap<AbstractName, GBeanData>();

    /**
     * The classloader used to load the child GBeans contained in this configuration.
     */
    private final MultiParentClassLoader configurationClassLoader;

    /**
     * The relative class path (URI) of this configuation.
     */
    private final LinkedHashSet<String> classPath;

    /**
     * Naming system used when generating a name for a new gbean
     */
    private final Naming naming;

    /**
     * Environment, classpath, gbeans and other data for this configuration.
     */
    private ConfigurationData configurationData;

    /**
     * The nested configurations of this configuration.
     */
    List<Configuration> children = new ArrayList<Configuration>();

    /**
     * The parent of this configuration;
     */
    private Configuration parent = null;

    /**
     * Only used to allow declaration as a reference.
     */
    public Configuration() {
        id = null;
        abstractName = null;
        environment = null;
        classPath = null;
        configurationResolver = null;
        configurationClassLoader = null;
        naming = null;
    }

    /**
     * Creates a configuration.
     * @param parents parents of this configuation (not ordered)
     * @param configurationData the module type, environment and classpath of the configuration
     * @param configurationResolver used to resolve dependecies and paths
     */
    public Configuration(Collection<Configuration> parents,
            ConfigurationData configurationData,
            ConfigurationResolver configurationResolver,
            ManageableAttributeStore attributeStore) throws MissingDependencyException, MalformedURLException, NoSuchConfigException, InvalidConfigException {
        if (parents == null) parents = Collections.EMPTY_SET;
        if (configurationData == null) throw new NullPointerException("configurationData is null");
        if (configurationResolver == null) throw new NullPointerException("configurationResolver is null");

        this.configurationData = configurationData;
        this.environment = configurationData.getEnvironment();
        this.configurationResolver = configurationResolver;
        this.classPath = new LinkedHashSet<String>(configurationData.getClassPath());
        this.naming = configurationData.getNaming();

        this.id = environment.getConfigId();
        abstractName = getConfigurationAbstractName(id);

        //
        // Transitively resolve all the dependencies in the environment
        //
        List<Dependency> transitiveDependencies = configurationResolver.resolveTransitiveDependencies(parents, environment.getDependencies());

        //
        // Process transtive dependencies splitting it into classParents, serviceParents and artifactDependencies
        //
        Map<Artifact, Configuration> parentsById = new HashMap<Artifact, Configuration>();
        for (Configuration configuration : parents) {
            Artifact id = configuration.getId();
            parentsById.put(id, configuration);
        }

        for (Dependency dependency : transitiveDependencies) {
            Artifact artifact = dependency.getArtifact();
            if (parentsById.containsKey(artifact)) {
                Configuration parent = parentsById.get(artifact);
                if (dependency.getImportType() == ImportType.CLASSES || dependency.getImportType() == ImportType.ALL) {
                    classParents.add(parent);
                }
                if (dependency.getImportType() == ImportType.SERVICES || dependency.getImportType() == ImportType.ALL) {
                    serviceParents.add(parent);
                }
            } else if (dependency.getImportType() == ImportType.SERVICES) {
                throw new IllegalStateException("Could not find parent " + artifact + " in the parents collection");
            } else {
                dependencies.add(artifact);
            }
        }

        try {
            //
            // Build the configuration class loader
            //
            configurationClassLoader = createConfigurationClasssLoader(parents, environment, classPath);

            //
            // Get all service parents in depth first order
            //

            addDepthFirstServiceParents(this, allServiceParents, new HashSet<Artifact>());

            //
            // Deserialize the GBeans in the configurationData
            //
            Collection<GBeanData> gbeans = configurationData.getGBeans(configurationClassLoader);
            if (attributeStore != null) {
                gbeans = attributeStore.applyOverrides(id, gbeans, configurationClassLoader);
            }
            for (GBeanData gbeanData : gbeans) {
                this.gbeans.put(gbeanData.getAbstractName(), gbeanData);
            }

            //
            // Create child configurations
            //
            LinkedHashSet<Configuration> childParents = new LinkedHashSet<Configuration>(parents);
            childParents.add(this);
            for (Iterator iterator = configurationData.getChildConfigurations().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String moduleName = (String) entry.getKey();
                ConfigurationData childConfigurationData = (ConfigurationData) entry.getValue();
                Configuration childConfiguration = new Configuration(childParents, childConfigurationData, configurationResolver.createChildResolver(moduleName), attributeStore);
                childConfiguration.parent = this;
                children.add(childConfiguration);
            }
        } catch (RuntimeException e) {
            shutdown();
            throw e;
        } catch (Error e) {
            shutdown();
            throw e;
        } catch (MissingDependencyException e) {
            shutdown();
            throw e;
        } catch (MalformedURLException e) {
            shutdown();
            throw e;
        } catch (NoSuchConfigException e) {
            shutdown();
            throw e;
        } catch (InvalidConfigException e) {
            shutdown();
            throw e;
        }
    }

    private MultiParentClassLoader createConfigurationClasssLoader(Collection<Configuration> parents, Environment environment, LinkedHashSet<String> classPath) throws MalformedURLException, MissingDependencyException, NoSuchConfigException {
        // create the URL list
        URL[] urls = buildClassPath(classPath);

        // parents
        ClassLoader[] parentClassLoaders;
        if (parents.size() == 0 && classParents.size() == 0) {
            // no explicit parent set, so use the class loader of this class as
            // the parent... this class should be in the root geronimo classloader,
            // which is normally the system class loader but not always, so be safe
            parentClassLoaders = new ClassLoader[] {getClass().getClassLoader()};
        } else {
            parentClassLoaders = new ClassLoader[classParents.size()];
            for (ListIterator iterator = classParents.listIterator(); iterator.hasNext();) {
                Configuration configuration = (Configuration) iterator.next();
                parentClassLoaders[iterator.previousIndex()] = configuration.getConfigurationClassLoader();
            }
        }

        // hidden classes
        Set<String> hiddenClassesSet = environment.getHiddenClasses();
        String[] hiddenClasses = hiddenClassesSet.toArray(new String[hiddenClassesSet.size()]);

        // we need to propagate the non-overrideable classes from parents
        LinkedHashSet<String> nonOverridableSet = new LinkedHashSet<String>();
        for (Configuration parent : classParents) {

            Environment parentEnvironment = parent.getEnvironment();
            nonOverridableSet.addAll(parentEnvironment.getNonOverrideableClasses());
        }
        String[] nonOverridableClasses = nonOverridableSet.toArray(new String[nonOverridableSet.size()]);

        if (log.isDebugEnabled()) {
            StringBuffer buf = new StringBuffer("ClassLoader structure for configuration ").append(id).append("\n");
            buf.append("Parent configurations:\n");
            for (Configuration configuration : classParents) {
                buf.append("     ").append(configuration.getId()).append("\n");
            }
            buf.append("ClassPath:\n");
            for (URL url : urls) {
                buf.append("     ").append(url).append("\n");
            }
            log.debug(buf.toString());
        }

        // The JarFileClassLoader was created to address a locking problem seen only on Windows platforms.
        // It carries with it a slight performance penalty that needs to be addressed.  Rather than make
        // *nix OSes carry this burden we'll engage the JarFileClassLoader for Windows or if the user 
        // specifically requests it.  We'll look more at this issue in the future.
        boolean useJarFileClassLoader = false;
        if (System.getProperty("Xorg.apache.geronimo.JarFileClassLoader") == null) {
            useJarFileClassLoader = System.getProperty("os.name").startsWith("Windows");
        } else {
            useJarFileClassLoader = Boolean.getBoolean("Xorg.apache.geronimo.JarFileClassLoader");
        }
        if (useJarFileClassLoader) {
            return new JarFileClassLoader(environment.getConfigId(),
                    urls,
                    parentClassLoaders,
                    environment.isInverseClassLoading(),
                    hiddenClasses,
                    nonOverridableClasses);
        } else {
            return new MultiParentClassLoader(environment.getConfigId(),
                    urls,
                    parentClassLoaders,
                    environment.isInverseClassLoading(),
                    hiddenClasses,
                    nonOverridableClasses);
        }
    }

    private void addDepthFirstServiceParents(Configuration configuration, List<Configuration> ancestors, Set<Artifact> ids) {
        if (!ids.contains(configuration.getId())) {
            ancestors.add(configuration);
            ids.add(configuration.getId());
            for (Configuration parent : configuration.getServiceParents()) {
                addDepthFirstServiceParents(parent, ancestors, ids);
            }
        }
    }

    private URL[] buildClassPath(LinkedHashSet<String> classPath) throws MalformedURLException, MissingDependencyException, NoSuchConfigException {
        List<URL> urls = new ArrayList<URL>();
        for (Artifact artifact : dependencies) {
            File file = configurationResolver.resolve(artifact);
            urls.add(file.toURL());
        }
        if (classPath != null) {
            for (String pattern : classPath) {
                Set<URL> matches = configurationResolver.resolve(pattern);
                for (URL url : matches) {
                    urls.add(url);
                }
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

    /**
     * Return the unique Id
     * @return the unique Id
     */
    public Artifact getId() {
        return id;
    }

    /**
     * Gets the unique name of this configuration within the kernel.
     * @return the unique name of this configuration
     */
    public String getObjectName() {
        try {
            return getConfigurationObjectName(id).getCanonicalName();
        } catch (InvalidConfigException e) {
            throw new AssertionError(e);
        }
    }

    public AbstractName getAbstractName() {
        return abstractName;
    }

    /**
     * Gets the parent configurations used for class loading.
     * @return the parents of this configuration used for class loading
     */
    public List<Configuration> getClassParents() {
        return classParents;
    }

    /**
     * Gets the parent configurations used for service resolution.
     * @return the parents of this configuration used for service resolution
     */
    public List<Configuration> getServiceParents() {
        return serviceParents;
    }

    /**
     * Gets the artifact dependencies of this configuration.
     * @return the artifact dependencies of this configuration
     */
    public LinkedHashSet<Artifact> getDependencies() {
        return dependencies;
    }

    /**
     * Gets the declaration of the environment in which this configuration runs.
     * @return the environment of this configuration
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * This is used by the configuration manager to restart an existing configuation.
     * Do not modify the configuration data.
     * @return the configuration data for this configuration; do not modify
     */
    ConfigurationData getConfigurationData() {
        return configurationData;
    }

    public File getConfigurationDir() {
        return configurationData.getConfigurationDir();
    }

    /**
     * @deprecated this is only exposed temporarily for configuration manager
     */
    public ConfigurationResolver getConfigurationResolver() {
        return configurationResolver;
    }

    /**
     * Gets the relative class path (URIs) of this configuration.
     * @return the relative class path of this configuation
     */
    public List<String> getClassPath() {
        return new ArrayList<String>(classPath);
    }

    public void addToClassPath(String pattern) throws IOException {
        if (!classPath.contains(pattern)) {
            try {
                Set<URL> matches = configurationResolver.resolve(pattern);
                for (URL url : matches) {
                    configurationClassLoader.addURL(url);
                }
                classPath.add(pattern);
            } catch (Exception e) {
                throw (IOException)new IOException("Unable to extend classpath with " + pattern).initCause(e);
            }
        }
    }

    /**
     * Gets the type of the configuration (WAR, RAR et cetera)
     * @return Type of the configuration.
     */
    public ConfigurationModuleType getModuleType() {
        return configurationData.getModuleType();
    }

    /**
     * Gets the time at which this configuration was created (or deployed).
     * @return the time at which this configuration was created (or deployed)
     */
    public long getCreated() {
        return configurationData.getCreated();
    }

    /**
     * Gets the class loader for this configuration.
     * @return the class loader for this configuration
     */
    public ClassLoader getConfigurationClassLoader() {
        return configurationClassLoader;
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
     * @return the configurations owned by this configuration
     */
    public Set<Artifact> getOwnedConfigurations() {
        return configurationData.getOwnedConfigurations();
    }

    /**
     * Gets an unmodifiable collection of the GBeanDatas for the GBeans in this configuration.
     * @return the GBeans in this configuration
     */
    public Map<AbstractName, GBeanData> getGBeans() {
        return Collections.unmodifiableMap(gbeans);
    }

    /**
     * Determines of this configuration constains the specified GBean.
     * @param gbean the name of the GBean
     * @return true if this configuration contains the specified GBean; false otherwise
     */
    public synchronized boolean containsGBean(AbstractName gbean) {
        return gbeans.containsKey(gbean);
    }

    /**
     * Gets the enclosing configuration of this one (e.g. the EAR for a WAR),
     * or null if it has none.
     * @return enclosing configuration, if any
     */
    public Configuration getEnclosingConfiguration() {
        return parent;
    }

    public synchronized AbstractName addGBean(String name, GBeanData gbean) throws GBeanAlreadyExistsException {
        AbstractName abstractName = gbean.getAbstractName();
        if (abstractName != null) {
            throw new IllegalArgumentException("gbean already has an abstract name: " + abstractName);
        }

        String j2eeType = gbean.getGBeanInfo().getJ2eeType();
        if (j2eeType == null) j2eeType = "GBean";
        abstractName = naming.createRootName(id, name, j2eeType);
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
            return (GBeanData) result.iterator().next();
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
        for (GBeanData gBeanData: datas) {
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
     * @param patterns patterns to look for
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
        log.debug("Started configuration {}", id);
    }

    public synchronized void doStop() throws Exception {
        log.debug("Stopping configuration {}", id);
        shutdown();

    }

    public void doFail() {
        log.debug("Failed configuration {}", id);
        shutdown();
    }

    private void shutdown() {
        for (Configuration configuration : children) {
            configuration.shutdown();
        }

        // clear references to GBeanDatas
        gbeans.clear();

        // destroy the class loader
        if (configurationClassLoader != null) {
            configurationClassLoader.destroy();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(Configuration.class);//does not use jsr-77 naming
        infoFactory.addReference("Parents", Configuration.class);
        infoFactory.addAttribute("configurationData", ConfigurationData.class, true, false);
        infoFactory.addAttribute("configurationResolver", ConfigurationResolver.class, true);
        infoFactory.addAttribute("managedAttributeStore", ManageableAttributeStore.class, true);

        infoFactory.addInterface(Configuration.class);

        infoFactory.setConstructor(new String[]{
                "Parents",
                "configurationData",
                "configurationResolver",
                "managedAttributeStore"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
