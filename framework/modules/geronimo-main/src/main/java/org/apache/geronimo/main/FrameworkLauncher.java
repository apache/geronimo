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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.packageadmin.PackageAdmin;
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

    private static final Logger LOG = Logger.getLogger(FrameworkLauncher.class.getName());

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

    public void launch() throws Exception {
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

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                FrameworkLauncher.this.destroy(false);
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

        List<BundleInfo> startList = loadStartupProperties();
        startBundles(bundleContext, startList);

        framework.start();
    }

    public void destroy(boolean await) {
        try {
            destroyFramework(await);
        } catch (Exception e) {
            System.err.println("Error stopping framework: " + e);
        }

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

    private void destroyFramework(boolean await) throws BundleException, InterruptedException {
        if (framework == null) {
            return;
        }

        if (await) {
            while (true) {
                FrameworkEvent event = framework.waitForStop(0);
                if (event.getType() != FrameworkEvent.STOPPED_UPDATE) {
                    break;
                }
            }
        }
        if (framework.getState() == Bundle.ACTIVE) {
            framework.stop();
            framework.waitForStop(0);
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
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
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
            for (Entry entry : additionalProps.entrySet()) {
                configProps.setProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }

        // Perform variable substitution for system properties.
        for (Enumeration e = configProps.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            configProps.setProperty(name, Utils.substVars(configProps.getProperty(name), name, null, configProps));
        }

        return configProps;
    }

    protected static void copySystemProperties(Properties configProps) {
        for (Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements();) {
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
        mth.invoke(classLoader, bundleFile.toURL());
    }

    private void startBundles(BundleContext context, List<BundleInfo> startList) throws Exception {

        // Retrieve the Start Level service, since it will be needed
        // to set the start level of the installed bundles.
        StartLevel sl = (StartLevel) context.getService(context.getServiceReference(StartLevel.class.getName()));

        // Set the default bundle start level
        int ibsl = Integer.parseInt(configProps.getProperty("karaf.startlevel.bundle", "60"));
        sl.setInitialBundleStartLevel(ibsl);

        for (BundleInfo info : startList) {
            InputStream in = new FileInputStream(info.location);
            Bundle bundle = null;
            try {
                bundle = context.installBundle(info.mvnLocation, in);
            } finally {
                try { in.close(); } catch (Exception e) {}
            }
            if (info.startLevel > 0) {
                sl.setBundleStartLevel(bundle, info.startLevel);
            }

            info.bundle = bundle;
        }


        // Retrieve the PackageAdmin service
        PackageAdmin pa = (PackageAdmin) context.getService(context.getServiceReference(PackageAdmin.class.getName()));

        for (BundleInfo info : startList) {
            if (pa.getBundleType(info.bundle) != PackageAdmin.BUNDLE_TYPE_FRAGMENT) { //Fragment bundle can not start
                 info.bundle.start();
            }
        }

    }

    private List<BundleInfo> loadStartupProperties() throws Exception {
        File etc = new File(geronimoBase, "etc");

        File file = new File(etc, startupFile);
        Properties startupProps = Utils.loadPropertiesFile(file, true);

        ArrayList<File> bundleDirs = new ArrayList<File>();

        String defaultRepo = System.getProperty(DEFAULT_REPO, "repository");

        if (geronimoBase.equals(geronimoHome)) {
            bundleDirs.add(new File(geronimoHome, defaultRepo));
        } else {
            bundleDirs.add(new File(geronimoBase, defaultRepo));
            bundleDirs.add(new File(geronimoHome, defaultRepo));
        }

        return loadStartupProperties(startupProps, bundleDirs);
    }

    protected List<BundleInfo> loadStartupProperties(Properties startupProps, List<File> bundleDirs) {
        List<BundleInfo> startList = new ArrayList<BundleInfo>();

        for (Iterator iterator = startupProps.keySet().iterator(); iterator.hasNext();) {
            String location = (String) iterator.next();

            File file = findFile(bundleDirs, location);

            if (file == null) {
                System.err.println("Artifact " + location + " not found");
                continue;
            }

            int level;
            try {
                level = Integer.parseInt(startupProps.getProperty(location).trim());
            } catch (NumberFormatException e1) {
                System.err.print("Ignoring " + location + " (run level must be an integer)");
                continue;
            }

            String mvnLocation = toMvnUrl(location);

            BundleInfo info = new BundleInfo();
            info.location = file;
            info.mvnLocation = mvnLocation;
            info.startLevel = level;

            startList.add(info);
        }

        return startList;
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

    private static String toMvnUrl(String location) {
        String[] p = location.split("/");
        if (p.length >= 4 && p[p.length-1].startsWith(p[p.length-3] + "-" + p[p.length-2])) {
            String groupId = null;
            String artifactId = p[p.length-3];
            String version = p[p.length-2];
            String classifier;
            String type;
            String artifactIdVersion = artifactId + "-" + version;
            StringBuilder sb = new StringBuilder();
            if (p[p.length-1].charAt(artifactIdVersion.length()) == '-') {
                classifier = p[p.length-1].substring(artifactIdVersion.length() + 1, p[p.length-1].lastIndexOf('.'));
            } else {
                classifier = null;
            }
            type = p[p.length-1].substring(p[p.length-1].lastIndexOf('.') + 1);
            sb.append("mvn:");
            for (int j = 0; j < p.length - 3; j++) {
                if (j > 0) {
                    sb.append('.');
                }
                sb.append(p[j]);
            }
            sb.append('/').append(artifactId).append('/').append(version);
            if (!"jar".equals(type) || classifier != null) {
                sb.append('/');
                if (!"jar".equals(type)) {
                    sb.append(type);
                }
                if (classifier != null) {
                    sb.append('/').append(classifier);
                }
            }
            return sb.toString();
        } else {
            return location;
        }
    }

    static class BundleInfo {
        File location;
        String mvnLocation;
        int startLevel;
        Bundle bundle;
    }
}
