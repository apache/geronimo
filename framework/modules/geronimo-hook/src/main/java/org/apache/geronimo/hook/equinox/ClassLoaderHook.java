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

import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.hook.BundleExtender;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
import org.eclipse.osgi.internal.loader.BundleLoader;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;

public class ClassLoaderHook implements ClassLoadingHook, BundleExtender {

    private static final String USE_URL_CLASSLOADER = "org.apache.geronimo.equinox.useURLClassLoader";
    
    private static final boolean useURLClassLoader = initUseURLClassLoader();
    
    private static boolean initUseURLClassLoader() {
        String property = System.getProperty(USE_URL_CLASSLOADER, "false");
        return Boolean.parseBoolean(property);        
    }
    
    private final Map<Long, String> dynamicPackages = 
        Collections.synchronizedMap(new HashMap<Long, String>());
    
    public void addDynamicImportPackage(long bundleId, String packages) {
        if (packages != null && packages.trim().length() > 0) {
            dynamicPackages.put(bundleId, packages);
        }
    }
    
    public void removeDynamicImportPackage(long bundleId) {
        dynamicPackages.remove(bundleId);
    }
    
    public boolean addClassPathEntry(ArrayList arg0,
                                     String arg1,
                                     ClasspathManager arg2,
                                     BaseData arg3,
                                     ProtectionDomain arg4) {
        return false;
    }

    public BaseClassLoader createClassLoader(ClassLoader parent,
                                             ClassLoaderDelegate delegate,
                                             BundleProtectionDomain domain,
                                             BaseData data,
                                             String[] classpath) {
        BundleLoader loader = (BundleLoader) delegate;
        AbstractBundle bundle = loader.getBundle();
        String packages = dynamicPackages.get(bundle.getBundleId());
        if (packages != null) {
            try {
                loader.addDynamicImportPackage(ManifestElement.parseHeader(Constants.DYNAMICIMPORT_PACKAGE, packages));
            } catch (BundleException e) {
                throw new RuntimeException(e);
            }
        }
        
        ProtectionDomain protectionDomain = domain;
        if (protectionDomain == null) {
            /**
             * By default Equinox creates a ProtectionDomain for each bundle with AllPermission permission.
             * That breaks Geronimo security checks. See GERONIMO-5480 for details.
             * This work-around prevents Equinox from adding AllPermission permission to each bundle.
             */
            PermissionCollection emptyPermissionCollection = (new AllPermission()).newPermissionCollection();
            protectionDomain = new ProtectionDomain(null, emptyPermissionCollection);
        }

        BaseClassLoader classLoader;
        if (useURLClassLoader) {
            classLoader = new GeronimoClassLoader(this, parent, delegate, protectionDomain, data, classpath, bundle);
        } else {
            classLoader = new DefaultClassLoader(parent, delegate, protectionDomain, data, classpath);
        }
        return classLoader;
    }

    public String findLibrary(BaseData arg0, String arg1) {
        return null;
    }

    public ClassLoader getBundleClassLoaderParent() {
        return null;
    }

    public void initializedClassLoader(BaseClassLoader arg0, BaseData arg1) {
    }

    public byte[] processClass(String arg0,
                               byte[] arg1,
                               ClasspathEntry arg2,
                               BundleEntry arg3,
                               ClasspathManager arg4) {
        return null;
    }
    
}
