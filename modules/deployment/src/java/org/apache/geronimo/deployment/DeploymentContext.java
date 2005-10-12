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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.MultiParentClassLoader;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Repository;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentContext {

    private static final ClassLoader[] DEFAULT_PARENT_CLASSLOADERS = new ClassLoader[]{DeploymentContext.class.getClassLoader()};
    private final Kernel kernel;
    private final ConfigurationData configurationData;
    private final GBeanDataRegistry gbeans = new GBeanDataRegistry();
    private final File baseDir;
    private final URI baseUri;
    private final byte[] buffer = new byte[4096];
    private final List loadedAncestors = new ArrayList();
    private final List startedAncestors = new ArrayList();
//    private final ClassLoader[] parentCL;

    public DeploymentContext(File baseDir, URI configId, ConfigurationModuleType type, List parentID, Kernel kernel) throws DeploymentException {
        this(baseDir, configId, type, parentID, null, null, kernel);
    }

    public DeploymentContext(File baseDir, URI configId, ConfigurationModuleType type, List parentId, String domain, String server, Kernel kernel) throws DeploymentException {
        assert baseDir != null: "baseDir is null";
        assert configId != null: "configID is null";
        assert type != null: "type is null";

        this.kernel = kernel;

        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        if (!baseDir.isDirectory()) {
            throw new DeploymentException("Base directory is not a directory: " + baseDir.getAbsolutePath());
        }
        this.baseDir = baseDir;
        this.baseUri = baseDir.toURI();

        configurationData = new ConfigurationData();
        configurationData.setId(configId);
        configurationData.setModuleType(type);
        configurationData.setParentId(parentId);
        configurationData.setDomain(domain);
        configurationData.setServer(server);
        determineNaming();
    }

    private void determineNaming() throws DeploymentException {
        if (configurationData.getDomain() != null && configurationData.getServer() != null) {
            return;
        }
        List parentId = configurationData.getParentId();
        if (kernel == null || parentId == null || parentId.isEmpty()) {
            throw new DeploymentException("neither domain and server nor any way to determine them was provided for configuration " + configurationData.getId());
        }
        URI parent = (URI) parentId.get(0);
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);

        try {
            boolean loaded = false;
            if (!configurationManager.isLoaded(parent)) {
                configurationManager.load(parent);
                loaded = true;
            }
            try {
                ObjectName parentName = Configuration.getConfigurationObjectName(parent);
                configurationData.setDomain((String) kernel.getAttribute(parentName, "domain"));
                configurationData.setServer((String) kernel.getAttribute(parentName, "server"));
            } catch (Exception e) {
                throw new DeploymentException("Unable to copy domain and server from parent configuration", e);
            } finally {
                if (loaded) {
                    //we need to unload again so the loadedAncestors list will be in the correct order to start configs.
                    configurationManager.unload(parent);
                }
            }
        } catch (Exception e) {
            throw new DeploymentException("Unable to load first parent of configuration " + configurationData.getId(), e);
        } finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }

        //check that domain and server are now known
        if (configurationData.getDomain() == null || configurationData.getServer() == null) {
            throw new IllegalStateException("Domain or server could not be determined from explicit args or parent configuration. ParentID: " + parentId
                    + ", domain: " + configurationData.getDomain()
                    + ", server: " + configurationData.getServer());
        }
    }


    public URI getConfigID() {
        return configurationData.getId();
    }

    public void addParentId(List parentId) {
        configurationData.getParentId().addAll(parentId);
    }

    public ConfigurationModuleType getType() {
        return configurationData.getModuleType();
    }

    public File getBaseDir() {
        return baseDir;
    }

    public String getDomain() {
        return configurationData.getDomain();
    }

    public String getServer() {
        return configurationData.getServer();
    }

    public void addGBean(GBeanData gbean) {
        assert gbean.getName() != null: "GBean name is null";
        gbeans.register(gbean);
    }

    public Set getGBeanNames() {
        return gbeans.getGBeanNames();
    }

    public Set listGBeans(ObjectName pattern) {
        return gbeans.listGBeans(pattern);
    }

    public GBeanData getGBeanInstance(ObjectName name) throws GBeanNotFoundException {
        return gbeans.getGBeanInstance(name);
    }

    public void addDependency(URI uri) {
        configurationData.addDependency(uri);
    }

    /**
     * Copy a packed jar file into the deployment context and place it into the
     * path specified in the target path.  The newly added packed jar is added
     * to the classpath of the configuration.
     * <p/>
     * NOTE: The class loader that is obtained from this deployment context
     * may get out of sync with the newly augmented classpath; obtain a freshly
     * minted class loader by calling <code>getConfigurationClassLoader</code> method.
     *
     * @param targetPath where the packed jar file should be placed
     * @param jarFile    the jar file to copy
     * @throws IOException if there's a problem copying the jar file
     */
    public void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException {
        File targetFile = getTargetFile(targetPath);
        DeploymentUtil.copyToPackedJar(jarFile, targetFile);
        configurationData.addClassPathLocation(targetPath);
    }

    /**
     * Copy a ZIP file entry into the deployment context and place it into the
     * path specified in the target path.  The newly added entry is added
     * to the classpath of the configuration.
     * <p/>
     * NOTE: The class loader that is obtained from this deployment context
     * may get out of sync with the newly augmented classpath; obtain a freshly
     * minted class loader by calling <code>getConfigurationClassLoader</code> method.
     *
     * @param targetPath where the ZIP file entry should be placed
     * @param zipFile    the ZIP file
     * @param zipEntry   the ZIP file entry
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, zipFile, zipEntry);
        configurationData.addClassPathLocation(targetPath);
    }

    /**
     * Copy a file into the deployment context and place it into the
     * path specified in the target path.  The newly added file is added
     * to the classpath of the configuration.
     * <p/>
     * NOTE: The class loader that is obtained from this deployment context
     * may get out of sync with the newly augmented classpath; obtain a freshly
     * minted class loader by calling <code>getConfigurationClassLoader</code> method.
     *
     * @param targetPath where the file should be placed
     * @param source     the URL of file to be copied
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, URL source) throws IOException {
        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, source);
        configurationData.addClassPathLocation(targetPath);
    }

    /**
     * Copy a file into the deployment context and place it into the
     * path specified in the target path.  The newly added file is added
     * to the classpath of the configuration.
     * <p/>
     * NOTE: The class loader that is obtained from this deployment context
     * may get out of sync with the newly augmented classpath; obtain a freshly
     * minted class loader by calling <code>getConfigurationClassLoader</code> method.
     *
     * @param targetPath where the file should be placed
     * @param source     the file to be copied
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, File source) throws IOException {
        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, source);
        configurationData.addClassPathLocation(targetPath);
    }

    /**
     * Import the classpath from a jar file's manifest.  The imported classpath
     * is crafted relative to <code>moduleBaseUri</code>.
     * <p/>
     * NOTE: The class loader that is obtained from this deployment context
     * may get out of sync with the newly augmented classpath; obtain a freshly
     * minted class loader by calling <code>getConfigurationClassLoader</code> method.
     *
     * @param moduleFile    the jar file from which the manifest is obtained.
     * @param moduleBaseUri the base for the imported classpath
     * @throws DeploymentException if there is a problem with the classpath in
     *                             the manifest
     */
    public void addManifestClassPath(JarFile moduleFile, URI moduleBaseUri) throws DeploymentException {
        Manifest manifest = null;
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

            URI targetUri = moduleBaseUri.resolve(pathUri);
            configurationData.addClassPathLocation(targetUri);
        }
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

    public void addClass(URI location, String fqcn, byte[] bytes, boolean addToClasspath) throws IOException, URISyntaxException {
        assert location.toString().endsWith("/");

        if (addToClasspath) {
            configurationData.addClassPathLocation(location);
        }
        String classFileName = fqcn.replace('.', '/') + ".class";
        addFile(getTargetFile(new URI(location.toString() + classFileName)), new ByteArrayInputStream(bytes));
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
        assert !targetPath.isAbsolute() : "targetPath is absolute";
        assert !targetPath.isOpaque() : "targetPath is opaque";
        return new File(baseUri.resolve(targetPath));
    }

    static interface ParentSource {
        Collection getParents(URI point) throws DeploymentException;
    }

    List getExtremalSet(Collection points, ParentSource parentSource) throws DeploymentException {
        LinkedHashMap pointToEnvelopeMap = new LinkedHashMap();
        for (Iterator iterator = points.iterator(); iterator.hasNext();) {
            URI newPoint = (URI) iterator.next();
            Set newEnvelope = new HashSet();
            getEnvelope(newPoint, parentSource, newEnvelope);
            boolean useMe = true;
            for (Iterator iterator1 = pointToEnvelopeMap.entrySet().iterator(); iterator1.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator1.next();
                Set existingEnvelope = (Set) entry.getValue();
                if (existingEnvelope.contains(newPoint)) {
                    useMe = false;
                } else if (newEnvelope.contains(entry.getKey())) {
                    iterator1.remove();
                }
            }
            if (useMe) {
                pointToEnvelopeMap.put(newPoint, newEnvelope);
            }
        }
        return new ArrayList(pointToEnvelopeMap.keySet());
    }

    private void getEnvelope(URI point, ParentSource parentSource, Set envelope) throws DeploymentException {
        Collection newPoints = parentSource.getParents(point);
        envelope.addAll(newPoints);
        for (Iterator iterator = newPoints.iterator(); iterator.hasNext();) {
            URI newPoint = (URI) iterator.next();
            getEnvelope(newPoint, parentSource, envelope);
        }
    }

    static class ConfigurationParentSource implements ParentSource {

        private final Kernel kernel;

        public ConfigurationParentSource(Kernel kernel) {
            this.kernel = kernel;
        }

        public Collection getParents(URI configID) throws DeploymentException {
            ObjectName configName;
            try {
                configName = Configuration.getConfigurationObjectName(configID);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Cannot convert ID to ObjectName: ", e);
            }
            try {
                URI[] parents = (URI[]) kernel.getAttribute(configName, "parentId");
                if (parents == null) {
                    return Collections.EMPTY_LIST;
                } else {
                    return Arrays.asList(parents);
                }
            } catch (Exception e) {
                throw new DeploymentException("Cannot find parents of alleged config: ", e);
            }
        }

    }

    private ClassLoader[] determineParents() throws DeploymentException {
        ClassLoader[] parentCL;
        List parentId = configurationData.getParentId();
        if (kernel != null && parentId != null && parentId.size() > 0) {
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            try {
                loadAncestors(kernel, parentId, loadedAncestors, configurationManager);
                ParentSource parentSource = new ConfigurationParentSource(kernel);
                parentId = getExtremalSet(parentId, parentSource);
                configurationData.setParentId(parentId);

                try {
                    for (Iterator iterator = parentId.iterator(); iterator.hasNext();) {
                        URI uri = (URI) iterator.next();
                        ObjectName ancestorName = Configuration.getConfigurationObjectName(uri);
                        List started = new ArrayList();
                        startAncestors(ancestorName, kernel, started, configurationManager);
                        startedAncestors.addAll(started);
                    }
                } catch (DeploymentException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeploymentException(e);
                }
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            }
            try {
                parentCL = new ClassLoader[parentId.size()];
                for (int i = 0; i < parentId.size(); i++) {
                    URI uri = (URI) parentId.get(i);
                    ObjectName parentName = Configuration.getConfigurationObjectName(uri);
                    parentCL[i] = (ClassLoader) kernel.getAttribute(parentName, "configurationClassLoader");
                }

            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            // no explicit parent set, so use the class loader of this class as
            // the parent... this class should be in the root geronimo classloader,
            // which is normally the system class loader but not always, so be safe
            parentCL = DEFAULT_PARENT_CLASSLOADERS;
        }


        return parentCL;
    }

    private void loadAncestors(Kernel kernel, List parentId, List loadedAncestors, ConfigurationManager configurationManager) throws DeploymentException {
        if (kernel != null && parentId != null) {
            try {
                for (Iterator iterator = parentId.iterator(); iterator.hasNext();) {
                    URI uri = (URI) iterator.next();
                    loadedAncestors.addAll(configurationManager.loadRecursive(uri));
                }
            } catch (Exception e) {
                throw new DeploymentException("Unable to load parents", e);
            }
        }
    }

    private void startAncestors(ObjectName name, Kernel kernel, List started, ConfigurationManager configurationManager) throws Exception {
        if (name != null && !isRunning(kernel, name)) {
            URI[] patterns = (URI[]) kernel.getGBeanData(name).getAttribute("parentId");
            if (patterns != null) {
                for (int i = 0; i < patterns.length; i++) {
                    URI pattern = patterns[i];
                    ObjectName ancestorName = Configuration.getConfigurationObjectName(pattern);
                    startAncestors(ancestorName, kernel, started, configurationManager);
                }
            }
            configurationManager.loadGBeans(name);
            started.add(name);
        }
    }

    private static boolean isRunning(Kernel kernel, ObjectName name) throws Exception {
        return State.RUNNING_INDEX == kernel.getGBeanState(name);
    }

    public ClassLoader getClassLoader(Repository repository) throws DeploymentException {
        ClassLoader[] parentCL = determineParents();
        // shouldn't user classpath come before dependencies?
        List dependencies = configurationData.getDependencies();
        List classPath = configurationData.getClassPath();
        URL[] urls = new URL[dependencies.size() + classPath.size()];
        try {
            int index = 0;
            for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
                URI uri = (URI) iterator.next();
                urls[index++] = repository.getURL(uri);
            }

            for (Iterator i = classPath.iterator(); i.hasNext();) {
                URI path = (URI) i.next();
                urls[index++] = getTargetFile(path).toURL();
            }
        } catch (MalformedURLException e) {
            throw new DeploymentException(e);
        }

        return new MultiParentClassLoader(configurationData.getId(), urls, parentCL);
    }

    public void close() throws IOException, DeploymentException {
        if (kernel != null) {
            if (startedAncestors != null) {
                //stopping one stops all it's children.
                //doesn't work though.
                Collections.reverse(startedAncestors);
                for (Iterator iterator = startedAncestors.iterator(); iterator.hasNext();) {
                    ObjectName objectName = (ObjectName) iterator.next();

                    try {
                        kernel.stopGBean(objectName);
                    } catch (GBeanNotFoundException e) {
                        throw new DeploymentException("Could not find a configuration we previously started! " + objectName, e);
                    }
                }
                startedAncestors.clear();
            }
            if (loadedAncestors != null) {
                Collections.reverse(loadedAncestors);
                for (Iterator iterator = loadedAncestors.iterator(); iterator.hasNext();) {
                    ObjectName objectName = (ObjectName) iterator.next();
                    try {
                        kernel.unloadGBean(objectName);
                    } catch (GBeanNotFoundException e) {
                        throw new DeploymentException("Could not find a configuration we previously loaded! " + objectName, e);
                    }
                }
                loadedAncestors.clear();
            }
        }
    }

    public void addChildConfiguration(ConfigurationData configurationData) {
        this.configurationData.addChildConfiguration(configurationData);
    }

    public ConfigurationData getConfigurationData() {
        ConfigurationData configurationData = new ConfigurationData(this.configurationData);
        configurationData.setGBeans(Arrays.asList(gbeans.getGBeans()));
        return configurationData;
    }

    /**
     * @return a copy of the configurations GBeanData
     * @deprecated Currently used only in some tests, and may not be appropriate as a public method.
     */
    public GBeanData getConfigurationGBeanData() throws MalformedObjectNameException, InvalidConfigException {
        URI id = configurationData.getId();
        GBeanData config = new GBeanData(Configuration.getConfigurationObjectName(id), Configuration.GBEAN_INFO);
        config.setAttribute("id", id);
        config.setAttribute("type", configurationData.getModuleType());
        config.setAttribute("domain", configurationData.getDomain());
        config.setAttribute("server", configurationData.getServer());

        List parentId = configurationData.getParentId();
        if (parentId != null) {
            config.setAttribute("parentId", parentId.toArray(new URI[parentId.size()]));
        }

        config.setAttribute("gBeanState", Configuration.storeGBeans(gbeans.getGBeans()));
        config.setReferencePatterns("Repositories", Collections.singleton(new ObjectName("*:name=Repository,*")));
        config.setAttribute("dependencies", configurationData.getDependencies());
        config.setAttribute("classPath", configurationData.getClassPath());

        return config;
    }

}
