/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Repository;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/20 16:18:14 $
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

    public DeploymentContext(JarOutputStream jos, URI id, URI parentID, Kernel kernel) throws IOException,MalformedObjectNameException, DeploymentException {
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

        if (parentID != null) {
            ObjectName parentName = Kernel.getConfigObjectName(parentID);
            config.setReferencePatterns("Parent", Collections.singleton(parentName));
            try {
                ancestors = kernel.loadRecursive(parentID);
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
                parentCL = (ClassLoader) kernel.getMBeanServer().getAttribute(parentName, "ClassLoader");
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            ancestors = null;
            parentCL = ClassLoader.getSystemClassLoader();
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
        classPath.add(path);
        includes.put(path, url);
    }

    public void addArchive(URI path, ZipInputStream archive) throws IOException {
        ZipEntry src;
        while ((src = archive.getNextEntry()) != null) {
            URI target = path.resolve(src.getName());
            addFile(target, archive);
        }
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
        int j=0;
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

        try {
            if (ancestors != null) {
                kernel.stopGBean((ObjectName) ancestors.get(0));
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
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
