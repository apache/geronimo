/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.blueprint;

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.Priority;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Blueprint starts beans asynchronously whereas Geronimo starts configurations in a single thread.
 * This bean waits for the blueprint activity to complete before returning.
 *
 * @version $Rev$ $Date$
 */
@GBean
@Priority(priority = 2)
public class WaitForBlueprintGBean {

    private volatile BlueprintEvent event;
    private CountDownLatch latch = new CountDownLatch(1);

    public WaitForBlueprintGBean(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                                 @ParamAttribute(name = "packageName") String packageName,
                                 @ParamAttribute(name = "symbolicName") String symbolicName) throws Exception {
        final Bundle bundle = BundleUtils.unwrapBundle(getBundle(bundleContext, symbolicName, packageName));
        BlueprintListener listener = new BlueprintListener() {

            @Override
            public void blueprintEvent(BlueprintEvent event) {
                if (event.getBundle() == bundle) {
                    if (event.getType() == BlueprintEvent.CREATED || event.getType() == BlueprintEvent.FAILURE) {
                        WaitForBlueprintGBean.this.event = event;
                        latch.countDown();
                    }
                }
            }
        };
        ServiceRegistration registration = bundleContext.registerService(BlueprintListener.class.getName(), listener, new Hashtable());
        latch.await();
        registration.unregister();
        if (event.getType() == BlueprintEvent.FAILURE) {
            throw new Exception("Could not start blueprint plan", event.getCause());
        }
    }

    private Bundle getBundle(BundleContext bundleContext, String symbolicName, String packageName) throws Exception {
        ServiceReference reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(reference);
        try {
            if (symbolicName != null) {
                Bundle[] bundles = packageAdmin.getBundles(symbolicName, null);
                if (bundles == null) {
                    throw new Exception("Unable to find bundle based on symbolic name. There is no bundle with " + symbolicName + " symbolic name");
                } else if (bundles.length > 1) {
                    throw new Exception("Found multiple bundles with the same symbolic name: " + symbolicName);
                } else {
                    return bundles[0];
                }
            } else if (packageName != null) {
                ExportedPackage exportedPackage = packageAdmin.getExportedPackage(packageName);
                if (exportedPackage == null) {
                    throw new Exception("Unable to find bundle based on package name. There is no bundle that exports " + packageName + " package");
                }
                return exportedPackage.getExportingBundle();
            } else {
                return bundleContext.getBundle();
            }
        } finally {
            bundleContext.ungetService(reference);
        }
    }
}
