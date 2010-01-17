/*
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
package org.apache.geronimo.osgi.web;

import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of a WAB deployed to an available Web Container
 * instance.
 * 
 * @version $Rev$, $Date$
 */
public class WebApplication implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplication.class);

    private final WebContainerExtender extender;
    // the bundle where the web application resides
    private final Bundle bundle;
    // the deployed context path from the bundle headers
    private final String contextPath;
    
    private final AtomicBoolean scheduled = new AtomicBoolean();
    private final AtomicBoolean running = new AtomicBoolean();
    
    private boolean destroyed;

    /**
     * Construct a WebApplicationImp object to represent a
     * WAB-resident application.
     *
     * @param bundle    The bundle containing the WAB.
     * @param contextPath
     *                  The context path from the WAB headers.
     */
    public WebApplication(WebContainerExtender extender, Bundle bundle, String contextPath) {
        this.extender = extender;
        this.bundle = bundle;
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;        
    }
    
    /**
     * Provide access to the bundle where the application resides.
     *
     * @return The Bundle instance for the WAB.
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Schedule this handler for deployment processing.
     */
    public void schedule() {
        // only one scheduled startup at a time.
        if (scheduled.compareAndSet(false, true)) {
            extender.getExecutorService().submit(this);
        }
    }

    /**
     * Run the application deployment process in a separate thread.
     */
    public void run() {
        scheduled.set(false);
        synchronized (scheduled) {
            synchronized (running) {
                running.set(true);
                try {
                    doRun();
                } finally {
                    running.set(false);
                    running.notifyAll();
                }
            }
        }
    }

    private void deploying() {
        LOGGER.debug("Deploying bundle {}", getBundle().getSymbolicName());
        extender.getEventDispatcher().deploying(bundle, contextPath);
    }

    private void deployed() {
        LOGGER.debug("Deployed bundle {}", getBundle().getSymbolicName());
        extender.getEventDispatcher().deployed(bundle, contextPath);
    }

    private void undeploying() {
        LOGGER.debug("Undeploying bundle {}", bundle.getSymbolicName());
        extender.getEventDispatcher().undeploying(bundle, contextPath);
    }

    private void undeployed() {
        LOGGER.debug("Undeployed bundle {}", bundle.getSymbolicName());
        extender.getEventDispatcher().undeployed(bundle, contextPath);
    }

    private void failed(Throwable cause) {
        extender.getEventDispatcher().failed(bundle, contextPath, cause);
    }
    
    /**
     * This method must be called inside a synchronized block to ensure this method is not run concurrently
     */
    private void doRun() {
        if (destroyed) {
            return;
        }
        try {
            // send out a broadcast alert that we're going to do this
            deploying();

            // TODO: do actual deployment
            System.out.println("Deploying " + contextPath + " " + bundle);
            
            // send out the deployed event
            deployed();
        } catch (Throwable exception) {
            LOGGER.error("Unable to start web application for bundle " + getBundle().getSymbolicName(), exception);
            // broadcast a failure event
            failed(exception);
            // unregister the application and possibly let other WABs with the same ContextPath to deploy
            extender.unregisterWebApplication(this);
        }
    }

    /**
     * Undeploy a web application.
     */
    public void undeploy() {
        destroyed = true;
        
        synchronized (running) {
            while (running.get()) {
                try {
                    running.wait();
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
        
        // send the undeploying event
        undeploying();
        
        // TODO: do actual undeployment
        System.out.println("Undeploying " + contextPath + " " + bundle);

        // finished with the undeploy operation
        undeployed();
        
        // unregister the application and possibly let other WABs with the same ContextPath to deploy
        extender.unregisterWebApplication(this);        
    }

}
