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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.jasper.deployment;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeployableJarFile;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarFileTldScanner {

    private static final Logger log = LoggerFactory.getLogger(JarFileTldScanner.class);

    /**
     * Scan the module being deployed for JAR files or TLD files in the WEB-INF directory
     *
     * @param webModule module being deployed
     * @return list of the URL(s) for the TLD files in the module
     * @throws DeploymentException if module cannot be scanned
     */
    public List<URL> scanModule(WebModule webModule) throws DeploymentException {
        log.debug("scanModule( " + webModule.getName() + " ): Entry");

        Deployable deployable = webModule.getDeployable();
        if (!(deployable instanceof DeployableJarFile)) {
            throw new IllegalArgumentException("Expected DeployableJarFile");
        }
        JarFile jarFile = ((DeployableJarFile) deployable).getJarFile();
        List<URL> modURLs = new ArrayList<URL>();
        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                String jarEntryName = entries.nextElement().getName();
                if (jarEntryName.startsWith("WEB-INF") && jarEntryName.endsWith(".tld")) {

                    if (jarEntryName.startsWith("WEB-INF/classes") || jarEntryName.startsWith("WEB-INF/lib") || (jarEntryName.startsWith("WEB-INF/tags") && !jarEntryName.endsWith("implicit.tld"))) {
                        continue;
                    }
                    File targetFile = webModule.getEarContext().getTargetFile(webModule.resolve(createURI(jarEntryName)));
                    if (targetFile != null) {
                        modURLs.add(targetFile.toURI().toURL());
                    }
                } else if (jarEntryName.startsWith("WEB-INF/lib/") && jarEntryName.endsWith(".jar")) {
                    File targetFile = webModule.getEarContext().getTargetFile(webModule.resolve(createURI(jarEntryName)));
                    List<URL> jarUrls = scanJAR(new JarFile(targetFile), "META-INF/");
                    for (URL jarURL : jarUrls) {
                        modURLs.add(jarURL);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new DeploymentException("Could not scan module for TLD files: " + webModule.getName() + " " + ioe.getMessage(), ioe);
        } catch (Exception e) {
            throw new DeploymentException("Could not scan module for TLD files: " + webModule.getName() + " " + e.getMessage(), e);
        }

        log.debug("scanModule() Exit: URL[" + modURLs.size() + "]: " + modURLs.toString());
        return modURLs;
    }

    /**
     * scanJAR(): Scan a JAR files looking for all TLD
     *
     * @param jarFile jar file to scan
     * @param prefix  Optional prefix to limit the search to a specific subdirectory in the JAR file
     * @return list of the URL(s) for the TLD files in the JAR file
     * @throws DeploymentException if jar file cannot be scanned
     */
    private List<URL> scanJAR(JarFile jarFile, String prefix) throws DeploymentException {
        log.debug("scanJAR( " + jarFile.getName() + " ): Entry");

        List<URL> jarURLs = new ArrayList<URL>();
        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (prefix != null) {
                    if (jarEntry.getName().endsWith(".tld") && jarEntry.getName().startsWith(prefix)) {
                        jarURLs.add(new URL("jar:file:" + jarFile.getName() + "!/" + jarEntry.getName()));
                    }
                } else {
                    if (jarEntry.getName().endsWith(".tld")) {
                        jarURLs.add(new URL("jar:file:" + jarFile.getName() + "!/" + jarEntry.getName()));
                    }
                }
            }
        }
        catch (MalformedURLException mfe) {
            throw new DeploymentException("Could not scan JAR file for TLD files: " + jarFile.getName() + " " + mfe.getMessage(), mfe);
        }
        catch (Exception e) {
            throw new DeploymentException("Could not scan JAR file for TLD files: " + jarFile.getName() + " " + e.getMessage(), e);
        }

        log.debug("scanJAR() Exit: URL[" + jarURLs.size() + "]: " + jarURLs.toString());
        return jarURLs;
    }


    /**
     * scanDirectory(): Scan a directory for all TLD files
     *
     * @param url URL for the directory to be scanned
     * @return list of the URL(s) for the TLD files in the directory
     * @throws DeploymentException if directory cannot be scanned
     */
    private List<URL> scanDirectory(URL url) throws DeploymentException {
        log.debug("scanDirectory( " + url.toString() + " ): Entry");

        List<URL> dirURLs = new ArrayList<URL>();
        File directory;
        if (url != null) {
            if (url.toString().startsWith("jar:file:")) {
                try {
                    JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                    URL urlJC = jarConnection.getJarFileURL();
                    URI baseURI = createURI(urlJC.toString());
                    directory = new File(baseURI);
                    if (directory.isDirectory()) {
                        if (directory.canRead()) {
                            JarFile temp = new JarFile(directory);
                            List<URL> tempURLs = scanJAR(temp, "META-INF");
                            for (URL jarURL : tempURLs) {
                                dirURLs.add(jarURL);
                            }
                        } else {
                            log.warn("Cannot read JAR file: " + url.toString());
                        }
                    }
                }
                catch (Exception e) {
                    throw new DeploymentException("Could not scan directory for TLD files: " + url.toString() + " " + e.getMessage(), e);
                }
            } else if (url.toString().startsWith("file:")) {
                try {
                    URI baseURI = createURI(url.toString());
                    directory = new File(baseURI);
                    if (directory.isDirectory() && directory.canRead()) {
                        File[] children = directory.listFiles();
                        for (File child : children) {
                            if (child.getName().endsWith(".tld")) {
                                dirURLs.add(child.toURI().toURL());
                            }
                        }
                    } else {
                        log.warn("Cannot read directory: " + url.toString());
                    }
                }
                catch (Exception e) {
                    throw new DeploymentException("Could not scan directory for TLD files: " + url.toString() + " " + e.getMessage(), e);
                }
            } else if (url.toString().startsWith("jar:")) {
                log.warn("URL type not accounted for: " + url.toString());
            }
        }

        log.debug("scanDirectory() Exit: URL[" + dirURLs.size() + "]: " + dirURLs.toString());
        return dirURLs;
    }

    private static URI createURI(String path) throws URISyntaxException {
        path = path.replaceAll(" ", "%20");
        return new URI(path);
    }

}
