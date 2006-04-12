/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class Configuration implements GBeanLifecycle, ConfigurationParent {
    private static final Log log = LogFactory.getLog(Configuration.class);
    public static final Object JSR77_BASE_NAME_PROPERTY = "org.apache.geronimo.name.javax.management.j2ee.BaseName";

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
    private final List classParents = new ArrayList();

    /**
     * Parent configuations used for service resolution.
     */
    private final List serviceParents = new ArrayList();

    /**
     * All service parents depth first
     */
    private final List allServiceParents = new ArrayList();

    /**
     * Artifacts added to the class loader (non-configuation artifacts).
     */
    private final LinkedHashSet dependencies = new LinkedHashSet();

    /**
     * The GBeanData objects by ObjectName
     */
    private final Map gbeans = new HashMap();

    /**
     * The classloader used to load the child GBeans contained in this configuration.
     */
    private final MultiParentClassLoader configurationClassLoader;

    /**
     * The relative class path (URI) of this configuation.
     */
    private final LinkedHashSet classPath;

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
    List children = new ArrayList();

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
    public Configuration(Collection parents,
            ConfigurationData configurationData,
            ConfigurationResolver configurationResolver) throws MissingDependencyException, MalformedURLException, NoSuchConfigException, InvalidConfigException {
        if (parents == null) parents = Collections.EMPTY_SET;
        if (configurationData == null) throw new NullPointerException("configurationData is null");
        if (configurationResolver == null) throw new NullPointerException("configurationResolver is null");

        this.configurationData = configurationData;
        this.environment = this.configurationData.getEnvironment();
        this.configurationResolver = configurationResolver;
        this.classPath = new LinkedHashSet(configurationData.getClassPath());
        this.naming = configurationData.getNaming();

        this.id = environment.getConfigId();
        abstractName = getConfigurationAbstractName(id);

        //
        // Transitively resolve all the dependencies in the environment
        //
        List transtiveDependencies = configurationResolver.resolveTransitiveDependencies(parents, environment.getDependencies());

        //
        // Process transtive dependencies splitting it into classParents, serviceParents and artifactDependencies
        //
        Map parentsById = new HashMap();
        for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            Artifact id = configuration.getId();
            parentsById.put(id, configuration);
        }

        for (Iterator iterator = transtiveDependencies.iterator(); iterator.hasNext();) {
            Dependency dependency = (Dependency) iterator.next();
            Artifact artifact = dependency.getArtifact();
            if (parentsById.containsKey(artifact)) {
                Configuration parent = (Configuration) parentsById.get(artifact);
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

        //
        // Build the configuration class loader
        //
        configurationClassLoader = createConfigurationClasssLoader(parents, environment, classPath);

        //
        // Get all service parents in depth first order
        //
        addDepthFirstServiceParents(this, allServiceParents);

        //
        // Deserialize the GBeans in the configurationData
        //
        List gbeans = configurationData.getGBeans(configurationClassLoader);
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            this.gbeans.put(gbeanData.getAbstractName(), gbeanData);
        }

        //
        // Create child configurations
        //
        LinkedHashSet childParents = new LinkedHashSet(parents);
        childParents.add(this);
        for (Iterator iterator = configurationData.getChildConfigurations().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String moduleName = (String) entry.getKey();
            ConfigurationData childConfigurationData = (ConfigurationData) entry.getValue();
            Configuration childConfiguration = new Configuration(childParents, childConfigurationData, configurationResolver.createChildResolver(moduleName));
            childConfiguration.parent = this;
            children.add(childConfiguration);
        }
    }

    private MultiParentClassLoader createConfigurationClasssLoader(Collection parents, Environment environment, LinkedHashSet classPath) throws MalformedURLException, MissingDependencyException, NoSuchConfigException {
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
        Set hiddenClassesSet = environment.getHiddenClasses();
        String[] hiddenClasses = (String[]) hiddenClassesSet.toArray(new String[hiddenClassesSet.size()]);

        // we need to propagate the non-overrideable classes from parents
        LinkedHashSet nonOverridableSet = new LinkedHashSet();
        for (Iterator iterator = classParents.iterator(); iterator.hasNext();) {
            Configuration parent = (Configuration) iterator.next();

            Environment parentEnvironment = parent.getEnvironment();
            nonOverridableSet.addAll(parentEnvironment.getNonOverrideableClasses());
        }
        String[] nonOverridableClasses = (String[]) nonOverridableSet.toArray(new String[nonOverridableSet.size()]);

        log.debug("ClassPath for " + id + " resolved to " + Arrays.asList(urls));

        return new MultiParentClassLoader(environment.getConfigId(),
                urls,
                parentClassLoaders,
                environment.isInverseClassLoading(),
                hiddenClasses,
                nonOverridableClasses);
    }

    private void addDepthFirstServiceParents(Configuration configuration, List ancestors) {
        ancestors.add(configuration);
        for (Iterator parents = configuration.getServiceParents().iterator(); parents.hasNext();) {
            Configuration parent = (Configuration) parents.next();
            addDepthFirstServiceParents(parent, ancestors);
        }
    }

    private URL[] buildClassPath(LinkedHashSet classPath) throws MalformedURLException, MissingDependencyException, NoSuchConfigException {
        List urls = new ArrayList();
        for (Iterator i = dependencies.iterator(); i.hasNext();) {
            Artifact artifact = (Artifact) i.next();
            File file = configurationResolver.resolve(artifact);
            urls.add(file.toURL());
        }
        if (classPath != null) {
            for (Iterator i = classPath.iterator(); i.hasNext();) {
                URI uri = (URI) i.next();
                urls.add(configurationResolver.resolve(uri));
            }
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
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
    public List getClassParents() {
        return classParents;
    }

    /**
     * Gets the parent configurations used for service resolution.
     * @return the parents of this configuration used for service resolution
     */
    public List getServiceParents() {
        return serviceParents;
    }

    /**
     * Gets the artifact dependencies of this configuration.
     * @return the artifact dependencies of this configuration
     */
    public LinkedHashSet getDependencies() {
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
     * @deprecated this is only exposed temporarily for configuration manager
     */
    public ConfigurationResolver getConfigurationResolver() {
        return configurationResolver;
    }

    /**
     * Gets the relative class path (URIs) of this configuration.
     * @return the relative class path of this configuation
     */
    public List getClassPath() {
        return new ArrayList(classPath);
    }

    public void addToClassPath(URI path) throws IOException {
        if (!classPath.contains(path)) {
            try {
                URL url = configurationResolver.resolve(path);
                configurationClassLoader.addURL(url);
                classPath.add(path);
            } catch (Exception e) {
                throw new IOException("Unable to extend classpath with " + path);
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
     * Gets the class loader for this configuration.
     * @return the class loader for this configuration
     */
    public ClassLoader getConfigurationClassLoader() {
        return configurationClassLoader;
    }

    /**
     * Gets the nested configurations of this configuration.
     * @return the nested configuration of this configuration
     */
    public List getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Gets an unmodifiable collection of the GBeanDatas for the GBeans in this configuration.
     * @return the GBeans in this configuration
     */
    public Map getGBeans() {
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
        Set patterns = referencePatterns.getPatterns();
        return findGBean(patterns);
    }

    public AbstractName findGBean(Set patterns) throws GBeanNotFoundException {
        if (patterns == null) throw new NullPointerException("patterns is null");
        return findGBeanData(patterns).getAbstractName();
    }

    public GBeanData findGBeanData(Set patterns) throws GBeanNotFoundException {
        if (patterns == null) throw new NullPointerException("patterns is null");
        Set result = findGBeanDatas(this, patterns);
        if (result.size() > 1) {
            throw new GBeanNotFoundException("More than one match to referencePatterns", patterns);
        } else if (result.size() == 1) {
            return (GBeanData) result.iterator().next();
        }

        // search all parents
        for (Iterator iterator = allServiceParents.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            result.addAll(findGBeanDatas(configuration, patterns));

            // if we already found a match we have an ambiguous query
            if (result.size() > 1) {
                throw new GBeanNotFoundException("More than one match to referencePatterns", patterns);
            }
        }

        if (result.isEmpty()) {
            throw new GBeanNotFoundException("No matches for referencePatterns", patterns);
        }

        return (GBeanData) result.iterator().next();
    }

    public LinkedHashSet findGBeans(AbstractNameQuery pattern) {
        if (pattern == null) throw new NullPointerException("pattern is null");
        return findGBeans(Collections.singleton(pattern));
    }

    public LinkedHashSet findGBeans(ReferencePatterns referencePatterns) {
        if (referencePatterns == null) throw new NullPointerException("referencePatterns is null");
        if (referencePatterns.getAbstractName() != null) {
            // this pattern is already resolved
            LinkedHashSet result = new LinkedHashSet();
            result.add(referencePatterns.getAbstractName());
            return result;
        }

        // check the local config
        Set patterns = referencePatterns.getPatterns();
        return findGBeans(patterns);
    }

    public LinkedHashSet findGBeans(Set patterns) {
        if (patterns == null) throw new NullPointerException("patterns is null");
        LinkedHashSet datas = findGBeanDatas(patterns);
        LinkedHashSet result = new LinkedHashSet(datas.size());
        for (Iterator iterator = datas.iterator(); iterator.hasNext();) {
            GBeanData gBeanData = (GBeanData) iterator.next();
            result.add(gBeanData.getAbstractName());
        }

        return result;
    }

    public LinkedHashSet findGBeanDatas(Set patterns) {
        if (patterns == null) throw new NullPointerException("patterns is null");
        LinkedHashSet datas = findGBeanDatas(this, patterns);

        // search all parents
        for (Iterator iterator = allServiceParents.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            Set match = findGBeanDatas(configuration, patterns);
            datas.addAll(match);
        }
        return datas;
    }

    private LinkedHashSet findGBeanDatas(Configuration configuration, Set patterns) {
        LinkedHashSet result = new LinkedHashSet();

        Set gbeanNames = configuration.getGBeans().entrySet();
        for (Iterator abstractNameQueries = patterns.iterator(); abstractNameQueries.hasNext();) {
            AbstractNameQuery abstractNameQuery =  (AbstractNameQuery) abstractNameQueries.next();
            Artifact queryArtifact = abstractNameQuery.getArtifact();

            // Does this query apply to this configuration
            if (queryArtifact == null || queryArtifact.matches(configuration.getId())) {

                // Search the GBeans
                for (Iterator iterator = gbeanNames.iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    AbstractName abstractName = (AbstractName) entry.getKey();
                    GBeanData gbeanData = (GBeanData) entry.getValue();
                    if (abstractNameQuery.matches(abstractName, gbeanData.getGBeanInfo().getInterfaces())) {
                        result.add(gbeanData);
                    }
                }
            }
        }
        return result;
    }

    public void doStart() throws Exception {
        log.debug("Started configuration " + id);
    }

    public synchronized void doStop() throws Exception {
        log.debug("Stopping configuration " + id);
        shutdown();

    }

    public void doFail() {
        log.debug("Failed configuration " + id);
        shutdown();
    }

    private void shutdown() {
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
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

        infoFactory.addInterface(Configuration.class);

        infoFactory.setConstructor(new String[]{
                "Parents",
                "configurationData",
                "configurationResolver"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
