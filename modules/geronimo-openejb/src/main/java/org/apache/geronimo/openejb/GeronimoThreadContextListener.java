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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContextImpl;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoThreadContextListener implements ThreadContextListener {
    private static final Log log = LogFactory.getLog(GeronimoThreadContextListener.class);

    // A single stateless listener is used for Geronimo
    private static final GeronimoThreadContextListener instance = new GeronimoThreadContextListener();

    static {
        ThreadContext.addThreadContextListener(instance);
    }

    public static void init() {
        // do nothing.. the goal here is to kick off the onetime init above
    }

    private GeronimoThreadContextListener() {
    }

    public void contextEntered(ThreadContext oldContext, ThreadContext newContext) {
        CoreDeploymentInfo deploymentInfo = newContext.getDeploymentInfo();
        if (deploymentInfo == null) return;
        if (deploymentInfo.get(EjbDeployment.class) == null) {
	    synchronized (deploymentInfo) {
                if (deploymentInfo.get(EjbDeployment.class) == null) {
                    if (!deploymentInfo.isDestroyed()) {
                        try {
                            deploymentInfo.wait();
                        } catch (InterruptedException e) {
                        log.warn("Wait on deploymentInfo interrupted unexpectedly");
                        }
                    }
                }
            }
        } 
        EjbDeployment ejbDeployment = deploymentInfo.get(EjbDeployment.class);
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
        Context jndiContext = ejbDeployment.getComponentContext();
        geronimoCallContext.oldJndiContext = RootContext.getComponentContext();
        // Set the jndi context into Geronimo's root context
        RootContext.setComponentContext(jndiContext);

        // set the policy (security) context id
        geronimoCallContext.contextID = PolicyContext.getContextID();
        String moduleID = newContext.getDeploymentInfo().getModuleID();
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
        CoreDeploymentInfo deploymentInfo = exitedContext.getDeploymentInfo();
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

    private static final class GeronimoCallContext {
        private Context oldJndiContext;
        private ConnectorInstanceContext oldConnectorContext;
        private boolean clearCallers;
        private Callers callers;
        private String contextID;
    }
}
