/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.deployment.util.URLType;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 *
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/25 09:57:38 $
 */
public class ServiceModule implements DeploymentModule {
    private final URI moduleID;
    private final URLInfo urlInfo;
    private final List pathURIs;
    private final List gbeanDefaults;

    public ServiceModule(URI moduleID, URLInfo urlInfo, List urls, List gbeanDefaults) {
        this.moduleID = moduleID;
        this.urlInfo = urlInfo;
        this.pathURIs = urls;
        this.gbeanDefaults = gbeanDefaults;
    }

    public void init() throws DeploymentException {
    }

    public void generateClassPath(ConfigurationCallback callback) throws DeploymentException {
        URI moduleBase = URI.create(moduleID.toString() + "/");
        if (urlInfo.getType() == URLType.PACKED_ARCHIVE) {
            try {
                InputStream is = urlInfo.getUrl().openStream();
                try {
                    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
                    for (ZipEntry entry; (entry = zis.getNextEntry()) != null; zis.closeEntry()) {
                        String name = entry.getName();
                        if (name.endsWith("/")) {
                            continue;
                        }
                        for (Iterator i = pathURIs.iterator(); i.hasNext();) {
                            URI path = (URI) i.next();
                            if (!path.isAbsolute() && name.startsWith(path.getPath())) {
                                callback.addFile(moduleBase.resolve(name), zis);
                                break;
                            }
                        }
                    }
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                throw new DeploymentException("Error adding content from archive " + urlInfo.getUrl().toString(), e);
            }
        } else if (urlInfo.getType() == URLType.UNPACKED_ARCHIVE) {
            //@todo support proper recursive file scanning (with WebDAV and filters)
            URL url = urlInfo.getUrl();
            if (!"file".equals(url.getProtocol())) {
                throw new DeploymentException("Unpacked archives not supported for URL " + url.toString());
            }
            URI root = URI.create(url.toString());
            for (Iterator i = pathURIs.iterator(); i.hasNext();) {
                URI pathRoot = (URI) i.next();
                if (pathRoot.isAbsolute()) {
                    continue;
                }
                pathRoot = root.resolve(pathRoot);
                LinkedList dirs = new LinkedList();
                dirs.add(pathRoot);
                while (!dirs.isEmpty()) {
                    URI uri = (URI) dirs.removeFirst();
                    File dir = new File(uri);
                    if (!dir.exists()) {
                        continue;
                    }
                    assert (dir.isDirectory()) : "Found file in directory list";
                    File[] files = dir.listFiles();
                    for (int j = 0; j < files.length; j++) {
                        File file = files[j];
                        if (file.isDirectory()) {
                            if (!"CVS".equals(file.getName())) {
                                dirs.addFirst(file.toURI());
                            }
                        } else {
                            try {
                                FileInputStream is = new FileInputStream(file);
                                try {
                                    URI path = root.relativize(file.toURI());
                                    callback.addFile(moduleBase.resolve(path), is);
                                } finally {
                                    is.close();
                                }
                            } catch (IOException e) {
                                throw new DeploymentException("Unable to add file:" + file, e);
                            }
                        }
                    }
                }
            }
        }
        for (Iterator i = pathURIs.iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            callback.addToClasspath(moduleBase.resolve(uri));
        }
    }

    public void defineGBeans(ConfigurationCallback callback, ClassLoader cl) throws DeploymentException {
        for (Iterator i = gbeanDefaults.iterator(); i.hasNext();) {
            GBeanDefault defs = (GBeanDefault) i.next();
            ObjectName name;
            try {
                name = new ObjectName(defs.getObjectName());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid JMX ObjectName: " + defs.getObjectName(), e);
            }

            GBeanInfo gbeanInfo = defs.getGBeanInfo();
            if (gbeanInfo == null) {
                String className = defs.getClassName();
                try {
                    gbeanInfo = GBeanInfo.getGBeanInfo(className, cl);
                } catch (InvalidConfigurationException e) {
                    throw new DeploymentException("Unable to get GBeanInfo from class " + className, e);
                }
            }

            GBeanMBean gbean;
            try {
                gbean = new GBeanMBean(gbeanInfo, cl);
            } catch (InvalidConfigurationException e) {
                throw new DeploymentException("Unable to create GMBean", e);
            }
            for (Iterator j = defs.getValues().entrySet().iterator(); j.hasNext();) {
                Map.Entry entry = (Map.Entry) j.next();
                try {
                    gbean.setAttribute((String) entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    throw new DeploymentException("Unable to set GMBean attribute " + entry.getKey(), e);
                }
            }
            for (Iterator iterator = defs.getEndpoints().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                gbean.setReferencePatterns((String) entry.getKey(), (Set) entry.getValue());
            }
            callback.addGBean(name, gbean);
        }
    }

    public void complete() {
    }
}
