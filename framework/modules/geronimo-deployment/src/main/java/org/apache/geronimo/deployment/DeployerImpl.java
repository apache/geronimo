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

package org.apache.geronimo.deployment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.DeploymentWatcher;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.system.configuration.ExecutableConfigurationUtil;
import org.apache.geronimo.system.main.CommandLineManifest;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GBean that knows how to deploy modules (by consulting available module builders)
 *
 * @version $Rev$ $Date$
 */
@Component(immediate = true, metatype = true)
@Service
public class DeployerImpl implements Deployer {
    private static final Logger log = LoggerFactory.getLogger(DeployerImpl.class);
    private static final URI PLAN_LOCATION = URI.create("META-INF/plan.xml");

    private final int REAPER_INTERVAL = 60 * 1000;
    private final DeployerReaper reaper = new DeployerReaper(REAPER_INTERVAL) ;

    @Property(value = "localhost:8080")
    private static final String REMOTE_DEPLOY_ADDRESS = "remoteDeployAddress";

    private String remoteDeployAddress;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ConfigurationBuilder.class, policy = ReferencePolicy.DYNAMIC)
    private final Collection<ConfigurationBuilder> configurationBuilders = new ArrayList<ConfigurationBuilder>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ConfigurationStore.class, policy = ReferencePolicy.DYNAMIC)
    private final Collection<ConfigurationStore> configurationStores = new ArrayList<ConfigurationStore>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = DeploymentWatcher.class, policy = ReferencePolicy.DYNAMIC)
    private final Collection<DeploymentWatcher> deploymentWatchers = new ArrayList<DeploymentWatcher>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC)
    private ArtifactResolver artifactResolver;
//    private final Kernel kernel;


//    public Deployer(String remoteDeployAddress, Collection builders, Collection stores, Collection watchers, Kernel kernel) throws GBeanNotFoundException {
//        this(remoteDeployAddress, builders, stores, watchers, getArtifactResolver(kernel), kernel);
//    }
//
//    private static ArtifactResolver getArtifactResolver(Kernel kernel) throws GBeanNotFoundException {
//        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
//        return configurationManager.getArtifactResolver();
//    }
//
//    public Deployer(String remoteDeployAddress, Collection builders, Collection stores, Collection watchers, ArtifactResolver artifactResolver, Kernel kernel) {
//        this.remoteDeployAddress = remoteDeployAddress;
//        this.builders = builders;
//        this.stores = stores;
//        this.watchers = watchers;
//        this.artifactResolver = artifactResolver;
////        this.kernel = kernel;
//
//        // Create and start the reaper...
//        this.reaper = new DeployerReaper(REAPER_INTERVAL);
//
//    }

    public DeployerImpl() {
    }

    @Activate
    public void activate(Map context) {
        String remoteDeployAddress = (String) context.get(REMOTE_DEPLOY_ADDRESS);
        if (remoteDeployAddress != null) {
            this.remoteDeployAddress = remoteDeployAddress;
        }
    }

    @Deactivate
    public void doStop() throws Exception {
        reaper.close();
    }

    public void bindConfigurationStore(ConfigurationStore store) {
        configurationStores.add(store);
    }

    public void unbindConfigurationStore(ConfigurationStore store) {
        configurationStores.remove(store);
    }

    public void bindConfigurationBuilder(ConfigurationBuilder builder) {
        configurationBuilders.add(builder);
    }

    public void unbindConfigurationBuilder(ConfigurationBuilder builder) {
        configurationBuilders.remove(builder);
    }

    public void bindDeploymentWatcher(DeploymentWatcher watcher) {
        deploymentWatchers.add(watcher);
    }

    public void unbindDeploymentWatcher(DeploymentWatcher watcher) {
        deploymentWatchers.remove(watcher);
    }

    public void setArtifactResolver(ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }
    public void unsetArtifactResolver(ArtifactResolver artifactResolver) {
        if (this.artifactResolver == artifactResolver) {
            this.artifactResolver = null;
        }
    }

    public void setRemoteDeployAddress(String remoteDeployAddress) {
        this.remoteDeployAddress = remoteDeployAddress;
    }

    @Override
    public List<String> deploy(boolean inPlace, File moduleFile, File planFile) throws DeploymentException {
        return deploy(inPlace, moduleFile, planFile, null);
    }

    @Override
    public List<String> deploy(boolean inPlace, File moduleFile, File planFile, String targetConfigStore) throws DeploymentException {
        File originalModuleFile = moduleFile;
        File tmpDir = null;
        if (moduleFile != null && !moduleFile.isDirectory()) {
            // todo jar url handling with Sun's VM on Windows leaves a lock on the module file preventing rebuilds
            // to address this we use a gross hack and copy the file to a temporary directory
            // the lock on the file will prevent that being deleted properly until the URLJarFile has
            // been GC'ed.
            boolean cleanup = true;
            try {
                tmpDir = File.createTempFile("geronimo-deployer", ".tmpdir");
                tmpDir.delete();
                tmpDir.mkdir();
                File tmpFile = new File(tmpDir, moduleFile.getName());
                FileUtils.copyFile(moduleFile, tmpFile);
                moduleFile = tmpFile;
                cleanup = false;
            } catch (IOException e) {
                throw new DeploymentException(e);
            } finally {
                // If an Exception is thrown in the try block above, we will need to cleanup here.
                if(cleanup && tmpDir != null && !FileUtils.recursiveDelete(tmpDir)) {
                    reaper.delete(tmpDir.getAbsolutePath(), "delete");
                }
            }
        }

        try {
            return deploy(inPlace, moduleFile, planFile, null, true, null, null, null, null, null, null, null, targetConfigStore);
        } catch (DeploymentException e) {
            log.debug("Deployment failed: plan=" + planFile + ", module=" + originalModuleFile, e);
            throw e.cleanse();
        } finally {
            if (tmpDir != null) {
                if (!FileUtils.recursiveDelete(tmpDir)) {
                    reaper.delete(tmpDir.getAbsolutePath(), "delete");
                }
            }
        }
    }

    /**
     * Gets a URL that a remote deploy client can use to upload files to the
     * server. Looks up a remote deploy web application by searching for a
     * particular GBean and figuring out a reference to the web application
     * based on that.  Then constructs a URL pointing to that web application
     * based on available connectors for the web container and the context
     * root for the web application.
     *
     * @return The URL that clients should use for deployment file uploads.
     */
    @Override
    public String getRemoteDeployUploadURL() {
        // Get the token GBean from the remote deployment configuration
        //TODO osgi-gbean
        return null;
//        Set set = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.deployment.remote.RemoteDeployToken"));
//        if (set.size() == 0) {
//            return null;
//        }
//        AbstractName token = (AbstractName) set.iterator().next();
//        // Identify the parent configuration for that GBean
//        set = kernel.getDependencyManager().getParents(token);
//        ObjectName config = null;
//        for (Iterator it = set.iterator(); it.hasNext();) {
//            AbstractName name = (AbstractName) it.next();
//            if (Configuration.isConfigurationObjectName(name.getObjectName())) {
//                config = name.getObjectName();
//                break;
//            }
//        }
//        if (config == null) {
//            log.warn("Unable to find remote deployment configuration; is the remote deploy web application running?");
//            return null;
//        }
//        // Generate the URL based on the remote deployment configuration
//        Map<String, String> hash = new HashMap<String, String>();
//        hash.put("J2EEApplication", token.getObjectName().getKeyProperty("J2EEApplication"));
//        hash.put("j2eeType", "WebModule");
//        try {
//            hash.put("name", Configuration.getConfigurationID(config).toString());
//            Set<AbstractName> names = kernel.listGBeans(new AbstractNameQuery(null, hash));
//            if (names.size() != 1) {
//                log.error("Unable to look up remote deploy upload URL");
//                return null;
//            }
//            AbstractName module = names.iterator().next();
//            String contextPath = (String) kernel.getAttribute(module, "contextPath");
//            if (null == contextPath) {
//                throw new IllegalStateException("Cannot find contextPath attribute for [" + module + "]");
//            }
//            String temp = remoteDeployAddress + "/" + contextPath + "/upload";
//            return URI.create(temp).normalize().toString();
//        } catch (Exception e) {
//            log.error("Unable to look up remote deploy upload URL", e);
//            return null;
//        }
    }

    @Override
    public List<String> deploy(boolean inPlace,
                       File moduleFile,
                       File planFile,
                       File targetFile,
                       boolean install,
                       String mainClass,
                       String mainGBean, String mainMethod, String manifestConfigurations, String classPath,
                       String endorsedDirs,
                       String extensionDirs,
                       String targetConfigurationStore) throws DeploymentException {
        if (planFile == null && moduleFile == null) {
            throw new DeploymentException("No plan or module specified");
        } else if (configurationStores.isEmpty()) {
            throw new DeploymentException("No ConfigurationStores!");
        }
        validatePlanFile(planFile);

        ModuleIDBuilder idBuilder = new ModuleIDBuilder();

        DeploymentContext context = null;
        JarFile module = null;
        try {
            module = getModule(inPlace, moduleFile);
            validateModuleFile(module);

            Object plan = null;
            ConfigurationBuilder builder = null;
            for (Iterator<ConfigurationBuilder> i = configurationBuilders.iterator(); i.hasNext();) {
                ConfigurationBuilder candidate = i.next();
                plan = candidate.getDeploymentPlan(planFile, module, idBuilder);
                if (plan != null) {
                    builder = candidate;
                    break;
                }
            }
            if (builder == null) {
                throw new DeploymentException("Cannot deploy the requested application module because no deployer is able to handle it. " +
                        " This can happen if you have omitted the J2EE deployment descriptor, disabled a deployer module, or if, for example, you are trying to deploy an" +
                        " EJB module on a minimal Geronimo server that does not have EJB support installed.  (" +
                        (planFile == null ? "" : "planFile=" + planFile.getAbsolutePath()) +
                        (moduleFile == null ? "" : (planFile == null ? "" : ", ") + "moduleFile=" + moduleFile.getAbsolutePath()) + ")");
            }

            Artifact configID = getConfigID(module, idBuilder, plan, builder);

            ConfigurationStore store = getConfigurationStore(targetConfigurationStore);

            // It's our responsibility to close this context, once we're done with it...
            context = builder.buildConfiguration(inPlace, configID, plan, module, configurationStores, artifactResolver, store);
            // Copy the external plan to the META-INF folder with the uniform name plan.xml if there is nothing there already
            if (planFile != null && !context.getTargetFile(PLAN_LOCATION).exists()) {
                context.addFile(PLAN_LOCATION, planFile);
            }
            // install the configuration
            // create the manifest
            Manifest manifest = createManifest(mainClass,
                mainGBean,
                mainMethod,
                manifestConfigurations,
                classPath,
                endorsedDirs,
                extensionDirs);

            return install(targetFile, install, manifest, store, context);
        } catch (Throwable e) {
            //TODO not clear all errors will result in total cleanup
//            File configurationDir = configurationData.getConfigurationDir();
//            if (!DeploymentUtil.recursiveDelete(configurationDir)) {
//                pendingDeletionIndex.setProperty(configurationDir.getName(), new String("delete"));
//                log.debug("Queued deployment directory to be reaped " + configurationDir);
//            }
//            if (targetFile != null) {
//                targetFile.delete();
//            }

            //Clean Up the created deploymentContext, as some initial work might be done in the buildConfiguration invocation
            if (context != null) {
                try {
                    context.close();
                } catch (Exception ingore) {
                }
            }

            if (e instanceof Error) {
                log.error("Deployment failed due to ", e);
                throw (Error) e;
            } else if (e instanceof DeploymentException) {
                throw (DeploymentException) e;
            } else if (e instanceof Exception) {
                log.error("Deployment failed due to ", e);
                throw new DeploymentException(e);
            }
            throw new Error(e);
        } finally {
            JarUtils.close(module);
        }
    }

    private void validateModuleFile(JarFile module) throws DeploymentException {
        if (module != null && module.getEntry("META-INF/config.ser") != null) {
            throw new DeploymentException("The target applicaiton is an Geronimo plugin as config.ser was found in the META-INF directory, please use the install-plugin command.");
        }
    }

    private ConfigurationStore getConfigurationStore(String targetConfigurationStore)
            throws URISyntaxException, GBeanNotFoundException {
//        if (targetConfigurationStore != null) {
//            AbstractName targetStoreName = new AbstractName(new URI(targetConfigurationStore));
//            return (ConfigurationStore) kernel.getGBean(targetStoreName);
//        } else {
        long timeout = 1000l;
        while (timeout > 1) {
            if (!configurationStores.isEmpty()) {
                break;
            }
            try {
                Thread.sleep(100l);
            } catch (InterruptedException e) {

            }
            timeout =- 100l;
        }
        return configurationStores.iterator().next();
//        }
    }

    private Artifact getConfigID(JarFile module,
            ModuleIDBuilder idBuilder,
            Object plan,
            ConfigurationBuilder builder) throws IOException, DeploymentException, InvalidConfigException {
        Artifact configID = builder.getConfigurationID(plan, module, idBuilder);
        // If the Config ID isn't fully resolved, populate it with defaults
        if (!configID.isResolved()) {
            configID = idBuilder.resolve(configID, "car");
        }

        // Make sure this configuration doesn't already exist
        //TODO osgi-gbean
//        try {
//            kernel.getGBeanState(Configuration.getConfigurationAbstractName(configID));
//            throw new DeploymentException("Module " + configID + " already exists in the server.  Try to undeploy it first or use the redeploy command.");
//        } catch (GBeanNotFoundException e) {
//            // this is good
//        }
        return configID;
    }

    private List<String> install(File targetFile,
            boolean install,
            Manifest manifest,
            ConfigurationStore store,
            DeploymentContext context) throws DeploymentException, IOException, Throwable {
        List<ConfigurationData> configurationDatas = new ArrayList<ConfigurationData>();

        boolean configsCleanupRequired = false;

        // Set TCCL to the classloader for the configuration being deployed
        // so that any static blocks invoked during the loading of classes
        // during serialization of the configuration have the correct TCCL
        // ( a TCCL that is consistent with what is set when the same
        // classes are loaded when the configuration is started.
        Thread thread = Thread.currentThread();
        ClassLoader oldCl = thread.getContextClassLoader();
        //TODO OSGI fixme
        thread.setContextClassLoader( new BundleClassLoader(context.getConfiguration().getBundle()));
        try {
            try {
                configurationDatas.add(context.getConfigurationData());
            } catch (DeploymentException e) {
                Configuration configuration = context.getConfiguration();
                if (configuration != null) {
                    ConfigurationData dumbConfigurationData = new ConfigurationData(null, null, null,
                            configuration.getEnvironment(), context.getBaseDir(), null, context.getNaming());
                    configurationDatas.add(dumbConfigurationData);
                }
                configurationDatas.addAll(context.getAdditionalDeployment());
                throw e;
            }

            configurationDatas.addAll(context.getAdditionalDeployment());

            if (configurationDatas.isEmpty()) {
                throw new DeploymentException("Deployer did not create any configurations");
            }

            if (targetFile != null) {
                if (configurationDatas.size() > 1) {
                    throw new DeploymentException("Deployer created more than one configuration");
                }
                ConfigurationData configurationData = configurationDatas.get(0);
                ExecutableConfigurationUtil.createExecutableConfiguration(configurationData, manifest, targetFile);
            }
            if (install) {
                List<String> deployedURIs = new ArrayList<String>();
                for (ConfigurationData configurationData : configurationDatas) {
                    store.install(configurationData);
                    deployedURIs.add(configurationData.getId().toString());
                }
                notifyWatchers(deployedURIs);
                return deployedURIs;
            } else {
                configsCleanupRequired = true;
                return Collections.emptyList();
            }
        } catch (DeploymentException e) {
            configsCleanupRequired = true;
            throw e;
        } catch (IOException e) {
            configsCleanupRequired = true;
            throw e;
        } catch (InvalidConfigException e) {
            configsCleanupRequired = true;
            // unlikely as we just built this
            throw new DeploymentException(e);
        } catch (Throwable e) {
            // Could get here if serialization of the configuration failed (GERONIMO-1996)
            configsCleanupRequired = true;
            throw e;
        } finally {
            thread.setContextClassLoader(oldCl);
            context.close();
            if (configsCleanupRequired) {
                // We do this after context is closed so the module jar isn't open
                cleanupConfigurations(configurationDatas);
            }
        }
    }

    private Manifest createManifest(String mainClass,
            String mainGBean,
            String mainMethod,
            String manifestConfigurations,
            String classPath,
            String endorsedDirs,
            String extensionDirs) {
        if (mainClass == null) {
            return null;
        }
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        if (mainClass != null) {
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), mainClass);
        }
        if (mainGBean != null) {
            mainAttributes.putValue(CommandLineManifest.MAIN_GBEAN.toString(), mainGBean);
        }
        if (mainMethod != null) {
            mainAttributes.putValue(CommandLineManifest.MAIN_METHOD.toString(), mainMethod);
        }
        if (manifestConfigurations != null) {
            mainAttributes.putValue(CommandLineManifest.CONFIGURATIONS.toString(), manifestConfigurations);
        }
        if (classPath != null) {
            mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), classPath);
        }
        if (endorsedDirs != null) {
            mainAttributes.putValue(CommandLineManifest.ENDORSED_DIRS.toString(), endorsedDirs);
        }
        if (extensionDirs != null) {
            mainAttributes.putValue(CommandLineManifest.EXTENSION_DIRS.toString(), extensionDirs);
        }
        return manifest;
    }

    private JarFile getModule(boolean inPlace, File moduleFile) throws DeploymentException {
        JarFile module = null;
        if (moduleFile != null) {
            if (inPlace && !moduleFile.isDirectory()) {
                throw new DeploymentException("In place deployment is not allowed for packed module");
            }
            if (!moduleFile.exists()) {
                throw new DeploymentException("Module file does not exist: " + moduleFile.getAbsolutePath());
            }
            try {
                module = JarUtils.createJarFile(moduleFile);
            } catch (IOException e) {
                throw new DeploymentException("Cound not open module file: " + moduleFile.getAbsolutePath(), e);
            }
        }
        return module;
    }

    private void validatePlanFile(File planFile) throws DeploymentException {
        if (planFile != null) {
            if (!planFile.exists()) {
                throw new DeploymentException("Plan file does not exist: " + planFile.getAbsolutePath());
            }
            if (!planFile.isFile()) {
                throw new DeploymentException("Plan file is not a regular file: " + planFile.getAbsolutePath());
            }
        }
    }

    private void notifyWatchers(List<String> list) {
        Artifact[] arts = new Artifact[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            arts[i] = Artifact.create(s);
        }
        for (Iterator<DeploymentWatcher> it = deploymentWatchers.iterator(); it.hasNext();) {
            DeploymentWatcher watcher = it.next();
            for (int i = 0; i < arts.length; i++) {
                Artifact art = arts[i];
                watcher.deployed(art);
            }
        }
    }

    private void cleanupConfigurations(List<ConfigurationData> configurations) {
        for (Iterator<ConfigurationData> iterator = configurations.iterator(); iterator.hasNext();) {
            ConfigurationData configurationData = iterator.next();
            File configurationDir = configurationData.getConfigurationDir();
            if (!FileUtils.recursiveDelete(configurationDir)) {
                reaper.delete(configurationDir.getAbsolutePath(), "delete");
            }
        }
    }

    /**
     * Thread to cleanup unused temporary Deployer directories (and files).
     * On Windows, open files can't be deleted. Until MultiParentClassLoaders
     * are GC'ed, we won't be able to delete Config Store directories/files.
     */
    class DeployerReaper implements Runnable {
        private final int reaperInterval;
        private final Properties pendingDeletionIndex = new Properties();
        private volatile boolean done = false;
        private Thread thread;

        public DeployerReaper(int reaperInterval) {
            this.reaperInterval = reaperInterval;
        }

        public void delete(String dir, String type) {
            pendingDeletionIndex.setProperty(dir, type);
            log.debug("Queued deployment directory to be reaped " + dir);
            startThread();
        }

        private synchronized void startThread() {
            if (this.thread == null) {
                this.thread = new Thread(this, "Geronimo Config Store Reaper");
                this.thread.setDaemon(true);
                this.thread.start();
            }
        }

        public void close() {
            this.done = true;
        }

        public void run() {
            log.debug("ConfigStoreReaper started");

            // DeployerReaper in offline deployment does not get a chance to reap the temporary
            // directories. Reap any temporary directories left behind by previous runs here.
            reapBacklog();

            while (!done) {
                try {
                    Thread.sleep(reaperInterval);
                } catch (InterruptedException e) {
                    continue;
                }
                reap();
            }
        }

        /**
         * Reap any temporary directories left behind by previous runs.
         */
        private void reapBacklog() {
            try {
                File tempFile = File.createTempFile("geronimo-deployer", ".tmpdir");
                File tempDir = tempFile.getParentFile();
                tempFile.delete();
                String[] backlog = tempDir.list(new FilenameFilter(){
                    public boolean accept(File dir, String name) {
                        return name.startsWith("geronimo-deployer") && name.endsWith(".tmpdir") && new File(dir, name).isDirectory();
                    }});
                for(String dir: backlog) {
                    File deleteDir = new File(tempDir, dir);
                    FileUtils.recursiveDelete(deleteDir);
                    log.debug("Reaped deployment directory from previous runs " + deleteDir);
                }
            } catch (IOException ignored) {
            }
        }

        /**
         * For every directory in the pendingDeletionIndex, attempt to delete all
         * sub-directories and files.
         */
        public void reap() {
            // return, if there's nothing to do
            if (pendingDeletionIndex.size() == 0)
                return;
            // Otherwise, attempt to delete all of the directories
            Enumeration<?> list = pendingDeletionIndex.propertyNames();
            while (list.hasMoreElements()) {
                String dirName = (String) list.nextElement();
                File deleteDir = new File(dirName);

                if (FileUtils.recursiveDelete(deleteDir)) {
                    pendingDeletionIndex.remove(dirName);
                    log.debug("Reaped deployment directory " + deleteDir);
                }
            }
        }
    }

}
