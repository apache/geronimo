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

package org.apache.geronimo.deployment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev: 384933 $ $Date$
 */
public class DeploymentContext {
    private static int deploymentCount = 42;

    private final File baseDir;
    private final URI baseUri;
    private final byte[] buffer = new byte[4096];
    private final List childConfigurationDatas = new ArrayList();
    private final ConfigurationManager configurationManager;
    private final Configuration configuration;
    private final Environment environment;

    public DeploymentContext(File baseDir, Environment environment, ConfigurationModuleType moduleType, Kernel kernel) throws DeploymentException {
        this(baseDir,
                environment,
                moduleType,
                ConfigurationUtil.getConfigurationManager(kernel));
    }

    public DeploymentContext(File baseDir, Environment environment, ConfigurationModuleType moduleType, ConfigurationManager configurationManager) throws DeploymentException {
        this(createTempConfiguration(environment, moduleType, baseDir, configurationManager),
                baseDir,
                environment,
                moduleType,
                configurationManager);
    }

    public DeploymentContext(Configuration configuration, File baseDir) throws DeploymentException {
        this(configuration,
                baseDir,
                configuration.getEnvironment(),
                configuration.getModuleType(),
                null);
    }

    private DeploymentContext(Configuration configuration, File baseDir, Environment environment, ConfigurationModuleType moduleType, ConfigurationManager configurationManager) throws DeploymentException {
        if (baseDir == null) throw new NullPointerException("baseDir is null");
        if (environment == null) throw new NullPointerException("environment is null");
        if (moduleType == null) throw new NullPointerException("type is null");
        if (configuration == null) throw new NullPointerException("configuration is null");

        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        if (!baseDir.isDirectory()) {
            throw new DeploymentException("Base directory is not a directory: " + baseDir.getAbsolutePath());
        }
        this.baseDir = baseDir;
        this.baseUri = baseDir.toURI();

        this.environment = environment;
        this.configurationManager = configurationManager;
        this.configuration = configuration;
    }

    private static Configuration createTempConfiguration(Environment environment, ConfigurationModuleType moduleType, File baseDir, ConfigurationManager configurationManager) throws DeploymentException {
        // create a new environment object for use in our temporary configuration
        // NOTE: the configuration class will resolve all dependencies and set them
        // back into this environment object, so don't use this environment for the
        // final configuration data
        Environment deploymentEnvironment = new Environment(environment);

        // use a modified configuration id for the configuration object in case this
        // configuation is already running in the server
        Artifact id = environment.getConfigId();
        synchronized (DeploymentContext.class) {
            id = new Artifact("geronimo-deployment", id.getArtifactId(), "" + deploymentCount++, id.getType());
        }
        deploymentEnvironment.setConfigId(id);

        // Add a new temporary configuration to hold our data
        ConfigurationData configurationData = new ConfigurationData(moduleType, null, null, null, deploymentEnvironment, baseDir);
        try {
            return configurationManager.loadConfiguration(configurationData, new DeploymentContextConfigurationStore(baseDir));
        } catch (Exception e) {
            throw new DeploymentException("Unable to create configuration for deployment", e);
        }
    }

    public Artifact getConfigID() {
        return configuration.getId();
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void addGBean(GBeanData gbean) throws GBeanAlreadyExistsException {
        if (gbean == null) throw new NullPointerException("gbean is null");
        if (gbean.getName() == null) throw new NullPointerException("gbean.getName() is null");
        configuration.addGBean(gbean);
    }

    public Set getGBeanNames() {
        return new HashSet(configuration.getGBeans().keySet());
    }

    /**
     * @deprecated use findGBeans(pattern)
     */
    public Set listGBeans(AbstractNameQuery pattern) {
        return findGBeans(pattern);
    }

    public AbstractName findGBean(AbstractNameQuery pattern) throws GBeanNotFoundException {
        return configuration.findGBean(pattern);
    }

    public AbstractName findGBean(Set patterns) throws GBeanNotFoundException {
        return configuration.findGBean(patterns);
    }

    public LinkedHashSet findGBeans(AbstractNameQuery pattern) {
        return configuration.findGBeans(pattern);
    }

    public LinkedHashSet findGBeans(Set patterns) {
        return configuration.findGBeans(patterns);
    }

    public GBeanData getGBeanInstance(AbstractName name) throws GBeanNotFoundException {
        Map gbeans = configuration.getGBeans();
        GBeanData gbeanData = (GBeanData) gbeans.get(name);
        if (gbeanData == null) {
            throw new GBeanNotFoundException(name);
        }
        return gbeanData;
    }

    /**
     * Copy a packed jar file into the deployment context and place it into the
     * path specified in the target path.  The newly added packed jar is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the packed jar file should be placed
     * @param jarFile the jar file to copy
     * @throws IOException if there's a problem copying the jar file
     */
    public void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException {
        if (targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must not end with a '/' character: " + targetPath);

        File targetFile = getTargetFile(targetPath);
        DeploymentUtil.copyToPackedJar(jarFile, targetFile);

        if (!targetFile.isFile()) throw new IllegalStateException("target file should be a file: " + targetFile);
        configuration.addToClassPath(targetPath);
    }

    /**
     * Copy a ZIP file entry into the deployment context and place it into the
     * path specified in the target path.  The newly added entry is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the ZIP file entry should be placed
     * @param zipFile the ZIP file
     * @param zipEntry the ZIP file entry
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        if (!targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must end with a '/' character: " + targetPath);

        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, zipFile, zipEntry);

        if (!targetFile.isDirectory()) throw new IllegalStateException("target file should be a directory: " + targetFile);
        configuration.addToClassPath(targetPath);
    }

    /**
     * Copy a file into the deployment context and place it into the
     * path specified in the target path.  The newly added file is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the file should be placed
     * @param source     the URL of file to be copied
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, URL source) throws IOException {
        if (targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must not end with a '/' character: " + targetPath);

        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, source);

        if (!targetFile.isFile()) throw new IllegalStateException("target file should be a file: " + targetFile);
        configuration.addToClassPath(targetPath);
    }

    /**
     * Copy a file into the deployment context and place it into the
     * path specified in the target path.  The newly added file is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the file should be placed
     * @param source     the file to be copied
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, File source) throws IOException {
        if (targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must not end with a '/' character: " + targetPath);

        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, source);

        if (!targetFile.isFile()) throw new IllegalStateException("target file should be a file: " + targetFile);
        configuration.addToClassPath(targetPath);
    }

    /**
     * Import the classpath from a jar file's manifest.  The imported classpath
     * is crafted relative to <code>moduleBaseUri</code>.
     *
     * @param moduleFile    the jar file from which the manifest is obtained.
     * @param moduleBaseUri the base for the imported classpath
     * @throws DeploymentException if there is a problem with the classpath in
     *                             the manifest
     */
    public void addManifestClassPath(JarFile moduleFile, URI moduleBaseUri) throws DeploymentException {
        Manifest manifest;
        try {
            manifest = moduleFile.getManifest();
        } catch (IOException e) {
            throw new DeploymentException("Could not read manifest: " + moduleBaseUri);
        }

        if (manifest == null) {
            return;
        }
        String manifestClassPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        if (manifestClassPath == null) {
            return;
        }

        for (StringTokenizer tokenizer = new StringTokenizer(manifestClassPath, " "); tokenizer.hasMoreTokens();) {
            String path = tokenizer.nextToken();

            URI pathUri;
            try {
                pathUri = new URI(path);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid manifest classpath entry: module=" + moduleBaseUri + ", path=" + path);
            }

            if (!pathUri.getPath().endsWith(".jar")) {
                throw new DeploymentException("Manifest class path entries must end with the .jar extension (J2EE 1.4 Section 8.2): module=" + moduleBaseUri);
            }
            if (pathUri.isAbsolute()) {
                throw new DeploymentException("Manifest class path entries must be relative (J2EE 1.4 Section 8.2): moduel=" + moduleBaseUri);
            }

            try {
                URI targetUri = moduleBaseUri.resolve(pathUri);
                if (targetUri.getPath().endsWith("/")) throw new IllegalStateException("target path must not end with a '/' character: " + targetUri);
                configuration.addToClassPath(targetUri);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        }
    }

    public void addClass(URI targetPath, String fqcn, byte[] bytes) throws IOException, URISyntaxException {
        if (!targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must end with a '/' character: " + targetPath);

        String classFileName = fqcn.replace('.', '/') + ".class";

        File targetFile = getTargetFile(new URI(targetPath.toString() + classFileName));
        addFile(targetFile, new ByteArrayInputStream(bytes));

        configuration.addToClassPath(targetPath);
    }

    public void addFile(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        addFile(getTargetFile(targetPath), zipFile, zipEntry);
    }

    public void addFile(URI targetPath, URL source) throws IOException {
        addFile(getTargetFile(targetPath), source);
    }

    public void addFile(URI targetPath, File source) throws IOException {
        addFile(getTargetFile(targetPath), source);
    }

    public void addFile(URI targetPath, String source) throws IOException {
        addFile(getTargetFile(targetPath), new ByteArrayInputStream(source.getBytes()));
    }

    private void addFile(File targetFile, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        if (zipEntry.isDirectory()) {
            targetFile.mkdirs();
        } else {
            InputStream is = zipFile.getInputStream(zipEntry);
            try {
                addFile(targetFile, is);
            } finally {
                DeploymentUtil.close(is);
            }
        }
    }

    private void addFile(File targetFile, URL source) throws IOException {
        InputStream in = null;
        try {
            in = source.openStream();
            addFile(targetFile, in);
        } finally {
            DeploymentUtil.close(in);
        }
    }

    private void addFile(File targetFile, File source) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(source);
            addFile(targetFile, in);
        } finally {
            DeploymentUtil.close(in);
        }
    }

    private void addFile(File targetFile, InputStream source) throws IOException {
        targetFile.getParentFile().mkdirs();
        OutputStream out = null;
        try {
            out = new FileOutputStream(targetFile);
            int count;
            while ((count = source.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
        } finally {
            DeploymentUtil.close(out);
        }
    }

    public File getTargetFile(URI targetPath) {
        if (targetPath == null) throw new NullPointerException("targetPath is null");
        if (targetPath.isAbsolute()) throw new IllegalArgumentException("targetPath is absolute");
        if (targetPath.isOpaque()) throw new IllegalArgumentException("targetPath is opaque");
        return new File(baseUri.resolve(targetPath));
    }

    public ClassLoader getClassLoader() throws DeploymentException {
        return configuration.getConfigurationClassLoader();
    }

    public Configuration getConfiguration(Configuration knownParent) {
        return configuration;
    }

    public void close() throws IOException, DeploymentException {
        if (configurationManager != null) {
            try {
                configurationManager.unloadConfiguration(configuration);
            } catch (NoSuchConfigException ignored) {
            }
        }
    }

    public void addChildConfiguration(ConfigurationData configurationData) {
        childConfigurationDatas.add(configurationData);
    }

    public ConfigurationData getConfigurationData() {
        //
        // DO NOT use the environment in the configuration, it is modifed by the configuration
        //
        ConfigurationData configurationData = new ConfigurationData(configuration.getModuleType(),
                new LinkedHashSet(configuration.getClassPath()),
                new ArrayList(configuration.getGBeans().values()),
                childConfigurationDatas,
                environment,
                baseDir);
        return configurationData;
    }

    private static class DeploymentContextConfigurationStore implements ConfigurationStore {
        private final File baseDir;

        public DeploymentContextConfigurationStore(File baseDir) {
            this.baseDir = baseDir;
        }

        public void install(ConfigurationData configurationData) {
        }

        public void uninstall(Artifact configID) {
        }

        public GBeanData loadConfiguration(Artifact configId) {
            return null;
        }

        public boolean containsConfiguration(Artifact configID) {
            return false;
        }

        public String getObjectName() {
            return null;
        }

        public List listConfigurations() {
            return null;
        }

        public File createNewConfigurationDir(Artifact configId) {
            return null;
        }

        public URL resolve(Artifact configId, URI uri) throws MalformedURLException {
            return new File(baseDir, uri.toString()).toURL();
        }
    }
}
