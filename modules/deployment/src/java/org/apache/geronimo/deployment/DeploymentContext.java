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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.repository.Repository;

/**
 *
 *
 * @version $Revision: 1.9 $ $Date: 2004/03/10 09:58:48 $
 */
public class DeploymentContext {
    private final URI configID;
    private final Kernel kernel;
    private final GBeanMBean config;
    private final Map gbeans = new HashMap();
    private final Set dependencies = new LinkedHashSet();
    private final Set classPath = new LinkedHashSet();
    private final Map includes = new HashMap();
    private final JarOutputStream jos;
    private final byte[] buffer = new byte[4096];
    private final List ancestors;
    private final ClassLoader parentCL;
    private final Collection tmpfiles = new ArrayList();

    public DeploymentContext(JarOutputStream jos, URI id, URI parentID, Kernel kernel) throws IOException, MalformedObjectNameException, DeploymentException {
        this.configID = id;
        this.jos = jos;
        this.kernel = kernel;

        config = new GBeanMBean(Configuration.GBEAN_INFO);

        try {
            config.setAttribute("ID", id);
            config.setAttribute("ParentID", parentID);
        } catch (Exception e) {
            // we created this GBean ...
            throw new AssertionError();
        }

        if (kernel != null && parentID != null) {
            ObjectName parentName = ConfigurationManager.getConfigObjectName(parentID);
            config.setReferencePatterns("Parent", Collections.singleton(parentName));
            try {
                ancestors = kernel.getConfigurationManager().loadRecursive(parentID);
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
                parentCL = (ClassLoader) kernel.getAttribute(parentName, "ClassLoader");
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

    public void addGBean(ObjectName name, GBeanMBean gbean) {
        gbeans.put(name, gbean);
    }

    public void addDependency(URI uri) {
        dependencies.add(uri);
    }

    public void addInclude(URI path, URL url) throws IOException {
        InputStream is = url.openStream();
        try {
            addFile(path, is);
        } finally {
            is.close();
        }
        addToClassPath(path, url);
    }

    public void addStreamInclude(URI path, InputStream is) throws IOException {
        File tmp = FileUtil.toTempFile(is);
        addInclude(path, tmp.toURL());
        tmpfiles.add(tmp);

    }

    public void addArchive(URI path, ZipInputStream archive) throws IOException {
        ZipEntry src;
        while ((src = archive.getNextEntry()) != null) {
            URI target = path.resolve(src.getName());
            addFile(target, archive);
        }
    }

    //This method was once public.  It appears to be useless in most cases so I made it private.
    //Deploying from a stream (usual jsr 88 case) there is no URL to map unless the stream
    //contents are copied to a temp file.
    private void addToClassPath(URI path, URL url) {
        classPath.add(path);
        includes.put(path, url);
    }

    public ClassLoader getClassLoader(Repository repository) throws DeploymentException {
        // save the dependencies and classpath
        try {
            config.setReferencePatterns("Repositories", Collections.singleton(new ObjectName("*:role=Repository,*")));
            config.setAttribute("Dependencies", new ArrayList(dependencies));
            config.setAttribute("ClassPath", new ArrayList(classPath));
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize Configuration", e);
        }

        URL[] urls = new URL[dependencies.size() + classPath.size()];
        int j = 0;
        for (Iterator i = dependencies.iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            try {
                urls[j++] = repository.getURL(uri);
            } catch (MalformedURLException e) {
                throw new DeploymentException(e);
            }
        }

        for (Iterator i = classPath.iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            urls[j++] = (URL) includes.get(uri);
        }
        return new URLClassLoader(urls, parentCL);
    }

    public void addFile(URI path, InputStream source) throws IOException {
        if (jos == null) {
            throw new IllegalStateException();
        }
        jos.putNextEntry(new ZipEntry(path.getPath()));
        int count;
        while ((count = source.read(buffer)) > 0) {
            jos.write(buffer, 0, count);
        }
        jos.closeEntry();
    }

    public void close() throws IOException, DeploymentException {
        if (jos == null) {
            throw new IllegalStateException();
        }
        saveConfiguration();
        jos.flush();
        jos.close();

        for (Iterator iterator = tmpfiles.iterator(); iterator.hasNext();) {
            try {
                ((File) iterator.next()).delete();
            } catch (Exception e) {
            }
        }

        if (kernel != null && ancestors != null && ancestors.size() > 0) {
            try {
                kernel.stopGBean((ObjectName) ancestors.get(0));
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }
    }

    private void saveConfiguration() throws IOException, DeploymentException {
        if (jos == null) {
            throw new IllegalStateException();
        }

        // persist all the GBeans in this Configuration
        try {
            config.setAttribute("GBeanState", Configuration.storeGBeans(gbeans));
        } catch (Exception e) {
            throw new DeploymentException("Unable to persist GBeans", e);
        }

        // save the persisted form in the archive
        jos.putNextEntry(new ZipEntry("META-INF/config.ser"));
        ObjectOutputStream oos = new ObjectOutputStream(jos);
        try {
            Configuration.storeGMBeanState(config, oos);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException("Unable to save Configuration state", e);
        }
        oos.flush();
        jos.closeEntry();
    }
}
