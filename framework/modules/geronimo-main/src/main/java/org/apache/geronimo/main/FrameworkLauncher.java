/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;

public class FrameworkLauncher {

    /**
     * The default name used for the system properties file.
     */
    public static final String SYSTEM_PROPERTIES_FILE_NAME = "system.properties";

    /**
     * The default name used for the configuration properties file.
     */
    public static final String CONFIG_PROPERTIES_FILE_NAME = "config.properties";

    /**
     * The default name used for the startup properties file.
     */
    public static final String STARTUP_PROPERTIES_FILE_NAME = "startup.properties";
    
    /**
     * The flag file to detect whether server is shutdown normally or by terminating jvm.
     */
    public static final String SERVER_FLAG_FILE_NAME = "server.flag";

    /**
     * The system property for specifying the Karaf home directory.  The home directory
     * hold the binary install of Karaf.
     */
    private static final String PROP_KARAF_HOME = "karaf.home";

    /**
     * The system property for specifying the Karaf base directory.  The base directory
     * holds the configuration and data for a Karaf instance.
     */
    private static final String PROP_KARAF_BASE = "karaf.base";

    private static final String DEFAULT_REPO = "karaf.default.repository";

    private static final String KARAF_FRAMEWORK = "karaf.framework";

    private boolean uniqueInstance = false;
    private String log4jFile;
    private String startupFile = STARTUP_PROPERTIES_FILE_NAME;
    private String configFile = CONFIG_PROPERTIES_FILE_NAME;
    private String additionalConfigFile = null;
    private boolean cleanStorage = false;

    private ServerInfo serverInfo;
    private File geronimoHome;
    private File geronimoBase;
    private File cacheDirectory;

    private Properties configProps = null;

    private Framework framework = null;
    private List<BundleInfo> startList = null;

    public void setLog4jConfigFile(String log4jFile) {
        this.log4jFile = log4jFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setAdditionalConfigFile(String additionalConfigFile) {
        this.additionalConfigFile = additionalConfigFile;
    }

    public void setStartupFile(String startupFile) {
        this.startupFile = startupFile;
    }

    public void setUniqueInstance(boolean uniqueInstance) {
        this.uniqueInstance = uniqueInstance;
    }

    public void setCleanStorage(boolean cleanStorage) {
        this.cleanStorage = cleanStorage;
    }

    public void init() throws Exception {
        geronimoHome = Utils.getGeronimoHome();
        geronimoBase = Utils.getGeronimoBase(geronimoHome);

        Utils.setTempDirectory(geronimoBase);
        Utils.setLog4jConfigurationFile(geronimoBase, log4jFile);

        System.setProperty(Utils.HOME_DIR_SYS_PROP, geronimoHome.getAbsolutePath());
        System.setProperty(Utils.SERVER_DIR_SYS_PROP, geronimoBase.getAbsolutePath());

        System.setProperty(PROP_KARAF_HOME, geronimoHome.getPath());
        System.setProperty(PROP_KARAF_BASE, geronimoBase.getPath());

        // Load system properties.
        loadSystemProperties(geronimoBase);

        // Read configuration properties.
        configProps = loadConfigProperties(geronimoBase);

        // Copy framework properties from the system properties.
        copySystemProperties(configProps);

        updateClassLoader(configProps);

        processSecurityProperties(configProps);

        configJansiPath();

        setFrameworkStorage(configProps);

        if (cleanStorage) {
            configProps.setProperty(Constants.FRAMEWORK_STORAGE_CLEAN,
                                    Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
        
        if(detectServerFlag()){
           configProps.setProperty(Constants.FRAMEWORK_STORAGE_CLEAN,
                    Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
    }
    
    public void launch() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // wait up to 3 minutes for complete shutdown
                FrameworkLauncher.this.shutdown(3 * 60 * 1000);
            }
        });

        launchOSGiFramework();
    }

    private void launchOSGiFramework() throws Exception {
        // Start up the OSGI framework
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        FrameworkFactory factory = loader.iterator().next();
        framework = factory.newFramework(new StringMap(configProps, false));

        framework.init();

        BundleContext bundleContext = framework.getBundleContext();

        serverInfo = new ServerInfo(geronimoHome, geronimoBase);
        bundleContext.registerService(ServerInfo.class.getName(), serverInfo, null);

        startList = loadStartupProperties();
        
        startBundles(bundleContext);

        framework.start();
    }

    public void destroy(boolean await) {
        try {
            if (await) {
                waitForFramework(0);
            }
            stopFramework(0);            
        } catch (Exception e) {
            System.err.println("Error stopping framework: " + e);
        }

        cleanup();
    }

    public void shutdown(long timeout) {
        try {
            stopFramework(timeout);
        } catch (Exception e) {
            System.err.println("Error stopping framework: " + e);
        }

        cleanup();
    }

    private void cleanup() {
        if (uniqueInstance && cacheDirectory != null) {
            Utils.clearSunJarFileFactoryCache(cacheDirectory.getAbsolutePath());
            Utils.recursiveDelete(cacheDirectory);
        }
    }
    
    private void configJansiPath() {
        String osName = "";
        String name = System.getProperty("os.name").toLowerCase().trim();
        if (name.startsWith("linux")) {
            osName = "linux";
        } else if (name.startsWith("mac os x")) {
            osName = "osx";
        } else if (name.startsWith("win")) {
            osName = "windows";
        } else {
            osName = name.replaceAll("\\W+", "_");
        }
        String bitMode = System.getProperty("sun.arch.data.model");
        if (bitMode == null) {
            bitMode = System.getProperty("com.ibm.vm.bitmode");
        }
        System.setProperty("library.jansi.path", new File(geronimoBase, "var/native/jansi/" + osName + bitMode).getAbsolutePath());
    }
   
    private void waitForFramework(long timeout) throws InterruptedException {
        if (framework == null) {
            return;
        }
        while (true) {
            FrameworkEvent event = framework.waitForStop(timeout);
            if (event.getType() != FrameworkEvent.STOPPED_UPDATE) {
                break;
            }
        }
    }
    
    private void stopFramework(long timeout) throws BundleException, InterruptedException {
        if (framework == null) {
            return;
        }
        if (framework.getState() == Bundle.ACTIVE) {
            framework.stop();
            framework.waitForStop(timeout);
        }
    }
    
    public Framework getFramework() {
        return framework;
    }

    private void setFrameworkStorage(Properties configProps) throws IOException {
        if (configProps.getProperty(Constants.FRAMEWORK_STORAGE) != null) {
            return;
        }

        if (uniqueInstance) {
            File var = new File(geronimoBase, "var");
            File tmpFile = File.createTempFile("instance-", "", var);
            cacheDirectory = new File(var, tmpFile.getName() + "-cache");
            tmpFile.delete();
        } else {
            cacheDirectory = new File(geronimoBase, "var/cache");
        }

        cacheDirectory.mkdirs();
        configProps.setProperty(Constants.FRAMEWORK_STORAGE, cacheDirectory.getAbsolutePath());
    }

    private static void processSecurityProperties(Properties m_configProps) {
        String prop = m_configProps.getProperty("org.apache.karaf.security.providers");
        if (prop != null) {
            String[] providers = prop.split(",");
            for (String provider : providers) {
                try {
                    Security.addProvider((Provider) Class.forName(provider).newInstance());
                } catch (Throwable t) {
                    System.err.println("Unable to register security provider: " + t);
                }
            }
        }
    }

    /**
     * <p>
     * Loads the properties in the system property file associated with the
     * framework installation into <tt>System.setProperty()</tt>. These properties
     * are not directly used by the framework in anyway. By default, the system
     * property file is located in the <tt>conf/</tt> directory of the Felix
     * installation directory and is called "<tt>system.properties</tt>". The
     * installation directory of Felix is assumed to be the parent directory of
     * the <tt>felix.jar</tt> file as found on the system class path property.
     * The precise file from which to load system properties can be set by
     * initializing the "<tt>felix.system.properties</tt>" system property to an
     * arbitrary URL.
     * </p>
     */
    protected static void loadSystemProperties(File baseDir) throws IOException {
        File file = new File(new File(baseDir, "etc"), SYSTEM_PROPERTIES_FILE_NAME);
        Properties props = Utils.loadPropertiesFile(file, false);

        // Perform variable substitution on specified properties.
        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            String value = System.getProperty(name, props.getProperty(name));
            System.setProperty(name, Utils.substVars(value, name, null, null));
        }
    }

    private Properties loadConfigProperties(File baseDir) throws Exception {
        File config = new File(new File(baseDir, "etc"), configFile);
        Properties configProps = Utils.loadPropertiesFile(config, false);

        // Perform config props override
        if (additionalConfigFile != null){
            File additionalConfig = new File(new File(baseDir, "etc"), additionalConfigFile);
            Properties additionalProps = Utils.loadPropertiesFile(additionalConfig, false);
            for (Entry<Object, Object> entry : additionalProps.entrySet()) {
                configProps.setProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }

        // Perform variable substitution for system properties.
        for (Enumeration<?> e = configProps.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            configProps.setProperty(name, Utils.substVars(configProps.getProperty(name), name, null, configProps));
        }

        return configProps;
    }

    protected static void copySystemProperties(Properties configProps) {
        for (Enumeration<?> e = System.getProperties().propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (key.startsWith("felix.") ||
                    key.startsWith("karaf.") ||
                    key.startsWith("org.osgi.framework.")) {
                configProps.setProperty(key, System.getProperty(key));
            }
        }
    }

    private void updateClassLoader(Properties configProps) throws Exception {
        String framework = configProps.getProperty(KARAF_FRAMEWORK);
        if (framework == null) {
            throw new IllegalArgumentException("Property " + KARAF_FRAMEWORK + " must be set in the etc/" + configFile + " configuration file");
        }
        String bundle = configProps.getProperty(KARAF_FRAMEWORK + "." + framework);
        if (bundle == null) {
            throw new IllegalArgumentException("Property " + KARAF_FRAMEWORK + "." + framework + " must be set in the etc/" + configFile + " configuration file");
        }
        File bundleFile = new File(geronimoBase, bundle);
        if (!bundleFile.exists()) {
            bundleFile = new File(geronimoHome, bundle);
        }
        if (!bundleFile.exists()) {
            throw new FileNotFoundException(bundleFile.getAbsolutePath());
        }

        URLClassLoader classLoader = (URLClassLoader) FrameworkLauncher.class.getClassLoader();
        Method mth = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        mth.setAccessible(true);
        mth.invoke(classLoader, bundleFile.toURI().toURL());
    }

    private void startBundles(BundleContext context) throws Exception {

        // Retrieve the Start Level service, since it will be needed
        // to set the start level of the installed bundles.
        StartLevel sl = (StartLevel) context.getService(context.getServiceReference(StartLevel.class.getName()));

        // Set the default bundle start level
        int ibsl = Integer.parseInt(configProps.getProperty("karaf.startlevel.bundle", "60"));
        sl.setInitialBundleStartLevel(ibsl);

        for (BundleInfo info : startList) {
            Bundle bundle = context.installBundle(info.bundleLocation);           
            if (info.startLevel > 0) {
                sl.setBundleStartLevel(bundle, info.startLevel);
            }
            info.bundle = bundle;
        }

        for (BundleInfo info : startList) {
            if (!isFragment(info.bundle)) {
                 info.bundle.start();
            }
        }

    }
    
    protected void checkStartBundles() {
        for (BundleInfo info : startList) {
            int state = info.bundle.getState();
            if (state == Bundle.ACTIVE) {
                // bundle already is active - nothing to do
                continue; 
            } else if ((state == Bundle.RESOLVED || state == Bundle.INSTALLED) && !isFragment(info.bundle)) {
                // try to start the bundle again
                try {
                    info.bundle.start();
                } catch (Exception e) {
                    Logger.getLogger(FrameworkLauncher.class.getName()).warning("Error starting bundle " + info.bundle);
                }
            }
        }
    }
    
    private static boolean isFragment(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        return (headers != null && headers.get(Constants.FRAGMENT_HOST) != null);
    }

    private List<BundleInfo> loadStartupProperties() throws Exception {
        File etc = new File(geronimoBase, "etc");

        File file = new File(etc, startupFile);
        Properties startupProps = Utils.loadPropertiesFile(file, true);

        ArrayList<File> bundleDirs = new ArrayList<File>();

        String defaultRepo = System.getProperty(DEFAULT_REPO, "repository");
        
        File GeronimoHomeRepo = new File(geronimoHome, defaultRepo).getCanonicalFile();
        File GeronimoBaseRepo = new File(geronimoBase, defaultRepo).getCanonicalFile();

        if (GeronimoHomeRepo.getCanonicalPath().equals(GeronimoBaseRepo.getCanonicalPath())) {
            bundleDirs.add(GeronimoHomeRepo);
        } else {
            bundleDirs.add(GeronimoHomeRepo);
            bundleDirs.add(GeronimoBaseRepo);
        }

        return loadStartupProperties(startupProps, bundleDirs);
    }

    protected List<BundleInfo> loadStartupProperties(Properties startupProps, List<File> bundleDirs) {
        List<BundleInfo> startList = new ArrayList<BundleInfo>();

        for (Entry<Object, Object> entry : startupProps.entrySet()) {
            String location = (String) entry.getKey();

            File file = findFile(bundleDirs, location);

            if (file == null) {
                System.err.println("Artifact " + location + " not found");
                continue;
            }

            int level;
            try {
                level = Integer.parseInt(((String) entry.getValue()).trim());
            } catch (NumberFormatException e1) {
                System.err.print("Ignoring " + location + " (run level must be an integer)");
                continue;
            }

            BundleInfo info = new BundleInfo();
            info.location = file;
            try {
                info.bundleLocation = toReferenceFileLocation(file);
            } catch (IOException e) {
                System.err.println("Ignoring Artifact " + location + " (" + e.getMessage() + ")");
                continue;
            }
            info.startLevel = level;

            startList.add(info);
        }

        return startList;
    }

    private String toReferenceFileLocation(File file) throws IOException {
        return "reference:" + file.toURI().toString();
    }

    private static File findFile(List<File> bundleDirs, String name) {
        for (File bundleDir : bundleDirs) {
            File file = findFile(bundleDir, name);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private static File findFile(File dir, String name) {
        if (!dir.exists()) dir.mkdir();
        if (!dir.isDirectory()) {
            dir.delete();
            dir.mkdir();
        }
        
        File theFile = new File(dir, name);

        if (theFile.exists() && !theFile.isDirectory()) {
            return theFile;
        }

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                return findFile(file, name);
            }
        }

        return null;
    }

    static class BundleInfo {
        File location;
        String bundleLocation;
        int startLevel;
        Bundle bundle;
    }
    
    boolean detectServerFlag(){
        File serverFlagFile = null;
        try {
            serverFlagFile = new File(geronimoBase, "var/" + SERVER_FLAG_FILE_NAME);
            serverFlagFile.deleteOnExit();
            boolean newFile = serverFlagFile.createNewFile();
            if (!newFile){
                return true;
            }                
        } catch (IOException e) {
            System.err.println("Can not create server flag file at " + serverFlagFile.getAbsolutePath() + " ( " + e.getMessage() + " ) ");
        }
        return false;
    }
}
