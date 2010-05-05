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
package org.apache.geronimo.kernel.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ClassLoader implementation that delegates calls to multiple classloaders.
 */
public class DelegatingClassLoader extends ClassLoader {
    
    private Set<ClassLoader> loaders = new LinkedHashSet<ClassLoader>();

    public void addLoader(ClassLoader loader) {
        loaders.add(loader);
    }

    public void addLoader(Class<?> clazz) {
        loaders.add(clazz.getClassLoader());
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader loader : loaders) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                // Try next
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader loader : loaders) {
            URL url = loader.getResource(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }
    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> resources = new ArrayList<URL>();
        for (ClassLoader loader : loaders) {
            Enumeration<URL> e = loader.getResources(name);
            while (e.hasMoreElements()) {
                resources.add(e.nextElement());
            }
        }
        return Collections.enumeration(resources);
    }
}
