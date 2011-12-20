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


package org.apache.geronimo.gbean.wrapper;

import java.util.concurrent.CountDownLatch;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A GBean that exposes an osgi service as a gbean.  The service has to be a singleton of the supplied interface
 * @version $Rev:$ $Date:$
 */
public class AbstractServiceWrapper<T> {

    private volatile T actual;
    private volatile CountDownLatch latch = new CountDownLatch(1);

    public AbstractServiceWrapper(final Bundle bundle, Class<T> clazz) {
        final BundleContext bundleContext = bundle.getBundleContext();
        ServiceTracker t = new ServiceTracker(bundleContext, clazz.getName(), new ServiceTrackerCustomizer() {

            @Override
            public Object addingService(ServiceReference serviceReference) {
                actual = (T) bundleContext.getService(serviceReference);
                latch.countDown();
                return actual;
            }

            @Override
            public void modifiedService(ServiceReference serviceReference, Object o) {
            }

            @Override
            public void removedService(ServiceReference serviceReference, Object o) {
                latch = new CountDownLatch(1);
                actual = null;
            }
        });
        t.open();
    }

    protected T get() {
        T actual = null;
        while (actual == null) {
            try {
                latch.await();
            } catch (InterruptedException e) {

            }
            actual = this.actual;
        }
        return actual;
    }

}
