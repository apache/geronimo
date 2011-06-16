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

package org.apache.geronimo.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */

public class JdbcLeakPreventionListener implements BundleListener {

    private static final Logger logger = LoggerFactory.getLogger(JdbcLeakPreventionListener.class);

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.UNINSTALLED) {
            Bundle bundle = event.getBundle();
            unRegisterJdbcDrivers(bundle);
        }
    }

    public static void unRegisterJdbcDrivers(Bundle bundle) {
        /*
         * DriverManager.getDrivers() has a nasty side-effect of registering
         * drivers that are visible to this class loader but haven't yet been
         * loaded. Therefore, the first call to this method a) gets the list
         * of originally loaded drivers and b) triggers the unwanted
         * side-effect. The second call gets the complete list of drivers
         * ensuring that both original drivers and any loaded as a result of the
         * side-effects are all de-registered.
         */
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            Bundle driverBundle = BundleUtils.getBundle(driver.getClass().getClassLoader(), true);
            if (driverBundle != bundle) {
                continue;
            }
            try {
                DriverManager.deregisterDriver(driver);
            } catch (Exception e) {
                logger.warn("Fail to unregister the driver " + driver.getClass().getName(), e);
            }
        }
    }
}
