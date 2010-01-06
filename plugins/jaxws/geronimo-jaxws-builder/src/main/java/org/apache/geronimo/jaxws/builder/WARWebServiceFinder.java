/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.jaxws.builder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.kernel.classloader.TemporaryClassLoader;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.kernel.util.NestedJarFile;
import org.apache.geronimo.kernel.util.UnpackedJarFile;
import org.apache.xbean.finder.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WARWebServiceFinder implements WebServiceFinder {

    private static final Logger LOG = LoggerFactory.getLogger(WARWebServiceFinder.class);

    private static final WebServiceFinder webServiceFinder = getWebServiceFinder();

    private static WebServiceFinder getWebServiceFinder() {
        boolean useSimpleFinder =
            Boolean.getBoolean("org.apache.geronimo.jaxws.builder.useSimpleFinder");

        WebServiceFinder webServiceFinder = null;

        if (useSimpleFinder) {
            webServiceFinder = new SimpleWARWebServiceFinder();
        } else {
            webServiceFinder = new AdvancedWARWebServiceFinder();
        }

        return webServiceFinder;
    }

    public Map<String, PortInfo> discoverWebServices(Module module,
                                                     boolean isEJB,
                                                     Map correctedPortLocations)
            throws DeploymentException {
        return webServiceFinder.discoverWebServices(module, isEJB, correctedPortLocations);
    }

    /**
     * Returns a list of any classes annotated with @WebService or
     * @WebServiceProvider annotation.
     */
    static List<Class> discoverWebServices(JarFile moduleFile,
                                           boolean isEJB,
                                           ClassLoader parentClassLoader)
            throws DeploymentException {
        LOG.debug("Discovering web service classes");

        File tmpDir = null;
        List<URL> urlList = new ArrayList<URL>();
        if (isEJB) {
            File jarFile = new File(moduleFile.getName());
            try {
                urlList.add(jarFile.toURL());
            } catch (MalformedURLException e) {
                // this should not happen
                throw new DeploymentException(e);
            }
        } else {
            File baseDir = null;

            if (moduleFile instanceof UnpackedJarFile) {
                // war directory is being deployed (--inPlace)
                baseDir = ((UnpackedJarFile)moduleFile).getBaseDir();
            } else if (moduleFile instanceof NestedJarFile && ((NestedJarFile)moduleFile).isUnpacked()) {
                // ear directory is being deployed (--inPlace)
                baseDir = new File(moduleFile.getName());
            } else {
                // war file or ear file is being deployed
                /*
                 * Can't get ClassLoader to load nested Jar files, so
                 * unpack the module Jar file and discover all nested Jar files
                 * within it and the classes/ directory.
                 */
                try {
                    tmpDir = FileUtils.createTempDir();
                    /*
                     * This is needed becuase JarUtils.unzipToDirectory()
                     * always closes the passed JarFile.
                     */
                    JarFile module = new JarFile(moduleFile.getName());
                    JarUtils.unzipToDirectory(module, tmpDir);
                } catch (IOException e) {
                    if (tmpDir != null) {
                        FileUtils.recursiveDelete(tmpDir);
                    }
                    throw new DeploymentException("Failed to expand the module archive", e);
                }

                baseDir = tmpDir;
            }

            // create URL list
            Enumeration<JarEntry> jarEnum = moduleFile.entries();
            while (jarEnum.hasMoreElements()) {
                JarEntry entry = jarEnum.nextElement();
                String name = entry.getName();
                if (name.equals("WEB-INF/classes/")) {
                    // ensure it is first
                    File classesDir = new File(baseDir, "WEB-INF/classes/");
                    try {
                        urlList.add(0, classesDir.toURL());
                    } catch (MalformedURLException e) {
                        // this should not happen, ignore
                    }
                } else if (name.startsWith("WEB-INF/lib/")
                        && name.endsWith(".jar")) {
                    File jarFile = new File(baseDir, name);
                    try {
                        urlList.add(jarFile.toURL());
                    } catch (MalformedURLException e) {
                        // this should not happen, ignore
                    }
                }
            }
        }

        URL[] urls = urlList.toArray(new URL[] {});
        TemporaryClassLoader tempClassLoader = null;
        try {
            tempClassLoader = new TemporaryClassLoader(urls, parentClassLoader);
            List<Class> classes = new ArrayList<Class>();
            for (URL url : urlList) {
                try {
                    ClassFinder classFinder = new ClassFinder(tempClassLoader, Collections.singletonList(url));
                    classes.addAll(classFinder.findAnnotatedClasses(WebService.class));
                    classes.addAll(classFinder.findAnnotatedClasses(WebServiceProvider.class));
                } catch (Exception e) {
                    LOG.warn("Fail to search Web Service in jar [" + url + "]", e);
                }
            }
            return classes;
        } finally {
            if (tmpDir != null) {
                FileUtils.recursiveDelete(tmpDir);
            }
        }
    }
}
