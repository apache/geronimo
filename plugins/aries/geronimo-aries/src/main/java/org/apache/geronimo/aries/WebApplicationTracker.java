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
package org.apache.geronimo.aries;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.osgi.web.WebApplicationListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @version $Rev:385232 $ $Date$
 */
public class WebApplicationTracker implements WebApplicationListener {

    private final BundleContext bundleContext;
    private ServiceRegistration registration;
    // list of bundles that are deployed successfully or failed to deploy
    private Set<Bundle> completed;
    
    public WebApplicationTracker(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.completed = Collections.synchronizedSet(new HashSet<Bundle>());
    }
    
    public void start() {
        registration = bundleContext.registerService(WebApplicationListener.class.getName(), this, null);
    }
    
    public void stop() {
        if (registration != null) {
            registration.unregister();
        }
    }

    public synchronized boolean waitForBundles(Set<Bundle> bundles, long timeout) throws InterruptedException {
        while (!completed.containsAll(bundles)) {
            if (timeout <= 0) {
                return false;
            }
            long startTime = System.currentTimeMillis();
            wait(timeout);
            long endTime = System.currentTimeMillis();
            timeout -= (endTime - startTime);
        }
        return true;
    }
    
    @Override
    public synchronized void deployed(Bundle bundle, String contextPath, Dictionary<String, Object> arg2) {
        completed.add(bundle);
        notifyAll();
    }

    @Override
    public synchronized void deploying(Bundle bundle, String contextPath, Dictionary<String, Object> arg2) {
        completed.remove(bundle);
    }

    @Override
    public synchronized void failed(Bundle bundle, String contextPath, Dictionary<String, Object> arg2) {
        completed.add(bundle);
        notifyAll();
    }

    @Override
    public synchronized void undeployed(Bundle bundle, String contextPath, Dictionary<String, Object> arg2) {
    }

    @Override
    public synchronized void undeploying(Bundle bundle, String contextPath, Dictionary<String, Object> arg2) {
        completed.remove(bundle);
    }
   
}
