/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.myfaces.config.resource.osgi;

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.myfaces.config.resource.osgi.api.ConfigRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


/**
 * The activator that starts and manages the life-cycle of
 * the class factory registry.
 */
public class Activator implements BundleActivator {
    // tracker to watch for bundle updates
    protected BundleTracker bt;
    // service tracker for a logging service
    protected ServiceTracker lst;
    // Our provider registry
    protected ConfigRegistryImpl registry;
    // The service registration for the provider registry
    protected ServiceRegistration registryRegistration;
    // our bundle context
    protected BundleContext context;
    // an array of all active logging services.
    List<LogService> logServices = new ArrayList<LogService>();


    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        this.context = context;
        lst = new LogServiceTracker(context, LogService.class.getName(), null);
        lst.open();

        registry = new ConfigRegistryImpl(this);
        // register this as a service
        registryRegistration = context.registerService(ConfigRegistry.class.getName(), registry, null);

	    bt = new BundleTracker(context, Bundle.STARTING | Bundle.ACTIVE, new ConfigBundleTrackerCustomizer(this, context.getBundle(), registry));
	    bt.open();
	}

    @Override
	public synchronized void stop(BundleContext context) throws Exception {
	    bt.close();
	    lst.close();
        registryRegistration.unregister();
	}

    public BundleContext getBundleContext() {
        return context;
    }

	void log(int level, String message) {
	    synchronized (logServices) {
	        for (LogService log : logServices) {
	            log.log(level, message);
	        }
        }
	}

	void log(int level, String message, Throwable th) {
        synchronized (logServices) {
            for (LogService log : logServices) {
                log.log(level, message, th);
            }
        }
    }

	private final class LogServiceTracker extends ServiceTracker {
        private LogServiceTracker(BundleContext context, String clazz,
                ServiceTrackerCustomizer customizer) {
            super(context, clazz, customizer);
        }

        @Override
        public Object addingService(ServiceReference reference) {
            Object svc = super.addingService(reference);
            if (svc instanceof LogService) {
                synchronized (logServices) {
                    logServices.add((LogService) svc);
                }
            }
            return svc;
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            synchronized (logServices) {
                logServices.remove(service);
            }
            super.removedService(reference, service);
        }
    }
}
