/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.apache.geronimo.jasper;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder.ResourceFinderCallback;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GBean
public class TldRegistration implements GBeanLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(TldRegistration.class);
    
    private final Bundle bundle;
    private final String packageNameList;

    public TldRegistration(@ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                           @ParamAttribute(name = "packageNameList") String packageNameList) throws Exception {
        this.bundle = bundle;
        this.packageNameList = packageNameList;
    }
    
    public void doStart() throws Exception {
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
        try {
            registerTlds(packageAdmin);
        } finally {
            bundle.getBundleContext().ungetService(reference);
        }
    }
    
    private void registerTlds(PackageAdmin packageAdmin) throws Exception {
        String[] packageNames = packageNameList.split(",");
        for (String packageName : packageNames) {
            ExportedPackage exportedPackage = packageAdmin.getExportedPackage(packageName.trim());
            if (exportedPackage == null || exportedPackage.getExportingBundle().getState() != Bundle.ACTIVE) {
                LOGGER.warn("Package {} is not currently exported by any active bundle", packageName);
            } else {
                Bundle exportingBundle = exportedPackage.getExportingBundle();
                
                BundleResourceFinder resourceFinder = new BundleResourceFinder(packageAdmin, exportingBundle, "META-INF/", ".tld");
                TldResourceFinderCallback callback = new TldResourceFinderCallback();
                resourceFinder.find(callback);
                if (!callback.getTlds().isEmpty()) {
                    exportingBundle.getBundleContext().registerService(TldProvider.class.getName(), callback, null);
                }
            }
        }        
    }
        
    public void doFail() {
    }

    public void doStop() throws Exception {
    }
             
    private static class TldResourceFinderCallback implements ResourceFinderCallback, TldProvider {

        private final List<TldProvider.TldEntry> tlds = new ArrayList<TldProvider.TldEntry>();

        private TldResourceFinderCallback() {
        }
        
        public Collection<TldProvider.TldEntry> getTlds() {
            return tlds;
        }
        
        public void foundInDirectory(Bundle bundle, String basePath, URL url) throws Exception {
            LOGGER.debug("Found {} TLD in bundle {}", url, bundle);
            tlds.add(new TldProvider.TldEntry(bundle, url));
        }

        public void foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception {
            URL jarURL = bundle.getEntry(jarName);
            URL url = new URL("jar:" + jarURL.toString() + "!/" + entry.getName());
            LOGGER.debug("Found {} TLD in bundle {}", url, bundle);
            tlds.add(new TldProvider.TldEntry(bundle, url, jarURL));
        }
        
    }

}
