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
import java.util.List;
import java.util.Set;

import org.apache.axis2.jaxws.message.databinding.ClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.xbean.osgi.bundle.util.ClassDiscoveryFilter;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoBundleClassFinder implements ClassFinder {

    private PackageAdmin packageAdmin;

    public GeronimoBundleClassFinder(PackageAdmin packageAdmin) {
        this.packageAdmin = packageAdmin;
    }

    @Override
    public ArrayList<Class> getClassesFromJarFile(final String packageName, ClassLoader cl) throws ClassNotFoundException {
        if (!(cl instanceof BundleClassLoader)) {
            return new ArrayList<Class>(0);
        }
        Bundle bundle = ((BundleClassLoader) cl).getBundle(true);
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
        Set<String> classNames = bundleClassFinder.find();
        ArrayList<Class> clses = new ArrayList<Class>(classNames.size());
        for (String className : classNames) {
            clses.add(bundle.loadClass(className));
        }
        return clses;
    }

    //@Override
    public void updateClassPath(String filePath, ClassLoader cl) throws Exception {
    }

}
