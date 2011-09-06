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

package org.apache.geronimo.axis2.osgi;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.axis2.jaxws.message.databinding.ClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.ClassDiscoveryFilter;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoBundleClassFinder implements ClassFinder, SynchronousBundleListener {

    private static final Logger logger = LoggerFactory.getLogger(GeronimoBundleClassFinder.class);

    private Map<Long, Map<String, Set<String>>> bundleIdPackageClassNamesMap = new ConcurrentHashMap<Long, Map<String, Set<String>>>();

    private PackageAdmin packageAdmin;

    public GeronimoBundleClassFinder(PackageAdmin packageAdmin) {
        this.packageAdmin = packageAdmin;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.UPDATED || event.getType() == BundleEvent.UNINSTALLED) {
            bundleIdPackageClassNamesMap.remove(event.getBundle().getBundleId());
        }
    }

    @Override
    public ArrayList<Class> getClassesFromJarFile(final String packageName, ClassLoader cl) throws ClassNotFoundException {
        Bundle bundle = BundleUtils.getBundle(cl, true);
        if (bundle == null) {
            //Special actions due to the comments below. While Equinox Jar ClassLoader is enabled, we might got problems
            //--- Quoted from org.apache.axis2.jaxws.server.JAXWSMessageReceiver
            // we need to merge the deployment class loader to the TCCL. This is because, in JAX-WS
            // services, there can be situations where we have to load classes from the deployment
            // artifact (JAX-WS jar file) in the message flow. Ex: Handler classes in the service
            // artifact. Adding this as a fix for AXIS2-4930.
            //---
            bundle = BundleUtils.getBundle(cl.getParent(), true);
            if (bundle == null) {
                return new ArrayList<Class>();
            }
        }
        Long bundleId = bundle.getBundleId();
        Map<String, Set<String>> packageClassNamesMap = bundleIdPackageClassNamesMap.get(bundleId);
        Set<String> classNames = null;
        if (packageClassNamesMap == null) {
            synchronized (bundleIdPackageClassNamesMap) {
                packageClassNamesMap = bundleIdPackageClassNamesMap.get(bundleId);
                if (packageClassNamesMap == null) {
                    packageClassNamesMap = new ConcurrentHashMap<String, Set<String>>();
                    bundleIdPackageClassNamesMap.put(bundleId, packageClassNamesMap);
                }
            }
        } else {
            classNames = packageClassNamesMap.get(packageName);
        }

        if (classNames == null) {
            synchronized (packageClassNamesMap) {
                classNames = packageClassNamesMap.get(packageName);
                if (classNames == null) {
                    //TODO Do we need to limit the scanning scope in the target application ? As we share one bundle for all the sub modules except for car
                    BundleClassFinder bundleClassFinder = new BundleClassFinder(packageAdmin, bundle, new ClassDiscoveryFilter() {

                        @Override
                        public boolean directoryDiscoveryRequired(String directory) {
                            return true;
                        }

                        @Override
                        public boolean jarFileDiscoveryRequired(String jarFile) {
                            return true;
                        }

                        @Override
                        public boolean packageDiscoveryRequired(String p) {
                            return p.equals(packageName);
                        }

                        @Override
                        public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                            return discoveryRange == DiscoveryRange.BUNDLE_CLASSPATH;
                        }
                    });
                    classNames = bundleClassFinder.find();
                    packageClassNamesMap.put(packageName, classNames);
                }
            }
        }
        ArrayList clses = new ArrayList<Class>(classNames.size());
        for (String className : classNames) {
            try {
                Class<?> cls = bundle.loadClass(className);
                //Invoke getConstructors() to force the classloader to resolve the target class
                cls.getConstructors();
                clses.add(cls);
            } catch (Throwable e) {
                String message = "Fail to load class " + className + " in GeronimoBundleClassFinder, it might not be considered while processing SOAP message due to " + e.getMessage();
                if (logger.isDebugEnabled()) {
                    logger.debug(message, e);
                } else {
                    logger.warn(message);
                }
            }
        }
        return clses;
    }

    //@Override
    public void updateClassPath(String filePath, ClassLoader cl) throws Exception {
    }

}
