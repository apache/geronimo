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

package org.apache.geronimo.axis2;

import org.apache.axis2.jaxws.message.factory.ClassFinderFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.geronimo.axis2.osgi.GeronimoBundleClassFinder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class Axis2SystemInitializer implements GBeanLifecycle {

    private BundleContext bundleContext;

    private ServiceReference packageAdminServiceReference;

    private GeronimoBundleClassFinder bundleClassFinder;

    public Axis2SystemInitializer(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    @Override
    public void doStart() throws Exception {
        packageAdminServiceReference = bundleContext.getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(packageAdminServiceReference);
        ClassFinderFactory classFinderFactory = (ClassFinderFactory) FactoryRegistry.getFactory(ClassFinderFactory.class);
        bundleClassFinder = new GeronimoBundleClassFinder(packageAdmin);
        classFinderFactory.setClassFinder(bundleClassFinder);
        bundleContext.addBundleListener(bundleClassFinder);
    }

    @Override
    public void doStop() throws Exception {
        ClassFinderFactory classFinderFactory = (ClassFinderFactory) FactoryRegistry.getFactory(ClassFinderFactory.class);
        classFinderFactory.setClassFinder(null);
        if (packageAdminServiceReference != null) {
            try {
                bundleContext.ungetService(packageAdminServiceReference);
            } catch (Exception e) {
            }
        }
        bundleContext.removeBundleListener(bundleClassFinder);
    }

}
