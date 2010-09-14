/**
 *
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
package org.apache.geronimo.openejb;

import javax.naming.Context;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.apache.openejb.BeanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContextImpl;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoThreadContextListener implements ThreadContextListener {
    private static final Logger log = LoggerFactory.getLogger(GeronimoThreadContextListener.class);

    // A single stateless listener is used for Geronimo
    private static final GeronimoThreadContextListener instance = new GeronimoThreadContextListener();

    static {
        ThreadContext.addThreadContextListener(instance);
    }

    public static void init() {
        // do nothing.. the goal here is to kick off the onetime init above
    }

    private final Map<String, Deployment> ejbs = new ConcurrentHashMap<String, Deployment>();

    private GeronimoThreadContextListener() {
    }

    public static GeronimoThreadContextListener get() {
        return instance;
    }

    public void addEjb(EjbDeployment ejbDeployment) {
        this.ejbs.put(ejbDeployment.getDeploymentId(), new Deployment(ejbDeployment));
    }

    public void removeEjb(String id) {
        this.ejbs.remove(id);
    }

    EjbDeployment getEjbDeployment(BeanContext deploymentInfo) {
        Deployment deployment = ejbs.get(deploymentInfo.getDeploymentID());

        if (deployment == null) return null;

        return deployment.get(deploymentInfo);
    }


    public void contextEntered(ThreadContext oldContext, ThreadContext newContext) {
        BeanContext deploymentInfo = newContext.getBeanContext();
        if (deploymentInfo == null) return;

        EjbDeployment ejbDeployment = getEjbDeployment(deploymentInfo);

        if (ejbDeployment == null) return;

        // Geronimo call context is used to track old state that must be restored
        GeronimoCallContext geronimoCallContext = new GeronimoCallContext();

        // Demarcate component boundaries for connection tracking if we have a tracker
        TrackedConnectionAssociator trackedConnectionAssociator = ejbDeployment.getTrackedConnectionAssociator();
        if (trackedConnectionAssociator != null) {
            // create the connector context... this only works with a TrackedConnectionAssociator using lazy association
            ConnectorInstanceContext connectorContext = new ConnectorInstanceContextImpl(ejbDeployment.getUnshareableResources(),
                    ejbDeployment.getApplicationManagedSecurityResources());

            // Set connector context
            try {
                geronimoCallContext.oldConnectorContext = trackedConnectionAssociator.enter(connectorContext);
            } catch (ResourceException e) {
                log.error("Error while entering TrackedConnectionAssociator");
                return;
            }
        }

        // Get the jndi context
        Context jndiContext = deploymentInfo.getJndiEnc();
        geronimoCallContext.oldJndiContext = RootContext.getComponentContext();
        // Set the jndi context into Geronimo's root context
        RootContext.setComponentContext(jndiContext);

        // set the policy (security) context id
        geronimoCallContext.contextID = PolicyContext.getContextID();
        String moduleID = newContext.getBeanContext().getModuleID();
        PolicyContext.setContextID(moduleID);

        // set the default subject if needed
        if (ContextManager.getCurrentCaller() == null) {
            Subject defaultSubject = ejbDeployment.getDefaultSubject();

            if (defaultSubject != null) {
                ContextManager.setCallers(defaultSubject, defaultSubject);
                geronimoCallContext.clearCallers = true;
            }
        }

        // apply run as
        Subject runAsSubject = ejbDeployment.getRunAs();
        geronimoCallContext.callers = ContextManager.pushNextCaller(runAsSubject);

        newContext.set(GeronimoCallContext.class, geronimoCallContext);
    }

    public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
        BeanContext deploymentInfo = exitedContext.getBeanContext();
        if (deploymentInfo == null) return;

        EjbDeployment ejbDeployment = deploymentInfo.get(EjbDeployment.class);
        if (ejbDeployment == null) return;

        // Geronimo call context is used to track old state that must be restored
        GeronimoCallContext geronimoCallContext = exitedContext.get(GeronimoCallContext.class);
        if (geronimoCallContext == null) return;

        // reset run as
        ContextManager.popCallers(geronimoCallContext.callers);

        // reset default subject
        if (geronimoCallContext.clearCallers) {
            ContextManager.clearCallers();
        }

        //reset ContextID
        PolicyContext.setContextID(geronimoCallContext.contextID);

        // reset Geronimo's root jndi context
        RootContext.setComponentContext(geronimoCallContext.oldJndiContext);

        // reset old connector context
        TrackedConnectionAssociator trackedConnectionAssociator = ejbDeployment.getTrackedConnectionAssociator();
        if (trackedConnectionAssociator != null) {
            try {
                trackedConnectionAssociator.exit(geronimoCallContext.oldConnectorContext);
            } catch (ResourceException e) {
                log.error("Error while exiting TrackedConnectionAssociator");
            }
        }
    }

    private static final class Deployment {
        private final EjbDeployment geronimoDeployment;
        private final AtomicReference<Future<EjbDeployment>> initialized = new AtomicReference<Future<EjbDeployment>>();

        private Deployment(EjbDeployment geronimoDeployment) {
            this.geronimoDeployment = geronimoDeployment;
        }

        public EjbDeployment get(final BeanContext openejbDeployment) {
            try {
                // Has the deployment been initialized yet?

                // If there is a Future object in the AtomicReference, then
                // it's either been initialized or is being initialized now.
                Future<EjbDeployment> initializedRef = initialized.get();
                if (initializedRef != null) return initializedRef.get();

                // The deployment has not been initialized nor is being initialized

                // We will construct this FutureTask and compete with the
                // other threads for the right to initialize the deployment
                FutureTask<EjbDeployment> initializer = new FutureTask<EjbDeployment>(new Callable<EjbDeployment>() {
                    public EjbDeployment call() throws Exception {
                        return geronimoDeployment.initialize(openejbDeployment);
                    }
                });


                do {
                    // If our FutureTask was the one to win the slot
                    // than we are the ones responsisble for initializing
                    // the deployment while the others wait.
                    if (initialized.compareAndSet(null, initializer)) {
                        initializer.run();
                    }

                    // If we didn't win the slot and no other FutureTask
                    // has been set by a different thread, than we need
                    // to try again.
                } while ((initializedRef = initialized.get()) == null);


                // At this point we can safely return the initialized deployment
                return initializedRef.get();
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new IllegalStateException("EjbDeployment.initialize() interrupted", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("EjbDeployment.initialize() failed", e.getCause());
            }
        }
    }
    
    private static final class GeronimoCallContext {
        private Context oldJndiContext;
        private ConnectorInstanceContext oldConnectorContext;
        private boolean clearCallers;
        private Callers callers;
        private String contextID;
    }
}
