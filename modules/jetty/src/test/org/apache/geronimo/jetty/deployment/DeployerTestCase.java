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
package org.apache.geronimo.jetty.deployment;

import java.util.Collections;
import javax.management.ObjectName;
import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.deployment.plugin.DeploymentManagerImpl;
import junit.framework.TestCase;

/**
 * Base class for web deployer test.
 * Handles setting up the deployment environment.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/22 04:44:44 $
 */
public class DeployerTestCase extends TestCase {
    protected Kernel kernel;
    protected ObjectName managerName;
    private ObjectName warName;
    protected GBeanMBean managerGBean;
    protected DeploymentManager manager;
    protected ClassLoader classLoader;

    protected void setUp() throws Exception {
        classLoader = Thread.currentThread().getContextClassLoader();
        kernel = new Kernel("test");
        kernel.boot();

        warName = new ObjectName("geronimo.deployment:role=WARFactory");
        GBeanMBean warFactory = new GBeanMBean(WARConfigurationFactory.GBEAN_INFO);
        kernel.loadGBean(warName, warFactory);
        kernel.startGBean(warName);

        managerName = new ObjectName("geronimo.deployment:role=DeploymentManager");
        managerGBean = new GBeanMBean(DeploymentManagerImpl.GBEAN_INFO);
        managerGBean.setEndpointPatterns("WARFactory", Collections.singleton(warName));
        kernel.loadGBean(managerName, managerGBean);
        kernel.startGBean(managerName);

        manager = (DeploymentManager) managerGBean.getTarget();
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(managerName);
        kernel.unloadGBean(managerName);
        kernel.stopGBean(warName);
        kernel.unloadGBean(warName);
        kernel.shutdown();
    }
}
