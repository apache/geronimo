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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationParent;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 19:58:16 $
 */
public class ModuleDeployer implements ConfigurationCallback {
    private final ConfigurationParent parent;
    private final URI configRoot;
    private final List modules = new ArrayList();
    private final LinkedHashSet classPath = new LinkedHashSet();
    private final GBeanMBean config;
    private final Map gbeans = new HashMap();
    private final byte[] buffer = new byte[4096];

    public ModuleDeployer(ConfigurationParent parent, URI configID, File workingDir) {
        if (!workingDir.isDirectory()) {
            throw new IllegalArgumentException("workingDir is not a directory");
        }

        this.parent = parent;
        this.configRoot = workingDir.toURI();

        try {
            config = new GBeanMBean(Configuration.GBEAN_INFO);
        } catch (InvalidConfigurationException e) {
            throw (AssertionError) new AssertionError("Unable to initialize Configuration GMBean").initCause(e);
        }
        try {
            config.setAttribute("ID", configID);
        } catch (Exception e) {
            throw (AssertionError) new AssertionError("Unable to initialize ID attribute").initCause(e);
        }
    }

    public void addModule(DeploymentModule module) {
        modules.add(module);
    }

    public void deploy() throws DeploymentException {
        classPath.clear();
        gbeans.clear();
        try {
            // tell each module we are starting deployment
            for (Iterator i = modules.iterator(); i.hasNext();) {
                DeploymentModule module = (DeploymentModule) i.next();
                module.init();
            }

            // get all the classpath URLs the modules define
            for (Iterator i = modules.iterator(); i.hasNext();) {
                DeploymentModule module = (DeploymentModule) i.next();
                module.generateClassPath(this);
            }
            ArrayList path = new ArrayList(classPath);
            try {
                config.setAttribute("ClassPath", path);
            } catch (Exception e) {
                throw new DeploymentException("Unable to save state to configuration", e);
            }

            // build the ClassLoader for those URLs
            URL[] urls = new URL[path.size()];
            int idx = 0;
            for (Iterator i = path.iterator(); i.hasNext();) {
                URI uri = (URI) i.next();
                uri = configRoot.resolve(uri);
                try {
                    urls[idx] = uri.toURL();
                } catch (MalformedURLException e) {
                    throw new DeploymentException("Unable to convert classPath URI to absolute URL: " + uri, e);
                }
            }
            ClassLoader cl;
            if (parent == null) {
                cl = new URLClassLoader(urls);
            } else {
                cl = new URLClassLoader(urls, parent.getClassLoader());
            }

            // get the GBeans from each module
            for (Iterator i = modules.iterator(); i.hasNext();) {
                DeploymentModule module = (DeploymentModule) i.next();
                module.defineGBeans(this, cl);
            }

            // initialize the Configuration state
            try {
                config.setAttribute("GBeanState", Configuration.storeGBeans(gbeans));
            } catch (Exception e) {
                throw new DeploymentException("Unable to save state to configuration", e);
            }
        } finally {
            // tell each module we are done with it
            for (Iterator i = modules.iterator(); i.hasNext();) {
                DeploymentModule module = (DeploymentModule) i.next();
                module.complete();
            }
        }
    }

    public void addFile(URI path, InputStream is) throws IOException {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("path must be a relative URI");
        }

        File to = new File(configRoot.resolve(path));
        to.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(to);
        try {
            for (int count; (count = is.read(buffer)) > 0;) {
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
            try {
                os.close();
                to.delete();
            } catch (IOException e1) {
                // ignore
            }
            throw e;
        }
    }

    public void addToClasspath(URI uri) {
        classPath.add(uri);
    }

    public void addGBean(ObjectName name, GBeanMBean gbean) {
        gbeans.put(name, gbean);
    }

    public void saveConfiguration(JarOutputStream jos) throws IOException {
        // add the configuration data
        jos.putNextEntry(new ZipEntry("META-INF/config.ser"));
        ObjectOutputStream oos = new ObjectOutputStream(jos);
        try {
            Configuration.storeGMBeanState(config, oos);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw (IllegalStateException) new IllegalStateException("Unable to save configuration").initCause(e);
        }
        oos.flush();
        jos.closeEntry();

        // add the files from the modules
        LinkedList dirs = new LinkedList();
        dirs.add(new File(configRoot));
        while (!dirs.isEmpty()) {
            File dir = (File) dirs.removeFirst();
            assert dir.isDirectory() : "Added a file to the directory list" + dir;
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    dirs.addFirst(file);
                } else {
                    URI name = configRoot.relativize(file.toURI());
                    assert (!name.isAbsolute()) : "Could not construct relative path for file " + file;
                    jos.putNextEntry(new ZipEntry(name.toString()));
                    InputStream is = new FileInputStream(file);
                    try {
                        for (int count; (count = is.read(buffer)) > 0;) {
                            jos.write(buffer, 0, count);
                        }
                    } finally {
                        is.close();
                    }
                    jos.closeEntry();
                }
            }
        }
    }

    public LinkedHashSet getClassPath() {
        return classPath;
    }

    public GBeanMBean getConfiguration() {
        return config;
    }
}
