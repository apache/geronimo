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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentContext {
    private final URI configID;
    /**
     * Identifies the type of configuration (WAR, RAR, EAR et cetera)
     */
    private final ConfigurationModuleType type;
    private final Kernel kernel;
    private final GBeanMBean config;
    private final Map gbeans = new HashMap();
    private final Set dependencies = new LinkedHashSet();
    private final LinkedHashSet classPath = new LinkedHashSet();
    private final File baseDir;
    private final URI baseUri;
    private final byte[] buffer = new byte[4096];
    private final List ancestors;
    private final ClassLoader parentCL;

    public DeploymentContext(File baseDir, URI configID, ConfigurationModuleType type, URI parentID, Kernel kernel) throws MalformedObjectNameException, DeploymentException {
        assert baseDir != null: "baseDir is null";
        assert configID != null: "configID is null";
        assert type != null: "type is null";

        this.configID = configID;
        this.type = type;
        this.kernel = kernel;

        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        if (!baseDir.isDirectory()) {
            throw new DeploymentException("Base directory is not a directory: " + baseDir.getAbsolutePath());
        }
        this.baseDir = baseDir;
        this.baseUri = baseDir.toURI();

        config = new GBeanMBean(Configuration.GBEAN_INFO);

        try {
            config.setAttribute("ID", configID);
            config.setAttribute("type", type);
            config.setAttribute("parentID", parentID);
        } catch (Exception e) {
            // we created this GBean ...
            throw new AssertionError();
        }

        if (kernel != null && parentID != null) {
            ConfigurationManager configurationManager = kernel.getConfigurationManager();
            ObjectName parentName = Configuration.getConfigurationObjectName(parentID);
            config.setReferencePatterns("Parent", Collections.singleton(parentName));
            try {
                ancestors = configurationManager.loadRecursive(parentID);
            } catch (Exception e) {
                throw new DeploymentException("Unable to load parents", e);
            }

            for (Iterator i = ancestors.iterator(); i.hasNext();) {
                ObjectName name = (ObjectName) i.next();
                try {
                    // start the config to get the classloaders going,
                    // by not specfying startRecursive none of the GBeans should start
                    kernel.startGBean(name);
                } catch (Exception e) {
                    throw new DeploymentException(e);
                }
            }
            try {
                parentCL = (ClassLoader) kernel.getAttribute(parentName, "classLoader");
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            ancestors = null;
            // no explicit parent set, so use the class loader of this class as
            // the parent... this class should be in the root geronimo classloader,
            // which is normally the system class loader but not always, so be safe
            parentCL = getClass().getClassLoader();
        }
    }

    public URI getConfigID() {
        return configID;
    }

    public ConfigurationModuleType getType() {
        return type;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void addGBean(ObjectName name, GBeanMBean gbean) {
        gbeans.put(name, gbean);
    }

    public void addGBean(GBeanData gbean, ClassLoader classLoader) {
        GBeanMBean gbeanMBean = new GBeanMBean(gbean, classLoader);
        gbeans.put(gbean.getName(), gbeanMBean);
    }

    public void addDependency(URI uri) {
        dependencies.add(uri);
    }

    public void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException {
        File targetFile = getTargetFile(targetPath);
        DeploymentUtil.copyToPackedJar(jarFile, targetFile);
        classPath.add(targetPath);
    }

    public void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, zipFile, zipEntry);
        classPath.add(targetPath);
    }

    public void addInclude(URI targetPath, URL source) throws IOException {
        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, source);
        classPath.add(targetPath);
    }

    public void addInclude(URI targetPath, File source) throws IOException {
        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, source);
        classPath.add(targetPath);
    }

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
            classPath.add(targetUri);
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

    public ClassLoader getClassLoader(Repository repository) throws DeploymentException {
        // save the dependencies and classpath
        try {
            config.setReferencePatterns("Repositories", Collections.singleton(new ObjectName("*:role=Repository,*")));
            config.setAttribute("dependencies", new ArrayList(dependencies));
            config.setAttribute("classPath", new ArrayList(classPath));
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize Configuration", e);
        }

        // shouldn't user classpath come before dependencies?
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

        return new URLClassLoader(urls, parentCL);
    }

    public void close() throws IOException, DeploymentException {
        saveConfiguration();

        if (kernel != null && ancestors != null && ancestors.size() > 0) {
            try {
                kernel.stopGBean((ObjectName) ancestors.get(0));
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }
    }

    private void saveConfiguration() throws IOException, DeploymentException {
        // persist all the GBeans in this Configuration
        try {
            config.setAttribute("gBeanState", Configuration.storeGBeans(gbeans));
        } catch (Exception e) {
            throw new DeploymentException("Unable to persist GBeans", e);
        }

        // save the persisted form in the archive
        File metaInf = new File(baseDir, "META-INF");
        metaInf.mkdirs();
        File configSer = new File(metaInf, "config.ser");

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(configSer));
            try {
                GBeanData gbeanData = config.getGBeanData();
                gbeanData.setName(Configuration.getConfigurationObjectName(configID));
                gbeanData.writeExternal(out);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new DeploymentException("Unable to save Configuration state", e);
            }
        } finally {
            DeploymentUtil.flush(out);
            DeploymentUtil.close(out);
        }
    }
}
