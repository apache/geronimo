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

package org.apache.geronimo.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class DatabaseDriverExtender implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseDriverExtender.class);

    private BundleTracker tracker;

    //private JdbcLeakPreventionListener jdbcPreventionListener;

    public void start(BundleContext context) throws Exception {
        tracker = new BundleTracker(context, Bundle.ACTIVE, new DriverBundleTrackerCustomizer());
        tracker.open();

        //jdbcPreventionListener = new JdbcLeakPreventionListener();
        //context.addBundleListener(jdbcPreventionListener);
    }

    public void stop(BundleContext context) throws Exception {
        tracker.close();
        //context.removeBundleListener(jdbcPreventionListener);
        //Remove any driver from myself
        DriverManager.deregisterDriver(DelegatingDriver.DELEGATINGDRIVER_INSTANCE);
    }

    private static List<Driver> loadDrivers(Bundle bundle, URL providerURL) {
        List<Driver> drivers = new ArrayList<Driver>();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(providerURL.openStream()));
            String line;
            while ( (line = in.readLine()) != null) {
                line = line.trim();

                try {
                    Class<?> driverClass = bundle.loadClass(line);
                    drivers.add( (Driver) driverClass.newInstance());
                } catch (Exception e) {
                    LOG.warn("Failed to load driver {}", line, e);
                }

            }
        } catch (IOException e) {
            // ignore - shouldn't happen
            LOG.warn("Error reading {} service file", providerURL);
        }

        return drivers;
    }

    private static void register(List<Driver> drivers) {
        for (Driver driver : drivers) {
            DelegatingDriver.registerDriver(driver);
        }
    }

    private static void unregister(List<Driver> drivers) {
        for (Driver driver : drivers) {
            DelegatingDriver.unregisterDriver(driver);
        }
    }

    private static class DriverBundleTrackerCustomizer implements BundleTrackerCustomizer {

        public Object addingBundle(Bundle bundle, BundleEvent event) {
            URL providerURL = bundle.getEntry("META-INF/services/java.sql.Driver");
            if (providerURL != null) {
                List<Driver> drivers = loadDrivers(bundle, providerURL);
                register(drivers);
                LOG.debug("Registered {} drivers in bundle {}", drivers, bundle);
                return drivers;
            }
            return null;
        }

        public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
        }

        public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
            List<Driver> drivers = (List<Driver>) object;
            unregister(drivers);
            LOG.debug("Unregistered {} drivers in bundle {}", drivers, bundle);
        }

    }

}
