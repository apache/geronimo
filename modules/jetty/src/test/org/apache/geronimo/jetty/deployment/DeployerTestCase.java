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

import java.io.File;
import java.util.Collections;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.geronimo.deployment.plugin.DeploymentManagerImpl;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.LocalConfigStore;
import junit.framework.TestCase;

/**
 * Base class for web deployer test.
 * Handles setting up the deployment environment.
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/25 21:07:04 $
 */
public class DeployerTestCase extends TestCase {
    protected File configStore;
    protected Kernel kernel;
    protected ObjectName managerName;
    protected ObjectName serverName;
    private ObjectName warName;
    protected GBeanMBean managerGBean;
    protected DeploymentManager manager;
    protected WARConfigurationFactory warFactory;
    protected ClassLoader classLoader;
    protected DocumentBuilder parser;

    protected void setUp() throws Exception {
        classLoader = Thread.currentThread().getContextClassLoader();
        parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        configStore = new File(System.getProperty("java.io.tmpdir"), "config-store");
        configStore.mkdir();

        kernel = new Kernel("test", LocalConfigStore.GBEAN_INFO, configStore);
        kernel.boot();

        serverName = new ObjectName("geronimo.deployment:role=Server");

        warName = new ObjectName("geronimo.deployment:role=WARFactory");
        GBeanMBean warFactoryGBean = new GBeanMBean(WARConfigurationFactory.GBEAN_INFO);

        managerName = new ObjectName("geronimo.deployment:role=DeploymentManager");
        managerGBean = new GBeanMBean(DeploymentManagerImpl.GBEAN_INFO);
        managerGBean.setReferencePatterns("WARFactory", Collections.singleton(warName));
        managerGBean.setReferencePatterns("Server", Collections.singleton(serverName));

        kernel.loadGBean(warName, warFactoryGBean);
        kernel.startGBean(warName);
        kernel.loadGBean(managerName, managerGBean);
        kernel.startGBean(managerName);

        manager = (DeploymentManager) managerGBean.getTarget();
        warFactory = (WARConfigurationFactory) warFactoryGBean.getTarget();
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(managerName);
        kernel.unloadGBean(managerName);
        kernel.stopGBean(warName);
        kernel.unloadGBean(warName);
        kernel.shutdown();
        FileUtil.recursiveDelete(configStore);
    }
}
