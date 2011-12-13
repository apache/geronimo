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

package org.apache.geronimo.hook.equinox;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.hook.BundleHelper;
import org.apache.geronimo.hook.SharedLibraryRegistry;
import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegateHook;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class SharedLibClassLoaderDelegateHook implements ClassLoaderDelegateHook {

    public Class<?> preFindClass(String name, BundleClassLoader classLoader, BundleData data) throws ClassNotFoundException {
        return null;
    }

    public Class<?> postFindClass(String name, BundleClassLoader classLoader, BundleData data) throws ClassNotFoundException {
        SharedLibraryRegistry sharedLibraryRegistry = BundleHelper.getSharedLibraryRegistry();
        if (sharedLibraryRegistry == null) {
            return null;
        }
        List<Bundle> dependentSharedLibBundles = sharedLibraryRegistry.getDependentSharedLibBundles(data.getBundleID());
        if (dependentSharedLibBundles != null) {
            for (Bundle sharedLibBundle : dependentSharedLibBundles) {
                try {
                    return sharedLibBundle.loadClass(name);
                } catch (ClassNotFoundException e) {
                }
            }
        }
        return null;
    }

    public URL preFindResource(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        return null;
    }

    public URL postFindResource(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        SharedLibraryRegistry sharedLibraryRegistry = BundleHelper.getSharedLibraryRegistry();
        if (sharedLibraryRegistry == null) {
            return null;
        }
        List<Bundle> dependentSharedLibBundles = sharedLibraryRegistry.getDependentSharedLibBundles(data.getBundleID());
        if (dependentSharedLibBundles != null) {
            for (Bundle sharedLibBundle : dependentSharedLibBundles) {
                URL url = sharedLibBundle.getResource(name);
                if (url != null) {
                    return url;
                }
            }
        }
        return null;
    }

    public Enumeration<URL> preFindResources(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        return null;
    }

    public Enumeration<URL> postFindResources(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        SharedLibraryRegistry sharedLibraryRegistry = BundleHelper.getSharedLibraryRegistry();
        if (sharedLibraryRegistry == null) {
            return null;
        }
        List<Bundle> dependentSharedLibBundles = sharedLibraryRegistry.getDependentSharedLibBundles(data.getBundleID());
        if (dependentSharedLibBundles == null || dependentSharedLibBundles.isEmpty()) {
            return null;
        }
        Set<URL> foundResources = new LinkedHashSet<URL>();
        for (Bundle sharedLibBundle : dependentSharedLibBundles) {
            try {
                Enumeration<URL> en = sharedLibBundle.getResources(name);
                if (en == null) {
                    continue;
                }
                while (en.hasMoreElements()) {
                    foundResources.add(en.nextElement());
                }
            } catch (IOException e) {
                //ignore this bundle
            }
        }
        return Collections.enumeration(foundResources);
    }

    public String preFindLibrary(String name, BundleClassLoader classLoader, BundleData data) throws FileNotFoundException {
        return null;
    }

    public String postFindLibrary(String name, BundleClassLoader classLoader, BundleData data) {
        return null;
    }

    protected Class<?> loadClassFromSharedLib(String name, BundleClassLoader classLoader, BundleData data) {
        return null;
    }

}
