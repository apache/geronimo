/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.system.sharedlib;

import java.util.Arrays;
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
    public SharedLib(ClassLoader classLoader, String[] classesDirs, String[] libDirs, ServerInfo serverInfo) throws MalformedURLException {
        MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) classLoader;
        Set currentUrls = new HashSet(Arrays.asList(multiParentClassLoader.getURLs()));

        LinkedHashSet newUrls = new LinkedHashSet(classesDirs.length + libDirs.length);
        for (int i = 0; i < classesDirs.length; i++) {
            String classesDir = classesDirs[i];
            File dir = serverInfo.resolve(classesDir);
            if (!dir.exists()) {
                throw new IllegalArgumentException("Classes dir does not exist: " + dir);
            }
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("Classes dir is not a directory: " + dir);
            }
            URL location = dir.toURL();
            if (!currentUrls.contains(location)) {
                newUrls.add(location);
            }
        }

        for (int i = 0; i < libDirs.length; i++) {
            String libDir = libDirs[i];
            File dir = serverInfo.resolve(libDir);
            if (!dir.exists()) {
                throw new IllegalArgumentException("Lib dir does not exist: " + dir);
            }
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("Lib dir is not a directory: " + dir);
            }

            File[] files = dir.listFiles();
            for (int j = 0; j < files.length; j++) {
                File file = files[j];
                if (file.canRead() && file.getName().endsWith(".jar")) {
                    URL location = dir.toURL();
                    if (!currentUrls.contains(location)) {
                        newUrls.add(location);
                    }
                }
            }
        }

        for (Iterator iterator = newUrls.iterator(); iterator.hasNext();) {
            URL url = (URL) iterator.next();
            multiParentClassLoader.addURL(url);
        }
    }
}
