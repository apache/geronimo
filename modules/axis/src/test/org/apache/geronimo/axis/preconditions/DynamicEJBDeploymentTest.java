/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis.preconditions;

import org.apache.geronimo.axis.AbstractTestCase;
import org.apache.geronimo.axis.testUtils.J2EEManager;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.openejb.deployment.OpenEJBModuleBuilder;

import javax.management.ObjectName;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

/**
 * <p>This test case show the infomation about openEJB that we assumed. And the
 * simmlier code code is used in the real code. As the OpenEJB is developing and
 * rapidly changing this test case act as a notifier for saying things has chaged</p>
 */
public class DynamicEJBDeploymentTest extends AbstractTestCase {
    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=ConnectionTracker");
    private Kernel kernel;
    private J2EEManager j2eeManager;

    /**
     * @param testName
     */
    public DynamicEJBDeploymentTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null) {
            str = ":org.apache.geronimo.naming";
        } else {
            str = str + ":org.apache.geronimo.naming";
        }
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        kernel = new Kernel("blah");
        kernel.boot();
        j2eeManager = new J2EEManager();
        j2eeManager.startJ2EEContainer(kernel);
    }

    public void testEJBJarDeploy() throws Exception {
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder();
        File jarFile = new File(outDir + "echo-jar/echo-ewsimpl.jar");
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{jarFile.toURL()}, oldCl);
        Thread.currentThread().setContextClassLoader(cl);
        File carFile = File.createTempFile("OpenEJBTest", ".car");
        try {
            EARConfigBuilder earConfigBuilder =
                    new EARConfigBuilder(new ObjectName(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName),
                            transactionManagerObjectName,
                            connectionTrackerObjectName,
                            null,
                            null,
                            null,
                            moduleBuilder,
                            moduleBuilder,
                            null,
                            null,
                            null,
                            null,
                            null);
            File unpackedDir = new File(tempDir, "OpenEJBTest-ear-Unpacked");
            JarFile jarFileModules = null;
            try {
                jarFileModules = new JarFile(jarFile);
                Object plan = earConfigBuilder.getDeploymentPlan(null, jarFileModules);
                earConfigBuilder.buildConfiguration(plan, jarFileModules, unpackedDir);
            } finally {
                if (jarFile != null) {
                    jarFileModules.close();
                }
            }
        } finally {
            carFile.delete();
        }
    }

    protected void tearDown() throws Exception {
        j2eeManager.stopJ2EEContainer(kernel);
    }
}

