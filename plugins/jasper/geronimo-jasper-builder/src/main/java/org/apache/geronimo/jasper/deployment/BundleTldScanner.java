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

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeployableBundle;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

public class BundleTldScanner {

    /**
     * Scan the module being deployed for JAR files or TLD files in the WEB-INF directory
     *
     * @param webModule module being deployed
     * @return list of the URL(s) for the TLD files in the module
     * @throws DeploymentException if module cannot be scanned
     */
    public List<URL> scanModule(WebModule webModule) throws DeploymentException {
        Deployable deployable = webModule.getDeployable();
        if (!(deployable instanceof DeployableBundle)) {
            throw new IllegalArgumentException("Expected DeployableBundle");
        }
        Bundle bundle = ((DeployableBundle) deployable).getBundle();

        List<URL> modURLs = new ArrayList<URL>();
        Enumeration<URL> e = bundle.findEntries("WEB-INF/", "*.tld", true);
        if (e != null) {
            while (e.hasMoreElements()) {
                URL tldURL = e.nextElement();
                String tldPath = tldURL.getPath();
                if (tldPath.startsWith("/WEB-INF/classes") || tldPath.startsWith("/WEB-INF/lib") || (tldPath.startsWith("/WEB-INF/tags") && !tldPath.endsWith("implicit.tld"))) {
                    continue;
                }
                modURLs.add(tldURL);
            }
        }

        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);

        BundleResourceFinder resourceFinder = new BundleResourceFinder(packageAdmin, bundle, "META-INF/", ".tld");
        modURLs.addAll(resourceFinder.find());

        bundle.getBundleContext().ungetService(reference);

        return modURLs;
    }

}
