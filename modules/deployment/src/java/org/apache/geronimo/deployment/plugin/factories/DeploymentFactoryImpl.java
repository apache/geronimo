/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment.plugin.factories;

import java.util.Collections;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.plugin.DeploymentManagerImpl;
import org.apache.geronimo.deployment.plugin.DisconnectedServer;
import org.apache.geronimo.deployment.plugin.local.LocalServer;
import org.apache.geronimo.deployment.plugin.application.EARConfigurationFactory;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

/**
 * Implementation of JSR88 DeploymentFactory.
 *
 * This will create a DeploymentManager using a local Geronimo kernel
 * to contain the GBeans that are responsible for deploying each module
 * type.
 * 
 * @version $Revision: 1.5 $ $Date: 2004/02/04 05:43:31 $
 */
public class DeploymentFactoryImpl implements DeploymentFactory {
    public static final String URI_PREFIX = "deployer:geronimo:";

    public String getDisplayName() {
        return "Geronimo";
    }

    public String getProductVersion() {
        return "0.1";
    }

    public boolean handlesURI(String uri) {
        return uri.startsWith(URI_PREFIX);
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            return null;
        }

        try {
            GBeanMBean server = new GBeanMBean(DisconnectedServer.GBEAN_INFO);
            return createManager(server);
        } catch (InvalidConfigurationException e) {
            throw (IllegalStateException) new IllegalStateException("Unable to create disconnected server").initCause(e);
        }
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            return null;
        }

        try {
            GBeanMBean server = new GBeanMBean(LocalServer.GBEAN_INFO);
            return createManager(server);
        } catch (InvalidConfigurationException e) {
            throw (IllegalStateException) new IllegalStateException("Unable to create disconnected server").initCause(e);
        }
    }

    private DeploymentManager createManager(GBeanMBean server) throws DeploymentManagerCreationException {
        Kernel kernel = new Kernel("geronimo.deployment", "geronimo.deployment");
        try {
            kernel.boot();
        } catch (Exception e) {
            throw (DeploymentManagerCreationException) new DeploymentManagerCreationException("Unable to boot embedded kernel").initCause(e);
        }

        GBeanMBean manager;
        try {
            ObjectName serverName = new ObjectName("geronimo.deployment:role=DeploymentServer");
            kernel.loadGBean(serverName, server);
            kernel.startGBean(serverName);

            ObjectName managerName = new ObjectName("geronimo.deployment:role=DeploymentManager");
            manager = new GBeanMBean(DeploymentManagerImpl.GBEAN_INFO);
            manager.setReferencePatterns("Server", Collections.singleton(serverName));

            // @todo for now lets hard code the deployers to use - ultimately this should use a predefined Configuration
            loadFactory(kernel, manager, "EARFactory", EARConfigurationFactory.class.getName());

            kernel.loadGBean(managerName, manager);
            kernel.startGBean(managerName);

        } catch (Exception e) {
            // this should not happen - we own this kernel!
            throw (IllegalStateException) new IllegalStateException("Unable to load DeploymentManager").initCause(e);
        }
        return (DeploymentManager) manager.getTarget();
    }

    private void loadFactory(Kernel kernel, GBeanMBean manager, String factory, String className) throws Exception {
        ObjectName earFactoryName = new ObjectName("geronimo.deployment:role="+factory);
        GBeanMBean earFactory = new GBeanMBean(className);
        kernel.loadGBean(earFactoryName, earFactory);
        kernel.startGBean(earFactoryName);
        manager.setReferencePatterns(factory, Collections.singleton(earFactoryName));
    }
}
