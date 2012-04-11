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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Maven2Repository;
import org.apache.geronimo.kernel.util.CircularReferencesException;
import org.apache.geronimo.kernel.util.IllegalNodeConfigException;
import org.apache.geronimo.kernel.util.SortUtils;
import org.apache.geronimo.kernel.util.SortUtils.Visitor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:386276 $ $Date$
 */
public final class ConfigurationUtil {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationUtil.class);
    private static final ConfigurationMarshaler configurationMarshaler;

    static {
        ConfigurationMarshaler marshaler = null;
        String marshalerClass = System.getProperty("Xorg.apache.geronimo.kernel.config.Marshaler");
        if (marshalerClass != null) {
            try {
                marshaler = createConfigurationMarshaler(marshalerClass);
            } catch (Exception e) {
                log.error("Error creating configuration marshaler class " + marshalerClass , e);
            }
        }

        // todo this code effectively makes the default format xstream
        //if (marshaler == null) {
        //    try {
        //        marshaler = createConfigurationMarshaler("org.apache.geronimo.kernel.config.xstream.XStreamConfigurationMarshaler");
        //    } catch (Throwable ignored) {
        //    }
        //}

        if (marshaler == null) {
            marshaler = new SerializedConfigurationMarshaler();
        }

        configurationMarshaler = marshaler;
    }

    private static File bootDirectory;

    private static File getStartupDirectory() {
        // guess from the location of the jar
        URL url = ConfigurationUtil.class.getClassLoader().getResource("META-INF/startup-jar");

        File directory = null;
        if (url != null) {
            try {
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                url = jarConnection.getJarFileURL();

                URI baseURI = new URI(url.toString()).resolve("..");
                directory = new File(baseURI);
            } catch (Exception ignored) {
                log.error("Error while determining the installation directory of Apache Geronimo", ignored);
            }
        } else {
            log.error("Cound not determine the installation directory of Apache Geronimo, because the startup jar could not be found in the current class loader.");
        }

        return directory;
    }

    private static File getBootDirectory() {
        if (bootDirectory == null) {
            bootDirectory = getStartupDirectory();
        }
        return bootDirectory;
    }

    public static ConfigurationMarshaler createConfigurationMarshaler(String marshalerClass) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class clazz = null;
        if (classLoader != null) {
            try {
                clazz = ClassLoading.loadClass(marshalerClass, classLoader);
            } catch (ClassNotFoundException ignored) {
                // doesn't matter
            }
        }
        if (clazz == null) {
            classLoader = ConfigurationUtil.class.getClassLoader();
            try {
                clazz = ClassLoading.loadClass(marshalerClass, classLoader);
            } catch (ClassNotFoundException ignored) {
                // doesn't matter
            }
        }

        if (clazz != null) {
            Object object = clazz.newInstance();
            if (object instanceof ConfigurationMarshaler) {
                return (ConfigurationMarshaler) object;
            } else {
                log.warn("Configuration marshaler class is not an instance of ConfigurationMarshaler " + marshalerClass + ": using default configuration ");
            }
        }
        return null;
    }

    private ConfigurationUtil() {
    }

    public static GBeanState newGBeanState(Collection gbeans) {
        return configurationMarshaler.newGBeanState(gbeans);
    }

    public static AbstractName loadBootstrapConfiguration(Kernel kernel, InputStream in, BundleContext bundleContext, ConfigurationManager configurationManager) throws Exception {
        return loadBootstrapConfiguration(kernel, in, bundleContext, false, configurationManager);
    }

    public static AbstractName loadBootstrapConfiguration(Kernel kernel, InputStream in, BundleContext bundleContext, boolean enableBootRepo, ConfigurationManager configurationManager) throws Exception {
        ConfigurationData configurationData = readConfigurationData(in);
        return loadBootstrapConfiguration(kernel, configurationData, bundleContext, enableBootRepo, configurationManager);
    }

    public static AbstractName loadBootstrapConfiguration(Kernel kernel, ConfigurationData configurationData, BundleContext bundleContext, ConfigurationManager configurationManager) throws Exception {
        return loadBootstrapConfiguration(kernel, configurationData, bundleContext, false, configurationManager);
    }

    public static AbstractName loadBootstrapConfiguration(Kernel kernel, ConfigurationData configurationData, BundleContext bundleContext, boolean enableBootRepo, ConfigurationManager configurationManager) throws Exception {
        if (kernel == null) throw new NullPointerException("kernel is null");
        if (configurationData == null) throw new NullPointerException("configurationData is null");
        if (bundleContext == null) throw new NullPointerException("bundle is null");

        configurationData.setBundleContext(bundleContext);

        // build the gbean data
        Artifact configId = configurationData.getId();
        AbstractName abstractName = Configuration.getConfigurationAbstractName(configId);
        GBeanData gbeanData = new GBeanData(abstractName, Configuration.class);
        gbeanData.setAttribute("configurationData", configurationData);

        Collection repositories = null;
        ArtifactResolver artifactResolver = null;
        if (enableBootRepo) {
            String repository = System.getProperty("Xorg.apache.geronimo.repository.boot.path", "repository");
            Maven2Repository bootRepository = new Maven2Repository(new File(getBootDirectory(), repository));
            repositories = Collections.singleton(bootRepository);
            artifactResolver = new DefaultArtifactResolver(new DefaultArtifactManager(), bootRepository);
        } else {
            // a bootstrap configuration can not have any dependencies
            List dependencies = configurationData.getEnvironment().getDependencies();
            if (!dependencies.isEmpty()) {
                configurationData.getEnvironment().setDependencies(Collections.EMPTY_SET);
            }
        }
        ConfigurationResolver configurationResolver = new ConfigurationResolver(configurationData, repositories, artifactResolver);

        DependencyNode dependencyNode = new DependencyNode(configId, new LinkedHashSet<Artifact>(), new LinkedHashSet<Artifact>());
        gbeanData.setAttribute("dependencyNode", dependencyNode);
        gbeanData.setAttribute("allServiceParents", Collections.<Configuration>emptyList());


        // load and start the gbean
        kernel.loadGBean(gbeanData, bundleContext);
        kernel.startGBean(gbeanData.getAbstractName());

        Configuration configuration = (Configuration) kernel.getGBean(gbeanData.getAbstractName());

        // start the gbeans
        startConfigurationGBeans(configuration.getAbstractName(), configuration, kernel);

//        ConfigurationManager configurationManager = getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configId);
        return gbeanData.getAbstractName();
    }

    public static void writeConfigurationData(ConfigurationData configurationData, OutputStream out) throws IOException {
        configurationMarshaler.writeConfigurationData(configurationData, out);
    }

    public static ConfigurationData readConfigurationData(InputStream in) throws IOException, ClassNotFoundException {
        return configurationMarshaler.readConfigurationData(in);
    }

    public static void writeConfigInfo(PrintWriter writer, ConfigurationData configurationData) {
        writeConfigInfo("", writer, configurationData);
    }

    private static void writeConfigInfo(String prefix, PrintWriter writer, ConfigurationData configurationData) {
        writer.println(prefix+"id=" + configurationData.getId());
        writer.println(prefix+"type=" + configurationData.getModuleType());
        writer.println(prefix+"created=" + configurationData.getCreated());
        Set<Artifact> ownedConfigurations = configurationData.getOwnedConfigurations();
        int i = 0;
        for (Artifact ownedConfiguration : ownedConfigurations) {
            writer.println(prefix + "owned." + i++ + "=" + ownedConfiguration);
        }
        i = 0;
        for (ConfigurationData data : configurationData.getChildConfigurations().values()) {
            writeConfigInfo("child." + i++ + ".", writer, data);
        }
        writer.flush();
    }

    public static ConfigurationInfo readConfigurationInfo(InputStream in, AbstractName storeName, File inPlaceLocation) throws IOException {
        Properties properties = new Properties();
        // file is written with UTF-8
        properties.load(new InputStreamReader(in, "UTF-8"));
        return readConfigurationInfo("", properties, storeName, inPlaceLocation);
    }

    private static ConfigurationInfo readConfigurationInfo(String prefix, Properties properties, AbstractName storeName, File inPlaceLocation) throws IOException {
        String id = properties.getProperty(prefix+"id");
        Artifact configId = Artifact.create(id);

        String type = properties.getProperty(prefix+"type");
        ConfigurationModuleType moduleType = ConfigurationModuleType.getByName(type);
        if (moduleType == null) {
            throw new IllegalArgumentException("Unknown module type: " + type);
        }

        String created = properties.getProperty(prefix+"created");
        long time;
        try {
            time = Long.parseLong(created);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid created time: " + created);
        }

        LinkedHashSet ownedConfigurations = new LinkedHashSet();
        for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            if (name.startsWith(prefix+"owned.")) {
                String value = (String) entry.getValue();
                Artifact ownedConfiguration = Artifact.create(value);
                ownedConfigurations.add(ownedConfiguration);
            }
        }
        LinkedHashSet childConfigurations = new LinkedHashSet();
        int test = 0;
        while(true) {
            String next = prefix+"child."+test+".";
            String value = properties.getProperty(next+".id");
            if(value == null) {
                break;
            }
            childConfigurations.add(readConfigurationInfo(next, properties, storeName, inPlaceLocation));
            ++test;
        }

        return new ConfigurationInfo(storeName, configId, moduleType, time, ownedConfigurations, childConfigurations, inPlaceLocation);
    }

    /**
     * Gets the name of the ConfigurationManager running in the specified kernel.
     *
     * @return Its AbstractName
     * @throws IllegalStateException Occurs if a ConfigurationManager cannot be identified
     */
    public static AbstractName getConfigurationManagerName(Kernel kernel) {
        Set names = kernel.listGBeans(new AbstractNameQuery(ConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            if (!kernel.isRunning(abstractName)) {
                iterator.remove();
            }
        }
        if (names.isEmpty()) {
            throw new IllegalStateException("A Configuration Manager could not be found in the kernel");
        }
        if (names.size() > 1) {
            String error = "More than one Configuration Manager was found in the kernel: ";
            for (Object name : names) {
                AbstractName abName = (AbstractName)name;
                error = error + "\"" + abName.toString() + "\" ";
            }
            throw new IllegalStateException(error);
        }
        return (AbstractName) names.iterator().next();
    }


    /**
     * Gets a reference or proxy to the ConfigurationManager running in the specified kernel.
     *
     * @return The ConfigurationManager
     * @throws IllegalStateException Occurs if a ConfigurationManager cannot be identified
     */
    public static ConfigurationManager getConfigurationManager(Kernel kernel) throws GBeanNotFoundException {
        return kernel.getGBean(ConfigurationManager.class);
    }

    /**
     * Gets a reference or proxy to an EditableConfigurationManager running in the specified kernel, if there is one.
     *
     * @return The EdtiableConfigurationManager, or none if there is not one available.
     * @throws IllegalStateException Occurs if there are multiple EditableConfigurationManagers in the kernel.
     */
    public static EditableConfigurationManager getEditableConfigurationManager(Kernel kernel) {
        Set names = kernel.listGBeans(new AbstractNameQuery(EditableConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            if (!kernel.isRunning(abstractName)) {
                iterator.remove();
            }
        }
        if (names.isEmpty()) {
            return null; // may be one, just not editable
        }
        if (names.size() > 1) {
            throw new IllegalStateException("More than one Configuration Manager was found in the kernel");
        }
        AbstractName configurationManagerName = (AbstractName) names.iterator().next();
        return kernel.getProxyManager().createProxy(configurationManagerName, EditableConfigurationManager.class);
    }

    public static void releaseConfigurationManager(Kernel kernel, ConfigurationManager configurationManager) {
//        kernel.getProxyManager().destroyProxy(configurationManager);
    }

    static void preprocessGBeanData(AbstractName configurationName, Configuration configuration, GBeanData gbeanData) throws InvalidConfigException {
        if (log.isDebugEnabled()) {
            log.debug("resolving dependencies for " + gbeanData.getAbstractName());
        }
        for (String referenceName : gbeanData.getReferencesNames()) {
            GReferenceInfo referenceInfo = gbeanData.getGBeanInfo().getReference(referenceName);
            if (referenceInfo == null) {
                throw new InvalidConfigException("No reference named " + referenceName + " in gbean " + gbeanData.getAbstractName());
            }
            boolean isSingleValued = !referenceInfo.getProxyType().equals(Collection.class.getName());
            if (isSingleValued) {
                ReferencePatterns referencePatterns = gbeanData.getReferencePatterns(referenceName);
                AbstractName abstractName;
                try {
                    abstractName = configuration.findGBean(referencePatterns);
                    if (log.isDebugEnabled()) {
                        log.debug("referencePatterns: " + referencePatterns + " resolved to " + abstractName);
                    }
                } catch (GBeanNotFoundException e) {
                    throw new InvalidConfigException("Unable to resolve reference \"" + referenceName + "\" in gbean " + gbeanData.getAbstractName() + " to a gbean matching the pattern " + referencePatterns, e);
                }
                gbeanData.setReferencePatterns(referenceName, new ReferencePatterns(abstractName));
            }
        }

        Set newDependencies = new HashSet();
        for (ReferencePatterns referencePatterns : gbeanData.getDependencies()) {
            AbstractName abstractName;
            try {
                abstractName = configuration.findGBean(referencePatterns);
                if (log.isDebugEnabled()) {
                    log.debug("referencePatterns: " + referencePatterns + " resolved to " + abstractName);
                }
            } catch (GBeanNotFoundException e) {
                throw new InvalidConfigException("Unable to resolve dependency in gbean " + gbeanData.getAbstractName(), e);
            }
            newDependencies.add(new ReferencePatterns(abstractName));
        }
        gbeanData.setDependencies(newDependencies);

        // If the GBean has a configurationBaseUrl attribute, set it
        // todo Even though this is not used by the classloader, web apps and persistence units still need this.  WHY???
        //this doesn't work in osgi
        /*
        GAttributeInfo attribute = gbeanData.getGBeanInfo().getAttribute("configurationBaseUrl");
        if (attribute != null && attribute.getType().equals("java.net.URL")) {
            try {
                Set set = configuration.getConfigurationResolver().resolve("");
                if (set.size() != 1) {
                    throw new AssertionError("Expected one match for pattern \".\", but got " + set.size() + " matches");
                }
                URL baseURL = (URL) set.iterator().next();
                gbeanData.setAttribute("configurationBaseUrl", baseURL);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to set attribute named " + "configurationBaseUrl" + " in gbean " + gbeanData.getAbstractName(), e);
            }
        }
        */
        // add a dependency from the gbean to the configuration
        gbeanData.addDependency(configurationName);
    }

    static void startConfigurationGBeans(AbstractName configurationName, Configuration configuration, Kernel kernel) throws InvalidConfigException {
        List<GBeanData> gbeans = new ArrayList<GBeanData>(configuration.getGBeans().values());
        Collections.sort(gbeans, new GBeanData.PriorityComparator());

        List<AbstractName> loaded = new ArrayList<AbstractName>(gbeans.size());
        List<AbstractName> started = new ArrayList<AbstractName>(gbeans.size());

        try {
            // register all the GBeans
            for (GBeanData gbeanData : gbeans) {

                // copy the gbeanData object as not to mutate the original
                gbeanData = new GBeanData(gbeanData);

                // preprocess the gbeanData (resolve references, set base url, declare dependency, etc.)
                preprocessGBeanData(configurationName, configuration, gbeanData);

                try {
                    kernel.loadGBean(gbeanData, configuration.getBundleContext());
                    loaded.add(gbeanData.getAbstractName());
                } catch (GBeanAlreadyExistsException e) {
                    throw new InvalidConfigException(e);
                } catch (Throwable e) {
                    log.warn("Could not load gbean " + gbeanData.getAbstractName(), e);
                    throw e;
                }
            }

            try {
                // start the gbeans
                for (GBeanData gbeanData : gbeans) {
                    AbstractName gbeanName = gbeanData.getAbstractName();
                    kernel.startRecursiveGBean(gbeanName);
                    started.add(gbeanName);
                }

                // assure all of the gbeans are started
                List<String> unstarted = new ArrayList<String>();
                for (GBeanData gbeanData : gbeans) {
                    AbstractName gbeanName = gbeanData.getAbstractName();
                    if (State.RUNNING_INDEX != kernel.getGBeanState(gbeanName)) {
                        String stateReason = null;
                        if (kernel instanceof BasicKernel) {
                            stateReason = kernel.getStateReason(gbeanName);
                        }
                        String name = gbeanName.toURI().getQuery();
                        if (stateReason != null) {
                            unstarted.add("The service " + name + " did not start because " + stateReason);
                        } else {
                            unstarted.add("The service " + name + " did not start for an unknown reason");
                        }
                    }
                }
                if (!unstarted.isEmpty()) {
                    StringBuilder message = new StringBuilder();
                    message.append("Configuration ").append(configuration.getId()).append(" failed to start due to the following reasons:\n");
                    for (String reason : unstarted) {
                        message.append("  ").append(reason).append("\n");
                    }
                    throw new InvalidConfigurationException(message.toString());
                }
            } catch (GBeanNotFoundException e) {
                throw new InvalidConfigException(e);
            }

            for (Configuration childConfiguration : configuration.getChildren()) {
                ConfigurationUtil.startConfigurationGBeans(configurationName, childConfiguration, kernel);
            }
        } catch (Throwable e) {
            for (AbstractName gbeanName : started) {
                try {
                    kernel.stopGBean(gbeanName);
                } catch (GBeanNotFoundException ignored) {
                } catch (IllegalStateException ignored) {
                } catch (InternalKernelException kernelException) {
                    log.debug("Error cleaning up after failed start of configuration " + configuration.getId() + " gbean " + gbeanName, kernelException);
                }
            }
            for (AbstractName gbeanName : loaded) {
                try {
                    kernel.unloadGBean(gbeanName);
                } catch (GBeanNotFoundException ignored) {
                } catch (IllegalStateException ignored) {
                } catch (InternalKernelException kernelException) {
                    log.debug("Error cleaning up after failed start of configuration " + configuration.getId() + " gbean " + gbeanName, kernelException);
                }
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw new InvalidConfigException("Unknown start exception", e);
        }
    }

    public static List<GBeanData> sortGBeanDataByDependency(final Collection<GBeanData> gbeanDatas) throws IllegalNodeConfigException, CircularReferencesException {

        return SortUtils.sort(gbeanDatas, new Visitor<GBeanData>() {

            @Override
            public String getName(GBeanData t) {
                return t.getAbstractName().toString();
            }

            @Override
            public List<String> getAfterNames(GBeanData t) {
                List<String> afterNames = new ArrayList<String>();
                for (GBeanData gbeanData : gbeanDatas) {
                    if (gbeanData == t) {
                        continue;
                    }
                    if (isDependent(t, gbeanData)) {
                        afterNames.add(gbeanData.getAbstractName().toString());
                    }
                }
                return afterNames;
            }

            @Override
            public List<String> getBeforeNames(GBeanData t) {
                return Collections.<String>emptyList();
            }

            @Override
            public boolean afterOthers(GBeanData t) {
                return false;
            }

            @Override
            public boolean beforeOthers(GBeanData t) {
                return false;
            }

            /**
            *
            * @param o1
            * @param o2
            * @return true if o1 is dependent on o2
            */
           private boolean isDependent(GBeanData o1, GBeanData o2) {
               for (ReferencePatterns referencePatterns : o1.getDependencies()) {
                   if (match(referencePatterns, o2)) {
                       return true;
                   }
               }
               for (Map.Entry<String, ReferencePatterns> entry : o1.getReferences().entrySet()) {
                   if (o1.getGBeanInfo().getReference(entry.getKey()).getProxyType().equals(Collection.class.getName())) {
                       continue;
                   }
                   if (match(entry.getValue(), o2)) {
                       return true;
                   }
               }
               return false;
           }

           private boolean match(ReferencePatterns referencePatterns, GBeanData targetGBeanData) {
               AbstractName targetAbstractName = targetGBeanData.getAbstractName();
               if (referencePatterns.isResolved()) {
                   return referencePatterns.getAbstractName().equals(targetAbstractName);
               } else if (referencePatterns.getPatterns() != null) {
                   for (AbstractNameQuery abstractNameQuery : referencePatterns.getPatterns()) {
                       if (abstractNameQuery.matches(targetAbstractName, targetGBeanData.getGBeanInfo().getInterfaces())) {
                           return true;
                       }
                   }
               }
               return false;
           }
        });

    }
}
