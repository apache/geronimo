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
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.kernel.deployment.scanner.URLType;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/19 06:40:07 $
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
                gbean.setEndpointPatterns((String) entry.getKey(), (Set) entry.getValue());
            }
            callback.addGBean(name, gbean);
        }
    }

    public void complete() {
    }
}
