/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.sharedlib;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.SingleElementCollection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.kernel.config.MultiParentClassLoader;

/**
 * @version $Rev$ $Date$
 */
public class SharedLib {
    public SharedLib(ClassLoader classLoader, String[] classesDirs, String[] libDirs, Collection<ServerInfo> serverInfos) throws MalformedURLException {
        this(classLoader, classesDirs, libDirs, new SingleElementCollection<ServerInfo>(serverInfos).getElement());
    }

    private SharedLib(ClassLoader classLoader, String[] classesDirs, String[] libDirs, ServerInfo serverInfo) throws MalformedURLException {
        MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) classLoader;
        Set currentUrls = new HashSet(Arrays.asList(multiParentClassLoader.getURLs()));

        int size=0;
        if (classesDirs != null) size += classesDirs.length;
        if (libDirs != null) size += libDirs.length;

        LinkedHashSet newUrls = new LinkedHashSet(size);
        if (classesDirs != null) {
            for (int i = 0; i < classesDirs.length; i++) {
                String classesDir = classesDirs[i];
                File dir = serverInfo.resolveServer(classesDir);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        throw new IllegalArgumentException("Failed to create classes dir: " + dir);
                    }
                }
                if (!dir.isDirectory()) {
                    throw new IllegalArgumentException("Shared classes dir is not a directory: " + dir);
                }
                URL location = dir.toURL();
                if (!currentUrls.contains(location)) {
                    newUrls.add(location);
                }
            }
        }

        if (libDirs != null) {
            for (int i = 0; i < libDirs.length; i++) {
                String libDir = libDirs[i];
                File dir = serverInfo.resolveServer(libDir);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        throw new IllegalArgumentException("Failed to create lib dir: " + dir);
                    }
                }
                if (!dir.isDirectory()) {
                    throw new IllegalArgumentException("Shared lib dir is not a directory: " + dir);
                }

                File[] files = dir.listFiles();
                for (int j = 0; j < files.length; j++) {
                    File file = files[j];
                    if (file.canRead() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {
                        URL location = file.toURL();
                        if (!currentUrls.contains(location)) {
                            newUrls.add(location);
                        }
                    }
                }
            }
        }

        for (Iterator iterator = newUrls.iterator(); iterator.hasNext();) {
            URL url = (URL) iterator.next();
            multiParentClassLoader.addURL(url);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SharedLib.class);
        infoFactory.setPriority(GBeanInfo.PRIORITY_CLASSLOADER);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false, false);
        infoFactory.addAttribute("classesDirs", String[].class, true, true);
        infoFactory.addAttribute("libDirs", String[].class, true, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.setConstructor(new String[]{"classLoader", "classesDirs", "libDirs", "ServerInfo"});  
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
